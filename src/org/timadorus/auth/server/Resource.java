package org.timadorus.auth.server;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.timadorus.auth.util.Util;

/**
 * The base class from which the other resource classes derive. 
 *
 * @author Torben Könke
 */
public abstract class Resource {
  /**
   * The HTTP headers of the HTTP request.
   */
  @Context HttpHeaders headers;
  
  /**
   * The Servlet configuration.
   */
  @Context ServletConfig config;
  
  /**
   * Gets the username of the user making the HTTP request.
   * 
   * @return
   *  The username of the user performing the HTTP request.
   */
  protected String getUsername() {
    // Get the username from HTTP request headers.
    String auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
    return Util.getBasicAccessUsername(auth);
  }
  
  /**
   * Determines whether the user making the HTTP request is an administrator.
   * 
   * @return
   *  true if the user performing the HTTP request is an administrator;
   *  Otherwise false.
   * @throws Exception
   *  An unexpected error occurred.
   */
  protected boolean isAdmin() throws Exception {
    return Database.isAdmin(getUsername());
  }
}
