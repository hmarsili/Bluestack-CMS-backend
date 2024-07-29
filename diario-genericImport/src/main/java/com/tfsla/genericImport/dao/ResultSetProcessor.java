package com.tfsla.genericImport.dao;

import java.sql.ResultSet;

public interface ResultSetProcessor<T> {

	/**
	 * Procesa una fila del resultSet
	 * No debe hacerle next al rs
	 * @param rs
	 * @throws Exception 
	 */
	public void processTuple(ResultSet rs) throws Exception;
	
	/**
	 * @return El resultado de la operacion
	 */
	public T getResult();
	
}
