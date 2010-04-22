/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.BasicUserPasswordCredential.java
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

import javax.security.auth.login.LoginException;

import com.sun.jersey.core.util.Base64;


/**
 * @author sage
 *
 */
public class BasicUserPasswordCredential implements IdentityCredentials {

  protected String password;
  protected String username;
  
  
  public BasicUserPasswordCredential(String credData) throws LoginException {
    String authText = "";
    
      authText = Base64.base64Decode(credData);
    String[] elems = authText.split(":");    
    if (elems.length != 2) { 
      throw new LoginException("http basic credentials not valid");
    }
    
    username = elems[0];
    password = elems[1];
  
  }
  
  /**
   * @see org.timadorus.auth.IdentityCredentials#getCredentialsType()
   *
   * @return
   */
  @Override
  public String getCredentialsType() {
    return "HTTPBasicUserPassword";
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

}
