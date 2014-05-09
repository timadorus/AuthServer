package org.timadorus.auth.server;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.timadorus.auth.util.Crypto;

/**
 * Provides an easy-to-use API for accessing and managing the auth database
 * tables.
 * 
 * The SQL dialect used is the Apache Derby SQL dialect. If, at some point,
 * another database provider is to be used, the SQL statements in this class
 * must be altered accordingly.
 * 
 * @author Torben Könke
 */
public final class Database {
  /**
   * The datasource.
   */
  private static final BasicDataSource DATA_SOURCE = new BasicDataSource();

  /**
   * The prefix to use for the database tables.
   */
  private static String prefix = "";
  
  /**
   * Make CheckStyle happy.
   */
  private Database() {
  }

  /**
   * Initializes the database class.
   * 
   * @param className
   *          The classname of the database driver.
   * @param connectionString
   *          The connection-string for the database.
   * @param prefix
   *          The prefix to use for the database tables.
   * @throws IllegalArgumentException
   *           The className parameter is null, or the connectionString
   *           parameter is null.
   */
  public static void init(String className, String connectionString, String prefix) {
    if (className == null) {
      throw new IllegalArgumentException("className");
    }
    if (connectionString == null) {
      throw new IllegalArgumentException("connectionString");
    }
    DATA_SOURCE.setDriverClassName(className);
    DATA_SOURCE.setUrl(connectionString);
    if (prefix != null) {
      Database.prefix = prefix;
    }
  }

  /**
   * Determines whether the configured database settings are valid.
   * 
   * @return true if connections to the database can be established;
   *         Otherwise false.
   */
  public static boolean testConnection() {
    Connection con = null;
    try {
      con = getConnection();
      return true;
    } catch (Exception e) {
      return false;
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (SQLException e) {
      }
    }
  }
  
