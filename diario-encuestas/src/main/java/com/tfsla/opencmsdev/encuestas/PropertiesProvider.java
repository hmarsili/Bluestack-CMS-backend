package com.tfsla.opencmsdev.encuestas;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author jpicasso
 */
public class PropertiesProvider {

    private Properties properties;

    public PropertiesProvider(String fileName) {
        this.properties = this.loadConfigurationProperties(fileName);
    }

    private Properties loadConfigurationProperties(String fileName) {
        InputStream configurationStream = this.getClass().getResourceAsStream(
                fileName);

        if (configurationStream == null) {
            throw new RuntimeException("Error de configuración."
                    + "No se encontró el archivo " + fileName);
        }

        try {
            Properties properties = new Properties();
            properties.load(configurationStream);
            return properties;
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Error al leer el archivo de configuración.", exception);
        }
    }

    public String get(String propertyName) {
        if (propertyName == null) {
            return "Warning: null propertyName";
        }

        return this.properties.getProperty(propertyName);
    }

    /**
     * Utilizar este metodo para properties que sean multivaluadas. Se toma como
     * separador la coma (,).
     * 
     * @param propertyName
     *            la key de la property.
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