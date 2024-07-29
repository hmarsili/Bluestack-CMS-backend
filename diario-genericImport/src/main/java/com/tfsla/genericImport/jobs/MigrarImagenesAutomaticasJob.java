package com.tfsla.genericImport.jobs;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.genericImport.model.A_ImportService;
import com.tfsla.genericImport.service.ImportImagesService;

public class MigrarImagenesAutomaticasJob implements I_CmsScheduledJob{
	private String resultados = "";
	private boolean doit = true;

	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		int daysBefore=0;
		int daysAfter=0;	
		
		String _importContent = (String)parameters.get("ImportUrl");
		
		A_ImportService iService = null;
		iService = new ImportImagesService(cms, _importContent);
		ImportImagesService imgService = (ImportImagesService)iService;

		iService.createLog();
		new Thread(iService).start();
		resultados += "Lanzando importacion de noticias automaticas: " + _importContent;	
		
		return resultados;

	}	
}
