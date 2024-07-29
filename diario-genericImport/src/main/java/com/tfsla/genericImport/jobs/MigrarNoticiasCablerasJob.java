package com.tfsla.genericImport.jobs;

import java.util.Map;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.genericImport.model.A_ImportServiceWires;
import com.tfsla.genericImport.service.ImportAgencyAFPNewsService;
import com.tfsla.genericImport.service.ImportAgencyAssociatedPressNewsService;
import com.tfsla.genericImport.service.ImportAgencyEfeNewsService;
import com.tfsla.genericImport.service.ImportAgencyEuropaPressNewsService;
import com.tfsla.genericImport.service.ImportAgencyNotimexNewsService;
import com.tfsla.genericImport.service.ImportAgencyReutersNewsService;

public class MigrarNoticiasCablerasJob implements I_CmsScheduledJob{
	private String resultados = "";
	private boolean isActiveReuters = false;
	private boolean isActiveAFP = false;
	private boolean isActiveAP = false;
	private boolean isAtiveNotimex = false;
	private boolean isActiveEfe = false;
	private boolean isActiveEuropaPress = false;

	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		String rootSite = cms.getRequestContext().getSiteRoot();
	
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		String importActiveAgencies = config.getParam("", "", "importAgency", "importActiveAgencies","");
		
		if(importActiveAgencies ==null || importActiveAgencies.equals(""))
			return "No hay agencias activas";
		else{
			
			String[] agencies = importActiveAgencies.split(",");
			
			for (int i=0; i < agencies.length; i++) {
	            if(agencies[i].toLowerCase().equals("reuters"))
					isActiveReuters = true;
				if(agencies[i].toLowerCase().equals("afp"))
					isActiveAFP = true;
				if(agencies[i].toLowerCase().equals("ap"))
					isActiveAP = true;
				if(agencies[i].toLowerCase().equals("notimex"))
					isAtiveNotimex = true;
				if(agencies[i].toLowerCase().equals("efe"))
					isActiveEfe = true;
				if(agencies[i].toLowerCase().equals("europapress"))
					isActiveEuropaPress = true;
	        }
			
			
		}
		
		A_ImportServiceWires iService = null;
		
		if(isActiveReuters){
			iService = new ImportAgencyReutersNewsService(cms, "");
			ImportAgencyReutersNewsService xmlService = (ImportAgencyReutersNewsService)iService;
	
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias reuters";
		}
		
		if(isActiveAFP){
			iService = new ImportAgencyAFPNewsService(cms, "");
			ImportAgencyAFPNewsService xmlAFPService = (ImportAgencyAFPNewsService)iService;
	
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias AFP";
		}
		
		if(isAtiveNotimex)
		{
			iService = new ImportAgencyNotimexNewsService(cms, "");
			ImportAgencyNotimexNewsService xmlNotimexService = (ImportAgencyNotimexNewsService)iService;
	
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias Notimex";
		}
		
		if(isActiveAP){
			iService = new ImportAgencyAssociatedPressNewsService(cms, "");
			ImportAgencyAssociatedPressNewsService xmlAPService = (ImportAgencyAssociatedPressNewsService)iService;
	
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias AssociatedPress";
		}
		
		if(isActiveEfe)
		{
			iService = new ImportAgencyEfeNewsService(cms,rootSite ,"");
			
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias Efe";
		}
		
		if(isActiveEuropaPress)
		{
			iService = new ImportAgencyEuropaPressNewsService(cms,rootSite,"");
			
			iService.createLog();
			new Thread(iService).start();
			resultados += "Lanzando importacion de noticias EuropaPress";
		}
		
		return resultados;

	}	
}
