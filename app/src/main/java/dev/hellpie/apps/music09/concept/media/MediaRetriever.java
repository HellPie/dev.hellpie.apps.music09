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

package dev.hellpie.apps.music09.concept.media;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

import dev.hellpie.apps.music09.concept.listeners.MediaRetrieverListener;
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Song_DEPRECATED;
import dev.hellpie.apps.music09.concept.utils.ContextExpiredException;

/**
 * Class that manages retrieving and organizing music into MusicLibrary.
 */
public class MediaRetriever {
	private static final String EXCEPTION_MSG = "Context expired while operating in %s";

	private static final String IS_MUSIC = MediaStore.Audio.Media.IS_MUSIC + " = 1";

	private static final String[] SONG_TABLE_COLUMNS = new String[] {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.DURATION
	};

	private static final String[] ALBUM_TABLE_COLUMNS = new String[] {
			MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM,
			MediaStore.Audio.Albums.ALBUM_ART,
			MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.NUMBER_OF_SONGS
	};

	private static final String[] ARTIST_TABLE_COLUMNS = new String[] {
			MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS
	};

	private WeakReference<Context> context;

	public MediaRetriever(@NonNull Context context) {
		setContext(context);
	}

	/**
	 * This method should be called only when another method in this class throws a
	 * ContextExpiredException, to refresh the WeakReference holding the Context
	 * used to access the ContentResolver and other resources from this object.
	 * @param context The new Context to set on this instance
	 */
	public void setContext(@NonNull Context context) {
		this.context = new WeakReference<>(context);
	}

	/**
	 * Runs the loadLibrary() method asynchronously to avoid blocking the process' main thread.
	 * @param listener The listener that will be called on completion
	 * @throws ContextExpiredException When the GC collects the context this instance uses
	 */
	public void loadLibraryAsync(final MediaRetrieverListener listener) throws ContextExpiredException {
		checkContext("loadLibraryAsync()");

		new Thread(new Runnable() {

			@Override
			public void run() {
				loadLibrary();
				listener.onMediaLoadingComplete();
			}
		}).start();
	}

	/**
	 * Loads all the songs the system can provide through the official APIs.
	 * Tries to resolve the album for each song, using an empty album if the defined one does
	 * not exist.
	 *
	 * @throws ContextExpiredException When the Context has been cleared by the GC.
	 */
	public void loadLibrary() throws ContextExpiredException {
		checkContext("loadLibrary()");

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor results = context.get().getContentResolver().query(uri, SONG_TABLE_COLUMNS, IS_MUSIC, null, null);

		// No need to continue if we have no results
		if(results == null) return;

		// Although SQL is user friendly it doesn't mean it's developer friendly
		final int idCol = results.getColumnIndex(MediaStore.Audio.Media._ID);
		final int titleCol = results.getColumnIndex(MediaStore.Audio.Media.TITLE);
		final int artistCol = results.getColumnIndex(MediaStore.Audio.Media.ARTIST);
		final int albumCol = results.getColumnIndex(MediaStore.Audio.Media.ALBUM);
		final int locationCol = results.getColumnIndex(MediaStore.Audio.Media.DATA);
		final int albumIdCol = results.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
		final int artistIdCol = results.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
		final int durationCol = results.getColumnIndex(MediaStore.Audio.Media.DURATION);

		// Don't store the count in the for loop as it will be recalculated each time
		final int count = results.getCount();
		for(int i = 0; i < count; i++) {
			results.moveToPosition(i);

			long albumId = results.getLong(albumIdCol);
			long artistId = results.getLong(artistIdCol);

			Album_DEPRECATED album = loadAlbum(albumId);
			Artist_DEPRECATED artist = loadArtist(artistId);
			Song_DEPRECATED song = new Song_DEPRECATED.Builder()
					.withId(results.getLong(idCol))
					.withTitle(results.getString(titleCol))
					.withArtistName(results.getString(artistCol))
					.withAlbumName(results.getString(albumCol))
					.withLocation(results.getString(locationCol))
					.withDuration(results.getLong(durationCol))
					.withAlbum(album)
					.withArtist(artist)
					.withAlbumId(albumId)
					.withArtistId(artistId)
					.build();

			if(album.getId() != Album_DEPRECATED.NO_ALBUM.getId()) MediaLibrary.add(album);
			if(artist.getId() != Artist_DEPRECATED.NO_ARTIST.getId()) MediaLibrary.add(artist);
			MediaLibrary.add(song);
		}

		// Clear and free the Cursor
		results.close();
	}

	@NonNull
	private Album_DEPRECATED loadAlbum(long id) throws ContextExpiredException {
		checkContext(String.format("loadAlbum(long id = %s)", String.valueOf(id)));

		Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		String query = MediaStore.Audio.Albums._ID + " = " + String.valueOf(id);
		Cursor result = context.get().getContentResolver().query(uri, ALBUM_TABLE_COLUMNS, query, null, null);

		// Either we have no result, or we have an empty row, we know we don't have an album
		if(result == null || result.getCount() < 1) return Album_DEPRECATED.NO_ALBUM;

		final int idCol = result.getColumnIndex(MediaStore.Audio.Albums._ID);
		final int nameCol = result.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
		final int artistCol = result.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
		final int artCol = result.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
		final int numCol = result.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

		result.moveToPosition(0);

		Album_DEPRECATED album = new Album_DEPRECATED.Builder()
				.withId(result.getLong(idCol))
				.withName(result.getString(nameCol))
				.withArtist(result.getString(artistCol))
				.withArt(result.getString(artCol))
				.withTracks(result.getInt(numCol))
				.build();

		result.close();
		return album;
	}

	private Artist_DEPRECATED loadArtist(long id) throws ContextExpiredException {
		checkContext(String.format("loadArtist(long id  = %s)", String.valueOf(id)));

		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		String query = MediaStore.Audio.Artists._ID + " = " + String.valueOf(id);
		Cursor result = context.get().getContentResolver().query(uri, ARTIST_TABLE_COLUMNS, query, null, null);

		// Either we have no result or we have an empty row, we know we don't have artists
		if(result == null || result.getCount() < 1) return Artist_DEPRECATED.NO_ARTIST;

		final int idCol = result.getColumnIndex(MediaStore.Audio.Artists._ID);
		final int nameCol = result.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
		final int numCol = result.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

		result.moveToPosition(0);

		Artist_DEPRECATED artist = new Artist_DEPRECATED.Builder()
				.withId(result.getLong(idCol))
				.withName(result.getString(nameCol))
				.withTracks(result.getInt(numCol))
				.build();

		result.close();
		return artist;
	}

	private void checkContext(@NonNull String func) throws ContextExpiredException {
		if(context.get() == null) throw new ContextExpiredException(String.format(EXCEPTION_MSG, func));
	}
}
