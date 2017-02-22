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

package dev.hellpie.apps.music09.concept.services;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import dev.hellpie.apps.music09.concept.R;
import dev.hellpie.apps.music09.concept.ui.activities.PlayerActivity;
import dev.hellpie.libs.ghupdater.GHUpdateInfo;

public class UpdateReadyReceiver extends BroadcastReceiver {

	public static final String INTENT_EXTRAS_TITLE =
			"dev.hellpie.apps.music09.concept.services.UpdateReadyReceiver.INTENT_EXTRAS_TITLE";
	public static final String INTENT_EXTRAS_CHANGELOG =
			"dev.hellpie.apps.music09.concept.services.UpdateReadyReceiver.INTENT_EXTRAS_CHANGELOG";
	public static final String INTENT_EXTRAS_STATUS =
			"dev.hellpie.apps.music09.concept.services.UpdateReadyReceiver.INTENT_EXTRAS_STATUS";

	public static final String INTENT_ACTION_CHANGELOG =
			"dev.hellpie.apps.music09.concept.services.UpdateReadyReceiver.INTENT_ACTION_CHANGELOG";
	public static final String INTENT_ACTION_INSTALL =
			"dev.hellpie.apps.music09.concept.services.UpdateReadyReceiver.INTENT_ACTION_INSTALL";

	public static final int NOTFICATION_COMPLETED_ONCLICK = 4319;
	public static final int NOTIFICATION_COMPLETED_SHOW_CHANGELOG = 9722;
	public static final int NOTIFICATION_COMPLETED_INSTALL = 5051;
	public static final int NOTIFICATION_COMPLETED_ID = 1337;

	private UpdateReceiverCallbacks callbacks;
	private GHUpdateInfo updateInfo;

	@Keep
	public UpdateReadyReceiver() { /* System Instantiable */ }

	public UpdateReadyReceiver(Activity creator, UpdateReceiverCallbacks callbacks) {
		this.callbacks = callbacks;

		if(creator == null) return;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		switch(intent.getAction()) {
			case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
				updateInfo = callbacks.getUpdateInfo();
				onDownloadCompleted(context, intent);
				break;
			case DownloadManager.ACTION_NOTIFICATION_CLICKED:
				updateInfo = callbacks.getUpdateInfo();
				onNotificationClicked(context);
				break;
			default:
				break;
		}
	}

	private void onDownloadCompleted(Context context, Intent intent) {

		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

		long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		if(downloadId == 0) return;

		Uri uri = manager.getUriForDownloadedFile(downloadId);

		Intent showChangelogIntent = new Intent(context, PlayerActivity.class)
				.setData(uri)
				.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
				.setAction(INTENT_ACTION_CHANGELOG)
				.putExtra(INTENT_EXTRAS_STATUS, true)
				.putExtra(INTENT_EXTRAS_TITLE, updateInfo.getReleaseName())
				.putExtra(INTENT_EXTRAS_CHANGELOG, updateInfo.getChangelog());

		PendingIntent notiClicked = PendingIntent.getActivity(
				context,
				NOTFICATION_COMPLETED_ONCLICK,
				showChangelogIntent,
				PendingIntent.FLAG_CANCEL_CURRENT
		);

		PendingIntent showChangelog = PendingIntent.getActivity(
				context,
				NOTIFICATION_COMPLETED_SHOW_CHANGELOG,
				showChangelogIntent,
				PendingIntent.FLAG_CANCEL_CURRENT
		);

		PendingIntent install = PendingIntent.getActivity(
				context,
				NOTIFICATION_COMPLETED_INSTALL,
				new Intent(Intent.ACTION_VIEW).setDataAndType(
						uri,
						"application/vnd.android.package-archive"
				).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
				PendingIntent.FLAG_UPDATE_CURRENT
		);

		Notification notification = new NotificationCompat.Builder(context)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setCategory(NotificationCompat.CATEGORY_STATUS)
				.setSmallIcon(R.drawable.ic_audiotrack)
				.setColor(ContextCompat.getColor(context, R.color.colorAccent))
				.setStyle(new NotificationCompat.BigTextStyle())
				.setContentTitle(String.format(
						context.getString(R.string.update_available_dialog_title),
						updateInfo.getReleaseName()
				))
				.setContentText(updateInfo.getChangelog())
				.setContentInfo(String.format("%.2f MB", (float) (updateInfo.getFileSize()/1024^2)))
				.setContentIntent(notiClicked)
				.addAction(new NotificationCompat.Action(
						R.drawable.ic_comment,
						context.getString(R.string.update_available_notif_changelog),
						showChangelog
				))
				.addAction(new NotificationCompat.Action(
						R.drawable.ic_update,
						context.getString(R.string.update_available_notif_install),
						install
				))
				.setWhen(updateInfo.getReleaseDate().getTime())
				.build();

		NotificationManagerCompat.from(context).notify(NOTIFICATION_COMPLETED_ID, notification);
		callbacks.updateReady(updateInfo.getReleaseName(), updateInfo.getChangelog(), uri);
	}

	private void onNotificationClicked(Context context) {

		context.startActivity(new Intent(context, PlayerActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
				.setAction(INTENT_ACTION_CHANGELOG)
				.putExtra(INTENT_EXTRAS_STATUS, false)
				.putExtra(INTENT_EXTRAS_TITLE, updateInfo.getReleaseName())
				.putExtra(INTENT_EXTRAS_CHANGELOG, updateInfo.getChangelog()));
	}

	public interface UpdateReceiverCallbacks {
		@NonNull GHUpdateInfo getUpdateInfo();
		void updateReady(@NonNull String name, @NonNull String changelog, @NonNull Uri uri);
	}
}
