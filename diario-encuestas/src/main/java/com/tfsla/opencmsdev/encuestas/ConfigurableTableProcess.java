package com.tfsla.opencmsdev.encuestas;

import org.opencms.file.CmsObject;

public abstract class ConfigurableTableProcess extends AbstractEncuestaProcess {

	protected final String getEncuestaTableName(CmsObject cms) {
		return isOnlineProject(cms) ? TFS_ENCUESTA_ONLINE : TFS_ENCUESTA;
	}

	protected final String getRespuestaEncuestaTableName(CmsObject cms) {
		return isOnlineProject(cms)
			? TFS_RESPUESTA_ENCUESTA_ONLINE
			: TFS_RESPUESTA_ENCUESTA;
	}

	protected boolean isOnlineProject(CmsObject cms) {
		return cms.getRequestContext().currentProject().isOnlineProject();
	}
}
