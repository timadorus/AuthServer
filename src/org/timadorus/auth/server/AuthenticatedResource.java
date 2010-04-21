/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.AuthenticatedResource.java
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
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.timadorus.auth.AuthManager;

/**
 * @author sage
 *
 * I am sure there must be a cleaner way to let the servlet container handle the 
 * authentication, but this works.
 *
 * method will return null if no principal could be validated from the header.
 */
public class AuthenticatedResource {

  protected Principal validateUser(HttpHeaders headers) {

    List<String> headerLines = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
    if (headerLines.size() != 1) { return null; }

    String[] elems = headerLines.get(0).split(" ");
    
    if (elems.length != 2) { return null; }

    return AuthManager.authenticate(AuthManager.AuthType.valueOf(elems[0]), elems[1]);
  }

}
