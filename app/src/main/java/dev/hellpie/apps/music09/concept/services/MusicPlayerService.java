/*
 * Copyright 2016 Diego Rossi (@_HellPie)
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.listeners.MediaPlayerListener;
import dev.hellpie.apps.music09.concept.listeners.MediaRetrieverListener;
import dev.hellpie.apps.music09.concept.media.MediaBindingLayer;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.MediaRetriever;
import dev.hellpie.apps.music09.concept.media.models.Song;

public class MusicPlayerService
		extends Service
		implements MediaPlayerListener,
			MediaRetrieverListener,
			AudioManager.OnAudioFocusChangeListener {

	public static final String BUNDLE_KEY_MAGIC = "dev.hellpie.apps.music09.concept.services.MAGIC";

	// TODO: Add docs and stuff for @StringDef
	@Retention(RetentionPolicy.SOURCE)
	@StringDef({_ACTION_PLAY, _ACTION_PAUSE, _ACTION_TOGGLE_PLAYBACK, _ACTION_STOP, _ACTION_REWIND, _ACTION_SKIP, _ACTION_GO_BACK})
	public @interface _Action {}
	// TODO: Add docs about naming convention and why it's needed
	public static final String _ACTION_PLAY = "dev.hellpie.apps.music09.ACTION_PLAY";
	public static final String _ACTION_PAUSE = "dev.hellpie.apps.music09.ACTION_PAUSE";
	public static final String _ACTION_TOGGLE_PLAYBACK = "dev.hellpie.apps.music09.ACTION_TOGGLE_PLAYBACK";
	public static final String _ACTION_STOP = "dev.hellpie.apps.music09.ACTION_STOP";
	public static final String _ACTION_REWIND = "dev.hellpie.apps.music09.ACTION_REWIND";
	public static final String _ACTION_SKIP = "dev.hellpie.apps.music09.ACTION_SKIP";
	public static final String _ACTION_GO_BACK = "dev.hellpie.apps.music09.ACTION_GO_BACK";

	// TODO: Add docs and stuff for @IntDef
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
	@IntDef({STATE_NORMAL, STATE_DUCKING, STATE_MUTED})
	public @interface AudioState {}
	public static final int STATE_NORMAL = 0x0010; // Audio focus present
	public static final int STATE_DUCKING = 0x0020; // No audio focus, but playing ducked
	public static final int STATE_MUTED = 0x0030; // No audio focus, can't play, can't duck either

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

	private MediaPlayer player;
	private AudioManager audioManager;

	private Bitmap defaultAlbumArt;

	// State Machine current info
	private @ServiceState int serviceState = STATE_RETRIEVING;
	private @AudioState int audioState = STATE_MUTED;
	private @PauseReason int pauseReason = REASON_FOCUS;

	private Song currentSong;
	private boolean resumePlayback;

	private List<Song> playingQueue = new ArrayList<>();
	private List<Song> playedSongs = new ArrayList<>();

	public MusicPlayerService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Load the music library
		new MediaRetriever(getApplicationContext()).loadLibraryAsync(this);

		// Initialize the AudioManager, which is a System-wide Singleton
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Get a default album art for when the song does not specify one
		defaultAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.album_art);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String command = intent.getAction();

		Log.d(getClass().getSimpleName(), String.format("Started with: %s", command));

		// Unfortunately we cannot switch() the intent's command because String is a really
		// interesting fucked up concept for a primitive type...
		if(command.equals(_ACTION_PLAY)) {
			MediaBindingLayer.get().scheduleActionChange(_ACTION_PLAY);
			play();
		} else if(command.equals(_ACTION_PAUSE)) {
			MediaBindingLayer.get().scheduleActionChange(_ACTION_PAUSE);
			pause();
		} else if(command.equals(_ACTION_TOGGLE_PLAYBACK)) {
			if(serviceState == STATE_PAUSED || serviceState == STATE_STOPPED) {
				MediaBindingLayer.get().scheduleActionChange(_ACTION_PLAY);
				play();
			} else {
				MediaBindingLayer.get().scheduleActionChange(_ACTION_PAUSE);
				pause();
			}
		} else if(command.equals(_ACTION_STOP)) {
			if(serviceState == STATE_PAUSED || serviceState == STATE_READY) {
				MediaBindingLayer.get().scheduleActionChange(_ACTION_STOP);
				serviceState = STATE_STOPPED;
				releaseResources(true);
				abandonAudioFocus();
				stopSelf(); // Close the Service
			}
		} else if(command.equals(_ACTION_SKIP)) {
			if(serviceState == STATE_READY || serviceState == STATE_PAUSED) {
				MediaBindingLayer.get().scheduleActionChange(_ACTION_SKIP);
				requestAudioFocus();
				playNext(null);
			}
		} else if(command.equals(_ACTION_REWIND)) {
			if(serviceState == STATE_READY || serviceState == STATE_PAUSED) {
				MediaBindingLayer.get().scheduleActionChange(_ACTION_REWIND);
				int percentage = player.getCurrentPosition() / player.getDuration();
				if(player.getCurrentPosition() == 0 || percentage < 0.05) {
					// Rewind one song and reload it
					playingQueue.add(playedSongs.remove(playedSongs.size() - 1));
					currentSong = playingQueue.remove(playingQueue.size() - 1);
					requestAudioFocus();
					playNext(null);
				} else {
					player.seekTo(0);
				}
			}
		} else if(command.equals(_ACTION_GO_BACK)) {
			MediaBindingLayer.get().scheduleActionChange(_ACTION_GO_BACK);
			// Rewind one song and reload it
			playingQueue.add(playedSongs.remove(playedSongs.size() - 1));
			currentSong = playingQueue.remove(playingQueue.size() - 1);
			playNext(null);
		}

		return START_NOT_STICKY; // Tell the system we don't want this service to be auto-restarted
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Cleanup states, this is basically the GC of the Service's functions
		serviceState = STATE_STOPPED;
		releaseResources(true);
		abandonAudioFocus();
	}

	@Override
	public void onMediaLoadingComplete() {

		// We are done retrieving so move into STOPPED
		serviceState = STATE_STOPPED;

		if(resumePlayback) {
			requestAudioFocus();
			// play song -> last song || play song -> random(?) song
		}
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {

	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {

	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		return false;
	}

	@Override
	public void onAudioFocusChange(int i) {
		switch(i) {
			case AudioManager.AUDIOFOCUS_GAIN:
				audioState = STATE_NORMAL;
				if(serviceState != STATE_READY) return; // Don't restart player, no changes
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				audioState = STATE_DUCKING;
				if(player == null || !player.isPlaying()) return; // Don't restart player, no changes
			case AudioManager.AUDIOFOCUS_LOSS:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				audioState = STATE_MUTED;
				if(player == null || !player.isPlaying()) return; // Don't restart player, no changes
				break;
			default:
				return; // We don't care about other cases, these are the only one affecting playback
		}

		// Always update the player (or create it) if the State Machine changed
		updateMediaPlayer();
	}

	/**
	 * Makes sure the MediaPlayer instance exists, is clean and ready for a new session.
	 */
	private void initMediaPlayer() {
		if(player != null) {
			player.reset();
			return;
		}

		// Need to create a new MediaPlayer

		// The MediaPlayer needs to be set to partially wake lock because when the screen is off
		// especially in API23+ (Doze Mode) the system tries to freeze all the processes
		// to save battery. This stops the MediaPlayer from playing music, which is an unpleasant
		// experience for the average user.
		player = new MediaPlayer();
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	/**
	 * Updates the MediaPlayer to match the new audio state.
	 */
	private void updateMediaPlayer() {
		if(player == null) initMediaPlayer(); // Just in case we lost it somewhere

		if(audioState == STATE_MUTED && player.isPlaying()) {
			player.pause(); // We simply won't be played by the system anymore so pause 'til later
			return; // Stop here otherwise the last if block will resume the player
		} else if(audioState == STATE_DUCKING) {
			player.setVolume(VOLUME_DUCKING, VOLUME_DUCKING);
		} else {
			player.setVolume(1.0f, 1.0f); // Restore normal volume in case we were ducking before
		}

		if(!player.isPlaying()) player.start(); // Restore normal functions since we aren't MUTED
	}

	private void play() {
		if(serviceState == STATE_RETRIEVING) {
			resumePlayback = true;
			return;
		}

		requestAudioFocus();

		if(serviceState == STATE_STOPPED) {
			playNext(null);
		} else if(serviceState == STATE_PAUSED) {
			serviceState = STATE_READY;
			// TODO: Media Player notification
			startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(getApplicationContext()).build());
			updateMediaPlayer();
		}
	}

	private void pause() {
		if(serviceState == STATE_RETRIEVING) {
			resumePlayback = false;
			return;
		}

		if(serviceState == STATE_READY) {
			serviceState = STATE_PAUSED;
			player.pause();
			releaseResources(false);
		}
	}

	/**
	 * Plays a song, first trying to play the one defined by uri.
	 * If uri is null then check if we have played a song before this one. If we did play the song
	 * after that one, otherwise play a random song.
	 * @param uri The URI of the song to play
	 */
	private void playNext(String uri) {

		// We are prepared to start playing
		serviceState = STATE_STOPPED;

		// Clean up and prepare for a new session
		releaseResources(false);

		try {

			updateMediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);

			if(uri != null) { // Play from the given URI
				player.setDataSource(getApplicationContext(), Uri.parse(uri));
				currentSong = new Song.Builder()
						.withLocation(uri)
						.build();
			} else {

				if(currentSong == null) {
					currentSong = MediaLibrary.getRandomSong();
					if(currentSong == null) {
						stopSelf();
						return;
					}
				} else {
					if(playingQueue.size() < 1) {
						playingQueue = MediaLibrary.getSongs();
						playedSongs.clear();
					}

					if(playingQueue.size() >= 1) {
						currentSong = playingQueue.remove(playingQueue.size() - 1);
						playedSongs.add(currentSong);
					} else {
						// stop player
						return;
					}
				}

				if(currentSong == null) {
					// stop player
					return;
				}

				player.setDataSource(getApplicationContext(), Uri.parse(currentSong.getLocation()));
				MediaBindingLayer.get().scheduleSongChange(currentSong);
			}

		} catch(IOException ignored) {

		}
	}

	/**
	 * Cleans up the Service.
	 * This will be called only when the Service does not need to run anymore.
	 * @param wipe If the method should also fully release and delete the MediaPlayer
	 */
	private void releaseResources(boolean wipe) {

		// Stop running in the foreground and kill the notifications controlled by this Service
		stopForeground(true);

		if(wipe && player != null) {
			player.reset();
			player.release();
			player = null; // Destroy the player after cleaning up its resources and states
		}
	}

	/**
	 * Try to obtain audio focus.
	 */
	public void requestAudioFocus() {
		if(audioState == STATE_NORMAL) return; // We already have focus

		// Ask the system to give us focus
		boolean result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(
				this,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN
		);

		if(result) audioState = STATE_NORMAL;
	}

	/**
	 * Try to abandon audio focus and release it to other applications.
	 */
	public void abandonAudioFocus() {
		if(audioState != STATE_NORMAL) return;
		if(AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)) {
			audioState = STATE_MUTED;
		}
	}

	private static class ActionHandler extends LeakSafeHandler<MusicPlayerService> {

		/*package*/ ActionHandler(@NonNull MusicPlayerService referenced) {
			super(referenced);
		}

		@Override
		public boolean handleMessage(Message message, MusicPlayerService reference) {
			switch(message.what) {
				case ACTION_TOGGLE_PLAYBACK:
					if(reference == null) break;
					//
					break;
				case ACTION_PLAY:
					if(reference == null) break;
					//
					break;
				case ACTION_PAUSE:
					if(reference == null) break;
					//
					break;
				case ACTION_STOP:
					if(reference == null) break;
					//
					break;
				case ACTION_SEEK:
					if(reference == null) break;
					//
					break;
				case ACTION_SKIP:
					if(reference == null) break;
					//
					break;
				case ACTION_REWIND:
					if(reference == null) break;
					//
					break;
				case ACTION_GO_BACK:
					if(reference == null) break;
					//
					break;
				case MAGIC_CONNECT:
					if(reference == null) break;
					reference.clientMessenger = message.replyTo;
					break;
				case MAGIC_DISCONNECT:
					if(reference == null) break;
					reference.clientMessenger = null;
					break;
				default:
					return false;
			}

			return true;
		}
	}
}
