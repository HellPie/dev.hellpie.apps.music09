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

package dev.hellpie.apps.music09.concept.utils;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Static class containing utility functions to help with the managing of asynchronous messaging
 * between Services and Activities or other classes.
 */
public final class MessagingUtils {
	private MessagingUtils() { /* Utils - Never instantiate */ }

	/**
	 * Sends a message to a specified messenger to be handled.
	 *
	 * @param messenger The receiver of the message
	 * @param what The action or event the message represents
	 */
	public static void send(Messenger messenger, final int what) {
		send(messenger, what, null);
	}

	/**
	 * Sends a message to a specified messenger to be handled and attached extra data to it.
	 *
	 * @param messenger The receiver of the message
	 * @param what The action or event the message represents
	 * @param attachment An integer as argument, a Messenger to reply back to or any other object
	 */
	public static void send(Messenger messenger, final int what, @Nullable Object attachment) {
		if(messenger == null) return;

		// Create message and choose the proper place for attachment if not null
		Message message = Message.obtain(null, what);
		if(attachment instanceof Messenger) {
			message.replyTo = messenger;
		} else if(attachment instanceof Integer) {
			message.arg1 = (int) attachment;
		} else if(attachment != null) {
			message.obj = attachment;
		}

		try { // *Try* to send the message, it should work as long as the Messenger doesn't leak
			messenger.send(message);
		} catch(RemoteException e) {
			e.printStackTrace();
		}
	}
}
