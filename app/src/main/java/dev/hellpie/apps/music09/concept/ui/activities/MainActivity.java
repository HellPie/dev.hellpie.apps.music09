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

package dev.hellpie.apps.music09.concept.ui.activities;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.MediaRetriever;
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Playlist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Song_DEPRECATED;
import dev.hellpie.apps.music09.concept.services.BaseServiceConnection;
import dev.hellpie.apps.music09.concept.services.LeakSafeHandler;
import dev.hellpie.apps.music09.concept.services.MusicPlayerService;
import dev.hellpie.apps.music09.concept.ui.resources.AnimatedPlayPauseDrawable;
import dev.hellpie.apps.music09.concept.ui.views.SquareView;
import dev.hellpie.apps.music09.concept.utils.GraphicUtils;
import dev.hellpie.apps.music09.concept.utils.MessagingUtils;
import dev.hellpie.apps.music09.concept.utils.UIUtils;

/**
 * Main Activity class.
 *
 * This class is @Deprecated, it is now replaced by PlayerActivity. This class is queued for planned
 * rewriting and updating for the future major release of the application.
 */
@Deprecated
public class MainActivity extends AppCompatActivity
		implements View.OnClickListener,
		SeekBar.OnSeekBarChangeListener,
		MusicChooserFragment.OnMusicChosenListener {

	private final static String MUSIC_FRAGMENT = "dev.hellpie.apps.music09.concept.ui.activities.MainActivity.MUSIC_FRAGMENT";

	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({EVENT_PLAYING, EVENT_PAUSED, EVENT_SEEK, EVENT_CHANGED, EVENT_STOPPED, MAGIC_ALERT})
	public @interface Event {}
	public static final int EVENT_PLAYING = 0;
	public static final int EVENT_PAUSED = 1;
	public static final int EVENT_SEEK = 2;
	public static final int EVENT_CHANGED = 3;
	public static final int EVENT_STOPPED = 4;
	public static final int MAGIC_ALERT = 5;

	private final BaseServiceConnection serviceConnection = new BackwardsServiceConnection(this);
	private final Messenger messenger = new Messenger(new EventHandler(this));

	private Toolbar toolbar;

	private VectorDrawableCompat playingBackgroundDrawable;
	private ImageView playingBackground;

	private SquareView albumArtSquareView;

	private TextView songNameTextView;
	private TextView songInfoTextView;

	private SeekBar songTimelineSeekBar;
	private TextView elapsedTimeTextView;
	private TextView totalTimeTextView;

	private FloatingActionButton playPauseSongFAB;
	private ImageButton prevSongButton;
	private ImageButton nextSongButton;

	private AnimatedPlayPauseDrawable playPauseDrawable;


	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, MusicPlayerService.class);
		bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// AppCompat ActionBar as Activity's Toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				// Show a DialogFragment and load it with the list of all the albums
				MusicChooserFragment.newInstance(MusicChooserFragment.LIST_ALBUMS, MainActivity.this)
						.show(MainActivity.this.getSupportFragmentManager(), MUSIC_FRAGMENT);
			}
		});

		// Remove title from Toolbar since we have our own
		ActionBar supportActionBar = getSupportActionBar();
		if(supportActionBar != null) supportActionBar.setDisplayShowTitleEnabled(false);

		// Store all the View instances from the UI (I usually use ButterKnife, damn good library)
		this.toolbar = toolbar;
		albumArtSquareView = (SquareView) findViewById(R.id.album_art);
		songNameTextView = (TextView) findViewById(R.id.song_title);
		songInfoTextView = (TextView) findViewById(R.id.song_info);
		songTimelineSeekBar = (SeekBar) findViewById(R.id.song_seek_bar);
		elapsedTimeTextView = (TextView) findViewById(R.id.song_seek_time_current);
		totalTimeTextView = (TextView) findViewById(R.id.song_seek_time_total);
		playPauseSongFAB = (FloatingActionButton) findViewById(R.id.controls_pause);
		prevSongButton = (ImageButton) findViewById(R.id.controls_previous);
		nextSongButton = (ImageButton) findViewById(R.id.controls_next);

		// Apply the fancy animated button which took me 2h to fix on the Play/Pause FAB
		playPauseDrawable = new AnimatedPlayPauseDrawable(this);
		playPauseSongFAB.setImageDrawable(playPauseDrawable);

		// Prepare the fancy background for later usage
		playingBackgroundDrawable = VectorDrawableCompat.create(getResources(), R.drawable.bg_player, getTheme());
		playingBackground = (ImageView) findViewById(R.id._activity_main_background);
		playingBackground.setBackground(playingBackgroundDrawable);

		// Set all the Listeners, ButterKnife handles clicks via @OnClick, but not SeekBarChanged
		playPauseSongFAB.setOnClickListener(this);
		prevSongButton.setOnClickListener(this);
		nextSongButton.setOnClickListener(this);
		songTimelineSeekBar.setOnSeekBarChangeListener(this);

		// Set custom onClickListener to open music choosers
		songInfoTextView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				MusicChooserFragment.newInstance(MusicChooserFragment.LIST_AUTHORS, MainActivity.this)
						.show(MainActivity.this.getSupportFragmentManager(), MUSIC_FRAGMENT);
				return true;
			}
		});

		songNameTextView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				MusicChooserFragment.newInstance(MusicChooserFragment.LIST_SONGS, MainActivity.this)
						.show(MainActivity.this.getSupportFragmentManager(), MUSIC_FRAGMENT);
				return true;
			}
		});

		albumArtSquareView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				MusicChooserFragment.newInstance(MusicChooserFragment.LIST_ALBUMS, MainActivity.this)
						.show(MainActivity.this.getSupportFragmentManager(), MUSIC_FRAGMENT);
				return true;
			}
		});

		// Update the colors otherwise the UI would look half transparent and generally weird
		updateColors();

		// Bind the Activity to the Service
