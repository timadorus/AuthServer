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
  /**
   * Make CheckStyle happy.
   */
  private Config() {

  }

  /**
   * Loads the specified property file.
   * 
   * @param path
   *          The property file to load.
   * @return An initialized instance of the Properties class representing the
   *         properties read from the specified file.
   * @throws IOException
   *           The property file could not be read.
   */
  public static Properties load(String path) throws IOException {
    InputStream configFileStream = new FileInputStream(path);
    Properties p = new Properties();
    p.loadFromXML(configFileStream);
    configFileStream.close();
    // Trim whitespaces and tabs off the values.
    for (Entry<Object, Object> entry : p.entrySet()) {
      p.setProperty((String) entry.getKey(),
                    ((String) entry.getValue()).trim());
    }
    return p;
  }
}
