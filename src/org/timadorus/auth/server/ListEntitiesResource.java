package org.timadorus.auth.server;

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.codec.binary.Base64;

/**
 * The resource class which handles the 'listEntities' HTTP request.
 * 
 * @author
 *  Torben Könke
 *
 */
@Path("/listEntities")
public class ListEntitiesResource extends Resource {
  /**
   * The method which is executed when the /listEntities resource is being
   * requested.
   * 
   * @return
   *  A base64-encoded colon-separated string of the respective user's entities.
   * @throws Exception
   *  The entities of the user making the request could not be fetched.
   */
  @GET 
  @Produces("text/plain")
  public String listEntities() throws Exception {
    // Get Username from Request headers.
    String username = getUsername();
    // Fetch entities from DB and return them as a colon-separated base64-encoded string.
    StringBuilder builder = new StringBuilder();
    Iterator<String> iter = Database.listEntities(username).iterator();
    while (iter.hasNext()) {
      builder.append(iter.next());
      if (iter.hasNext()) {
        builder.append(":");
      }
    }
    return Base64.encodeBase64String(builder.toString().getBytes());
  }
}
