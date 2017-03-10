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
import dev.hellpie.apps.music09.concept.media.models.Album_DEPRECATED;
import dev.hellpie.apps.music09.concept.media.models.Artist_DEPRECATED;

public class ArtistRecyclerViewAdapter extends BaseRecyclerViewAdapter<Artist_DEPRECATED, ArtistRecyclerViewAdapter.ArtistViewHolder> {


	public ArtistRecyclerViewAdapter(List<Artist_DEPRECATED> content, OnRecyclerViewItemChosenListener<Artist_DEPRECATED> listener) {
		super(content, listener, R.layout.viewholder_artist);
	}

	@Override
	protected ArtistViewHolder onInstantiateViewHolder(View view) {
		return new ArtistViewHolder(view);
	}

	@Override
	protected void onBindViewHolderToItem(ArtistViewHolder viewHolder, Artist_DEPRECATED item) {


		Context context = viewHolder.albumArt.getContext();

		// Get all albums for that artist
		List<Album_DEPRECATED> albums = MediaLibrary.getAlbums(item);
		Bitmap albumArt = null;
		for(Album_DEPRECATED album : albums) { // Selected the first album with an album art
			albumArt = MediaLibrary.getAlbumArt(album);
			if(albumArt != null) break;
		}

		Drawable imageDrawable;
		if(albumArt == null) { // No Album_DEPRECATED Art found, generate fallback
			imageDrawable = VectorDrawableCompat.create(
					context.getResources(),
					R.drawable.ic_artist,
					context.getTheme()
			);
		} else {
			imageDrawable = new BitmapDrawable(context.getResources(), albumArt);
		}

		// Set either fallback image or true image
		viewHolder.albumArt.setImageDrawable(imageDrawable);

		viewHolder.artistName.setText(item.getName());
		viewHolder.itemsCount.setText(String.valueOf(item.getTracks()));
	}

	protected static class ArtistViewHolder extends RecyclerView.ViewHolder {

		TextView artistName;
		TextView itemsCount;
		SquareView albumArt;

		public ArtistViewHolder(View itemView) {
			super(itemView);

			artistName = (TextView) itemView.findViewById(R.id.artist_name);
			itemsCount = (TextView) itemView.findViewById(R.id.artist_items);
			albumArt = (SquareView) itemView.findViewById(R.id.album_icon);
		}
	}
}
