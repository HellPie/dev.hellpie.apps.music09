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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Playlist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Song_DEPRECATED;

/**
 * Class that manages the music retrieved from the device and other sources, acting as a
 * temporary database.
 */
public class MediaLibrary {

	private static final Object songsLock = new Object();
	private static final Object albumsLock = new Object();
	private static final Object artistsLock = new Object();
	private static final Object playlistLock = new Object();

	// SparseArray are an Android-specific class that acts like a Map<primitive, Object>
	// They are really useful because in Maps often data is mapped to primitive types
	// which need to be boxed (int -> Integer, long -> Long) because Java does not
	// support primitive types for generic types.
	// SparseArray makes the data array much smaller because every key is a primitive value
	// and therefore does not have methods or a Class object assigned to it and also
	// makes everything faster because it always avoids boxing and unboxing (casting between
	// primitive and their Object versions) when getting and setting keys.
	// See: http://stackoverflow.com/a/31413003
	// Official Performance video: https://youtu.be/I16lz26WyzQ
	// Official docs: https://developer.android.com/reference/android/util/SparseArray.html
	private static LongSparseArray<Song_DEPRECATED> songs = new LongSparseArray<>();
	private static LongSparseArray<Album_DEPRECATED> albums = new LongSparseArray<>();
	private static LongSparseArray<Artist_DEPRECATED> artists = new LongSparseArray<>();
	private static LongSparseArray<Playlist_DEPRECATED> playlist = new LongSparseArray<>();

	public static void add(@NonNull Song_DEPRECATED song) {
		add(song, false);
	}

	public static void add(@NonNull Song_DEPRECATED song, boolean overwrite) {
		if(songs.get(song.getId()) != null && !overwrite) return;
		synchronized(songsLock) { songs.put(song.getId(), song); }
	}

	public static void add(@NonNull Album_DEPRECATED album) {
		add(album, false);
	}

	public static void add(@NonNull Album_DEPRECATED album, boolean overwrite) {
		if(albums.get(album.getId()) != null && !overwrite) return;
		synchronized(albumsLock) { albums.put(album.getId(), album); }
	}

	public static void add(Artist_DEPRECATED artist) {
		add(artist, false);
	}

	public static void add(@NonNull Artist_DEPRECATED artist, boolean overwrite) {
		if(artists.get(artist.getId()) != null && !overwrite) return;
		synchronized(artistsLock) { artists.put(artist.getId(), artist); }
	}

	public static void add(@NonNull Playlist_DEPRECATED playlist) {
		add(playlist, false);
	}

	public static void add(@NonNull Playlist_DEPRECATED playlist, boolean overwrite) {
		if(MediaLibrary.playlist.get(playlist.getId()) != null && !overwrite) return;
		synchronized(playlistLock) { MediaLibrary.playlist.put(playlist.getId(), playlist); }
	}

	/**
	 * Gathers all the songs known to the Library and returns them in an unordered list.
	 * @return All the songs in MediaLibrary.
	 */
	@NonNull
	public static List<Song_DEPRECATED> getSongs() {

		// Clone the array so we are sure that while building the list no async operation will add
		// a new item in the array
		LongSparseArray<Song_DEPRECATED> clone = songs.clone();

		// Don't waste time on looping if we have nothing to loop on
		if(clone.size() < 1) return Collections.emptyList();

		List<Song_DEPRECATED> allSongs = new ArrayList<>(clone.size());
		for(int i = 0; i < clone.size(); i++) allSongs.add(clone.valueAt(i));
		return allSongs;
	}

	/**
	 * Gathers all the songs matching a specific album in an unordered list.
	 * @param album The Album_DEPRECATED songs should be matched to
	 * @return All the songs in the given Album_DEPRECATED
	 */
	@NonNull
	public static List<Song_DEPRECATED> getSongs(@NonNull Album_DEPRECATED album) {
		if(albums.get(album.getId()) == null) return Collections.emptyList();

		LongSparseArray<Song_DEPRECATED> clone = songs.clone();

		List<Song_DEPRECATED> albumSongs = new ArrayList<>();
		for(int i = 0; i < clone.size(); i++) {
			Song_DEPRECATED song = clone.valueAt(i);
			if(song.getAlbumId() == album.getId()) albumSongs.add(song);
		}

		return albumSongs;
	}

	public static List<Song_DEPRECATED> getSongs(@NonNull Artist_DEPRECATED artist) {
		if(artists.get(artist.getId()) == null) return Collections.emptyList();

		LongSparseArray<Song_DEPRECATED> clone = songs.clone();

		List<Song_DEPRECATED> artistSongs = new ArrayList<>();
		for(int i = 0; i < clone.size(); i++) {
			Song_DEPRECATED song = clone.valueAt(i);
			if(song.getArtistId() == artist.getId()) artistSongs.add(song);
		}

		return artistSongs;
	}

