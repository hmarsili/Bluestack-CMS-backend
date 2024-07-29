package com.tfsla.diario.ediciones.jobs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.workplace.CmsWorkplace;


public class PublicacionControladaJob implements I_CmsScheduledJob {

	private static final Log LOG = CmsLog.getLog(PublicacionControladaJob.class);

	
	protected String resultados = "";
	
	private String size = "";
	private String queueSizeLimit = "";
	private String resourcesOnQueueLimit = "";
	
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		resultados = "Ejecutando publicacion automatizada de recursos: ";
		
		LOG.info("Ejecutando publicacion automatizada de recursos: ");
		
		
		String property = (String) parameters.get("property");
		String propertyValue = (String) parameters.get("propertyValue");
		
		String folders = (String) parameters.get("folders");
		
		String foldersDateFormated = (String) parameters.get("folders-formated");
		String _daysBack = (String) parameters.get("day-back");
		int daysBack = 0;
		if (_daysBack!=null) {
			daysBack = Integer.parseInt(_daysBack);
		}
		
		String publishRelated = (String) parameters.get("publishRelated");
		String forcePublish = (String) parameters.get("forcePublish");
		
		getPublishConfiguration(parameters);
		
		
		if ((folders==null || folders.trim().equals("")) && (foldersDateFormated==null || foldersDateFormated.trim().equals(""))) {
			resultados += "!!! Error al ejecutar - FALTA PARAMETRO folders o folders-formated";
			LOG.info("Se detiene el proceso. Parametros 'folders' o 'folders-formated' ausentes.");
			return resultados;
		}
		
		if (size==null  || size.trim().equals("")) {
			resultados += "!!! Error al ejecutar - FALTA PARAMETRO size ";
			LOG.info("Se detiene el proceso. Parametro 'size' ausente.");
			return resultados;
		}
		
		int iSize=0;
		try {
			iSize = Integer.parseInt(size);
		}
		catch (NumberFormatException e) {
			resultados += "!!! Error al ejecutar - PARAMETRO size NO ES UN NUMERO";
			LOG.info("Se detiene el proceso. Parametro 'size' no es numerico.");
			return resultados;
			
		}
		
		
		int iQueueSizeLimit=10;
		if (queueSizeLimit!=null && !queueSizeLimit.trim().equals("")) {
			try {
				iQueueSizeLimit = Integer.parseInt(queueSizeLimit);
			}
			catch (NumberFormatException e) {
				resultados += "!!! Error al ejecutar - PARAMETRO queueSizeLimit NO ES UN NUMERO";
				LOG.info("Se detiene el proceso. Parametro 'queueSizeLimit' no es numerico.");
				return resultados;
				
			}
		}
		
		int iResourcesOnQueueLimit=200;
		if (resourcesOnQueueLimit!=null && !resourcesOnQueueLimit.trim().equals("")) {
			try {
				iResourcesOnQueueLimit = Integer.parseInt(resourcesOnQueueLimit);
			}
			catch (NumberFormatException e) {
				resultados += "!!! Error al ejecutar - PARAMETRO resourcesOnQueueLimit NO ES UN NUMERO";
				LOG.info("Se detiene el proceso. Parametro 'resourcesOnQueueLimit' no es numerico.");
				return resultados;
				
			}
		}
		
		String propertyDef=null;
		if (property!=null) {
			CmsPropertyDefinition prop = cms.readPropertyDefinition(property);
			if (prop==null) {
				resultados += "!!! Error al ejecutar - PARAMETRO property NO ES UN PROPIEDAD VALIDA";
				LOG.info("Se detiene el proceso. Parametro 'property' no corresponde a una propiedad existente.");
				return resultados;
			}
			propertyDef=prop.getId().getStringValue();
		}
		
		
		
		boolean related = (publishRelated!=null && publishRelated.trim().toLowerCase().equals("true"));
			
		
		//Me fijo que no haya muchos trabajos encolados en publicacion. Para evitar saturar.
		int currentJobs = getNumberOfJobInQueue();
		if (iQueueSizeLimit < currentJobs) {
			resultados += "(!) salteando ejecucion. Existen " + currentJobs + " jobs en la cola de publicacion";
			LOG.info("Se evita ejecucion del proceso. Existen " + currentJobs + " jobs en la cola de publicacion");
			return resultados;	
		}

		int currentResources = getNumberOfResourcesInQueue();
		if (iResourcesOnQueueLimit < currentResources) {
			resultados += "(!) salteando ejecucion. Existen " + currentResources + "recursos pendientes en la cola de publicacion";
			LOG.info("Se evita ejecucion del proceso. Existen " + currentResources + " recursos pendientes en la cola de publicacion");
			return resultados;	
		}
		
		
		CmsPublishList list = null;
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		
		String[] _folders = foldersDateFormated!=null ? foldersDateFormated.split(";") : folders.split(";");
		
