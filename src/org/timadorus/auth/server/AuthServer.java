package org.timadorus.auth.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.ssl.SSLSelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.util.net.jsse.JSSEImplementation;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * Implements the actual HTTP server-component of the auth-server.
 * 
 * @author
 *  sage,
 *  Torben Könke
 */
public class AuthServer {
  /**
   * The port on which the server is accepting HTTP requests.
   */
  private int port;

  /**
   * The file that contains the key-store to use for the SSL server.
   */
  private String keyStoreFile;

  /**
   * The password for the key-store that is being used.
   */
  private String keyStorePassword;
  
  /**
   * The file that contains the trust-store to use for the SSL server.
   */
  private String trustStoreFile;
  
  /**
   * The secret key that is shared with the gameserver.
   */
  private String sharedSecretKey;
  
  /**
   * The network address the server is bound to.
   */
  private InetAddress inetAddress;
  
  /**
   * The NIO selector-thread.
   */
  private SelectorThread threadSelector;
  
  /**
   * Initializes a new instance of the AuthServer class.
   * 
   * @param port
   *  The port to accept HTTP requests on.
   * @param keyStoreFile
   *  The file containing the key-store to use for the SSL server.
   * @param keyStorePassword
   *  The password for the key-store.
   * @param trustStoreFile
   *  The file containing the trust-store to use for the SSL server. If
   *  a trust-store is not needed, this parameter can be null.
   * @param sharedSecretKey
   *  The secret key that is shared with the gameserver.
   * @param inetAddress
   *  The address to bind the server to. If this is null, the server will
   *  accept connections on any of its interfaces.
   * @throws IllegalArgumentException
   *  The port parameter is not a valid port, or the keyStoreFile parameter
   *  is null, or the sharedSecretKey parameter is null.
   */
  public AuthServer(int port, String keyStoreFile, String keyStorePassword,
    String trustStoreFile, String sharedSecretKey, InetAddress inetAddress) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port.");
    }
    if (keyStoreFile == null) {
      throw new IllegalArgumentException("keyStoreFile");
    }
    if (sharedSecretKey == null) {
      throw new IllegalArgumentException("sharedSecretKey");
    }
    this.port = port;
    this.keyStoreFile = keyStoreFile;
    this.keyStorePassword = keyStorePassword;
    this.trustStoreFile = trustStoreFile;
    this.sharedSecretKey = sharedSecretKey;
    this.inetAddress = inetAddress;
  }

  /**
   * Creates the NIO selector for the Grizzly HTTP server.
   * 
   * @return
   *  An initialized instance of the SSLSelectorThread class.
   * @throws IOException
   *  The selector-thread could not be created.
   */
  private SSLSelectorThread createSelector() throws IOException {
    ServletAdapter adapter = new ServletAdapter();
    Map<String, String> initParams =  new HashMap<String, String>();
    // This _must_ be set up to point to the proper package.
    initParams.put("com.sun.jersey.config.property.packages",
                   getClass().getPackage().getName());    
    for (Map.Entry<String, String> e : initParams.entrySet()) {
      adapter.addInitParameter(e.getKey(), e.getValue());
    }
    adapter.setServletInstance(new ServletContainer());
    adapter.setContextPath("/");
    // Add the shared secret-key as an init parameter so that it can be
    // accessed from the Resource classes, serving the HTTP requests.
    adapter.addInitParameter("sharedSecretKey", sharedSecretKey);
    // Set up request filtering for convenient verification of credentials.
    adapter.addInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                             SecurityFilter.class.getName());

    final SSLSelectorThread selectorThread = new SSLSelectorThread();
    try {
      selectorThread.setSSLImplementation(new JSSEImplementation());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
    // Setup SSL.
    SSLConfig sslConfig = new SSLConfig(true);
    if (trustStoreFile != null) {
      sslConfig.setTrustStoreFile(trustStoreFile);
    }
    sslConfig.setKeyStoreFile(keyStoreFile);
    sslConfig.setKeyStorePass(keyStorePassword);
    selectorThread.setSSLConfig(sslConfig);
    if (selectorThread.getSSLContext() == null) {
      selectorThread.setSSLContext(sslConfig.createSSLContext());
    }

    selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
    if (inetAddress != null) {
      selectorThread.setInet(inetAddress);
    }
    selectorThread.setPort(port);
    selectorThread.setAdapter(adapter);
    try {
      // Starts the server on a separate thread.
      selectorThread.listen();
    } catch (InstantiationException e) {
      IOException innerE = new IOException();
      innerE.initCause(e);
      throw innerE;
    }
    return selectorThread;
  }

  /**
   * Starts the auth-server.
   * 
   * @throws IOException
   *  An error occurred while starting the auth-server.
   */
  public void start() throws IOException {
    threadSelector = createSelector();
  }

  /**
   * Stops the auth-server.
   */
  public void stop() {
    threadSelector.stopEndpoint();
  }

  /**
   * Gets the port on which the HTTP server is running.
   * 
   * @return
   *  The port on which the HTTP server is running.
   */
  public int getPort() {
    return port;
  }
  
  /**
   * Gets the key-store file.
   * 
   * @return
   *  The key-store file.
   */
  public String getKeyStoreFile() {
    return keyStoreFile;
  }
  
  /**
   * Gets the password for the key-store.
   * 
   * @return
   *  The password for the key-store.
   */
  public String getKeyStorePassword() {
    return keyStorePassword;
  }
  
  /**
   * Gets the trust-store file.
   * 
   * @return
   *  The trust-store file or null if no trust-store has been set.
   */
  public String getTrustStoreFile() {
    return trustStoreFile;
  }
}
