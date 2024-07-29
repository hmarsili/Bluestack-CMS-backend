package org.opencms.configuration;

import org.opencms.configuration.uuid.IPSeeker;
import org.opencms.configuration.uuid.MacAddressSeeker;

public class License {
	private String MACAddress = MacAddressSeeker.getMACAddress();
	private String ip= IPSeeker.getIPAddress();
	
	private String site=null;
	private String userName=null;
	private String userMail=null;
	private String licenseKey=null;
	private String views=null;
	private String expiration=null;
	private boolean firstTime=true;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		if (this.userName==null)
			this.userName = userName;
	}
	public String getUserMail() {
		return userMail;
	}
	public void setUserMail(String userMail) {
		if (this.userMail==null)
			this.userMail = userMail;
	}
	public String getLicenseKey() {
		return licenseKey;
	}
	public void setLicenseKey(String licenseKey) {
		if (this.licenseKey==null)
			this.licenseKey = licenseKey;
	}
	public String getViews() {
		return views;
	}
	public void setViews(String views) {
		if (this.views==null)
			this.views = views;
	}
	public String getExpiration() {
		return expiration;
	}
	public void setExpiration(String expiration) {
		if (this.expiration==null)
			this.expiration = expiration;
	}
	public String getMACAddress() {
		return MACAddress;
	}
	public String getIp() {
		return ip;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		if (this.site==null)
			this.site = site;
	}
	public boolean isFirstTime() {
		boolean _firstTime = firstTime;
		firstTime = false;
		return _firstTime;
	}
	
}
