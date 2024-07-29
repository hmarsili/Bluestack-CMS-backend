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

import com.tfsla.bitly.model.MetricsResponse;

/**
 * <p>
 * Please see the bit.ly documentation for the <a href="http://dev.bitly.com/user_metrics.html#v3_user_shorten_counts">/v3/user/shorten_counts</a> request.
 * </p>
 * @author Patrick Huber (gmail: stackmagic)
 */
public class UserShortenCountsRolledUpResponse extends MetricsResponse {

	/** the user shorten counts */
	public long user_shorten_counts;
}