//		MediaBindingLayer.get().setListener(this); TODO: Fix this

		// TODO: Cleanup debug code
		final MediaRetriever mediaRetriever = new MediaRetriever(this);
		mediaRetriever.loadLibrary();

		Song_DEPRECATED song = null;
		for(Song_DEPRECATED track : MediaLibrary.getSongs()) {
			if(track.getAlbumName().toLowerCase().contains("Sun".toLowerCase())) song = track;
		}

		if(song != null) MainActivity.this.onPlayingSongChanged(song);

		executeAction(MusicPlayerService.ACTION_PLAY, new Playlist_DEPRECATED());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		executeAction(MusicPlayerService.MAGIC_DISCONNECT);
		unbindService(serviceConnection);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_HEADSETHOOK:
				executeAction(MusicPlayerService.ACTION_TOGGLE_PLAYBACK);
				animatePlayPauseFAB();
				return true;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				executeAction(MusicPlayerService.ACTION_SKIP);
				return true;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				executeAction(MusicPlayerService.ACTION_REWIND);
				return true;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				executeAction(MusicPlayerService.ACTION_GO_BACK);
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.controls_pause:
				boolean isPlay = playPauseDrawable.isPlay();
				executeAction(isPlay ? MusicPlayerService.ACTION_PLAY : MusicPlayerService.ACTION_PAUSE);
				animatePlayPauseFAB();
				break;
			case R.id.controls_next:
				executeAction(MusicPlayerService.ACTION_SKIP);
				break;
			case R.id.controls_previous:
				executeAction(MusicPlayerService.ACTION_REWIND);
				break;
			default:
				break;
		}
	}

	@Override
	public void onMusicChosen(Object choice) {
		if(choice instanceof Song_DEPRECATED) {
			// Song_DEPRECATED chosen
			onPlayingSongChanged((Song_DEPRECATED) choice);
		} else if(choice instanceof Album_DEPRECATED) {
			//

			//TODO: Remove debug code
			onPlayingSongChanged(MediaLibrary.getSongs((Album_DEPRECATED) choice).get(0));
		} else //noinspection StatementWithEmptyBody
			if(choice instanceof Artist_DEPRECATED) {
			//
		} else //noinspection StatementWithEmptyBody
			if(choice instanceof Playlist_DEPRECATED) {
			// Playlist_DEPRECATED contains all the songs, so just play them all randomly
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int pos, boolean user) {
		//
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { /* SeekBar.OnSeekBarChangeListener */ }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { /* SeekBar.OnSeekBarChangeListener */ }

	private void executeAction(@MusicPlayerService.Action int action) {
		executeAction(action, null);
	}

	private void executeAction(@MusicPlayerService.Action int action, Object attachment) {
		Messenger messenger = serviceConnection.getMessenger();
		if(messenger == null || !serviceConnection.isConnected()) return;
		MessagingUtils.send(messenger, action, attachment);
	}

	private void updateColors() {
		int fallbackColor;
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			fallbackColor = getResources().getColor(R.color.colorAccent, getTheme());
		} else {
			//noinspection deprecation
			fallbackColor = getResources().getColor(R.color.colorAccent);
		}

		updateColors(GraphicUtils.extractVibrantColor(albumArtSquareView.getDrawable(), fallbackColor));
	}

	private void updateColors(int color) {
		int colorPrimaryDark = GraphicUtils.darken(color, 0.20f);
		int colorPrimaryLight = GraphicUtils.lighten(color, 0.20f);

		// TODO: Use ColorUtils.calculateLuminance() to avoid colors that are too light

		// Start applying the new colors

		getWindow().setStatusBarColor(colorPrimaryDark);

		toolbar.setBackgroundColor(color);

		playingBackgroundDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		playingBackground.invalidateDrawable(playingBackgroundDrawable); // Cache keeps old ColorFilter

		songTimelineSeekBar.setProgressTintList(ColorStateList.valueOf(colorPrimaryLight));
		songTimelineSeekBar.setIndeterminateTintList(ColorStateList.valueOf(colorPrimaryLight));
		songTimelineSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(colorPrimaryLight));
		songTimelineSeekBar.setThumbTintList(ColorStateList.valueOf(colorPrimaryLight));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			songTimelineSeekBar.setForegroundTintList(ColorStateList.valueOf(colorPrimaryLight));
		}

		// TODO: Fallback color? What if we fail to set a color?
		prevSongButton.setColorFilter(colorPrimaryLight, PorterDuff.Mode.MULTIPLY);
		nextSongButton.setColorFilter(colorPrimaryLight, PorterDuff.Mode.MULTIPLY);
		playPauseSongFAB.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryLight));
	}

	// TODO: Clean up onPlayingSongChanged(Song_DEPRECATED) { ... } (View other TODOs for more info)
	public void onPlayingSongChanged(final Song_DEPRECATED song) {
		Bitmap artwork = MediaLibrary.getAlbumArt(song);
		if(artwork == null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// Set the artwork
					albumArtSquareView.setImageDrawable(
							VectorDrawableCompat.create(
									getResources(),
									R.drawable.ic_audiotrack,
									getTheme()
							)
					);

					updateColors(GraphicUtils.BLUE_GREY_500);

					// Update info about the song

					songNameTextView.setText(song.getTitle());


					String artist = song.getArtistName();
					String album = song.getAlbumName();
					String info;

					// Decide the best way to display information about artist and album
					if(artist == null || artist.isEmpty()) {
						info = album;
					} else if(album == null || album.isEmpty()) {
						info = artist;
					} else {
						info = String.format(getString(R.string.album_info_template), artist, album);
					}

					songInfoTextView.setText(info);

					// Calculate and set the times for the song
					songTimelineSeekBar.setMax((song.getDuration() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) song.getDuration()));
					totalTimeTextView.setText(UIUtils.humanReadableTime(song.getDuration()));
					elapsedTimeTextView.setText(UIUtils.humanReadableTime(0, song.getDuration()));
				}
			});

			return;
		}

		// Cut and resize the artwork to be squared and of the right size

		// Determine the smallest possible scaling factor to avoid scaling too much

