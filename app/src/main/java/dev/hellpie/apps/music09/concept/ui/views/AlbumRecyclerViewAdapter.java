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
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.listeners.OnRecyclerViewItemChosenListener;
import dev.hellpie.apps.music09.concept.media.MediaLibrary;
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;

public class AlbumRecyclerViewAdapter extends BaseRecyclerViewAdapter<Album_DEPRECATED, AlbumRecyclerViewAdapter.AlbumViewHolder> {

	public AlbumRecyclerViewAdapter(List<Album_DEPRECATED> content, @NonNull OnRecyclerViewItemChosenListener<Album_DEPRECATED> listener) {
		super(content, listener, R.layout.viewholder_album);
	}

	@Override
	protected AlbumViewHolder onInstantiateViewHolder(View view) {
		return new AlbumViewHolder(view);
	}

	@Override
	protected void onBindViewHolderToItem(AlbumViewHolder viewHolder, Album_DEPRECATED item) {
		// Apply the info from the album to the ViewHolder

		Context context = viewHolder.albumArt.getContext();

		Bitmap albumArt = MediaLibrary.getAlbumArt(item);
		Drawable imageDrawable;
		if(albumArt == null) { // No Album_DEPRECATED Art found, generate fallback
			imageDrawable = VectorDrawableCompat.create(
					context.getResources(),
					R.drawable.ic_album,
					context.getTheme()
			);
		} else {
			imageDrawable = new BitmapDrawable(context.getResources(), albumArt);
		}

		// Set either fallback image or true image
		viewHolder.albumArt.setImageDrawable(imageDrawable);

		viewHolder.albumName.setText(item.getName());
		viewHolder.albumArtist.setText(item.getArtist());
		viewHolder.songsCount.setText(String.valueOf(item.getTracks())); // It would search R.string
	}

	@SuppressWarnings("WeakerAccess") // RecyclerView.Adapter requires this *not* to be private
	protected static class AlbumViewHolder extends RecyclerView.ViewHolder {

		private TextView albumName;
		private TextView albumArtist;
		private TextView songsCount;
		private SquareView albumArt;

		private AlbumViewHolder(View itemView) {
			super(itemView);

			albumArt = (SquareView) itemView.findViewById(R.id.album_icon);
			albumName = (TextView) itemView.findViewById(R.id.album_name);
			albumArtist = (TextView) itemView.findViewById(R.id.album_artist);
			songsCount = (TextView) itemView.findViewById(R.id.album_items);
		}
	}
}
