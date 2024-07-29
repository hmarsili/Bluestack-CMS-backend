package org.opencms.file;

import org.opencms.util.CmsUUID;

public class CmsUserTwoFactor {

	
	public CmsUserTwoFactor(CmsUUID id, long lastmodified, String secret, long counter, String temporalToken, String uniqueCodes) {
		super();
		this.m_id = id;
		this.m_lastmodified = lastmodified;
		this.secret = secret;
		this.counter = counter;
		this.temporalToken = temporalToken;
		this.uniqueCodes = uniqueCodes;
	}

	/** The unique id of this principal. */
    protected CmsUUID m_id;
    
    /** The last modified date of this user login. */
    protected long m_lastmodified;
    
    protected String secret;
     
    protected long counter;
    
    protected String temporalToken;
    
    protected String uniqueCodes;

	public CmsUUID getId() {
		return m_id;
	}

	public void setId(CmsUUID m_id) {
		this.m_id = m_id;
	}

	public long getLastmodified() {
		return m_lastmodified;
	}

	public void setLastmodified(long m_lastmodified) {
		this.m_lastmodified = m_lastmodified;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	public void setTemporalToken(String temporalToken) {
		this.temporalToken = temporalToken;
	}

	public String getTemporalToken() {
		return temporalToken;
	}

	public void setUniqueCodes(String uniqueCodes) {
		this.uniqueCodes = uniqueCodes;
	}

	public String getUniqueCodes() {
		return uniqueCodes;
	}
	
	
	
}
