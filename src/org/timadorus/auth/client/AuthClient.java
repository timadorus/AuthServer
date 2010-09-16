/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.client.AuthClient.java
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

package org.timadorus.auth.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * @author sage
 *
 */
public class AuthClient {

  private class MyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
      try {
        System.out.println("Was asked to verify that " + arg0 + " is " + arg1.getPeerPrincipal().getName());
      } catch (SSLPeerUnverifiedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return true;
    }

  }

  public class EntityLine {
    private final String label;
    private final String identifier;
    private final boolean isLeaf;
    
    public EntityLine(String line) {
      int first = line.indexOf(':');
      int second = line.indexOf(':', first + 1);
   
      if ((first == -1) || (second == -1)) {
        // TODO: throw some badass Exception here. really! ;-)
        label = null;
        isLeaf = true;
        identifier = null;
        return;
      }
      
      label = line.substring(0, first);
      isLeaf = line.substring(first + 1, second).equals("L");
      identifier = line.substring(second + 1, line.length());
      
    }
    
    /**
     * @return the label
     */
    public String getLabel() {
      return label;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * @return the isLeaf
     */
    public boolean isLeaf() {
      return isLeaf;
    }

  }
  
  private X509TrustManager x509TrustManager = new X509TrustManager() {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
      return;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
      return;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  };
  TrustManager[] myTrustManager = {x509TrustManager};


  private static final String AUTHENTICATION_HEADER = "Authorization";
  
  private String authentication;
  private WebResource wr = null;


  public AuthClient(String baseURI) {

    ClientConfig config = new DefaultClientConfig();

    SSLContext ctx;
    try {
      ctx = SSLContext.getInstance("SSL");
      ctx.init(null, myTrustManager, null);
      
      config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, 
                                 new HTTPSProperties(new MyHostnameVerifier(), ctx));
      Client client = Client.create(config);

      wr = client.resource(baseURI);

    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (KeyManagementException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * <p>Convenience string for Base 64 encoding.</p>
   */
  private static final String BASE64_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
    + "abcdefghijklmnopqrstuvwxyz" 
    + "0123456789+/";

  /**
   * <p>Encode the specified credentials into a String as required by
   * HTTP Basic Authentication (<a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>).</p>
   *
   * @param username Username to be encoded
   * @param password Password to be encoded
   */
  public String encodeCredentialsBasic(
    // CHECKSTYLE OFF MagicNumber
    
          String username, String password) {

      String encode = username + ":" + password;
      int paddingCount = (3 - (encode.length() % 3)) % 3;
      encode +=
              "\0\0".substring(0, paddingCount);
      StringBuilder encoded = new StringBuilder();
      for (int i = 0; i < encode.length(); i += 3) {
          int j = (encode.charAt(i) << 16) + (encode.charAt(i + 1) << 8) + encode.charAt(i + 2);
          encoded.append(BASE64_CHARS.charAt((j >> 18) & 0x3f));
          encoded.append(BASE64_CHARS.charAt((j >> 12) & 0x3f));
          encoded.append(BASE64_CHARS.charAt((j >> 6) & 0x3f));
          encoded.append(BASE64_CHARS.charAt(j & 0x3f));
      }

      // CHECKSTYLE ON MagicNumber

      return encoded.toString();
  }


  private EntityLine[] listEntities(String parentIdentifier) {
    String path = "listEntities";
    EntityLine[] entities;
    
    if (parentIdentifier != null) {
     path += "/" + parentIdentifier; 
    }
    String[] strLines = wr.path(path).header(AUTHENTICATION_HEADER, authentication).get(String.class).split("\n");
    entities = new EntityLine[strLines.length];
    for (int i = 0; i < strLines.length; i++) {
      entities[i] = new EntityLine(strLines[i].trim());
    }
    return entities;
  }

  public void setAuth(String username, String password) {
    authentication = "Basic " + encodeCredentialsBasic(username, password);
  }

  public String getAuthToken(String identifier) {
    String path = "getAuthorization/" + identifier;
    
    String retval = wr.path(path).header(AUTHENTICATION_HEADER, authentication).get(String.class);
    return retval;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
    String baseURI = "https://localhost:9998";

    AuthClient client = new AuthClient(baseURI);

    // set the username and password
    client.setAuth("fii", "br");

    // Get a list of valid entities
    EntityLine[] lines = client.listEntities("LotR:Two_Towers");
    for (EntityLine line : lines) {
      System.out.println("label: '" + line.getLabel() + "' (" + line.getIdentifier() + ")");
    }
    
    String authToken = client.getAuthToken("LotR:Two_Towers:Eowin");
    
    System.out.println("token: '" + authToken + "'");
  }



}
