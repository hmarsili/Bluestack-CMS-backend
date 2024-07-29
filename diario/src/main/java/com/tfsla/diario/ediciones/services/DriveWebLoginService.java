package com.tfsla.diario.ediciones.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.CmsJspBean;
import org.opencms.jsp.Messages;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.tfsla.diario.ediciones.model.TipoEdicion;

public class DriveWebLoginService extends A_DriveWebService {

	
	private static final Log LOG = CmsLog.getLog(DriveWebLoginService.class);
	
	private PageContext page; 
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private String clientSecret = "client_secret.json";
	/** OpenCms core CmsObject. */
   
	
	public DriveWebLoginService(PageContext page, HttpServletRequest request, HttpServletResponse response) {
		super();
		this.init(page, request, response);
		
		this.page = page;
		this.request = request;
		this.response = response;
		
	}
	
    private static final java.io.File DATA_STORE_DIR = new java.io.File(OpenCms.getSystemInfo().getWebInfRfsPath(), "google-drive-credentials");
    
    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;


	protected String getModuleName() {
		return "googleDrive";
	}
	
	protected String siteName;
	protected String publication;

	public void initializeContext() throws Exception {
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();

		publication = String.valueOf(PublicationService.getPublicationId(getCmsObject()));

	}
	
	public void initializeContext(String path) throws Exception {
		
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
		
		TipoEdicionBaseService service = new TipoEdicionBaseService();
		TipoEdicion publicacion = service.obtenerTipoEdicion(getCmsObject(), path);
		
		publication = "" + publicacion.getId();
		
		
		LOG.debug("initializeContext | path: " + path + " - siteName: " + siteName + " - publication: " + publication );
	}
	
	
	public boolean isDriveIntegrationEnabled() {
    	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "driveIntegrationEnabled", false);		
	}
	
    public boolean isUseUserAccount() {
    	
    	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "useUserDrive", false);
    }

    public String getUserImpersonalization() {    	
    	return CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "userDriveName", "user");
    }

    private String userId = "";
    
    public void setUserId(String userId) {
    	this.userId = userId;
    }
    
    public String getCredentialId() {
    	return 
    			(!userId.equals("") ? userId :
    				getUserImpersonalization() + (isUseUserAccount() ? 
	    				"-" + getCmsObject().getRequestContext().currentUser().getId().getStringValue() : 
	    					"" 
	    			)
    			);
    }
    
    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE);

        static {
            try {
                DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            } catch (Throwable t) {
                t.printStackTrace();
                //System.exit(1);
            }
        }

        
        private AuthorizationCodeFlow flow=null;
        private Credential credential;
        
        protected GoogleAuthorizationCodeFlow createFlow() throws IOException {
        	// Load client secrets.
            InputStream in =
            		DriveWebLoginService.class.getResourceAsStream(clientSecret);
            GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(DATA_STORE_FACTORY)
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .build();
            
            return flow;
        }
        
        
        protected String getRedirectUri(HttpServletRequest request) throws ServletException, IOException {

            GenericUrl url = new GenericUrl(request.getRequestURL().toString());

            url.setRawPath("/system/modules/com.tfsla.diario.admin/elements/workPapers/login.jsp");

            return url.build();

          }
        
        public boolean isAuthorized() throws IOException {
        	
        	if (flow==null)
        		flow = createFlow();
        	
        	
        	String userId = getCredentialId();
        	
        	//System.out.println("isAuthorized - userid:" + userId);
        	credential = flow.loadCredential(userId);
        	
        	if (credential != null && credential.getExpiresInSeconds()!=null) {
        		LOG.debug("token will expire in " + credential.getExpiresInSeconds() + " seconds");
        		LOG.debug("refresh token: " + credential.getRefreshToken());
	        	if (credential.getExpiresInSeconds()<60) {
	        		credential.refreshToken();
	        		
	        		LOG.debug("token from user " + userId + " was refreshed");
	        		
	        	}
        	}
        	
        	//flow.createAndStoreCredential(response, userId)
        	return (credential != null && credential.getAccessToken() != null);
        }
        
        
        public Credential getCredential() {
        	return credential;
        } 
        
        public void authorize() throws IOException, ServletException {
        	if (flow==null)
        		flow = createFlow();
        	
        	String userId = getCredentialId();
        	
        	LOG.debug("authorize - userId:" + userId);
        	
        	credential = flow.loadCredential(userId);
        	//flow.createAndStoreCredential(response, userId)
        	if (credential == null || credential.getAccessToken() == null) {
        		AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
        		//System.out.println("no credencial - userId:" + userId);
        	      authorizationUrl.setRedirectUri(getRedirectUri(request));
        	      credential = null;
        	      
        	      //provide optional parameters such as the recommended state parameter
        	      authorizationUrl.setState(publication);
        	      //authorizationUrl.set("uid", userId + "-" + publication);
        	      
        	      response.sendRedirect(authorizationUrl.build());
        	      
        	}
        	
        }
        
        
        public void callBack() throws IOException, ServletException {
        	
        	String code = request.getParameter("code");
        	
        	String state = request.getParameter("state");
        	
        	publication = state;

        	String userId = getCredentialId();

        	LOG.debug("callBack - userId:" + userId);
        	LOG.debug("callBack - code:" + code);
        	LOG.debug("callBack - publication: " + state);
        	if (flow==null)
        		flow = createFlow();
        	
        	
       
        	TokenResponse tokenResponse = flow.newTokenRequest(code)
        			.setRedirectUri(getRedirectUri(request))
        			.execute();
        	
        	flow.createAndStoreCredential(tokenResponse, userId);
        }
        
        public boolean isCallBack() {
        	return request.getParameter("code")!=null;
        }
}
