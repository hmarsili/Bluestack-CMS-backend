package com.tfsla.diario.ediciones.services;

import java.util.HashMap;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;

public class AudioService extends UploadService {

	int folderType = -1;

	private static Map<String, AudioService> instances = new HashMap<String, AudioService>();


	public static AudioService getInstance(CmsObject cms)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			LOG.error(e);
		}

    	String id = siteName + "||" + publication;
    	
    	AudioService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new AudioService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
        return instance;
    }
	
	public AudioService(CmsObject cmsObject, String siteName, String publication) {
		this.loadProperties(siteName,publication);
	}

	public void loadProperties(String siteName, String publication) {
    	loadBaseProperties(siteName, publication);
	}
	
	@Override
	protected int getVfsFolderType() {
		try {
			return OpenCms.getResourceManager().getResourceType("linkgallery").getTypeId();
		} catch (CmsLoaderException e) {
			LOG.error(e);
			return CmsResourceTypeFolder.getStaticTypeId();
		}
	}

	@Override
	protected String getModuleName() {
		return "audioUpload";
	}
}
