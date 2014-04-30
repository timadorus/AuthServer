package org.timadorus.auth.util;

import com.sun.jersey.core.util.Base64;

/**
 * Contains utility methods.
 * 
 * @author
 *  Torben Könke
 */
public final class Util {
  /**
   * Make StyleCheck happy.
   */
  private Util() {
  }

  /**
   * Extracts the username-portion of a BASE64-encoded "Basic Access
   * Authentication"-String.
   * 
   * @param authentication
   *  The basic access authentication header sent as part of an HTTP
   *  request.
   * @return
   *  The username-portion of the specified "Basic Access Authentication"-
   *  String.
   * @throws IllegalArgumentException
   *  The authentication parameter is null.
   */
  public static String getBasicAccessUsername(String authentication) {
    if (authentication == null) {
      throw new IllegalArgumentException("authentication");
    }
    if (!authentication.startsWith("Basic ")) {
      throw new IllegalArgumentException("The specified string is not a valid "
          + "basic access authentication string.");
    }
    try {
      authentication = authentication.substring("Basic ".length());
      String[] values = new String(Base64.base64Decode(authentication))
                            .split(":");
      return values[0];
    } catch (Exception e) {
      throw new IllegalArgumentException("The specified string is not a valid "
          + "basic access authentication string.", e);
    }
  }
}
