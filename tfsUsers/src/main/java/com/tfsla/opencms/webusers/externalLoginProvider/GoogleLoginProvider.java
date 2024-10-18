package com.tfsla.opencms.webusers.externalLoginProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.GeneralSecurityException;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.externalLoginProvider.Exception.InvalidSecretKeyException;
import com.tfsla.opencms.webusers.externalLoginProvider.Exception.Messages;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class GoogleLoginProvider {


	 /** The log object for this class. */
   private static final Log LOG = CmsLog.getLog(GoogleLoginProvider.class);
   
   /** Flag to indicate if a login was successful. */
   private CmsException m_loginException;
	
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

   public GoogleLoginProvider(PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
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
   
   
   public boolean hasGoogleLoginInformation() {
		getJsonRequest();
		
		try {
			String provider = jsonRequest.getString("provider");
			if (provider ==null) 
					return false;
			
			if (provider.trim().toLowerCase().equals("google"))
				return true;
		}
		catch (JSONException e) {
			return false;
		}
		
		return false;
	}
   
   
   public CmsUser login()  {
		
		getJsonRequest();
		
		try {	
			String token = jsonRequest.getString("token");
			return login(token);
		}
		catch (JSONException e) {
			m_loginException = new InvalidSecretKeyException(Messages.get().container(
	               Messages.ERR_INVALID_DATA_1),e);
			return null;
		}
		
		
		
	}
   
   public CmsUser login(String token)  {
		
		//HttpSession session = null;
		m_loginException = null;
	
		try {
			Payload payload = getPayload(token);
			if (payload==null)
				throw new InvalidSecretKeyException(Messages.get().container(
	                    Messages.ERR_INVALID_TOKEN_1,token));
			
			
			CmsUser user = null;
			String userId = payload.getSubject();
			String email = payload.getEmail();
			//boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
			//String name = (String) payload.get("name");
			//String pictureUrl = (String) payload.get("picture");
			//String locale = (String) payload.get("locale");
			String lastname = (String) payload.get("family_name");
			String firstname = (String) payload.get("given_name");
			
			//System.out.println("User ID: " + userId);

			// me logueo
			try {
				getCmsObject().loginUserByExternalProvider("google", userId, getRequestContext().getRemoteAddress());
				user = getCmsObject().getRequestContext().currentUser();
			} catch (CmsUnknownUserException e) {
				//No existe. ergo, lo genero (para la primera vez)
				
				RegistrationModule regModule = RegistrationModule.getInstance(getCmsObject());
				
				//Verifico que el email no este tomado por otro usuario
				try {
					user = getCmsObject().readUser(regModule.getOu() + email);
				
					CmsUserExternalProvider userProv = new CmsUserExternalProvider(
							user.getId(), user.getName(), "google", userId, token);
					
					getCmsObject().createUserExternalProvider(userProv);
				
					return user;
					//Si llego aca es que existe.... no puede seguir.
					//m_loginException =  new InvalidSecretKeyException(Messages.get().container(
		            //        Messages.ERR_INVALID_EMAIL_1,userId));
					//return null;
				
				}
				catch (CmsDbEntryNotFoundException ex) {
					//Si no lo encontre. sigo adelante.
					
					String password = regModule.getRandomPassword();
					user = regModule.addWebUser(getCmsObject(), email, password, email, firstname, lastname);
					
					
					CmsUserExternalProvider userProv = new CmsUserExternalProvider(
							user.getId(), user.getName(), "google", userId, token);
					
					getCmsObject().createUserExternalProvider(userProv);
					
					
					return user;
				}
				
			} catch (CmsAuthentificationException e) {
				// TODO Auto-generated catch block
				//No consigo el usuario. es posible que este deshabilitado u algun error
				
				 m_loginException = e;
				 return null;
			}
			
			//fijarme si hay que actualizar algo.
			CmsUserExternalProvider userProv = new CmsUserExternalProvider(
					user.getId(), user.getName(), "google", userId, token);
			getCmsObject().updateUserExternalProvider(userProv);
			
			return user;
			  
		} catch (CmsException e) {
            // the login has failed
            m_loginException = e;
        } catch (GeneralSecurityException e) {
			m_loginException = new InvalidSecretKeyException(Messages.get().container(
                    Messages.ERR_INVALID_TOKEN_1),e);

		} catch (IOException e) {
			m_loginException = new InvalidSecretKeyException(Messages.get().container(
                    Messages.ERR_INVALID_TOKEN_1),e);

		}
		
		return null;
		
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
	public void retriveMessages() {
	    if (getLocale()==null)
	    	setLocale(getCmsObject().getRequestContext().getLocale());

    	// initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
	    // generate a new multi messages object and add the messages from the workplace
	    
	    m_messages = new CmsMultiMessages(getLocale());
	    m_messages.addMessages(messages);
    }
	
	protected String getClientId() {
		return config.getParam(siteName, publication, moduleConfigName, "google-cliendId");
	}
	
	public Payload getPayload(String idTokenString) throws GeneralSecurityException, IOException  {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
				new NetHttpTransport(), 
				new JacksonFactory())
			    // Specify the CLIENT_ID of the app that accesses the backend:
			    .setAudience(Collections.singletonList(getClientId()))
			    // Or, if multiple clients access the backend:
			    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
			    .build();

			// (Receive idTokenString by HTTPS POST)

			GoogleIdToken idToken= verifier.verify(idTokenString);
			
			if (idToken != null) {
				long _expires = idToken.getPayload().getExpirationTimeSeconds();
				expirationDate = new Date(_expires * 1000L);
				
				
				return idToken.getPayload();
			  
			}
			else
				return null;
			
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
	
	 public CmsRequestContext getRequestContext() {

	        return getCmsObject().getRequestContext();
	    }
	
	public CmsException getLoginException() {

        return m_loginException;
    }
	
    public boolean isLoginSuccess() {

        return (m_loginException == null);
    }
    
    public boolean isLoggedIn() {

        return !getCmsObject().getRequestContext().currentUser().isGuestUser();
    }
    
    public Date getExpirationDate() {
		return expirationDate;
	}

    
}
