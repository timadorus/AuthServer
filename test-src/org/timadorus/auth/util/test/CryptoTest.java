package org.timadorus.auth.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.timadorus.auth.util.Crypto;

/**
 * Contains unit-tests for the Crypto class.
 * 
 * @author Torben Könke
 */
public class CryptoTest {
  /**
   * Encrypts and subsequently decrypts some data and ensures the decrypted data
   * equals the original data.
   * 
   * @throws Exception
   *  An unexpected error occurred.
   */
  @Test
  public void encryptData() throws Exception {
    String s = "This is a quite secret message indeed.";
    String password = "MySecretPassword";
    
    byte[] buffer = Crypto.aesEncrypt(s.getBytes(), password);
    byte[] decrypted = Crypto.aesDecrypt(buffer, password);
    String decryptedString = new String(decrypted);
    assertEquals(s, decryptedString);
  }
  
  /**
   * Ensures the encrypt method actually does _something_ with the input.
   * 
   * @throws Exception
   *  An unexpected error occurred.
   */
  @Test
  public void encryptActuallyDoesEncrypt() throws Exception {
    String password = "MySecretPassword";
    byte[] data = "This better be working.".getBytes();
    byte[] buffer = Crypto.aesEncrypt(data, password);
    assertThat(data, IsNot.not(IsEqual.equalTo(buffer)));
  }
  
  /**
   * Ensures hashed passwords are properly validated.
   */
  @Test
  public void validatePasswordHashing() {
    String password = "MyVerySecretPassword";
    String pwHash = Crypto.createHash(password);
    boolean shouldBeTrue = Crypto.validatePassword(password, pwHash);
    assertTrue(shouldBeTrue);
    boolean shouldBeFalse = Crypto.validatePassword(password,
                                                    Crypto.createHash("Wrong"));
    assertFalse(shouldBeFalse);
  }
}
