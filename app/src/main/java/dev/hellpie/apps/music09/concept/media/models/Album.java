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

public class Album {

	public static final Album NO_ALBUM = new Album();

	private long id;
	private String name = "";
	private String artist = "";
	private String art = null;
	private int tracks = 0;

	private Album() {}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public String getArt() {
		return art;
	}

	public int getTracks() {
		return tracks;
	}

	public static final class Builder {

		private long id;
		private String name = "";
		private String artist = "";
		private String art = null;
		private int tracks = 0;

		public Builder() {}
		public Builder(Album base) {
			id = base.id;
			name = base.name;
			artist = base.artist;
			art = base.art;
			tracks = base.tracks;
		}

		public Builder withId(long id) {
			this.id = id;
			return this;
		}

		public Builder withName(String name) {
			if(name != null) this.name = name;
			return this;
		}

		public Builder withArtist(String artist) {
			if(artist != null) this.artist = artist;
			return this;
		}

		public Builder withArt(String art) {
			this.art = art;
			return this;
		}

		public Builder withTracks(int tracks) {
			this.tracks = tracks;
			return this;
		}

		public Album build() {
			Album album = new Album();
			album.id = id;
			album.name = name;
			album.artist = artist;
			album.art = art;
			album.tracks = tracks;
			return album;
		}
	}
}
