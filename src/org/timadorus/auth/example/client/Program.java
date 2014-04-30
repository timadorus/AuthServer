package org.timadorus.auth.example.client;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import org.timadorus.auth.client.Authenticator;

/**
 * A simple example "game"-client application that makes use of the API
 * provided by the auth-client-lib.jar to demonstrate authentication.
 * 
 * @author Torben Könke
 */
public final class Program {
  /**
   * The default service port of the auth-server to connect to.
   */
  private static final int DEFAULT_AUTH_SERVICE_PORT = 50001;

  /**
   * The port at which the game-server is listening for connections.
   */
  private static final int GAME_SERVER_PORT = 60004;

  /**
   * The username of the client.
   */
  private static final String USERNAME = "Hans.Wurst";

  /**
   * The password of the client.
   */
  private static final String PASSWORD = "Geheim";
  
  /**
   * Make CheckStyle happy.
   */
  private Program() {
  }
  
  /**
   * The entry-point of the application.
   * 
   * @param args
   *  The command-line arguments.
   * @throws Exception
   *  An unexpected error occurred.
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: example-client <auth-server-address> [<port>]");
      return;
    }
    String authServerHostName = args[0];
    int port = DEFAULT_AUTH_SERVICE_PORT;
    if (args.length > 1) {
      port = Integer.parseInt(args[1]);
    }
    // The username and password provided here obviously must exist in the
    // database of the auth-server for this example to work.
    Authenticator auth = new Authenticator(InetAddress.getByName(authServerHostName),
                                           port, USERNAME, PASSWORD);
    System.out.println("Retrieving list of available characters for account "
                       + "'" + USERNAME + "' from auth-server at "
                       + authServerHostName + ":" + port);
    List<String> ents = auth.listEntities();
    System.out.println();
    for (String entity : ents) {
      System.out.println(" - " + entity);
    }
    System.out.println();
    if (ents.isEmpty()) {
      System.out.println("No characters found for account '" + USERNAME + "'.");
      return;
    }
    String entity = ents.get(0);
    System.out.println("Requesting auth-token for " + entity);
    String authToken = auth.getAuthToken(entity);
    // The auth-server returns an opaque auth-token which we hand to the
    // game server as our proof of authentication.
    System.out.println("Connecting to gameserver...");
    connectToGameServer(authToken);
  }
  
  /**
   * Connets to the game-server and performs a login.
   * 
   * @param authToken
   *          The auth-token for the game-server received from the
   *          authentication server.
   * @throws Exception
   *           The connection to the game-server could not be established or
   *           another socket-related error occurred.
   */
  private static void connectToGameServer(String authToken) throws Exception {
    Socket socket = new Socket("localhost", GAME_SERVER_PORT);
    DataOutputStream os = null;
    try {
      os = new DataOutputStream(socket.getOutputStream());
      os.writeUTF(authToken);
    } finally {
      if (os != null) {
        os.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
  }
}
