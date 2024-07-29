package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class GetResultadosProcess extends ConfigurableTableProcess {

	// *********************************************************************
	// ** un solo resultado - encuesta activa ya votada o encuesta cerrada
	// *********************************************************************
	public ResultadoEncuestaBean executeOnline(CmsObject cms, String encuestaURL) {
		List respuestas = this.getVotosOnline(cms, encuestaURL);
		return new ResultadoEncuestaBean(respuestas);
	}

	
	public ResultadoEncuestaBean execute(CmsObject cms, String encuestaURL) {
		List respuestas = this.getVotos(cms, encuestaURL);
		return new ResultadoEncuestaBean(respuestas);
	}

	private List<RespuestaEncuestaConVotos> getVotos(CmsObject cms, String encuestaURL) {
		int encuestaID = this.isOnlineProject(cms)
			? ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL)
			: ModuloEncuestas.getEncuestaID(cms, encuestaURL);

		String getVotosSQL = "SELECT " + NRO_RESPUESTA + ", " + CANT_VOTOS + ", " + ID_RESPUESTA + " " + "FROM "
			+ getRespuestaEncuestaTableName(cms) + " " + "WHERE " + ID_ENCUESTA + " = " + encuestaID + " ORDER BY "
			+ NRO_RESPUESTA;

		return new QueryBuilder<List<RespuestaEncuestaConVotos>>(cms).setSQLQuery(getVotosSQL).execute(
			new ResultSetProcessor<List<RespuestaEncuestaConVotos>>() {

				private List<RespuestaEncuestaConVotos> respuestas = new ArrayList<RespuestaEncuestaConVotos>();

				public void processTuple(ResultSet rs) {
					try {
						int nroRespuesta = rs.getInt(NRO_RESPUESTA);
						int cantVotos = rs.getInt(CANT_VOTOS);
						int id_respuesta = rs.getInt(ID_RESPUESTA);
						this.respuestas.add(new RespuestaEncuestaConVotos(id_respuesta, nroRespuesta, cantVotos));
					}
					catch (Exception e) {
						throw new ApplicationException("Error al leer las respuestas de la base de encuestas", e);
					}
				}

				public List<RespuestaEncuestaConVotos> getResult() {
					return this.respuestas;
				}
			});
	}

	private List<RespuestaEncuestaConVotos> getVotosOnline(CmsObject cms, String encuestaURL) {
		int encuestaID = ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL);

		String getVotosSQL = "SELECT " + NRO_RESPUESTA + ", " + CANT_VOTOS + ", " + ID_RESPUESTA + " " + "FROM "
			+ TFS_RESPUESTA_ENCUESTA_ONLINE + " " + "WHERE " + ID_ENCUESTA + " = " + encuestaID + " ORDER BY "
			+ NRO_RESPUESTA;

		return new QueryBuilder<List<RespuestaEncuestaConVotos>>(cms).setSQLQuery(getVotosSQL).execute(
			new ResultSetProcessor<List<RespuestaEncuestaConVotos>>() {

				private List<RespuestaEncuestaConVotos> respuestas = new ArrayList<RespuestaEncuestaConVotos>();

				public void processTuple(ResultSet rs) {
					try {
						int nroRespuesta = rs.getInt(NRO_RESPUESTA);
						int cantVotos = rs.getInt(CANT_VOTOS);
						int id_respuesta = rs.getInt(ID_RESPUESTA);
						this.respuestas.add(new RespuestaEncuestaConVotos(id_respuesta, nroRespuesta, cantVotos));
					}
					catch (Exception e) {
						throw new ApplicationException("Error al leer las respuestas de la base de encuestas", e);
					}
				}

				public List<RespuestaEncuestaConVotos> getResult() {
					return this.respuestas;
				}
			});
	}

}