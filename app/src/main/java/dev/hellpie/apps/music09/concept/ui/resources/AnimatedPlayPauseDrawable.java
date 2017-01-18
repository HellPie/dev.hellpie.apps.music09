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

package dev.hellpie.apps.music09.concept.ui.resources;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Property;

import dev.hellpie.apps.music09.concept.utils.UIUtils;

/**
 * TAKEN FROM: https://github.com/alexjlockwood/material-pause-play-animation/blob/master/app/src/main/java/com/alexjlockwood/example/playpauseanimation/PlayPauseDrawable.java
 *
 * CHANGES:
 * - Fixed sizes to match the Material Design Guidelines
 * - Removed dependency from resource files
 * - Fixed artifact between the two bars when morphed in play mode
 * - Fixed artifact on the point of the play morphed shape
 * - Added Material's default animation duration for easy picking
 */
public class AnimatedPlayPauseDrawable extends Drawable {

	public static final int DEFAULT_ANIMATION_DURATION = 300;

	private static final Property<AnimatedPlayPauseDrawable, Float> PROGRESS =
			new Property<AnimatedPlayPauseDrawable, Float>(Float.class, "progress") {
				@Override
				public Float get(AnimatedPlayPauseDrawable d) {
					return d.getProgress();
				}

				@Override
				public void set(AnimatedPlayPauseDrawable d, Float value) {
					d.setProgress(value);
				}
			};

	private final Path mLeftPauseBar = new Path();
	private final Path mRightPauseBar = new Path();
	private final Paint mPaint = new Paint();
	private final RectF mBounds = new RectF();
	private final float mPauseBarWidth;
	private final float mPauseBarHeight;
	private final float mPauseBarDistance;

	private float mWidth;
	private float mHeight;

	private float mProgress;
	private boolean mIsPlay;

	public AnimatedPlayPauseDrawable(Context context) {
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.WHITE);
		mPauseBarWidth = UIUtils.dpToPx(5, context);
		mPauseBarHeight = UIUtils.dpToPx(15, context);
		mPauseBarDistance = UIUtils.dpToPx(5, context) - 1;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mBounds.set(bounds);
		mWidth = mBounds.width();
		mHeight = mBounds.height();
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		mLeftPauseBar.rewind();
		mRightPauseBar.rewind();

		// The current distance between the two pause bars.
		final float barDist = lerp(mPauseBarDistance, 0, mProgress);
		// The current width of each pause bar.
		final float barWidth = lerp(mPauseBarWidth, mPauseBarHeight / 2f, mProgress);
		// The current position of the left pause bar's top left coordinate.
		final float firstBarTopLeft = lerp(0, barWidth, mProgress);
		// The current position of the right pause bar's top right coordinate.
		final float secondBarTopRight = lerp(2 * barWidth + barDist, barWidth + barDist, mProgress);

		// Fixes the tiny 0.5px white bar in the original code and makes the point of the
		// triangle a little bit less stretched forward
		// This is really hacky but it's faster than implementing a better line drawing
		// as it would be much more complex
		float fixedLeftBarHeight = mIsPlay ? (-mPauseBarHeight + 2) : -mPauseBarHeight;
		float fixedLeftBarWidth = mIsPlay ? (barWidth + 1) : barWidth;
		float fixedRightBarHeight = mIsPlay ? (-mPauseBarHeight + 1) : -mPauseBarHeight;
		float fixedRightBarDist = mIsPlay ? (barDist - 0.5f) : barDist;

		// Draw the left pause bar. The left pause bar transforms into the
		// top half of the play button triangle by animating the position of the
		// rectangle's top left coordinate and expanding its bottom width.
		mLeftPauseBar.moveTo(0, 0);
		mLeftPauseBar.lineTo(firstBarTopLeft, fixedLeftBarHeight);
		mLeftPauseBar.lineTo(fixedLeftBarWidth, fixedLeftBarHeight);
		mLeftPauseBar.lineTo(fixedLeftBarWidth, 0);
		mLeftPauseBar.close();

		// Draw the right pause bar. The right pause bar transforms into the
		// bottom half of the play button triangle by animating the position of the
		// rectangle's top right coordinate and expanding its bottom width.
		mRightPauseBar.moveTo(barWidth + fixedRightBarDist, 0);
		mRightPauseBar.lineTo(barWidth + fixedRightBarDist, fixedRightBarHeight);
		mRightPauseBar.lineTo(secondBarTopRight, fixedRightBarHeight);
		mRightPauseBar.lineTo(2 * barWidth + barDist, 0);
		mRightPauseBar.close();

		canvas.save();

		// Translate the play button a tiny bit to the right so it looks more centered.
		canvas.translate(lerp(0, mPauseBarHeight / 8f, mProgress), 0);

		// (1) Pause --> Play: rotate 0 to 90 degrees clockwise.
		// (2) Play --> Pause: rotate 90 to 180 degrees clockwise.
		final float rotationProgress = mIsPlay ? 1 - mProgress : mProgress;
		final float startingRotation = mIsPlay ? 90 : 0;
		canvas.rotate(lerp(startingRotation, startingRotation + 90, rotationProgress), mWidth / 2f, mHeight / 2f);

		// Position the pause/play button in the center of the drawable's bounds.
		canvas.translate(mWidth / 2f - ((2 * barWidth + barDist) / 2f), mHeight / 2f + (mPauseBarHeight / 2f));

		// Draw the two bars that form the animated pause/play button.
		canvas.drawPath(mLeftPauseBar, mPaint);
		canvas.drawPath(mRightPauseBar, mPaint);

		canvas.restore();
	}

	public Animator getPausePlayAnimator() {
		final Animator anim = ObjectAnimator.ofFloat(this, PROGRESS, mIsPlay ? 1 : 0, mIsPlay ? 0 : 1);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mIsPlay = !mIsPlay;
			}
		});
		return anim;
	}

	public boolean isPlay() {
		return mIsPlay;
	}

	private void setProgress(float progress) {
		mProgress = progress;
		invalidateSelf();
	}

	private float getProgress() {
		return mProgress;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/**
	 * Linear interpolate between a and b with parameter t.
	 */
	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
