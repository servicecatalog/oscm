/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: pock
 *
 *  Creation Date: 14.01.2010
 *
 *******************************************************************************/
package org.oscm.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.oscm.authorization.PasswordHash;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.IdentityClient;
import org.oscm.identity.IdentityConfiguration;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.TokenType;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.string.Strings;

/**
 * Login implementation for supporting authentication on the base of the local LDAP or an
 * organization specific remote LDAP.
 */
public class ADMRealmImpl {

  private static final String RESET_COUNTER =
      "UPDATE platformuser SET failedlogincounter = 0 WHERE failedlogincounter > 0 AND tkey = ?";
  private static final String LOCK_USER_ACCOUNT =
      "UPDATE platformuser SET status = ? WHERE failedlogincounter >= COALESCE(CAST((SELECT env_value FROM configurationsetting WHERE information_id = 'MAX_NUMBER_LOGIN_ATTEMPTS' AND context_id = 'global') AS int), 3) AND tkey = ?";
  private static final String INCREASE_LOGIN_COUNTER =
      "UPDATE platformuser SET failedlogincounter = failedlogincounter + 1 WHERE tkey = ?";
  private static final String LOCKED_STATUS = "LOCKED_FAILED_LOGIN_ATTEMPTS";

  private static final String ERR_DB_LOOKUP = "Database lookup error occured: ";
  private static final String ERR_DB_ACCESS = "Database access error occured: ";

  private static final String DN = "dn";
  private static final List<String> activeStatusList =
      Arrays.asList("ACTIVE", "PASSWORD_MUST_BE_CHANGED");

  protected static final String GROUP_USER = "PlatformUsers";
  protected static final String GROUP_ADMIN = UserRoleType.ORGANIZATION_ADMIN.name();
  protected static final List<String> GROUPLIST_USER =
          new ArrayList<>(Arrays.asList(GROUP_USER));

  private static final int SSO_CALLER_SPEC_LEN = 2;

    private final Logger logger;

  ADMRealmImpl(Logger logger) {
    this.logger = logger;
  }

  /**
   * Returns enumeration of groups that a particular user belongs to.
   *
   * @return enumeration of groups that a particular user belongs to.
   */
  Enumeration<String> getGroupNames(String userKey) throws SQLException, NamingException {
    UserQuery userQuery = getUserQuery(userKey);
    userQuery.execute();
    if (userQuery.getUserId() == null) {
      throw new SQLException("PlatformUser with user key '" + userKey + "' not found.");
    }
    List<String> roleNames = loadRoleNames(userKey);
    return Collections.enumeration(roleNames);
  }

  /**
   * Checks the authentication of a user and returns the groups it belongs to.
   *
   * @return groups that this particular user belongs to
   * @throws LoginException if the login failed.
   */
  List<String> authenticateUser(final String userKey, String password) throws LoginException {

    try {
      UserQuery userQuery = getUserQuery(userKey);
      try {
        Long.parseLong(userKey);
        userQuery.execute();
        if (userQuery.getOrgKey() == null) {
          logAndThrowException(String.format("PlatformUser '%s' not found.", userKey));
        }
      } catch (NumberFormatException ex) {
        logger.finest(
            "User key "
                + userKey
                + " contains non numeric characters,"
                + " database access skipped.");
        throw new LoginException(String.format("Login failed for invalid user key '%s'", userKey));
      }

      if (!activeStatusList.contains(userQuery.getStatus())) {
        // no matter what the result of the login attempt was, the
        // account is locked, so the operation must fail, but not
        // increase the failed login counter
        logAndThrowException(
            String.format("Login for user '%s' failed as the user account is locked.", userKey));
      }

      AuthenticationModeQuery authModeQuery = getAuthenticationModeQuery();
      authModeQuery.execute();

      if (AuthenticationMode.OIDC.name().equals(authModeQuery.getAuthenticationMode())) {
        handleOIDCLogin(userKey, password, userQuery);
      } else {
        handleInternalLogin(userKey, password, userQuery);
      }

        return loadRoleNames(userKey);
    } catch (SQLException e) {
      throw new LoginException(ERR_DB_ACCESS + e.toString());
    } catch (NamingException e) {
      e.printStackTrace();
      throw new LoginException(ERR_DB_LOOKUP);
    }
  }

  void handleOIDCLogin(String userKey, String password, UserQuery user)
      throws LoginException, SQLException, NamingException {

    boolean isPasswordEmpty = Strings.isEmpty(password);

    if (isPasswordEmpty) {
      final String errMsg =
          "Missing required authentication credentials: password must not be empty";
      logger.info(errMsg);
      throw new LoginException(errMsg);
    }

    final String callerType = getCallerType(password);

    String userId = user.getUserId();
    String tenantId = user.getTenantId();
    password = password.substring(SSO_CALLER_SPEC_LEN);

    switch (callerType) {
      case "WS":
        handleWebServiceCaller(userId, password, tenantId);
        break;
      case "UI":
        handleUICaller(userId, password, tenantId);
        break;
      default:
        handleOperatorClientCaller(userKey, password, user);
        break;
    }

    logger.info(String.format("Single Sign On: User '%s' successfully logged in.", userKey));
  }

