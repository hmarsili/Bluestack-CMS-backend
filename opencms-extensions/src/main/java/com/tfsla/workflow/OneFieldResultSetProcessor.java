package com.tfsla.workflow;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.tfsla.opencms.exceptions.ProgramException;

public class OneFieldResultSetProcessor<T> implements ResultSetProcessor<T> {

	private T value;
	
	public T getResult() {
		return this.value;
	}

	@SuppressWarnings("unchecked")
	public void processTuple(ResultSet rs) {
		try {
			this.value = (T) rs.getObject(1);
		}
		catch (SQLException e) {
			throw new ProgramException("Error obtiendo el campo 1 del resultSet", e);
		}
	}

}
