package org.timadorus.auth.server;

import java.sql.Timestamp;

/**
 * Represents an entity of the auth-database entitiesPerUser table.
 * 
 * @author Torben Könke
 */
public class Entity {
  /**
   * The unique id of the entity.
   */
  private final int id;
  /**
   * The unique id of the user the entity belongs to.
   */
  private final int userId;
  /**
   * The name of the entity.
   */
  private final String name;
  /**
   * The timestamp at which the entity last logged in.
   */
  private final Timestamp lastLogin;
  /**
   * The flags set on the entity.
   */
  private final int flags;

  /**
   * Initializes a new instance of the Entity class.
   * 
   * @param name
   *  The name of the entity.
   * @param flags
   *  The flags to set on the entity.
   * @throws IllegalArgumentException
   *  The name parameter is null.
   */
  public Entity(String name, int flags) {
    this(-1, -1, name, null, flags);
  }
  
  /**
   * Initializes a new instance of the Entity class.
   * 
   * @param id
   *  The id of the entity.
   * @param userId
   *  The id of the user the entity belongs to.
   * @param name
   *  The name of the entity.
   * @param lastLogin
   *  The timestamp at which the entity last logged in.
   * @param flags
   *  The flags to set on the entity.
   * @throws IllegalArgumentException
   *  The name parameter is null.
   */
  public Entity(int id, int userId, String name, Timestamp lastLogin, int flags) {
    if (name == null) {
      throw new IllegalArgumentException("name");
    }
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.flags = flags;
    this.lastLogin = lastLogin;
  }
  
  /**
   * Gets the id of the entity.
   * @return
   *  The id of the entity.
   */
  public int getId() {
    return id;
  }
  
  /**
   * Gets the id of the user the entity belongs to.
   * @return
   *  The id of the user the entity belongs to.
   */
  public int getUserId() {
    return userId;
  }
  
  /**
   * Gets the name of the entity.
   * @return
   *  The name of the entity.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the timestamp at which the entity last logged in.
   * @return
   *  The timestamp at which the entity last logged in.
   */
  public Timestamp getLastLogin() {
    return lastLogin;
  }
  
  /**
   * Gets the flags set on the entity.
   * @return
   *  The flags set on the entity.
   */
  public int getFlags() {
    return flags;
  }
}
