/*
 * Copyright (c) Patrick Huber (gmail: stackmagic)
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
 * limitations under the License.
 */
package com.tfsla.bitly.builder.v3;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.tfsla.bitly.builder.MetricsRequest;
import com.tfsla.bitly.model.Response;
import com.tfsla.bitly.model.v3.UserPopularLinksResponse;

/**
 * <p>
 * Please see the bit.ly documentation for the <a href="http://dev.bitly.com/user_metrics.html#v3_user_popular_links">/v3/user/popular_links</a> request.
 * </p>
 * @author Patrick Huber (gmail: stackmagic)
 */
public class UserPopularLinksRequest extends MetricsRequest<UserPopularLinksRequest, UserPopularLinksResponse> {

	/**
	 * Create a new request builder
	 * @param accessToken the access token to access the bitly api
	 */
	public UserPopularLinksRequest(String accessToken) {
		super(accessToken);
	}

	@Override
	public String getEndpoint() {
		return "https://api-ssl.bitly.com/v3/user/popular_links";
	}

	@Override
	protected Type getTypeForGson() {
		return new TypeToken<Response<UserPopularLinksResponse>>() {}.getType();
	}
}
