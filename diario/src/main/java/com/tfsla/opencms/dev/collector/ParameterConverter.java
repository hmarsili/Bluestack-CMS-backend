package com.tfsla.opencms.dev.collector;

public interface ParameterConverter <T extends CollectorParameter>{

	/**
	 * @param param el parametro a convertir
	 * @param owner Es el objeto al cual el ParameterConverter le enviara un mensaje en funcion de param
	 */
	public void convert(String param, T owner);
	
	public boolean canConvert(String param, T owner);

}
