package com.tfsla.diario.admin.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class MigrarNoticiasCablerasJob {
	private String resultados = "";
	private boolean doit = true;

	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "";
		int daysBefore=0;
		int daysAfter=0;

		String _daysBefore = (String)parameters.get("daysBefore");
		String _daysAfter = (String)parameters.get("daysAfter");
		String _publication = (String)parameters.get("publication");
		String _newsType = (String)parameters.get("newstype");

		String _test = (String)parameters.get("test");
		doit = (_test==null || !_test.toLowerCase().trim().equals("true"));

		TipoEdicionService tService = new TipoEdicionService();

		if (_daysBefore!=null)
			daysBefore = Integer.parseInt(_daysBefore);
		
		if (_daysAfter!=null)
			daysAfter = Integer.parseInt(_daysAfter);

		Date now = new Date();
		
		String proyecto = OpenCmsBaseService.getCurrentSite(cms);
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(_publication,proyecto);
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

		NoticiasService nService = new NoticiasService();		
		
		resultados += "\n";
		if (!doit)
			resultados += "TEST MODE\n";

		resultados += "Lanzando eliminacion de noticias de tipo " + (_newsType !=null ? _newsType : "noticia") + " vacias entre los ultimos " + daysAfter + " y " + daysBefore + " dias\n";
		
		//A_ImportService iService = null;
		//iService = new ImportAgencyReutersNewsService(cms, "");
		//ImportAgencyReutersNewsService xmlService = (ImportAgencyReutersNewsService)iService;

		

		return resultados;

	}

	public List<CmsFile> listEmptyFilesRecursive(CmsObject cms, String path, List<CmsFile> files) throws CmsException 
	{

		if  (files==null)
			files = new ArrayList<CmsFile>();
					
		List<CmsFolder> listFolders = cms.getSubFolders(path); 


		for(CmsFolder subfolder : listFolders)
		{
			listEmptyFilesRecursive(cms,cms.getSitePath(subfolder),files);
		}
		
		List<CmsFile> listFiles = cms.getFilesInFolder(path);
		for (CmsFile file : listFiles)
		{
			//CmsProperty title = cms.readPropertyObject(file,"Title",false);
	                //if (title.getValue()==null || title.getValue().trim().length()==0) {	                	
	         files.add(file);
	                //}

		}
		return files;
	}
	
	public List<CmsFile> borrarNoticiasVacias(CmsObject cmsObject, String pathFolder) throws CmsException {
		List<CmsFile> files = listEmptyFilesRecursive(cmsObject, pathFolder, new ArrayList<CmsFile>());
		List<CmsFile> resourcesToPublish = new ArrayList<CmsFile>();
		
		for (CmsFile file : files) {
			try {
				resultados += "Eliminando noticia " + cmsObject.getSitePath(file) + ".\n";

				CmsResourceState estado = file.getState();

				if (doit && !estado.isDeleted()) {
					
					CmsLock lock = cmsObject.getLock(file);
					if (!lock.isUnlocked() && !lock.getUserId().equals(cmsObject.getRequestContext().currentUser().getId()))
						cmsObject.changeLock(cmsObject.getSitePath(file));
					else
						cmsObject.lockResource(cmsObject.getSitePath(file));

					cmsObject.deleteResource(cmsObject.getSitePath(file), CmsResource.DELETE_REMOVE_SIBLINGS);
				}
				
				if (!estado.isNew()) {
					cmsObject.unlockResource(cmsObject.getSitePath(file));
					resourcesToPublish.add(cmsObject.readFile(file));
				}

				
			} catch (CmsException e) {
				// TODO Auto-generated catch block
				System.out.println("error en noticia " + cmsObject.getSitePath(file));
				e.printStackTrace();
			}
		}
		return resourcesToPublish;
	}
	
	public void despublicarNoticiasBorradas(CmsObject cmsObject, List<CmsFile> resourcesToPublish) throws CmsException {
		resultados += "Despublicando las noticias eliminados.\n";
		
		if (doit)
			OpenCms.getPublishManager().publishProject(cmsObject,
				new CmsLogReport(Locale.getDefault(), this.getClass()),
				OpenCms.getPublishManager().getPublishList(cmsObject,resourcesToPublish, false));
	}
}
