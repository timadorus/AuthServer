/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.ListEntitiesResource.java
 *                                                                       *
 * Project:           TimadorusAuthServer
 * Programm:
 * Function:
 * Documentation file:
 *
 * This file is distributed under the GNU Public License 2.0
 * See the file Copying for more information
 *
 * copyright (c) 2010 Lutz Behnke <lutz.behnke@gmx.de>
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.timadorus.auth.server;

import java.security.Principal;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;


/**
 * @author sage
 *
 */
@Path("/listEntities")
public class ListEntitiesResource extends AuthenticatedResource {

  /** header of each request.
   * 
   */
  @Context HttpHeaders headers;

  @GET 
  @Produces("text/plain")
  public String getEntityRoot() {
    return getEntities(null);
  }
  
  @GET 
  @Produces("text/plain")
  @Path("{identifier}")
  public String getEntities(@PathParam("identifier") String identifierPath) {
    
    Principal user = validateUser(headers);    
    if (user == null) { return ""; } // for a non-valid user there are no entitiesPerPrincipal to choose from.

    List<Entity> entities = AuthManager.getEntities(user, AuthManager.getEntityByIdentifier(identifierPath));
    
    StringBuilder retStr = new StringBuilder();
    for (Entity entity : entities) {
      retStr.append(entity.getLabel());
      retStr.append(":");

      retStr.append(entity.isLeaf() ? "L" : "T");  // leaf or (sub-)tree?
      retStr.append(":");
      
      retStr.append(entity.getIdentifier());
      retStr.append("\n");
    }
    
    return "param = " + identifierPath + "\n" + retStr.toString();  
           
  }
}
