package org.timadorus.auth.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Provides a convenient means for loading property files.
 * 
 * @author Torben Könke
 */
public final class Config {
  private final Properties props = new Properties();
  
  /**
   * Initializes a new instance of the Config class.
   * 
   * @param path
   *          The property file to load.
   * @throws IllegalArgumentException
   *  The path parameter is null.
   * @throws IOException
   *           The property file could not be read.
   */
  public Config(String path) throws IOException {
    if (path == null) {
      throw new IllegalArgumentException("path");
    }
    InputStream configFileStream = new FileInputStream(path);
    props.loadFromXML(configFileStream);
    configFileStream.close();
    // Trim whitespaces and tabs off the values.
    for (Entry<Object, Object> entry : props.entrySet()) {
      props.setProperty((String) entry.getKey(),
                    ((String) entry.getValue()).trim());
    }
  }
  
  /**
   * Determines whether the specified configuration value exists.
   * 
   * @param key
   *  The name of the configuration value to probe.
   * @return
   *  true if the configuration value exists; Otherwise false.
   * @throws IllegalArgumentException
   *  The key parameter is null.
   */
  public boolean hasProperty(String key) {
    if (key == null) {
      throw new IllegalArgumentException("key");
    }
    return props.getProperty(key) != null;
  }
  
  /**
   * Gets the configuration value with the specified name as a string.
   * 
   * @param key
   *  The name of the configuration value to retrieve.
   * @return
   *  The configuration value as a string.
   * @throws IllegalArgumentException
   *  The key parameter is null, or a configuration value with the specified
   *  name does not exist.
   */
  public String getString(String key) {
    if (key == null) {
      throw new IllegalArgumentException("key");
    }
    String value = props.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException("A property with key='" + key
                                         + "' does not exist.");
    }
    return value;
  }
  
  /**
   * Gets the configuration value with the specified name as a boolean.
   * 
   * @param key
   *  The name of the configuration value to retrieve.
   * @return
   *  The configuration value as a boolean value.
   * @throws IllegalArgumentException
   *  The key parameter is null, or a configuration value with the specified
   *  name does not exist, or the value is not a valid boolean value.
   */
  public boolean getBoolean(String key) {
    if (key == null) {
      throw new IllegalArgumentException("key");
    }
    String value = props.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException("A property with key='" + key
                                         + "' does not exist.");
    }
    return !value.equalsIgnoreCase("false");
  }
  
  /**
   * Gets the configuration value with the specified name as an integer.
   * 
   * @param key
   *  The name of the configuration value to retrieve.
   * @return
   *  The configuration value as an integer value.
   * @throws IllegalArgumentException
   *  The key parameter is null, or a configuration value with the specified
   *  name does not exist, or the value is not a valid integer value.
   */
  public int getInt(String key) {
    if (key == null) {
      throw new IllegalArgumentException("key");
    }
    String value = props.getProperty(key);
    if (value == null) {
      throw new IllegalArgumentException("A property with key='" + key
                                         + "' does not exist.");
    }
    return Integer.parseInt(value);
  }
}
