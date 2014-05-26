package org.timadorus.auth.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.timadorus.auth.util.Config;

/**
 * The entry-point of the application.
 * 
 * @author Torben Könke
 */
public final class Program {
  /**
   * Logging facility.
   */
  private static final Logger LOG = Logger.getLogger(Program.class.getName());

  /**
   * The default port at which the server runs.
   */
  private static final int DEFAULT_SERVICE_PORT = 50001;
  
  /**
   * The server's configuration file.
   */
  private static final String CONFIG = "server-config.xml";
  
  /**
   * Can we get rid of this useless StyleCheck warning already?
   */
  private Program() {
    // Make StyleCheck happy.
  }
  
  /**
   * Retrieves the version string from the jar's manifest file.
   * 
   * @return
   *  The version string (specification-version) of the jar's manifest or
   *  "n/a" if the version string could not be retrieved.
   */
  private static String getVersionString() {
    try {
      String className = Program.class.getSimpleName() + ".class";
      String classPath = Program.class.getResource(className).toString();
      if (!classPath.startsWith("jar")) {
        return "n/a";
      }
      String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
          + "/META-INF/MANIFEST.MF";
      Manifest manifest = new Manifest(new URL(manifestPath).openStream());
      Attributes attr = manifest.getMainAttributes();
      return attr.getValue("Specification-Version");    
    } catch (Exception e) {
      return "n/a";
    }
  }
  
  /**
   * The entry-point of the application.
   * 
   * @param args
   *  The command-line arguments.
   * @exception Exception
   *  An unexpected error occurred.
   */
  public static void main(String[] args) throws Exception {
    String configPath = "./";
    if (args.length > 0) {
      configPath = args[0];
    }
    // Read properties from the configuration file.
    Config config = new Config(configPath + "/" + CONFIG);
    if (config.hasProperty("loggingFile")) {
      loadLoggingProperties(config.getString("loggingFile"));
    }
    int listenPort = config.hasProperty("listenPort")
        ? config.getInt("listenPort") : DEFAULT_SERVICE_PORT;
    InetAddress inetAddr = config.hasProperty("networkInterface")
        ? InetAddress.getByName(config.getString("networkInterface")) : null;
    boolean interactive = config.getBoolean("interactiveMode");
        
    // Init the database class.
    Database.init(config.getString("dbDriverClassName"),
                  config.getString("dbConnectionString"),
                  config.hasProperty("dbTablePrefix")
                  ? config.getString("dbTablePrefix") : null);
    // Test the database settings before starting the actual server.
    if (!Database.testConnection()) {
      throw new Exception("The connection to the database could not be "
        + "established. Please verify the 'dbDriverClassName' and "
        + "'dbConnectionString' settings in the '" + CONFIG
        + "' file are correct.");
    }
    
    // Be sure the tables exist.
    if (!Database.tablesExist()) {
      System.out.println("Attempting to create database tables...");
      Database.createTables();
      // Create the default administrator account.
      Database.createUser("admin", "password", true, 0);
      System.out.println("Created database tables. You should now change the "
          + "password 'password' of the default administrator account "
          + "'account'.");
    }
    // Create and start a new auth-server instance.
    AuthServer server = new AuthServer(listenPort,
     config.getString("keyStoreFile"),
     config.getString("keyStorePassword"),
     config.hasProperty("trustStoreFile") ? config.getString("trustStoreFile") : null,
     config.getString("sharedSecretKey"),
     inetAddr,
     config.getString("gameServers"),
     config.getBoolean("sessionEncryption"));
    
    server.start();
    String m = "Timadorus auth server (Version " + getVersionString()
        + ") started. Accepting connections on "
        + (inetAddr != null ? inetAddr : "all interfaces") + " on port "
        + listenPort + ".";
    System.out.println(m);
    LOG.info(m);
    // Start a simple command-line interpreter.
    if (interactive) {
      System.out.println("Type help for a list of available commands.");
      new Interpreter().start(server);
    } else {
      System.out.println("Running as service.");
    }
  }
  
  /**
   * Loads the specified file as a configuration file for the java.util.log
   * logging framework.
   * 
   * @param file
   *  The logging.properties file to load.
   * @throws Exception
   *  The file could not be loaded or another unexpected error occurred.
   */
  private static void loadLoggingProperties(String file) throws Exception {
    try {
      System.setProperty("java.util.logging.config.file", file);
      LogManager logManager = LogManager.getLogManager();
      logManager.readConfiguration();
    } catch (IOException e) {
      throw new Exception("Failed to load logging properties from '"
          + file + "'", e);
    }
  }
}
