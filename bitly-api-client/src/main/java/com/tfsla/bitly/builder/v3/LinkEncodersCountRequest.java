package com.tfsla.bitly.builder.v3;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.tfsla.bitly.builder.Request;
import com.tfsla.bitly.model.Response;
import com.tfsla.bitly.model.v3.LinkEncodersCountResponse;

/**
 * <p>
 * Please see the bit.ly documentation for the <a href="http://dev.bitly.com/link_metrics.html#v3_link_encoders_count">/v3/link/encoders_count </a> request.
 * </p>
 * @author Patrick Huber (gmail: stackmagic)
 */
public class LinkEncodersCountRequest extends Request<LinkEncodersCountResponse> {

	/**
	 * Create a new request builder
	 * @param accessToken the access token to access the bitly api
	 */
	public LinkEncodersCountRequest(String accessToken) {
		super(accessToken);
	}

	@Override
	public String getEndpoint() {
		return "https://api-ssl.bitly.com/v3/link/encoders_count";
	}

	@Override
	protected Type getTypeForGson() {
		return new TypeToken<Response<LinkEncodersCountResponse>>() {}.getType();
	}

	/**
	 * Set the link
	 * @param link a bitly link
	 * @return this builder
	 */
	public LinkEncodersCountRequest setLink(String link) {
		addQueryParameter("link", link);
		return this;
	}
}
