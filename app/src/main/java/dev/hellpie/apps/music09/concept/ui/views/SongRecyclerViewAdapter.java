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

package dev.hellpie.apps.music09.concept.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.listeners.OnRecyclerViewItemChosenListener;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.models.Song;

public class SongRecyclerViewAdapter extends BaseRecyclerViewAdapter<Song, SongRecyclerViewAdapter.SongViewHolder> {

	public SongRecyclerViewAdapter(List<Song> content, OnRecyclerViewItemChosenListener<Song> listener) {
		super(content, listener, R.layout.viewholder_song);
	}

	@Override
	protected SongViewHolder onInstantiateViewHolder(View view) {
		return new SongViewHolder(view);
	}

	@Override
	protected void onBindViewHolderToItem(SongViewHolder viewHolder, Song item) {
		Context context = viewHolder.albumArt.getContext();

		Bitmap albumArt = null;
		if(content.indexOf(item) != 0) albumArt = MediaLibrary.getAlbumArt(item);

		Drawable imageDrawable;
		if(albumArt == null) { // No Album Art found, generate fallback
			imageDrawable = VectorDrawableCompat.create(
					context.getResources(),
					R.drawable.ic_audiotrack,
					context.getTheme()
			);
		} else {
			imageDrawable = new BitmapDrawable(context.getResources(), albumArt);
		}

		viewHolder.songName.setText(item.getTitle());
		viewHolder.songInfo.setText(
				String.format(
						context.getString(R.string.album_info_template),
						item.getArtistName(),
						item.getAlbumName()
				)
		);

		// Set the image, custom shuffle image if the item is the hacky All Songs song
		viewHolder.albumArt.setImageDrawable(
				content.indexOf(item) == 0 ?
						VectorDrawableCompat.create(context.getResources(), R.drawable.ic_shuffle, context.getTheme())
						:
						imageDrawable
		);

		if(content.indexOf(item) == 0) viewHolder.songInfo.setVisibility(View.GONE);
	}

	@SuppressWarnings("WeakerAccess")
	protected static class SongViewHolder extends RecyclerView.ViewHolder {

		private SquareView albumArt;
		private TextView songName;
		private TextView songInfo;

		private SongViewHolder(View itemView) {
			super(itemView);

			albumArt = (SquareView) itemView.findViewById(R.id.album_icon);
			songName = (TextView) itemView.findViewById(R.id.song_name);
			songInfo = (TextView) itemView.findViewById(R.id.song_info);
		}
	}
}
