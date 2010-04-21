/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.server.JDBCBasicPWAuthenticator.java
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.security.auth.login.LoginException;


/**
 * @author sage
 *
 */
public class JDBCBasicPWAuthenticator implements IdentityAuthenticator {

  // driver to use
  private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  // the database name
  private String dbName="jdbcDemoDB";
  // Derby connection URL to use
  private String connectionURL = "jdbc:derby:" + dbName + ";create=true";

  /* the actual database connection */
  private Connection dbConn = null;

  /* statements to be used */
  private PreparedStatement psGetUser;
  private PreparedStatement psGetEntities;
  
  protected Connection getDbConn() {
    if(dbConn == null) { initDB(); }
    
    // TODO: check for tables;
    
    return dbConn;
  }
  
  private void prepareStatements() throws SQLException {
    Connection conn = getDbConn();

    //  Prepare the insert statement to use
    psGetUser = dbConn.prepareStatement("select password from users where user_name = ? ");
    
    psGetEntities = dbConn.prepareStatement("select * from entitiesPerPrincipal where user_id = ? ");
    
  }
  
  /** create needed tables, using a prefix to the table names.
   * 
   * @param prefix
   * @throws SQLException 
   */
  public void createTables(String prefix) throws SQLException {
    Connection conn = getDbConn();
    Statement s;

    String createString = "CREATE TABLE " + prefix + "users "
    +  "(user_id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
    +  "   CONSTRAINT user_id_pk PRIMARY KEY, "
    +  " user_name VARCHAR(255) NOT NULL CONSTRAINT user_name_const UNIQUE, "
    +  " password VARCHAR(255) NOT NULL) ";

    s = conn.createStatement();
    s.execute(createString);
    
    createString = "CREATE TABLE " + prefix + "entitiesPerPrincipal "
    +  "(entity_id INT NOT NULL GENERATED ALWAYS AS IDENTITY "
    +  "   CONSTRAINT entity_id_pk PRIMARY KEY, "
    +  " user_id INT NOT NULL, "
    +  " parent_id INT, "
    +  " label VARCHAR(80) NOT NULL) ";
    
    s = conn.createStatement();
    s.execute(createString);

    prepareStatements();
    

  }

  /** init database
   * 
   * load the JDBC driver and open a connection to the database.
   * 
   */
  protected void initDB() {

    try {
      Class.forName(driver);
    } catch(java.lang.ClassNotFoundException e)     {
      System.err.println("ClassNotFoundException: " + e.getMessage());
      return;
    }

    try {
      // Create (if needed) and connect to the database
      dbConn = DriverManager.getConnection(connectionURL);
      System.out.println("Connected to database " + dbName);

      //  Beginning of the primary catch block: uses errorPrint method
    }  catch (Throwable e)  {
      System.err.println(" . . . exception thrown:");
      errorPrint(e);
    }

  }

  /** this might never be called, but we can provide it anyway.
   * clean up the database connection
   */
  protected void closeDB() {
    if (dbConn == null) { return; }
    
    // Release the resources (clean up )
    try {
      psGetUser.close();
      psGetEntities.close();
      dbConn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    //   ## DATABASE SHUTDOWN SECTION ##
    /*** In embedded mode, an application should shut down Derby.
                Shutdown throws the XJ015 exception to confirm success. ***/
    if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
      boolean gotSQLExc = false;
      try {
        DriverManager.getConnection("jdbc:derby:;shutdown=true");
      } catch (SQLException se)  {
        if ( se.getSQLState().equals("XJ015") ) {
          gotSQLExc = true;
        }
      }
      if (!gotSQLExc) {
        System.out.println("Database did not shut down normally");
      }  else  {
        System.out.println("Database shut down normally");
      }
    }
  }
  
  protected boolean doJDBC(String username, String password) throws LoginException {
    // check if the DB connection has already been set up.
    getDbConn();

    ResultSet userSet;
    boolean retval = false;
    
    
    try {
      if ( psGetUser == null || psGetEntities == null) { prepareStatements(); }

      psGetUser.setString(1, username);
      userSet = psGetUser.executeQuery();
      
      if ((userSet.next()) && password.equals(userSet.getString(1))) {
        retval = true;
      }
      userSet.close();
      
      //  Beginning of the primary catch block: uses errorPrint method
    }  catch (Throwable e)  {
      /*       Catch all exceptions and pass them to
       **       the exception reporting method             */
      System.out.println(" . . . exception thrown:");
      errorPrint(e);
      throw new LoginException("providing login information failed:"+e.getMessage());
    }
    
    return retval;
  }

  //   ## DERBY EXCEPTION REPORTING CLASSES  ##
  /***     Exception reporting methods
   **      with special handling of SQLExceptions
   ***/
   void errorPrint(Throwable e) {
    if (e instanceof SQLException)
      SQLExceptionPrint((SQLException)e);
    else {
      System.out.println("A non SQL error occured.");
      e.printStackTrace();
    }
  }  // END errorPrint

  //  Iterates through a stack of SQLExceptions
   void SQLExceptionPrint(SQLException sqle) {
    while (sqle != null) {
      System.out.println("\n---SQLException Caught---\n");
      System.out.println("SQLState:   " + (sqle).getSQLState());
      System.out.println("Severity: " + (sqle).getErrorCode());
      System.out.println("Message:  " + (sqle).getMessage());
      sqle.printStackTrace();
      sqle = sqle.getNextException();
    }
  }  //  END SQLExceptionPrint


  /**
   * @see org.timadorus.auth.IdentityAuthenticator#authenticateIdentity(org.timadorus.auth.IdentityCredentials)
   *
   * @param credentials
   * @return
   * @throws LoginException
   */
  @Override
  public Principal authenticateIdentity(IdentityCredentials credentials) throws LoginException {
    if(! (credentials instanceof BasicUserPasswordCredential) ) { 
      throw new LoginException("invalid credential"); 
    }
    BasicUserPasswordCredential creds = (BasicUserPasswordCredential) credentials;

    if (! doJDBC(creds.getUsername(),creds.getPassword())) { 
       throw new LoginException("unknown username/password"); 
    } 

    return new Identity(creds.getUsername());
  }

  /** test method to quickly add a user when using the embedded db.
   * 
   * @param string
   * @param string2
   */
  public Principal addUser(String user, String password) {
    Connection conn = getDbConn();

    String insertString = "insert into users ( user_name, password ) values ('" + user + "', '" + password + "') ";
    
    try {
      Statement s = conn.createStatement();
      s.execute(insertString);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return new Identity(user);
  }

}