		boolean resourceLeft = true;
		int nroFolder=0;
		int offset=0;
		String folder;
		while (resourceLeft && iSize >0) {
			List resources = null;
			folder = _folders[nroFolder];
			if (foldersDateFormated!=null)
				folder = getFolderFormatted(folder, daysBack);
			
			LOG.debug("Buscando " + iSize + " recursos en la carpeta " + folder + "( offset: " + offset + ")");
			
			
			CmsResource path = cms.readResource(folder);
			CmsLock lock = cms.getLock(path);
			LOG.debug("path " + path.getRootPath() + " - state: " + path.getState());
			if (!lock.getSystemLock().isPublish() && !path.getState().isUnchanged() && lock.isLockableBy(cms.getRequestContext().currentUser()))
				publishList.add(path);
			while ((resources==null || resources.size()==0) && resourceLeft) { 
				folder = _folders[nroFolder];
				if (foldersDateFormated!=null)
					folder = getFolderFormatted(folder, daysBack);
				
				if (propertyDef==null) {
					resources = cms.readResources(folder, CmsResourceFilter.ALL_MODIFIED, true, iSize, offset);
				}
				else {
					resources = cms.readResourcesWithProperty(folder, property, propertyValue, CmsResourceFilter.ALL_MODIFIED, iSize, offset);
				}
				
				if (LOG.isDebugEnabled()) {
					int i =1;
					for (CmsResource res : (List<CmsResource>)resources) {
						LOG.debug("Recurso obtenido " + i + ": " + res.getRootPath());
						i++;
					}
				}
				if (resources.size()==0) {
					nroFolder++;
					offset = 0;
					if (nroFolder==_folders.length)
						resourceLeft = false;
					else {
						folder = _folders[nroFolder];
						if (foldersDateFormated!=null)
							folder = getFolderFormatted(folder, daysBack);
						LOG.debug("No se encontraron recursos para publicar. Pasando a carpeta " + folder + "( offset: " + offset + ")");
						path = cms.readResource(folder);
						lock = cms.getLock(path);
						if (!lock.getSystemLock().isPublish() && !path.getState().isUnchanged() && lock.isLockableBy(cms.getRequestContext().currentUser()))
							publishList.add(path);
					}
				}
			}
			
			publishList.addAll(resources);
			
			LOG.debug("Se encontraron " + publishList.size() + " recursos en la carpeta " + folder + "( offset: " + offset + ")");
			
			
			
			//Agrego carpetas padres si es necesario.
			List parentFolders = new ArrayList();
			for (Iterator<CmsResource> iterator = publishList.iterator(); iterator.hasNext();) {
				CmsResource resource = iterator.next();
				CmsResource parentFolder = cms.readResource(CmsResource.getParentFolder(cms.getSitePath(resource)));
				CmsLock lockRes = cms.getLock(parentFolder);
				while (!lockRes.getSystemLock().isPublish() && !parentFolder.getState().isUnchanged() && lock.isLockableBy(cms.getRequestContext().currentUser())) {
					parentFolders.add(parentFolder);
					
					parentFolder = cms.readResource(CmsResource.getParentFolder(cms.getSitePath(parentFolder)));
					lockRes = cms.getLock(parentFolder);
				} 
			}
			
			publishList.addAll(parentFolders);
			
			//verifico que no se publiquen recursos temporales
			boolean unlock = (forcePublish!=null && forcePublish.trim().toLowerCase().equals("true"));
			for (Iterator<CmsResource> iterator = publishList.iterator(); iterator.hasNext();) {
				CmsResource resource = iterator.next();
			    LOG.debug("revisando recurso " + resource.getRootPath());

				if (CmsWorkplace.isTemporaryFile(resource)) {
					LOG.debug("Removiendo de lista de publicacion " + resource.getRootPath() + " por ser temporal.");
			        iterator.remove();
			    }
			    //Verifico si hay que desbloquearlo para publicar
			    else  {
			    	lock = cms.getLock(resource);
					if (!lock.isUnlocked()) {
						if (lock.getSystemLock().isPublish()) {
							LOG.debug("Removiendo de lista de publicacion " + resource.getRootPath() + " por estar ya en cola de publicacion.");
							iterator.remove();
							continue;
						}
						else if (!lock.getUserId().equals(cms.getRequestContext().currentUser().getId()))
							if (unlock)
								cms.changeLock(cms.getSitePath(resource));
							else {
								LOG.debug("Removiendo de lista de publicacion " + resource.getRootPath() + " por estar lockeado por el usuario " + lock.getUserId());
								iterator.remove();
								continue;
							}
						cms.unlockResource(cms.getSitePath(resource));
					}
					else if (lock.getSystemLock().isPublish()) {
						LOG.debug("Removiendo de lista de publicacion " + resource.getRootPath() + " por estar ya en cola de publicacion.");
						iterator.remove();
						continue;
					}
			    }
			}
			
			CmsPublishList innerList = OpenCms.getPublishManager().getPublishList(cms, publishList, true,false);

			offset+=iSize;
			iSize-=innerList.getAllResources().size();
			LOG.debug("Se agregaron "+ innerList.getAllResources().size() + " ( DirectPublishResources:" + innerList.getDirectPublishResources().size() + ") " + " recursos a publicar.");
			if (list!=null)
				list = OpenCms.getPublishManager().mergePublishLists(cms, list, innerList);
			else
				list = innerList;
		}
	
