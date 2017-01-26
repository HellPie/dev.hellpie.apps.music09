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

package dev.hellpie.apps.music09.concept.libraries.ghupdater;

import android.support.annotation.NonNull;

import java.net.URI;
import java.util.Date;

import dev.hellpie.apps.music09.concept.libraries.ghupdater.internal.AssetModel;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.internal.ReleaseResponse;

public final class GHUpdateInfo {

	private URI downloadURI;
	private String fileName;
	private int fileSize;

	private String releaseName;
	private String changelog;
	private boolean isPrerelease;

	private Date releaseDate;

	private ReleaseResponse chosenRelease;
	private AssetModel choosenAsset;

	private GHUpdateInfo() { /* Do Not Instantiate */ }

	public URI getDownloadURI() {
		return downloadURI;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public String getChangelog() {
		return changelog;
	}

	public boolean isPrerelease() {
		return isPrerelease;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	/*package*/ ReleaseResponse getChosenRelease() {
		return chosenRelease;
	}

	/*package*/ AssetModel getChoosenAsset() {
		return choosenAsset;
	}

	@Override
	public String toString() {
		return "Download Url: " + downloadURI.toString()
				+ "\nFile Name: " + fileName
				+ "\nFile Size: " + String.valueOf(((float) fileSize / (1024*1024))) + "MB"
				+ "\n\nRelease Name: " + releaseName
				+ "\nChangelog: " + changelog
				+ "\nRelease Status: " + (isPrerelease ? "Prerelease" : "Release")
				+ "\nRelease Date: " + releaseDate.toString();
	}

	public static final class Builder {
		private final GHUpdateInfo info = new GHUpdateInfo();

		public Builder(@NonNull URI downloadUri) {
			info.downloadURI = downloadUri;
		}

		public Builder withFileName(@NonNull String fileName) {
			info.fileName = fileName;
			return this;
		}

		public Builder withFileSize(int fileSize) {
			info.fileSize = fileSize;
			return this;
		}

		public Builder withReleaseName(@NonNull String releaseName) {
			info.releaseName = releaseName;
			return this;
		}

		public Builder withChangelog(@NonNull String changelog) {
			info.changelog = changelog;
			return this;
		}

		public Builder markPrerelease(boolean prerelease) {
			info.isPrerelease = prerelease;
			return this;
		}

		public Builder withReleaseDate(@NonNull Date releaseDate) {
			info.releaseDate = releaseDate;
			return this;
		}

		@NonNull
		public GHUpdateInfo build() {
			return info;
		}

		@NonNull
		/*package*/ static GHUpdateInfo fromData(@NonNull ReleaseResponse release, @NonNull AssetModel asset) {
			GHUpdateInfo info = new GHUpdateInfo();
			info.downloadURI = URI.create(asset.browserDownloadUrl);
			info.fileName = asset.name;
			info.fileSize = asset.size;
			info.releaseName = release.name;
			info.changelog = release.body;
			info.isPrerelease = release.prerelease;
			info.releaseDate = release.publishedAt;
			info.chosenRelease = release;
			info.choosenAsset = asset;
			return info;
		}
	}
}
