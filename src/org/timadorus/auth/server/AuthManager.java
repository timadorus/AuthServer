/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.AuthManager.java
 * 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;




/**
 * @author sage
 *
 */
public class AuthManager {

  /**
   * @author sage
   *
   */
  public enum AuthType {
    Basic
  }

  Map<AuthType, List<IdentityAuthenticator>> authenticatorList = new HashMap<AuthType, List<IdentityAuthenticator>>();
  List<SubjectAuthorizer> authorizerList = new ArrayList<SubjectAuthorizer>();
  
  /** singleton instance
   * 
   */
  protected static AuthManager instance = null;
  
  /** create singleton instance if needed.
   * 
   * @return
   */
  protected static AuthManager getInstance() {
    if (instance == null) { instance = new AuthManager(); }
    
    return instance;
  }
  
  /** non-public CTOR for utility class.
   * 
   */
  protected AuthManager() { 
  }
  
  public static void addAuthenticator(AuthType type, IdentityAuthenticator auth) {
    getInstance().objAddAuthenticator(type, auth);
  }
  
  protected void objAddAuthenticator(AuthType type, IdentityAuthenticator auth) {
    List<IdentityAuthenticator> currList = authenticatorList.get(type);
    if (currList == null) { 
      currList = new ArrayList<IdentityAuthenticator>(); 
      authenticatorList.put(type, currList);
    }
    
    currList.add(auth);
    
  }
  
  /* try to authenticate a principal form HTTP-Auth header.
   * 
   * The list of mechanisms and algorithms this method can
   * handle depends on the configuration of the auth server, but
   * http-basic authentication will allways be handled.
   */
  public static Principal authenticate(AuthType type, String credData) {
    switch (type) {
    case Basic:
      return getInstance().authenticateBasic(credData);
    default:
      return null;
    }
    
  }

  private  Principal authenticateBasic(String credData) {
    IdentityCredentials cred = null;
    
    try {
      cred = new BasicUserPasswordCredential(credData);
    } catch (LoginException e) {
      // TODO: logging warn
      return null;
    }
    
    List<IdentityAuthenticator> baseAuthList = authenticatorList.get(AuthType.Basic);
    
    if (baseAuthList == null) { return null; }
    
    for (IdentityAuthenticator auth : baseAuthList) {
      Principal retval;
      try {
        retval = auth.authenticateIdentity(cred);
      } catch (LoginException e) {
        // TODO: logging warn
        return null;
      }
      if (retval != null) { return retval; }
    }
    
    return null;
  }

  /**
   * 
   * @param authorizer
   */
  public static void addAuthorizer(SimpleAuthorizer authorizer) {
    getInstance().objAddAuthorizer(authorizer);
  }

  /**
   * 
   * @param authorizer
   */
  protected void objAddAuthorizer(SimpleAuthorizer authorizer) {        
   authorizerList.add(authorizer);
  }

  public static List<Entity> getEntities(Principal princ, Entity parent) {
    return getInstance().objGetEntities(princ, parent);
  }

  public static List<Entity> getEntities(Principal princ) {
    return getInstance().objGetEntities(princ, null);
  }
  
  public List<Entity> objGetEntities(Principal princ, Entity parent) {
    List<Entity> retval = new ArrayList<Entity>();
    
    for (SubjectAuthorizer auth : authorizerList) {
      retval.addAll(auth.getEntities(princ, parent));
    }
    
    return retval;
  }

  public Entity objGetEntityByIdentifier(String identifierPath) {
    Entity retval = null;

    for (SubjectAuthorizer auth : authorizerList) {
      retval = auth.getEntityByIdentifier(identifierPath);
      if (retval != null) { return retval; }
    }
    return null;
  }
  
  /** the entitiy for identifier path
   * 
   * the method will return the result from the first authorizer to
   * supply a non-null result.
   * 
   * @param identifierPath
   * @return
   */
  public static Entity getEntityByIdentifier(String identifierPath) {
    return getInstance().objGetEntityByIdentifier(identifierPath);
  }
  
  private String objGetAuhtToken(Principal princ, Entity entity) throws CredentialException {
    String retval = null;

    for (SubjectAuthorizer auth : authorizerList) {
      retval = auth.getAuthToken(princ, entity);
      if (retval != null) { return retval; }
    }

    return null;
  }

  /** create a authentication and authorization token to be sent to
   * a usefull service.
   * 
   * @param princ
   * @param entity
   * @return
   * @throws CredentialException 
   */
  public static String getAuthToken(Principal princ, Entity entity) throws CredentialException {
    return getInstance().objGetAuhtToken(princ, entity);
  }
}
