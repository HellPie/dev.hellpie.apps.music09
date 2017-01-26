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

import java.io.IOException;
import java.util.List;

import dev.hellpie.apps.music09.concept.libraries.ghupdater.internal.ReleaseResponse;
import dev.hellpie.apps.music09.concept.libraries.ghupdater.internal.SimpleGitHubAPI;

public final class GHUtils {
	private GHUtils() { /* Do Not Instantiate */ }

	public static boolean isValidRepo(@NonNull String userName, @NonNull String repoName) throws IOException {
		return SimpleGitHubAPI.GITHUB_API.repoExists(userName, repoName).execute().body().exists();
	}

	public static List<ReleaseResponse> getReleases(@NonNull String userName, @NonNull String repoName) throws IOException {
		return SimpleGitHubAPI.GITHUB_API.fetchReleases(userName, repoName).execute().body();
	}
}
