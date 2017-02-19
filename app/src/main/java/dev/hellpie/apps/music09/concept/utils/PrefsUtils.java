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

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dev.hellpie.apps.music09.concept.R;

public final class PrefsUtils {

	@IntDef({PREF_UPDATE_ENA, PREF_UPDATE_ENA_BETA, PREF_UPDATE_ENA_ALPHA,
			PREF_PLAY_RESUME, PREF_PLAY_RESUME_JACK, PREF_PLAY_CONTINUE,
			PREF_DBG_ENA, PREF_DBG_ENA_LOG, PREF_DBG_ENA_TRACE,
			PREF_DBG_ENA_UPLOAD, PREF_DBG_ENA_STATS})
	@Retention(RetentionPolicy.SOURCE)
	public @interface BoolPref {}

	@IntDef({PREF_UPDATE_TIMER})
	@Retention(RetentionPolicy.SOURCE)
	public @interface IntPref {}

	@IntDef({PREF_UPDATE_TIMER_LAST})
	@Retention(RetentionPolicy.SOURCE)
	public @interface LongPref {}

	@StringRes public static final int PREF_UPDATE_ENA = R.string.preferences_update_enable_key;
	@StringRes public static final int PREF_UPDATE_ENA_BETA = R.string.preferences_update_betas_key;
	@StringRes public static final int PREF_UPDATE_ENA_ALPHA = R.string.preferences_update_alphas_key;
	@StringRes public static final int PREF_UPDATE_TIMER = R.string.preferences_update_timer_key;
	@StringRes public static final int PREF_UPDATE_TIMER_LAST = R.string.preferences_update_timer_last_key;
	@StringRes public static final int PREF_PLAY_RESUME = R.string.preferences_playback_resume_key;
	@StringRes public static final int PREF_PLAY_RESUME_JACK = R.string.preferences_playback_resume_headset_key;
	@StringRes public static final int PREF_PLAY_CONTINUE = R.string.preferences_playback_headset_key;
	@StringRes public static final int PREF_DBG_ENA = R.string.preferences_debug_enable_key;
	@StringRes public static final int PREF_DBG_ENA_LOG = R.string.preferences_debug_logcat_key;
	@StringRes public static final int PREF_DBG_ENA_TRACE = R.string.preferences_debug_stacktrace_key;
	@StringRes public static final int PREF_DBG_ENA_UPLOAD = R.string.preferences_debug_upload_key;
	@StringRes public static final int PREF_DBG_ENA_STATS = R.string.preferences_debug_analytics_key;

	private PrefsUtils() { /* Utils - Do Not Instantiate */ }

	public static boolean getBool(@NonNull Context context, @BoolPref int preference) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getString(preference), false);
	}

	public static int getInt(@NonNull Context context, @IntPref int preference) {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(context.getString(preference), "0"));
	}

	public static long getString(@NonNull Context context, @LongPref int preference) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong(context.getString(preference), 0);
	}

	public static void save(@NonNull Context context, @BoolPref int preference, boolean newValue) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putBoolean(context.getString(preference), newValue).apply();
	}

	public static void save(@NonNull Context context, @IntPref int preference, int newValue) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putInt(context.getString(preference), newValue).apply();
	}

	public static void save(@NonNull Context context, @LongPref int preference, long newValue) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit().putLong(context.getString(preference), newValue).apply();
	}
}
