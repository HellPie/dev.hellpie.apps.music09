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

public final class AssetModel {

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("id")
	@Expose
	public int id;

	@SerializedName("name")
	@Expose
	public String name;

	@SerializedName("label")
	@Expose
	public Object label;

	@SerializedName("uploader")
	@Expose
	public UserModel uploader;

	@SerializedName("content_type")
	@Expose
	public String contentType;

	@SerializedName("state")
	@Expose
	public String state;

	@SerializedName("size")
	@Expose
	public int size;

	@SerializedName("download_count")
	@Expose
	public int downloadCount;

	@SerializedName("created_at")
	@Expose
	public Date createdAt;

	@SerializedName("updated_at")
	@Expose
	public Date updatedAt;

	@SerializedName("browser_download_url")
	@Expose
	public String browserDownloadUrl;
}
