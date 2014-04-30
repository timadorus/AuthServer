package org.timadorus.auth.client;

/**
 * The exception that is thrown when authentication fails.
 * 
 * @author Torben Könke
 */
public class AuthException extends RuntimeException {
  /**
   * The unique UID.
   */
  private static final long serialVersionUID = 8475541481706526635L;

  /**
   * Initializes a new instance of the AuthException class.
   * 
   * @param message
   *          The message of the exception.
   */
  public AuthException(String message) {
    super(message);
  }

  /**
   * Initializes a new instance of the AuthException class.
   * 
   * @param message
   *          The message of the exception.
   * @param inner
   *          The inner exception.
   */
  public AuthException(String message, Exception inner) {
    super(message, inner);
  }
}
