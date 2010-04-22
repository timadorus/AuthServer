/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.SimpleAuthorizer.java
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

package org.timadorus.auth;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.sun.jersey.core.util.Base64;



/**
 * @author sage
 *
 */
public class SimpleAuthorizer implements SubjectAuthorizer {

  /** default length of AES keys in bits
   * 
   */
  protected static final int DEFAULT_KEY_LENGTH = 128;
 
  /**
   * @author sage
   *
   */
  public class SimpleEntity implements Entity {

    String name;
    SimpleEntity parent;
    Map<String, Entity> children = new HashMap<String, Entity>(); 
    
    public SimpleEntity(String name, SimpleEntity parent) {
      this.name = name;
      this.parent = parent;
      if (parent != null) {
        parent.children.put(name, parent);
      }
    }

    /**
     * @see org.timadorus.auth.Entity#getName()
     *
     * @return
     */
    @Override
    public String getLabel() {
      return name;
    }

    /**
     * 
     * @see org.timadorus.auth.Entity#getIdentifier()
     *
     * @return
     */
    @Override
    public String getIdentifier() {
      
      if (parent == null) { return name; }
      
      String parentId = parent.getIdentifier();
      if (parentId == null) { return name; }
        
      StringBuilder retval = new StringBuilder(parentId);
      retval.append(":").append(name);
      
      return retval.toString();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SimpleEntity)) { return false; }
      SimpleEntity se = (SimpleEntity) obj;
      
      return (name == null) ? se.name == null : name.equals(se.name);
      
    }

    /**
     * @see java.lang.Object#hashCode()
     *
     * @return
     */
    @Override
    public int hashCode() {
      return name.hashCode();
    }

    
    @Override
    public boolean isLeaf() {
      return (children.size() == 0);
    }


  }
  
  protected Map<Principal, List<SimpleEntity>> entitiesPerPrincipal = new HashMap<Principal, List<SimpleEntity>>();
  
  protected SimpleEntity treeRoot = new SimpleEntity(null, null);
  
  private byte[] keyData = null;
  
  private Charset charset = Charset.forName("latin1");
  
  /** set the secret shared with the game server
   * 
   */
  public void setSharedSecret(String data) {
    final int bitsPerByte = 8;
    
    if (data.length() != (DEFAULT_KEY_LENGTH / bitsPerByte)) {
      throw new InvalidParameterException("Key lenght must be " + (DEFAULT_KEY_LENGTH / bitsPerByte) + " bytes");
    }
    keyData = data.getBytes();
  }
  
  /**
   * @see org.timadorus.auth.SubjectAuthorizer#getAuthToken(org.timadorus.auth.Entity, java.security.Principal)
   *
   * returns a base64 encoded authenticator string, containing the current time as milliseconds since the epoch.
   * 
   * TODO: This is a design weakness: a crypto-attacker could guess the first 5-6 Bytes of the message.
   *  
   * @param entity
   * @param princ
   * @return
   * @throws IllegalArgumentException if the key data has not been set.
   */
  @Override
  public String getAuthToken(Principal princ, Entity entity) {
    
    byte[] timeBytes = Long.toString(new Date().getTime()).getBytes(charset);
    byte[] ident = entity.getIdentifier().getBytes(charset);
    
    byte[] input = new byte[timeBytes.length + ident.length];
    System.arraycopy(timeBytes, 0, input, 0, timeBytes.length);
    System.arraycopy(ident, 0, input, timeBytes.length, ident.length);

    System.out.println("orig string: '" + Arrays.toString(input) + "'");

    byte[] retval = null;
    
    if (keyData == null) { throw new IllegalArgumentException("Key data not set"); } 
    
    try {
      KeyGenerator kgen = KeyGenerator.getInstance("AES");
      kgen.init(DEFAULT_KEY_LENGTH); // 192 and 256 bits may not be available

      SecretKeySpec keySpec = new SecretKeySpec(keyData, "AES");      

      Cipher cipher = Cipher.getInstance("AES");      
      cipher.init(Cipher.ENCRYPT_MODE, keySpec);

      
      retval = cipher.doFinal(input);
      
      
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BadPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 
    
    return new String(Base64.encode(retval));
  }

  /**
   * @see org.timadorus.auth.SubjectAuthorizer#getEntities(java.security.Principal)
   *
   * @param princ
   * @return
   */
  @Override
  public List<Entity> getEntities(Principal princ) {
    return getEntities(princ, null);
  }

  /**
   * @see org.timadorus.auth.SubjectAuthorizer#getEntities(java.security.Principal, org.timadorus.auth.Entity)
   *
   * @param princ
   * @param parent
   * @return
   */
  @Override
  public List<Entity> getEntities(Principal princ, Entity parent) {
    List<SimpleEntity> candidate = entitiesPerPrincipal.get(princ);
    if (candidate == null) { return new ArrayList<Entity>(); }

    List<Entity> retlist = new ArrayList<Entity>();
    if (parent == null) { parent = treeRoot; } // handle for root of entities
    
    // filter for parent
    for (SimpleEntity entity : candidate) {
      if (entity.parent.equals(parent)) {
        retlist.add(entity);
      }
    }
    
    return retlist;
  }

  
  
  /**
   * 
   * @see org.timadorus.auth.SubjectAuthorizer#getEntityByIdentifier(java.lang.String)
   *
   * @param ident
   * @return
   */
  @Override
  public Entity getEntityByIdentifier(String identPath) throws IllegalArgumentException {
    if (identPath == null) { return null; }
    
    SimpleEntity parent = treeRoot;
    String[] idents = identPath.split(":");
    for (String ident : idents) {
      
      parent = new SimpleEntity(ident, parent);
    }
    return parent;
  }

  /**
   * 
   * @param entity
   */
  public void addEntity(Principal princ, SimpleEntity entity) throws IllegalArgumentException {
        
    List<SimpleEntity> entList = entitiesPerPrincipal.get(princ);
    if (entList == null) { 
      entList = new ArrayList<SimpleEntity>(); 
      entitiesPerPrincipal.put(princ, entList);
    }    
    entList.add(entity);
    
  }

  /** create a new entity.
   * 
   * this is an insert action, while getEntityByIdentifier() will only read existing entries.
   * 
   * @param name
   * @param parent
   */
  public SimpleEntity createEntity(String name, SimpleEntity parent) {
    if (parent == null) { parent = treeRoot; }
    SimpleEntity retval = new SimpleEntity(name, parent);
    
    return retval;
  }
  
}
