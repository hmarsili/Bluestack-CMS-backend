package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.DateUtils;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class CerrarEncuestaProcess extends AbstractEncuestaProcess {

	public void execute(CmsJspActionElement jsp, String encuestaURL) {
		try {
			
			CmsResourceUtils.forceLockResource(jsp.getCmsObject(),encuestaURL);
			
			Encuesta encuesta = Encuesta.getEncuestaFromURL(jsp.getCmsObject(), encuestaURL);
			encuesta.cerrar();

			Locale locale = java.util.Locale.ENGLISH;
			
			CmsFile readFile = jsp.getCmsObject().readFile(encuestaURL);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(jsp.getCmsObject(), readFile);
					      content.setAutoCorrectionEnabled(true); 
			              content.correctXmlStructure(jsp.getCmsObject());
			              
			String grupo = content.getValue("grupo", locale).getStringValue(jsp.getCmsObject());
			String estadoYGrupo = "Cerrada-"+grupo;
			              
			              content.getValue("estado", locale).setStringValue(jsp.getCmsObject(), "Cerrada");
			              content.getValue("estadoYGrupo", locale).setStringValue(jsp.getCmsObject(),estadoYGrupo);
			              readFile.setContents(content.marshal());
			              jsp.getCmsObject().writeFile(readFile);
			       
			 CmsResourceUtils.unlockResource(jsp.getCmsObject(), encuestaURL, false);
			              
			updateEstadoPublicacionCerrada(jsp.getCmsObject(), encuestaURL, grupo);

			boolean existsInOnline = existsInOnline(jsp, encuesta);
			
			if (existsInOnline) {
				updateEstadoPublicacionCerradaOnline(jsp.getCmsObject(), encuestaURL, grupo);
			}
		}
		catch (Exception e) {
			throw new CmsRuntimeException(Messages.get().container(Messages.ENCUESTA_CERRAR_ERROR,
				new Object[] { encuestaURL, e.getMessage() }));
		}
	}
	
	private static void updateEstadoPublicacionCerrada(CmsObject cms, String encuestaURL, String grupo) {
		String updateEstadoPublicacionSQL = "UPDATE " + TFS_ENCUESTA + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ Encuesta.CERRADA + "', " + GRUPO + " = '" + grupo + "' " + "WHERE " + URL_ENCUESTA + " = '" + encuestaURL
			+ "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();
	}
	
	private static void updateEstadoPublicacionCerradaOnline(CmsObject cms, String encuestaURL, String grupo) {
		String updateEstadoPublicacionSQL = "UPDATE " + TFS_ENCUESTA_ONLINE + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ Encuesta.CERRADA + "', " + GRUPO + " = '" + grupo + "' " + "WHERE " + URL_ENCUESTA + " = '" + encuestaURL
			+ "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();
	}
	
	private boolean existsInOnline(CmsJspActionElement jsp, Encuesta encuestaDAO) {
		String existsEncuestaInOnlineQuery = "SELECT " + URL_ENCUESTA + " FROM " + TFS_ENCUESTA_ONLINE + " WHERE "
			+ URL_ENCUESTA + " = '" + encuestaDAO.getEncuestaURL() + "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(jsp.getCmsObject()) + "'";

		ResultSetProcessor processor = new EncuestasResultSetProcessor() {

			private Boolean exists = false;

			@Override
			protected void doProcessTuple(ResultSet rs) throws Exception {
				this.exists = Boolean.TRUE;
			}

			public Boolean getResult() {
				return this.exists;
			}
		};

		Boolean existsInOnline = (Boolean) getQueryRunner(jsp.getCmsObject(), existsEncuestaInOnlineQuery).execute(
			processor);
		return existsInOnline.booleanValue();
	}
	
}