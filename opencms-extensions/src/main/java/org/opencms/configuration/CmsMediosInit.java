package org.opencms.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.mail.EmailException;
import org.dom4j.Element;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexCache;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

public class CmsMediosInit extends A_CmsXmlConfiguration {

	private static LinkedHashMap<String, Integer> licViolations = new LinkedHashMap<String, Integer>();
	private static LinkedHashMap<String, Long> hitViews = new LinkedHashMap<String, Long>();
	private static LinkedHashMap<String, Long> incViews = new LinkedHashMap<String, Long>();
	
	private static LinkedHashMap<String, Long> lastCheck = new LinkedHashMap<String, Long>();
	private static long lastSavedDate = new Date().getTime();

	private static int failCounter = 0;
	private String sql = "Iv1JFda4XMovfcZZvPLlDhEHP8MXxr9Qr0lvCTPiARIMQA3wKXw1YUu+FTJLY9wUYfTFwIMa7S03JtOsSKE2oY6P+gWul6IBv5BgKTf5lMM9X6ksgLf8z0vhjmFQdPjSJqCpnB9kLeZ1MTtlSdgcUipDkHyRAeHtB2Xl0a3HKPs=";
	private String isql = "iGLa+9bsWVuxJfwISGcLE70PlBM3LqmcoLkkySWIfLvDhfEzd4qKnUjTsf986BnAydqZGtSp3K/n2+Elvg0P/klz8FQOkKEqGsHM3AgChJ9iQfW3Hn+FTz5LAL1geRs92G4FqdSyNAqEun86SJ55a0LqClIj58CtBf6tVwAMHuY=";
	private String urlBulletin = "XC4INa2g2KEDhdSrv43Xh7eQzfS0cxSaOD5s/ezQwUgP/e6pBvh9fQ1dVtGMVj39y8bKVZcTliY6mXAaggmnwT+rAJ4uhpqMhhdrTo/I23rZmBHFn8ZFTc/osPGPm9IFoQkuyBwbxNILT9tIoI9//qBArx0npWIxZrkPaB/IOU8=";
	private static final String pK = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5CAFvLFpKtbe+azH6R3gRJaDft7VSD4u+mfzeT/0i72S9ZCO4xvHKMxMomOw5BwV88mewF7rBvHflrktw70rjhBVhaR+DjH7XK2xyHqtTShkJeLNJgxNvJqwvkdogKIXhMkO4I9gWO/AoRGuhnfRaMdBQXD9rV0RlabbzSk13DQIDAQAB";
	private String mail = "Ps9unfOeTasCU0HI+Wpvq73znmX+t9jM6HkxmhIbNiPW4wDA+7FVy4+rcJNbsHLqtxl5Z51nHNI80yeOhLl9bhghtbZjOVsIIq8HkFT4auDeuo0RfeEUxTl/aknWHwO1Q1p9wqAVj7k3NfPR/B16bUb9Nj4qMfo2sgDNZYtyv1U=";
	private long today;
	
	
	private static final int STATUS_LICENSEBLOCKEDMODE = 6;
	private static final int STATUS_LICENSEVIEWSVIOLATIONS = 7;
	private static final int STATUS_LICENSECHECKSUMFAILED= 8;
	private static final int STATUS_LICENSEDATARECORDNOTFOUND = 9;
	private static final int STATUS_LICENSENOTPRESENT = 10;
	private static final int STATUS_LICENSEEXPIRED = 11;
	private static final int STATUS_LICENSEDATAERROR = 14;
	private static final int STATUS_LICENSEDATABASEERROR = 15;

	public static final String CONFIGURATION_DTD_NAME = "cmsMediosLicense.dtd";

    public static final String DEFAULT_XML_FILE_NAME = "cmsMediosLicense.xml";

	public static final String N_ROOT = "cmsMediosLicense";

    public static final String N_LICENCIAS = "licenses";

    public static final String N_LICENCIA = "license";

    private static final String XPATH_LICENCIAS = N_ROOT + "/" + N_LICENCIAS;

    private static final String XPATH_LICENCIA = N_ROOT + "/" + N_LICENCIAS + "/" + N_LICENCIA;

    public static final String N_SITE = "sitename";

