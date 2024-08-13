package com.tfsla.diario.ediciones.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.pines.data.PinesDAO;
import com.tfsla.diario.pines.model.Pin;

import net.sf.json.JSONObject;

public class PinesService extends baseService {

	private static final Log LOG = CmsLog.getLog(PinesService.class);

	public JSONObject newPin(Pin pin)
	{
		JSONObject result = new JSONObject();
		
		PinesDAO pDAO =  new PinesDAO();
		try {
			if(!pDAO.existsPin(pin.getUser(), pin.getPublication(), pin.getResource())) {
				pDAO.insertPin(pin);
				result.put("status","ok");
			}else{
				result.put("status","error");
				result.put("errorCode","999.008");
			}
		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate entry")){
				result.put("status","error");
				result.put("errorCode","999.007");
			}else {
				result.put("status","error");
				result.put("errorCode","999.001");
				result.put("error",e.getMessage());
			}
		}
		
		return result;
		
	}

	public JSONObject deletePin(int idPin)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO =  new PinesDAO();
		try {
			pDAO.deletePin(idPin);
			result.put("status","ok");
		} catch (Exception e) {
				result.put("status","error");
				result.put("errorCode","999.001");
				result.put("error",e.getMessage());
		}		
		return result;
	}


	public JSONObject updateOrder(Pin pin)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO =  new PinesDAO();
		try {
			pDAO.updatePin(pin);
			result.put("status","ok");
		} catch (Exception e) {
			result.put("status","error");
			result.put("errorCode","999.001");
			result.put("error",e.getMessage());
		}
		return result;
	}

	public JSONObject getPines(String userId, int tipoEdicion)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO = new PinesDAO();
		try {
			List<Pin> pines = pDAO.getPines(userId,tipoEdicion);
			result.put("status","ok");
			result.put("pines",pines);
		} catch (Exception e) {
			result.put("status","error");
			result.put("errorCode","999.002");
			result.put("error",e.getMessage());
		}
		return result;
	}
	
	public JSONObject getPines(String userId, int tipoEdicion, int type)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO = new PinesDAO();
		try {
			List<Pin> pines = pDAO.getPines(userId,tipoEdicion,type);
			result.put("status","ok");
			result.put("pines",pines);
		} catch (Exception e) {
			result.put("status","error");
			result.put("errorCode","999.002");
			result.put("error",e.getMessage());
		}
		return result;
	}

	
	public JSONObject isExistsPin(String userId, int tipoEdicion, String resource)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO = new PinesDAO();
		try {
			boolean exitPin = pDAO.existsPin(userId,tipoEdicion,resource);
			result.put("status","ok");
			result.put("isExitPin",exitPin);
		} catch (Exception e) {
			result.put("status","error");
			result.put("errorCode","999.008");
			result.put("error",e.getMessage());
		}
		return result;
	}
	
	public JSONObject getPin(String userId, int tipoEdicion, String resource)
	{
		JSONObject result = new JSONObject();
		PinesDAO pDAO = new PinesDAO();
		
		try {
			if(pDAO.existsPin(userId,tipoEdicion,resource)) {
				Pin pin = pDAO.getPin(userId,tipoEdicion,resource);
				result.put("status","ok");
				result.put("pin",pin);
			}else {
				result.put("status","error");
				result.put("errorCode","999.008");
			}
		} catch (Exception e) {
			result.put("status","error");
			result.put("errorCode","999.001");
			result.put("error",e.getMessage());
		}
		return result;

	}
	
}
