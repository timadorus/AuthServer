package org.timadorus.auth.server;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.timadorus.auth.util.Crypto;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Implements an HTTP-Request filter that is executed whenever a resource of
 * the webserver is requested.
 * 
 * This is used to verify the client's credentials which are passed as part
 * of the HTTP request (HTTP Basic Access Authentication).
 * 
 * @author
 *  Torben Könke
 */
public class SecurityFilter implements ContainerRequestFilter  {
  /**
   * The HTTP status code for unauthorized access.
   */
  private static final int HTTP_NOT_AUTHORIZED = 401;
  
  /**
   * Logging facility.
   */
  private static final Logger LOG = Logger.getLogger(SecurityFilter.class.getName());
  
  @Context HttpServletRequest httpServletRequest;
  
  /**
   * A filter method that is being executed whenever an HTTP request comes in.
   * 
   * @param request
   *  Represents the HTTP request.
   * @return
   *  The HTTP request.
   */
  @Override
  public ContainerRequest filter(ContainerRequest request) {
    if (!authenticate(request)) {
      LOG.warning("Failed auth attempt for resource '"
               + request.getPath()
               + "' from <" + httpServletRequest.getRemoteAddr()
               + "> with authorization-header '"
               + request.getHeaderValue(ContainerRequest.AUTHORIZATION) + "'.");
      throw new WebApplicationException(HTTP_NOT_AUTHORIZED);
    }
    return request;
  }
  
  /**
   * Authenticates the credentials included in the specified request's HTTP
   * headers.
   * 
   * @param request
   *  The request whose credentials to verify.
   * @return
   *  true if the passed credentials are valid; Otherwise false.
   */
  private boolean authenticate(ContainerRequest request) {
    // Extract authentication credentials from the HTTP headers.
    String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
    if (authentication == null) {
      return false;
    }
    if (!authentication.startsWith("Basic ")) {
      return false;
    }
    authentication = authentication.substring("Basic ".length());
    String[] values = new String(Base64.base64Decode(authentication)).split(":");
    if (values.length < 2) {
      return false;
    }
    String username = values[0];
    String password = values[1];
    if ((username == null) || (password == null)) {
      return false;
    }
    // Validate the extracted credentials.
    try {
      String dbPasswordHash = Database.getPassword(username);
      // The user doesn't exist.
      if (dbPasswordHash == null) {
        return false;
      }
      // Compare the password hashes.
      return Crypto.validatePassword(password, dbPasswordHash);
    } catch (Exception e) {
      return false;
    }
  }
}
