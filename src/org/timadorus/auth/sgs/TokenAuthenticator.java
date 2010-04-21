/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.sgs.TokenAuthenticator.java
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

package org.timadorus.auth.sgs;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

import org.timadorus.auth.Base64;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.IdentityImpl;
import com.sun.sgs.impl.auth.NamePasswordCredentials;

/**
 * @author sage
 *
 */
public class TokenAuthenticator implements IdentityAuthenticator {

  private byte[] keyData = null;
  
  // number of seconds that a token is valid
  private int grace = 60;
  
  /**
   * 
   * @param data
   */
  public void setKeyData(byte[] data) {
    keyData = data.clone();
  }
  
  /** set the grace period for the token
   * 
   * The grace period is the time a token is valid.
   * By default the value is set to 60 seconds.
   * 
   * @param grace amount of seconds that a token is valid
   */
  public void setGrace(int grace) {
    this.grace = grace;
  }
  
  /**
   * @see com.sun.sgs.auth.IdentityAuthenticator#authenticateIdentity(com.sun.sgs.auth.IdentityCredentials)
   *
   * @param arg0
   * @return
   * @throws LoginException
   */
  @Override
  public Identity authenticateIdentity(IdentityCredentials credentials) throws LoginException {
    
 // make sure that we were given the right type of credentials
    if (!(credentials instanceof NamePasswordCredentials)) {
        throw new CredentialException("unsupported credentials");
    }
    NamePasswordCredentials creds = (NamePasswordCredentials) credentials;

    String name = creds.getName();
    String token = new String(creds.getPassword());
    
    byte[] retval = null;
    
    if (keyData == null) { throw new LoginException("shared secret data not set. Call the admin"); } 

    byte[] input;
    try {
      input = Base64.decode(token);
    } catch (IOException e) {
      throw new LoginException("Invalid base64 encoding in token");
    }

    try {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(128); // 192 and 256 bits may not be available

      SecretKeySpec keySpec = new SecretKeySpec(keyData,"AES");      

      Cipher cipher = Cipher.getInstance("AES");      
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      
      retval = cipher.doFinal(input);
            
    } catch (NoSuchAlgorithmException e) {
      throw new LoginException("invalid crypto configuration. Call the admin ('"+e.getMessage()+"')");
    } catch (NoSuchPaddingException e) {
      throw new LoginException("invalid crypto configuration. Call the admin ('"+e.getMessage()+"')");
    } catch (InvalidKeyException e) {
      throw new LoginException("invalid crypto configuration. Call the admin ('"+e.getMessage()+"')");
    } catch (IllegalBlockSizeException e) {
      throw new LoginException("invalid crypto configuration. Call the admin ('"+e.getMessage()+"')");
    } catch (BadPaddingException e) {
      throw new LoginException("invalid crypto configuration. Call the admin ('"+e.getMessage()+"')");
    } 

    String tokenStr = new String(retval);
    
    Long tokenDate = Long.decode(tokenStr.substring(0, 13));

    long tDelta = new Date().getTime() - tokenDate;

    // check age of token
    if (tDelta > (grace*1000)) {
      throw new LoginException("token to old");
    }
    if (tDelta < 0) {
      throw new LoginException("token timestamp lies in the future");
    }
    
    String tokenName = tokenStr.substring(13);
    
    if (!tokenName.equals(name)) {
      throw new LoginException("name in token does not match name supplied by client");
    }

    return new IdentityImpl(name);
  }


  /**
   * {@inheritDoc}
   */
  public String [] getSupportedCredentialTypes() {
      return new String [] { NamePasswordCredentials.TYPE_IDENTIFIER };
  }


  
}
