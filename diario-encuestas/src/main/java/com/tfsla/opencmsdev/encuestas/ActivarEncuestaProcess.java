package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class ActivarEncuestaProcess extends AbstractEncuestaProcess {

	public void execute(CmsJspActionElement jsp, String encuestaURL, String grupo) {
		try {
				Encuesta encuesta = Encuesta.getEncuestaFromURL(jsp.getCmsObject(), encuestaURL);
				encuesta.activar();

				//TfsEncuestasContenidoEditor editor = new TfsEncuestasContenidoEditor(jsp, encuestaURL, encuesta);
			    //editor.save();
				Locale locale = java.util.Locale.ENGLISH;
				
				String estadoYGrupo = "Activa-"+grupo;
				
				CmsFile readFile = jsp.getCmsObject().readFile(encuestaURL);
				CmsXmlContent content = CmsXmlContentFactory.unmarshal(jsp.getCmsObject(), readFile);
						      content.setAutoCorrectionEnabled(true); 
				              content.correctXmlStructure(jsp.getCmsObject());
				              content.getValue("estado", locale).setStringValue(jsp.getCmsObject(), "Activa");
				              content.getValue("estadoYGrupo", locale).setStringValue(jsp.getCmsObject(),estadoYGrupo);
				              readFile.setContents(content.marshal());
				              jsp.getCmsObject().writeFile(readFile);

				updateEstadoPublicacionActiva(jsp.getCmsObject(), encuestaURL, grupo);
				
				boolean existsInOnline = existsInOnline(jsp, encuesta);
				
				if (existsInOnline) {
					updateEstadoPublicacionActivaOnline(jsp.getCmsObject(), encuestaURL, grupo);
				}
				 
		}
		catch (Exception e) {
			throw new CmsRuntimeException(Messages.get().container(Messages.ENCUESTA_PUBLICAR_ERROR,
				new Object[] { encuestaURL, e.getMessage() }));
		}
	}

	private static void updateEstadoPublicacionActiva(CmsObject cms, String encuestaURL, String grupo) {
		String updateEstadoPublicacionSQL = "UPDATE " + TFS_ENCUESTA + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ Encuesta.ACTIVA + "', " + GRUPO + " = '" + grupo + "' " + "WHERE " + URL_ENCUESTA + " = '" + encuestaURL
			+ "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();
	}
	
	private static void updateEstadoPublicacionActivaOnline(CmsObject cms, String encuestaURL, String grupo) {
		String updateEstadoPublicacionSQL = "UPDATE " + TFS_ENCUESTA_ONLINE + " " + "SET " + ESTADO_PUBLICACION + " = '"
			+ Encuesta.ACTIVA + "', " + GRUPO + " = '" + grupo + "' " + "WHERE " + URL_ENCUESTA + " = '" + encuestaURL
			+ "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		new QueryBuilder<String>(cms).setSQLQuery(updateEstadoPublicacionSQL).execute();
	}

	private static String hayEncuesta(CmsObject cms, String grupo, String estadoPublicacion) {
		String selectEncuestaAnteriorSQL = "SELECT " + URL_ENCUESTA + " FROM " + TFS_ENCUESTA + " " + "WHERE " + GRUPO
			+ " = '" + grupo + "' AND " + ESTADO_PUBLICACION + " = '" + estadoPublicacion + "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		String result = new QueryBuilder<String>(cms).setSQLQuery(selectEncuestaAnteriorSQL).execute(
			new ResultSetProcessor<String>() {

				private String urlEncuestaAnterior;

				public void processTuple(ResultSet rs) {
					try {
						this.urlEncuestaAnterior = rs.getString(URL_ENCUESTA);
					}
					catch (SQLException e) {
						throw new ApplicationException("No se pudo leer la columna " + URL_ENCUESTA, e);
					}
				}

				public String getResult() {
					return this.urlEncuestaAnterior == null ? NADA : this.urlEncuestaAnterior;
				}
			});

		return result;
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