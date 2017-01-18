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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;

/**
 * Manages connections and disconnections between Services and Activities.
 *
 * This class is especially useful when the Service and the Activity do not depend on each
 * other's lifecycle and the communication needs to happen independently and asynchronously.
 */
public class BaseServiceConnection implements ServiceConnection {

	protected boolean isConnected = false;
	protected Messenger messenger = null;

	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
		isConnected = true;
		messenger = new Messenger(iBinder);
	}

	@Override
	public final void onServiceDisconnected(ComponentName componentName) {
		onPreServiceDisconnected(componentName);
		isConnected = false;
		messenger = null;
	}

	protected void onPreServiceDisconnected(ComponentName componentName) {}

	public boolean isConnected() {
		return isConnected;
	}

	@Nullable
	public Messenger getMessenger() {
		return messenger;
	}
}
