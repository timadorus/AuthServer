package org.timadorus.auth.client;

import java.net.InetSocketAddress;

import javax.crypto.SecretKey;

/**
 * Represents the response sent by the auth-server when an auth-token is
 * requested by a client.
 * 
 * @author
 *  Torben Könke
 */
public class AuthResponse {
  /**
   * The opaque auth-token which can be passed on to the gameserver specified
   * in this instance.
   */
  public final String authToken;
  
  /**
   * The gameserver for which the auth-token is valid.
   */
  public final InetSocketAddress gameServer;
  
  /**
   * The AES session-key for encrypting session-data between client and the
   * gameserver. If this is null, session-data encryption is disabled.
   */
  public final SecretKey sessionKey;
  
  /**
   * Initializes a new instance of the AuthResponse class.
   * 
   * @param authToken
   *  The opaque auth-token.
   * @param gameServer
   *  The gameserver endpoint for which the auth-token is valid.
   * @param sessionKey
   *  The session-key to use for session-encryption. This may be null if
   *  session-encryption is not used.
   * @throws IllegalArgumentException
   *  The authToken parameter is null, or the gameServer parameter is null.
   */
  public AuthResponse(String authToken, InetSocketAddress gameServer,
    SecretKey sessionKey) {
    if (authToken == null) {
      throw new IllegalArgumentException("authToken");
    }
    if (gameServer == null) {
      throw new IllegalArgumentException("gameServer");
    }
    this.authToken = authToken;
    this.gameServer = gameServer;
    this.sessionKey = sessionKey;
  }
}
