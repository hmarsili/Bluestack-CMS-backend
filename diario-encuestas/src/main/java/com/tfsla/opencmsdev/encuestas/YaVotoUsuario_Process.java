package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class YaVotoUsuario_Process extends AbstractEncuestaProcess {
	
	public boolean execute(HttpServletRequest request,CmsObject cms, Encuesta encuesta, String Usuario) {
		int CantidadVotosPermitidos = GetEncuestasProperties.getInstance(cms).getCantVotosxUsuario();
		boolean YaVoto = false;
		
		int IdEncuesta = encuesta.getIdEncuesta();
		
		int VotosYaEmitidosPorUsuario = getEncuestaVotosxUsuario(cms, IdEncuesta, Usuario);
		
		if(VotosYaEmitidosPorUsuario >= CantidadVotosPermitidos ){
			YaVoto = true;
		}
		
		return YaVoto;
	}
	
	
	@Deprecated
	public boolean execute(HttpServletRequest request,CmsObject cms, String encuestaURL, String Usuario) {
		int CantidadVotosPermitidos = GetEncuestasProperties.getInstance(cms).getCantVotosxUsuario();
		boolean YaVoto = false;
		
		int IdEncuesta = cms.getRequestContext().currentProject().isOnlineProject()
		? ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL)
		: ModuloEncuestas.getEncuestaID(cms, encuestaURL);
		
		int VotosYaEmitidosPorUsuario = getEncuestaVotosxUsuario(cms, IdEncuesta, Usuario);
		
		if(VotosYaEmitidosPorUsuario >= CantidadVotosPermitidos ){
			YaVoto = true;
		}
		
		return YaVoto;
	}
	
	public static int getEncuestaVotosxUsuario(CmsObject cms,int IdEncuesta, String Usuario){
		
		String getEncuestaSQL = "SELECT " + CANT_VOTOS_IP + " FROM "+ TABLA_ENCUESTA_VOTOS +" WHERE "
		+ ID_ENCUESTA + " = '" + IdEncuesta + "' AND  "+ VOTO_USUARIO +"='"+ Usuario +"' ";

		Integer cant_votos_encuesta_por_Usuario = new QueryBuilder<Integer>(cms).setSQLQuery(getEncuestaSQL).execute(
		new ResultSetProcessor<Integer>() {

			private Integer CantidadVotosPorUsuario;

			public void processTuple(ResultSet rs) {
				try {
					this.CantidadVotosPorUsuario = new Integer(rs.getInt(CANT_VOTOS_IP));
				}
				catch (SQLException e) {
					throw new ApplicationException("No se pudo leer la columna " + CANT_VOTOS_IP, e);
				}
			}

			public Integer getResult() {
				return this.CantidadVotosPorUsuario;
			}
		});
		
		if(cant_votos_encuesta_por_Usuario == null ){
			cant_votos_encuesta_por_Usuario=-1;
		}

		return cant_votos_encuesta_por_Usuario;
		
	}

}