	/**
	 * Returns a song matching a specific id.
	 * @param id The ID of the song to search for
	 * @return A song if the id is found, or null
	 */
	@Nullable
	public static Song_DEPRECATED getSong(long id) {
		return songs.get(id);
	}

	/**
	 * Returns a random song of the known ones, will return null if no song is present.
	 * @return A random song or null if no songs are found
	 */
	public static Song_DEPRECATED getRandomSong() {
		if(songs.size() < 1) return null;
		Random random = new Random(System.currentTimeMillis());
		return songs.valueAt(random.nextInt(songs.size()));
	}

	/**
	 * Gathers all the known albums and returns them as an unordered list.
	 * @return All the albums as a list
	 */
	public static List<Album_DEPRECATED> getAlbums() {
		LongSparseArray<Album_DEPRECATED> clone = albums.clone();
		if(clone.size() < 1) return Collections.emptyList();
		List<Album_DEPRECATED> allAlbums = new ArrayList<>();
		for(int i = 0; i < clone.size(); i++) allAlbums.add(clone.valueAt(i));
		return allAlbums;
	}

	public static List<Artist_DEPRECATED> getArtists() {
		LongSparseArray<Artist_DEPRECATED> clone = artists.clone();
		if(clone.size() < 1) return Collections.emptyList();
		List<Artist_DEPRECATED> allArtists = new ArrayList<>();
		for(int i = 0; i < clone.size(); i++) allArtists.add(clone.valueAt(i));
		return allArtists;
	}

	public static List<Album_DEPRECATED> getAlbums(Artist_DEPRECATED artist) {
		if(albums.size() < 1) return Collections.emptyList();
		List<Album_DEPRECATED> validAlbums = new ArrayList<>();
		for(int i = 0; i < albums.size(); i++) { // TODO: Artist_DEPRECATED.getId() in Album_DEPRECATED class for MediaLibrary.getAlbums(Artist_DEPRECATED)
			if(albums.valueAt(i).getArtist().toLowerCase().contains(artist.getName().toLowerCase())) {
				validAlbums.add(albums.valueAt(i));
			}
		}
		return validAlbums;
	}

	/**
	 * Returns the Album_DEPRECATED matching a specific album ID or an empty album if no match is found.
	 * @param albumId The album ID to search for
	 * @return The Album_DEPRECATED matching albumId or Album_DEPRECATED.NO_ALBUM if no match is found or albumId == -1
	 */
	@NonNull
	public static Album_DEPRECATED getAlbum(long albumId) {
		Album_DEPRECATED album = albums.get(albumId);
		return (album != null ? album : Album_DEPRECATED.NO_ALBUM);
	}

	/**
	 * Returns a random album of the ones known, will return null is song is present.
	 * @return A random album or null if no albums are found
	 */
	public static Album_DEPRECATED getRandomAlbum() {
		if(albums.size() < 1) return null;
		Random random = new Random(System.currentTimeMillis());
		return albums.valueAt(random.nextInt(albums.size()));
	}

	@NonNull
	public static Album_DEPRECATED getRandomAlbum(Artist_DEPRECATED artist) {
		List<Album_DEPRECATED> albums = getAlbums(artist);
		if(albums.size() < 1) return Album_DEPRECATED.NO_ALBUM;
		return albums.get(new Random(System.currentTimeMillis()).nextInt(albums.size()));
	}

	/**
	 * Returns the artwork for the given song. This tries to return the art of the album the
	 * song is linked to if possible.
	 * Can return null if no album art can be found in both the song and the album.
	 * @param song The song for which to get the artwork
	 * @return Either a Bitmap containing the album art or null if it cannot be found
	 */
	public static Bitmap getAlbumArt(@NonNull Song_DEPRECATED song) {

		// Try to get the image from the album first
		Album_DEPRECATED album = getAlbum(song.getAlbumId());
		if(album.getId() == Album_DEPRECATED.NO_ALBUM.getId()) {
			Bitmap result = getAlbumArt(album);
			if(result != null) return result;
		}

		// If no album is found, try to retrieve the art from the song itself
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(song.getLocation());
		byte[] rawArt = retriever.getEmbeddedPicture();
		if(rawArt != null) return BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length);

		// In case we really have no picture we'll let the caller use its own custom one
		return null;
	}

	/**
	 * Returns the album art of a given Album_DEPRECATED, or null if the specified album art cannot be found.
	 * @param album The Album_DEPRECATED into which the artwork is defined
	 * @return The artwork of the given album, or null if the path to the art cannot be found
	 */
	public static Bitmap getAlbumArt(@NonNull Album_DEPRECATED album) {
		if(album.getArt() == null) return null;
		return BitmapFactory.decodeFile(album.getArt());
	}
}
