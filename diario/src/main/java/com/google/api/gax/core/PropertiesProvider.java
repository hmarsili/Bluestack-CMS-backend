package com.google.api.gax.core;

import com.google.api.core.BetaApi;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Provides meta-data properties stored in a properties file. */
@BetaApi
public class PropertiesProvider {
  private static final String DEFAULT_VERSION = "";
  private static final Logger logger = Logger.getLogger(PropertiesProvider.class.getName());

  /**
   * Utility method for retrieving the value of the given key from a property file in the package.
   *
   * @param loadedClass The class used to get the resource path
   * @param propertiesPath The relative file path to the property file
   * @param key Key string of the property
   */
  public static String loadProperty(Class loadedClass, String propertiesPath, String key) {
    try {
      InputStream inputStream = loadedClass.getResourceAsStream(propertiesPath);
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties.getProperty(key);
      } else {
        logMissingProperties(loadedClass, propertiesPath);
        return null;
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception loading properties at \"" + propertiesPath + "\"", e);
      return null;
    }
  }

  /**
   * Utility method for retrieving the value of the given key from a property file in the package.
   *
   * @param properties The properties object to cache the properties data in
   * @param propertiesPath The relative file path to the property file
   * @param key Key string of the property
   */
  public static String loadProperty(Properties properties, String propertiesPath, String key) {
    try {
      if (properties.isEmpty()) {
        InputStream inputStream = PropertiesProvider.class.getResourceAsStream(propertiesPath);
        if (inputStream != null) {
          properties.load(inputStream);
        } else {
          logMissingProperties(PropertiesProvider.class, propertiesPath);
          return null;
        }
      }
      return properties.getProperty(key);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception loading properties at \"" + propertiesPath + "\"", e);
      return null;
    }
  }

  private static void logMissingProperties(Class loadedClass, String propertyFilePath) {
    logger.log(
        Level.WARNING,
        "Warning: Failed to open properties resource at '%s' of the given class '%s'\n",
        new Object[] {propertyFilePath, loadedClass.getName()});
  }
}