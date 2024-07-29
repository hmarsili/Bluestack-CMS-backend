package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class GetEncuestasActivasProcess extends ConfigurableTableProcess {

	/**
	 * TODO que tome las encuestas para noticias del cms y no de tablas custom (usando la property estadoYGrupo)
	 * @param cms
	 * @param tableName
	 * @param limit
	 * @param grupo
	 * @return
	 */
	public List execute(CmsObject cms, String tableName, String limit, String grupo) {
		String filtroGrupo =  (grupo != null && !grupo.equals("")) ? GRUPO + " = '" + grupo + "' AND " : "";
		String limitSQL = (limit != null) ? " LIMIT " + limit : "";

		String getEncuestasNoticiasSQL = "SELECT " + URL_ENCUESTA + ", " + FECHA_CIERRE + " " + "FROM " + tableName
			+ " " + "WHERE SITIO = '" + openCmsService.getCurrentSite(cms) + "' AND " + filtroGrupo + ESTADO_PUBLICACION + " = '" + Encuesta.ACTIVA + "' ORDER BY "
			+ FECHA_CIERRE + " DESC" + limitSQL;

		List<String> encuestasNoticiasURLs = new QueryBuilder<List<String>>(cms).setSQLQuery(
			getEncuestasNoticiasSQL).execute(new ResultSetProcessor<List<String>>() {

			private List<String> encuestasURLs = new ArrayList<String>();

			public void processTuple(ResultSet rs) {
				try {
					this.encuestasURLs.add(rs.getString(URL_ENCUESTA));
				}
				catch (Exception e) {
					throw new ApplicationException("No se pudo leer la columna " + URL_ENCUESTA, e);
				}
			}

			public List<String> getResult() {
				return this.encuestasURLs;
			}
		});

		return encuestasNoticiasURLs;
	}

	public List execute(CmsObject cms, String limit, String grupo) {
		return this.execute(cms, this.getEncuestaTableName(cms), limit, grupo);
	}
}
