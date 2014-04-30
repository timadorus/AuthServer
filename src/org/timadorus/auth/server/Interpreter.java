package org.timadorus.auth.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implements a simple command-line interpreter for managing the auth server
 * from the console.
 * 
 * @author Torben Könke
 */
public class Interpreter {
  /**
   * Logging facility.
   */
  private static final Logger LOG = Logger.getLogger(Interpreter.class.getName());

  /**
   * The input-stream from which to read commands.
   */
  private InputStream is;

  /**
   * The output-stream into which to write.
   */
  private PrintStream os;

  /**
   * The buffered-reader for reading lines from the input-stream.
   */
  private BufferedReader br;

  /**
   * Initializes a new instance of the Interpreter class using System.in and
   * System.out as input and output streams.
   */
  public Interpreter() {
    this(System.in, System.out);
  }

  /**
   * Initializes a new instance of the Interpreter class.
   * 
   * @param is
   *          The input-stream from which to read.
   * @param os
   *          The output-stream into which to output.
   * @throws IllegalArgumentException
   *           The is parameter is null, or the os parameter is null.
   */
  public Interpreter(InputStream is, PrintStream os) {
    if (is == null) {
      throw new IllegalArgumentException("is");
    }
    if (os == null) {
      throw new IllegalArgumentException("os");
    }
    this.is = is;
    this.os = os;
    br = new BufferedReader(new InputStreamReader(this.is));
  }

