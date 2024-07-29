package com.tfsla.utils;

import java.util.List;

/**
 * Lo usa el StringParser, 
 * Sirve para controlar cuando se cumple un token en la cadena de procesamiento
 * @author lgassman
 *
 */
public interface TokenParser {

	/**
	 * Avisa que se está procesando ese caracter
	 * @return si este TokeParser estáen condiciones de parsear la cadena actual. 
	 */
	public boolean addCharacter(char character);
	
	
	/**
	 * Parsea la cadena en distintos tokens.
	 * @param string
	 * @return
	 */
	public List<String> parse(String string);
	
	/**
	 * Avisa que el token actual ya se procesá, con lo cual debe
	 * desechar su estado actual
	 */
	public void newToken();

}
