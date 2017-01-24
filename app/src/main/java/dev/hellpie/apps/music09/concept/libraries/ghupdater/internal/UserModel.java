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

public class UserModel {

	@SerializedName("login")
	@Expose
	public String login;

	@SerializedName("id")
	@Expose
	public int id;

	@SerializedName("avatar_url")
	@Expose
	public String avatarUrl;

	@SerializedName("gravatar_id")
	@Expose
	public String gravatarId;

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("html_url")
	@Expose
	public String htmlUrl;

	@SerializedName("followers_url")
	@Expose
	public String followersUrl;

	@SerializedName("following_url")
	@Expose
	public String followingUrl;

	@SerializedName("gists_url")
	@Expose
	public String gistsUrl;

	@SerializedName("starred_url")
	@Expose
	public String starredUrl;

	@SerializedName("subscriptions_url")
	@Expose
	public String subscriptionsUrl;

	@SerializedName("organizations_url")
	@Expose
	public String organizationsUrl;

	@SerializedName("repos_url")
	@Expose
	public String reposUrl;

	@SerializedName("events_url")
	@Expose
	public String eventsUrl;

	@SerializedName("received_events_url")
	@Expose
	public String receivedEventsUrl;

	@SerializedName("type")
	@Expose
	public String type;

	@SerializedName("site_admin")
	@Expose
	public boolean siteAdmin;
}
