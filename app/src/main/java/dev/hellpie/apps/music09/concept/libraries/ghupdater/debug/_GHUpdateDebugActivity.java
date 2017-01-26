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

package dev.hellpie.apps.music09.concept.libraries.ghupdater.debug;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHConfig;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHUpdateInfo;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHUpdaterUtils;

public class _GHUpdateDebugActivity extends AppCompatActivity {

	@BindView(R.id.debug_ghupdate)
	TextView debugOutput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout._debug_activity_ghupdate);

		ButterKnife.bind(this);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					GHUpdateInfo info = new GHUpdaterUtils(new GHConfig.Builder("HellPie", "dev.hellpie.apps.music09")
							.acceptPrereleases(true)
							.withMIMETypeFilter(new GHConfig.MIMETypeFilter() {
								@Override
								public boolean isValidFileName(String fileName) {
									return fileName.endsWith(".apk");
								}

								@Override
								public boolean isValidMIMEType(String mimeType) {
									return mimeType.equals("application/vnd.android.package-archive");
								}
							})
							.build()
					).getLatestVersion();

					if(info == null) setDebug("Null Release was found.");
					else setDebug(info.toString());

				} catch(InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void setDebug(final String debug) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				debugOutput.setText(debug);
			}
		});
	}
}
