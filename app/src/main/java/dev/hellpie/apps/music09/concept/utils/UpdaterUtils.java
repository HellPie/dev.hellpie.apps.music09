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


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Date;

import dev.hellpie.apps.music09.concept.BuildConfig;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHConfig;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHUpdateInfo;

public class UpdaterUtils {

	public static final GHConfig UPDATER_CONFIG = new GHConfig.Builder("HellPie", "dev.hellpie.apps.music09")
			.acceptPrereleases(BuildConfig.VERSION_NAME.toLowerCase().contains("beta"))
			.withMinimumDate(new Date(1485451800000L)) // 26-Jan-2017 18:30:00 UTC+01:00
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
			.build();

	private WeakReference<Context> appContextRef;
	public BroadcastReceiver updateReceiver;

	public UpdaterUtils(@NonNull Context context) {
		appContextRef = new WeakReference<>(context);
	}

	public void download(@NonNull GHUpdateInfo info) {
		Context context = appContextRef.get();
		if(context == null) return;

		String downloadUrl = info.getDownloadURI().toString();

		final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
				.setAllowedOverMetered(false)
				.setAllowedOverRoaming(false)
				.setVisibleInDownloadsUi(false)
				.setMimeType("application/vnd.android.package-archive")
				.setTitle(info.getReleaseName())
				.setDescription(info.getFileName())
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

		downloadManager.enqueue(request);
	}
}
