package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;

import com.tfsla.workflow.ResultSetProcessor;

public abstract class EncuestasResultSetProcessor implements ResultSetProcessor {

	public final void processTuple(ResultSet rs) {
		try {
			this.doProcessTuple(rs);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void doProcessTuple(ResultSet rs) throws Exception {
	}
}