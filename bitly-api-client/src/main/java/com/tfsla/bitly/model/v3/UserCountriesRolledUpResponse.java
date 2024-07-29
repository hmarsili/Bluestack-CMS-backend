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
package com.tfsla.bitly.model.v3;

import java.util.List;

import com.tfsla.bitly.model.MetricsResponse;
import com.tfsla.bitly.model.ToStringSupport;

/**
 * <p>
 * Please see the bit.ly documentation for the <a href="http://dev.bitly.com/user_metrics.html#v3_user_countries">/v3/user/countries</a> request.
 * </p>
 * @author Patrick Huber (gmail: stackmagic)
 */
public class UserCountriesRolledUpResponse extends MetricsResponse {

	/** a list of countries referring traffic to this user's links */
	public List<UserCountry> user_countries;

	/** a country referring traffic to this user's links */
	public static class UserCountry extends ToStringSupport {

		/** the number of clicks referred from this country */
		public long clicks;

		/** the two-letter code of the referring country */
		public String country;
	}
}
