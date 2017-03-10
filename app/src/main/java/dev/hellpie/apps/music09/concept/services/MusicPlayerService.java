/*
 * Copyright 2017 Diego Rossi (@_HellPie)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.hellpie.apps.music09.concept.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.LongSparseArray;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Timer;

import dev.hellpie.apps.music09.concept.listeners.MediaPlayerListener;
import dev.hellpie.apps.music09.concept.listeners.MediaRetrieverListener;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Playlist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Song_DEPRECATED;
import dev.hellpie.apps.music09.concept.ui.activities.MainActivity;
import dev.hellpie.apps.music09.concept.utils.MessagingUtils;

/**
 * Music Player Service class.
 *
 * This class is @Deprecated, it is now integrated in PlayerActivity.
 * This class is queued for planned rewriting and updating for the future major release of
 * the application.
 */
@Deprecated
public class MusicPlayerService
		extends Service
		implements MediaPlayerListener,
			MediaRetrieverListener,
			AudioManager.OnAudioFocusChangeListener {

	/**
	 * Represent the type of message to send to the Service.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ACTION_PLAY, ACTION_PAUSE, ACTION_TOGGLE_PLAYBACK, ACTION_STOP, ACTION_SEEK,
			ACTION_REWIND, ACTION_SKIP, ACTION_GO_BACK, MAGIC_CONNECT, MAGIC_DISCONNECT})
	public @interface Action {}
	public static final int ACTION_PLAY = 1;
	public static final int ACTION_PAUSE = 2;
	public static final int ACTION_TOGGLE_PLAYBACK = 3;
	public static final int ACTION_STOP = 4;
	public static final int ACTION_SEEK = 5;
	public static final int ACTION_REWIND = 6;
	public static final int ACTION_SKIP = 7;
	public static final int ACTION_GO_BACK = 8;
	public static final int MAGIC_CONNECT = 9;
	public static final int MAGIC_DISCONNECT = 10;

	/**
	 * Defines the various states the Service can be in.
	 * These states might be different from the precise state of the player, for example:
	 * When in STATE_READY the player might be either already be playing or being paused while
	 * in STATE_PAUSED the player might be fully stopped, but the Service ready to restart it.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({STATE_RETRIEVING, STATE_STOPPED, STATE_PREPARING, STATE_READY, STATE_PAUSED})
	public @interface ServiceState {}
	public static final int STATE_RETRIEVING = 0x0001; // Retrieving albums and songs
	public static final int STATE_STOPPED = 0x0002; // Not ready to play
	public static final int STATE_PREPARING = 0x0003; // Preparing to play
	public static final int STATE_READY = 0x0004; // Ready to play
	public static final int STATE_PAUSED = 0x0005; // Paused playing, ready to resume

	/**
	 * Defines the state of audio focus for the service.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({STATE_NORMAL, STATE_DUCKING, STATE_MUTING})
	public @interface AudioState {}
	public static final int STATE_NORMAL = 0x0010; // Audio focus present
	public static final int STATE_DUCKING = 0x0020; // No audio focus, but playing ducked
	public static final int STATE_MUTING = 0x0030; // No audio focus, can't play, can't duck either

	/**
	 * Defines why the music stopped playing.
	 * This is relevant pretty much only when in STATE_PAUSED.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({REASON_USER, REASON_FOCUS})
	public @interface PauseReason {}
	public static final int REASON_USER = 0x0100;
	public static final int REASON_FOCUS = 0x0200;

	// The ID of the Notification controlled by this Service.
	// This number is unique to our application.
	private static final int NOTIFICATION_ID = 1337; // H4x0R is always the best value

	// Volume ducking is when volume is lowered while playing other audios, for example
	// when music players lower the volume when a notification arrives, then they set it
	// back to normal when the notification sound is done playing.
	private static final float VOLUME_DUCKING = 0.1f;

	/**
	 * Stores the Messenger the client will use to send Actions to this Service.
	 */
	private final Messenger messenger = new Messenger(new ActionHandler(this));

	/**
	 * Stores the Messenger used to send Events back to the client.
	 */
	@Nullable
	private Messenger clientMessenger;
	private static final Object clientMessengerLock = new Object();

	// Current State Machine data
	private @ServiceState int serviceState = STATE_RETRIEVING;
	private @AudioState int audioState = STATE_MUTING;
	private @PauseReason int pauseReason = REASON_FOCUS;

	// Media Player data
	private MediaPlayer mediaPlayer;

	// Service data
	private static PowerManager.WakeLock wakeLock; // To avoid Deep Sleep
	private Timer eventTimer;
	private Looper serviceLooper;

	// Music related data
	private LongSparseArray<Song_DEPRECATED> playingQueue = new LongSparseArray<>(100);
	private LongSparseArray<Song_DEPRECATED> playedDeque = new LongSparseArray<>(100);
	int currentSong = 0; // Current playing song index
	private long pauseTime = Long.MAX_VALUE; // Point in the song where we last paused playback

	// System service references
	AudioManager audioManager;
	NotificationManagerCompat notificationManager;


	public MusicPlayerService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
//
//		// Load the music library
//		new MediaRetriever(getApplicationContext()).loadLibraryAsync(this);
//
//		// Initialize the AudioManager, which is a System-wide Singleton
//		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//		// Get a default album art for when the song does not specify one
//		defaultAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.album_art);

		// Initialize all the stuff the Service will need

		// Load the MediaPlayer and configure it for this Service.
		initializeMediaPlayer();

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Music Playback data
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false; // We don't want to kill the service!
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//
//		// Cleanup states, this is basically the GC of the Service's functions
//		serviceState = STATE_STOPPED;
//		releaseResources(true);
//		abandonAudioFocus();

		// Cleanup certain states since they are stored outside this instance

		// Drop audio focus back to the system
		if(audioManager != null) audioManager.abandonAudioFocus(this);

		mediaPlayer.stop();
		mediaPlayer.reset();
		mediaPlayer.release();

		serviceState = STATE_STOPPED;
	}

	@Override
	public void onMediaLoadingComplete() {
//
//		// We are done retrieving so move into STOPPED
//		serviceState = STATE_STOPPED;
//
//		if(resumePlayback) {
//			requestAudioFocus();
//			// play song -> last song || play song -> random(?) song
//		}
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		mediaPlayer.start();
		serviceState = STATE_READY;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		if(playingQueue.size() == 0) {
			for(int i = 0; i < playedDeque.size(); i++) {
				Song_DEPRECATED song = playedDeque.valueAt(i);
				if(song ==  null) {
					stopSelf();
					return;
				}
				playingQueue.append(song.getId(), song);
			}
		}

		serviceState = STATE_READY;
		nextSong();
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		mediaPlayer.reset();
		return false;
	}

	@Override
	public void onAudioFocusChange(int i) {
//		switch(i) {
//			case AudioManager.AUDIOFOCUS_GAIN:
//				audioState = STATE_NORMAL;
//				if(serviceState != STATE_READY) return; // Don't restart player, no changes
//			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//				audioState = STATE_DUCKING;
//				if(player == null || !player.isPlaying()) return; // Don't restart player, no changes
//			case AudioManager.AUDIOFOCUS_LOSS:
//			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//				audioState = STATE_MUTING;
//				if(player == null || !player.isPlaying()) return; // Don't restart player, no changes
//				break;
//			default:
//				return; // We don't care about other cases, these are the only one affecting playback
//		}
//
//		// Always update the player (or create it) if the State Machine changed
//		refreshMediaPlayer();
		switch(i) {
			case AudioManager.AUDIOFOCUS_GAIN:
				initializeMediaPlayer();
				if(audioState != STATE_NORMAL) {
					audioState = STATE_NORMAL;
					resumePlaying();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				audioState = STATE_MUTING;
				pause();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if(mediaPlayer != null) mediaPlayer.setVolume(VOLUME_DUCKING, VOLUME_DUCKING);
				audioState = STATE_DUCKING;
				break;
			default:
				break;
		}
	}

	// MediaPlayer Management
	private void resumePlaying() {
		if(serviceState == STATE_STOPPED || serviceState == STATE_RETRIEVING) return;

		mediaPlayer.start();
		mediaPlayer.setVolume(1.0f, 1.0f);

		serviceState = STATE_READY;
	}

	private void toggle() {
		if(mediaPlayer.isPlaying()) {
			pause();
		} else {
			play(null);
		}
	}

	private void play(Object object) {
		if(mediaPlayer.isPlaying()) return;

		if(object == null) return;

		Class objClass = object.getClass();
		if(objClass.isInstance(Song_DEPRECATED.class)) {
			playingQueue.clear();
			playedDeque.clear();
			playingQueue.append(((Song_DEPRECATED)object).getId(), (Song_DEPRECATED) object);
		} else if(objClass.isInstance(Album_DEPRECATED.class)) {
			playingQueue.clear();
			playedDeque.clear();
			List<Song_DEPRECATED> songs = MediaLibrary.getSongs((Album_DEPRECATED) object);
			for(Song_DEPRECATED song : songs) {
				playingQueue.append(song.getId(), song);
			}
		} else if(objClass.isInstance(Artist_DEPRECATED.class)) {
			playingQueue.clear();
			playedDeque.clear();
			List<Song_DEPRECATED> songs = MediaLibrary.getSongs((Album_DEPRECATED) object);
			for(Song_DEPRECATED song : songs) {
				playingQueue.append(song.getId(), song);
			}
		} else if(objClass.isInstance(Playlist_DEPRECATED.class)) {
			playingQueue.clear();
			playedDeque.clear();
			List<Song_DEPRECATED> songs = MediaLibrary.getSongs();
			for(Song_DEPRECATED song: songs) {
				playingQueue.append(song.getId(), song);
			}
		}

		resumePlaying();
	}

	private void pause() {
		if(!mediaPlayer.isPlaying()) return;

		mediaPlayer.pause();
		notifyEvent(MainActivity.EVENT_PAUSED);
	}

	private void stop() {
		//
	}

	@SuppressWarnings("UnusedParameters")
	private void seek(int arg1) {
		//
	}

	private void skip() {
		nextSong();
		notifyEvent(MainActivity.EVENT_CHANGED, playingQueue.valueAt(currentSong));
	}

	private void rewind() {
		if(mediaPlayer.getCurrentPosition() >= 100) previous();
		mediaPlayer.seekTo(0);

		notifyEvent(MainActivity.EVENT_SEEK, 0);
	}

	private void previous() {
		//
	}

	private void nextSong() {
		serviceState = STATE_READY;
		//noinspection ConstantConditions
		if(serviceState != STATE_READY && serviceState != STATE_PAUSED) return;

		if(currentSong == playingQueue.size() - 1) {
			currentSong = 0;
		} else {
			currentSong++;
		}

		try {
			Song_DEPRECATED song = playingQueue.valueAt(currentSong);
			if(song == null) {
				stopSelf();
				return;
			}
			mediaPlayer.setDataSource(song.getLocation());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void updateNotification() {
		//
	}

	private void removeNotification() {
		//
	}

	private void setServiceState(@ServiceState int state) {
		if(state == serviceState) return;
		serviceState = state;
	}

	private void setAudioState(@AudioState int state) {
		if(state == audioState) return;
		audioState = state;
	}

	private void setPauseReason(@PauseReason int reason) {
		if(reason == pauseReason) return;
		pauseReason = reason;
	}

	private void refreshMediaPlayer() {
		//
	}

	private void initializeMediaPlayer() {
		if(mediaPlayer != null) { // Avoid rebuilding everything if possible
			mediaPlayer.reset();
			return;
		}

		// Initialize the MediaPlayer instance for this Service, it might be destroyed by us later
		mediaPlayer = new MediaPlayer();

		// WakeLocks avoid the CPU to go to sleep when the screen is off so that music keeps playing
		mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		// Set Listeners for the MediaPlayer to know what's happening to it
		mediaPlayer.setOnPreparedListener(this); // Ready to play
		mediaPlayer.setOnCompletionListener(this); // Done playing
		mediaPlayer.setOnErrorListener(this); // Whoopsies
	}

	private void notifyEvent(@MainActivity.Event int event) {
		notifyEvent(event, null);
	}

	private void notifyEvent(@MainActivity.Event int event, Object attachment) {
		MessagingUtils.send(clientMessenger, event, attachment);
	}

	private static class ActionHandler extends LeakSafeHandler<MusicPlayerService> {

		/*package*/ ActionHandler(@NonNull MusicPlayerService referenced) {
			super(referenced);
		}

		@Override
		public boolean handleMessage(Message message, MusicPlayerService reference) {
			switch(message.what) {
				case ACTION_TOGGLE_PLAYBACK:
					if(reference != null) reference.toggle();
					break;
				case ACTION_PLAY:
					if(reference != null) reference.play(message.obj);
					break;
				case ACTION_PAUSE:
					if(reference != null) reference.pause();
					break;
				case ACTION_STOP:
					if(reference != null) reference.stop();
					break;
				case ACTION_SEEK:
					if(reference != null) reference.seek(message.arg1);
					break;
				case ACTION_SKIP:
					if(reference != null) reference.skip();
					break;
				case ACTION_REWIND:
					if(reference != null) reference.rewind();
					break;
				case ACTION_GO_BACK:
					if(reference != null) reference.previous();
					break;
				case MAGIC_CONNECT:
					synchronized(clientMessengerLock) {
						if(reference != null) reference.clientMessenger = message.replyTo;
					}
					break;
				case MAGIC_DISCONNECT:
					synchronized(clientMessengerLock) {
						if(reference != null) reference.clientMessenger = null;
					}
					break;
				default:
					return false;
			}

			return true;
		}
	}
}