  private void handleUICaller(String userId, String idToken, String tenantId)
      throws LoginException {

    ApiIdentityClient idc = getIdentityClient(tenantId);
    validateIdToken(idc, userId, idToken);
  }

  private void handleWebServiceCaller(String userId, String password, String tenantId)
      throws LoginException {

    try {
      ApiIdentityClient idc = getIdentityClient(tenantId);
      String token = idc.getIdToken(userId, password);
      validateIdToken(idc, userId, token);
    } catch (IdentityClientException e) {
      logger.info(
          String.format(
              "OIDC: User '%s' not logged in. Error in realm retrieving ID token.", userId));
      throw new LoginException(e.getMessage());
    }
  }

  void handleOperatorClientCaller(
      final String userKey, String password, UserQuery userQuery)
      throws LoginException, SQLException, NamingException {
    if (userKey.equals("1000")) {
      handleLoginAttempt(userKey, password, userQuery);
    } else {
      logger.info(
          String.format(
              "Single Sign On: User '%s' not logged in. Only the operator client with user key 1000 has permission.",
              userKey));
      throw new LoginException();
    }
  }

  void validateIdToken(IdentityClient identityClient, String userId, String idToken)
      throws LoginException {

    try {
      String tokenUser = identityClient.validateToken(idToken, TokenType.ID_TOKEN);
      checkUserIdMatch(userId, idToken, tokenUser);
    } catch (IdentityClientException e) {
      logger.info(
          String.format(
              "OIDC: User '%s' not logged in. Error in realm verifying ID token.", userId));
      throw new LoginException(e.getMessage());
    }
  }

