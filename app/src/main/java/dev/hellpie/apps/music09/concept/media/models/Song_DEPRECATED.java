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

package dev.hellpie.apps.music09.concept.media.models;

/**
 * This class is DEPRECATED - Use the new "Track" class instead.
 */
@Deprecated
public class Song_DEPRECATED {

	private long id = -1L;
	private String title = "";
	private String artistName = "";
	private String albumName = "";
	private String location = "";
	private long duration = 0;
	private Album_DEPRECATED album = Album_DEPRECATED.NO_ALBUM;
	private Artist_DEPRECATED artist = Artist_DEPRECATED.NO_ARTIST;
	private long albumId = -1L;
	private long artistId = -1L;

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtistName() {
		return artistName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public Album_DEPRECATED getAlbum() {
		return album;
	}

	public Artist_DEPRECATED getArtist() {
		return artist;
	}

	public String getLocation() {
		return location;
	}

	public long getDuration() {
		return duration;
	}

	public long getAlbumId() {
		return albumId;
	}

	public long getArtistId() {
		return artistId;
	}

	public static final class Builder {

		private long id = -1L;
		private String title = "";
		private String albumName = "";
		private String artistName = "";
		private String location = "";
		private long duration = 0;
		private Album_DEPRECATED album = Album_DEPRECATED.NO_ALBUM;
		private Artist_DEPRECATED artist = Artist_DEPRECATED.NO_ARTIST;
		private long albumId = -1L;
		private long artistId = -1L;

		public Builder withId(long id) {
			this.id = id;
			return this;
		}

		public Builder withTitle(String title) {
			if(title != null) this.title = title;
			return this;
		}

		public Builder withArtistName(String artistName) {
			if(artistName != null) this.artistName = artistName;
			return this;
		}

		public Builder withAlbumName(String albumName) {
			if(albumName != null) this.albumName = albumName;
			return this;
		}

		public Builder withLocation(String location) {
			if(location != null) this.location = location;
			return this;
		}

		public Builder withDuration(long duration) {
			this.duration = duration;
			return this;
		}

		public Builder withAlbum(Album_DEPRECATED album) {
			if(album != null) this.album = album;
			return this;
		}

		public Builder withArtist(Artist_DEPRECATED artist) {
			if(artist != null) this.artist = artist;
			return this;
		}

		public Builder withAlbumId(long albumId) {
			this.albumId = albumId;
			return this;
		}

		public Builder withArtistId(long artistId) {
			this.artistId = artistId;
			return this;
		}

		public Song_DEPRECATED build() {
			Song_DEPRECATED song = new Song_DEPRECATED();
			song.id = id;
			song.title = title;
			song.artistName = artistName;
			song.albumName = albumName;
			song.location = location;
			song.duration = duration;
			song.album = album;
			song.artist = artist;
			song.albumId = albumId;
			song.artistId = artistId;
			return song;
		}
	}
}
