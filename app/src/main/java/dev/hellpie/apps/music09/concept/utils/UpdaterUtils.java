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
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;

import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHConfig;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.GHUpdateInfo;

public class UpdaterUtils {

	private static final GHConfig.Builder CONFIG = new GHConfig.Builder("HellPie", "dev.hellpie.apps.music09")
			.withMinimumDate(new Date(1487505600000L)); // 19-Feb-2017 13:00:00 UTC+01:00

	private UpdaterUtils() { /* Utils - Never instantiate */ }

	public static void download(@NonNull GHUpdateInfo info, @NonNull Context context) {

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

	public static GHConfig getConfig(@NonNull final Context ctx) {
		return CONFIG.acceptPrereleases(PrefsUtils.getBool(ctx, PrefsUtils.PREF_UPDATE_ENA_BETA))
				.withMIMETypeFilter(new GHConfig.MIMETypeFilter() {
					@Override
					public boolean isValidFileName(String fileName) {
						String lower = fileName.toLowerCase();
						boolean beta = PrefsUtils.getBool(ctx, PrefsUtils.PREF_UPDATE_ENA_BETA);
						boolean alpha = beta && PrefsUtils.getBool(ctx, PrefsUtils.PREF_UPDATE_ENA_ALPHA);

						// Adjust the config to only consider valid update channels
						return ((beta && lower.contains("beta")) || (alpha && lower.contains("alpha")))
								&& lower.endsWith(".apk");
					}

					@Override
					public boolean isValidMIMEType(String mimeType) {
						return mimeType.equals("application/vnd.android.package-archive");
					}
				})
				.build();
	}
}
