package com.tfsla.genericImport.jobs;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.genericImport.model.A_ImportService;
import com.tfsla.genericImport.service.ImportTXTService;

public class MigrarNoticiasAutomaticasJob implements I_CmsScheduledJob{
	private String resultados = "";

	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		String _importContent = (String)parameters.get("ImportUrl");
		String _publishFolders = (String)parameters.get("publishFolders");
		
		if (_publishFolders==null)
			_publishFolders = "false";
		
		boolean publishFolders = Boolean.parseBoolean(_publishFolders);
		

		ImportTXTService iService = new ImportTXTService(cms, _importContent);
		iService.setPublishFolders(publishFolders);
		
		iService.createLog();
		new Thread(iService).start();
		resultados += "Lanzando importacion de noticias automaticas: " + _importContent;	
		
		return resultados;

	}	
}
