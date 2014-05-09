package org.timadorus.auth.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.codec.binary.Base64;
import org.timadorus.auth.util.Crypto;
import org.timadorus.auth.util.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The resource class which handles the '/users' HTTP request and thus
 * implements the RESTful Webservice API.
 * 
 * @author Torben Könke
 */
@Produces("application/json")
@Path("/users")
public class UsersResource {
  /**
   * The HTTP headers of the HTTP request.
   */
  @Context HttpHeaders headers;
  
  /**
   * The Servlet configuration.
   */
  @Context ServletConfig config;
  
  /**
   * Gets the username of the user making the HTTP request.
   * 
   * @return
   *  The username of the user performing the HTTP request.
   */
  private String getUsername() {
    // Get the username from HTTP request headers.
    String auth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION).get(0);
    return Util.getBasicAccessUsername(auth);
  }
  
  /**
   * Determines whether the user making the HTTP request is an administrator.
   * 
   * @return
   *  true if the user performing the HTTP request is an administrator;
   *  Otherwise false.
   * @throws Exception
   *  An unexpected error occurred.
   */
  private boolean isAdmin() throws Exception {
    return Database.isAdmin(getUsername());
  }
  
  /**
   * The method that is executed when the /users resource is being
   * requested via the HTTP GET method.
   * 
   * @param match
   *  An optional parameter denoting a regular expression for filtering the
   *  selection of usernames in the returned list.
   * @return
   *  A JSON-encoded list of usernames.
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws Exception
   *  An unexpected error occured.
   */
  @GET
  public String getUsers(@QueryParam("match") String match) throws Exception {
    assertAdmin();
    return new Gson().toJson(Database.listUsers(match));
  }
  
  /**
   * The method that is executed when the /users/{username} resource is being
   * requested via the HTTP GET method.
   * 
   * @param name
   *  The name of the user whose data will be returned.
   * @return
   *  A JSON-object containing information about the requested user.
   * @throws Exception 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   */
  @GET
  @Path("{name}")
  public String getUser(@PathParam("name") String name) throws Exception {
    // A user may only request information about his own account, unless he is
    // an administrator.
    assertAllowed(name);
    // Fetch the user information.
    User user = Database.getUser(name);
    if (user == null) {
      throw new IllegalStateException("The user '" + name + "' does not exist.");
    }
    List<Entity> ents = Database.listEntities(name);
    // Construct and return a proper JSON object.
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("name", user.getName());
    // Make some fields visible only if the requestor is privileged.
    if (isAdmin()) {
      props.put("id", user.getId());
      props.put("hash", user.getHash());
      props.put("admin", user.isAdmin());
      props.put("lastLogin", user.getLastLogin());
      props.put("flags", user.getFlags());
    }
    List<String> entityNames = new LinkedList<String>();
    props.put("entities", entityNames);
    for (Entity e : ents) {
      entityNames.add(e.getName());
    }
    return new Gson().toJson(props);
  }
  
  /**
   * The method that is executed when the /users/{username} resource is being
   * requested via the HTTP DELETE method.
   * 
   * @param name
   *  The name of the user to delete.
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The specified user does not exist.
   * @throws Exception 
   *  An unexpected error occurred.
   */
  @DELETE
  @Path("{name}")
  public void deleteUser(@PathParam("name") String name) throws Exception {
    // Only administrators may delete user accounts.
    assertAdmin();
    Database.deleteUser(name);
  }
  
  /**
   * The method that is executed when the /users/{username} resource is being
   * requested via the HTTP PUT method.
   * 
   * @param name
   *  The name of the user to create.
   * @param json
   *  A JSON-object containing the attributes of the new user to create. 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The specified user already exists.
   * @throws Exception 
   *  An unexpected error occurred.
   */
  @PUT
  @Path("{name}")
  public void createUser(@PathParam("name") String name, String json)
      throws Exception {
    // Only administrators may create user accounts.
    assertAdmin();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = new Gson().fromJson(json, Map.class);
      String password = (String) map.get("password");
      boolean admin = map.get("admin") != null
          ? ((boolean) map.get("admin")) : false;
      int flags = map.get("flags") != null ? ((Double) map.get("flags")).intValue() : 0;
      Database.createUser(name, password, admin, flags);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
  
  /**
   * The method that is executed when the /users/{username} resource is being
   * requested via the HTTP POST method.
   * 
   * @param name
   *  The name of the user to update.
   * @param json
   *  A JSON-object containing the attributes to update. 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The specified user does not exist.
   * @throws Exception 
   *  An unexpected error occurred.
   */
  @POST
  @Path("{name}")
  public void updateUser(@PathParam("name") String name, String json)
      throws Exception {
    // A user may only update his own account, unless he is an administrator.
    assertAllowed(name);
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = new Gson().fromJson(json, Map.class);
      String password = null;
      Boolean admin = null;
      Integer flags = null;
      if (map.containsKey("password")) {
        password = (String) map.get("password");
      }
      // Only administrators may update privileged fields.
      if (isAdmin()) {
        if (map.containsKey("admin")) {
          admin = (boolean) map.get("admin");
        }
        if (map.containsKey("flags")) {
          flags = ((Double) map.get("flags")).intValue();
        }
      }
      Database.updateUser(name, password, admin, flags);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
  
  /**
   * The method that is executed when the /users/username/character resource
   * is being requested via the HTTP GET method.
   * 
   * @param user
   *  The name of the user whose entity will be returned.
   * @param entity
   *  The name of the entity to return information for.
   * @return
   *  A JSON-object containing information about the requested entity.
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The entity does not exist.
   * @throws Exception
   *  An unexpected error occurred.
   */
  @GET
  @Path("{user}/{entity}")
  public String getEntity(@PathParam("user") String user,
    @PathParam("entity") String entity) throws Exception {
    assertAllowed(user);
    // Fetch and return information on entity.
    Entity ent = Database.getEntity(user, entity);
    if (ent == null) {
      throw new IllegalStateException("The entity '" + entity + "' does not exist.");
    }
    // Construct and return a proper JSON object.
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("name", ent.getName());
    props.put("authToken", generateAuthToken(user, entity));
    // Make some fields visible only if the requestor is privileged.
    if (isAdmin()) {
      props.put("id", ent.getId());
      props.put("lastLogin", ent.getLastLogin());
      props.put("flags", ent.getFlags());
    }
    return new GsonBuilder().disableHtmlEscaping().create().toJson(props);
  }
  
  /**
   * The method that is executed when the /users/{username}/{entity} resource
   * is being requested via the HTTP DELETE method.
   * 
   * @param user
   *  The name of the user whose entity to delete.
   * @param entity
   *  The entity to delete.
   * @throws Exception 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The entity does not exist.
   */
  @DELETE
  @Path("{user}/{entity}")
  public void deleteEntity(@PathParam("user") String user,
    @PathParam("entity") String entity) throws Exception {
    // Only administrators may delete user accounts.
    assertAllowed(user);
    Database.deleteEntity(user, entity);
  }

  /**
   * The method that is executed when the /users/{username}/{entity} resource
   * is being requested via the HTTP PUT method.
   * 
   * @param user
   *  The name of the user.
   * @param entity
   *  The name of the entity to create.
   * @param json
   *  A JSON-object containing the attributes of the new entity to create. 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The specified entity already exists.
   * @throws Exception 
   *  An unexpected error occurred.
   */
  @PUT
  @Path("{user}/{entity}")
  public void createEntity(@PathParam("user") String user,
    @PathParam("entity") String entity, String json) throws Exception {
    assertAllowed(user);
    try {
      int flags = 0;
      if (json != null && !json.isEmpty()) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = new Gson().fromJson(json, Map.class);
        if (map.get("flags") != null) {
          flags = ((Double) map.get("flags")).intValue();
        }
      }
      Database.createEntity(user, entity, flags);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
  
  /**
   * The method that is executed when the /users/{username}/{entity} resource
   * is being requested via the HTTP POST method.
   * 
   * @param user
   *  The name of the user the to-be-updated entity belongs to.
   * @param entity
   *  The name of entity to update.
   * @param json
   *  A JSON-object containing the attributes to update. 
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The specified user does not exist.
   * @throws Exception 
   *  An unexpected error occurred.
   */
  @POST
  @Path("{user}/{entity}")
  public void updateEntity(@PathParam("user") String user,
    @PathParam("entity") String entity, String json) throws Exception {
    // Only administratory may update entities.
    assertAdmin();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = new Gson().fromJson(json, Map.class);
      Integer flags = null;
      if (map.containsKey("flags")) {
        flags = ((Double) map.get("flags")).intValue();
      }
      String name = null;
      if (map.containsKey("name")) {
        name = (String) map.get("name");
      }
      Database.updateEntity(user, entity, name, flags);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  /**
   * The method that is executed when the /users/username/character/stats
   * resource is being requested via the HTTP GET method.
   * 
   * @param user
   *  The name of the user whose entity's stats will be returned.
   * @param entity
   *  The name of the entity whose stats will be returned.
   * @return
   *  A JSON-object containing the stats of the requested entity.
   * @throws SecurityException
   *  The requestor is not allowed to request the resource.
   * @throws IllegalStateException
   *  The entity does not exist, or the request is invalid.
   * @throws Exception
   *  An unexpected error occured.
   */
  @GET
  @Path("{user}/{entity}/stats")
  public String getStats(@PathParam("user") String user,
    @PathParam("entity") String entity) throws Exception {
    assertAllowed(user);
    // Fetch stats for entity and return as a JSON-object.
    Map<String, String> attr = Database.getAttributes(user, entity);
    return new Gson().toJson(attr);
  }
  
  /**
   * Asserts that the requestor performing the HTTP request is an administrator.
   * 
   * @throws Exception 
   *  The requestor is not an administrator, or an unexpected error occured.
   */
  private void assertAdmin() throws Exception {
    if (!isAdmin()) {
      throw new SecurityException("Forbidden request");
    }
  }
  
  /**
   * Asserts that the requestor is allowed to access the resource pertaining
   * to the user with the specified name.
   * 
   * @param name
   *  The name of the user.
   * @throws Exception 
   */
  private void assertAllowed(String name) throws Exception {
    // Ensure requestor is either an administrator, or the name equals the
    // name of HTTP Authorize header field.
    if (!isAdmin() && !name.equals(getUsername())) {
      throw new SecurityException("Forbidden request");
    }
  }
  
  /**
   * Generates the auth-token for the specified entity.
   * 
   * @param username
   *  The name of the user the entity belongs to.
   * @param entity
   *  The entity to generate an auth-token for.
   * @return
   *  The generated auth-token for the entity.
   * @throws Exception
   *  An unexpected error occured.
   */
  private String generateAuthToken(String username, String entity) throws Exception {
    // Get the shared secret key.
    String sharedSecretKey = config.getInitParameter("sharedSecretKey");
    // Generate and return an encrypted auth-token. The auth-token has the
    // form 'User:Entity:Timestamp'.
    String authToken = username + ":" + entity + ":"
        + Long.toString(getUnixTime());
    byte[] encrypted = Crypto.aesEncrypt(authToken.getBytes("UTF-8"),
                      sharedSecretKey);
    return Base64.encodeBase64String(encrypted);
  }
  
  /**
   * Returns the unix time, that is, the number of seconds that have passed
   * since 01.01.1970.
   * 
   * @return
   *  The current time.
   */
  private long getUnixTime() {
    return System.currentTimeMillis() / 1000L;
  }
}
