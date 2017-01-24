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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public final class ReleaseResponse {

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("assets_url")
	@Expose
	public String assetsUrl;

	@SerializedName("upload_url")
	@Expose
	public String uploadUrl;

	@SerializedName("html_url")
	@Expose
	public String htmlUrl;

	@SerializedName("id")
	@Expose
	public int id;

	@SerializedName("tag_name")
	@Expose
	public String tagName;

	@SerializedName("target_commitish")
	@Expose
	public String targetCommitish;

	@SerializedName("name")
	@Expose
	public String name;

	@SerializedName("draft")
	@Expose
	public boolean draft;

	@SerializedName("author")
	@Expose
	public UserModel author;

	@SerializedName("prerelease")
	@Expose
	public boolean prerelease;

	@SerializedName("created_at")
	@Expose
	public Date createdAt;

	@SerializedName("published_at")
	@Expose
	public Date publishedAt;

	@SerializedName("assets")
	@Expose
	public List<AssetModel> assets = null;

	@SerializedName("tarball_url")
	@Expose
	public String tarballUrl;

	@SerializedName("zipball_url")
	@Expose
	public String zipballUrl;

	@SerializedName("body")
	@Expose
	public String body;
}
