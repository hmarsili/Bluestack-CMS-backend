package com.tfsla.diario.pines.data;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsUUID;
import org.opencms.main.CmsLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.diario.ediciones.services.PinesService;

import com.tfsla.diario.pines.model.Pin;

import java.sql.PreparedStatement;
import com.tfsla.data.baseDAO;

import net.sf.json.JSONObject;

public class PinMigration  extends baseDAO {

	private static final Log LOG = CmsLog.getLog(PinMigration.class);

	public String migrar(String pub, CmsObject cms) throws Exception
	{
		String result = "";
		
		List<CmsUUID> usersIdList = new ArrayList<CmsUUID>();

		LOG.debug("INICIO PROCESO - Se van a procesar los usuarios de la pub " + pub);
		result = "INICIO PROCESO - Se van a procesar los usuarios de la pub " + pub;
		result += "<br/>";
		
		String dataKeytSearch = "USER_PIN_"+pub+"%";
		
		try {
			if (!connectionIsOpen())
				OpenConnection();

			PreparedStatement stmt;

			stmt = conn.prepareStatement("select USER_ID from CMS_USERDATA WHERE DATA_KEY LIKE ?;");

			stmt.setString(1, dataKeytSearch);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String userid = rs.getString("USER_ID");
				usersIdList.add(new CmsUUID(userid));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly())
				closeConnection();
		}
		
		PinesService pService = new PinesService();
		
		if (usersIdList.size()>0) {
			
			LOG.debug("se encontraron " + usersIdList.size() + " usuarios para migra");
			result += "se encontraron " + usersIdList.size() + " usuarios para migra";
			result += "<br/>";
			
		for (int i=0; i < usersIdList.size() ; i ++) {
				
				boolean statusMigrate = true;
				
				CmsUUID userID = usersIdList.get(i);
				
				LOG.debug("Se procesa el usuario " + userID);
				result += "Se procesa el usuario " + userID;
				result += "<br/>";

				CmsUser cmsuser = cms.readUser(userID);
				
				String aditionalInfoKey = "USER_PIN_"+pub;
				if (((String)cmsuser.getAdditionalInfo(aditionalInfoKey)).indexOf(",") > -1) {
					String[] valuesSplt = ((String) cmsuser.getAdditionalInfo(aditionalInfoKey)).split(",");
					
					for (int j=0;j < valuesSplt.length ; j ++) {
					
						if (!valuesSplt[j].equals("")) {
							Pin newPin = new Pin();			
							newPin.setResource(valuesSplt[j]);
							newPin.setUser(userID.toString());
							newPin.setOrder(1);
							newPin.setPublication(Integer.parseInt(pub));
							newPin.setResourceType(200);
	
							JSONObject resultPin = pService.newPin(newPin);
							if (resultPin.getString("status").equals("ok")) {
								LOG.debug("Se agrega pin " + resultPin);
								result += "Se agrega pin " + resultPin;
								result += "<br/>";
							}else {
								LOG.debug("Error al agregar el pin " + resultPin);
								result += "Error al agregar el pin " + resultPin;
								result += "<br/>";
								statusMigrate = false;
							}
						}
					}
					
				} else {
				
					Pin newPin = new Pin();			
					newPin.setResource((String)cmsuser.getAdditionalInfo(aditionalInfoKey));
					newPin.setUser(userID.toString());
					newPin.setOrder(1);
					newPin.setPublication(Integer.parseInt(pub));
					newPin.setResourceType(200);

					JSONObject resultPin = pService.newPin(newPin);
					if (resultPin.getString("status").equals("ok")) {
						LOG.debug("Se agrega pin " + resultPin);
						result += "Se agrega pin " + resultPin;
						result += "<br/>";
					}else {
						LOG.debug("Error al agregar el pin " + resultPin);
						result += "Error al agregar el pin " + resultPin;
						result += "<br/>";
						statusMigrate = false;
					}
				}

				if (statusMigrate) {	
					try {
						if (!connectionIsOpen())
							OpenConnection();
	
						PreparedStatement stmt;
	
						stmt = conn.prepareStatement("delete from CMS_USERDATA WHERE USER_ID = ? and DATA_KEY = ? ");
	
						stmt.setString(1, userID.toString());
						stmt.setString(2, aditionalInfoKey);
	
						stmt.execute();
	
						stmt.close();
						LOG.debug("Eliminamos el registro de la tabla CMS_USERDATA");
						result += "Eliminamos el registro de la tabla CMS_USERDATA";
						result += "<br/>";
					} catch (SQLException e) {
						LOG.debug("Error al eliminar el registro de la tabla CMS_USERDATA" + userID);
						result += "Error al eliminar el registro de la tabla CMS_USERDATA" + userID;
						result += "<br/>";
						throw e;
					} finally {
						if (connectionIsOpenLocaly())
							closeConnection();
					}		 
				}else {
					LOG.debug("Revisar la migracion del usuario " + cmsuser.getName() +" ("+userID+") hubo pines que no se migraron.");
					result += "Revisar la migracion del usuario " + cmsuser.getName() +" ("+userID+") hubo pines que no se migraron.";
					result += "<br/>";

				}
			}
			
		}else {
			LOG.debug("No hay usuarios para migrar");
			result += "No hay usuarios para migrar";
			result += "<br/>";
		}
		
		return result;
	}

	
}
