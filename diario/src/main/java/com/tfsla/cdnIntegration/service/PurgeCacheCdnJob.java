package com.tfsla.cdnIntegration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class PurgeCacheCdnJob  implements I_CmsScheduledJob {

	private String module = "contentDeliveryNetwork";

	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		String resultado = "";
		
		String publication = (String)parameters.get("publication");
		String site = (String)parameters.get("site");
	
		
		List<String> publicaciones =  new ArrayList<String>();
		
		try {
			if (publication.equals("all")){
				TipoEdicionService tService = new TipoEdicionService();
				List<TipoEdicion> ediciones= tService.obtenerTipoEdiciones(site.substring(site.indexOf("/", 1)+1));
				for (TipoEdicion tipoEdicion : ediciones) {
					publicaciones.add(String.valueOf(tipoEdicion.getId()));
				}
			} else {
				if (publication.indexOf(",") >0){
					String[] ids = publication.split(",");
					for (String id : ids) {
						publicaciones.add(id);
					}
				}else {
					publicaciones.add(publication);
				}
			}
		} catch (Exception ex) {
			CmsLog.getLog(this).error("Error al buscar las publicaciones", ex);
		}
		
		for (String publicacion : publicaciones) {
			if (CmsMedios.getInstance().getCmsParaMediosConfiguration().getModule(site, publicacion, module) != null &&
					CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(site, publicacion, module, "isActive", false)) {
				CdnManager.getInstance(site, publicacion).sendPurgePackage(cms);
			}
		}
		return resultado;
	}

}