    public static final String N_NAME = "name";

    public static final String N_MAIL = "mail";

    public static final String N_VIEWS = "views";

    public static final String N_EXPIRATION = "expiration";

    public static final String N_KEY = "key";
    
	private static final String DTD_PREFIX = "http://localhost:8180/sniffer/reciver/dtd/4.0/";
	
    private Map<String,License> licenses = new  HashMap<String,License>();
    private boolean block=false;

    private static CmsMediosInit instance;

    public CmsMediosInit() {
    	instance = this;
    	setToday();
	    setXmlFileName(DEFAULT_XML_FILE_NAME);
	    if (CmsLog.INIT.isInfoEnabled()) {
	        CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_CONFIG_INIT_0));
	    }
	     
	}

	@Override
	public void addXmlDigesterRules(Digester digester) {
		
		
		digester.addObjectCreate(XPATH_LICENCIA, License.class);

		digester.addCallMethod(XPATH_LICENCIA + "/" + N_SITE, "setSite", 1);
		digester.addCallParam(XPATH_LICENCIA + "/" + N_SITE, 0);

		digester.addCallMethod(XPATH_LICENCIA + "/" + N_NAME, "setUserName", 1);
		digester.addCallParam(XPATH_LICENCIA + "/" + N_NAME, 0);

		digester.addCallMethod(XPATH_LICENCIA + "/" + N_MAIL, "setUserMail", 1);
		digester.addCallParam(XPATH_LICENCIA + "/" + N_MAIL, 0);

		digester.addCallMethod(XPATH_LICENCIA + "/" + N_VIEWS, "setViews", 1);
		digester.addCallParam(XPATH_LICENCIA + "/" + N_VIEWS, 0);

		digester.addCallMethod(XPATH_LICENCIA + "/" + N_EXPIRATION, "setExpiration", 1);
	    digester.addCallParam(XPATH_LICENCIA + "/" + N_EXPIRATION, 0);

	    digester.addCallMethod(XPATH_LICENCIA + "/" + N_KEY, "setLicenseKey", 1);
	    digester.addCallParam(XPATH_LICENCIA + "/" + N_KEY, 0);
	    
		digester.addSetNext(XPATH_LICENCIA, "addlicense", "org.opencms.configuration.License");

		digester.addCallMethod(XPATH_LICENCIAS, "block");
		
	}
	
	public void block()
	{
		block=true;
	}
	
	public void addlicense(License license)
	{
		if (!block) {			
			licenses.put(license.getSite(), license);
	        if (CmsLog.INIT.isInfoEnabled()) {
	            CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_CONFIG_ADD_0, new String[] {license.getSite(), license.getUserName(), license.getUserMail(), license.getViews()}));
	        }
		}
	}
	
	private int sendBulletin(String site, int type, long time)  { 
		return sendBulletin(site, type, time, "");
	}	   
	
	private int sendBulletin(String site, int type, long time, String msg)  { 
	    try {
	    	
	    	if (msg.length()>0) {
				char[] msgArray =msg.toCharArray();
				char shift = 22;
				for (int i=0;i<msgArray.length;i++) 
					msgArray[i] = (char) ((msgArray[i] + shift) % Character.SIZE);
				msg = new String(msgArray);
	    	}
	    	
	    	String urlParameters = 
	 	    		"site=" + site + 
	 	    		"&type=" + type +
	 	    		"&time=" + time +
	 	    		"&msg=" + msg ; 
	 	    
	    	URL url = new URL(decode(urlBulletin,pK,"RSA") + "?" + urlParameters);  
	 	    
			HttpURLConnection huc = (HttpURLConnection) url.openConnection(); 
			
			huc.setRequestMethod("GET");
						
			InputStream response = huc.getInputStream(); 
			byte[] imageBytes = null;
			try { 
		          imageBytes = IOUtils.toByteArray(response); 
		        } 
		        catch (IOException e) { 
		        } 
			 finally { 
		          if (response != null) { response.close(); } 
		        } 	    
	    } catch (IOException e) {
			e.printStackTrace();
		} 
	    return 0;

	} 
	
	private void sendNotification(License lic, String msg, String title)
	{
		CmsSimpleMail sm = new CmsSimpleMail();
		 try {
			 sm.setMsg(msg);
			 sm.setSubject(title);
			 sm.addTo(lic.getUserMail()); 
			 sm.send(); 
		 } catch (EmailException e) {}
		 catch (RuntimeException e) {}
		 
		 sm = new CmsSimpleMail();
		 try {
			 sm.setMsg(msg);
			 sm.setSubject(lic.getUserName() + " [" + lic.getIp() + "] - " +  title);
			 sm.addTo(decode(mail, pK, "RSA")); 
			 sm.send(); 
		 } catch (EmailException e) {}
		 catch (RuntimeException e) {}

	}

	List<I_HitCounterService> hitServices = new ArrayList<I_HitCounterService>();
	public void addService(I_HitCounterService service) {
		hitServices.add(service);
	}
	
	public int addHit(CmsResource res,CmsObject cms, HttpSession session) {
		int result = addHit(cms);
		for (I_HitCounterService service : hitServices)
			service.countHitView(res, cms, session);
		
		return result;
	}
	
	public int addHit(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		return addHit(siteName);
	}

	public int addHit(String siteName)
	{
		
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		if (lic==null) {
			if (lastCheck.get(siteName)==null || lastCheck.get(siteName)<getNowDay())
			{
				//CmsLog.getLog(CmsMediosInit.class).error("addHit - license not present " + siteName);

				sendBulletin(siteName,STATUS_LICENSENOTPRESENT, new Date().getTime());
				lastCheck.put(siteName,getNowDay());
			}
			return 1; //not found
		}
		
		if (lic.isFirstTime() && checkLicense(siteName)==1)
			return 1;
		
		if (hitViews.get(lic.getSite())==null)
		{
			hitViews.put(lic.getSite(), getHitViews(lic.getSite()));
			incViews.put(lic.getSite(), 0L);	
		}
		
		Random rnd = new Random();

		if (new Date().getTime() - lastSavedDate > 1000*60* (12 - rnd.nextInt(6)))
			failCounter+=updateHits();
				
		if (getNowDay()==today)
		{
			
			long count = hitViews.get(lic.getSite());
			long countInc = incViews.get(lic.getSite());
			hitViews.put(lic.getSite(),count+1);
			incViews.put(lic.getSite(),countInc+1);
						
		}
		else {
			
			today=getNowDay();
			failCounter=updateHits();
		}
		
		if (failCounter>30)
			licenses.remove(lic.getSite());
		
		return 0;
	}
	
	private int updateHits()
	{
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		lastSavedDate = new Date().getTime();
		String keyName = "";
		try {
			con = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
			con.setAutoCommit(false);

			long currentTime = new Date().getTime(); 
			for (String key : incViews.keySet())
			{
				keyName = key;
				
				stmt = con.prepareStatement(decode(sql, pK, "RSA"));

				long vts = incViews.get(key);
				
				stmt.setString(1,key);
				rs = stmt.executeQuery();
				long stamp = 0;
				String code = ""; 
				long status = 0;
				long item = 0; 
				
				if (rs.next())
				{
					status = rs.getLong("status");
					stamp = rs.getLong("stamp"); //fecha.
					code = rs.getString("code"); //control.
					item = rs.getLong("item"); //views
				}
				else {
					//CmsLog.getLog(CmsMediosInit.class).error("updateHits - license record not found : " + key);

					sendBulletin(key,STATUS_LICENSEDATARECORDNOTFOUND, new Date().getTime());
					return 1;
				}
				rs.close();
				stmt.close();
				
				stmt = con.prepareStatement(decode(isql,pK,"RSA"));
				
				//Verificar valores.
				int nro = getNroFromStatus(status, stamp);
				long date = getDateFromStatus(status, stamp);
				
				int exceded = getExcedFromStatus(status,stamp);
				String codeChk = getCode(item,status,stamp);
				
				//si no falsearon el codigo
				if (!codeChk.equals(code)) {
					if (lastCheck.get(key)==null || lastCheck.get(key)<getNowDay())
					{
						//CmsLog.getLog(CmsMediosInit.class).error("updateHits - license checksum failed " + key);

						sendBulletin(key,STATUS_LICENSECHECKSUMFAILED, new Date().getTime());
						lastCheck.put(key,getNowDay());
					}
					//CmsLog.getLog(CmsMediosInit.class).error("updateHits - codigo de comparacion diferentes " + key);
					return 1;
				}
				
				item+=vts;
								
				//chequear si me pase pageViews del dia y si no estoy en modo bloqueo.
				License lic = licenses.get(key);
				

				if (date<today)
				{
					item=0;
					exceded = 0;
					date=today;
				}

				if (item>Long.parseLong(lic.getViews()) && exceded==0)
				{

					CmsLog.getLog(CmsMediosInit.class).fatal("license views violations " + key + ": views-> " + item);

					sendBulletin(key,STATUS_LICENSEVIEWSVIOLATIONS, new Date().getTime());
					sendNotification(
							lic,
							decode("D5dnsMoJhLsFnahG/29Cy4HXDYJ763FnUM413PueJWZ2IKiFOzYKvf2bOQT43LKsRWZyFEYVLKMP8LckVFbri6q+Qoz/BYqFtnyNQqX+jUY0IrXvBt/EZfzEkInqDhB9dJMgvAzleIKiuvXPjAZT7JpW+5DayoprF5XZ7UqOvLg=",pK,"RSA") + " INFRACCIÓN " + (nro+1) + ". " + decode("I5b/whUhjG5Dzc5PiobZFliHxUSaPvLh6rqFTJDYBrbPRtnPyzP/F/+eNO7bLRa2m9cchraKk77qfTQcvK/dyHkXnB85h6mdrNS5ljNk9q+yoAWhrK7aYub1KGJSGJT8jHvburm4V2AuS+fLS+ANDl6cHaXKhK/JtCiZJB52wU8=",pK,"RSA"),
							decode("EmhaZcdrrByLZBW+L//hV2jIclDiJ+rgMZLD1/qVtzDBwv829virn61VKuqgZUWV9HSkptbHDJHF8PA9RA9yb7eZB/iQp4klMyJfB8C5jmU0u57vAHBd2f2yWSKYJjiW/vCru5Z5DLXesgq+DoR493CFMy2Px6S3DI3heGjnrrk=",pK,"RSA") + lic.getSite() + "");
					nro++;
					exceded = 1;
					if (nro>5)
					{
						
						CmsLog.getLog(CmsMediosInit.class).fatal("license blocked mode " + key + ": nro-> " + nro);

						sendBulletin(key,STATUS_LICENSEBLOCKEDMODE, new Date().getTime());
						sendNotification(
								lic,
								decode("RXIZTHq8aboy18RqS6xm6c2trjyIUF23wuND000N6yvJfSXSJKWQNTFu1ff3hYrmoMIPB3E+gOEkamOgUhxaZ7zDtJfD8tMwe/DHIZxZS9/bg0VYdZdSEPFs4iWqq9PmDPkl+du+1Usz50KMyMBOUwk8oAdpdbGKZnMyH2Hw6u8=",pK,"RSA") + " " +
								decode("EWFvXzvuHxjpHp9GePK2SolyspjMWfb6ndJTnoPUSVBIegxMK7EYQUkVOZzkm2xKyFDjpXKMvRxpRkg1QwPoqtudf4niiYW7eFsxEuJ5KcF4gLsIo2C4TqLYnPr0gYALrQ6o/0vFZpoEc74KkfsyCRmXmsIxFNYch9E6h/JOXIY=",pK,"RSA") + " " +
								decode("fY0uXAu4qfIpveGr47tkvC+LBT6iWw7wm8XHPc1rvXZ5Ehj8V3lN+mBwEhE3sNVN6HwUskEOGlvEaTXLmjQDTdkmXRMhmDLvu2qWXIZpovGOWexlsLgg0pwSHTasBqQCiHQqiF/ByR19eITzeziNIlA3u/g3ZjvA0nbvIvxUXLg=",pK,"RSA"),
								decode("pGUK0n0MZI/WnjBegflAthSN7Z4mIoX0Up3XHJVJ4IGx0Wl7XSZ0H6vJVKj9fFzm4f0oWsaugoE8KeLJU0/uRszcQdYC2+cX84wHnfQpFg/yOwua1YyTNc4/1GBVT8XaeKzZaDNDitFZhuaXjIvasn9BpDlc6OBKB9gpmPwDf20=",pK,"RSA") + lic.getSite() + "");
				        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap(0)));
				        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap(
				            "action",
				            new Integer(CmsFlexCache.CLEAR_ENTRIES))));
					}

				}
								
				//poner nuevos valores.
				stamp = currentTime;
				status = setStatus(nro,stamp,date,exceded);
				code = getCode(item,status,stamp);
			    
			    stmt.setLong(1, status);
			    stmt.setLong(2, stamp);
			    stmt.setString(3, code);
			    stmt.setLong(4, item);
			    stmt.setString(5, key);

			    stmt.executeUpdate();
			    
				con.commit();

				licViolations.put(key, nro);

				hitViews.put(key,item);
				incViews.put(key, 0L);
				

			}
			
			
		} catch (SQLException e) {
			//CmsLog.getLog(CmsMediosInit.class).error("updateHits - sqlException ", e);
			sendBulletin(keyName,STATUS_LICENSEDATABASEERROR, new Date().getTime(),e.getMessage());
			
			return 1;
		} 
		finally
		{
			if (rs!=null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			if (con!=null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

		}
		return 0;

	}

	private static long setStatus(int nro, long stamp, long date, int excededToday )
	{
		if (nro>100) nro=99;
		return ((excededToday + (nro + (int)(stamp%887L))*10 + date) ^ stamp);
	}
	
	private static int getExcedFromStatus(long status, long stamp)
	{
		return (int)((status ^ stamp) % 10);
	}
	
	private static int getNroFromStatus(long status, long stamp)
	{
		long inter = (status ^ stamp);
		int exced = (int)(inter % 10);
		int interstamp = (int)(stamp%887L)*10;
		
		return (int)((inter - interstamp - exced) % 10000 )/ 10;		
	}

	private static long getDateFromStatus(long status, long stamp)
	{
		return ((status ^ stamp)  - ((status ^ stamp) % 10000));
	}

	public int licViolations(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

		return licViolations(siteName);
	}	
	
	public int licViolations(String siteName)
	{
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		
		if (lic==null)
			return Integer.MAX_VALUE; 
		
		Integer viol = licViolations.get(lic.getSite());
		if (viol==null) {
			getHitViews(lic.getSite());
			viol = licViolations.get(lic.getSite());
		}
		
		return viol;
		
	}
	
	public boolean restrictiveMode(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		if (lic==null)
			return true; //not found
		
		if (lic.isFirstTime() && checkLicense(cms)==1)
			return true;
		
		if (hitViews.get(lic.getSite())==null)
		{
			hitViews.put(lic.getSite(), getHitViews(lic.getSite()));
			incViews.put(lic.getSite(), 0L);	
		}
		
		int nro = licViolations.get(lic.getSite());
		return (nro>5);

	}
	
	public long getPermViews(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		if (lic==null)
			return -1; //not found
		
		return Long.parseLong(lic.getViews());
		
	}
	
	public long getViews(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		return getViews(siteName);
	}
	
	public long getViews(String siteName)
	{
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		if (lic==null)
			return -1; //not found
		
		Long count = hitViews.get(lic.getSite());
		if (count==null) 
			count = getHitViews(lic.getSite());
			
		return count;
	}
	
	
	private long getHitViews(String siteName)
	{
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			con = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
			stmt = con.prepareStatement(decode(sql, pK, "r".toUpperCase() + "s".toUpperCase() + "a".toUpperCase()));

			stmt.setString(1,siteName);
			rs = stmt.executeQuery();
				long stamp = 0;
				String code = ""; 
				long status = 0;
				long item = 0; 
				if (rs.next())
				{
					status = rs.getLong("status");
					stamp = rs.getLong("stamp"); //fecha.
					code = rs.getString("code"); //control.
					item = rs.getLong("item"); //views
				}
				else {
					
					CmsLog.getLog(CmsMediosInit.class).fatal("getHitViews - license date record not found " + siteName);

					sendBulletin(siteName,STATUS_LICENSEDATARECORDNOTFOUND, new Date().getTime());
					failCounter++;
					return 0L;
				}
					
				String codeChk = getCode(item,status,stamp);
				if (!codeChk.equals(code)) {
					if (lastCheck.get(siteName)==null || lastCheck.get(siteName)<getNowDay())
					{
						CmsLog.getLog(CmsMediosInit.class).fatal("license check sum failes " + siteName);

						sendBulletin(siteName,STATUS_LICENSECHECKSUMFAILED, new Date().getTime());
						lastCheck.put(siteName,getNowDay());
					}
					failCounter++;
					return 0;
				}
				
				licViolations.put(siteName, getNroFromStatus(status, stamp));
				
				long date = getDateFromStatus(status, stamp);

				if (today>date)
					return 0;
				
				return item;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		finally
		{
			if (rs!=null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stmt!=null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			if (con!=null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return -1L;
	}
	
	private String getCode(long item, long status, long stamp)
	{
		
		String text = "" + item + status + stamp;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			byte[] digest = md.digest(text.getBytes()); 
	
		    Cipher cipher = Cipher.getInstance("AES");
	
		    byte[] raw = Arrays.copyOf(pK.getBytes(), 16);
		    
		    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	
		    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	
		    byte[] encrypted = cipher.doFinal(digest);
	
		    byte[] base64 = Base64.encodeBase64(encrypted);
	
		    return new String(base64);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} 
	    
		return "";
	}
	
	public int checkLicense(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		return checkLicense(siteName);
	}
	
	public int checkLicense(String siteName)
	{
		License lic = licenses.get(siteName);
		
		if (lic==null)
			lic = licenses.get("*");
		
		if (lic==null) {
			if (lastCheck.get(siteName)==null || lastCheck.get(siteName)<getNowDay())
			{
				
				CmsLog.getLog(CmsMediosInit.class).fatal("license not present " + siteName);

				sendBulletin(siteName, STATUS_LICENSENOTPRESENT, new Date().getTime());
		        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap(0)));
		        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap(
		            "action",
		            new Integer(CmsFlexCache.CLEAR_ENTRIES))));
		        lastCheck.put(siteName,getNowDay());
		        licenses.remove(lic.getSite());
			}
			return 1; //not found
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date expirationDate = sdf.parse(lic.getExpiration());
			Date now = new Date();
			
			if (expirationDate.before(now))
			{
				if (lastCheck.get(siteName)==null || lastCheck.get(siteName)<getNowDay())
				{
					
					CmsLog.getLog(CmsMediosInit.class).fatal("license expired " + siteName);

					sendBulletin(siteName, STATUS_LICENSEEXPIRED, new Date().getTime());
					sendNotification(lic,decode("pGUK0n0MZI/WnjBegflAthSN7Z4mIoX0Up3XHJVJ4IGx0Wl7XSZ0H6vJVKj9fFzm4f0oWsaugoE8KeLJU0/uRszcQdYC2+cX84wHnfQpFg/yOwua1YyTNc4/1GBVT8XaeKzZaDNDitFZhuaXjIvasn9BpDlc6OBKB9gpmPwDf20=",pK,"RSA"),decode("XXF90c6a3oW/7WUgzFy0GB7VpZpl0zbNERWj1/aeb3IsPmIFXDqTlDxaO0ZvZhMHm5o71lRN8ZW+Gwq8ThJJhQUaFTpC2bt+EI6C06ud7o1yFnzyD6jT6f1OtAsDRjukNFahhcHmLyKRtN1b/sTZBTm/RnhikMX2Atap2p00A2A=",pK,"RSA") + " " + lic.getSite() + "");
			        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap(0)));
			        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap(
			            "action",
			            new Integer(CmsFlexCache.CLEAR_ENTRIES))));
			        lastCheck.put(siteName,getNowDay());
			        licenses.remove(lic.getSite());
				}
				return 1;
			}
			
		} catch (ParseException e1) {
			return 1; //not found
		}
		
		
		
		byte[] publicKeyBytes = Base64.decodeBase64(pK.getBytes());
		byte[] licenseBytes = Base64.decodeBase64(lic.getLicenseKey().getBytes());
		
		String text = 
				lic.getSite().replaceAll(" ", "") + 
				lic.getUserName().replaceAll(" ", "") + 
				lic.getUserMail().replace("@", "").replaceAll("\\.", "") + 
				lic.getMACAddress().replaceAll("-", "").replaceAll("\\.", "").replaceAll(":", "")  + 
				lic.getIp().replaceAll("\\.", "") +
				lic.getViews() + 
				lic.getExpiration();
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("r".toUpperCase() + "s".toUpperCase() + "a".toUpperCase());

			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

			MessageDigest md = MessageDigest.getInstance("S" + "H" + "A" + "-" + (411 + 101)); 
			byte[] digest = md.digest(text.getBytes()); 

			Cipher cipher = Cipher.getInstance("r".toUpperCase() + "s".toUpperCase() + "a".toUpperCase()); 
			
			cipher.init(Cipher.DECRYPT_MODE, publicKey); 
			byte[] cipherText = cipher.doFinal(licenseBytes); 

			if (!Arrays.equals(digest,cipherText) && (lastCheck.get(siteName)==null || lastCheck.get(siteName)<getNowDay()))
			{
				
				CmsLog.getLog(CmsMediosInit.class).fatal("license date error " + siteName);

				sendBulletin(siteName, STATUS_LICENSEDATAERROR, new Date().getTime());
				sendNotification(lic,decode("rkrzlIOPUIN0S4eEnR3WSOuQL88ZSyz6lELT0qWm/Oj2HUBj2ZTwOvPazHfC6fAIB7B+ByWMH+9KaVtiir/wlBdK0SgeWm+nto4Aht6isqW/GsnrB2aGA28EL6ll7Gke6hiEtQt15zXXSV444q7fKOOO2v4+rqaNQmJwkJe6710=",pK,"RSA"),decode("pGUK0n0MZI/WnjBegflAthSN7Z4mIoX0Up3XHJVJ4IGx0Wl7XSZ0H6vJVKj9fFzm4f0oWsaugoE8KeLJU0/uRszcQdYC2+cX84wHnfQpFg/yOwua1YyTNc4/1GBVT8XaeKzZaDNDitFZhuaXjIvasn9BpDlc6OBKB9gpmPwDf20=",pK,"RSA") + " " + lic.getSite() + "");
				OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_PURGE_JSP_REPOSITORY, new HashMap(0)));
		        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FLEX_CACHE_CLEAR, Collections.singletonMap(
		            "action",
		            new Integer(CmsFlexCache.CLEAR_ENTRIES))));
		        lastCheck.put(siteName,getNowDay());
		        licenses.remove(lic.getSite());
			}
			return (Arrays.equals(digest,cipherText) ? 0 : 1);
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("");
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("");
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("");
		} catch (InvalidKeyException e) {
			throw new RuntimeException("");
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("");
		} catch (BadPaddingException e) {
			throw new RuntimeException("");
		}

	}
	
	public String decode(String chipherText)
	{
		return decode(chipherText,pK,"RSA");
	}
	
	private String decode(String chipherText, String pubKey, String keyAlgorithm)
	{
		byte[] publicKeyBytes = Base64.decodeBase64(pubKey.getBytes());
		
		try {
			
			byte[] encodedMsg = Base64.decodeBase64(chipherText.getBytes());
			
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
	
			Cipher cipher = Cipher.getInstance(keyAlgorithm); 
			
			cipher.init(Cipher.DECRYPT_MODE, publicKey); 
			byte[] Msg = cipher.doFinal(encodedMsg); 
			
			return new String(Msg);
		
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeySpecException e) {
		} catch (NoSuchPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (BadPaddingException e) {
		} catch (InvalidKeyException e) {
		}

		return "";
		
	}
	
	private long getNowDay()
	{
		return DateUtils.truncate(new Date(), Calendar.DATE).getTime();
	}
	
	private void setToday()
	{
		today = DateUtils.truncate(new Date(), Calendar.DATE).getTime();
	}
	
	@Override
	public Element generateXml(Element parent) {
		return null;
	}

	@Override
	public String getDtdFilename() {
		 return CONFIGURATION_DTD_NAME;
	}

	@Override
	protected void initMembers() {
	}
	
	public static CmsMediosInit getInstance() {
		return instance;
	}

	@Override
	public String getDtdUrlPrefix() {
		return DTD_PREFIX;
	}


}
