package org.timadorus.auth.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.gson.GsonBuilder;

/**
 * Maps the SecurityException class to an HTTP Forbidden status response.
 *  
 * @author Torben Könke
 */
@Provider
public class SecurityMapper implements ExceptionMapper<SecurityException> {
  /**
   * The HTTP Forbidden status code.
   */
  private static final int HTTP_STATUS_FORBIDDEN = 403;
  
  @Override
  public Response toResponse(SecurityException ex) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("error", HTTP_STATUS_FORBIDDEN);
    map.put("text", ex.getMessage());
    return Response
            .status(HTTP_STATUS_FORBIDDEN)
            .entity(new GsonBuilder().disableHtmlEscaping().create().toJson(map))
            .type("application/json")
            .build();
    }
}