  protected void checkUserIdMatch(String userId, String token, String tokenUser)
      throws LoginException {
    if (!userId.equals(tokenUser)) {
      final String errMsg =
          String.format(
              "User %s from retrieved ID token does not match with the login user '%s'.",
              tokenUser, userId);
      logger.info(errMsg);
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest(String.format("Retrieved token: %s", token));
      }

      throw new LoginException(errMsg);
    }
  }

  ApiIdentityClient getIdentityClient(String tenantId) {
    return new ApiIdentityClient(IdentityConfiguration.of().tenantId(tenantId).build());
  }

  void handleInternalLogin(String userKey, String password, UserQuery userQuery)
      throws LoginException, SQLException, NamingException {

    if (!userQuery.isRemoteLdapActive()) {
      handleLoginAttempt(userKey, password, userQuery);
    } else {
      // use the remote LDAP for the authentication
      OrganizationSettingQuery settingQuery = getOrganizationSettingQuery(userQuery);
      settingQuery.execute();

      if (userQuery.getRealmUserId() == null) {
        throw new LoginException("No LDAP specific user id was found.");
      }
      findAndBind(
          settingQuery.getProperties(),
          settingQuery.getBaseDN(),
          settingQuery.getAttrUid() + "=" + userQuery.getRealmUserId(),
          password);
    }
  }

  String getCallerType(String password) {
    return password.substring(0, SSO_CALLER_SPEC_LEN);
  }

  AuthenticationModeQuery getAuthenticationModeQuery() throws NamingException {
    return new AuthenticationModeQuery(getDataSource());
  }

  UserQuery getUserQuery(final String userKey) throws NamingException {
    return new UserQuery(getDataSource(), userKey);
  }

  OrganizationSettingQuery getOrganizationSettingQuery(UserQuery userQuery) throws NamingException {
    return new OrganizationSettingQuery(getDataSource(), userQuery.getOrgKey());
  }

  List<String> loadRoleNames(final String userKey) throws NamingException, SQLException {
    RoleQuery roleQuery = new RoleQuery(getDataSource(), userKey);
    roleQuery.execute();
    List<String> roles = roleQuery.getRoleNames();
    roles.add(0, GROUP_USER);
    return roles;
  }

  void handleLoginAttempt(final String userKey, String password, UserQuery userQuery)
      throws SQLException, NamingException, LoginException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      if (!PasswordHash.verifyPassword(
          userQuery.getPasswordSalt(), userQuery.getPasswordHash(), password)) {
        String message = String.format("Password verification failed for user '%s'", userKey);
        logger.info(message);
        // update user object
        conn = getDataSource().getConnection();
        stmt = conn.prepareStatement(INCREASE_LOGIN_COUNTER);
        stmt.setLong(1, Long.parseLong(userKey));
        stmt.executeUpdate();
        stmt = conn.prepareStatement(LOCK_USER_ACCOUNT);
        stmt.setString(1, LOCKED_STATUS);
        stmt.setLong(2, Long.parseLong(userKey));
        int rowCount = stmt.executeUpdate();
        if (rowCount > 0) {
          logger.info(
              String.format(
                  "Locked account of user '%s' due to too many failed login attempts.", userKey));
        }
        throw new LoginException(message);
      } else {
        // reset the counter, as the login was successful
        conn = getDataSource().getConnection();
        stmt = conn.prepareStatement(RESET_COUNTER);
        stmt.setLong(1, Long.parseLong(userKey));
        stmt.executeUpdate();
        logger.info(String.format("User '%s' successfully logged in.", userKey));
      }
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore, wanted to close anyway
        }
      }
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          // ignore, wanted to close anyway
        }
      }
    }
  }

  private void logAndThrowException(String message) throws LoginException {
    logger.info(message);
    throw new LoginException(message);
  }

  /**
   * Get the database data source.
   *
   * @return the database data source.
   * @throws NamingException if the database data source lookup failed.
   */
  DataSource getDataSource() throws NamingException {
    Properties p = new Properties();
    p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.OpenEJBInitialContextFactory");
    Context context = new InitialContext(p);
    return (DataSource) context.lookup("BSSDS");
  }

  /**
   * Attempt to bind as a specific DN.
   *
   * @param ldapProperties global LDAP properties
   * @param bindDN the bind data name
   * @param password the password for the bind
   * @return true if the bind was successful
   */
  boolean bindAsUser(Properties ldapProperties, String bindDN, String password) {
    boolean bindSuccessful = false;

    Properties p = (Properties) ldapProperties.clone();

    p.put(Context.SECURITY_PRINCIPAL, bindDN);
    p.put(Context.SECURITY_CREDENTIALS, password);

    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(p);
      bindSuccessful = true;
    } catch (Exception e) {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Error binding to directory as: " + bindDN);
        logger.finest("Exception from JNDI: " + e.toString());
      }
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException e) {
          logger.finest("Exception closing directory: " + e.toString());
        }
      }
    }
    return bindSuccessful;
  }

  /**
   * Do a search for the user. Should be unique if exists. If no search bind-db/bind-password are
   * defined an anonymous search is performed.
   *
   * @param ldapProps the LDAP properties
   * @param baseDN the base data name
   * @param filter the search filter
   * @return the found data name of the user
   * @throws NamingException
   */
  String userSearch(Properties ldapProps, String baseDN, String filter) throws NamingException {
    String foundDN = null;
    NamingEnumeration<SearchResult> namingEnum = null;
    final DirContext ctx = new InitialDirContext(ldapProps);

    SearchControls ctls = new SearchControls();
    ctls.setReturningAttributes(new String[] {DN});
    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    ctls.setCountLimit(1);

    try {
      namingEnum = ctx.search(baseDN, filter, ctls);
      if (namingEnum.hasMore()) {
        SearchResult res = namingEnum.next();

        StringBuffer sb = new StringBuffer();
        // for dn name with '/'
        String name = retrieveName(ldapProps, res);
        CompositeName compDN = new CompositeName(name);
        if (!compDN.isEmpty()) {
          String ldapDN = compDN.get(0);
          sb.append(ldapDN);

          if (res.isRelative()) {
            sb.append(",");
            sb.append(baseDN);
          }
          foundDN = sb.toString();
        }
      }
    } finally {
      if (namingEnum != null) {
        try {
          namingEnum.close();
        } catch (Exception ex) {
          logger.finest("Exception closing search result: " + ex.toString());
        }
      }
      try {
        ctx.close();
      } catch (Exception ex) {
        logger.finest("Exception closing directory: " + ex.toString());
      }
    }

    return foundDN;
  }

  String retrieveName(Properties ldapProps, SearchResult res) {
    String name;
    if (res.isRelative()) {
      name = res.getName();
    } else {
      name = res.getNameInNamespace();
      String urlName = res.getName();
      int index = urlName.lastIndexOf("/");
      if (index > 0) {
        ldapProps.put(Context.PROVIDER_URL, urlName.substring(0, index));
      }
    }
    return name;
  }

  /**
   * Search the user in the LDAP and perform a bind to his data name.
   *
   * @return groups that this particular user belongs to.
   * @throws LoginException if the login failed.
   */
  String findAndBind(Properties ldapProperties, String baseDN, String searchFilter, String password)
      throws LoginException {

    String realUserDN;
    boolean bindSuccessful;

    try {
      realUserDN = userSearch(ldapProperties, baseDN, searchFilter);
      if (realUserDN == null) {
        throw new LoginException("No User found for '" + searchFilter + "'.");
      }

      bindSuccessful = bindAsUser(ldapProperties, realUserDN, password);
      if (!bindSuccessful) {
        throw new LoginException("Bind with DN '" + realUserDN + "' failed.");
      }

    } catch (NamingException e) {
      throw new LoginException(e.toString());
    }

    return realUserDN;
  }
}
