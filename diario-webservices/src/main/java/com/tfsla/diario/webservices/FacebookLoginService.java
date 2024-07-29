package com.tfsla.diario.webservices;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceAction;

import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.Token;
import com.tfsla.diario.webservices.common.WebSession;
import com.tfsla.diario.webservices.common.interfaces.IFacebookLoginService;
import com.tfsla.diario.webservices.core.SessionManager;
import com.tfsla.diario.webservices.core.TokenGenerator;
import com.tfsla.diario.webservices.helpers.FacebookLoginHelper;
import com.tfsla.diario.webservices.helpers.NicknameHelper;
import com.tfsla.diario.webservices.data.UserInfoDAO;
import com.tfsla.opencms.webusers.Encrypter;
import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.TfsUserHelper;
import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.DataSyncProcess;
import com.tfsla.utils.TfsAdminUserProvider;

public class FacebookLoginService implements IFacebookLoginService {
	
	public FacebookLoginService(PageContext context, HttpServletRequest request, HttpServletResponse response) {
		this(context, request, response, null, null);
	}
	
	public FacebookLoginService(PageContext context, HttpServletRequest request, HttpServletResponse response, String site, String publication) {
		this.request = request;
		this.response = response;
		this.context = context;
		this.siteName = site;
		this.publication = publication;
		this.logger = CmsLog.getLog(this);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Token loginAndRegister(String userToken, String email) throws Throwable {
		JSONObject data = this.getFBtokenInformation(userToken);
		UserInfoDAO dao = new UserInfoDAO();
		try {
			dao.openConnection();
			String username = dao.getUsernameByEmail(email);
			
			//Register new user into the CMS
			if(username == null || username.equals("")) {
				CmsObject cmsObject = this.getAdminCmsObject();
				String password = RegistrationModule.getRandomPassword();
				CmsUser newUser = cmsObject.createUser(OU + email, password, "Web User", new HashMap());
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.YEAR, -10);
				newUser.setEmail(email);
				newUser.setAdditionalInfo("USER_DNI","");
				newUser.setAdditionalInfo("USER_TELEPHONE","");
				newUser.setAdditionalInfo("USER_CELLPHONE","");
				newUser.setAdditionalInfo("USER_STATE","");
				newUser.setAdditionalInfo("USER_GENDER",TfsUserHelper.SEXO_MASCULINO);
				newUser.setAdditionalInfo("USER_BIRTHDATE","01-01-1900");
				newUser.setAdditionalInfo("USER_LAST_SYNC", cal.getTime());
				newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_PROVIDER_KEY, Encrypter.encrypt(data.getString("user_id")));
				newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_TOKEN, Encrypter.encrypt(userToken));
				newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD, Encrypter.encrypt(password));
				newUser.setAdditionalInfo(USER_SET_NATIVE_PASSWORD, "false");
				cmsObject.addUserToGroup(newUser.getName(), "TFS-WEBUSERS");
				cmsObject.writeUser(newUser);
				
