package org.opencms.db;

import org.opencms.util.CmsStringUtil;

public class CmsTwoFactorLoginConfiguration {
	
	/** Default number of digits counter based one-time password use. */
    protected static final int DEFAULT_HOTP_DIGITS = 8;
    
	/** Default number of digits time based one-time password use. */
    protected static final int DEFAULT_TOTP_DIGITS = 6;

	/** Default duration in seconds the time based one-time password is valid. */
    protected static final int DEFAULT_TOTP_DURATION = 30;

	/** Default duration in seconds the time based one-time password is valid. */
    protected static final int DEFAULT_HOTP_VALIDTIME = 15;

    /** Default label of time based one-time password. */
    protected static final String DEFAULT_TOTP_LABEL = "CmsMedios Admin Login";
    
    /** SHA1 algorithm for the one-time password. */
    protected static final String OTP_ALGORITHM_SHA1 = "SHA1";

    /** SHA256 algorithm for the one-time password. */
    protected static final String OTP_ALGORITHM_SHA256 = "SHA256";

    /** SHA512 algorithm for the one-time password. */
    protected static final String OTP_ALGORITHM_SHA512 = "SHA512";

    /** Default algorithm for the based one-time password. */
    protected static final String DEFAULT_OTP_ALGORITHM = OTP_ALGORITHM_SHA256;
    
    protected static final int DEFAULT_TOKEN_DURATION = 1440;
    
	/* if counter based one-time password is enabled */
	private boolean b_hotpEnabled;

	/* if time based one-time password is enabled */
	private boolean b_totpEnabled;
	
	/* if time based one-time codes is enabled */
	private boolean b_otcpEnabled;
	
	/* number of digits the counter based one-time password generate */
	private int i_hoptDigits;

	/* number of digits the time based one-time password generate */
	private int i_toptDigits;

	/* duration in seconds the time based one-time password is valid */
	private int i_toptDuration;

	/* duration in minutes the counter based one-time password is valid */
	private int i_hoptValidTime;

	/* Label of the time based one-time password */
	private String m_toptLabel;
	
	/* Encryptation algorithm for the one-time password */
	private String m_optAlgorithm;

	/* if counter based one-time password by sms is enabled */
	private boolean b_smsHotpSendEnabled;

	/* if counter based one-time password by email is enabled */
	private boolean b_emailHotpSendEnabled;

	/* the endpoint for the code sms delivery */
	private String m_smsEndpoint;
	
	private String m_rsaPuKToken;
	private String m_rsaPrKToken;
	
	private String m_rsaKeyExtChannel;
	
	private int i_tokenDuration;
	
    /**
     * Constructor that initializes all names with default values.<p>
     * 
     * See the constants of this class for the defaule values that are uses.<p> 
     */
    public CmsTwoFactorLoginConfiguration() {

    	b_hotpEnabled=true;
    	b_totpEnabled=true;
    	b_otcpEnabled=true;

    	i_hoptDigits = DEFAULT_HOTP_DIGITS;
    	i_toptDigits = DEFAULT_TOTP_DIGITS;
    	i_toptDuration = DEFAULT_TOTP_DURATION;
    	i_hoptValidTime = DEFAULT_HOTP_VALIDTIME;
    	m_toptLabel = DEFAULT_TOTP_LABEL;
    	m_optAlgorithm = DEFAULT_OTP_ALGORITHM;
        
        b_emailHotpSendEnabled = true;
        b_smsHotpSendEnabled = false;
        m_smsEndpoint="";
        
        m_rsaPuKToken="";
    	m_rsaPrKToken="";
    	
    	m_rsaKeyExtChannel="";
    	
    	i_tokenDuration = DEFAULT_TOKEN_DURATION;
    	
    }

