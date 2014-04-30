package org.timadorus.auth.server;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.timadorus.auth.util.Crypto;
import org.timadorus.auth.util.Util;

/**
 * The resource class which handles the 'getAuthToken' HTTP request.
 * 
 * @author
 *  Torben Könke
 *
 */
@Path("/getAuthToken")
public class GetAuthTokenResource {
  /**
   * The HTTP headers of the HTTP request.
   */
  @Context HttpHeaders headers;
  
  /**
   * The Servlet configuration.
   */
  @Context ServletConfig config;

  /**
   * The method which is executed when the /getAuthToken resource is being
   * requested.
   * 
   * @param entity
   *  The entity to request an auth-token for, as a base64-encoded string.
   * @return
   *  The auth-token for the requested entity.
   * @throws Exception 
   */
  @GET 
  @Produces("text/plain")
  @Path("{entity}")
  public String getAuthToken(@PathParam("entity") String entity) throws Exception {    
    // Get Username from HTTP request headers.
    String auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
    String username = Util.getBasicAccessUsername(auth);
    // Make sure the entity actually exists.
    if (!Database.entityExists(username, entity)) {
      throw new Exception("The entity does not exist.");
    }
    // Get the shared secret key.
    String sharedSecretKey = config.getInitParameter("sharedSecretKey");
    // Generate and return an encrypted auth-token. The auth-token has the
    // form 'User:Entity:Timestamp'.
    String authToken = username + ":" + entity + ":"
        + Long.toString(getUnixTime());
    byte[] encrypted = Crypto.aesEncrypt(authToken.getBytes("UTF-8"),
                      sharedSecretKey);
    return Base64.encodeBase64String(encrypted);
  }
  
  /**
   * Returns the unix time, that is, the number of seconds that have passed
   * since 01.01.1970.
   * 
   * @return
   *  The current time.
   */
  private long getUnixTime() {
    return System.currentTimeMillis() / 1000L;
  }
}
