package org.opencms.file;

import org.opencms.util.CmsUUID;

public class CmsUserExternalProvider {
	
	public CmsUserExternalProvider(CmsUUID id, String name, String provider, String provUserId, String token ) {
		this.m_id = id;
		this.m_name = name;
		this.token = token;
		this.provider = provider;
		this.providerUserId = provUserId;
	}
	
	 /** The fully qualified name of this principal. */
    protected String m_name;
	
	/** The unique id of this principal. */
    protected CmsUUID m_id;
    
    protected String provider;
    
    protected String providerUserId;
    
    protected String token;

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public CmsUUID getId() {
		return m_id;
	}

	public void setId(CmsUUID id) {
		this.m_id = id;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderUserId() {
		return providerUserId;
	}

	public void setProviderUserId(String providerUserId) {
		this.providerUserId = providerUserId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
    
    
}
