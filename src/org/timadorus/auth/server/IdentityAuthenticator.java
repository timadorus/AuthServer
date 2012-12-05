/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.IdentityAuthenticator.java
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

import javax.security.auth.login.LoginException;



/**
 * This interface is used to define modules that know how to authenticate
 * an identity based on provided credentials. The credentials must be of
 * a form recognizable to the implementation. Note that each application
 * context has its own instances of <code>IdentityAuthenticator</code>s.
 * Typically implementations of <code>IdentityAuthenticator</code> are
 * invoked by a containing <code>IdentityManager</code>.
 * <p>
 * All implementations of <code>IdentityAuthenticator</code> must have a
 * constructor that accepts an instance of <code>java.util.Properties</code>.
 * The provided properties are part of the application's configuration.
 * 
 * This interface duplicates the interface {@code com.sun.sgs.auth.IdentityAuthenticator}, but
 * is replicated here to avoid dependency on the full SGS distribution for the 
 * auth server.
 * 
 */
public interface IdentityAuthenticator {

    /**
     * Authenticates the given credentials. The returned <code>Identity</code>
     * is valid, but has not yet been notified as logged in.
     *
     * @param credentials the <code>IdentityCredentials</code> to authenticate
     *
     * @return an authenticated <code>Identity</code>
     *
     * @throws LoginException if authentication fails
     */
    Principal authenticateIdentity(IdentityCredentials credentials)
        throws LoginException;

}