    /**
     * Public constructor. <p>
     * 
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param userDeletedResource the name of the deleted resource user, can be <code>null</code>
     * @param groupAdministrators the name of the administrators group
     * @param groupProjectmanagers the name of the project managers group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    public CmsTwoFactorLoginConfiguration(
    	String hotpEnabled,
    	String totpEnabled,
    	String otcpEnabled,
    	String hoptDigits,
    	String toptDigits,
    	String toptDuration,
    	String hoptValidTime,
    	String toptLabel,
    	String optAlgorithm,
    	String emailHotpSendEnabled,
    	String smsHotpSendEnabled,
    	String smsEndpoint,
    	String rsaPuKToken,
    	String rsaPrKToken,
    	String rsaKeyExtChannel,
    	String tokenDuration
    		) {

        init(
        		hotpEnabled,
            	totpEnabled,
            	otcpEnabled,
            	hoptDigits,
            	toptDigits,
            	toptDuration,
            	hoptValidTime,
            	toptLabel,
            	optAlgorithm,
                emailHotpSendEnabled,
                smsHotpSendEnabled,
                smsEndpoint,
            	rsaPuKToken,
            	rsaPrKToken,
            	rsaKeyExtChannel,
            	tokenDuration);
    }
	
	public boolean isHotpEnabled() {
		return b_hotpEnabled;
	}

	public void setHotpEnabled(boolean b_hotpEnabled) {
		this.b_hotpEnabled = b_hotpEnabled;
	}

	public boolean isTotpEnabled() {
		return b_totpEnabled;
	}

	public void setTotpEnabled(boolean b_totpEnabled) {
		this.b_totpEnabled = b_totpEnabled;
	}

	public boolean isOtcEnabled() {
		return b_otcpEnabled;
	}

	public void setOtcEnabled(boolean b_otcpEnabled) {
		this.b_otcpEnabled = b_otcpEnabled;
	}
	

	public int getHoptDigits() {
		return i_hoptDigits;
	}

	public void setHoptDigits(int i_hoptDigits) {
		this.i_hoptDigits = i_hoptDigits;
	}

	public int getToptDigits() {
		return i_toptDigits;
	}

	public void setToptDigits(int i_toptDigits) {
		this.i_toptDigits = i_toptDigits;
	}

	public int getToptDuration() {
		return i_toptDuration;
	}

	public void setToptDuration(int i_toptDuration) {
		this.i_toptDuration = i_toptDuration;
	}

	public String getToptLabel() {
		return m_toptLabel;
	}

	public void setToptLabel(String m_toptLabel) {
		this.m_toptLabel = m_toptLabel;
	}

	public String getOptAlgorithm() {
		return m_optAlgorithm;
	}

	public void setOptAlgorithm(String m_optAlgorithm) {
		this.m_optAlgorithm = m_optAlgorithm;
	}

	public boolean isSmsHotpSendEnabled() {
		return b_smsHotpSendEnabled;
	}

	public void setSmsHotpSendEnabled(boolean b_smsHotpSendEnabled) {
		this.b_smsHotpSendEnabled = b_smsHotpSendEnabled;
	}

	public boolean isEmailHotpSendEnabled() {
		return b_emailHotpSendEnabled;
	}

	public void setEmailHotpSendEnabled(boolean b_emailHotpSendEnabled) {
		this.b_emailHotpSendEnabled = b_emailHotpSendEnabled;
	}

	public int getHoptValidTime() {
		return i_hoptValidTime;
	}

	public void setHoptValidTime(int i_hoptValidTime) {
		this.i_hoptValidTime = i_hoptValidTime;
	}

	public String getSmsEndpoint() {
		return m_smsEndpoint;
	}

	public void setSmsEndpoint(String m_smsEndpoint) {
		this.m_smsEndpoint = m_smsEndpoint;
	}
	
	/**
     * Initializes this instance.<p>
     * 
     * @param totpEnabled 
     * @param hoptDigits
     * @param toptDigits
     * @param toptDuration
     * @param hoptValidTime
     * @param toptLabel
     * @param optAlgorithm
     * @param emailHotpSendEnabled
     * @param smsHotpSendEnabled
     * @param smsEndpoint
     */
    protected void init(String hotpEnabled,
        	String totpEnabled,
        	String otcpEnabled,
        	String hoptDigits,
        	String toptDigits,
        	String toptDuration,
        	String hoptValidTime,
        	String toptLabel,
        	String optAlgorithm,
        	String emailHotpSendEnabled,
        	String smsHotpSendEnabled,
        	String smsEndpoint,
        	String rsaPuKToken,
        	String rsaPrKToken,
        	String rsaKeyExtChannel,
        	String tokenDuration) {
    	
    	
    	if (CmsStringUtil.isEmptyOrWhitespaceOnly(toptLabel))
    		m_toptLabel = DEFAULT_TOTP_LABEL;
    	else 
    		m_toptLabel = toptLabel.trim();
    	
    	try{
    		i_hoptDigits = Integer.parseInt(hoptDigits);
        }
        catch (NumberFormatException ex){
        	i_hoptDigits = DEFAULT_HOTP_DIGITS;
            ex.printStackTrace();
        }
        
    	try{
    		i_toptDigits = Integer.parseInt(toptDigits);
        }
        catch (NumberFormatException ex){
        	i_toptDigits = DEFAULT_TOTP_DIGITS;
            ex.printStackTrace();
        }

    	try{
    		i_toptDuration = Integer.parseInt(toptDuration);
        }
        catch (NumberFormatException ex){
        	i_toptDuration = DEFAULT_TOTP_DURATION;
            ex.printStackTrace();
        }
    	
    	try{
    		i_hoptValidTime = Integer.parseInt(hoptValidTime);
        }
        catch (NumberFormatException ex){
        	i_hoptValidTime = DEFAULT_HOTP_VALIDTIME;
            ex.printStackTrace();
        }
    	
    	m_smsEndpoint = smsEndpoint.trim();

    	
    	b_hotpEnabled=Boolean.parseBoolean(hotpEnabled);
    	b_totpEnabled=Boolean.parseBoolean(totpEnabled);
    	b_otcpEnabled=Boolean.parseBoolean(otcpEnabled);
    	
    	b_emailHotpSendEnabled = Boolean.parseBoolean(emailHotpSendEnabled);
        b_smsHotpSendEnabled = Boolean.parseBoolean(smsHotpSendEnabled);
        
        if (optAlgorithm.trim().toUpperCase().equals(OTP_ALGORITHM_SHA1))
        	m_optAlgorithm = OTP_ALGORITHM_SHA1;
        else if (optAlgorithm.trim().toUpperCase().equals(OTP_ALGORITHM_SHA256))
        	m_optAlgorithm = OTP_ALGORITHM_SHA256;
        else if (optAlgorithm.trim().toUpperCase().equals(OTP_ALGORITHM_SHA512))
        	m_optAlgorithm = OTP_ALGORITHM_SHA512;
        else
        	m_optAlgorithm = DEFAULT_OTP_ALGORITHM;
        
        m_rsaPuKToken = rsaPuKToken.trim();
        m_rsaPrKToken = rsaPrKToken.trim();
    	m_rsaKeyExtChannel = rsaKeyExtChannel.trim();
        

    	try{
    		i_tokenDuration = Integer.parseInt(tokenDuration);
        }
        catch (NumberFormatException ex){
        	i_toptDigits = DEFAULT_TOKEN_DURATION;
            ex.printStackTrace();
        }

	}

	public String getRsaPuKToken() {
		return m_rsaPuKToken;
	}

	public void setRsaPuKToken(String rsaPuKToken) {
		this.m_rsaPuKToken = rsaPuKToken;
	}

	public String getRsaPrKToken() {
		return m_rsaPrKToken;
	}

	public void setRsaPrKToken(String rsaPrKToken) {
		this.m_rsaPrKToken = rsaPrKToken;
	}

	public String getRsaKeyExtChannel() {
		return m_rsaKeyExtChannel;
	}

	public void setRsaKeyExtChannel(String rsaKeyExtChannel) {
		this.m_rsaKeyExtChannel = rsaKeyExtChannel;
	}

	public int getTokenDuration() {
		return i_tokenDuration;
	}

	public void setTokenDuration(int tokenDuration) {
		this.i_tokenDuration = tokenDuration;
	}
	
}