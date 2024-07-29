package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.workflow.ResultSetProcessor;

public class EncuestasURLsResultSetProcessor implements ResultSetProcessor<List<String>>, EncuestasSQLConstants {

	private List<String> results = new ArrayList<String>();

	public void processTuple(ResultSet rs) {
		try {
			this.results.add(rs.getString(URL_ENCUESTA));
		}
		catch (SQLException e) {
			throw new ApplicationException("No se pudo leer la columna " + URL_ENCUESTA, e);
		}
	}

	public List<String> getResult() {
		return this.results;
	}
}