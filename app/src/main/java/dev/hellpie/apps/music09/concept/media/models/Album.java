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

import android.support.annotation.NonNull;

import org.parceler.Parcel;

import java.util.Collections;
import java.util.List;

@Parcel
public class Album extends TrackList {

	public static final Album EMPTY = new Album(-1, "", "", "", -1, Collections.<String>emptyList());

	public final long id;
	@NonNull public final String name;
	@NonNull public final String artist;
	@NonNull public final String art;
	public final int year;

	public Album(long id, @NonNull String name, @NonNull String artist, @NonNull String art, int year, @NonNull List<String> tracks) {
		super(tracks);
		this.id = id;
		this.name = name;
		this.artist = artist;
		this.art = art;
		this.year = year;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj != null && obj instanceof Album && id == ((Album) obj).id);
	}
}
