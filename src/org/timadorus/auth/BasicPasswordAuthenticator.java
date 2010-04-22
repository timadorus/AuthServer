/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.BasicPasswordAuthenticator.java
 *                                                                       *
 * Project:           TimadorusAuthServer
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

package org.timadorus.auth;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;


/**
 * @author sage
 *
 */
public class BasicPasswordAuthenticator implements IdentityAuthenticator {

  Map<String, String> pwCombos = new HashMap<String, String>();
  
  public void addUser(String user, String password) {
    pwCombos.put(user, password);
  }
  
  /**
   * @see org.timadorus.auth.IdentityAuthenticator#authenticateIdentity(org.timadorus.auth.IdentityCredentials)
   *
   * @param credentials
   * @return
   * @throws LoginException
   */
  @Override
  public Principal authenticateIdentity(IdentityCredentials credentials) throws LoginException {
    if (!(credentials instanceof BasicUserPasswordCredential)) { 
      throw new LoginException("invalid credential"); 
    }
    BasicUserPasswordCredential creds = (BasicUserPasswordCredential) credentials;
    
    String pw = pwCombos.get(creds.getUsername());
    if ((pw == null) || (!pw.equals(creds.getPassword()))) { 
      throw new LoginException("unknown username/password"); 
    } 
    
    return new Identity(creds.getUsername());
  }

}
