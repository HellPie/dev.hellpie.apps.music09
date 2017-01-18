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
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.listeners.MediaPlayerListener;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.MediaRetriever;
import dev.hellpie.apps.music09.concept.media.models.Album;
import dev.hellpie.apps.music09.concept.media.models.Artist;
import dev.hellpie.apps.music09.concept.media.models.Playlist;
import dev.hellpie.apps.music09.concept.media.models.Song;
import dev.hellpie.apps.music09.concept.ui.resources.AnimatedPlayPauseDrawable;
import dev.hellpie.apps.music09.concept.ui.views.SquareView;
import dev.hellpie.apps.music09.concept.utils.GraphicUtils;
import dev.hellpie.apps.music09.concept.utils.UIUtils;

public class PlayerActivity extends AppCompatActivity
		implements SeekBar.OnSeekBarChangeListener,
		MediaPlayerListener,
		MusicChooserFragment.OnMusicChosenListener {

	private final static String MUSIC_FRAGMENT = "dev.hellpie.apps.music09.concept.ui.activities.PlayerActivity.MUSIC_FRAGMENT";

	// GUI STUFF

	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.toolbar_title) TextView titleTextView;

	private VectorDrawableCompat playingBackgroundDrawable;
	@BindView(R.id._activity_main_background) ImageView playingBackground;

	@BindView(R.id.album_art) SquareView albumArtSquareView;

	@BindView(R.id.song_title) TextView songTitleTextView;
	@BindView(R.id.song_info) TextView songInfoTextView;

	@BindView(R.id.song_seek_bar) SeekBar songTimelineSeekBar;
	@BindView(R.id.song_seek_time_current) TextView elapsedTimeTextView;
	@BindView(R.id.song_seek_time_total) TextView totalTimeTextView;

	@BindView(R.id.controls_pause) FloatingActionButton playPauseSongFAB;
	@BindView(R.id.controls_previous) ImageButton prevSongButton;
	@BindView(R.id.controls_next) ImageButton nextSongButton;

	private AnimatedPlayPauseDrawable playPauseDrawable;

	// MUSIC PLAYER STUFF

	private MediaPlayer mediaPlayer;
	private final List<Song> tracksQueue = Collections.synchronizedList(new ArrayList<Song>(50));
	private int currentIndex = 0;
	private Handler timeUpdaterHandler = new Handler();
	Runnable timeUpdaterRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Make the ButterKnife Library look through this Activity class and setup its annotations
		ButterKnife.bind(this);

		// AppCompat ActionBar as Activity's Toolbar
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				// Show a DialogFragment and load it with the list of all the albums
				MusicChooserFragment.newInstance(MusicChooserFragment.LIST_ALBUMS, PlayerActivity.this)
						.show(PlayerActivity.this.getSupportFragmentManager(), MUSIC_FRAGMENT);
			}
		});

		// Remove title from Toolbar since we have our own
		ActionBar supportActionBar = getSupportActionBar();
		if(supportActionBar != null) supportActionBar.setDisplayShowTitleEnabled(false);

		// Apply the fancy animated button which took me 2h to fix on the Play/Pause FAB
		playPauseDrawable = new AnimatedPlayPauseDrawable(this);
		playPauseSongFAB.setImageDrawable(playPauseDrawable);

		// Prepare the fancy background for later usage
		playingBackgroundDrawable = VectorDrawableCompat.create(getResources(), R.drawable.bg_player, getTheme());
		playingBackground.setBackground(playingBackgroundDrawable);

		// ButterKnife handles clicks via @OnClick, but lacks annotations for SeekBar and some other views
		songTimelineSeekBar.setOnSeekBarChangeListener(this);

		// Load media from device into MediaLibrary class
		final MediaRetriever mediaRetriever = new MediaRetriever(this);
		mediaRetriever.loadLibrary();

		// Store queue
		shuffleMusic(MediaLibrary.getSongs());

		// Prepare the media player, but don't start it yet
		play(true);
		pause();

		// Setup the runnable that will update the time in the SeekBar
		timeUpdaterRunnable = new Runnable() {
			@Override
			public void run() {
				if(mediaPlayer == null || !mediaPlayer.isPlaying()) return;

				final int currentTime = mediaPlayer.getCurrentPosition();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						elapsedTimeTextView.setText(
								UIUtils.humanReadableTime(
										currentTime,
										mediaPlayer.getDuration()
								)
						);

						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							songTimelineSeekBar.setProgress(currentTime, true);
						} else {
							songTimelineSeekBar.setProgress(currentTime);
						}
					}
				});

				// Schedule another update, this will stop by itself thanks to the "if" at the top
				timeUpdaterHandler.postDelayed(this, 40); // Every triple buffering redraw (16ms@60fps->[3frames - computation_time])
			}
		};
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_HEADSETHOOK:
				togglePlayback();
				animatePlayPauseFAB();
				return true;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				next();
				return true;
			case KeyEvent.KEYCODE_MEDIA_REWIND:
				// Only go back one song if there is no sense in rewinding the current track
				if(mediaPlayer == null) {
					rewind();
					return true;
				}
				if(mediaPlayer.getCurrentPosition() < (mediaPlayer.getDuration() / 20)) {
					previous();
				} else {
					rewind();
				}
				return true;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				previous();
				return true;
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	@OnClick({R.id.controls_pause, R.id.controls_previous, R.id.controls_next})
	public void onClick(View view) {

		switch(view.getId()) {
			case R.id.controls_pause:
				togglePlayback();
				break;
			case R.id.controls_next:
				next();
				break;
			case R.id.controls_previous:
				// Only go back one song if there is no sense in rewinding the current track
				if(mediaPlayer == null) rewind();
				if(mediaPlayer.getCurrentPosition() < (mediaPlayer.getDuration() / 20)) {
					previous();
				} else {
					rewind();
				}
				break;
			default:
				break;
		}
	}

	@OnLongClick({R.id.album_art, R.id.song_title, R.id.song_info})
	public boolean onLongClick(View view) {
		@MusicChooserFragment.ListType int listType;

		// Understand which view was clicked
		switch(view.getId()) {
			case R.id.album_art:
				listType = MusicChooserFragment.LIST_ALBUMS;
				break;
			case R.id.song_title:
				listType = MusicChooserFragment.LIST_SONGS;
				break;
			case R.id.song_name:
				listType = MusicChooserFragment.LIST_AUTHORS;
				break;
			default:
				return false; // Not a specific view, stop (false -> same as "Magikarp uses Splash")
		}

		// Show the appropriate music chooser Bottom Sheet Dialog
		MusicChooserFragment.newInstance(listType, this).show(this.getSupportFragmentManager(), MUSIC_FRAGMENT);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
		if(b && mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.seekTo(i);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { /* EMPTY */ }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { /* EMPTY */ }

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) { /* EMPTY */ }

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		next();
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
		return false;
	}

	@Override
	public void onMusicChosen(Object choice) {

		List<Song> chosenTracks = new ArrayList<>();

		if(choice instanceof Song) {
			chosenTracks.add((Song) choice);
		} else if(choice instanceof Album) {
			chosenTracks.addAll(MediaLibrary.getSongs((Album) choice));
		} else if(choice instanceof Artist) {
			chosenTracks.addAll(MediaLibrary.getSongs((Artist) choice));
		} else if(choice instanceof Playlist) {
			chosenTracks.addAll(MediaLibrary.getSongs());
		}

		if(chosenTracks.size() > 0) {
			shuffleMusic(chosenTracks);
			play(true);
		}
	}

	private void togglePlayback() {
		if(playPauseDrawable.isPlay()) {
			play(false);
		} else {
			pause();
		}
	}

	private void play(boolean newSong) {
		if(mediaPlayer != null && mediaPlayer.isPlaying() && !newSong) return;

		animatePlayPauseFAB(false);
		timeUpdaterHandler.post(timeUpdaterRunnable); // Async, but instant

		if(newSong) {
			playCurrentSong();
		} else {
			mediaPlayer.start();
		}
	}

	private void pause() {
		if(mediaPlayer == null || !mediaPlayer.isPlaying()) return;

		animatePlayPauseFAB(true);
		mediaPlayer.pause();
	}

	private void next() {
		if(currentIndex >= tracksQueue.size() - 1) {
			currentIndex = 0; // Loop to head if we were on the last song
		} else {
			currentIndex++;
		}

		play(true);
	}

	private void previous() {
		if(currentIndex <= 0) {
			currentIndex = tracksQueue.size() - 1; // Loop to tail if we were on the first song
		} else {
			currentIndex--;
		}

		play(true);
	}

	private void rewind() {
		if(mediaPlayer == null || mediaPlayer.getCurrentPosition() == 0) return;
		mediaPlayer.seekTo(0);
	}

	private void shuffleMusic(List<Song> queue) {

		// Shuffle music
		Collections.shuffle(queue, new Random(System.currentTimeMillis()));

		// Clear and append to queue
		tracksQueue.clear();
		tracksQueue.addAll(queue);

		// Reset index
		currentIndex = 0;

		// Reset or init the Media Player instance
		if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
	}

	private void playCurrentSong() {
		// Obtain the song
		Song song = tracksQueue.get(currentIndex);

		// Update the layouts for the new song (before starting playback 'cause it takes more time and is async)
		updateViewsContent(song);

		// Stop the ongoing music player if it exists
		if(mediaPlayer != null) mediaPlayer.stop();

		// Reinitialize the media player
		mediaPlayer = MediaPlayer.create(this, Uri.parse(song.getLocation()));
		initializeMediaPlayer();
		if(mediaPlayer != null) mediaPlayer.start();
	}

	private void initializeMediaPlayer() {
		if(mediaPlayer == null) return;

		// WakeLocks avoid the CPU to go to sleep when the screen is off so that music keeps playing
		mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

		// Set Listeners for the MediaPlayer to know what's happening to it
		mediaPlayer.setOnPreparedListener(this); // Ready to play
		mediaPlayer.setOnCompletionListener(this); // Done playing
		mediaPlayer.setOnErrorListener(this); // Whoopsies
	}

	private void updateViewsContent(final Song song) {

		// Fallback already initialized in case song has no album art
		Drawable albumArtDrawable = VectorDrawableCompat.create(
				getResources(),
				R.drawable.ic_audiotrack,
				getTheme()
		);

		// Assemble the final string containing both artist and album, or the one we know
		String artist = song.getArtistName();
		String album = song.getAlbumName();
		String info;

		// Decide the best way to display information about artist and album
		if(Strings.isNullOrEmpty(artist) || artist.equals(MediaStore.UNKNOWN_STRING)) {
			info = album;
		} else if(Strings.isNullOrEmpty(album) || album.equals(MediaStore.UNKNOWN_STRING)) {
			info = artist;
		} else {
			info = String.format(getString(R.string.album_info_template), artist, album);
		}

		// Get the Bitmap for the album art, try to use this one as shown album art
		final Bitmap albumArtBitmap = MediaLibrary.getAlbumArt(song);
		if(albumArtBitmap != null) albumArtDrawable = new BitmapDrawable(getResources(), albumArtBitmap);

		// Everything touching the UI **must** be run on the UI Thread
		// Since this method MIGHT be called asynchronously, we want to make sure we use the right Thread
		final Drawable finalAlbumArtDrawable = albumArtDrawable;
		final String finalInfo = info;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Update info about the song

				songTitleTextView.setText(song.getTitle());
				songInfoTextView.setText(finalInfo);

				// Calculate and set the times for the song
				songTimelineSeekBar.setMax((song.getDuration() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) song.getDuration()));
				totalTimeTextView.setText(UIUtils.humanReadableTime(song.getDuration()));
				elapsedTimeTextView.setText(UIUtils.humanReadableTime(0, song.getDuration()));

				// Set album art
				albumArtSquareView.setImageDrawable(finalAlbumArtDrawable);

				// Knowing we can't extract a color from a real album art, set a fixed one
				// This is the color used in the fallback album art, we won't extract it via Palette
				// because it's much faster to set it directly, since we know it.
				if(albumArtBitmap == null) {
					updateColors(GraphicUtils.BLUE_GREY_500);
				} else {
					updateColors(); // EASY: the others will do the dirty job for us (even tho I wrote the code for it...)
				}

			}
		});
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
}
