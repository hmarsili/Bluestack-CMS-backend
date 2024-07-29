package com.tfsla.diario.webservices.common;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.NoticiasService;
import com.tfsla.diario.ediciones.services.PublicationService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;

public class ContentsHelper {
	
	public static void createFolder(CmsObject cms, String uploadPath) throws CmsException {
		if (!cms.existsResource(uploadPath)) {
			cms.createResource(uploadPath, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
			cms.unlockResource(uploadPath);
		}
	}
	
	public static String getNextResourceName(CmsObject cms, String vfsPath, String preffixName) throws CmsException {
		@SuppressWarnings("unchecked")
		List<CmsResource> cmsFiles = cms.getResourcesInFolder(vfsPath, CmsResourceFilter.ALL);
		String fileName = "";
		int maxNewsValue = 0;
		for (CmsResource resource : cmsFiles) {
			fileName = resource.getName();
			if (fileName.matches(".*" + preffixName + "_[0-9]{4}.html")) {
				String auxFileName = fileName.substring(fileName.indexOf(preffixName + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(preffixName + "_","").replace(".html",""));
				if (maxNewsValue < newsValue)
					maxNewsValue = newsValue;
			}
		}
		DecimalFormat df = new DecimalFormat("0000");
		if (!vfsPath.endsWith("/"))
			vfsPath += "/";
		fileName = vfsPath + preffixName + "_" + df.format(maxNewsValue+1) + ".html";
		return fileName;
	}
	
	public static CmsFile createResource(CmsObject cmsObject, String tipo, String publication, Date date) throws Exception {
		NoticiasService nService = new NoticiasService();
		String fileName = "";
		
		String tipoNews = (tipo == null || tipo.equals("") ? "news" : tipo);
		int publicationNews = (publication == null || publication.equals("") ? PublicationService.getCurrentPublication(cmsObject).getId() : Integer.parseInt(publication));
		
		fileName = nService.crearNoticia(cmsObject, publicationNews, tipoNews, new HashMap<String,String>(),date);
		
		return cmsObject.readFile(fileName);
	}
	
	
	public static CmsFile createResource(CmsObject cmsObject, String tipo, String publication) throws Exception {
		NoticiasService nService = new NoticiasService();
		String fileName = "";
		
		String tipoNews = (tipo == null || tipo.equals("") ? "news" : tipo);
		int publicationNews = (publication == null || publication.equals("") ? PublicationService.getCurrentPublication(cmsObject).getId() : Integer.parseInt(publication));
		
		fileName = nService.crearNoticia(cmsObject, publicationNews, tipoNews, new HashMap<String,String>());
		
		return cmsObject.readFile(fileName);
	}
	
	public static int getPublicationId(CmsObject cmsObject) throws Exception {
		TipoEdicionService tService = new TipoEdicionService();
		String defaultPublication = (String)cmsObject.getRequestContext().currentUser().getAdditionalInfo("USERPREFERENCES_workplace-startupsettingspublication");
		
		TipoEdicion currentPublication = null;
		if (defaultPublication != null && !defaultPublication.toLowerCase().trim().equals("null")) {
			currentPublication = tService.obtenerTipoEdicion(Integer.parseInt(defaultPublication) );
		} else {
			String proyecto = openCmsService.getCurrentSite(cmsObject);
			currentPublication = tService.obtenerEdicionOnlineRoot(proyecto);
		}
		return currentPublication.getId();
	}
}