//		// Scale the bitmap, after this we will only need to cut it in a square
//		artwork = Bitmap.createScaledBitmap(
//				artwork,
//				(int) (width * scalingFactor),
//				(int)(height * scalingFactor),
//				false
//		);

		// Determine the borders to cut out of the artwork for it to be squared and match the ImageView

		// Crop the bitmap
//		artwork = Bitmap.createBitmap(
//				artwork,
//				centeredOffsetX,
//				centeredOffsetY,
//				width,
//				height
//		);

		final Bitmap finalArtwork = artwork;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Set the artwork
				albumArtSquareView.setImageBitmap(finalArtwork);
				updateColors();

				// Update info about the song

				songNameTextView.setText(song.getTitle());


				String artist = song.getArtistName();
				String album = song.getAlbumName();
				String info;

				// Decide the best way to display information about artist and album
				if(artist == null || artist.isEmpty()) {
					info = album;
				} else if(album == null || album.isEmpty()) {
					info = artist;
				} else {
					info = String.format(getString(R.string.album_info_template), artist, album);
				}

				songInfoTextView.setText(info);

				// Calculate and set the times for the song
				songTimelineSeekBar.setMax((song.getDuration() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) song.getDuration()));
				totalTimeTextView.setText(UIUtils.humanReadableTime(song.getDuration()));
				elapsedTimeTextView.setText(UIUtils.humanReadableTime(0, song.getDuration()));
			}
		});

