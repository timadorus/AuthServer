/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.SubjectAuthorizer.java
 *                                                                       *
 * Project:           TimadorusAuthServer
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

import javax.security.auth.login.CredentialException;


/** interface for searching of subjects authorizing the access of a user to a subject.
 * 
 * the entitiesPerPrincipal may either be organized as a flat list, or in a tree like hierarchy
 * of arbitrary depth. If the parent is not provided, it is meant to be null and 
 * the search is started at the root.
 * 
 * The search and caching semantics of the code calling implementation of this
 * interface assume that there are comparatively few 
 * @author sage
 *
 */
public interface SubjectAuthorizer {
  
  /**
   * 
   * @param princ
   * @return
   */
  List<Entity> getEntities(Principal princ);
  
  /**
   * 
   * @param princ
   * @param parent
   * @return
   */
  List<Entity> getEntities(Principal princ, Entity parent);
  
  /** retrieve an authenticator to be shown to the game server. 
   * 
   * @param entity
   * @param princ
   * @return 
   * @throws CredentialException if the principal is not allowed to get the authenticator.
   */
  String getAuthToken(Principal princ, Entity entity) throws CredentialException;
  
  /** create a Entity from an identifier, possibly a path of identifiers
   * 
   * the identifier is considered a path of identifiers, separated by colon (':').
   * 
   * if a chain is provided, only the last Entity in the chain is returned
   * 
   * @param ident
   * @return
   * @throws IllegalArgumentException if any of the idendifiers in the path is not a valid identifier path element.
   */
  Entity getEntityByIdentifier(String ident) throws IllegalArgumentException;
}
