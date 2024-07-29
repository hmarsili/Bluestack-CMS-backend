package com.tfsla.opencmsdev.encuestas;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.opencms.file.CmsObject;

import com.tfsla.workflow.QueryBuilder;

public abstract class AbstractEncuestaProcess implements EncuestasSQLConstants {

	protected QueryBuilder getQueryRunner(CmsObject cms, String query) {
		return new QueryBuilder(cms).setSQLQuery(query);
	}
	
	//******************************
	//** Exception helpers
	//******************************
	protected String getCauseAsString(Exception e) {
		try {
			StringWriter sw = new StringWriter();

			PrintWriter st = new PrintWriter(sw);
			e.printStackTrace(st);

			return sw.getBuffer().toString();
		}
		catch (Exception e1) {
			return "nested error trying to get cause " + e;
		}
	}

}
