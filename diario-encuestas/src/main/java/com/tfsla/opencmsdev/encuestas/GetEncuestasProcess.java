package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class GetEncuestasProcess extends ConfigurableTableProcess {

	public List execute(CmsObject cms, String tableName, String sitios, String publicaciones, String grupos, String states, String limit, String order) {

		String whereClause = "";
		if (sitios!=null)
		{
			String[] sitiosList = sitios.split(",");
			
			whereClause += "AND " + SITIO + " in (";
			for (String sitio : sitiosList)
				whereClause += "?,";
			whereClause =  whereClause.substring(0, whereClause.length()-1) + ")";
			
		}
		if (publicaciones!=null)
		{
			String[] publicacionesList = publicaciones.split(",");
			
			whereClause += "AND " + PUBLICACION + " in (";
			for (String publicacion : publicacionesList)
				whereClause += "?,";
			whereClause =  whereClause.substring(0, whereClause.length()-1) + ")";
			
		}

		if (grupos!=null)
		{
			String[] gruposList = grupos.split(",");
			
			whereClause += "AND " + GRUPO + " in (";
			for (String grupo : gruposList)
				whereClause += "?,";
			whereClause =  whereClause.substring(0, whereClause.length()-1) + ")";
			
		}
		if (states!=null)
		{
			String[] statesList = states.split(",");
			
			whereClause += "AND " + ESTADO_PUBLICACION + " in (";
			for (String estado : statesList)
				whereClause += "?,";
			whereClause =  whereClause.substring(0, whereClause.length()-1) + ")";
			
		}

		whereClause = whereClause.replaceFirst("AND ", "WHERE ");
		String orderClause = (order != null ) ? " ORDER BY " + order :"";
		String limitSQL = (limit != null) ? " LIMIT 0," + limit : "";

		
		String getEncuestasAnterioresSQL = 
				"SELECT " + 
						URL_ENCUESTA + ", " + 
						FECHA_CIERRE + " " + 
				"FROM " + tableName + " " + 
				whereClause + 
				orderClause + 
				limitSQL;

		QueryBuilder<List<String>> qb =new QueryBuilder<List<String>>(cms).setSQLQuery(
				getEncuestasAnterioresSQL);
		
		if (sitios!=null)
		{
			String[] sitiosList = sitios.split(",");
			for (String sitio : sitiosList)
				qb.addParameter(sitio);
		}

		if (publicaciones!=null)
		{
			String[] publicacionesList = publicaciones.split(",");
			for (String publicacion : publicacionesList)
				qb.addParameter(publicacion);
		}

		if (grupos!=null)
		{
			String[] gruposList = grupos.split(",");
			for (String grupo : gruposList)
				qb.addParameter(grupo);
		}
		
		if (states!=null)
		{
			String[] statesList = states.split(",");
			
			for (String estado : statesList)
				qb.addParameter(estado);
		}
		
		List<String> encuestasAnterioresURLs = qb.execute(new ResultSetProcessor<List<String>>() {

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

		return encuestasAnterioresURLs;
	}

	public List execute(CmsObject cms, String grupo, String state, String limit, String order) {
		return this.execute(cms, this.getEncuestaTableName(cms), null, null, grupo, state, limit, order);
	}

	public List execute(CmsObject cms, String sitio, String publicacion,
			String grupo, String state, String limit, String order) {
		return this.execute(cms, this.getEncuestaTableName(cms), sitio, publicacion, grupo, state, limit, order);
	}

}