  /**
   * Starts the command-line interpreter. This method blocks until the
   * interpreter is shut down.
   * 
   * @param server
   *          The server instance this interpreter manages.
   * @throws IllegalArgumentException
   *           The server parameter is null.
   */
  public void start(AuthServer server) {
    if (server == null) {
      throw new IllegalArgumentException("server");
    }
    try {
      boolean quit = false;
      while (!quit) {
        os.print("$ ");
        String[] token = br.readLine().split("\\s+");
        if (token.length < 1) {
          continue;
        }
        String predicate = token[0];
        String[] args = Arrays.copyOfRange(token, 1, token.length);
        switch (predicate) {
        case "stop":
          stop(server);
          quit = true;
          break;
        case "user.create":
          create(args);
          break;
        case "user.edit":
          edit(args);
          break;
        case "user.delete":
          delete(args);
          break;
        case "truncate":
          truncate();
          break;
        case "help":
          help();
          break;
        case "user.list":
          list(args);
          break;
        case "user.exists":
          exists(args);
          break;
        case "entity.create":
          createEntity(args);
          break;
        case "entity.delete":
          deleteEntity(args);
          break;
        case "entity.list":
          listEntities(args);
          break;
        case "entity.exists":
          entityExists(args);
          break;
        default:
          os.println("Unknown command '" + predicate + "'. "
              + "Type help for a list of commands.");
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace(os);
    }
  }

  /**
   * Stops the server and exits the interpreter.
   * 
   * @param server
   *          The server this interpreter manages.
   */
  private void stop(AuthServer server) {
    os.println("Stopping the auth server...");
    server.stop();
    os.println("Exiting...");
  }

  /**
   * Creates a new user in the auth table.
   * 
   * @param args
   *          The command-line arguments.
   * @throws Exception
   */
  private void create(String[] args) {
    if (args.length < 2) {
      os.println("Invalid syntax. Try: user.create <name> <password>");
      return;
    }
    String name = args[0];
    String password = args[1];
    try {
      Database.createUser(name, password);
      String s = "Created user '" + name + "' with password '" + password
          + "'.";
      os.println(s);
      LOG.info(s);
    } catch (Exception e) {
      os.println("Could not create user '" + name + "': " + e.getMessage());
    }
  }

  /**
   * Edits an existing user in the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void edit(String[] args) {
    if (args.length < 2) {
      os.println("Invalid syntax. Try user.edit <name> <newpassword>");
      return;
    }
    String name = args[0];
    String password = args[1];
    try {
      Database.editPassword(name, password);
      String s = "Set password of user '" + name + "' to '" + password
          + "'.";
      os.println(s);
      LOG.info(s);
    } catch (Exception e) {
      os.println("Could not edit user '" + name + "': " + e.getMessage());
    }
  }

  /**
   * Deletes the requested user from the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void delete(String[] args) {
    if (args.length < 1) {
      os.println("Invalid syntax. Try user.delete <name>");
      return;
    }
    String name = args[0];
    try {
      // Try to delete user from the database.
      Database.deleteUser(name);
      String s = "Deleted user '" + name + "'.";
      os.println(s);
      LOG.info(s);
    } catch (Exception e) {
      os.println("Could not delete user '" + name + "': " + e.getMessage());
    }
  }

  /**
   * Determines whether a user exists in the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void exists(String[] args) {
    if (args.length < 1) {
      os.println("Invalid syntax. Try: user.exists <name>");
      return;
    }
    String name = args[0];
    try {
      if (Database.userExists(name)) {
        os.println("User '" + name + "' exists in auth table.");
      } else {
        os.println("User '" + name + "' doesn't exist in auth table.");
      }
    } catch (Exception e) {
      os.println("Could not probe user '" + name + "': " + e.getMessage());
    }
  }

  /**
   * Lists all users of the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void list(String[] args) {
    int maxList = Integer.MAX_VALUE;
    if (args.length > 0) {
      maxList = Integer.parseInt(args[0]);
    }
    try {
      os.println("Listing auth table:");
      List<String> users = Database.listUsers();
      int count = 0;
      for (String name : users) {
        if (count >= maxList) {
          return;
        }
        os.println(" - " + name);
        count++;
      }
    } catch (Exception e) {
      os.println("Could not list users: " + e.getMessage());
    }
  }

  /**
   * Truncates the auth table.
   * 
   * @throws IOException
   *           An error occured while reading from the input-stream.
   */
  private void truncate() throws IOException {
    os.println("This will delete all records in the auth table. Proceed? (Y/N)");
    String s = br.readLine();
    if (s.equals("Y")) {
      try {
        os.println("Truncating auth table...");
        Database.truncate();
        os.println("Auth table truncated.");
        LOG.info("Truncated auth table.");
      } catch (Exception e) {
        os.println("Could not truncate auth table: " + e.getMessage());
      }
    }
  }
  
  /**
   * Creates a new entity in the auth table.
   * 
   * @param args
   *          The command-line arguments.
   * @throws Exception
   */
  private void createEntity(String[] args) {
    if (args.length < 2) {
      os.println("Invalid syntax. Try: entity.create <user> <name>");
      return;
    }
    String username = args[0];
    String entity = args[1];
    try {
      Database.createEntity(username, entity);
      String s = "Created entity '" + entity + "' for user '" + username
          + "'.";
      os.println(s);
      LOG.info(s);
    } catch (Exception e) {
      os.println("Could not create entity '" + entity + "': " + e.getMessage());
    }
  }
  
  /**
   * Deletes the requested entity of the specified user from the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void deleteEntity(String[] args) {
    if (args.length < 2) {
      os.println("Invalid syntax. Try entity.delete <user> <name>");
      return;
    }
    String username = args[0];
    String entity = args[1];
    try {
      // Try to delete entity from the database.
      Database.deleteEntity(username, entity);
      String s = "Deleted entity '" + entity + "'.";
      os.println(s);
      LOG.info(s);
    } catch (Exception e) {
      os.println("Could not delete entity '" + entity + "': " + e.getMessage());
    }
  }
  
  /**
   * Lists all entity of the specified user.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void listEntities(String[] args) {
    if (args.length < 1) {
      os.println("Invalid syntax. Try entity.list <user>");
      return;
    }
    try {
      String username = args[0];
      os.println("Listing entities of user '" + username + "':");
      List<String> ent = Database.listEntities(username);
      for (String name : ent) {
        os.println(" - " + name);
      }
    } catch (Exception e) {
      os.println("Could not list entities: " + e.getMessage());
    }
  }
  
  /**
   * Determines whether an entity exists in the auth table.
   * 
   * @param args
   *          The command-line arguments.
   */
  private void entityExists(String[] args) {
    if (args.length < 2) {
      os.println("Invalid syntax. Try: entity.exists <user> <name>");
      return;
    }
    String username = args[0];
    String entity = args[1];
    try {
      if (Database.entityExists(username, entity)) {
        os.println("Entity '" + entity + "' exists in auth table.");
      } else {
        os.println("Entity '" + entity + "' doesn't exist in auth table.");
      }
    } catch (Exception e) {
      os.println("Could not probe entity '" + entity + "': " + e.getMessage());
    }
  }

  /**
   * Prints a help message to the output-stream.
   */
  private void help() {
    os.println("");
    os.println("stop");
    os.println(" Stops the server and exits the application.");
    os.println("user.create <name> <password>");
    os.println(" Creates a new user with the specified password.");
    os.println("user.edit <name> <password>");
    os.println(" Sets a new password for the user with the specified name.");
    os.println("user.delete <name>");
    os.println(" Deletes the user with the specified name.");
    os.println("user.exists <name>");
    os.println(" Determines whether the specified user exists in the auth table.");
    os.println("user.list [<num>]");
    os.println(" Lists (the first <num> entries of) the auth table");
    os.println("entity.create <user> <name>");
    os.println(" Creates a new entity with the specified name for the specified user.");
    os.println("entity.delete <user> <name>");
    os.println(" Deletes the entity of the specified user with the specified name.");
    os.println("entity.list <user>");
    os.println(" Lists all entities of the specified user.");
    os.println("entity.exists <user> <name>");
    os.println(" Determines whether the specified entity of the specified user exists.");
    os.println("truncate");
    os.println(" Deletes all records in the auth table.");
    os.println("help");
    os.println(" This menu.");
  }
}
