package com.tfsla.diario.webservices;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.webservices.common.interfaces.IPersonGetService;
import com.tfsla.diario.webservices.common.strings.ExceptionMessages;
import com.tfsla.diario.webservices.helpers.ParametersHelper;

public class PersonGetService implements IPersonGetService {

	public PersonGetService(HttpServletRequest request) {
		this.request = request;
	}
	
	@Override
	public JSON getPersonById() throws Exception {
		ParametersHelper helper = new ParametersHelper();
		String id = helper.assertRequestParameter("id", request);
		PersonsDAO dao = new PersonsDAO();
		
		try {
			Persons person = dao.getPersonaById(Long.valueOf(id));
			if(person.getId_person() == 0) {
				throw new Exception(
					String.format(
							ExceptionMessages.ERROR_RECORD_NOT_FOUND_FORMAT,
							"person",
							"id",
							id
						)
				);
			}
			return JSONObject.fromObject(person);
		} catch(Exception e) {
			LOG.error(e);
			throw e;
		}
	}
	
	private HttpServletRequest request;
	private Log LOG = CmsLog.getLog(this);
}