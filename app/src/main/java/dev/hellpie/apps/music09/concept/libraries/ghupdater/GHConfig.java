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

import java.util.Date;

public final class GHConfig {

	private String userName = null;
	private String repoName = null;

	private boolean acceptPrereleases = false;
	private Date minimumDate = new Date(0);

	private MIMETypeFilter mimeTypeFiler = new MIMETypeFilter() {
		@Override
		public boolean isValidFileName(String fileName) {
			return false;
		}

		@Override
		public boolean isValidMIMEType(String mimeType) {
			return false;
		}
	};

	private GHConfig() { /* Do Not Instantiate */ }

	public final String getUserName() {
		return userName;
	}

	public final String getRepoName() {
		return repoName;
	}

	public final boolean acceptPrereleases() {
		return acceptPrereleases;
	}

	public final Date getMinimumDate() {
		return minimumDate;
	}

	public final boolean isValidFile(String fileName) {
		return mimeTypeFiler.isValidFileName(fileName);
	}

	public final boolean isValidMIMEType(String mimeType) {
		return mimeTypeFiler.isValidMIMEType(mimeType);
	}

	public static final class Builder {
		private final GHConfig config = new GHConfig();

		public Builder(@NonNull String userName, @NonNull String repoName) {
			config.userName = userName;
			config.repoName = repoName;
		}

		public Builder acceptPrereleases(boolean acceptPrereleases) {
			config.acceptPrereleases = acceptPrereleases;
			return this;
		}

		public Builder withMinimumDate(Date minimumDate) {
			config.minimumDate = minimumDate;
			return this;
		}

		public Builder withMIMETypeFilter(@NonNull MIMETypeFilter mimeTypeFilter) {
			config.mimeTypeFiler = mimeTypeFilter;
			return this;
		}

		public final GHConfig build() {
			return config;
		}
	}

	public interface MIMETypeFilter {
		boolean isValidFileName(String fileName);
		boolean isValidMIMEType(String mimeType);
	}
}
