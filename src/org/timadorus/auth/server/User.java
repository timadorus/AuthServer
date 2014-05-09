package org.timadorus.auth.server;

import java.sql.Timestamp;

/**
 * Represents a user of the auth-database users table.
 * 
 * @author Torben Könke
 */
public class User {
  /**
   * The unique id of the user.
   */
  private final int id;
  /**
   * The name of the user.
   */
  private final String name;
  /**
   * The password hash of the user.
   */
  private final String hash;
  /**
   * Determines whether the user is an administrator.
   */
  private final boolean admin;
  /**
   * The timestamp at which the user last logged in.
   */
  private final Timestamp lastLogin;
  /**
   * The flags set on the user.
   */
  private final int flags;
  
  /**
   * Initializes a new instance of the User class.
   * 
   * @param name
   *  The name of the user.
   * @param hash
   *  The password hash of the user.
   * @param admin
   *  true if the user is an administrator; Otherwise false.
   * @param flags
   *  The flags to set on the user.
   * @throws IllegalArgumentException
   *  The name parameter is null, or the hash parameter is null.
   */
  public User(String name, String hash, boolean admin, int flags) {
    this(-1, name, hash, admin, null, flags);
  }
  
  /**
   * Initializes a new instance of the User class.
   * 
   * @param id
   *  The id of the user.
   * @param name
   *  The name of the user.
   * @param hash
   *  The password hash of the user.
   * @param admin
   *  true if the user is an administrator; Otherwise false.
   * @param lastLogin
   *  The timestamp at which the user last logged in.
   * @param flags
   *  The flags to set on the user.
   * @throws IllegalArgumentException
   *  The name parameter is null, or the hash parameter is null.
   */
  public User(int id, String name, String hash, boolean admin, Timestamp lastLogin, int flags) {
    if (name == null) {
      throw new IllegalArgumentException("name");
    }
    if (hash == null) {
      throw new IllegalArgumentException("hash");
    }
    this.id = id;
    this.name = name;
    this.hash = hash;
    this.admin = admin;
    this.flags = flags;
    this.lastLogin = lastLogin;
  }
  
  /**
   * Gets the id of the user.
   * @return
   *  The id of the user.
   */
  public int getId() {
    return id;
  }
  
  /**
   * Gets the name of the user.
   * @return
   *  The name of the user.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the password hash of the user.
   * @return
   *  The password hash of the user.
   */
  public String getHash() {
    return hash;
  }
  
  /**
   * Gets whether the user is an administrator.
   * @return
   *  true if the user is an administrator; Otherwise false.
   */
  public boolean isAdmin() {
    return admin;
  }
  
  /**
   * Gets the timestamp at which the user last logged in.
   * @return
   *  The timestamp at which the user last logged in.
   */
  public Timestamp getLastLogin() {
    return lastLogin;
  }
  
  /**
   * Gets the flags set on the user.
   * @return
   *  The flags set on the user.
   */
  public int getFlags() {
    return flags;
  }
}
