package org.timadorus.auth.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;
import org.timadorus.auth.util.Crypto;

/**
 * Provides an easy-to-use API for accessing and managing the auth database
 * tables.
 * 
 * The SQL dialect used is the Apache Derby SQL dialect. If, at some point,
 * another database provider is to be used, you must alter the SQL statements
 * in this class accordingly.
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
   * Creates the tables in the auth database.
   * 
   * @return true if the tables were created; Otherwise false.
   * @throws SQLException
   *           The connection to the database could not be established.
   */
  public static boolean createTables() throws SQLException {
    String usersTable = "CREATE TABLE " + prefix + "users"
        +  "(user_id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
        +  "   CONSTRAINT user_id_pk PRIMARY KEY, "
        +  " name VARCHAR(255) NOT NULL CONSTRAINT user_name_const UNIQUE, "
        +  " password VARCHAR(255) NOT NULL) ";
    
    String entitiesTable = "CREATE TABLE " + prefix + "entitiesPerUser "
        +  "(entity_id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
        +  "   CONSTRAINT entity_id_pk PRIMARY KEY, "
        +  " user_id INT NOT NULL, "
        +  " label VARCHAR(80) NOT NULL) ";
    Statement statement = null;
    Connection con = getConnection();
    try {
      statement = con.createStatement();
      statement.execute(usersTable);
      statement = con.createStatement();
      statement.execute(entitiesTable);
      return true;
    } catch (SQLException e) {
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
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The username parameter is null, or the password parameter is null.
   * @throws Exception
   *          A user with the specified name already exists in the auth table.
   */
  public static void createUser(String username, String password) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    if (userExists(username)) {
      throw new Exception("A user with the name of '" + username
        + "' already exists in the auth table.");
    }
    String sqlStatement = "INSERT INTO " + prefix + "users (name, password) "
        + "VALUES (?, ?)";
    Connection con = null;
    PreparedStatement statement = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, username);
      statement.setString(2, Crypto.createHash(password));
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
   *          The username of the user to edit.
   * @param password
   *          The new password for the user.
   * @throws SQLException
   *          The connection to the database could not be established, or
   *          another database-related error occurred.
   * @throws IllegalArgumentException
   *          The usename parameter is null, or the password parameter is null.
   * @throws Exception
   *          A user with the specified username does not exist in the auth table.
   */
  public static void editPassword(String username, String password) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    if (!userExists(username)) {
      throw new Exception("A user with the name of '" + username
        + "' does not exist in the auth table.");
    }
    String sqlStatement = "UPDATE " + prefix + "users SET password = ? "
        + "WHERE name = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setString(1, Crypto.createHash(password));
      statement.setString(2, username);
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
   * @throws Exception
   *          A user with the specified username does not exist in the auth table.
   */
  public static void deleteUser(String username) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (!userExists(username)) {
      throw new Exception("A user with the name of '" + username
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
   * Returns a list of the names of all users in the auth table.
   * 
   * @return A list of names of all users in the auth table.
   * @throws SQLException
   *           The connection to the database could not be established, or
   *           another error occurred.
   */
  public static List<String> listUsers() throws SQLException {
    String sqlStatement = "SELECT name from " + prefix + "users";
    Connection con = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.createStatement();
      resultSet = statement.executeQuery(sqlStatement);
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
   * @throws IllegalArgumentException
   *  The username parameter is null, or the entity parameter is null.
   * @throws Exception
   *  A user with the specified name does not exist in the auth table, or
   *  an entity with the specified name already exists in the specified
   *  user's set of entities.
   */
  public static void createEntity(String username, String entity) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    if (entityExists(userId, entity)) {
      throw new Exception("entity already exists");
    }
    String sqlStatement = "INSERT INTO " + prefix
        + "entitiesPerUser (user_id, label) "
        + "VALUES (?, ?)";
    Connection con = null;
    PreparedStatement statement = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      statement.setString(2, entity);
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
   * @throws Exception
   *          A user with the specified username does not exist in the auth table,
   *          or the entity with the specified name does not exist.
   */
  public static void deleteEntity(String username, String entity) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    int userId = getUserId(username);
    String sqlStatement = "DELETE FROM " + prefix
        + "entitiesPerUser WHERE user_id = ? AND label = ?";
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
   * @throws Exception
   *          The user with the specified name does not exist in the auth table.
   * @throws IllegalArgumentException
   *          The username parameter is null, or the entity parameter is null.
   */
  public static boolean entityExists(String username, String entity) throws SQLException, Exception {
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
        + "entitiesPerUser WHERE user_id = ? AND label = ?";
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
   * @throws IllegalArgumentException
   *  The username parameter is null.
   * @throws Exception
   *  A user with the specified name does not exist in the auth table.
   */
  public static List<String> listEntities(String username) throws Exception {
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    int userId = getUserId(username);
    String sqlStatement = "SELECT label from " + prefix
        + "entitiesPerUser WHERE user_id = ?";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      con = Database.getConnection();
      statement = con.prepareStatement(sqlStatement);
      statement.setInt(1, userId);
      resultSet = statement.executeQuery();
      List<String> names = new LinkedList<String>();
      while (resultSet.next()) {
        names.add(resultSet.getString("label"));
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
   * Returns the unique user-id for the user with the specified username.
   * 
   * @param username
   *  The name of the user whose id to lookup.
   * @return
   *  The user-id of the user with the specified username.
   * @throws IllegalArgumentException
   *  The username parameter is null.
   * @throws Exception
   *  A user with the specified username does not exist in the auth table.
   */
  private static int getUserId(String username) throws Exception {
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
        throw new Exception("A user with the name of '" + username
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
