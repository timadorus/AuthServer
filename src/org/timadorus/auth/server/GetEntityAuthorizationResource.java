/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.GetEntityAuthorizationResource.java
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

import javax.security.auth.login.CredentialException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.timadorus.auth.AuthManager;
import org.timadorus.auth.Entity;

/**
 * @author sage
 *
 */
@Path("/getAuthorization")
public class GetEntityAuthorizationResource extends AuthenticatedResource {
  
  @Context HttpHeaders headers;  // set by inspection for each request
  
  @GET 
  @Produces("text/plain")
  @Path("{identifier}")
  public String getEntitiesMessage(@PathParam("identifier") String identifierPath) {
    
    Principal user = validateUser(headers);    
    if (user == null) { return ""; } // for a non-valid user there are no entitiesPerPrincipal to choose from.
    
      
    Entity entity = AuthManager.getEntityByIdentifier(identifierPath);
    try {
      return AuthManager.getAuthToken(user, entity);
    } catch (CredentialException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return "failure!";
    }  
           
  }

}
