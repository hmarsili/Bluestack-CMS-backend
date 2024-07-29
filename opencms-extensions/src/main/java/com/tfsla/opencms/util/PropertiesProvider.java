package com.tfsla.opencms.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.tfsla.opencms.exceptions.ProgramException;

/**
 * @author jpicasso
 */
public class PropertiesProvider {

	public static String propBinding = "4XsrljXpvDh5SQ2sov9XcdHFLHhevw";
	private Properties properties;

	/**
	 * @param callerClass se usa para buscar el file con el classloader del invocador
	 */
	public PropertiesProvider(Class callerClass, String fileName) {
		this.properties = this.loadConfigurationProperties(callerClass, fileName);
	}

	private Properties loadConfigurationProperties(Class callerClass, String fileName) {
		try {

			InputStream configurationStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

			if (configurationStream == null) {
				// si no lo encontre por el classloader lo busco a nivel de la clase llamadora
				configurationStream = callerClass.getResource(fileName).openStream();
				callerClass.getResource(fileName);
				if (configurationStream == null) {
					throw new ProgramException("Error de configuraci�n." + "No se encontr� el archivo "
							+ fileName);
				}
			}

			Properties properties = new Properties();
			properties.load(configurationStream);
			configurationStream.close();
			return properties;
		}
		catch (IOException exception) {
			throw ProgramException.wrap("Error al leer el archivo de configuraci�n.", exception);
		}
	}

	public String get(String propertyName) {
		if (propertyName == null) {
			return "Warning: null propertyName";
		}

		return this.properties.getProperty(propertyName);
	}

	/**
	 * Utilizar este metodo para properties que sean multivaluadas. Se toma como separador la coma (,).
	 * 
	 * @param propertyName la key de la property.
	 * @return una collection con todos los valores de la clave
	 */
	public List getAll(String propertyName) {
		String property = get(propertyName);
		StringTokenizer tokenizer = new StringTokenizer(property, ",");

		List<String> propertyValues = new ArrayList<String>();

		while (tokenizer.hasMoreTokens()) {
			propertyValues.add(tokenizer.nextToken());
		}

		return propertyValues;
	}
}