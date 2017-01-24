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

package dev.hellpie.apps.music09.concept.libraries.ghupdater.internal;

import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface SimpleGitHubAPI {

	SimpleGitHubAPI GITHUB_API = new Retrofit.Builder()
			.baseUrl("https://api.github.com/")
			.addConverterFactory(GsonConverterFactory.create(
					new GsonBuilder()
							.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
							.create()
			))
			.build()
			.create(SimpleGitHubAPI.class);

	@Headers({
			"User-Agent: HellPie/GH-Update-API",
			"Content-Type: application/json;charset=UTF-8",
			"Accept: application/vnd.github.v3+json",
	})
	@GET("repos/{userName}/{repoName}")
	Call<RepoResponse> repoExists(@Path("userName") String userName, @Path("repoName") String repoName);

	@Headers({
			"User-Agent: HellPie/GH-Update-API",
			"Content-Type: application/json;charset=UTF-8",
			"Accept: application/vnd.github.v3+json",
	})
	@GET("repos/{userName}/{repoName}/releases")
	Call<List<ReleaseResponse>> fetchReleases(@Path("userName") String userName, @Path("repoName") String repoName);
}
