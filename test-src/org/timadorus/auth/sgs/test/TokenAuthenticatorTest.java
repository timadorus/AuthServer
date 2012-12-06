/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.sgs.test/TokenAuthenticatorTest.java
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

package org.timadorus.auth.sgs.test;

import static org.junit.Assert.fail;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import org.junit.Test;
import org.timadorus.auth.sgs.TokenAuthenticator;

import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;

/**
 * @author sage
 *
 */
public class TokenAuthenticatorTest {

  private class TestCredentials implements IdentityCredentials {

    @Override
    public String getCredentialsType() {
      return "TestCredentials";
    }
    
  }
  /** test the method authenticateIdentity().
   * 
   * @throws Exception for any exception that was not caught by the test.
   */
  @Test
  public void testAuthenticateIdentity() throws Exception {
    TokenAuthenticator authenticator = new TokenAuthenticator();

    boolean fail = false;

    try {
      authenticator.authenticateIdentity(null);
      fail = true;
    } catch(CredentialException cex) {
      if (!cex.getMessage().equals("credentials must not be null")) {
        fail = true;
      }
    }    
    if(fail) { fail("class did not detect unknown credential type"); }

    try {
      IdentityCredentials testCredentials = new TestCredentials();
      authenticator.authenticateIdentity(testCredentials);
      fail = true;
    } catch(CredentialException cex) {
      if (!cex.getMessage().equals("unsupported credentials type: TestCredentials")) {
        fail = true;
      }
    }    
    if(fail) { fail("class did not detect unknown credential type"); }

    char[] wrongPW = {(char)1, (char)2, (char)3, (char)4};
    IdentityCredentials credentials = new NamePasswordCredentials("dummy", wrongPW);
    
    try {
      authenticator.authenticateIdentity(credentials);
      fail = true;
    } catch(LoginException lex) {
      if (!lex.getMessage().equals("shared secret data not set. Call the admin")) {
        fail = true;
      }
    }
    
    if(fail) { fail("class did not detect missing key data"); }
  }
}
