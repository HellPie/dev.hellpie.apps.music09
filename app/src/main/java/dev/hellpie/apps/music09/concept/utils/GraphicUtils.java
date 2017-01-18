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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.util.SparseIntArray;

import java.util.Random;

public final class GraphicUtils {
	private GraphicUtils() { /* Utils - Never instantiate */ }

	// Material design colors
	public static final int RED_500 = 0xFFF44336;
	public static final int PINK_500 = 0xFFE91E63;
	public static final int PURPLE_500 = 0xFF9C27B0;
	public static final int DEEP_PURPLE_500 = 0xFF673AB7;
	public static final int INDIGO_500 = 0xFF3F51B5;
	public static final int BLUE_500 = 0xFF2196F3;
	public static final int LIGHT_BLUE_500 = 0xFF03A9F4;
	public static final int CYAN_500 = 0xFF00BCD4;
	public static final int TEAL_500 = 0xFF009688;
	public static final int GREEN_500 = 0xFF4CAF50;
	public static final int LIGHT_GREEN_500 = 0xFF8BC34A;
	public static final int LIME_500 = 0xFFCDDC39;
	public static final int YELLOW_500 = 0xFFFFEB3B;
	public static final int AMBER_500 = 0xFFFFC107;
	public static final int ORANGE_500 = 0xFFFF9800;
	public static final int DEEP_ORANGE_500 = 0xFFFF5722;
	public static final int BROWN_500 = 0xFF795548;
	public static final int GREY_500 = 0xFF9E9E9E;
	public static final int BLUE_GREY_500 = 0xFF607D8B;

	public static final int[] MATERIAL_COLORS = new int[]{
			RED_500,
			PINK_500,
			PURPLE_500,
			DEEP_PURPLE_500,
			INDIGO_500,
			BLUE_500,
			LIGHT_BLUE_500,
			CYAN_500,
			TEAL_500,
			GREEN_500,
			LIGHT_GREEN_500,
			LIME_500,
			YELLOW_500,
			AMBER_500,
			ORANGE_500,
			DEEP_ORANGE_500,
			BROWN_500,
			GREY_500,
			BLUE_GREY_500
	};

	// Used to cache values returned from extractVibrantColor to skip image analysis process.
	private static SparseIntArray paletteVibrantColorCache = new SparseIntArray(50);

	/**
	 * Creates and returns a Bitmap from a given Drawable.
	 *
	 * @param source The Drawable to extract the Bitmap from
	 * @return The Bitmap content of the Drawable
	 */
	@NonNull
	public static Bitmap extractBitmap(Drawable source) {
		Bitmap result;

		// Try to extract directly if the Drawable provides the right methods to do it
		if(source instanceof BitmapDrawable) {
			result = ((BitmapDrawable) source).getBitmap();
			if(result != null) return result;
		}

		// If we failed before, we need to do it manually
		if(source.getIntrinsicWidth() < 1 || source.getIntrinsicWidth() < 1) { // No size = no image
			result = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Solid color 1x1px Bitmap
		} else { // At least a 1x1px bitmap/image
			result = Bitmap.createBitmap(source.getIntrinsicWidth(), source.getMinimumHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(result);
		source.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		source.draw(canvas); // Draw the Drawable's content into the canvas writing into the Bitmap

		return result;
	}

	/**
	 * Extracts the most vibrant color from a given Bitmap and returns it, or returns a given
	 * fallback color if the operation fails.
	 *
	 * @param bitmap The Bitmap to scan for a vibrant color
	 * @param defaultColor The color to return if the operation fails
	 * @return Either a vibrant color in bitmap or defaultColor
	 */
	@ColorInt
	public static int extractVibrantColor(@NonNull Bitmap bitmap, @ColorInt final int defaultColor) {

		// Search if we already analysed this Bitmap, this speeds up the process a lot
		int cacheResult = paletteVibrantColorCache.get(bitmap.hashCode(), Integer.MAX_VALUE);
		if(cacheResult != Integer.MAX_VALUE) return cacheResult;

		Palette palette = Palette.from(bitmap)
				.addTarget(Target.VIBRANT)
//				.addFilter(new Palette.Filter() {
//					@Override
//					public boolean isAllowed(int rgb, float[] hsl) {
//						return false; // TODO: Check if color is similar to those in the MDGs?
//					}
//				})
				.generate();

		int result = palette.getVibrantColor(defaultColor);
		if(defaultColor == result) result = palette.getDominantColor(defaultColor);

		// Add Bitmap and result to cache and return it
		paletteVibrantColorCache.put(bitmap.hashCode(), result);
		return result;
	}

	/**
	 * Extracts the most vibrant color from a given Drawable and returns it, or returns a given
	 * fallback color if the operation fails.
	 *
	 * @param drawable The Drawable to scan for a vibrant color
	 * @param defaultColor The color to return if the operation fails
	 * @return Either a vibrant color in drawable or defaultColor
	 */
	public static int extractVibrantColor(Drawable drawable, @ColorInt final int defaultColor) {
		return extractVibrantColor(extractBitmap(drawable), defaultColor);
	}

	/**
	 * Lightens the given color by the percentage signified by the given fraction.
	 *
	 * @param color The color to lighten
	 * @param fraction How much to lighten
	 * @return The lightened color
	 */
	public static int lighten(@ColorInt int color, double fraction) {
		if(fraction <= 0) return color;

		// Make every part of the color lighter and merge back together
		int red = lightenColor(Color.red(color), fraction);
		int green = lightenColor(Color.green(color), fraction);
		int blue = lightenColor(Color.blue(color), fraction);

		return Color.argb(Color.alpha(color), red, green, blue);
	}

	/**
	 * Darkens the given color by the percentage signified by the given fraction.
	 *
	 * @param color The color to darken
	 * @param fraction How much to darken
	 * @return The darkened color
	 */
	public static int darken(@ColorInt int color, double fraction) {
		if(fraction <= 0) return color;

		// Darken every color by the percentage and recompose them into the single final color
		int red = darkenColor(Color.red(color), fraction);
		int green = darkenColor(Color.green(color), fraction);
		int blue = darkenColor(Color.blue(color), fraction);

		return Color.argb(Color.alpha(color), red, green, blue);
	}

	/**
	 * Obtains and returns a random color from the Material Design ones, at its "500" value.
	 *
	 * @return A random Material Design color
	 */
	public static int getRandomColor() {
		return MATERIAL_COLORS[new Random(System.currentTimeMillis()).nextInt(MATERIAL_COLORS.length)];
	}

	private static int darkenColor(int color, double fraction) {
		return (int) Math.max(color - (color * fraction), 0);
	}

	private static int lightenColor(int color, double fraction) {
		return (int) Math.min(color + (color * fraction), 255);
	}
}