  /**
   * Determines whether the auth tables already exist.
   * 
   * @return
   *  true if the auth tables exist; Otherwise false.
   * @throws SQLException
   *           The connection to the database could not be established.
   */
  public static boolean tablesExist() throws SQLException {
    Connection con = getConnection();
    ResultSet rs = null;
    try {
      DatabaseMetaData dbmd = con.getMetaData();
      // With Derby, the default database schema applied to all SQL statements
      // is the same as the user id provided, even if the schema does not exist.
      // If no user id is provided, the default schema 'APP' is used.
      rs = dbmd.getTables(null, dbmd.getUserName().toUpperCase(),
                          (prefix + "users").toUpperCase(), null);
      return rs.next();
    } catch (SQLException e) {
      return false;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Creates the tables in the auth database.
   * 
   * @return true if the tables were created; Otherwise false.
   * @throws SQLException
   *           The connection to the database could not be established.
   */
  public static boolean createTables() throws SQLException {
    String usersTable = "CREATE TABLE " + prefix + "users"
        + "(user_id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT "
        + "user_id_pk PRIMARY KEY, name VARCHAR(255) NOT NULL CONSTRAINT "
        + "name_unique UNIQUE, password VARCHAR(255) NOT NULL, "
        + "admin SMALLINT DEFAULT 0 NOT NULL, last_login TIMESTAMP, "
        + "flags INTEGER DEFAULT 0 NOT NULL)";
    
    String entitiesTable = "CREATE TABLE " + prefix + "entitiesPerUser"
        + "(entity_id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT "
        + "entity_id_pk PRIMARY KEY, user_id INT NOT NULL CONSTRAINT "
        + "user_foreign_key REFERENCES " + prefix + "users ON DELETE CASCADE, "
        + "name VARCHAR(255) NOT NULL, last_login TIMESTAMP, "
        + "flags INTEGER DEFAULT 0 NOT NULL)";
    
    String attributesTable = "CREATE TABLE " + prefix + "attributesPerEntity"
        + "(entity_id INT NOT NULL CONSTRAINT entity_foreign_key REFERENCES "
        + prefix + "entitiesPerUser ON DELETE CASCADE, name VARCHAR(255) "
        + "NOT NULL, value VARCHAR(255) NOT NULL, PRIMARY KEY(entity_id, name))";
    
    Statement statement = null;
    Connection con = getConnection();
    try {
      statement = con.createStatement();
      statement.execute(usersTable);
      statement = con.createStatement();
      statement.execute(entitiesTable);
      statement = con.createStatement();
      statement.execute(attributesTable);
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println(e);
      return false;
    } finally {
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Determines whether the specified user exists in the auth table.
   * 
   * @param username
   *          The name of the user to look up.
   * @return true if the user exists; Otherwise false.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null.
   */
  public static boolean userExists(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    String sqlStatement = "SELECT * from " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.execute();
      
      resultSet = statement.getResultSet();
      return resultSet.next();
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Retrieves the data of the user with the the specified username.
   * 
   * @param username
   *    The name of the user whose data to retrieve.
   * @return
   *    An initialized instance of the User class containing the user's
   *    data, or null if no such user exists.
   * @throws SQLException
   *    The connection to the database could not be established, or
   *    another database-related error occurred.
   * @throws IllegalArgumentException
   *    The username parameter is null.
   */
  public static User getUser(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    String sqlStatement = "SELECT * from " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.execute();
      rs = statement.getResultSet();
      if (!rs.next()) {
        return null;
      }
      return new User(rs.getInt("user_id"), rs.getString("name"),
                      rs.getString("password"), rs.getShort("admin") != 0,
                      rs.getTimestamp("last_login"), rs.getInt("flags"));
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  
  /**
   * Retrieves the password-hash for the specified user.
   * 
   * @param username
   *          The name of the user whose password to retrieve.
   * @return
   *          The password-hash of the specified user or null if no such user
   *          exists.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null.
   */
  public static String getPassword(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    String sqlStatement = "SELECT password from " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.execute();
      resultSet = statement.getResultSet();
      if (!resultSet.next()) {
        return null;
      }
      return resultSet.getString("password");
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Creates a new user in the auth table with the specified name and password.
   * 
   * @param username
   *          The name of the user to create.
   * @param password
   *          The password of the user to create.
   * @param admin
   *          Set to true to create an administrator, or false to create a normal
   *          user.
   * @param flags
   *          The flags to set on the user.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null, or the password parameter is null.
   * @throws IllegalStateException
   *          A user with the specified name already exists in the auth table.
   */
  public static void createUser(String username, String password, boolean admin,
    int flags) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    if (userExists(username)) {
      throw new IllegalStateException("A user with the name of '" + username
        + "' already exists in the auth table.");
    }
    String sqlStatement = "INSERT INTO " + prefix + "users (name, password, admin, flags) "
        + "VALUES (?, ?, ?, ?)";
    Connection con = null;
    PreparedStatement statement = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.setString(2, Crypto.createHash(password));
      statement.setShort(3, (short) (admin ? 1 : 0));
      statement.setInt(4, flags);
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Insertion failed.");
      }
    } finally {
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Edits the user in the auth table with the specified username.
   * 
   * @param username
   *  The username of the user to edit.
   * @param password
   *  The new password for the user, or null to keep the existing password.
   * @param admin
   *  true to make the user an administrator, false to make the user a
   *  normal user, or null to keep the current setting.
   * @param flags
   *  The new flags to set on the user, or null to keep the existing flags.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *  The usename parameter is null.
   * @throws IllegalStateException
   *  A user with the specified username does not exist in the auth table.
   */
  public static void updateUser(String username, String password, Boolean admin,
    Integer flags) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (!userExists(username)) {
      throw new IllegalStateException("A user with the name of '" + username
        + "' does not exist in the auth table.");
    }
    int stack = 1;
    StringBuilder b = new StringBuilder("UPDATE " + prefix + "users SET ");
    if (password != null) {
      b.append("password = ?");
      stack++;
    }
    if (admin != null) {
      if (stack > 1) {
        b.append(", ");
      }
      b.append("admin = ?");
      stack++;
    }
    if (flags != null) {
      if (stack > 1) {
        b.append(", ");
      }
      b.append("flags = ?");
      stack++;
    }
    b.append(" WHERE name = ?");
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(b.toString());
      statement.setString(stack--, username);
      if (flags != null) {
        statement.setInt(stack--, flags);
      }
      if (admin != null) {
        statement.setShort(stack--, (short) (admin.booleanValue() ? 1 : 0));
      }
      if (password != null) {
        statement.setString(stack--, password);
      }
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Update failed.");
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Deletes the user in the auth table with the specified username.
   * 
   * @param username
   *          The name of the user to delete.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null.
   * @throws IllegalStateException
   *          A user with the specified username does not exist in the auth table.
   */
  public static void deleteUser(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (!userExists(username)) {
      throw new IllegalStateException("A user with the name of '" + username
        + "' does not exist in the auth table.");
    }
    String sqlStatement = "DELETE FROM " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Delete failed.");
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Determines whether the specified user is an administrator.
   * 
   * @param username
   *  The name of the user.
   * @return
   *  true if the specified user is an administrator; Otherwise false.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   */
  public static boolean isAdmin(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    String sqlStatement = "SELECT admin FROM " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.execute();
      resultSet = statement.getResultSet();
      if (!resultSet.next()) {
        return false;
      }
      return resultSet.getShort("admin") != 0;
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Returns a list of the names of all users in the auth table.
   * 
   * @param filter
   *           A character pattern, which is a character string that
   *           includes one or more wildcards. Supported wildcards are
   *           % for any number (zero or more) of characters in the
   *           corresponding position and _ for one character in the
   *           corresponding position in the character expression.
   *           This parameter can be null.
   * @return A list of names of all users in the auth table.
   * @throws SQLException
   *           The connection to the database could not be established, or
   *           another error occurred.
   */
  public static List<String> listUsers(String filter) throws SQLException {
    String sqlStatement = "SELECT name from " + prefix + "users"
        + ((filter != null) ? " WHERE name LIKE ?" : "");
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      if (filter != null) {
        statement.setString(1, filter);
      }
      statement.execute();
      resultSet = statement.getResultSet();
      List<String> names = new LinkedList<String>();
      while (resultSet.next()) {
        names.add(resultSet.getString("name"));
      }
      return names;
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Truncates the auth table, i.e. deletes all entries from it.
   * 
   * @throws SQLException
   *           The connection to the database could not be established, or
   *           another database-related error occurred.
   */
  public static void truncate() throws SQLException {
    String sqlStatement = "TRUNCATE TABLE " + prefix + "users";
    Connection con = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.createStatement();
      statement.executeUpdate(sqlStatement);
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Creates a new entity to the specified user's set of entities.
   * 
   * @param username
   *  The name of the user to add a new entity for.
   * @param entity
   *  The name of the new entity to add.
   * @param flags
   *  The flags to set on the new entity.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *  The username parameter is null, or the entity parameter is null.
   * @throws IllegalStateException
   *  A user with the specified name does not exist in the auth table, or
   *  an entity with the specified name already exists in the specified
   *  user's set of entities.
   */
  public static void createEntity(String username, String entity, int flags)
      throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    if (entityExists(userId, entity)) {
      throw new IllegalStateException("The entity '" + entity + "' already exists.");
    }
    String sqlStatement = "INSERT INTO " + prefix
        + "entitiesPerUser (user_id, name, flags) "
        + "VALUES (?, ?, ?)";
    Connection con = null;
    PreparedStatement statement = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      statement.setString(2, entity);
      statement.setInt(3, flags);
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Insertion failed.");
      }
    } finally {
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Deletes the specified entity of the user with the specified username.
   * 
   * @param username
   *          The name of the user whose entity to delete.
   * @param entity
   *          The name of the entity to delete.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null, or the entity parameter is null.
   * @throws IllegalStateException
   *          A user with the specified username does not exist in the auth table,
   *          or the entity with the specified name does not exist.
   */
  public static void deleteEntity(String username, String entity) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    String sqlStatement = "DELETE FROM " + prefix
        + "entitiesPerUser WHERE user_id = ? AND name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      statement.setString(2, entity);
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Delete failed.");
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Edits the entity of the user in the auth table with the specified username.
   * 
   * @param username
   *  The username of the user whose entity to edit.
   * @param entity
   *  The name of the entity to edit.
   * @param newName
   *  The new name of the entity, or null to keep the current name.
   * @param flags
   *  The new flags to set on the entity, or null to keep the existing flags.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *  The usename parameter is null, or the entity parameter is null.
   * @throws IllegalStateException
   *  A user with the specified username does not exist in the auth table.
   */
  public static void updateEntity(String username, String entity, String newName,
    Integer flags) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    int stack = 2;
    StringBuilder b = new StringBuilder("UPDATE " + prefix + "entitiesPerUser SET ");
    if (newName != null) {
      b.append("name = ?");
      stack++;
    }
    if (flags != null) {
      if (stack > 1) {
        b.append(", ");
      }
      b.append("flags = ?");
      stack++;
    }
    b.append(" WHERE user_id = ? AND name = ?");
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(b.toString());
      statement.setString(stack--, entity);
      statement.setInt(stack--, userId);
      if (flags != null) {
        statement.setInt(stack--, flags);
      }
      if (newName != null) {
        statement.setString(stack--, newName);
      }
      if (statement.executeUpdate() == 0) {
        throw new SQLException("Update failed.");
      }
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Determines whether the specified entity exists in the auth table.
   * 
   * @param username
   *          The name of the user whose entities to lookup.
   * @param entity
   *          The name of the entity to lookup.
   * @return true if the entity exists; Otherwise false.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalStateException
   *          The user with the specified name does not exist in the auth table.
   * @throws IllegalArgumentException
   *          The username parameter is null, or the entity parameter is null.
   */
  public static boolean entityExists(String username, String entity) throws SQLException {
    return entityExists(getUserId(username), entity);
  }
  
  /**
   * Determines whether the specified entity exists in the auth table.
   * 
   * @param userId
   *          The unique id of the user whose entities to lookup.
   * @param entity
   *          The name of the entity to lookup.
   * @return true if the entity exists; Otherwise false.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The entity parameter is null.
   */
  private static boolean entityExists(int userId, String entity) throws SQLException {
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    String sqlStatement = "SELECT * from " + prefix
        + "entitiesPerUser WHERE user_id = ? AND name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      statement.setString(2, entity);
      statement.execute();
      
      resultSet = statement.getResultSet();
      return resultSet.next();
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Retrieves a list of the entities of the user with the specified username.
   * 
   * @param username
   *  The name of the user whose entities to retrieve.
   * @return
   *  A list of the user's entities.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *  The username parameter is null.
   * @throws IllegalStateException
   *  A user with the specified name does not exist in the auth table.
   */
  public static List<Entity> listEntities(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    int userId = getUserId(username);
    String sqlStatement = "SELECT * from " + prefix
        + "entitiesPerUser WHERE user_id = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      rs = statement.executeQuery();
      List<Entity> ents = new LinkedList<Entity>();
      while (rs.next()) {
        Entity e = new Entity(rs.getInt("entity_id"), rs.getInt("user_id"),
                              rs.getString("name"), rs.getTimestamp("last_login"),
                              rs.getInt("flags"));
        ents.add(e);
      }
      return ents;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Retrieves the data of the entity of the user with the the specified username.
   * 
   * @param username
   *    The name of the user whose entity's data to retrieve.
   * @param entity
   *    The name of the entity whose data to retrieve.
   * @return
   *    An initialized instance of the Entity class containing the entity's
   *    data, or null if no such entity exists.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *    The username parameter is null, or the entity parameter is null.
   * @throws IllegalStateException
   *  A user with the specified name does not exist in the auth table.
   */
  public static Entity getEntity(String username, String entity) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    String sqlStatement = "SELECT * from " + prefix
        + "entitiesPerUser WHERE user_id = ? AND name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      statement.setString(2, entity);
      statement.execute();
      rs = statement.getResultSet();
      if (!rs.next()) {
        return null;
      }
      return new Entity(rs.getInt("entity_id"), rs.getInt("user_id"),
                        rs.getString("name"), rs.getTimestamp("last_login"),
                        rs.getInt("flags"));
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Retrieves the attributes of the entity of the user with the the
   * specified username.
   * 
   * @param username
   *  The name of the user whose entity's data to retrieve.
   * @param entity
   *  The name of the entity whose data to retrieve.
   * @return
   *  A map of key/value pairs containing the entity's attributes.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *    The username parameter is null, or the entity parameter is null.
   * @throws IllegalStateException
   *  A user with the specified name does not exist in the auth table.
   */
  public static Map<String, String> getAttributes(String username, String entity)
      throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    Entity ent = getEntity(username, entity);
    String sqlStatement = "SELECT * from " + prefix
        + "attributesPerEntity WHERE entity_id = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    Map<String, String> attr = new HashMap<String, String>();
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, ent.getId());
      statement.execute();
      rs = statement.getResultSet();
      while (rs.next()) {
        attr.put(rs.getString("name"), rs.getString("value"));
      }
      return attr;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }
  
  /**
   * Returns the unique user-id for the user with the specified username.
   * 
   * @param username
   *  The name of the user whose id to lookup.
   * @return
   *  The user-id of the user with the specified username.
   * @throws SQLException
   *  The connection to the database could not be established, or
   *  another database-related error occurred.
   * @throws IllegalArgumentException
   *  The username parameter is null.
   * @throws IllegalStateException
   *  A user with the specified username does not exist in the auth table.
   */
  private static int getUserId(String username) throws SQLException {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    String sqlStatement = "SELECT user_id from " + prefix + "users WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.execute();
      resultSet = statement.getResultSet();
      if (!resultSet.next()) {
        throw new IllegalStateException("A user with the name of '" + username
                            + "' does not exist in the auth table.");
      }
      return resultSet.getInt("user_id");
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Returns a new Connection instance from the pool of connections.
   * 
   * @return A Connection instance for the configured database.
   * @throws SQLException
   *           The connection could not be established.
   */
  private static Connection getConnection() throws SQLException {
    return DATA_SOURCE.getConnection();
  }
}
