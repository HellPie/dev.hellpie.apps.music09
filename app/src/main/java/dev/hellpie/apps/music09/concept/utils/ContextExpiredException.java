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

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Basic Exception used to specify the Context stored in a WeakReference
 * object has been Garbage Collected.
 */
public final class ContextExpiredException extends RuntimeException {

	public ContextExpiredException() {
	}

	public ContextExpiredException(String message) {
		super(message);
	}

	public ContextExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContextExpiredException(Throwable cause) {
		super(cause);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public ContextExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