				this.facebookDataSync(cmsObject, email, userToken);
			}
		} catch(Exception e) {
			logger.error("Error trying to register user, email: " + email, e);
			e.printStackTrace();
		} finally {
			dao.closeConnection();
		}
		
		return this.login(userToken);
	}
	
	public Token login(String userToken) throws Throwable {
		JSONObject data = this.getFBtokenInformation(userToken);
		int expirationDateAsInt = data.getInt("expires_at");
		final Date expirationDate = new Date(expirationDateAsInt * 1000L);
		String fbUserid = data.getString("user_id");
		
		//Valido si el usuario ya está registrado
		String encryptedId = Encrypter.encrypt(fbUserid);
		UserInfoDAO dao = new UserInfoDAO();
		dao.openConnection();
		String userid = dao.getUserForInfo(PROVIDER_KEY, encryptedId);
		dao.closeConnection();
		
		if(userid != null && !userid.trim().equals("")) {
			//Ya registrado, iniciar sesión y generar token

			if(this.siteName == null || this.siteName.equals("")) {
				this.siteName = OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
			}
			final CmsObject cmsObject = this.getAdminCmsObject();
			cmsObject.getRequestContext().setSiteRoot(this.siteName);
			CmsUser user = cmsObject.readUser(new CmsUUID(userid));
			final CmsJspLoginBean loginBean = new CmsJspLoginBean(context, request, response);
			
			//Obtener password de OpenID
			String password = "";
			try {
				//ou + username ?
				password = RegistrationModule.getInstance(cmsObject).PasswOpenID(cmsObject, user.getName());
			} catch(Exception exPassword) {
				password = RegistrationModule.getInstance(cmsObject).forgotPassword(cmsObject, user.getName(), user.getEmail(), false);
  				RegistrationModule.getInstance(cmsObject).SaveOpenIdPassw(user.getEmail(), password);
			}
			
			//Generar sesión de VFS y registrarla en el session pool
			loginBean.login(user.getName(), password, "Online");
			long duration = expirationDateAsInt - (int)(new Date().getTime()) / 1000;
			Token token = TokenGenerator.getToken(duration);
			
			WebSession webSession = new WebSession() {{
				setCmsObject(loginBean.getCmsObject());
				setContext(context);
				setLoginBean(loginBean);
				setRequest(request);
				setResponse(response);
				setSite(siteName);
				setPublication(publication);
				setExpirationDate(expirationDate);
			}};
			SessionManager.saveSession(token, webSession);
			
			this.facebookDataSync(cmsObject, user.getName(), userToken);
			
			return token;
		} else {
			//Retornar error, que se registre el usuario
			throw new Exception("The user is not yet registered into the site");
		}
	}
	
	private void facebookDataSync(CmsObject cmsObject, String email, String userToken) {
		try {
			String userName = email.startsWith(OU) ? email : OU + email;
			UserProfileData profileData = new UserProfileData();
			JSONObject providerData = FacebookLoginHelper.getProviderData(userToken, siteName, publication);
			profileData.setProviderResponse(providerData);
			profileData.setUserUrl(providerData.getString("link"));
			profileData.setFirstName(providerData.getString("first_name"));
			profileData.setLastName(providerData.getString("last_name"));
			profileData.setKey(providerData.getString("id"));
			profileData.setPicture(String.format(USER_PICTURE_URL_FORMAT, providerData.getString("id")));
			String nickName = providerData.getString("first_name") + "_" + providerData.getString("last_name");
			try {
				nickName = NicknameHelper.getValidNickname(userName, nickName);
			} catch(Exception e) {
				logger.error("Error while processing user nickname, userName: " + userName, e);
				e.printStackTrace();
			} finally {
				profileData.setNickName(nickName);
			}
			DataSyncProcess dataSync = new DataSyncProcess(
				userName,
				cmsObject
			);
			dataSync.providerDataSync("facebook", profileData);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error on data sync process", e);
		}
	}
	
	
	private JSONObject getFBtokenInformation(String userToken) throws Exception {
		if(this.tokenInformation == null) {
			//Obtengo el app token del site
			String appToken = FacebookLoginHelper.getAppToken(siteName, publication);
			
			//Obtengo información sobre el token de usuario
			JSONObject jsonObject = FacebookLoginHelper.getTokenInformation(userToken, appToken);
			this.tokenInformation = jsonObject.getJSONObject("data");
			if(this.tokenInformation.containsKey("error")) {
				JSONObject error = this.tokenInformation.getJSONObject("error");
				logger.error("FacebookLoginService - response: " + jsonObject.toString());
				throw new Exception("Token error: " + error.getString("message"));
			}
		}
		return this.tokenInformation;
	}
	
	private CmsObject getAdminCmsObject() throws CmsException {
		if(adminCmsObject == null) {
			CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
			adminCmsObject = OpenCms.initCmsObject(_cmsObject);

		}
		return adminCmsObject;
	}
	
	private Log logger;
	private String siteName;
	private String publication;
	private PageContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private JSONObject tokenInformation;
	private CmsObject adminCmsObject;
	
	private static final String PROVIDER_KEY = "USER_OPENAUTHORIZATION_PROVIDER_FACEBOOK_KEY";
	private static final String USER_OPENAUTHORIZATION_ACCESS_TOKEN = "USER_OPENAUTHORIZATION_FACEBOOK_ACCESS_TOKEN";
	private static final String USER_OPENAUTHORIZATION_PROVIDER_KEY = "USER_OPENAUTHORIZATION_PROVIDER_FACEBOOK_KEY";
	private static final String USER_OPENAUTHORIZATION_PASSWORD = "USER_OPENAUTHORIZATION_PASSWORD";
	private static final String USER_SET_NATIVE_PASSWORD = "USER_SET_NATIVE_PASSWORD";
	private static final String USER_PICTURE_URL_FORMAT = "https://graph.facebook.com/%s/picture?type=large";
	private static final String OU = "webUser/";
}
