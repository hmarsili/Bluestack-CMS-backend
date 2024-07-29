package com.tfsla.workflow;

import java.sql.ResultSet;

public interface ResultSetProcessor<T> {

	/**
	 * Procesa una fila del resultSet
	 * No debe hacerle next al rs
	 * @param rs
	 */
	public void processTuple(ResultSet rs);
	
	/**
	 * @return El resultado de la operacion
	 */
	public T getResult();
	
}
