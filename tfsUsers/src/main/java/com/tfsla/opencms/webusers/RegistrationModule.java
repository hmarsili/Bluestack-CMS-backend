package com.tfsla.opencms.webusers;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryAlreadyExistsException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.I_CmsUserDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.MailSettingsService;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.diario.webusers.services.ImagenUsuariosService;
import com.tfsla.exceptions.BusinessException;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.mail.MailSender;
import com.tfsla.opencms.mail.SimpleMail;
import com.tfsla.opencms.webusers.webusersposts.IUserPostsService;
import com.tfsla.opencmsdev.modules.AbstractCmsModule;
import com.tfsla.utils.TFSDriversContainer;
import com.tfsla.utils.TfsAdminUserProvider;

/**
 * Modulo de Administracion de usuarios.
 * 
 * @author vpode
 */
public class RegistrationModule extends AbstractCmsModule {

    public String getOu() {
		return ou;
	}

	private static Map<String, RegistrationModule> instances = new HashMap<String, RegistrationModule>();

	private static final String CMS_CREDENTIAL_COOKIE_NAME = "CMS_CREDENTIAL";
	private static final String CMS_USERNAME_COOKIE_NAME = "CMS_USERNAME";
	private static final String DEFAULT_VALIDATION_USERNAME = "[A-Za-z0-9]*";
	public static final String VALIDATION_PASSWORD = "[A-Za-z0-9]{5,}";
	public static final String VALIDATION_EMAIL = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
	
	public static final String USER_PENDING = "USER_PENDING";
	public static final String SOTANA = "A21W#Q!0";
	public static final String USER_TELEPHONE = "USER_TELEPHONE";
	public static final String USER_CELLPHONE = "USER_CELLPHONE";
	public static final String USER_STATE = "USER_STATE";
	public static final String USER_GENDER = "USER_GENDER";
	public static final String USER_BIRTHDATE = "USER_BIRTHDATE";
	public static final String USER_DNI = "USER_DNI";
	public static final String USER_OPENAUTHORIZATION_PASSWORD = "USER_OPENAUTHORIZATION_PASSWORD";
	public static final String USER_OPENAUTHORIZATION_ACCESS_TOKEN = "USER_OPENAUTHORIZATION_{0}_ACCESS_TOKEN";
	public static final String USER_OPENAUTHORIZATION_ACCESS_SECRET = "USER_OPENAUTHORIZATION_{0}_ACCESS_SECRET";
	public static final String USER_SET_NATIVE_PASSWORD = "USER_SET_NATIVE_PASSWORD";
	public static final String USER_OPENAUTHORIZATION_PROVIDER_KEY = "USER_OPENAUTHORIZATION_PROVIDER_{0}_KEY";
	public static final String defaultUserOU = "webUser/";
	public static final String ADMIN_RESERT_PASSWORD_MAIL_MODEL = "admin.resetPasswordMailModel.html";
	public static final String ADMIN_RESERT_PASSWORD_MAIL_SUBJECT = " CMS-MEDIOS Reset de Password";
	
	// valida formato dd/mm/aaaa
	private static final String dateFormatString = "dd-MM-yyyy";
	public static final DateFormat birthdayDateFormat = new SimpleDateFormat(dateFormatString);
	
	private static String publication = "1";

	public static void setPublication(String publicationId) {
		publication = publicationId;
	}
	
	public synchronized static RegistrationModule getInstance(String siteName, String publication) {
    	String id = siteName + "||" + publication;
    	RegistrationModule instance = instances.get(id);
		
		if (instance == null) {
			instance = new RegistrationModule(siteName, publication);
	    	instances.put(id, instance);
		}

		return instance;
	}
	
	public synchronized static RegistrationModule getInstance(CmsObject cms) {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null){
				publication = "" + tEdicion.getId();
				setPublication(publication);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(siteName, publication);
	}

	String cookieUserSuffix;
	String cookiePasswordSuffix;
	int expirationDays;
	String ou;
	boolean pendingOnCreate;
	boolean sendMailConfirmation;
	String confirmationMailSubject;
	String resetPasswordMailSubject;
	String rememberMailSubject;
	int usersPurgeDays;
	int userNameMaxLength;
	String userNameRegExp;
	String usernameErrorText;
	boolean usersFirstNameOptional;
	boolean usersLastNameOptional;
	boolean usersAllowDuplicateEmail;
	boolean usersBirthdateOptional;
	boolean usersAllowDuplicateDni;
	boolean usersDniOptional;
	boolean usersCountryOptional;
	boolean usersStateOptional;
	boolean usersAddressOptional;
	boolean usersCityOptional;
	boolean usersZipcodeOptional;
	boolean usersTelephoneOptional;
	boolean usersCellphoneOptional;
	String usersMinimumBirhtdate;
	String confirmationMailModel;
	String rememberMailModel;
	String setPasswordProviderNoEmail;
	String resetPasswordMailModel;
	String userNickName;
	String userNickNameRegExp;
	String reservedWords;
	String emailRegExp;
	
	boolean cookieHttpOnly;
	boolean cookieSecure;
	boolean cookieStoreToken;
	
