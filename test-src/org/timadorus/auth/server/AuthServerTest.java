/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server/AuthServerTest.java
 *                                                                       *
 * Project:           TimadorusAuthServer
 *
 * This file is distributed under the GNU Public License 2.0
 * See the file Copying for more information
 *
 * copyright (c) 2012 Lutz Behnke <lutz.behnke@gmx.de>
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.timadorus.auth.server;

import static org.junit.Assert.assertEquals;

import java.security.Principal;

import org.junit.Test;
import org.timadorus.auth.server.SimpleAuthorizer.SimpleEntity;

/**
 * @author sage
 *
 */
public class AuthServerTest {

  /**
   * 
   * @throws Exception for any exception not caught by the test
   */
  @Test
  public void testServerStartStop() throws Exception {
    
    AuthServer server = new AuthServer();
    
    
    BasicPasswordAuthenticator authenticator = new BasicPasswordAuthenticator();
    authenticator.addUser("fii", "br");
    
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
    
    System.out.println("Starting grizzly...");
    server.start();

    Thread.sleep(1000);
    
    server.stop();
  }
  
  
  /**
   * 
   * @throws Exception for any exception not caught by the test.
   */
  @Test
  public void testParseArgs() throws Exception {
    org.timadorus.auth.server.AuthServer server = new AuthServer();
    
    String[] args = {"-p", "4711"};
    server.parseArgs(args);
    
    assertEquals(4711, server.getPort());
    
  }
}
