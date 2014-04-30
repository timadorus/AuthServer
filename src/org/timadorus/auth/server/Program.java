package org.timadorus.auth.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.LogManager;

import org.timadorus.auth.util.Config;

/**
 * The entry-point of the application.
 * 
 * @author Torben Könke
 */
public final class Program {
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
    Properties props = Config.load(configPath + "/" + CONFIG);
    String loggingFile = props.getProperty("loggingFile");
    if (loggingFile != null) {
      loadLoggingProperties(loggingFile);
    }
    String port = props.getProperty("listenPort");
    String addr = props.getProperty("networkInterface");
    int listenPort = DEFAULT_SERVICE_PORT;
    if (port != null) {
      listenPort = Integer.parseInt(port);
    }
    InetAddress inetAddr = null;
    if (addr != null) {
      inetAddr = InetAddress.getByName(addr);
    }
    // Make sure a key-store file and password have been specified.
    String keyStoreFile = props.getProperty("keyStoreFile");
    String keyStorePass = props.getProperty("keyStorePassword");
    if (keyStoreFile == null || keyStorePass == null) {
      throw new Exception("The keyStoreFile and keyStorePass values "
          + "must be set in the '" + CONFIG + "' file.");
    }
    String trustStoreFile = props.getProperty("trustStoreFile");
    String sharedSecretKey = props.getProperty("sharedSecretKey");
    if (sharedSecretKey == null) {
      throw new Exception("The sharedSecretKey value must be set in the '"
                          + CONFIG + "' file.");
    }
    String driverClassName = props.getProperty("dbDriverClassName");
    String connectionString = props.getProperty("dbConnectionString");
    String dbTablePrefix = props.getProperty("dbTablePrefix");
    if (driverClassName == null) {
      throw new Exception("The dbDriverClassName property must be set "
          + "in the '" + CONFIG + "' file.");
    }
    if (connectionString == null) {
      throw new Exception("The dbConnectionString property must be set "
          + "in the '" + CONFIG + "' file.");
    }
    // Init the database class.
    Database.init(driverClassName, connectionString, dbTablePrefix);
    // Test the database settings before starting the actual server.
    if (!Database.testConnection()) {
      throw new Exception("The connection to the database could not be "
        + "established. Please verify the 'dbDriverClassName' and"
        + "'dbConnectionString' settings in the '" + CONFIG
        + "' file are correct.");
    }
    // Be sure the tables exist.
    Database.createTables();
    // Create and start a new auth-server instance.
    AuthServer server = new AuthServer(listenPort, keyStoreFile, keyStorePass,
        trustStoreFile, sharedSecretKey, inetAddr);
    server.start();
    String m = "Timadorus auth server started. Accepting connections on "
        + (addr != null ? addr : "all interfaces") + " on port "
        + listenPort + ". Type help for a list of available commands.";
    System.out.println(m);
    // Start a simple command-line interpreter.
    new Interpreter().start(server);
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
