package com.tfsla.cdnIntegration.util;

import java.util.Set;
import java.util.TreeSet;

public class ClientCredential {

    public static final int DEFAULT_MAX_BODY_SIZE_IN_BYTES = 131072;

    private String accessToken;

    private String clientSecret;

    private String clientToken;

    private TreeSet<String> headersToSign;

    private String host;

    private Integer maxBodySize;
    
    public ClientCredential() {
    	headersToSign = new TreeSet<String>();
	}

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientToken() {
        return clientToken;
    }

    public Set<String> getHeadersToSign() {
        return headersToSign;
    }



    public String getHost() {
        return host;
    }



    public int getMaxBodySize() {
        if (maxBodySize == null) {
            return DEFAULT_MAX_BODY_SIZE_IN_BYTES;
        }
        return maxBodySize;
    }

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	public void setHeadersToSign(TreeSet<String> headersToSign) {
		this.headersToSign = headersToSign;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setMaxBodySize(Integer maxBodySize) {
		this.maxBodySize = maxBodySize;
	}

}

