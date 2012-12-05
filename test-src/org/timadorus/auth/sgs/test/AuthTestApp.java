/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.sgs.test.AuthTestApp.java
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

package org.timadorus.auth.sgs.test;

import java.util.Properties;

import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;

/**
 * @author sage
 *
 */
public class AuthTestApp implements AppListener {

  /**
   * @see com.sun.sgs.app.AppListener#initialize(java.util.Properties)
   *
   * @param arg0
   */
  @Override
  public void initialize(Properties arg0) {
    // TODO Auto-generated method stub

  }

  /**
   * @see com.sun.sgs.app.AppListener#loggedIn(com.sun.sgs.app.ClientSession)
   *
   * @param arg0
   * @return
   */
  @Override
  public ClientSessionListener loggedIn(ClientSession arg0) {
    System.out.println(arg0.getName() + " has successfully logged in");
    return null;
  }

}
