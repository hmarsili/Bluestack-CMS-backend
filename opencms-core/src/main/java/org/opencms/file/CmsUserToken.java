package org.opencms.file;

import org.opencms.util.CmsUUID;

public class CmsUserToken {
	
	
	public CmsUserToken(CmsUUID id, String name, String token , long lastLogin, String salt, String browserId) {
		this.m_id = id;
		this.m_name = name;
		this.token = token;
		this.m_lastlogin = lastLogin;
		this.salt = salt;
		this.browserId = browserId;
	}
	
	private String token;
	
	 /** The fully qualified name of this principal. */
    protected String m_name;
	
	/** The unique id of this principal. */
    protected CmsUUID m_id;
	
	/** The last login date of this user. */
    protected long m_lastlogin;
    
    protected String salt;
    
    protected String browserId;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

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

	public long getLastlogin() {
		return m_lastlogin;
	}

	public void setLastlogin(long lastlogin) {
		this.m_lastlogin = lastlogin;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getBrowserId() {
		return browserId;
	}

	public void setBrowserId(String browserId) {
		this.browserId = browserId;
	}
    
}
