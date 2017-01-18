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

package dev.hellpie.apps.music09.concept.services;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Defines a Handler that will not lock instances of objects.
 * This is useful when a Service and an Activity need to communicate between each other but
 * they don't depend on each other's lifecycle, so they might be dead when communication happens.
 *
 * @param <T> The class of the Service for which this Handler needs to act as a bridge
 */
public abstract class LeakSafeHandler<T> extends Handler {

	private final WeakReference<T> reference;

	public LeakSafeHandler(@NonNull T referenced) {
		reference = new WeakReference<>(referenced);
	}

	@Nullable
	public final T get() {
		return reference.get();
	}

	@Override
	public final void handleMessage(Message msg) {
		if(!handleMessage(msg, reference.get())) super.handleMessage(msg);
	}

	public abstract boolean handleMessage(Message message, @Nullable T reference);
}
