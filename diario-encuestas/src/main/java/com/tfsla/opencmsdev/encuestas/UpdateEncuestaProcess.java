package com.tfsla.opencmsdev.encuestas;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.services.openCmsService;

public class UpdateEncuestaProcess extends AbstractEncuestaProcess {

	public void execute(CmsObject cms, Encuesta encuesta) {
		String updateSQL = "UPDATE " + TFS_ENCUESTA_ONLINE + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ encuesta.getEstado() + "', " + GRUPO + " = '" + encuesta.getGrupo() + "' " + ", " + FECHA_CIERRE + " = "
			+ encuesta.getFechaCierre() + " WHERE " + URL_ENCUESTA + " = '" + encuesta.getEncuestaURL() + "' AND " + SITIO + "= '" + openCmsService.getCurrentSite(cms) + "'";

		this.getQueryRunner(cms, updateSQL).execute();
	}
	
	public void executeOffline(CmsObject cms, Encuesta encuesta) {
		String updateSQL = "UPDATE " + TFS_ENCUESTA + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ encuesta.getEstado() + "', " + GRUPO + " = '" + encuesta.getGrupo() + "' " + ", " + FECHA_CIERRE + " = "
			+ encuesta.getFechaCierre() + " WHERE " + URL_ENCUESTA + " = '" + encuesta.getEncuestaURL() + "' AND " + SITIO + "= '" + openCmsService.getCurrentSite(cms) + "'";

		this.getQueryRunner(cms, updateSQL).execute();
	}

}