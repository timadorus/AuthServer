package org.timadorus.auth.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Implements a class which provides an easy-to-use API for authenticating
 * with an auth-server.
 * 
 * @author
 *  Torben Könke
 */
public class Authenticator {
  /**
   * The default port at which the server runs.
   */
  private static final int DEFAULT_SERVICE_PORT = 50001;
  
  /**
   * The name of the HTTP Auth header field.
   */
  private static final String AUTH_HEADER = "Authorization";
  
  /**
   * The standard HTTP status code for a successful request.
   */
  private static final int HTTP_OK = 200;
  
  /**
   * The address of the auth server to authenticate with.
   */
  private InetAddress serverAddress;

  /**
   * The port at which the auth service is running at the auth server.
   */
  private int port;
  
  /**
   * The username with which to authenticate.
   */
  private String username;
  
  /**
   * The password with which to authenticate.
   */
  private String password;
    
  /**
   * Ugly boilerplate code.
   */
  private class MyHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String arg0, SSLSession arg1) {
      // Do something meaningful here?
      return true;
    }
  }
  
  /**
   * Even more ugly boilerplate code.
   */
  private X509TrustManager x509TrustManager = new X509TrustManager() {
    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
        throws CertificateException {
      return;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
        throws CertificateException {
      return;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  };
  
  private TrustManager[] myTrustManager = { x509TrustManager };
    
  /**
   * Initializes a new instance of the Authenticator class.
   * 
   * @param serverAddress
   *          The address of the auth-server to authenticate at.
   * @param username
   *          The username with which to authenticate.
   * @param password
   *          The password with which to authenticate.
   * @throws GeneralSecurityException 
   *          An unexpected error occurred during SSL initialization.
   * @throws IllegalArgumentException
   *          The serverAddress parameter is null, or the username parameter is
   *          null, or the password parameter is null.
   */
  public Authenticator(InetAddress serverAddress, String username, String password)
      throws GeneralSecurityException {
    this(serverAddress, DEFAULT_SERVICE_PORT, username, password);
  }
  
  /**
   * Initializes a new instance of the Authenticator class.
   * 
   * @param serverAddress
   *          The address of the auth-server to authenticate at.
   * @param port
   *          The port at which the auth service is running at the auth server.
   * @param username
   *          The username with which to authenticate.
   * @param password
   *          The password with which to authenticate.
   * @throws GeneralSecurityException 
   *          An unexpected error occurred during SSL initialization.
   * @throws IllegalArgumentException
   *          The serverAddress parameter is null, or the port parameter is
   *          not a valid port, or the username parameter is null, or the
   *          password parameter is null.
   */
  public Authenticator(InetAddress serverAddress, int port, String username, String password)
      throws GeneralSecurityException {
    if (serverAddress == null) {
      throw new IllegalArgumentException("serverAddress");
    }
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("port");
    }
    if (username == null) {
      throw new IllegalArgumentException("username");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    this.serverAddress = serverAddress;
    this.port = port;
    this.username = username;
    this.password = password;
    
    initSecureSocketLayer();
  }
  
  /**
   * Performs SSL initialization so that HTTPS requests can be performed.
   * 
   * @throws GeneralSecurityException 
   *  An unexpected error occurred during SSL initialization.
   */
  private void initSecureSocketLayer() throws GeneralSecurityException {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, myTrustManager, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
  }
  
  /**
   * Encode the specified credentials into a String as required by HTTP Basic
   * Authentication.
   *
   * @param username
   *  The username to be encoded.
   * @param password
   *  The password to be encoded.
   * @return
   *  A Base64-encoded string as is required by HTTP Basic Authentication.
   */
  private String encodeCredentialsBasic(String username, String password) {
      String encode = username + ":" + password;
      return Base64.encodeBase64String(encode.getBytes());
  }
  
  /**
   * Performs an HTTP GET request for the specified resource.
   * 
   * @param resource
   *  The resource to request from the auth-server.
   * @return
   *  The response received from the auth-server.
   * @throws IOException
   *  The connection to the auth-server could not be established, or another
   *  IO-related error occurred.
   * @throws IllegalArgumentException
   *  The resource parameter is null.
   * @throws AuthException
   *  The server rejected the request for the specified resource.
   */
  private String makeRequest(String resource) throws IOException {
    if (resource == null) {
      throw new IllegalArgumentException("resource");
    }
    String url = null;
    try {
      // This performs URL encoding, so a resource such as 'The great frog'
      // becomes The%20great%frog so it can be safely passed as part of an
      // HTTP GET request.
      URI uri = new URI("https", null, serverAddress.getHostAddress(), port,
                      resource, null, null);
      url = uri.toURL().toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    URL obj = new URL(url);
    HttpsURLConnection  con = (HttpsURLConnection) obj.openConnection();
    con.setRequestProperty(AUTH_HEADER, "Basic "
            + encodeCredentialsBasic(username, password));
    if (con.getResponseCode() != HTTP_OK) {
      throw new AuthException("Erroneous server response ("
          + con.getResponseCode() + ")");
    }
    // Read and return the response.
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String line;
      StringBuffer response = new StringBuffer();
      while ((line = in.readLine()) != null) {
        response.append(line);
      }
      return response.toString();
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }
  
  /**
   * Gets a list of the user's entities on the server. 
   * 
   * @return
   *  A list of the user's entities.
   * @throws IOException
   *  The connection to the auth-server could not be established, or another
   *  IO-related error occurred.
   * @throws AuthException
   *  The provided credentials were rejected by the server.
   */
  public List<String> listEntities() throws IOException {
    List<String> list = new LinkedList<String>();
    String ret = makeRequest("/users/" + username);
    if (ret == null) {
      return list;
    }
    Gson gson = new Gson();
    Type stringStringMap = new TypeToken<Map<String, Object>>() { }
      .getType();
    Map<String, Object> map = gson.fromJson(ret, stringStringMap);
    if (map.containsKey("error")) {
      throw new AuthException("The server returned an error: "
                              + map.get("text"));
    }
    @SuppressWarnings("unchecked")
    ArrayList<String> ents = (ArrayList<String>) map.get("entities");
    for (String e : ents) {
      list.add(e);
    }
    return list;
  }
  
  /**
   * Gets an opaque auth-token for the specified entity. The auth-token can
   * be subsequently passed on to the game-server.
   * 
   * @param entity
   *  The entity for which to request an auth-token.
   * @return
   *  An initialized instance of the AuthResponse class containing an opaque
   *  auth-token for the specified entity as well as the gameserver
   *  endpoint to connect to.
   * @throws IOException 
   *  The connection to the auth-server could not be established, or another
   *  IO-related error occurred.
   * @throws AuthException
   *  The provided credentials were rejected by the server, or the specified
   *  entity does not exist.
   * @throws IllegalArgumentException
   *  The entity parameter is null.
   */
  public AuthResponse getAuthToken(String entity) throws IOException {
    if (entity == null) {
      throw new IllegalArgumentException("entity");
    }
    String ret = makeRequest("/users/" + username + "/" + entity);
    Gson gson = new Gson();
    Type stringStringMap = new TypeToken<Map<String, Object>>() { }
      .getType();
    Map<String, Object> map = gson.fromJson(ret, stringStringMap);
    if (map.containsKey("error")) {
      throw new AuthException("The server returned an error: "
                              + map.get("text"));
    }
    if (!map.containsKey("authToken") || !map.containsKey("gameServer")) {
      throw new AuthException("The server returned an invalid response: "
                              + ret);
    }
    // Parse the endpoint.
    String gameServer = (String) map.get("gameServer");
    int i = gameServer.indexOf(':');
    String h = gameServer.substring(0, i);
    int p = Integer.parseInt(gameServer.substring(i + 1));
    InetSocketAddress endpoint = new InetSocketAddress(h, p);
    // See if the response contains a session-key for session-encryption.
    try {
      SecretKey sessionKey = null;
      if (map.containsKey("sessionKey")) {
        byte[] keyBytes = Base64.decodeBase64((String) map.get("sessionKey"));
        sessionKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
      }
      return new AuthResponse((String) map.get("authToken"), endpoint,
                              sessionKey);
    } catch (Exception e) {
      throw new AuthException("The server returned an invalid response: "
                              + ret, e);
    }
  }
}