//		// Set the artwork
//		albumArtSquareView.setImageBitmap(artwork);
//		updateColors();
//
//		// Update info about the song
//
//		songNameTextView.setText(song.getTitle());
//
//
//		String artist = song.getArtistName();
//		String album = song.getAlbumName();
//		String info;
//
//		// Decide the best way to display information about artist and album
//		if(Strings.isNullOrEmpty(artist)) {
//			info = album;
//		} else if(Strings.isNullOrEmpty(album)) {
//			info = artist;
//		} else {
//			info = String.format(getString(R.string.album_info_template), artist, album);
//		}
//
//		songInfoTextView.setText(info);
//
//		// Calculate and set the times for the song
//		songTimelineSeekBar.setMax((song.getDuration() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) song.getDuration()));
//		totalTimeTextView.setText(UIUtils.humanReadableTime(song.getDuration()));
//		elapsedTimeTextView.setText(UIUtils.humanReadableTime(0, song.getDuration()));

	}

	private void animatePlayPauseFAB() {
		animatePlayPauseFAB(!playPauseDrawable.isPlay());
	}

	private void animatePlayPauseFAB(boolean musicPaused) {
		if(playPauseDrawable.isPlay() == musicPaused) return;

		Animator animator = playPauseDrawable.getPausePlayAnimator();
		animator.cancel();
		animator.setDuration(AnimatedPlayPauseDrawable.DEFAULT_ANIMATION_DURATION)
				.start();
	}

	private void onMusicPlayingEvent() {
		animatePlayPauseFAB(false);
	}

	private void onMusicPausedEvent() {
		animatePlayPauseFAB(true);
	}

	private void onMusicSeekEvent(Message message) {
		if(message.arg1 < 0 || message.arg1 == songTimelineSeekBar.getProgress()) return;
		onProgressChanged(songTimelineSeekBar, message.arg1, false);
	}

	// TODO: Merge onPlayingSongChanged(Song_DEPRECATED) { ... } with onMusicChangedEvent(Message) { ... }
	private void onMusicChangedEvent(Message message) {
		Object obj = message.obj; // Proguard might force instanceof for to fail when optimizing
		if(!(obj instanceof Song_DEPRECATED)) return;

		onPlayingSongChanged((Song_DEPRECATED) obj);
	}

	private void onMusicStoppedEvent() {
		//
	}

	private static class EventHandler extends LeakSafeHandler<MainActivity> {

		/*package*/ EventHandler(@NonNull MainActivity referenced) {
			super(referenced);
		}

		@Override
		public boolean handleMessage(Message message, MainActivity reference) {
			switch(message.what) {
				case EVENT_PLAYING:
					if(reference == null) break;
					reference.onMusicPlayingEvent();
					break;
				case EVENT_PAUSED:
					if(reference == null) break;
					reference.onMusicPausedEvent();
					break;
				case EVENT_SEEK:
					if(reference == null) break;
					reference.onMusicSeekEvent(message);
					break;
				case EVENT_CHANGED:
					if(reference == null) break;
					reference.onMusicChangedEvent(message);
					break;
				case EVENT_STOPPED:
					if(reference == null) break;
					reference.onMusicStoppedEvent();
					break;
				case MAGIC_ALERT:
					if(reference == null) break;
					Snackbar.make(reference.findViewById(R.id.activity_main), message.arg1, Snackbar.LENGTH_SHORT).show();
					break;
				default:
					return false;
			}

			return true;
		}
	}

	private static class BackwardsServiceConnection extends BaseServiceConnection {

		private final WeakReference<MainActivity> activityReference;

		/*package*/ BackwardsServiceConnection(MainActivity activity) {
			activityReference = new WeakReference<>(activity);
		}

		// TODO: Clean up onServiceConnected(ComponentName, IBinder) { ... }
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			super.onServiceConnected(componentName, iBinder);

			MainActivity activity = activityReference.get();
			if(activity == null || messenger == null) return;

			MessagingUtils.send(messenger, MusicPlayerService.MAGIC_CONNECT, activity.messenger);
		}

		// TODO: Check if this works or we'll have to create our own unbind() method in MainActivity
		@Override
		protected void onPreServiceDisconnected(ComponentName componentName) {
			super.onPreServiceDisconnected(componentName);
			if(messenger != null) MessagingUtils.send(messenger, MusicPlayerService.MAGIC_DISCONNECT);
		}
	}
}
