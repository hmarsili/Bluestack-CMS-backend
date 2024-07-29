package com.tfsla.opencms.webusers.externalLoginProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserExternalProvider;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsUnknownUserException;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.externalLoginProvider.Exception.InvalidSecretKeyException;
import com.tfsla.opencms.webusers.externalLoginProvider.Exception.Messages;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;

public class FacebookLoginProvider {

	 /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(FacebookLoginProvider.class);
    
    /** Flag to indicate if a login was successful. */
    private CmsException m_loginException;
    
	private static final String HMAC_SHA256_MAC_NAME = "HMACSHA256"; 
	
	private CmsFlexController m_controller;
    private HttpSession m_session;
    
    private String siteName;
    private TipoEdicion currentPublication;
    private String publication;
    private String moduleConfigName;
    private CPMConfig config;
    
    private CmsMultiMessages m_messages;
    private CmsWorkplaceSettings m_settings;
    private Locale m_elementLocale;
    
    private HttpServletRequest req;
    
    private JSONObject jsonRequest = null;
    private Date expirationDate;
	
	public FacebookLoginProvider(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
		m_controller = CmsFlexController.getController(req);
        m_session = req.getSession();
        this.req = req;
        
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        
        if (m_settings==null)  {
        	m_settings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), m_settings, true);
        	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
        	
        }
        retriveMessages();

    	siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
    	String proyecto  = siteName.replaceAll("/sites/","");

    	currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");

    	if (currentPublication==null) {
        	TipoEdicionService tService = new TipoEdicionService();

    		currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
    		m_session.setAttribute("currentPublication",currentPublication);
    	}
    	
    	publication = "" + currentPublication.getId();
    	moduleConfigName = "externalLoginProvider";
 		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

	}
	
	public void setPublication(String siteName, int publication) {
    	TipoEdicionService tService = new TipoEdicionService();
    	
    	this.publication = "" + publication;
    	currentPublication =tService.obtenerTipoEdicion(publication);
    	this.siteName = siteName;
    	getCmsObject().getRequestContext().setSiteRoot(this.siteName);

	}
	
	public void setPublicationFromRequest() {
		
		getJsonRequest();
		
		if (jsonRequest==null || jsonRequest.get("siteName")==null)
			return;
		
		String _site = jsonRequest.getString("siteName");
		String _publication = jsonRequest.getString("publication");
		
		if (_publication!=null && _site!=null)  {
			int pub = Integer.parseInt(publication);
			setPublication(_site,pub);
		}	
	}
	
	public void retriveMessages() {
	    if (getLocale()==null)
	    	setLocale(getCmsObject().getRequestContext().getLocale());

    	// initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
	    // generate a new multi messages object and add the messages from the workplace
	    
	    m_messages = new CmsMultiMessages(getLocale());
	    m_messages.addMessages(messages);
    }
	
	public Locale getLocale()
	{
		return m_elementLocale;
	}
	
	public void setLocale(Locale locale)
	{
		m_elementLocale = locale;
	}

	
	public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }
	
	protected String getSecretKey() {
		return config.getParam(siteName, publication, moduleConfigName, "facebook-secretkey");
	}
	
	public void setJsonRequest(JSONObject request) throws IOException {
		jsonRequest = request;
	}
	   
	public JSONObject getJsonRequest() {
		if (jsonRequest!=null)
			return jsonRequest;
		   
		String stringRequest = "";
		//Obtengo del post el json con los parametros del login de facebook.
		try {
			stringRequest = getRequestAsString(req);
		} catch (IOException e) {
			jsonRequest = new JSONObject();
		}
		
		if (!stringRequest.equals(""))
			jsonRequest = JSONObject.fromObject(stringRequest);
		else 
			jsonRequest = new JSONObject();
		
		return jsonRequest;
	}
	
	private String getRequestAsString(HttpServletRequest request) throws IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
			jb.append(line);
		return jb.toString();
	}
	
	public boolean hasFacebookLoginInformation() {
		
		getJsonRequest();
			
		
		try {
			String provider = jsonRequest.getString("provider");
			if (provider ==null) 
					return false;
			
			if (provider.trim().toLowerCase().equals("facebook"))
				return true;
		}
		catch (JSONException e) {
			return false;
		}
		return false;
	}
	
	public CmsUser login() {
		
		getJsonRequest();
		
		try {
				
			String signedRequest = jsonRequest.getString("signedRequest");
			String email = jsonRequest.getString("email");
			String userId = jsonRequest.getString("id");
			String firstname = jsonRequest.getString("firstname");
			String lastname = jsonRequest.getString("lastname");
			String token = jsonRequest.getString("token");
			
			return login(signedRequest,email,userId, firstname, lastname,token);
		}
		catch (JSONException e) {
			m_loginException = new InvalidSecretKeyException(Messages.get().container(
                    Messages.ERR_INVALID_DATA_1),e);
			
			LOG.error("Error: ",e);
			return null;
		}
		
	}
	
	public CmsException getLoginException() {

        return m_loginException;
    }
	
    public boolean isLoginSuccess() {

        return (m_loginException == null);
    }
	
	public CmsUser login(String signedRequest, String email, String userId, String firstname, String lastname, String token)  {
		
		//HttpSession session = null;
		m_loginException = null;
		 
		try {
			
			if (!verifySignedRequest(signedRequest))
				throw new InvalidSecretKeyException(Messages.get().container(
	                    Messages.ERR_INVALID_SIGNED_REQUEST_1,userId));
			
			
			CmsUser user = null;
			
			// me logueo
			try {
				getCmsObject().loginUserByExternalProvider("facebook", userId, getRequestContext().getRemoteAddress());
				user = getCmsObject().getRequestContext().currentUser();
			} catch (CmsUnknownUserException e) {
				//No existe. ergo, lo genero (para la primera vez)
				
				
				RegistrationModule regModule = RegistrationModule.getInstance(getCmsObject());
				
				//Verifico que el email no este tomado por otro usuario
				try {
					user = getCmsObject().readUser(regModule.getOu() + email);
				
					//Si llego aca es que existe.... 
					CmsUserExternalProvider userProv = new CmsUserExternalProvider(
							user.getId(), user.getName(), "facebook", userId, token);
					
					getCmsObject().createUserExternalProvider(userProv);
				
					return user;
					
					//no puede seguir.
					//m_loginException =  new InvalidSecretKeyException(Messages.get().container(
		            //        Messages.ERR_INVALID_EMAIL_1,userId));
					//return null;
				
				}
				catch (CmsDbEntryNotFoundException ex) {
					//Si no lo encontre. sigo adelante.
					String password = RegistrationModule.getRandomPassword();
					user = regModule.addWebUser(getCmsObject(), email, password, email, firstname, lastname);
					
					
					CmsUserExternalProvider userProv = new CmsUserExternalProvider(
							user.getId(), user.getName(), "facebook", userId, token);
					
					getCmsObject().createUserExternalProvider(userProv);
					
					
					return user;
				}
				
			} catch (CmsAuthentificationException e) {
				// TODO Auto-generated catch block
				//No consigo el usuario. es posible que este deshabilitado u algun error
				
				e.printStackTrace();
				 m_loginException = e;
				 return null;
			}
			
			//fijarme si hay que actualizar algo.
			CmsUserExternalProvider userProv = new CmsUserExternalProvider(
					user.getId(), user.getName(), "facebook", userId, token);
			getCmsObject().updateUserExternalProvider(userProv);
			
			JSONObject payload = getPayload(signedRequest);
			
			int _expires = payload.getInt("issued_at") + 60*24*60*60;
			expirationDate = new Date(_expires * 1000L);
			
			return user;
			
		} catch (CmsException e) {
            // the login has failed
			e.printStackTrace();
            m_loginException = e;
        } catch (InvalidKeyException e) {
        	m_loginException = new InvalidSecretKeyException(Messages.get().container(
                    Messages.ERR_INVALID_SECRETKEY_1),e);

		} catch (NoSuchAlgorithmException e) {
        	m_loginException = new InvalidSecretKeyException(Messages.get().container(
                    Messages.ERR_INVALID_ENCRYPT_ALGORITHM_1),e);
		}
		
		return null;
	}
	
	protected JSONObject getPayload(String signedRequest) {
		
		String[] split = signedRequest.split("\\.");
		String payload = split[1];
		
		payload = payload.replace("-", "+").replace("_", "/").trim();
		byte[] payloadBytes = new Base64(true).decodeBase64(payload.getBytes());
		
		String jsonString = new String(payloadBytes);
		LOG.error(jsonString);
		JSONObject payloadJson = JSONObject.fromObject(jsonString );
		
		return payloadJson;
	}
	
	protected boolean verifySignedRequest(String signedRequest) throws NoSuchAlgorithmException, InvalidKeyException {
		String[] split = signedRequest.split("\\.");
		String encodedSignature = split[0];
		String payload = split[1];
		
		payload = payload.replace("-", "+").replace("_", "/").trim();
		
		byte[] signature =  new Base64(true).decodeBase64(encodedSignature.getBytes());
		
		//Chequeo de la firma sea valida usando mi clave secreta.
		SecretKeySpec secretKeySpec = new SecretKeySpec(getSecretKey().getBytes(), HMAC_SHA256_MAC_NAME);
		Mac mac = Mac.getInstance(HMAC_SHA256_MAC_NAME);
		mac.init(secretKeySpec);
		byte[] expectedSignature = mac.doFinal(payload.getBytes());
		
		return Arrays.equals(expectedSignature, signature);

	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}
	
	public boolean isLoggedIn() {

        return !getCmsObject().getRequestContext().currentUser().isGuestUser();
    }
	
	 public CmsRequestContext getRequestContext() {

        return getCmsObject().getRequestContext();
    }
}