	private RegistrationModule(String siteName, String publication) {
		
		String module = "webusers";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String cookieNameSuffix = config.getParam(siteName, publication, module, "cookieSuffix","");
		
		cookieUserSuffix = CMS_USERNAME_COOKIE_NAME + (!cookieNameSuffix.equals("") ? "_" + cookieNameSuffix: "");
		cookiePasswordSuffix = CMS_CREDENTIAL_COOKIE_NAME + (!cookieNameSuffix.equals("") ? "_" + cookieNameSuffix: "");
		
		expirationDays = config.getIntegerParam(siteName, publication, module, "userSessionExpirationDays",365);
		cookieHttpOnly  = config.getBooleanParam(siteName, publication, module, "cookieHttponly",false);
		cookieSecure  = config.getBooleanParam(siteName, publication, module, "cookieSecure",false);
		cookieStoreToken = config.getBooleanParam(siteName, publication, module, "cookieStoreToken",false);
		
		ou =  config.getParam(siteName, publication, module, "usersOu",defaultUserOU);

		pendingOnCreate = config.getBooleanParam(siteName, publication, module, "usersCreatePending",false);
		sendMailConfirmation = config.getBooleanParam(siteName, publication, module, "sendConfirmationMail",false);

		usersPurgeDays = config.getIntegerParam(siteName, publication, module, "usersPurgeDays",7);
		userNameMaxLength = config.getIntegerParam(siteName, publication, module, "userNameMaxLength",8);
		userNameRegExp = config.getParam(siteName, publication, module, "userNameRegExp",DEFAULT_VALIDATION_USERNAME);
		usernameErrorText = config.getParam(siteName, publication, module, "usernameErrorText","El nombre de usuario solo puede tener numeros y letras.");
		usersFirstNameOptional = config.getBooleanParam(siteName, publication, module, "usersFirstNameOptional",false);
		usersLastNameOptional = config.getBooleanParam(siteName, publication, module, "usersLastNameOptional",false);
		usersAllowDuplicateEmail = config.getBooleanParam(siteName, publication, module, "usersAllowDuplicateEmail",false);
		usersAllowDuplicateDni = config.getBooleanParam(siteName, publication, module, "usersAllowDuplicateDni",false);
		usersBirthdateOptional = config.getBooleanParam(siteName, publication, module, "usersBirthdateOptional",false);
		usersMinimumBirhtdate = config.getParam(siteName, publication, module, "usersMinimumBirhtdate","01-01-1950");
		usersDniOptional = config.getBooleanParam(siteName, publication, module, "usersDniOptional",false);
		usersCountryOptional = config.getBooleanParam(siteName, publication, module, "usersCountryOptional",false);
		usersStateOptional = config.getBooleanParam(siteName, publication, module, "usersStateOptional",false);
		usersAddressOptional = config.getBooleanParam(siteName, publication, module, "usersAddressOptional",false);
		usersCityOptional = config.getBooleanParam(siteName, publication, module, "usersCityOptional",false);
		usersZipcodeOptional = config.getBooleanParam(siteName, publication, module, "usersZipcodeOptional",false);
		usersTelephoneOptional = config.getBooleanParam(siteName, publication, module, "usersTelephoneOptional",false);
		usersCellphoneOptional = config.getBooleanParam(siteName, publication, module, "usersCellphoneOptional",false);
		
		confirmationMailSubject = config.getParam(siteName, publication, module, "confirmationMailSubject","");
		rememberMailSubject = config.getParam(siteName, publication, module, "rememberMailSubject","");
		resetPasswordMailSubject =  config.getParam(siteName, publication, module, "resetPasswordMailSubject","");
		
		confirmationMailModel =  config.getParam(siteName, publication, module, "confirmationMailModel","confirmationMailModel.html");
		rememberMailModel = config.getParam(siteName, publication, module, "rememberMailModel","rememberMailModel.html");
		setPasswordProviderNoEmail = config.getParam(siteName, publication, module, "setPasswordProviderNoEmail","setPasswordProviderNoEmail.html");
		
		resetPasswordMailModel= config.getParam(siteName, publication, module, "resetPasswordMailModel","resetPasswordMailModel.html");
		userNickName = config.getItemGroupParam(siteName, publication, module, "nickname", "entryname", "APODO");
		userNickNameRegExp = config.getItemGroupParam(siteName, publication, module, "nickname", "regExp","[A-Za-z0-9_-]{1,20}" );
		reservedWords = config.getParam(siteName, publication, module, "reservedWords","");
		emailRegExp = config.getItemGroupParam(siteName, publication, module, "email", "regExp",VALIDATION_EMAIL);
		
		CmsLog.getLog(this).debug("Inicia WebUsers en site: "+siteName+" publication: "+publication+" module:"+module+" setNoEmail: "+setPasswordProviderNoEmail);
	}
		

	// ******************************
	// ** Login
	// ******************************
	public String getUserCookieName() {
			return cookieUserSuffix;
	}

	public String getPasswordCookieName() {
		return cookiePasswordSuffix;
	}
	
	public String getUserNameRegExp() {
		return userNameRegExp;
	}
	
	public int getUsernameMaxLength() {
		return userNameMaxLength;
	}
	
	public Boolean getUsersFirstNameOptional() {
		return usersFirstNameOptional;
	}
	
	public Boolean getUsersLastNameOptional() {
		return usersLastNameOptional;
	}
	
	public Boolean getCookieHttpOnly() {
		return cookieHttpOnly;
	}
	
	public Boolean getCookieSecure() {
		return cookieSecure;
	}
	
	public Boolean getCookieStoreToken() {
		return cookieStoreToken;
	}
	
	public void addCookie(String cookieName, String cookieValue, HttpServletResponse response) {
		addCookie(cookieName,cookieValue,response,getUserSessionExpirationDays(),false, false, false);
	}
	
	public void addCookie(String cookieName, String cookieValue, HttpServletResponse response,int sessionExpirationDays, boolean storeToken, boolean httpOnly, boolean secure ) {
		
		String cookieStoreValue = cookieValue;
		
		try {
				if(storeToken)
					cookieStoreValue = com.tfsla.opencms.webusers.Encrypter.encrypt(cookieValue);
		} catch (Exception e) {
				
				e.printStackTrace();
	    }
		
		Cookie cookie;
		
		if(httpOnly)
			cookie = new Cookie(cookieName, cookieStoreValue+"; HttpOnly");
		else 
			cookie = new Cookie(cookieName, cookieStoreValue);
		
		cookie.setMaxAge(86400 * sessionExpirationDays);
		cookie.setPath("/");
		
		if(secure)
			cookie.setSecure(true);
		
		response.addCookie(cookie);
	}
	
	public String getValueFromCookie(HttpServletRequest request, String cookieName, boolean storeToken) {
		try {
			Cookie cookie = this.getCookie(request, cookieName);

			if (cookie != null) {
				
				String cookieValue = cookie.getValue();
				
				if(storeToken)
					cookieValue = com.tfsla.opencms.webusers.Encrypter.decrypt(cookie.getValue());
				
				return cookieValue;
			} else {
				return null;
			}
		} catch(Exception ex){
			return null;
		}
	}

	
	public String getTokenFromCookie(HttpServletRequest request) {
		try {
			
			Cookie cookie = this.getCookie(request, this.getUserCookieName());

			if (cookie != null) {
				
				String token = URLDecoder.decode(cookie.getValue(),"UTF-8");
				
					
				return token;
			} else {
				return null;
			}
		} catch(Exception ex){
			return null;
		}
	}

	public String getBrowserId(HttpServletRequest request) {			
		return request.getParameter("browserId");
	}

