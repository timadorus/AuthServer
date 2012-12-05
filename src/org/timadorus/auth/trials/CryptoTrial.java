/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.CryptoTrial.java
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

package org.timadorus.auth.trials;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.timadorus.auth.shared.Base64;

/** Do some test with the crypto stuff
 * @author sage
 *
 */
public final class CryptoTrial {

  private CryptoTrial () { }
  
  static String keyData = "foobarsecret1234";  // needs to be 128/8: 16 Bytes long

  public static void main(String[] args) throws IOException {
    String transmission = encrypt(keyData);
    // String transmission = "FbL19iuXFhq/ay9k5Co+TbM2PKD2drpXPrp/6uzUQf8aIigP83kq43q4semu6lBO";
    System.out.println("Transmission: " + transmission);
    decrypt(keyData, transmission);
  }

  /**
   * 
   * @param keyStr
   * @param transmission
   * @throws IOException of the transmission contains invalid characters, i.e. not decodable base64 values 
   */
  public static void decrypt(String keyStr, String transmission) throws IOException {
    
    final int charsForTime = 13;
    
    byte[] data = null;
        data = Base64.decode(transmission);
    byte[] original = null;
    
    
    byte[] aesKeyData = keyStr.getBytes();

    try {
      SecretKeySpec keySpec = new SecretKeySpec(aesKeyData, "AES");      
      
      Cipher cipher = Cipher.getInstance("AES");      
      cipher.init(Cipher.DECRYPT_MODE, keySpec);

      
      original = cipher.doFinal(data);
      
      
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BadPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 

    System.out.println("decrypted string: '" + Arrays.toString(original) + "'");

    String tokenStr = new String(original);
    
    System.out.println("readable: " + tokenStr);
    
    Long tokenDate = Long.decode(tokenStr.substring(0, charsForTime));
    
    long timeDelta = new Date().getTime() - tokenDate;
    
    System.out.println("t_delta: " + timeDelta);
    String tokenName = tokenStr.substring(charsForTime);
    System.out.println("Name: " + tokenName);
  }

 
  
  public static String encrypt(String keyStr) {
    
    String identifier = "Boromir";
    Charset charset = Charset.forName("latin1");
    
    byte[] timeBytes = Long.toString(new Date().getTime()).getBytes(charset);
    byte[] ident = identifier.getBytes(charset);
    
    byte[] input = new byte[timeBytes.length + ident.length];
    System.arraycopy(timeBytes, 0, input, 0, timeBytes.length);
    System.arraycopy(ident, 0, input, timeBytes.length, ident.length);

    System.out.println("orig string: '" + Arrays.toString(input) + "'");

    byte[] retval = null;
    
    byte[] aesKeyData = keyStr.getBytes();

    try {
      // KeyGenerator kgen = KeyGenerator.getInstance("AES");
      // kgen.init(128); // 192 and 256 bits may not be available

      SecretKeySpec keySpec = new SecretKeySpec(aesKeyData, "AES");      

      Cipher cipher = Cipher.getInstance("AES");      
      cipher.init(Cipher.ENCRYPT_MODE, keySpec);

      
      retval = cipher.doFinal(input);
      
      
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BadPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    return Base64.encodeBytes(retval);
  }

}
