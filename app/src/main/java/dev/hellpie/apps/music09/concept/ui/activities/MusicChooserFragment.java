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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.listeners.OnRecyclerViewItemChosenListener;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Playlist_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Song_DEPRECATED;
import dev.hellpie.apps.music09.concept.ui.views.AlbumRecyclerViewAdapter;
import dev.hellpie.apps.music09.concept.ui.views.ArtistRecyclerViewAdapter;
import dev.hellpie.apps.music09.concept.ui.views.SongRecyclerViewAdapter;

public class MusicChooserFragment extends BottomSheetDialogFragment {

	public static final String ARG_LIST_TYPE = "dev.hellpie.apps.music09.concept.ui.activities.MusicChooserFragment.ARG_LIST_TYPE";

	/**
	 * Defines the type of query and therefore the content to show the user when choosing
	 * what queue to play next.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({LIST_ALBUMS, LIST_AUTHORS, LIST_SONGS})
	public @interface ListType {}
	public static final int LIST_ALBUMS = 0;
	public static final int LIST_AUTHORS = 1;
	public static final int LIST_SONGS = 2;

	private OnMusicChosenListener listener;

	public MusicChooserFragment() { /* Required empty public constructor */ }
	public static MusicChooserFragment newInstance(@ListType int listType, OnMusicChosenListener listener) {
		Bundle arguments = new Bundle();
		arguments.putInt(ARG_LIST_TYPE, listType);

		MusicChooserFragment fragment = new MusicChooserFragment();
		fragment.setArguments(arguments);
		fragment.listener = listener;

		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Fix the StatusBar color being set to black by using WRAP_CONTENT on the dialog instead
		// of the default MATCH_PARENT, MATCH_PARENT, which also overrides Status Bar color flags.

		Window window = getDialog().getWindow();
		if(window == null) return; // If the content hasn't been display, which should never be the case

		final WindowManager.LayoutParams params = window.getAttributes();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT; // This way we only take up the space we need
		getDialog().getWindow().setAttributes(params);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View content = inflater.inflate(R.layout.fragment_music_chooser, container, false);

		// Initialize the RecyclerView here and continue later based on item type
		RecyclerView recyclerView = (RecyclerView) content.findViewById(R.id.recycler);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));

		switch(getArguments().getInt(ARG_LIST_TYPE, LIST_ALBUMS)) {
			case LIST_AUTHORS:
				onCreateArtistsView(recyclerView);
				break;
			case LIST_SONGS:
				onCreateSongsView(recyclerView);
				break;
			case LIST_ALBUMS:
			default:
				onCreateAlbumsView(recyclerView);
				break;
		}

		return content;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void onCreateAlbumsView(final RecyclerView recyclerView) {

		// Count songs present in each album
		final List<Album_DEPRECATED> albums = MediaLibrary.getAlbums();
		List<Album_DEPRECATED> countingAlbums = new ArrayList<>(albums.size());
		for(Album_DEPRECATED album : albums) {
			countingAlbums.add(new Album_DEPRECATED.Builder(album).withTracks(MediaLibrary.getSongs(album).size()).build());
		}

		recyclerView.setAdapter(
				new AlbumRecyclerViewAdapter(
						countingAlbums,
						new OnRecyclerViewItemChosenListener<Album_DEPRECATED>() {
							@Override
							public void onRecyclerViewItemChosen(View chosenView) {
								int position = recyclerView.getChildAdapterPosition(chosenView);
								listener.onMusicChosen(albums.get(position));
								getDialog().dismiss();
							}
						}
				)
		);
	}

	private void onCreateArtistsView(final RecyclerView recyclerView) {

		// Count albums per artist
		final List<Artist_DEPRECATED> artists = MediaLibrary.getArtists();
		List<Artist_DEPRECATED> countingArtists = new ArrayList<>(artists.size());
		for(Artist_DEPRECATED artist : artists) {
			countingArtists.add(new Artist_DEPRECATED.Builder(artist).withTracks(MediaLibrary.getSongs(artist).size()).build());
		}

		// Initialize the RecyclerView
		recyclerView.setAdapter(
				new ArtistRecyclerViewAdapter(
						countingArtists,
						new OnRecyclerViewItemChosenListener<Artist_DEPRECATED>() {
							@Override
							public void onRecyclerViewItemChosen(View chosenView) {
								int position = recyclerView.getChildAdapterPosition(chosenView);
								listener.onMusicChosen(artists.get(position));
								getDialog().dismiss();
							}
						}
				)
		);
	}

	private void onCreateSongsView(final RecyclerView recyclerView) {

		// Hacky song instead of a button to avoid custom Dialog layout just for this
		Song_DEPRECATED allSongs = new Song_DEPRECATED.Builder()
				.withAlbum(Album_DEPRECATED.NO_ALBUM)
				.withAlbumId(Album_DEPRECATED.NO_ALBUM.getId())
				.withId(-1)
				.withTitle(getContext().getString(R.string.all_songs))
				.build();

		// Hack the hacky extra song at the top of other songs
		final List<Song_DEPRECATED> songs = MediaLibrary.getSongs();
		final List<Song_DEPRECATED> hackySongs = new ArrayList<>(songs.size() + 1);
		hackySongs.add(0, allSongs);
		for(Song_DEPRECATED song : songs) hackySongs.add(song);

		// Fill the RecyclerView
		recyclerView.setAdapter(
				new SongRecyclerViewAdapter(
						hackySongs,
						new OnRecyclerViewItemChosenListener<Song_DEPRECATED>() {
							@Override
							public void onRecyclerViewItemChosen(View chosenView) {
								int position = recyclerView.getChildAdapterPosition(chosenView);
								if(position == 0) {
									listener.onMusicChosen(Playlist_DEPRECATED.EMPTY_PLAYLIST); // Default for All-Songs
								} else {
									listener.onMusicChosen(songs.get(position - 1));
								}
								getDialog().dismiss();
							}
						}
				)
		);

		recyclerView.setHasFixedSize(true);
	}

	public interface OnMusicChosenListener {
		void onMusicChosen(Object choice);
	}
}
