/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.AuthServer.java
 *                                                                       *
 * Project:           TimadorusAuthServer
 * Programm:
 * Function:
 * Documentation file:
 *
 * This file is distributed under the GNU Public License 2.0
 * See the file Copying for more information
 *
 * copyright (c) 2010 Lutz Behnke <lutz.behnke@gmx.de>
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.timadorus.auth.server;

import gnu.getopt.Getopt;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.timadorus.auth.AuthManager;
import org.timadorus.auth.Identity;
import org.timadorus.auth.JDBCBasicPWAuthenticator;
import org.timadorus.auth.SimpleAuthorizer;
import org.timadorus.auth.SimpleAuthorizer.SimpleEntity;

import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.ssl.SSLSelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.util.net.jsse.JSSEImplementation;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author sage
 *
 */
public class AuthServer {

  private static final int DEFAULT_PORT = 9998;
  
  protected Map<String, String> initParams =  new HashMap<String, String>();
  protected String host = "localhost";
  protected int port = DEFAULT_PORT;
  protected String truststorePath = "./";
  protected String serverKeySecret = "123456";
  protected SSLSelectorThread threadSelector;

  public SSLSelectorThread createSelector(Map<String, String> initParams) throws IOException {

    ServletAdapter adapter = new ServletAdapter();
    for (Map.Entry<String, String> e : initParams.entrySet()) {
      adapter.addInitParameter(e.getKey(), e.getValue());
    }

    adapter.setServletInstance(new ServletContainer());
    adapter.setContextPath("/");
    adapter.setResourcesContextPath("/");

    final SSLSelectorThread selectorThread = new SSLSelectorThread();
    try {
      selectorThread.setSSLImplementation(new JSSEImplementation());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
    
    SSLConfig sslConfig = new SSLConfig(true);
    sslConfig.setTrustStoreFile(truststorePath + "/cacert");
    sslConfig.setKeyStoreFile(truststorePath + "/server");
    sslConfig.setKeyStorePass(serverKeySecret);
    selectorThread.setSSLConfig(sslConfig);
    if (selectorThread.getSSLContext() == null) {      
      selectorThread.setSSLContext(sslConfig.createSSLContext());
    }
        
    selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
    selectorThread.setPort(port);
    selectorThread.setAdapter(adapter);
    

    try {
      selectorThread.listen();
    } catch (InstantiationException e) {
      IOException innerE = new IOException();
      innerE.initCause(e);
      throw innerE;
    }
    return selectorThread;
  }    


  public void parseArgs(String[] args) throws IOException, URISyntaxException {
    
    Getopt g = new Getopt("authserver", args, "h:p:t:s:");
    
    int c;
    boolean runUsage = false;
    String arg;
    
    while ((c = g.getopt()) != -1) {
        switch(c) {
          case 'h':
            arg = g.getOptarg();
            if (arg == null) {
              runUsage = true;
              System.err.println("argument for option h missing: name of the host to map.\n");
            } else {
              host = arg;
            }
            break;
          case 'p':
            arg = g.getOptarg();
            if (arg == null) {
              runUsage = true;
              System.err.println("argument for option p missing: port to listen on.\n");
            } else {
              host = arg;
            }
            int argi = Integer.parseInt(g.getOptarg());
            if (argi != 0) { port = argi; }
            break;
          case 't':
            arg = g.getOptarg();
            if (arg == null) {
              runUsage = true;
              System.err.println("argument for option t missing: path to the truststore.\n");
            } else {
              truststorePath = arg;
            }
            break;
          case 's':
            arg = g.getOptarg();
            if (arg == null) {
              runUsage = true;
              System.err.println("argument for option s missing: server key secret\n");
            } else {
              serverKeySecret = arg;
            }
            break;
          case '?':
            runUsage = true;
            break;
          default:
            System.err.print("unknown option " + c + "\n");
            runUsage = true;
          }
      }
    }
    
  public void start() throws IOException, URISyntaxException {

    initParams.put("com.sun.jersey.config.property.packages", "org.timadorus.auth.server");


    threadSelector = createSelector(initParams);
  }    

  public void stop() {
    threadSelector.stopEndpoint();    
  }
  
  public static void main(String[] args) throws IOException, URISyntaxException {
    AuthServer server = new AuthServer();
    
    
    /*
     *            determine the User information for authentication  
     */
    //BasicPasswordAuthenticator authenticator = new BasicPasswordAuthenticator();
    JDBCBasicPWAuthenticator authenticator = new JDBCBasicPWAuthenticator();
    /*
    try {
      authenticator.createTables("");
    } catch (SQLException e) {
      System.err.println("failed to create the tables");
      e.printStackTrace();
    }
    Principal user = authenticator.addUser("fii", "br");
    */
    
    Principal user = new Identity("fii");
    /*
     *            determine the Entity information for authorization  
     */
    SimpleAuthorizer authorizer = new SimpleAuthorizer();
    SimpleEntity lotrEntity = authorizer.createEntity("LotR", null);
    authorizer.addEntity(user, lotrEntity);
    SimpleEntity fellowEntity = authorizer.createEntity("Fellowship", lotrEntity);
    authorizer.addEntity(user, fellowEntity);
    SimpleEntity twoTowerEntity = authorizer.createEntity("Two_Towers", lotrEntity);
    authorizer.addEntity(user, twoTowerEntity);
    authorizer.addEntity(user, authorizer.createEntity("Boromir", fellowEntity));
    authorizer.addEntity(user, authorizer.createEntity("Aragorn", fellowEntity));
    authorizer.addEntity(user, authorizer.createEntity("Eowin", twoTowerEntity));
    
    authorizer.setSharedSecret("foobarsecret1234"); // must be 16 chars in length
    
    AuthManager.addAuthenticator(AuthManager.AuthType.Basic, authenticator);
    AuthManager.addAuthorizer(authorizer);
    
    server.parseArgs(args);
    System.out.println("Starting grizzly...");
    server.start();
  }
}