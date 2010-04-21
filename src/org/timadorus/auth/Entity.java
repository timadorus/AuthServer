/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.Entity.java
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

/** Object that represents a target for authentication in the SGS game.
 * 
 * This may be a avatar, a certain capability or other element of 
 * limited access in the game.
 * 
 * Classes that implement this interface must also implement the hashCode()
 * and equals() methods.
 * 
 * @author sage
 *
 */
public interface Entity {
  
  /** retrieve a human readable label
   * 
   * this text should be displayed to humans
   * 
   * The label must not contain a colon character (':') in clear text (it may be encoded
   * as urlencode '%3A').
   * 
   * @return label of the object.
   */
  String getLabel();
  
  /** retrieve a unique identifier.
   * 
   * This may or may not be unique for a system. It must be possible to
   * recreate an entity from providing this string to the Authorizer
   * 
   * @return a unique identifier of the entity.
   */
  String getIdentifier();
  
  /** test for the existence of child entities.
   * 
   * an entity in a flat entity system will always return true;
   * 
   * @return false if the entity has child entities, true otherwise. 
   */
  boolean isLeaf();
}