	public void tokenize(CmsJspLoginBean loginBean, String browserId, HttpServletResponse response) {
		String token="";
		try {
			token = URLEncoder.encode(loginBean.tokenize(browserId),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Cookie cToken;
		if(this.cookieHttpOnly)
			cToken = new Cookie(this.getUserCookieName(), token+"; HttpOnly");
		else
			cToken = new Cookie(this.getUserCookieName(), token);
		
		cToken.setMaxAge(86400 * this.getUserSessionExpirationDays());  // un dia de duracion (en segundos) es 86400
		cToken.setPath("/");
		
		if(this.cookieSecure)
			cToken.setSecure(true);
		
		response.addCookie(cToken);
	}
	
	
	public void login(CmsJspLoginBean loginBean, String inputPassword, HttpServletResponse response, String rememberMe) {
		try {
			if (rememberMe != null) {
				
				Cookie cUserName;
				
				String cookieUserNameValue = URLEncoder.encode(loginBean.getUserName(), "UTF-8");
				
				if(this.cookieStoreToken)
					cookieUserNameValue = com.tfsla.opencms.webusers.Encrypter.encrypt(URLEncoder.encode(loginBean.getUserName(), "UTF-8"));
					
				if(this.cookieHttpOnly)
					cUserName = new Cookie(this.getUserCookieName(), cookieUserNameValue+"; HttpOnly");
				else
					cUserName = new Cookie(this.getUserCookieName(), cookieUserNameValue);
				
				cUserName.setMaxAge(86400 * this.getUserSessionExpirationDays());  // un dia de duracion (en segundos) es 86400
				cUserName.setPath("/");
				
				if(this.cookieSecure)
				cUserName.setSecure(true);
				
				response.addCookie(cUserName);
				
				
				Cookie cCredential;
				
				String cookiePasswordValue =  inputPassword;
				
				if(this.cookieStoreToken)
					cookiePasswordValue = com.tfsla.opencms.webusers.Encrypter.encrypt(inputPassword);
				
				if(this.cookieHttpOnly)
				     cCredential = new Cookie(this.getPasswordCookieName(), cookiePasswordValue+"; HttpOnly");
				else
					 cCredential = new Cookie(this.getPasswordCookieName(), cookiePasswordValue);
					
				cCredential.setMaxAge(86400 * this.getUserSessionExpirationDays());
				cCredential.setPath("/");
				
				if(this.cookieSecure)
				cCredential.setSecure(true);
				
				response.addCookie(cCredential);
				
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * @return el valor configurado, o 1 por default
	 */
	public int getUserSessionExpirationDays() {
		return expirationDays;
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Cookie cUserName = this.getCookie(request, this.getUserCookieName());
		// validez cero == borrar la cookie
		if (cUserName != null) {
			cUserName.setMaxAge(0);
			cUserName.setValue(null);
			response.addCookie(cUserName);
		}

		Cookie cCredential = this.getCookie(request, this.getPasswordCookieName());
		if (cCredential != null) {
			// validez cero == borrar la cookie
			cCredential.setMaxAge(0);
			cCredential.setValue(null);
			response.addCookie(cCredential);
		}
	}

	/**
	 * @return el nombre de usuario guardado en la cookie o null si la cookie no esta presente
	 */
	public String getUserNameFromCookie(HttpServletRequest request) {
		try {
			Cookie cookie = this.getCookie(request, this.getUserCookieName());

			if (cookie != null) {
				
				String userName = URLDecoder.decode(cookie.getValue(), "UTF-8");
				
				if(this.cookieStoreToken)
					userName = URLDecoder.decode(com.tfsla.opencms.webusers.Encrypter.decrypt(cookie.getValue()), "UTF-8");
					
				return userName;
			} else {
				return null;
			}
		} catch(Exception ex){
			return null;
		}
	}

	/**
	 * @return el password de usuario guardado en la cookie o null si la cookie no esta presente
	 */
	public String getPasswordFromCookie(HttpServletRequest request) {
		Cookie cookie = this.getCookie(request, this.getPasswordCookieName());

		if (cookie != null) {
			
			String password = cookie.getValue();
			
				try {
					if(this.cookieStoreToken)
						password = com.tfsla.opencms.webusers.Encrypter.decrypt(cookie.getValue());
						
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			return password;
		} else {
			return null;
		}
	}
	
	
	public CmsUser addWebUser(
			CmsObject cms, 
			String userName, 
			String password,
			String valueEmail,
			String valueFirstname, 
			String valueLastname 
	) throws CmsException
	{
		CmsObject cmsObject = getAdminCmsObject();

		cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());
		cmsObject.getRequestContext().setCurrentProject(cms.readProject("Offline"));

	    CmsUser newUser = cmsObject.createUser(ou + userName, password, "Web User", new HashMap());

		
		if(!valueFirstname.equals("")) newUser.setFirstname(valueFirstname);
		if(!valueLastname.equals("")) newUser.setLastname(valueLastname);
		newUser.setEmail(valueEmail);
		
		newUser.setEnabled(true);

		newUser.setAdditionalInfo(USER_PENDING,Boolean.toString(pendingOnCreate));
		cmsObject.addUserToGroup(newUser.getName(),"TFS-WEBUSERS" );
		
		cmsObject.writeUser(newUser);

		return newUser;
	}
	
	/**
	 * Permite el alta de un usuario.
	 * @param cms
	 * @param userName
	 * @param password
	 * @param confirmPassword
	 * @param valueFirstname
	 * @param valueLastname
	 * @param valueEmail
	 * @param valueConfirmEmail
	 * @param dni
	 * @param birthday
	 * @param sexo
	 * @param pais
	 * @param provincia
	 * @param localidad
	 * @param domicilio
	 * @param pcode
	 * @param telefono
	 * @param celular
	 * @param atributosAdicionales
	 * @param valoresAdicionales
	 * @param gruposAdicionales
	 * @return CmsUser
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public CmsUser addWebUser(
			CmsObject cms, 
			String userName, 
			String password, 
			String confirmPassword,
			String valueFirstname, 
			String valueLastname, 
			String valueEmail, 
			String valueConfirmEmail,
			String dni,
			String birthday,
			String sexo,
			String pais,
			String provincia,
			String localidad,
			String domicilio,
			String pcode,
			String telefono,
			String celular,
			List<String> atributosAdicionales,
			List<String> valoresAdicionales,
			List<String> gruposAdicionales
	) throws ParseException, Exception {
		this.userSaveAssertions(cms,userName, true,true, valueFirstname, valueLastname, valueEmail, valueConfirmEmail, dni, birthday, provincia, pais, domicilio, localidad, pcode, telefono, celular);
		BusinessException.assertTrue("Debe ingresar password", password.length() > 0);
		BusinessException.assertTrue("Debe confirmar su password", confirmPassword.length() > 0);
		BusinessException.assertTrue("El password no cumple con los requisitos", password.matches(VALIDATION_PASSWORD));
		BusinessException.assertTrue("El password y la confirmaci&oacute;n no coinciden", password.equals(confirmPassword));

		try {
			CmsObject cmsObject = getAdminCmsObject();
			//cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
			cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());
			cmsObject.getRequestContext().setCurrentProject(cms.readProject("Offline"));

		    CmsUser newUser = cmsObject.createUser(ou + userName, password, "Web User", new HashMap());
			
			if(!valueFirstname.equals("")) newUser.setFirstname(valueFirstname);
			if(!valueLastname.equals("")) newUser.setLastname(valueLastname);
			newUser.setEmail(valueEmail);
			newUser.setCountry(pais);
			newUser.setCity(localidad);
			newUser.setAddress(domicilio);
			newUser.setZipcode(pcode);
			newUser.setAdditionalInfo(USER_DNI,dni);
			newUser.setAdditionalInfo(USER_TELEPHONE,telefono);
			newUser.setAdditionalInfo(USER_CELLPHONE,celular);
			newUser.setAdditionalInfo(USER_STATE,provincia);
			newUser.setAdditionalInfo(USER_GENDER,sexo);
			newUser.setAdditionalInfo(USER_BIRTHDATE,birthday);
			newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD, Encrypter.encrypt(password));
			newUser.setAdditionalInfo(USER_SET_NATIVE_PASSWORD,"true");

			int i =0;
			for (String atributoAdicional : atributosAdicionales) {
				newUser.setAdditionalInfo(atributoAdicional ,valoresAdicionales.get(i));
				i++;
			}	
			
			if(pendingOnCreate) newUser.setEnabled(false);

			newUser.setAdditionalInfo(USER_PENDING,Boolean.toString(pendingOnCreate));
			cmsObject.addUserToGroup(newUser.getName(),"TFS-WEBUSERS" );

			for(String grupo : gruposAdicionales) {
				try {
					cmsObject.addUserToGroup(newUser.getName(),grupo );
				} catch (CmsException e) {
					cmsObject.addUserToGroup(newUser.getName(),ou + grupo );
				}
			}
			cmsObject.writeUser(newUser);
			
			if(sendMailConfirmation)
				sendConfirmationMail(newUser,cms);
			
			cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Online"));
			
			return newUser;
		} catch (CmsDbEntryAlreadyExistsException e) {
			throw new BusinessException("Ya existe un usuario con el nombre " + userName);
		} catch (CmsException e) {
			throw ProgramException.wrap("Error al intentar crear el usuario", e);
		}
	}

    /**
     * Alta de usuarios con OPENID
     */
	@SuppressWarnings("rawtypes")
	public CmsUser addWebUserOpenID(
			CmsObject cms,
			String userName,
			String password,
			String firstName,
			String lastName,
			String email,
			String providerName,
			String providerKey,
			String accessToken,
			String accessSecret,
			boolean pendingOnCreate,
			List<String> atributosAdicionales,
			List<String> valoresAdicionales,		
			String publicationId
	) 
	throws ParseException, Exception {

		mailFormatAssertion(email);

		try {
			CmsObject cmsObject = getAdminCmsObject();
			//cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
			cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());
		    cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));

		    CmsUser newUser = cmsObject.createUser(ou + userName, password, "Web User", new HashMap());
			
			if(firstName != null) newUser.setFirstname(firstName);
			if(lastName != null) newUser.setLastname(lastName);
			
			newUser.setEmail(email);
			newUser.setAdditionalInfo(USER_DNI,"");
			newUser.setAdditionalInfo(USER_TELEPHONE,"");
			newUser.setAdditionalInfo(USER_CELLPHONE,"");
			newUser.setAdditionalInfo(USER_STATE,"");
			newUser.setAdditionalInfo(USER_GENDER,TfsUserHelper.SEXO_MASCULINO);
			newUser.setAdditionalInfo(USER_BIRTHDATE,"01-01-1900");
			newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD, Encrypter.encrypt(password));
			newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_PROVIDER_KEY.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(providerKey));
			newUser.setAdditionalInfo(USER_SET_NATIVE_PASSWORD,"false");
			
	    	if(accessToken != null && !accessToken.equals(""))
	    		newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_TOKEN.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(accessToken));
	    	
	    	if(accessSecret != null && !accessSecret.equals(""))
	    		newUser.setAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_SECRET.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(accessSecret));    			
			
