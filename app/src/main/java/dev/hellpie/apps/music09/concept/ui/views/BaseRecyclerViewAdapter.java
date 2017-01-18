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

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dev.hellpie.apps.music09.concept.listeners.OnRecyclerViewItemChosenListener;

public abstract class BaseRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

	private int layout;
	protected List<T> content;
	protected OnRecyclerViewItemChosenListener<T> listener;

	public BaseRecyclerViewAdapter(List<T> content, OnRecyclerViewItemChosenListener<T> listener, @LayoutRes int layout) {
		this.content = new ArrayList<>(content);
		this.listener = listener;
		this.layout = layout;
	}


	@Override
	public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
		View viewHolderContent = LayoutInflater.from(parent.getContext())
				.inflate(layout, parent, false);

		viewHolderContent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listener.onRecyclerViewItemChosen(view);
			}
		});

		return onInstantiateViewHolder(viewHolderContent);
	}

	@Override
	public final void onBindViewHolder(VH holder, int position) {
		T item = content.get(position);
		onBindViewHolderToItem(holder, item);
	}

	@Override
	public final int getItemCount() {
		return content.size();
	}

	protected abstract VH onInstantiateViewHolder(View view);
	protected abstract void onBindViewHolderToItem(VH viewHolder, T item);
}
