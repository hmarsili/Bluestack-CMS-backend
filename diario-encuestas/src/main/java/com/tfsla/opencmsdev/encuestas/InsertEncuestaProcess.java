package com.tfsla.opencmsdev.encuestas;

import java.util.Iterator;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.workflow.QueryBuilder;

public class InsertEncuestaProcess extends AbstractEncuestaProcess {

	public void execute(CmsObject cms, String encuestaURL, Encuesta encuesta) {
		insertEncuesta(cms, encuestaURL, encuesta, TFS_ENCUESTA, null);
	}

	public void executeInOnline(CmsObject cms, Encuesta encuesta, List respuestas) {
		insertEncuesta(cms, encuesta.getEncuestaURL(), encuesta, TFS_ENCUESTA_ONLINE, respuestas);
	}

	/**
	 * insert parametrizado para reutilizar el insert en offline y online
	 * 
	 * @param cms
	 * @param encuestaURL
	 * @param encuesta
	 * @param encuestaTableName
	 * @param respuestasTableName
	 * @param respuestas
	 */
	
	//TODO: agregar publicacion
	private void insertEncuesta(CmsObject cms, String encuestaURL, Encuesta encuesta, String encuestaTableName,
			List respuestas) {
		String insertEncuestaSQL = "INSERT INTO " + encuestaTableName + " (" + URL_ENCUESTA + "," + ESTADO_PUBLICACION
			+ "," + GRUPO + "," + FECHA_CIERRE + "," + SITIO + "," + PUBLICACION + ") VALUES ('" + encuestaURL + "','"
			+ encuesta.getEstado() + "','" + encuesta.getGrupo() + "'," + encuesta.getFechaCierre() + ",'" + openCmsService.getCurrentSite(cms) + "'," + encuesta.getPublicacion() + ")";

		new QueryBuilder<String>(cms).setSQLQuery(insertEncuestaSQL).execute();

		// insercion de respuestas

		if (respuestas == null) {
			int id_encuesta = ModuloEncuestas.getEncuestaID(cms, encuestaURL);
			// se insertan en el offline las respuestas de la encuesta
			//List<String> respuestasFromEncuesta = encuesta.getRespuestas();
			
			String[][] respuestasFromEncuesta = encuesta.getRespuestas();
			
			for (int nroRespuesta = 0; nroRespuesta < respuestasFromEncuesta.length; nroRespuesta++) {
				String insertRespuestaSQL = "INSERT INTO " + TFS_RESPUESTA_ENCUESTA + " (" + ID_ENCUESTA + ", "
					+ NRO_RESPUESTA + ") " + "VALUES (" + id_encuesta + ", '" + nroRespuesta + "')";

				new QueryBuilder<String>(cms).setSQLQuery(insertRespuestaSQL).execute();
			}
		}
		else {
			int id_encuesta = ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL);
			// se insertan en el online, las respuestas pasadas por parametro
			for (Iterator iter = respuestas.iterator(); iter.hasNext();) {
				RespuestaEncuestaConVotos respuesta = (RespuestaEncuestaConVotos) iter.next();
				String insertRespuestaInOnline = "INSERT INTO " + TFS_RESPUESTA_ENCUESTA_ONLINE + " (" + ID_ENCUESTA
					+ ", " + NRO_RESPUESTA + ") " + "VALUES (" + id_encuesta + ", '" + respuesta.getNroRespuesta()
					+ "')";

				new QueryBuilder<String>(cms).setSQLQuery(insertRespuestaInOnline).execute();
			}
		}
	}
}