package com.tfsla.diario.ediciones.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;

import net.sf.json.JSONObject;

public class FilesService extends UploadService {

	private static final Log LOG = CmsLog.getLog(FilesService.class);

	private static Map<String, FilesService> instances = new HashMap<String, FilesService>();

	public static FilesService getInstance(CmsObject cms, String siteName, String publication)
    {
    	
    	String id = siteName + "||" + publication;
    	
    	FilesService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new FilesService(cms,siteName, publication);

	    	instances.put(id, instance);
    	}
    	
    	instance.cmsObject = cms;
    	
        return instance;
    }

	public static FilesService getInstance(CmsObject cms)
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

    
        return getInstance(cms, siteName, publication);
    }
	
	public FilesService(CmsObject cmsObject, String siteName, String publication) {
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
		return "fileUpload";
	}

	@Override
	public JSONObject callbackUpload(JSONObject data) {
		// TODO Falta implementar
		throw new RuntimeException("Metodo no implementado!");
	}

	@Override
	protected void addPreloadParameters(Map<String, String> metadata) {
		// TODO Auto-generated method stub
		
	}
}
