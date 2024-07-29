package com.tfsla.cdnIntegration.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.cdnIntegration.model.InteractionResponse;
import com.tfsla.cdnIntegration.model.PurgePackage;
import com.tfsla.cdnIntegration.service.cdnConnector.Akamai;
import com.tfsla.cdnIntegration.service.cdnConnector.CloudFlare;
import com.tfsla.cdnIntegration.service.cdnConnector.CloudFront;
import com.tfsla.cdnIntegration.service.cdnConnector.I_ContentDeliveryNetwork;
import com.tfsla.cdnIntegration.service.resourceManager.DataBasePurgeQueue;
import com.tfsla.cdnIntegration.service.resourceManager.I_PugeCacheFileQueue;
import com.tfsla.cdnIntegration.service.resourceManager.MemoryPurgeQueue;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.event.I_TfsEventListener;
import com.tfsla.utils.CmsResourceUtils;

public class CdnManager {
	
	private static final Log LOG = CmsLog.getLog(CdnManager.class);
	
	private String module = "contentDeliveryNetwork";
	
	private static Map<String, CdnManager> instances = new HashMap<String, CdnManager>();

	private String siteName = null;
    private String publication = null;
    
	private CdnManager(String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
		
		String cdnName = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "cdnName", "");
		String queueName = CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, module, "queueName", "");
		
		LOG.debug("Inicializando CdnManager para publicacion " + publication + " : " + cdnName + " | " + queueName);
		
		activeCdn = selectCdn(cdnName).create().configure(this.siteName,this.publication);
		activeQueueManager = selectQueueManager(queueName).create().configure(this.siteName,this.publication);
	}

	private I_PugeCacheFileQueue selectQueueManager(String name) {
		I_PugeCacheFileQueue baseQueue=null;
		for (I_PugeCacheFileQueue queue : soppertedQueueAdminitrator) {
			if (name.equals(queue.getName())) {
				baseQueue = queue;
			}
		}
		
		if (baseQueue==null) {
			throw new RuntimeException("Queue manager " + name + " not found");
		}

		return baseQueue;
	}
	
	private I_ContentDeliveryNetwork selectCdn(String name) {
		I_ContentDeliveryNetwork baseCdn=null;
		for (I_ContentDeliveryNetwork cdn : soppertedCdns) {
			if (name.equals(cdn.getName())) {
				baseCdn = cdn;
			}
		}
		
		if (baseCdn==null) {
			throw new RuntimeException("Cdn " + name + " not found");
		}

		return baseCdn;
		
	}
	
    public static CdnManager getInstance(String siteName, String publication) {
    	String id = siteName + "||" + publication;
    	
    	CdnManager instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new CdnManager(siteName, publication);

	    	instances.put(id, instance);
    	}
    		
        return instance;
    }
   

    public static CdnManager getInstance(CmsObject cms)
    {
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return getInstance(siteName, publication);
    }
    
    public static CdnManager getInstance(CmsObject cms,TipoEdicion cPublication)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	int publicationId = cPublication.getId();
    	
    	return getInstance(siteName, "" + publicationId);
    }


    
	private static List<I_ContentDeliveryNetwork> soppertedCdns;
	private static List<I_PugeCacheFileQueue> soppertedQueueAdminitrator;
	
	static {
		soppertedCdns = new ArrayList<I_ContentDeliveryNetwork>();
		soppertedQueueAdminitrator = new ArrayList<I_PugeCacheFileQueue>();
		
		soppertedCdns.add(new CloudFlare());
		soppertedCdns.add(new CloudFront());
		soppertedCdns.add(new Akamai());
		
		soppertedQueueAdminitrator.add(new MemoryPurgeQueue());
		soppertedQueueAdminitrator.add(new DataBasePurgeQueue());
	}
	
	private I_PugeCacheFileQueue activeQueueManager;
	private I_ContentDeliveryNetwork activeCdn;
	
	public void sendPurgePackage(CmsObject cmsObject) {
		
		if (activeQueueManager==null || activeCdn==null)
			return;

		LOG.debug("Creando paquete de purga a Cdn " + activeCdn.getName());
		PurgePackage pac = activeQueueManager.getNextResources(cmsObject, activeCdn.getMaxFilesToInvalidate());

		if (pac==null) {
			LOG.debug("No hay paquetes para purgar ");	
			//se vacian los elementos que deben remover
			try {
				activeQueueManager.removeOldPackages(activeCdn.getMaxPackageSendRetries());
			} catch (Exception ex) {
				LOG.error("CDN - Error al borrar elementos antiguos", ex);
			}
			return;
		}		
		LOG.debug("Obtenidas " + pac.getResources().size() + " noticias a purgar.");
		if (pac.getResources().size()==0) {
			pac.setStatus(PurgePackage.STATUS_ERROR);
			
			Map<String, Object> eventData = new HashMap<String, Object>();
	        eventData.put(I_TfsEventListener.KEY_PURGE_ID, pac.getProcessId());
	        eventData.put(I_TfsEventListener.KEY_ERROR_MESSAGE, "Error - No se pudieron leer las noticias a purgar");
	        eventData.put(I_TfsEventListener.KEY_ERROR_LIST, new ArrayList<String>());
	        
	        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_CDN_PURGE_ERROR, eventData));
	        
	        activeQueueManager.updatePackageStatus(pac);
		} else {
			List<String> cacheFiles = new ArrayList<String>();
			//busco todas las posibles urls
			for (CmsResource resource : pac.getResources()){
				List<String> files = getUrlToPurgeProperty (cmsObject, resource);
				cacheFiles.addAll(files);
			}
			try {
				double executions = 0;
				try {
					executions = cacheFiles.size() / activeCdn.getMaxFilesToInvalidate();
				} catch (Exception ex) {
					LOG.error("No esta definido el parametro MaxFilesToInvalidate");
				}
				int i = 0;
				int timesToExecute = (int)executions;
				for (i = 0; i <= timesToExecute; i++) {
					int maxIndex = activeCdn.getMaxFilesToInvalidate()*(i+1) > cacheFiles.size() ? cacheFiles.size(): activeCdn.getMaxFilesToInvalidate()*(i+1);
					
					InteractionResponse response = activeCdn.invalidateCacheFiles(
							cacheFiles.subList(i* activeCdn.getMaxFilesToInvalidate(), maxIndex ));
					
					
					if (response.isSuccess()) {
						pac.setStatus(PurgePackage.STATUS_DONE);
					} else {
						if (pac.getRetries()<activeCdn.getMaxPackageSendRetries()){
							pac.setRetries(pac.getRetries()+1);
							pac.setStatus(PurgePackage.STATUS_PENDING);
						} else {
							pac.setStatus(PurgePackage.STATUS_ERROR);
							
							Map<String, Object> eventData = new HashMap<String, Object>();
					        eventData.put(I_TfsEventListener.KEY_PURGE_ID, pac.getProcessId());
					        eventData.put(I_TfsEventListener.KEY_ERROR_MESSAGE, response.getResponseMsg());
					        eventData.put(I_TfsEventListener.KEY_ERROR_LIST, response.getErrorList());
					        
					        OpenCms.fireCmsEvent(new CmsEvent(I_TfsEventListener.EVENT_CDN_PURGE_ERROR, eventData));
						}
						//Si falla salgo de la ejecucion
						break;
					}
				}
				
				if (pac.getStatus() == PurgePackage.STATUS_DONE)
					activeQueueManager.removeStatusDone(pac);
				else 
					activeQueueManager.updatePackageStatus(pac);
			} catch (Exception e) {
				CmsLog.getLog(this).error("CDN - Error en el proceso de la purga", e);
			}
		}
		
	}
	
	public void addResource(CmsObject cms, CmsResource resource) {
		if (activeQueueManager==null || activeCdn==null)
			return;

		try {
			if (activeCdn.isActive()) {
				//if (!activeQueueManager.checkResource(cms, resource, siteName, publication)) {
					LOG.debug("Agregando noticia " + resource.getRootPath() + " a purga de cdn " + activeCdn.getName());
					activeQueueManager.addResource(cms, resource);
				//} else 
				//	LOG.debug ("CDN - Ya existe un registro insertado para :" + cms.getSitePath(resource) + " - site:" + siteName + " publication: " + publication  );
				
			}
		} catch (Exception e) {
			LOG.error("Error al insertar registro en la tabla de purga de cdn:", e);
		}
	}
	
	public List<String> getUrlToPurgeProperty (CmsObject cms, CmsResource resource) {
		CmsProperty property;
		List<String> urls = new ArrayList<String>();
		try {
			LOG.debug("leyendo la propiedad public.url.cdn para la noticia:" + resource.getRootPath());
			int i = 1;
			boolean exists = true;
			while (exists) {
				property = cms.readPropertyObject(resource,  "public.url.cdn" + i, false);
				if (property.getValue() != null && !property.getValue().equals("")) {
					urls.add( property.getValue());
					i++;
				} else 
					exists = false;
			}
			if (urls.size() >0 )
				return urls;
		} catch (Exception e) {
			LOG.error("error leyendo la propiedad public.url.cdn para la noticia:" + resource.getRootPath(), e);
		}
		urls.add(activeCdn.getCachedName(cms, resource));
		return urls;	
	}
	
	public void updateUrlToPurgeProperty (CmsObject cms, CmsResource resource, int index) {
		try{
			CmsProperty lastProperty = cms.readPropertyObject(resource, "public.url.cdn" + index, false);
			  if (lastProperty.getValue() == null ||  !lastProperty.getValue().equals(activeCdn.getCachedName(cms, resource))) {
		  		index = lastProperty.getValue() == null ? 1 : index+1;
				CmsResourceUtils.forceLockResource(cms, CmsResourceUtils.getLink(resource));
				CmsProperty prop = new CmsProperty("public.url.cdn" + index, null ,activeCdn.getCachedName(cms, resource) ,true);
				cms.writePropertyObject(CmsResourceUtils.getLink(resource), prop);
				
				CmsResourceUtils.unlockResource(cms,CmsResourceUtils.getLink(resource), false);
				
			  }
		} catch(Exception e){
			LOG.error("error al acutalizar property public.url.cdn para la noticia: " + CmsResourceUtils.getLink(resource), e);
			 CmsResourceUtils.unlockResource(cms,CmsResourceUtils.getLink(resource), false);
		}
	
	}
	
	
	public void sendPurgeFile(CmsObject cmsObject, CmsResource resource) {
		if (activeQueueManager==null || activeCdn==null)
			return;

		activeCdn.invalidateCacheFile(activeCdn.getCachedName(cmsObject, resource));
	}
	
	public InteractionResponse test() {
		if (activeQueueManager==null || activeCdn==null)
			return null;

		try {
			return activeCdn.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public InteractionResponse getInvalidationStatus(String invalidationId) throws Exception {
		if (activeQueueManager==null || activeCdn==null)
			return null;

		try {
			return activeCdn.getInvalidationStatus(invalidationId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
