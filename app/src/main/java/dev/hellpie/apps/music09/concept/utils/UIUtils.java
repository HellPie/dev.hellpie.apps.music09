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

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.Window;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Static class containing utility functions relative to UI user-friendly conversions.
 */
public class UIUtils {
	private UIUtils() { /* Utils - Don't instantiate */ }

	/**
	 * Calculates Pixel sizes on the current Display given DP dimensions.
	 *
	 * @param dp The size in dp to convert
	 * @param context Used to obtain the current Display metrics
	 * @return How many pixels represent the given dp at the current dpi
	 */
	public static int dpToPx(int dp, Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return Math.round(dp * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

	/**
	 * Calculates DP dimensions for the given Pixel sizes on the current Display.
	 *
	 * @param px The size in pixels to convert
	 * @param context Used to obtain the current Display metrics
	 * @return How many dp represent the given px at the current dpi
	 */
	public static int pxToDp(int px, Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return Math.round(px / (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

	/**
	 * Calculates the complete size of the current Display.
	 *
	 * @param activity Used to obtain Display information
	 * @return The height and width of the screen, in pixels, in a Size object
	 */
	public static Size getScreenSize(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize); // "Core" Android API using refs like C/C++. Ugly but eh.

		return new Size(displaySize.x, displaySize.y);
	}

	/**
	 * Calculates the height of the status bar on this device, if it's present.
	 *
	 * @param activity Used to obtain Display and Window information
	 * @return The height of the statusbar in pixels, 0 if there is no status bar rendered
	 */
	public static int getStatusBarHeight(Activity activity) {
		Rect activityContentArea = new Rect();
		Window window = activity.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(activityContentArea); // REFS REFS REFS

		return activityContentArea.top; // The offset from the top of the window to content :D
	}

	/**
	 * Converts the given time from milliseconds to the classic hh:mm:ss, mm:ss or ss format
	 *
	 * @param unixTime The milliseconds to convert
	 * @return A human readable formatted version of the given amount of milliseconds
	 */
	public static String humanReadableTime(long unixTime) {

		unixTime = unixTime / 1000; // Possibly milliseconds -> Seconds?

		int hours = (int) TimeUnit.SECONDS.toHours(unixTime);
		int minutes = (int) (TimeUnit.SECONDS.toMinutes(unixTime) - hours * 60);
		int seconds = (int) (unixTime - (hours * 3600 + minutes * 60));

		if(hours == 0) {
			if(minutes == 0) {
				return String.format(Locale.getDefault(), "%02d", seconds);
			} else {
				return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
			}
		} else {
			return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
		}
	}

	/**
	 * Converts a given amount of milliseconds into a formatted hhh:mm:ss, hh:mm:ss or whatever is
	 * represented by formatting the given amount of milliseconds in baseFormatTime.
	 *
	 * @param unixTime The amount of milliseconds to format
	 * @param baseFormatTime The amount of milliseconds from which to copy the formatting
	 * @return A formatted version of unixTime using baseFormatTime as the formatting rule
	 */
	public static String humanReadableTime(long unixTime, long baseFormatTime) {
		String baseTime = humanReadableTime(baseFormatTime);
		String newTime = humanReadableTime(unixTime);

		int separators = baseTime.split(":").length;
		int currentSeparators = newTime.split(":").length;
		if(separators > currentSeparators) {
			if(separators == 3) {
				newTime = "000:" + newTime;
			} else {
				newTime = "00:" + newTime;
			}
		}

		return newTime;
	}
}
