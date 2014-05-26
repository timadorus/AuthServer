package org.timadorus.auth.example.gameserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import org.apache.commons.codec.binary.Base64;
import org.timadorus.auth.util.Crypto;

/**
 * A simple example "game"-server application that, together with the
 * "game"-client application, serves as a demonstration for the authentication
 * process.
 * 
 * @author
 *  Torben Könke
 */
public final class Program {
    /**
     * The port at which the game-server is accepting connections.
     */
    private static final int SERVER_PORT = 60004;
    
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
        System.err.println("Usage: example-gameserver <shared-secret-key>");
        return;
      }
      String sharedSecretKey = args[0];
      
      // Start a listening server.
      ServerSocket socket = new ServerSocket(SERVER_PORT);
      System.out.println("Gameserver ready. Accepting connections on port "
              + SERVER_PORT + ".");
      try {
        while (true) {
          Socket client = socket.accept();
          DataInputStream is = new DataInputStream(client.getInputStream());
          System.out.println("Connection accepted. Waiting for client "
                  + "login-message...");
          // Read and verify the auth-token.
          String authToken = is.readUTF();
          System.out.println("Received auth-token: " + authToken);
          verifyAuthToken(authToken, sharedSecretKey);
        }
      } finally {
        if (socket != null) {
            socket.close();
        }
      }
    }
    
    /**
     * Verifies the specified auth-token.
     * 
     * @param authToken
     *  The auth-token to verify.
     * @param key
     *  The secret-key shared with the authentication server.
     * @throws Exception
     *  An unexpected error occurred.
     */
    private static void verifyAuthToken(String authToken, String key) throws Exception {
      // 1. Base64-decode the string.
      System.out.println("Base64-decoding auth-token...");
      byte[] data = Base64.decodeBase64(authToken);
      // 2. Decrypt the data with the shared secret-key.
      System.out.println("Decrypting auth-token with key = " + key);
      data = Crypto.aesDecrypt(data, key);
      String unencrypted = new String(data, "UTF-8");
      System.out.println("Unencrypted auth-token: " + unencrypted);
      // 3. Auth-token has the form User:Entity:Timestamp:Hostname.
      String[] parts = unencrypted.split(":");
      if (parts.length < 4) {
        System.out.println("Invalid auth-token!");
        return;
      }
      // 3. Assert the auth-token has actually been generated for us.
      System.out.println("Verifying hostname...");
      String hostname = parts[3];
      if (!isValidHostname(hostname)) {
        System.out.println("Invalid hostname.");
        return;
      }
      System.out.println("Verified hostname '" + hostname + "'");
      // 4. See if the ticket contains a session-key. If so, the session
      //    between client and gameserver will be AES encrypted.
      if (parts.length > 4) {
        String sessionKey = parts[4];
        System.out.println("Session-Key for AES session-encryption: "
                           + sessionKey);
      }
      // 5. Finally, assert the timestamp is valid.
      System.out.println("Verifying timestamp...");
      long timestamp = Long.parseLong(parts[2]);
      long current = getUnixTime();
      long dt = current - timestamp;
      // FIXME: Find a good upper threshold for how long a token is valid.
      if (dt < 0 || dt > 100) {
        System.out.println("Timestamp is too old or invalid (" + dt + ")");
      } else {
        System.out.println("Auth-token verified!");
      }
    }
    
    /**
     * Iterates over all IP addresses of all network interfaces of the machine
     * to determine whether the specified hostname actually belongs to this
     * machine.
     * 
     * @param hostname
     *  The hostname (either an actual name or an IP address) to verify.
     * @return
     *  true if the specified hostname belongs to the machine; Otherwise false.
     * @throws IOException
     *  An unexpected error occurred.
     */
    private static boolean isValidHostname(String hostname) throws IOException {
      InetAddress addr = InetAddress.getByName(hostname);
      Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
      while (ifs.hasMoreElements()) {
        Enumeration<InetAddress> addrs = ifs.nextElement().getInetAddresses();
        while (addrs.hasMoreElements()) {
          InetAddress ia = addrs.nextElement();
          if (ia.equals(addr)) {
            return true;
          }
        }
      }
      return false;
    }
    
    /**
     * Returns the current time as the number of seconds that have passed
     * since 01.01.1970.
     * 
     * @return
     *  The current time.
     */
    private static long getUnixTime() {
      return System.currentTimeMillis() / 1000L;
    }
}