		if (list!=null && (list.getDirectPublishResources().size()>0 || list.size()>0)) {
			resultados += "\nSe encontraron " + list.size() + " ( directPublish: " + list.getDirectPublishResources().size() + ") recursos a publicar.";
			LOG.info("Se encontraron " + list.size() + " ( directPublish: " + list.getDirectPublishResources().size() + ") recursos a publicar.");
			if (related) {	
				
				CmsPublishList relatedList = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, list);
				resultados += "\nSe encontraron " + relatedList.size() + " recursos relacionados a publicar.";				
				LOG.info("Agregando contenido relacionado de las noticias: " + relatedList.size() + " recursos.");
				list = OpenCms.getPublishManager().mergePublishLists(cms, list, relatedList);
			}

			
			OpenCms.getPublishManager().publishProject(cms,
				new CmsLogReport(Locale.getDefault(), this.getClass()),
				list
			);
		}
		else {
			resultados += "\nNo se encontraron recursos a publicar.";
			LOG.info("No se encontraron recursos a publicar.");
		}
		return resultados;
	}


	private void getPublishConfiguration(Map parameters) {
		String normalSize = (String) parameters.get("size");
		String normalQueueSizeLimit = (String) parameters.get("queueSizeLimit");
		String normalResourcesOnQueueLimit = (String) parameters.get("resourcesOnQueueLimit");

		String intensivePeriod = (String) parameters.get("intensivePeriod");
		String intensiveSize = (String) parameters.get("intensiveSize");
		String intensiveQueueSizeLimit = (String) parameters.get("intensiveQueueSizeLimit");
		String intensiveResourcesOnQueueLimit = (String) parameters.get("intensiveResourcesOnQueueLimit");
		
		
		
		if (insideIntensivePeriod(intensivePeriod)) {
			LOG.debug("Ejecucion intensiva | size:" + intensiveSize + " - queueSizeLimit: " + intensiveQueueSizeLimit + " - resourcesOnQueueLimit: " + intensiveResourcesOnQueueLimit);
			
			size = intensiveSize;
			queueSizeLimit =intensiveQueueSizeLimit;
			resourcesOnQueueLimit = intensiveResourcesOnQueueLimit;
		}
		else {
			LOG.debug("Ejecucion normal | size:" + normalSize + " - queueSizeLimit: " + normalQueueSizeLimit + " - resourcesOnQueueLimit: " + normalResourcesOnQueueLimit);

			size = normalSize;
			queueSizeLimit = normalQueueSizeLimit;
			resourcesOnQueueLimit = normalResourcesOnQueueLimit;
			
		}
	}
	
	
	private int getNumberOfJobInQueue() {
		int count=0;
		if (OpenCms.getPublishManager().isRunning()) {
			count++;
		}
		count+=OpenCms.getPublishManager().getPublishQueue().size();
		
		return count;
			
	}
	
	private int getNumberOfResourcesInQueue() {
		int number = 0;
		if (OpenCms.getPublishManager().isRunning()) {
			number +=OpenCms.getPublishManager().getCurrentPublishJob().getPublishList().size();
		}
		for (CmsPublishJobEnqueued job : (List<CmsPublishJobEnqueued>) OpenCms.getPublishManager().getPublishQueue()) {
			number+=job.getPublishList().size();
		}
		return number;
	}
	
	private boolean insideIntensivePeriod(String period) {
		if (period==null)
			return false;
		
		String[] periods = period.split("-");
		if (periods.length!=2)
			return false;
		
		String start = periods[0].trim();
		String end = periods[1].trim();
		
		String now = getCurrentHour();

		LOG.debug("Verificando si la ejecucion corresponde a periodo de proceso intensivo (" + start + " - " + end + "): " + now);
		if(start.compareTo(end) <= 0)
			return ((now.compareTo(start) >= 0) && (now.compareTo(end) <= 0));
		else
			return ((now.compareTo(start) >= 0) || (now.compareTo(end) <= 0));
		
	}

	private static String  HOUR_FORMAT = "HH:mm";
	
	public static String getCurrentHour() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdfHour = new SimpleDateFormat(HOUR_FORMAT);
        String hour = sdfHour.format(cal.getTime());
        return hour;
    }

	public static String getFolderFormatted(String folderFormat, int daysBack) {
		String folder=null;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1 * daysBack);
		SimpleDateFormat sdf = new SimpleDateFormat(folderFormat);
		folder=sdf.format(cal.getTime());
		return folder;
	}
	
	public static void main(String[] args) {
		System.out.println(PublicacionControladaJob.getFolderFormatted("'/contenidos/'yyyy/MM/dd/", 0));
		System.out.println(PublicacionControladaJob.getFolderFormatted("'/contenidos/'yyyy/MM/dd/", 1));

	}
}
