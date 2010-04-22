/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.Identity.java
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

package org.timadorus.auth;

import java.security.Principal;

/**
 * @author sage
 *
 */
public class Identity implements Principal {

  private String name;
  
  /**
   * 
   * @param name
   */
  public Identity(String name) {
    super();
    this.name = name;
  }

  /**
   * @see java.security.Principal#getName()
   *
   * @return
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Identity)) { return false; }
    Identity id = (Identity) obj;
    
    return (getName() == null) ? (id.getName() == null) : getName().equals(id.getName());
  }

  /**
   * @see java.lang.Object#hashCode()
   *
   * @return
   */
  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   *
   * @return
   */
  @Override
  public String toString() {
    return getName().toString();
  }

  
}
