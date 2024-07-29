package com.tfsla.diario.ediciones.services;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.opencms.dev.collector.DateFolder;

public class SeriesService extends baseService {
	public static final String PELICULAS_PATH = "vod";
	public static final int TYPE_ID = 5065;

	
	private static final Log LOG = CmsLog.getLog(SeriesService.class);

	
	/**
	 * Crea una serie en el directorio indicado con la numeracion correspondiente.
	 * @param obj
	 * @param location
	 * @return Path y nombre del evento creado.
	 * @throws CmsIllegalArgumentException
	 * @throws CmsException
	 */
	public String crearSerieNombre(CmsObject cms,String location) throws CmsIllegalArgumentException, CmsException{
		int shift=1;
		String fileName = "";
		boolean done = false;
		while (!done && shift <=10) {
			try {
				fileName = getNextSerieName(cms,location,shift);

				int eventType = OpenCms.getResourceManager().getResourceType("serie").getTypeId();
				cms.createResource(fileName,eventType);
		
				done=true;
				
			}
			catch (CmsException ex) {
				LOG.error("Intento " + shift + " - Error al crear la pelicula " + fileName,ex);
				if (shift>=10)
					throw ex;
			}
			shift+=2;
		}

		return fileName;
	}
	
	public String crearSerie(CmsObject cms, int tipoEdicion) throws CmsIllegalArgumentException, CmsException, UndefinedTipoEdicion {
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);
		
		String path = tEdicion.getBaseURL().replace("/contenidos/", "");
		
		path = cms.getRequestContext().removeSiteRoot(path);
		
		path +=  PELICULAS_PATH + "/";
		
		return crearSerieNombre(cms, path);
	}
	
	private String getNextSerieName(CmsObject obj,String location,int shift) throws CmsException {
		String fileName="";
		
		String[] niveles = location.split("/");
		
		
		//Solo si el contenido es de publicaciones de tipo ONLINE y ONLINE ROOT
		if (niveles.length <= 3)
			location = new DateFolder(location, true).getTodayFolder(obj);

		fileName = createSerieName(obj, location,"serie", shift);

		return fileName;
	}

	private String createSerieName(CmsObject obj, String location, String fileNamePreffix, int shift)
			throws CmsException {
		String fileName;
		int maxNewsValue  = 0;
		List cmsFiles = obj.getResourcesInFolder(location, CmsResourceFilter.ALL);
		for (Iterator it = cmsFiles.iterator(); it.hasNext();) {
			CmsResource resource = (CmsResource) it.next();
			fileName = resource.getName();
			if (fileName.matches(".*" + fileNamePreffix + "_[0-9]{4}.html")) {
				String auxFileName =fileName.substring(fileName.indexOf(fileNamePreffix + "_"));
				int newsValue = Integer.parseInt(auxFileName.replace(fileNamePreffix + "_","").replace(".html",""));
				if (maxNewsValue<newsValue)
					maxNewsValue=newsValue;
			}
		}

		DecimalFormat df = new DecimalFormat("0000");
		fileName = location + fileNamePreffix + "_" + df.format(maxNewsValue+shift) + ".html"; 
		return fileName;
	}
	
}
