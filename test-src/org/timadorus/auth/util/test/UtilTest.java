package org.timadorus.auth.util.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.timadorus.auth.util.Util;

/**
 * Contains unit-tests for the Util class.
 * 
 * @author Torben Könke
 */
public class UtilTest {
  /**
   * Extracts the username-portion of a BASE64-encoded "Basic Access
   * Authentication"-String.
   */
  @Test
  public void basicAccessUsername() {
    String authHeaderField = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    String username = Util.getBasicAccessUsername(authHeaderField);
    
    assertEquals("Aladdin", username);
  }
  
  /**
   * Attempts to extract the username-portion of an invalid BASE64-encoded
   * string and asserts an exception is being raised.
   */
  @Test(expected = IllegalArgumentException.class)
  public void invalidBasicAccessString() {
    String authHeaderField = "This is totally invalid.";
    Util.getBasicAccessUsername(authHeaderField);
  }
}
