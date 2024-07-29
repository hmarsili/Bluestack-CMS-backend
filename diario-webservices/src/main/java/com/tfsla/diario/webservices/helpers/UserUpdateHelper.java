package com.tfsla.diario.webservices.helpers;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.CmsMemoryMonitor.CacheType;

import com.tfsla.opencms.webusers.RegistrationModule;
import com.tfsla.opencms.webusers.UserDAO;

import net.sf.json.JSONObject;

public class UserUpdateHelper {
	
	public UserUpdateHelper(CmsObject cmsObject, CmsUser cmsUser) {
		_cmsObject = cmsObject;
		_cmsUser = cmsUser;
		_dao = new UserDAO();
	}
	
	public CmsUser assertAndUpdate(JSONObject jsonUser, RegistrationModule regModule) throws Exception {
		ArrayList<String> errors = new ArrayList<String>();
		Boolean isWebUser = _cmsUser.getName().toLowerCase().contains("webuser/");
		String userName = _cmsUser.getName().replace("webUser/", "");
		if(!_dao.existsUsername(_cmsObject, userName)) {
			throw new Exception(String.format("Error reading the user \"%s\"", userName));
		}
		
		if(jsonUser.containsKey("updates")) {
			JSONObject updates = jsonUser.getJSONObject("updates");
			if(updates.containsKey("username")) {
				String newUsername = updates.getString("username");
				if(newUsername == null || newUsername.trim().equals("")) {
					errors.add("The username is mandatory and cannot contain empty spaces only");
				} else if(newUsername.length() > regModule.getUsernameMaxLength()) {
					errors.add(String.format("The username can contain %s letters as maximum", regModule.getUsernameMaxLength()));
				}  else if(_dao.checkUsername(_cmsObject, newUsername, _cmsUser.getId().toString())) {
					errors.add(String.format("Username %s is already used", newUsername));
				} else {
					CmsMemoryMonitor m_monitor = OpenCms.getMemoryMonitor();
					m_monitor.clearUserCache(_cmsUser);
					_dao.updateUsername(newUsername, _cmsUser.getId().toString());
					_cmsUser = _cmsObject.readUser(isWebUser ? "webUser/" + newUsername : newUsername);
					userName = newUsername;
					
					m_monitor.flushCache(CacheType.USER);
				}
			}
			if(updates.containsKey("email")) {
				String email = updates.getString("email");
				if(!email.matches(RegistrationModule.VALIDATION_EMAIL)) {
					errors.add(String.format("Invalid email format for %s", email));
				} else if(_dao.chekNewUserMail(_cmsObject, email, userName)) {
					errors.add(String.format("Email %s is already used", email));
				} else {
					_cmsUser.setEmail(email);
				}
			}
			if(updates.containsKey("password")) {
				String password = updates.getString("password");
				if(password == null || password.trim().equals("")) {
					errors.add("The password is mandatory and cannot contain empty spaces only");
				} else if(password.length() < 5) {
					errors.add("The password must contain 5 letters as minimum");
				} else {
					_cmsObject.setPassword((isWebUser ? "webUser/" : "") + userName, password);
				}
			}
			if(updates.containsKey("first-name")) {
				String firstName = updates.getString("first-name");
				if(!regModule.getUsersFirstNameOptional()) {
					if(firstName == null || firstName.trim().equals("")) {
						errors.add("The first name is mandatory and cannot contain empty spaces only");
					} else {
						_cmsUser.setFirstname(firstName);
					}
				} else {
					_cmsUser.setFirstname(firstName);
				}
			}
			if(updates.containsKey("last-name")) {
				String lastName = updates.getString("last-name");
				if(!regModule.getUsersLastNameOptional()) {
					if(lastName == null || lastName.trim().equals("")) {
						errors.add("The last name is mandatory and cannot contain empty spaces only");
					} else {
						_cmsUser.setLastname(lastName);
					}
				} else {
					_cmsUser.setLastname(lastName);
				}
			}
			if(updates.containsKey("additional-info")) {
				JSONObject info = updates.getJSONObject("additional-info");
				for(Object key : info.keySet()) {
					if(key != null && !key.equals("")) {
						_cmsUser.setAdditionalInfo(key.toString(), info.get(key));
					}
				}
			}
		}
		if(errors.size() > 0) {
			throw new Exception("One or more fields have errors: \n" + StringUtils.join(errors.iterator(), "\n"));
		}
		return _cmsUser;
	}
	
	protected UserDAO _dao;
	protected CmsObject _cmsObject;
	protected CmsUser _cmsUser;
}