			int i = 0;
			String picture = "";
			String nickName = "";
			
			for (String atributoAdicional : atributosAdicionales) {
				newUser.setAdditionalInfo(atributoAdicional ,valoresAdicionales.get(i));
				
				if(atributoAdicional.equals("USER_PICTURE") && valoresAdicionales.get(i) != null && !valoresAdicionales.get(i).equals(""))
					picture = valoresAdicionales.get(i);
				
				if(atributoAdicional.equals("APODO") && valoresAdicionales.get(i) != null && !valoresAdicionales.get(i).equals(""))
					nickName = valoresAdicionales.get(i);
				
				i++;
			}			
			
			if (pendingOnCreate && this.pendingOnCreate) newUser.setEnabled(false);

			newUser.setAdditionalInfo(USER_PENDING, String.valueOf(pendingOnCreate));			
			cmsObject.addUserToGroup(newUser.getName(),"TFS-WEBUSERS" );
			cmsObject.writeUser(newUser);
			
			CmsLog.getLog(this).debug("RegistrationModule - addWebUserOpenID - pendiente: "+pendingOnCreate+ "&&" + this.pendingOnCreate);
			
			if(pendingOnCreate && this.pendingOnCreate) {
				CmsLog.getLog(this).debug("RegistrationModule - addWebUserOpenID - sendConfirmationMail");
				
				if (sendMailConfirmation) sendConfirmationMail(newUser,cms,publicationId);
			}
			
			if((providerName.equals("twitter") || providerName.equals("facebook") || providerName.equals("googlePlus") || providerName.equals("googlelinkedin")) && !picture.equals("") ) {
				String userId = newUser.getId().getStringValue();
				String imagePath = uploadImageOpenId(picture,nickName,userId,cms);  
				
				if(imagePath!=null) {
					newUser.setAdditionalInfo("USER_PICTURE", imagePath);
					cmsObject.writeUser(newUser);
				}
			}
			
			cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Online"));
			return newUser;
		} catch (CmsDbEntryAlreadyExistsException e) {
			throw new BusinessException("Ya existe un usuario con el nombre " + userName);
		} catch (CmsException e) {
			throw ProgramException.wrap("Error al intentar crear el usuario", e);
		}
	}
	
	public boolean isWebUser(CmsObject cms, String username) {
		try {
			cms.readUser(username);
			return false;
		} catch(CmsDbEntryNotFoundException e) {
			return true;
		} catch (Exception e) {
			return true;
		}
	}
	
    public boolean isPending(CmsObject cms, String username) throws CmsException {
    	CmsUser user = cms.readUser(username);
    	Object value = user.getAdditionalInfo(RegistrationModule.USER_PENDING);
    	if(value == null) return false;
    	return (value.toString()).equals("true");
	}
    
	public void saveAcccessTokenAndSecretProvider(String userName, String providerName, String accessToken, String accessSecret) throws CmsException, Exception {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
				
    	CmsUser user = cms.readUser(userName);
    	
    	if(accessToken != null && !accessToken.equals(""))
    		user.setAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_TOKEN.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(accessToken));
    	
    	if(accessSecret != null && !accessSecret.equals(""))
    		user.setAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_SECRET.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(accessSecret));    			
		
		cms.writeUser(user);
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}    
    
	public void associatedProviderKey(String userName, String providerName, String providerKey ) throws CmsException, Exception {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
				
    	CmsUser user = cms.readUser(userName);
		user.setAdditionalInfo(USER_OPENAUTHORIZATION_PROVIDER_KEY.replace("{0}", providerName.toUpperCase()), Encrypter.encrypt(providerKey));		
		cms.writeUser(user);
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}
	
	public void disassociateProviderKey(String userName, String providerName) throws CmsException {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
				
    	CmsUser user = cms.readUser(userName);
		user.deleteAdditionalInfo(USER_OPENAUTHORIZATION_PROVIDER_KEY.replace("{0}", providerName.toUpperCase()));
		user.deleteAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_TOKEN.replace("{0}", providerName.toUpperCase()));
		user.deleteAdditionalInfo(USER_OPENAUTHORIZATION_ACCESS_SECRET.replace("{0}", providerName.toUpperCase()));

		cms.writeUser(user);
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}
	
	public void addAdditionalInfo(String userName, String key, String value) throws CmsException, Exception {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
				
    	CmsUser user = cms.readUser(userName);
    	if(key != null && !key.equals(""))
    		user.setAdditionalInfo(key, value);
    			
		cms.writeUser(user);
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}
	
	public void changeNativePasswordFlag(String userName, boolean value) throws Exception, CmsException {
		changeNativePasswordFlag(userName, value, null);
	}
	
	/* Agrego cmsObjectReference para contemplar la publicacion desde la cual se ejecuta el metodo. Se utiliza para el envio de mails */
	public void changeNativePasswordFlag(String userName, boolean value, CmsObject cmsObjectReference) throws Exception, CmsException {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
				
    	CmsUser user = cms.readUser(userName);
		user.setAdditionalInfo(USER_SET_NATIVE_PASSWORD,String.valueOf(value));		
		cms.writeUser(user);
		
		this.sendConfigNativePassword(user, this.PasswOpenID(cms, userName),cmsObjectReference!=null? cmsObjectReference : cms);	
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}	

    public String PasswOpenID(CmsObject cms, String username) throws CmsException, Exception {
    	CmsUser user = cms.readUser(username);
    	return Encrypter.decrypt((String)user.getAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD));
	}
    
	public void SaveOpenIdPassw(String username, String password ) throws CmsException, Exception {
		CmsObject cms = getAdminCmsObject();
		cms.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
		cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));		
		
    	CmsUser user = cms.readUser(username);
		user.setAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD, Encrypter.encrypt(password));
		cms.writeUser(user);
		cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
	}    

	public void SaveOpenIdPassw(CmsObject cms, String username, String password ) throws CmsException, Exception {
    	CmsUser user = cms.readUser(username);
		user.setAdditionalInfo(USER_OPENAUTHORIZATION_PASSWORD,Encrypter.encrypt(password));
		cms.writeUser(user);
	}

   public String UserNameByMail(CmsObject cms, String email) throws CmsException {
		mailFormatAssertion(email);
		String userName = this.getUserDAO().getUserName(cms, email);
		return userName;
	}
   
   public String UserNameByAdditionalInfo(CmsObject cms, String key, String valueField, boolean fullName) throws CmsException {
		String userName = this.getUserDAO().getUserNameByAdditionalInfo(cms, key, valueField, fullName);	
		return userName;
   }   

   public String UserNameByAdditionalInfo(CmsObject cms, String key, String valueField) throws CmsException {
		return UserNameByAdditionalInfo(cms, key, valueField,false);
   } 
   
   public String UserNameByProviderNameAndKey(CmsObject cms, String providerName, String providerKey) throws Exception {
	   return this.getUserDAO().getUserNameByProviderNameAndKey(cms, providerName, Encrypter.encrypt(providerKey));
   }
   
   public boolean checkExistsPropertyInUsers(CmsObject cms, String property, String value, String userName, boolean isExtra) {
	   return this.getUserDAO().checkExistsPropertyInUsers(cms, property, value, userName, isExtra, true);
   }
   
   public boolean checkExistsPropertyInUsersCI(CmsObject cms, String property, String value, String userName, boolean isExtra) {
	   return this.getUserDAO().checkExistsPropertyInUsers(cms, property, value, userName, isExtra, false);
   }

   public void activateUser(CmsObject cms, CmsUser user) throws CmsException {
		boolean pending = ((String)user.getAdditionalInfo(USER_PENDING)).equals("true");
		boolean isEnabled = user.isEnabled();
		
		if(!isEnabled && !pending)
			BusinessException.assertTrue("El usuario esta deshabilitado", false);

		CmsObject cmsObject = getAdminCmsObject();
		cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
	    cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
	    
	    user.setEnabled(true); 
	    user.setAdditionalInfo(USER_PENDING, "false");
	    
	    cmsObject.writeUser(user);
		cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Online"));
		
		try {
			@SuppressWarnings("unchecked")
			Class<IUserPostsService> c = (Class<IUserPostsService>)Class.forName("com.tfsla.webusersposts.service.UserPostsService");
			IUserPostsService svc = c.newInstance();
			CmsLog.getLog(this).info(String.format("Processing anonymous posts for user %s", user.getName()));
			Boolean resendPassword = svc.processUserActivePosts(user);
			if(resendPassword) {
				String newPassword = this.resetPassword(cms, user);
				this.sendResetPasswordMail(user, newPassword,cms);
				//CmsLog.getLog(this).info(String.format("New password for user %s sent by mail, pass: %s", user.getName(), newPassword));
				CmsLog.getLog(this).info(String.format("New password for user %s sent by mail", user.getName()));
			}
		} catch(Exception e) {
			CmsLog.getLog(this).error("Error when trying to update user pending posts for user " + user.getFullName(), e);
			e.printStackTrace();
		}
	}

	public void deletePendingUsers(CmsObject cms, String ou) {
		this.getUserDAO().deletePendingUsers(cms, ou);
	}

	public int getUserPurgeDays() {
		return usersPurgeDays;
	}

	public void deleteUser(CmsObject cms, String userName) throws CmsException {		
		cms.deleteUser(userName);
	}

	private void userSaveAssertions(CmsObject cms, String userName, boolean validateUsername,boolean isWebUser, String valueFirstname, String valueLastname,
		String valueEmail, String valueConfirmEmail, String dni, String birthday, String provincia, String pais, String domicilio, String localidad, String pcode, String telefono, String celular) {

		if(validateUsername) {
			BusinessException.assertTrue("Debe ingresar nombre de usuario", userName.length() > 0);
			BusinessException.assertTrue("El nombre de usuario debe tener una extension maxima de " + userNameMaxLength + " caracteres.", userName.length() < userNameMaxLength);
			BusinessException.assertTrue(usernameErrorText, userName.matches(userNameRegExp));
		}

		if(!usersFirstNameOptional || !valueFirstname.equals(""))
			BusinessException.assertTrue("Debe ingresar nombre", valueFirstname.length() > 0);
		
		if(hasReservedWords(valueFirstname))
			BusinessException.assertTrue("El nombre contiene palabras reservadas", !hasReservedWords(valueFirstname));
		
		if (!usersLastNameOptional || !valueLastname.equals(""))
			BusinessException.assertTrue("Debe ingresar apellido", valueLastname.length() > 0);
		
		if (hasReservedWords(valueLastname))
			BusinessException.assertTrue("El apellido contiene palabras reservadas", !hasReservedWords(valueLastname));
		
		if(isWebUser) {
			if (!usersAllowDuplicateEmail) {
				boolean mailExists = this.getUserDAO().chekNewUserMail(cms, valueEmail,userName);
				BusinessException.assertTrue("La direccion de mail ingresada corresponde a un usuario previamente registrado", !mailExists);
			}
		}
	
		mailFormatAssertion(valueEmail);

		BusinessException.assertTrue("El mail y su confirmacion deben ser iguales", valueEmail.equals(valueConfirmEmail));

		if (!usersBirthdateOptional || birthday.length()>0) {
			try {
				birthdayDateFormat.parse(birthday);
			} catch (ParseException e) {
				throw new BusinessException("La fecha de nacimiento debe ingresarse en formato "
						+ dateFormatString.replace('y', 'a').replace('M', 'm'));
			}

			Date birthDate;
			try {
				birthDate = birthdayDateFormat.parse(birthday);
				Date minDate = birthdayDateFormat.parse(usersMinimumBirhtdate);
				BusinessException.assertTrue("La fecha de nacimiento ingresada es invalida.", !birthDate.before(minDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (!usersAllowDuplicateDni)
			BusinessException.assertTrue("El dni ingresado corresponde a un usuario previamente registrado", !this.getUserDAO().chekNewUserDni(cms, dni, userName));
		
		if (!usersDniOptional || dni.length()>0)
			BusinessException.assertTrue("Debe ingresar dni", dni.length() > 0);

		if (!usersCountryOptional || pais.length()>0)
			BusinessException.assertTrue("Debe ingresar una pais", pais.length() > 0);

		if (!usersStateOptional || provincia.length()>0)
			BusinessException.assertTrue("Debe ingresar una provincia", provincia.length() > 0);

		if (!usersAddressOptional || domicilio.length()>0) 
			BusinessException.assertTrue("Debe ingresar una direccion", domicilio.length() > 0);

		if (!usersCityOptional || localidad.length()>0) 
			BusinessException.assertTrue("Debe ingresar una localidad", localidad.length() > 0);

		if (!usersZipcodeOptional || pcode.length()>0) {
			BusinessException.assertTrue("Debe ingresar una codigo postal", pcode.length() > 0);
			BusinessException.assertTrue("El codigo postal unicamente puede contener letras y numeros", pcode.matches(DEFAULT_VALIDATION_USERNAME));
		}
		if (!usersTelephoneOptional || telefono.length()>0)
			BusinessException.assertTrue("Debe ingresar un telefono", telefono.length() > 0);

		if (!usersCellphoneOptional || celular.length()>0)
			BusinessException.assertTrue("Debe ingresar un celular", celular.length() > 0);
	}

	private void mailFormatAssertion(String valueEmail) {
		CmsLog.getLog(this).debug("Webusers - mailFormatAssertion - valueEmail" + valueEmail + " - emailRegExp: " + emailRegExp);
		BusinessException.assertTrue("El mail no tiene un formato correcto", valueEmail.length() > 0
				&& valueEmail.matches(emailRegExp));
	}
	
	public void sendConfirmationMail(final CmsUser cmsUser, CmsObject cms){
		sendConfirmationMail(cmsUser, cms, publication);
	}
	
	public void sendConfirmationMail(final CmsUser cmsUser, CmsObject cms, String publicationId) {
		CmsLog.getLog(this).debug("Webusers - sendConfirmationMail - publicationID: "+ publicationId+"("+publication+")");
		try {
			String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		 	CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		 			 	
		 	confirmationMailSubject = config.getParam(siteName,publication,"webusers", "confirmationMailSubject","");
			confirmationMailModel =  config.getParam(siteName,publication, "webusers", "confirmationMailModel","confirmationMailModel.html");
	
			CmsLog.getLog(this).debug("Webusers - sendConfirmationMail - confirmationMailModel: "+ confirmationMailModel);
			
			final SimpleMail confirmationMail = new SimpleMail();
			confirmationMail.addTo(cmsUser.getEmail());
			confirmationMail.setSubject(confirmationMailSubject);
			//confirmationMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
			confirmationMail.setFrom(MailSettingsService.getMailFrom(cms));
	
			String messageContent = readFileContents(confirmationMailModel);
	
			confirmationMail.setValue("userId", cmsUser.getId());
			confirmationMail.setValue("userName", cmsUser.getName().replace(ou, ""));
			confirmationMail.setValue("mail", cmsUser.getEmail());
			confirmationMail.setValue("nickname", cmsUser.getAdditionalInfo(userNickName) );
			confirmationMail.setHtmlContents(messageContent);
	
			MailSender.getInstance().sendMail(cmsUser, confirmationMail);
		} catch (Exception e) {
			CmsLog.getLog(this).error("Webusers - sendConfirmationMail - Error: "+e.getMessage());
		}
	}

	public void updateWebUser(
			CmsObject cms, 
			CmsUser webUser, 
			String firstName, 
			String lastName,
			String email, 
			String confirmEmail, 
			String dni,
			String birthDay, 
			String aviso,
			String sexo,
			String pais,
			String provincia,
			String localidad,
			String domicilio,
			String pcode,
			String telefono,
			String celular,
			List<String> atributosAdicionales,
			List<String> valoresAdicionales
		) throws ParseException, CmsException {
		
		CmsObject cmsObject = getAdminCmsObject();
		cmsObject.getRequestContext().setSiteRoot(OpenCms.getSiteManager().getDefaultSite().getSiteRoot());
	    cmsObject.getRequestContext().setCurrentProject(cmsObject.readProject("Offline"));
	    
	    String username = webUser.getName();
	    
	    if(webUser.isWebuser()) {
	    	//username = "/"+username;
	    	username = username.replace(ou, "");
	    }
	    
		this.userSaveAssertions(cms,username, false , webUser.isWebuser(), firstName, lastName, email, confirmEmail, dni, birthDay, provincia, pais, domicilio, localidad, pcode, telefono, celular);
		
		webUser.setFirstname(firstName);
		webUser.setLastname(lastName);
		webUser.setEmail(email);
		webUser.setCountry(pais);
		webUser.setCity(localidad);
		webUser.setAddress(domicilio);
		webUser.setZipcode(pcode);
		webUser.setAdditionalInfo(USER_DNI,dni);
		webUser.setAdditionalInfo(USER_TELEPHONE,telefono);
		webUser.setAdditionalInfo(USER_CELLPHONE,celular);
		webUser.setAdditionalInfo(USER_STATE,provincia);
		webUser.setAdditionalInfo(USER_GENDER,sexo);
		webUser.setAdditionalInfo(USER_BIRTHDATE,birthDay);

		int i = 0;
		for (String atributoAdicional : atributosAdicionales) {
			webUser.setAdditionalInfo(atributoAdicional ,valoresAdicionales.get(i));
			i++;
		}
		
		cmsObject.writeUser(webUser);
	}

	protected void sendRememberMail(final CmsUser cmsUser, CmsObject cms) {
		final SimpleMail rememberMail = new SimpleMail();
		rememberMail.addTo(cmsUser.getEmail());
		rememberMail.setSubject(rememberMailSubject);
		//rememberMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		rememberMail.setFrom(MailSettingsService.getMailFrom(cms));

		String messageContent = readFileContents(rememberMailModel);

		rememberMail.setValue("userName", cmsUser.getName().replace(ou, ""));
		rememberMail.setValue("mail", cmsUser.getEmail());
		rememberMail.setValue("nickname", cmsUser.getAdditionalInfo(userNickName) );
		rememberMail.setHtmlContents(messageContent);

		MailSender.getInstance().sendMail(cmsUser, rememberMail);
	}

	public CmsUser retrieveUser(CmsObject cms, String userName ) {
		CmsUser webUser=null;
		try {
			webUser = cms.readUser("/" + userName);
		} catch (CmsException E) {
			try {
				webUser = cms.readUser(ou + userName);
			} catch (CmsException e) {
				throw new BusinessException("Los datos ingresados no corresponden a un usuario registrado (" + userName + ")");
			}
		}

		return webUser;
	}

	public void forgotUserName(CmsObject cms, String email) throws CmsException {
		mailFormatAssertion(email);
		String userName = this.getUserDAO().getUserName(cms, email);
		BusinessException.assertTrue("Los datos ingresados no corresponden a un usuario registrado", userName!= null);
		CmsUser webUser = null;
		try {
			webUser = cms.readUser(ou + userName);
		} catch (CmsException e) {
			throw new BusinessException("Los datos ingresados no corresponden a un usuario registrado");
		}
		sendRememberMail(webUser,cms);
	}

	public String forgotPassword(CmsObject cms, String userName, String email) {
		return this.forgotPassword(cms, userName, email, true);
	}
	
	public String forgotPassword(CmsObject cms, String userName, String email, boolean concatOu) {
		mailFormatAssertion(email);
		BusinessException.assertTrue("El nombre de usuario es requerido", userName.length() > 0);
		String newPassword = "";
		try {
			CmsUser webUser = null;
			if(concatOu) 
				webUser = cms.readUser(ou + userName);
			else
				webUser = cms.readUser(userName);

			if (email.equals(webUser.getEmail())) {

				boolean pending = true;
				try {
					pending = ((String)webUser.getAdditionalInfo(USER_PENDING)).equals("true");
				} catch(Exception ex) {
					ex.printStackTrace();
				}

				if (!webUser.isEnabled() && !pending) {
					throw new BusinessException("El usuario se encuentra deshabilitado.");
				}
				if (!pending) {
					newPassword = this.resetPassword(cms, webUser);
					this.sendResetPasswordMail(webUser, newPassword,cms);
				} else {
					sendConfirmationMail(webUser,cms);
					throw new BusinessException("El usuario se encuentra inactivo. Hemos reenviado el e-mail con las instrucciones de activación. Por favor, complete los pasos para activar su cuenta.");
				}
			} else {
				throw new BusinessException("El usuario y mail informados no coinciden");
			}
		} catch (CmsDbEntryNotFoundException e) {
			throw new BusinessException("No existe un usuario registrado con el nombre indicado");
		} catch (CmsException e) {
			throw ProgramException.wrap("Error al intentar reestablecer la clave", e);
		}
		return newPassword;
	}

	public void adminResetPassword(CmsObject cms, CmsUser webUser) {
		this.adminResetPassword(cms, webUser, false);
	}
	public void adminResetPassword(CmsObject cms, CmsUser user, boolean isAdminUser) {
		String newPassword;
		try {
			newPassword = this.resetPassword(cms, user);
			
			this.sendResetPasswordMail(user, newPassword,cms, isAdminUser);
			
			if (user.isWebuser())
				SaveOpenIdPassw(cms,user.getName(),newPassword);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void adminChangePassword(CmsObject cms, CmsUser user, String newPassword) {
		this.adminChangePassword(cms, user, newPassword, false);		
	}
	
	public void adminChangePassword(CmsObject cms, CmsUser user, String newPassword, boolean sendMail) {
		CmsDbContext dbContext = null;
		try {
			I_CmsUserDriver userDriver = TFSDriversContainer.getInstance().getDriverManager().getUserDriver();
		
			dbContext = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
					cms.getRequestContext());
	
			userDriver.writePassword(dbContext, user.getName(), null, newPassword);
			
			CmsLog.getLog(this).info(String.format("Password for user %s changed by %s", user.getName(), cms.getRequestContext().currentUser().getName()));
			
			try {
				if (user.isWebuser())
					SaveOpenIdPassw(user.getName(), newPassword);
			} catch(Exception ex) {
				ex.printStackTrace();
			}

			if (sendMail)
				this.sendResetPasswordMail(user, newPassword,cms, true);
			
		} catch (CmsDataAccessException e) {
			e.printStackTrace();
		} catch (CmsPasswordEncryptionException e) {
			e.printStackTrace();
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (dbContext != null) {
				dbContext.clear();
			}
		}
	}
	
	public void forgotUserNameByDni(CmsObject cms, String dni) throws CmsException {
		BusinessException.assertTrue("Debe ingresar dni", dni.length() > 0);
		String userName = this.getUserDAO().getUserNameByDni(cms, dni);
		BusinessException.assertTrue("Los datos ingresados no corresponden a un usuario registrado", userName!= null);

		CmsUser webUser=null;
		try {
			webUser = cms.readUser(ou + userName);
		} catch (CmsException e) {
			 throw new BusinessException("Los datos ingresados no corresponden a un usuario registrado");
		}
		sendRememberMail(webUser,cms);
	}

	private String resetPassword(CmsObject cms, CmsUser user) throws CmsException {
		CmsDbContext dbContext = null;
		try {
			String newPassword = getRandomPassword();

			I_CmsUserDriver userDriver = TFSDriversContainer.getInstance().getDriverManager().getUserDriver();
			dbContext = TFSDriversContainer.getInstance().getDBContextFactory().getDbContext(
					cms.getRequestContext());

			userDriver.writePassword(dbContext, user.getName(), null, newPassword);
			
			try {
				if (user.isWebuser())
					SaveOpenIdPassw(user.getName(), newPassword);
			} catch(Exception ex){
				ex.printStackTrace();
			}

			return newPassword;
		} finally {
			if (dbContext != null) {
				dbContext.clear();
			}
		}
	}

	/**
	 * @return un string aleatorio de 8 caracteres
	 */
	public static String getRandomPassword() {
		int n = 8;
		char[] pw = new char[n];
		int c = 'A';
		int r1 = 0;
		for (int i = 0; i < n; i++) {
			r1 = (int) (Math.random() * 3);
			switch (r1) {
				case 0:
					c = '0' + (int) (Math.random() * 10);
					break;
				case 1:
					c = 'a' + (int) (Math.random() * 26);
					break;
				case 2:
					c = 'A' + (int) (Math.random() * 26);
					break;
			}
			pw[i] = (char) c;
		}
		return new String(pw);
	}
	
	private void sendConfigNativePassword(CmsUser webUser, String newPassword, CmsObject cms) {
		final SimpleMail resetPasswordMail = new SimpleMail();
		resetPasswordMail.addTo(webUser.getEmail());
		resetPasswordMail.setSubject("Has seleccionado una nueva contraseña");
		//resetPasswordMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
		resetPasswordMail.setFrom(MailSettingsService.getMailFrom(cms));
		
		String messageContent = readFileContents(setPasswordProviderNoEmail);

		resetPasswordMail.setValue("userName", webUser.getName().replace(ou, ""));
		resetPasswordMail.setValue("newPassword", newPassword);
		resetPasswordMail.setValue("nickname", webUser.getAdditionalInfo(userNickName) );
		resetPasswordMail.setHtmlContents(messageContent);

		MailSender.getInstance().sendMail(webUser, resetPasswordMail);
	}	

	private void sendResetPasswordMail(CmsUser webUser, String newPassword, CmsObject cms) {
		this.sendResetPasswordMail(webUser, newPassword, cms, false);
	}
	
	private void sendResetPasswordMail(CmsUser user, String newPassword, CmsObject cms, boolean isAdminUser) {
		final SimpleMail resetPasswordMail = new SimpleMail();
		resetPasswordMail.addTo(user.getEmail());

		if (isAdminUser)
			resetPasswordMail.setSubject(ADMIN_RESERT_PASSWORD_MAIL_SUBJECT);
		else 
			resetPasswordMail.setSubject(resetPasswordMailSubject);

		resetPasswordMail.setFrom(MailSettingsService.getMailFrom(cms));
		
		String messageContent;
		
		CmsLog.getLog(this).debug("RegistrationModule - sendResetPasswordMail - ADMIN_RESERT_PASSWORD_MAIL_MODEL: "+ADMIN_RESERT_PASSWORD_MAIL_MODEL);
		CmsLog.getLog(this).debug("RegistrationModule - sendResetPasswordMail - user.getEmail(): "+user.getEmail());
		CmsLog.getLog(this).debug("RegistrationModule - sendResetPasswordMail - newPassword: "+ newPassword );
		
		if (isAdminUser)
			messageContent = readFileContents(ADMIN_RESERT_PASSWORD_MAIL_MODEL);
		else
			messageContent = readFileContents(resetPasswordMailModel);
		
		resetPasswordMail.setValue("userName", user.getName().replace(ou, ""));
		resetPasswordMail.setValue("newPassword", newPassword);
		resetPasswordMail.setValue("nickname", user.getAdditionalInfo(userNickName) );
		resetPasswordMail.setHtmlContents(messageContent);

		MailSender.getInstance().sendMail(user, resetPasswordMail);
	}
	
	protected UserDAO getUserDAO() {
		return new UserDAO();
	}

	public void changePassword(HttpServletRequest request, HttpServletResponse response, CmsObject cms, CmsUser user, String actualPassword, String newPassword) throws Exception {
		Cookie cooky = this.getCookie(request, this.getPasswordCookieName());
		if(cooky != null) {
			cooky.setValue(newPassword);
			response.addCookie(cooky);
		}
		cms.setPassword(user.getName(), actualPassword, newPassword);
		
		try {
			if (user.isWebuser())
				SaveOpenIdPassw(user.getName(), newPassword);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isValidNickName(String nickname){
		boolean isValid = false;
		
		if(nickname.matches(userNickNameRegExp) && !hasReservedWords(nickname) )
			isValid = true;
		
		return isValid;
	}
	
	public boolean getSendMailConfirmation() {
		return this.sendMailConfirmation;
	}
	
	public boolean getPendingOnCreate() {
		return this.pendingOnCreate;
	}
	
	public boolean hasReservedWords(String valueField) {
		String[] regexStr = reservedWords.split(",");
		String regex = "";

	    for (int i = 0; i< regexStr.length; i++) {
		      String token = regexStr[i].trim();
		      token = token.replaceAll(" ","\\\\s");
		      
		      regex = regex + token;
		      
		      if(i<regexStr.length-1)
		          regex = regex+"|";
		}
		
		if(!regex.equals("")) {    
		    regex = "("+regex+")";
		    
		    Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		    Matcher m = p.matcher(valueField);
		    
		    return m.find();
		}
		
		return false;
	}
	
    public String uploadImageOpenId(String urlImage, String nickName, String userId ,CmsObject cms){
		URL url;
		try {
			url = new URL(urlImage);
			String extension = ".jpg";
			if(urlImage.indexOf(".jpg")>-1) extension = ".jpg";
			if(urlImage.indexOf(".png")>-1) extension = ".png";
			if(urlImage.indexOf(".gif")>-1) extension = ".gif";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
			String fileName = nickName + "_" + userId + "_" + sdf.format(new Date()) + extension;
			InputStream in = new BufferedInputStream(url.openStream());
			String path = "/" + ImagenUsuariosService.getInstance(cms).getDefaultVFSUploadFolder(null);
			String imageVFS = ImagenUsuariosService.getInstance(cms).uploadRFSFile(path, fileName, null, in);
			
			CmsObject cmsObject = getAdminCmsObject();
			cmsObject.getRequestContext().setSiteRoot("/");
			cmsObject.getRequestContext().setCurrentProject(cms.readProject("Offline"));
			OpenCms.getPublishManager().publishResource(cmsObject, imageVFS);
			return imageVFS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
    
    private CmsObject getAdminCmsObject() throws CmsException {
    	CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
		CmsObject cmsObject = OpenCms.initCmsObject(_cmsObject);
		return cmsObject;
    }
    
}