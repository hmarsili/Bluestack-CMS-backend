package com.tfsla.diario.webservices;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.webservices.common.interfaces.IForgotPasswordService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.common.strings.StringConstants;
import com.tfsla.diario.webservices.core.services.GuestSessionService;
import com.tfsla.opencms.webusers.RegistrationModule;

public class ForgotPasswordService extends GuestSessionService implements IForgotPasswordService {

	public ForgotPasswordService(HttpServletRequest request) throws Throwable {
		super(request);
	}

	@Override
	protected JSON doExecute() throws Throwable {
		JSONObject jsonResponse = new JSONObject();
		try {
			String email = this.assertRequestParameter("email");
			RegistrationModule regModule = RegistrationModule.getInstance(cms);
			String userName = regModule.UserNameByMail(cms, email);
			if(userName == null || userName.equals("")) {
				throw new Exception(ExceptionMessages.ERROR_INVALID_USER_FOR_EMAIL);
			}
			regModule.forgotPassword(cms, userName, email);
			jsonResponse.put(StringConstants.STATUS, StringConstants.OK);
		} catch(Exception e) {
			jsonResponse.put(StringConstants.STATUS, StringConstants.ERROR);
			jsonResponse.put(StringConstants.MESSAGE, e.getMessage());
		}
		return jsonResponse;
	}
}
