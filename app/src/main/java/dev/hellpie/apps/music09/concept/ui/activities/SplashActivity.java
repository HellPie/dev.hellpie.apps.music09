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

package dev.hellpie.apps.music09.concept.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.libraries.piemissions.PiemissionRequest;
import dev.hellpie.apps.music09.concept.libraries.piemissions.PiemissionsCallback;
import dev.hellpie.apps.music09.concept.libraries.piemissions.PiemissionsUtils;

public class SplashActivity extends Activity {
	public static final int PERMISSIONS_CODE = 9001;

	// This Activity will be used to show a branded splash screen.
	// This is a common technique and it allows to hide app load times
	// behind a good looking waiting room so that the user won't think
	// the app hanged.
	// This screen will stay up as long as it takes for the true activity
	// to load and then will automatically disappear.
	// This Activity lacks "setContentView()" because loading a layout
	// would mean adding a loading screen to this activity, too, so
	// the branding image will be set via custom style in "styles.xml".
	// See: https://www.bignerdranch.com/blog/splash-screens-the-right-way/
	// Unofficial docs: http://www.materialdoc.com/splash-screens/
	// Official performance explanation video: https://youtu.be/Vw1G1s73DsY
	// Design guidelines: https://material.google.com/patterns/launch-screens.html
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);

		// Permissions in Android 6+ are at runtime, init my helper library
		PiemissionsUtils.init(this);

		// Create a request
		final PiemissionRequest request = new PiemissionRequest(PERMISSIONS_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
		request.setCallback(new PiemissionsCallback() {
			@Override
			public void onGranted() {
				startActivity(new Intent(SplashActivity.this, PlayerActivity.class)); // Start the activity right away
				finish(); // Stop displaying the splash screen ONLY after the real activity finished loading
			}

			@Override
			public boolean onDenied(HashMap<String, Boolean> rationalizablePermissions) {
				if(rationalizablePermissions.get(Manifest.permission.READ_EXTERNAL_STORAGE)) {
					new AlertDialog.Builder(SplashActivity.this)
							.setMessage(R.string.permissions_read_storage)
							.setCancelable(true)
							.setPositiveButton(R.string.permissions_accept_button, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									PiemissionsUtils.requestPermission(request);
								}
							})
							.setNegativeButton(R.string.permissions_cancel_button, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
									SplashActivity.this.finish();
									SplashActivity.this.finishAffinity();
								}
							})
							.show();
				}
				return false;
			}
		});

		PiemissionsUtils.requestPermission(request);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		PiemissionsUtils.onRequestResult(requestCode, permissions, grantResults);
	}
}
