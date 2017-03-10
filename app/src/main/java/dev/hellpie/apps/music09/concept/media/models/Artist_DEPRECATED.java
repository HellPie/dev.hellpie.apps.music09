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
 * This class is DEPRECATED - Use the new "Artist" class instead.
 */
@Deprecated
public class Artist_DEPRECATED {

	public static final Artist_DEPRECATED NO_ARTIST = new Artist_DEPRECATED();

	private long id;
	private String name = "";
	private int tracks = 0;

	private Artist_DEPRECATED() {}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getTracks() {
		return tracks;
	}

	public static class Builder {

		private long id;
		private String name = "";
		private int tracks = 0;

		public Builder() {}
		public Builder(Artist_DEPRECATED artist) {
			id = artist.id;
			name = artist.name;
			tracks = artist.tracks;
		}

		public Builder withId(long id) {
			this.id = id;
			return this;
		}

		public Builder withName(String name) {
			if(name != null) this.name = name;
			return this;
		}

		public Builder withTracks(int count) {
			this.tracks = count;
			return this;
		}

		public Artist_DEPRECATED build() {
			Artist_DEPRECATED artist = new Artist_DEPRECATED();
			artist.id = id;
			artist.name = name;
			artist.tracks = tracks;
			return artist;
		}
	}
}
