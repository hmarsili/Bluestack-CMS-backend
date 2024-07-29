package com.tfsla.diario.admin.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.utils.CmsResourceUtils;

public class BorrarPlaylistTemporalesJob  implements I_CmsScheduledJob {

		private String resultados = "";
		private boolean doit = true;

		public String launch(CmsObject cms, Map parameters) throws Exception {
			resultados = "";
			int daysBefore=0;
			int daysAfter=0;
			
			String _daysBefore = (String)parameters.get("daysBefore");
			String _daysAfter = (String)parameters.get("daysAfter");
			String _publication = (String)parameters.get("publication");
			
			String _test = (String)parameters.get("test");
			doit = (_test==null || !_test.toLowerCase().trim().equals("true"));

			if (_daysBefore!=null)
				daysBefore = Integer.parseInt(_daysBefore);

			if (_daysAfter!=null)
				daysAfter = Integer.parseInt(_daysAfter);

			Date now = new Date();
			

			resultados += "\n";
			if (!doit)
				resultados += "TEST MODE\n";
			
			resultados += "Lanzando eliminacion de playlist  temporales entre los ultimos " + daysAfter + " y " + daysBefore + " dias\n";
			
			SimpleDateFormat folderDateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Calendar cal = new GregorianCalendar();
			cal.setTime(now);
			List<CmsFile> resuourcesToPublish = new ArrayList<CmsFile>();

			cal.add(Calendar.DAY_OF_MONTH, -1 * daysAfter);

			String proyecto = OpenCmsBaseService.getCurrentSite(cms);
			TipoEdicionService tService = new TipoEdicionService();
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(_publication,proyecto);
			
			for (int j=daysAfter;j<=daysBefore;j++) {
				
				String eventFolder = "";
				
				String baseUrl = cms.getRequestContext().removeSiteRoot(tEdicion.getBaseURL());
				if (baseUrl.contains("contenidos/"))
					baseUrl = baseUrl.replace("contenidos/", "");	
				baseUrl += "playlist/";
					
				eventFolder = baseUrl + folderDateFormat.format(cal.getTime()) + "/";
				
				resultados += "revisando carpeta " + eventFolder + ".\n";
				
				if (cms.existsResource(eventFolder))
					resuourcesToPublish.addAll(borrarPlaylistTemporales(cms,eventFolder));

				cal.add(Calendar.DAY_OF_MONTH, -1);
				
			}
			
			if (resuourcesToPublish.size()>0)
				despublicarPlaylistBorradas(cms,resuourcesToPublish);

			return resultados;

		}

		public List<CmsFile> listTemporalFilesRecursive(CmsObject cms, String path, List<CmsFile> files) throws CmsException {

			if  (files==null)
				files = new ArrayList<CmsFile>();
						
			List<CmsFolder> listFolders = cms.getSubFolders(path); 


			for(CmsFolder subfolder : listFolders) {
				listTemporalFilesRecursive(cms,cms.getSitePath(subfolder),files);
			}
			
			List<CmsFile> listFiles = cms.getFilesInFolder(path);
			for (CmsFile file : listFiles) {
				if( file.getName().indexOf("~") > -1){
					files.add(file);
		        }
			}
			return files;
		}
		
		public List<CmsFile> borrarPlaylistTemporales(CmsObject cmsObject, String pathFolder) throws CmsException {
			List<CmsFile> files = listTemporalFilesRecursive(cmsObject, pathFolder, new ArrayList<CmsFile>());
			List<CmsFile> resourcesToPublish = new ArrayList<CmsFile>();
			
			for (CmsFile file : files) {
				try {
					resultados += "Eliminando playlist " + cmsObject.getSitePath(file) + ".\n";

					CmsResourceState estado = file.getState();

					if (doit && !estado.isDeleted()) {
						CmsLock lock = cmsObject.getLock(file);
						if (!lock.isUnlocked() && !lock.getUserId().equals(cmsObject.getRequestContext().currentUser().getId()))
							cmsObject.changeLock(cmsObject.getSitePath(file));
						else
							cmsObject.lockResource(cmsObject.getSitePath(file));
						
						CmsResourceUtils.createPropertyInShadowMode(cmsObject, file,
			    				"disardChanges", "true");
						cmsObject.deleteResource(cmsObject.getSitePath(file), CmsResource.DELETE_REMOVE_SIBLINGS);
					}

					if (!estado.isNew()) {
						cmsObject.unlockResource(cmsObject.getSitePath(file));
						resourcesToPublish.add(cmsObject.readFile(file));
					}
					
				} catch (CmsException e) {
					// TODO Auto-generated catch block
					System.out.println("error en playlist " + cmsObject.getSitePath(file));
					e.printStackTrace();
				}
			}
			return resourcesToPublish;
		}
		
		public void despublicarPlaylistBorradas(CmsObject cmsObject, List<CmsFile> resourcesToPublish) throws CmsException {
			resultados += "Despublicando las playlist eliminadas.\n";
			

			if (doit) {
					//OpenCms.getPublishManager().publishProject(cmsObject,
					//	new CmsLogReport(Locale.getDefault(), this.getClass()),
					//	OpenCms.getPublishManager().getPublishList(cmsObject,resourcesToPublish, false));
				
					for (CmsFile cmsFile : resourcesToPublish) {
						try {
							CmsLock lock = cmsObject.getLock(cmsFile);
							if (!lock.isUnlocked() && !lock.getUserId().equals(cmsObject.getRequestContext().currentUser().getId()))
								cmsObject.changeLock(cmsObject.getSitePath(cmsFile));
							else
								cmsObject.lockResource(cmsObject.getSitePath(cmsFile));

							OpenCms.getPublishManager().publishResource(cmsObject, cmsObject.getSitePath(cmsFile));
						} catch (Exception e) {
							
						}
					}
			}
		}

}


