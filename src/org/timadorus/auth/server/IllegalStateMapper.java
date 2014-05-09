package org.timadorus.auth.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.gson.GsonBuilder;

/**
 * Maps the IllegalStateException class to an HTTP Internal Server Error status response.
 *  
 * @author Torben Könke
 */
@Provider
public class IllegalStateMapper implements ExceptionMapper<IllegalStateException> {
  /**
   * The HTTP Internal Server Error status code.
   */
  private static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

  @Override
  public Response toResponse(IllegalStateException ex) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("error", HTTP_STATUS_INTERNAL_SERVER_ERROR);
    map.put("text", ex.getMessage());
    return Response
            .status(HTTP_STATUS_INTERNAL_SERVER_ERROR)
            .entity(new GsonBuilder().disableHtmlEscaping().create().toJson(map))
            .type("application/json")
            .build();
    }
}
