package com.tfsla.cmsMedios.releaseManager.installer.service.deploySteps;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.tfsla.cmsMedios.releaseManager.installer.data.GenericDAO;
import com.tfsla.cmsMedios.releaseManager.installer.data.SQLParameter;
import com.tfsla.cmsMedios.releaseManager.installer.data.SQLParameterType;
import com.tfsla.cmsMedios.releaseManager.installer.service.SetupProgressService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SQLScriptsDeployStep extends DeployStepContext {

	@Override
	public void deploy() throws Exception {
		//RUN SQL SCRIPTS
		SetupProgressService.reportProgress("Running SQL scripts...");
		if(manifest.containsKey("scripts")) {
			JSONObject parameters = null;
			GenericDAO dao = new GenericDAO();
			if(manifest.containsKey("scripts-parameters")) {
				parameters = manifest.getJSONObject("scripts-parameters");
			}
			
			dao.openConnection();
			for(Object item : manifest.getJSONArray("scripts")) {
				ArrayList<SQLParameter> sqlParameters = new ArrayList<SQLParameter>();
				if(parameters != null && parameters.containsKey(item.toString()) && parameters.getJSONObject(item.toString()).containsKey("parameters")) {
					JSONArray jsonParameters = parameters.getJSONObject(item.toString()).getJSONArray("parameters");
					for(Object jsonParameter : jsonParameters.toArray()) {
						JSONObject jParameter = (JSONObject)jsonParameter;
						SQLParameter parameter = new SQLParameter();
						parameter.setIndex(jParameter.getInt("index"));
						String parameterValue = request.getParameter(item.toString().replace(".sql", "") + parameter.getIndex());
						SetupProgressService.reportProgress("Processing parameter " + item.toString().replace(".sql", "") + parameter.getIndex() + " - value: " + parameterValue);
						if(parameterValue == null || parameterValue.equals("")) {
							parameter.setValue(jParameter.getString("default-value"));
						} else {
							parameter.setValue(parameterValue);
						}
						
						parameter.setParameterType(SQLParameterType.valueOf(jParameter.getString("type")));
						if(jParameter.containsKey("format")) {
							parameter.setFormat(jParameter.getString("format"));
						}
						sqlParameters.add(parameter);
					}
				}
				
				String sqlString = FileUtils.readFileToString(new File(releasePath + "scripts/" + item.toString()));
				SetupProgressService.reportProgress("Executing " + item.toString() + " (" + sqlParameters.size() + ") parameters");
				int result = 0;
				try {
					if (sqlString.contains(";")) {
						String[] queries = sqlString.split(";");
						for (String query : queries) {
							result = dao.runSQL(query, sqlParameters);
							SetupProgressService.reportProgress(result + " rows updated");
						}
					} else {
						result = dao.runSQL(sqlString, sqlParameters);
						SetupProgressService.reportProgress(result + " rows updated");
					}
					
					SetupProgressService.reportProgress(result + " rows updated");
				} catch (Exception e) {
					SetupProgressService.warning("Cannot run SQL query: " + sqlString);
					SetupProgressService.warning(e.getMessage());
				}
				Thread.sleep(1000);
			}
			
			partialMessage = "SQL scripts execution finished";
		} else {
			partialMessage = "There are no SQL scripts to execute";
		}
	}

	@Override
	public String getPartialMessage() {
		return partialMessage;
	}

	@Override
	public String getStepName() {
		return "Ejecutar scripts de base de datos";
	}
	
	protected String partialMessage;

}
