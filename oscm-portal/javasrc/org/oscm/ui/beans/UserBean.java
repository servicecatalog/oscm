/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 18.02.2009
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.internal.intf.*;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.resolver.IPResolver;
import org.oscm.types.constants.Configuration;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.*;
import org.oscm.ui.dialog.common.saml2.AuthenticationHandler;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.filter.AuthenticationSettings;
import org.oscm.ui.model.User;
import org.oscm.ui.model.UserRole;

/** Backing bean for user related actions */
@ViewScoped
@ManagedBean(name = "userBean")
public class UserBean extends BaseBean implements Serializable {

  private static final long serialVersionUID = -5436122605762958859L;

  private static final Log4jLogger logger = LoggerFactory.getLogger(UserBean.class);

  private static final Logger logger4j = Logger.getLogger(UserBean.class);

  private static final Logger loggerJul = Logger.getLogger(UserBean.class.getName());

  static final String OUTCOME_ADD_USER = "addUser";
  static final String OUTCOME_IMPORT_USER = "importUsers";
  static final String OUTCOME_EDIT_LDAP = "ldapSettings";
  static final String LABEL_ADD_USER = "button.addUser";
  static final String LABEL_ADD_GROUP = "button.addGroup";
  static final String LABEL_IMPORT_USER = "button.importUsers";
  static final String APPLICATION_BEAN = "appBean";
  static final String SELF_REGISTRATION = "/marketplace/registration.jsf";
  static final String SAMPSP_FORM = "samlSPForm:";

  private List<UserRole> userRolesForNewUser;
  private String currentPassword;
  private String userId; // used for log in
  private String password;
  private String password2;
  private String email;
  private User newUser;
  private List<User> users;
  private String requestedRedirect;
  private String confirmedRedirect;
  private String serviceLoginType = Constants.REQ_ATTR_LOGIN_TYPE_NO_MPL;
  private AuthenticationSettings authenticationSettings;

  @ManagedProperty(value = "#{menuBean}")
  private MenuBean menuBean;

  @ManagedProperty(value = "#{sessionBean}")
  private SessionBean sessionBean;

  @ManagedProperty(value = "#{organizationBean}")
  private OrganizationBean organizationBean;

  @EJB private TenantService tenantService;

  private Part userImport;
  transient ApplicationBean appBean;

  public MenuBean getMenuBean() {
    return menuBean;
  }

  public void setMenuBean(final MenuBean menuBean) {
    this.menuBean = menuBean;
  }

  public List<UserRole> getUserRolesForNewUser()
      throws ObjectNotFoundException, OperationNotPermittedException {
    if (userRolesForNewUser == null) {
      userRolesForNewUser = getUserRolesForUser(null);
    }
    return userRolesForNewUser;
  }

  public void setUserRolesForNewUser(List<UserRole> userRolesForNewUser) {
    this.userRolesForNewUser = userRolesForNewUser;
  }

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getUserId() {
    if (ADMStringUtils.isBlank(userId)) {
      userId = getRequest().getParameter(Constants.REQ_PARAM_USER_ID);
    }
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword2() {
    return password2;
  }

  public void setPassword2(String password2) {
    this.password2 = password2;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRequestedRedirect() {
    // return empty string to ensure that the value attribute of the hidden
    // filed is actually created
    return requestedRedirect == null ? "" : requestedRedirect;
  }

  public void setRequestedRedirect(String redirect) {
    requestedRedirect = redirect;
  }

  public String cancel() {
    return OUTCOME_CANCEL;
  }

  /**
   * Returns whether a login to a service is requested, that is published on the global marketplace.
   *
   * @return <code>true</code> if such service login was requested, <code>false</code> otherwise
   */
  public boolean isServiceMarketplaceLogin() {
    HttpServletRequest request = getRequest();

    String loginType = request.getParameter(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
    // Check request attribute and parameter.
    if (loginType == null) {
      loginType = (String) request.getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
    }
    return loginType != null && loginType.equals(Constants.REQ_ATTR_LOGIN_TYPE_MPL);
  }

  /**
   * Returns the type in case of a service login. {@link Constants#REQ_ATTR_LOGIN_TYPE_MPL} is
   * returned, if - and only if - a login for a service on the global marketplace has been
   * requested.
   *
   * @return the type in case of a service login.
   */
  public String getServiceLoginType() {
    if (Constants.REQ_ATTR_LOGIN_TYPE_NO_MPL.equals(serviceLoginType)) {
      serviceLoginType =
          isServiceMarketplaceLogin()
              ? Constants.REQ_ATTR_LOGIN_TYPE_MPL
              : Constants.REQ_ATTR_LOGIN_TYPE_NO_MPL;
    }
    return serviceLoginType;
  }

  public void setServiceLoginType(String loginType) {
    serviceLoginType = loginType;
  }

  public String getConfirmedRedirect() {
    return confirmedRedirect;
  }

  public void setConfirmedRedirect(String redirect) {
    confirmedRedirect = redirect;
  }

  /**
   * Get all users of the current organization
   *
   * @returns all users of the current organization
   */
  public List<User> getUsers() {
    if (users == null) {
      Vo2ModelMapper<VOUserDetails, User> mapper =
          new Vo2ModelMapper<VOUserDetails, User>() {
            @Override
            public User createModel(VOUserDetails vo) {
              return new User(vo);
            }
          };

      users = mapper.map(getIdService().getUsersForOrganization());
    }
    return users;
  }

  /**
   * Get my locale
   *
   * @returns my locale
   */
  public String getMyLocale() {
    return getUserFromSession().getLocale();
  }

  /**
   * Get my userId
   *
   * @returns my userId
   */
  public String getMyUserId() {
    return getUserFromSession().getUserId();
  }

  @Override
  public boolean getIsOrganizationAdmin() {
    return getIdService().isCallerOrganizationAdmin();
  }

  public boolean getIsUnitAdmin() {
    return getUserFromSession().getUserRoles().contains(UserRoleType.UNIT_ADMINISTRATOR);
  }

  public boolean getIsSubscriptionManager() {
    return getUserFromSession().getUserRoles().contains(UserRoleType.SUBSCRIPTION_MANAGER);
  }

  /**
   * Returns the user details for the logged in user.
   *
   * @return the user details or <code>null</code> if no user is logged in
   */
  public VOUserDetails getLoggedInUser() {
    return (VOUserDetails) getSessionAttribute(Constants.SESS_ATTR_USER);
  }

  /**
   * Returns whether a password change is required before the (potentially) logged in user can
   * continue working.
   *
   * @return <code>true</code> if a user is logged in and is required to change the password
   */
  public boolean isPasswordChangeRequired() {
    VOUserDetails user = getLoggedInUser();
    return user != null && user.getStatus() == UserAccountStatus.PASSWORD_MUST_BE_CHANGED;
  }

  /**
   * Create a new user object which can be used to insert the attributes of a new user.
   *
   * @return the created user object
   */
  public User getNewUser() {
    if (newUser == null) {
      newUser = new User(new VOUserDetails());
      User user = getUserFromSession();
      newUser.setOrganizationId(user.getOrganizationId());
      newUser.setLocale(user.getLocale());
    }
    return newUser;
  }

  /**
   * Get the list with user's roles of the selected user. If no user isn't entered, return only list
   * of roles which the new user can be.
   *
   * @param user
   * @return list with user's roles of the selected user
   * @throws ObjectNotFoundException
   * @throws OperationNotPermittedException
   */
  private List<UserRole> getUserRolesForUser(User user)
      throws ObjectNotFoundException, OperationNotPermittedException {

    List<UserRole> result = new ArrayList<>();
    List<UserRoleType> availableRoles =
        getIdService().getAvailableUserRoles(getIdService().getCurrentUserDetails());
    Set<UserRoleType> selectedRoles;
    if (user == null) {
      selectedRoles = new HashSet<>();
      if (userRolesForNewUser != null) {
        for (UserRole role : userRolesForNewUser) {
          if (role.isSelected()) {
            selectedRoles.add(role.getUserRoleType());
          }
        }
      }
    } else {
      selectedRoles = user.getUserRoles();
    }

    for (UserRoleType availableRole : availableRoles) {
      UserRole roleObj = new UserRole();
      roleObj.setUserRoleType(availableRole);
      if (selectedRoles.contains(availableRole)) {
        roleObj.setSelected(true);
      } else {
        roleObj.setSelected(false);
      }
      result.add(roleObj);
    }

    return result;
  }

  /**
   * Check if the user is logged on the platform and destroys the session if the user is neither
   * logged on the platform nor on any service.
   *
   * @return true if the use is logged on the platform.
   */
  @Override
  public boolean isLoggedIn() {
    HttpServletRequest request = getRequest();
    HttpSession session = request.getSession(false);
    if (session != null) {
      request.setAttribute(
          Constants.REQ_PARAM_MARKETPLACE_ID,
          session.getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID));
    }
    return session != null && SessionListener.cleanup(session);
  }

  public String showRegistration() {
    HttpSession session = getSession();
    if (getAuthenticationSettings().isInternal()) {
      // use getRedirect to store originally requested page in session
      getLoginRedirect(getRequest(), session, false);
      return OUTCOME_SHOW_REGISTRATION;
    } else {
      storeRelayStateInSession(SELF_REGISTRATION);
      return handleAuthentication(session);
    }
  }

  public String showDetails(String userId) {
    try {
      getUserService().getUserAndSubscriptionDetails(userId, sessionBean.getTenantID());
      return BaseBean.OUTCOME_SHOW_DETAILS;
    } catch (ObjectNotFoundException e) {
      JSFUtils.addMessage(
          null,
          FacesMessage.SEVERITY_ERROR,
          BaseBean.ERROR_ACOUNT_MODIFIED_OR_DELETED_CONCURRENTLY,
          null);

      return BaseBean.OUTCOME_ERROR;
    } catch (SaaSApplicationException e) {
      e.printStackTrace();
      return BaseBean.OUTCOME_ERROR;
    }
  }

  private void storeRelayStateInSession(String relayState) {
    getSession().setAttribute(Constants.SESS_ATTR_RELAY_STATE, relayState);
  }

  public String login() throws ValidationException {
    confirmedRedirect = ""; // reset to avoid unintended redirects
    HttpServletRequest httpRequest = getRequest();

    if (isServiceProvider()) {
      httpRequest.getParameterMap();
      userId = httpRequest.getParameter(SAMPSP_FORM + Constants.REQ_PARAM_USER_ID);
      password = httpRequest.getParameter(SAMPSP_FORM + Constants.REQ_ATTR_PASSWORD);
      requestedRedirect =
          httpRequest.getParameter(SAMPSP_FORM + Constants.REQ_ATTR_REQUESTED_REDIRECT);
    }

    try {
      // set the character encoding to UTF-8 if it is unknown
      if (httpRequest.getCharacterEncoding() == null) {
        try {
          httpRequest.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
          // UTF-8 hardcoded => shouldn't happen
        }
      }

      HttpSession session = getRequest().getSession();
      ServiceAccess serviceAccess = ServiceAccess.getServiceAcccessFor(session);
      IdentityService service = serviceAccess.getService(IdentityService.class);
      VOUser voUser = new VOUser();
      String[] tmp = userId.split(":");
      String oId = "";
      String uId = userId;
      if (tmp.length == 2) {
        oId = tmp[0];
        uId = tmp[1];
      }
      voUser.setOrganizationId(oId);
      voUser.setUserId(uId);
      try {
        voUser.setTenantId(getMarketplaceService().getTenantIdFromMarketplace(getMarketplaceId()));
        voUser = service.getUser(voUser);
      } catch (ObjectNotFoundException e) {
        if (isServiceProvider() && !ADMStringUtils.isBlank(uId)) {
          httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN);
          return OUTCOME_MARKETPLACE_ERROR_PAGE;
        }
        voUser.setOrganizationId(oId);
        voUser.setUserId(uId);
        voUser.setKey(0L);
      }

      // check service key in session bean; if not set, try to read
      // them from cookie (fallback for re-login after session timeout)
      if (!getMarketplaceService()
              .doesOrganizationHaveAccessMarketplace(getMarketplaceId(), voUser.getOrganizationId())
          && !isServiceProvider()) {
        throw new LoginToClosedMarketplaceException();
      }
      Object sb = session.getAttribute(Constants.SESS_ATTR_SESSION_BEAN);
      if (sb != null) {
        SessionBean sessionBean = (SessionBean) sb;
        sessionBean.setMarketplaceBrandUrl(null);
        // try to read selected service key from cookie
        if (SessionBean.isValidServiceKey(sessionBean.getSelectedServiceKeyForCustomer())) {
          String svcKeyFromCookie =
              JSFUtils.getCookieValue(httpRequest, Constants.REQ_PARAM_SERVICE_KEY);
          String forwardUrl = (String) session.getAttribute(Constants.SESS_ATTR_FORWARD_URL);
          if (svcKeyFromCookie != null
              && svcKeyFromCookie.trim().length() > 0
              && forwardUrl != null
              && forwardUrl.contains(Marketplace.MARKETPLACE_ROOT + "/subscriptions")) {
            sessionBean.setSelectedServiceKeyForCustomer(Long.parseLong(svcKeyFromCookie));

            ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
            String viewId = Marketplace.MARKETPLACE_ROOT + "/serviceDetails.jsf";
            viewId = viewId + getSelectedServiceQueryPart(sessionBean);
            requestedRedirect = extContext.encodeActionURL(viewId);
          }
        }
      }

      // authenticate the user
      try {
        serviceAccess.login(voUser, password, httpRequest, getResponse());
        // get the service again because the credentials have been
        // changed (important for WS usage)
        service = serviceAccess.getService(IdentityService.class);
        service.refreshLdapUser();
        // check service key in session bean; if not set, try to read
        // them from cookie (fallback for re-login after session
        // timeout)
        if (!getMarketplaceService()
                .doesOrganizationHaveAccessMarketplace(
                    getMarketplaceId(), voUser.getOrganizationId())
            && isServiceProvider()) {
          session.setAttribute(Constants.SESS_ATTR_USER, service.getCurrentUserDetails());
          throw new LoginToClosedMarketplaceException();
        }
      } catch (LoginException e) {
        if (voUser.getKey() > 0) {
          voUser = service.getUser(voUser);
        }
        if (voUser.getStatus() != null
            && voUser.getStatus().getLockLevel() > UserAccountStatus.LOCK_LEVEL_LOGIN) {
          httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_USER_LOCKED);
          if (httpRequest.getServletPath().contains(BaseBean.SAML_SP_LOGIN_AUTOSUBMIT_PAGE)) {
            return OUTCOME_MARKETPLACE_ERROR_PAGE;
          }
          return null;
        }
        throw e;
      }

      // log info on the successful login
      logger.logInfo(
          Log4jLogger.ACCESS_LOG,
          LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS,
          voUser.getUserId(),
          IPResolver.resolveIpAddress(httpRequest),
          voUser.getTenantId());

      // read the user details value object and store it in the session
      session.setAttribute(Constants.SESS_ATTR_USER, service.getCurrentUserDetails());
      return getLoginRedirect(httpRequest, session, false);
    } catch (LoginException e) {
      return outcomeLoginException(httpRequest);
    } catch (NumberFormatException e) {
      return outcomeNumberFormatException(httpRequest);
    } catch (OperationNotPermittedException | OrganizationRemovedException e) {
      return outcomeSaaSApplicationException(httpRequest, e);
    } catch (ObjectNotFoundException e) {
      return outcomeObjectNotFoundException(httpRequest, e);
    } catch (CommunicationException e) {
      return outcomeCommunicationException(httpRequest);
    } catch (LoginToClosedMarketplaceException e) {
      return outcomeLoginToClosedMarketplaceException(httpRequest);
    }
  }

  public MarketplaceService getMarketplaceService() {
    return super.getMarketplaceService();
  }

  private String outcomeSaaSApplicationException(
      HttpServletRequest httpRequest, SaaSApplicationException e) {
    if (isServiceProvider()) {
      setErrorAttributes(httpRequest, e);
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN);
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  private String outcomeLoginToClosedMarketplaceException(HttpServletRequest httpRequest) {
    if (isServiceProvider()) {
      httpRequest.setAttribute(
          Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_ACCESS_TO_CLOSED_MARKETPLACE);
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      httpRequest.setAttribute(
          Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN_TO_CLOSED_MARKETPLACE);
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  private String outcomeCommunicationException(HttpServletRequest httpRequest) {
    httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN_IMPOSSIBLE);
    if (isServiceProvider()) {
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  private String outcomeObjectNotFoundException(
      HttpServletRequest httpRequest, ObjectNotFoundException e) {
    setErrorAttributes(httpRequest, e);
    if (isServiceProvider()) {
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  private String outcomeNumberFormatException(HttpServletRequest httpRequest) {
    if (isServiceProvider()) {
      httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_SUBSCRIPTION_KEY);
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN);
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  private String outcomeLoginException(HttpServletRequest httpRequest) {
    if (isServiceProvider()) {
      httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_INVALID_SAML_RESPONSE);
      return OUTCOME_PUBLIC_ERROR_PAGE;
    } else {
      httpRequest.setAttribute(Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_LOGIN);
      return OUTCOME_STAY_ON_PAGE;
    }
  }

  /** Checks if a redirect is required after the successful operation. */
  String getLoginRedirect(
      HttpServletRequest httpRequest, HttpSession session, boolean successOnEmptyRedirect) {
    VOUserDetails user = getLoggedInUser();

    checkAddSubacription(user);

    if (StringUtils.isNotBlank(requestedRedirect)) {
      confirmedRedirect = requestedRedirect;
    } else if (!successOnEmptyRedirect || user.getStatus() == UserAccountStatus.ACTIVE) {
      String relativePath = "";
      if (httpRequest.getServletPath() != null) {
        relativePath += httpRequest.getServletPath();
      }
      if (httpRequest.getPathInfo() != null) {
        relativePath += httpRequest.getPathInfo();
      }
      String queryPart = getServiceDetailsQueryPart(httpRequest, getSessionBean());
      relativePath += queryPart;

      confirmedRedirect = relativePath;
    } else {
      confirmedRedirect = null;
    }
    boolean isMPLServiceLogin =
        !(Constants.REQ_ATTR_LOGIN_TYPE_NO_MPL.equals(getServiceLoginType()));
    if (confirmedRedirect != null) {
      if (isMPLServiceLogin) {
        if (!confirmedRedirect.contains(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE)) {
          confirmedRedirect += (confirmedRedirect.contains("?")) ? "&" : "?";
          confirmedRedirect += Constants.REQ_ATTR_SERVICE_LOGIN_TYPE + "=" + getServiceLoginType();
        }
      }
    }

    session.setAttribute(Constants.SESS_ATTR_FORWARD_URL, confirmedRedirect);
    if (isPasswordChangeRequired()) {
      // return to screen in AJAX style to show changePWD fields
      return null;
    }
    if (confirmedRedirect == null) {
      return OUTCOME_SUCCESS;
    }
    return OUTCOME_MARKETPLACE_REDIRECT;
  }

  /**
   * The method to block the users with no access rights for subscriptions, direct after login
   *
   * @param user
   */
  void checkAddSubacription(VOUserDetails user) {
    if (user == null) {
      return;
    }
    final String subscriptionAddPage = "/marketplace/subscriptions/creation/add.jsf";
    if (requestedRedirect.contains(subscriptionAddPage)) {

      List<Long> invisibleProductKeys = new ArrayList<Long>();
      try {
        invisibleProductKeys = getUserGroupService().getInvisibleProductKeysForUser(user.getKey());
      } catch (ObjectNotFoundException e) {
      }

      long selectedServiceKey = sessionBean.getSelectedServiceKeyForCustomer();

      if (!user.hasAdminRole() && invisibleProductKeys.contains(Long.valueOf(selectedServiceKey))) {
        requestedRedirect =
            requestedRedirect.replaceAll(
                subscriptionAddPage, BaseBean.MARKETPLACE_ACCESS_DENY_PAGE);
      }
    }
  }

  /**
   * Set the error attribute in the current request
   *
   * @param request the HttpServletRequest.
   * @param e the application exception causing the error
   */
  private void setErrorAttributes(HttpServletRequest request, SaaSApplicationException e) {
    request.setAttribute(Constants.REQ_ATTR_ERROR_KEY, e.getMessageKey());
    Object[] params = e.getMessageParams();
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        request.setAttribute(Constants.REQ_ATTR_ERROR_PARAM + i, params[i]);
      }
    }
  }

  /**
   * Invalidate the current session and go to the login page
   *
   * @return OUTCOME_LOGIN.
   */
  public String logoff() {
    HttpServletRequest request = invalidateSession();

    if (isMarketplaceSet(request)) {
      return OUTCOME_MARKETPLACE_LOGOUT;
    }

    return OUTCOME_LOGIN;
  }

  /**
   * Invalidate the current session
   *
   * @return the current HTTP servlet request
   */
  private HttpServletRequest invalidateSession() {

    HttpServletRequest request = getRequest();
    HttpSession session = request.getSession(false);
    if (session != null) {
      try {

        String mId = getMarketplaceId();

        session.removeAttribute(Constants.SESS_ATTR_USER);
        getSessionService().deletePlatformSession(session.getId());

        SessionListener.cleanup(session);

        if (mId != null) {
          HttpSession currentSession = request.getSession(true);
          currentSession.setAttribute(Constants.REQ_PARAM_MARKETPLACE_ID, mId);
        }
      } catch (SaaSSystemException e) {
        if (!isMarketplaceSet(request)) {
          throw e;
        }
      }
    }

    return request;
  }

  /**
   * Add a new PlatformUser to the current organization. Method for classic portal with synchronizer
   * token
   *
   * @return the logical outcome string.
   * @throws NonUniqueBusinessKeyException Thrown in case userId already exists.
   * @throws UserRoleAssignmentException
   * @throws OperationPendingException
   */
  public String createClassic()
      throws NonUniqueBusinessKeyException, UserRoleAssignmentException, OperationPendingException,
          MarketplaceRemovedException {
    if (isTokenValid()) {
      String newUserId = this.newUser.getUserId();
      String outcome = createInt(null);
      if (outcome.equals(OUTCOME_SUCCESS)) {
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_USER_CREATED, newUserId);
        // reset user table paging if user was created
        TableState ts = ui.findBean(TableState.BEAN_NAME);
        ts.resetActivePages();
      } else if (outcome.equals(OUTCOME_PENDING)) {
        addMessage(PROGRESS_PANEL, FacesMessage.SEVERITY_INFO, PROGRESS_DEFAULT, (String) null);
        return OUTCOME_ADD_USER;
      }
      resetToken();
    }
    return null;
  }

  /**
   * Add a new PlatformUser to the current organization.
   *
   * @return the logical outcome string.
   * @throws NonUniqueBusinessKeyException Thrown in case userId already exists.
   * @throws UserRoleAssignmentException
   * @throws OperationPendingException
   */
  String createInt(String mId)
      throws NonUniqueBusinessKeyException, UserRoleAssignmentException, OperationPendingException,
          MarketplaceRemovedException {
    try {
      List<UserRoleType> selectedRoles = new ArrayList<>();
      for (UserRole userRole : userRolesForNewUser) {
        if (userRole.isSelected()) {
          selectedRoles.add(userRole.getUserRoleType());
        }
      }
      newUser.getVOUserDetails().setTenantId(sessionBean.getTenantID());
      VOUserDetails createdUser =
          getIdService().createUser(newUser.getVOUserDetails(), selectedRoles, mId);
      newUser = null;
      initializeSelectdUserRole();
      boolean pending = (createdUser == null);
      if (pending) {
        return OUTCOME_PENDING;
      }
      return OUTCOME_SUCCESS;
    } catch (MailOperationException e) {
      if (getAuthenticationSettings().isInternal()) {
        addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_USER_CREATE_MAIL);
      } else {
        addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_USER_CREATE_MAIL_NOT_INTERNAL);
      }
      return OUTCOME_ERROR;
    } catch (ValidationException e) {
      String s = "";
      if (e.getMessageParams() != null) {
        s = JSFUtils.getText("UserRoleType." + e.getMessageParams()[0], null);
        for (int i = 1; i < e.getMessageParams().length; i++) {
          final Object[] roles = {
            s, JSFUtils.getText("UserRoleType." + e.getMessageParams()[i], null)
          };
          s = JSFUtils.getText(ERROR_USER_CREATE_INSUFFICIENT_ROLES + "Concat", roles);
        }
      }
      addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_USER_CREATE_INSUFFICIENT_ROLES, s);
      return OUTCOME_ERROR;
    }
  }

  /** Reset <code>false</code> to userRolesForNewUser */
  private void initializeSelectdUserRole() {
    for (UserRole userRole : userRolesForNewUser) {
      if (userRole.isSelected()) {
        userRole.setSelected(false);
      }
    }
  }

  /**
   * Change the password of the current user
   *
   * @return the logical outcome string.
   * @throws SaaSApplicationException
   */
  public String change() throws SaaSApplicationException {
    boolean wasPwdChangeRequired = isPasswordChangeRequired();

    try {
      getIdService().changePassword(currentPassword, password);
    } catch (SecurityCheckException e) {
      VOUserDetails user = getIdService().getCurrentUserDetails();
      if (user.getStatus() == UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS) {
        if (!isChangePwdOnLogin()) {
          return logoff();
        } else {
          invalidateSession();
        }
      }

      getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY, "error.changePassword");
      throw e;
    }

    if (wasPwdChangeRequired) {
      // store updated user account status in session
      setUserInSession(getIdService().getCurrentUserDetails());
    }

    try {
      ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
      HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
      HttpServletResponse response = (HttpServletResponse) ctx.getResponse();
      request.getSession();
      request.login(String.valueOf(getUserFromSession().getVOUserDetails().getKey()), password);
      // ServiceAccess serviceAccess = ServiceAccess
      // .getServiceAcccessFor(getRequest().getSession());
      // serviceAccess.login(getUserFromSession().getVOUserDetails(),
      // password, request, response);
    } catch (Exception e) {
      SaaSSystemException se = new SaaSSystemException("Login failed after changePassword!");
      logger.logError(
          Log4jLogger.SYSTEM_LOG,
          se,
          LogMessageIdentifier.ERROR_LOGIN_FAILED_AFTER_CHANGE_PASSWORD);
      // try to continue
    }

    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PASSWORD_CHANGED);
    organizationBean.resetCurrentUser();

    if (isMarketplaceSet(getRequest())) {
      // MP runs this action AJAX style and needs special redirect
      // If we had to define the initial password we will reload the
      // current page (this hides the displayed modal login dialog)
      return getLoginRedirect(getRequest(), getSession(), !wasPwdChangeRequired);
    }

    try {
      loggerJul.info("changing pasword");
      VOUserDetails userDetails = getIdService().getCurrentUserDetails();

      System.out.println("Updating Password");
      APPlatformService platformService = getService(APPlatformService.class, null);

      System.out.println(userDetails);
      platformService.updateUserCredentials(
          userDetails.getKey(), userDetails.getUserId(), password);
    } catch (Exception e) {
      loggerJul.info("ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRR!!!!!!!!!!!!!!!!!!!!!" + e.getMessage());
      throw e;
    }
    return OUTCOME_SUCCESS;
  }

  /**
   * Check if the password change occurs on login.
   *
   * @return <code>true</code> if on login
   */
  private boolean isChangePwdOnLogin() {
    StringBuffer contextPath = getRequest().getRequestURL();
    return contextPath.indexOf("public/pwd.jsf") != -1;
  }

  /**
   * The system creates a list of organizationId and userId tuples for the specified email address
   * and sends this list to the specified email address.
   *
   * @return the logical outcome.
   * @throws ValidationException Thrown if the validation in the service layer failed
   * @throws MailOperationException Thrown if the mail cannot be sent
   */
  public String sendAccounts() throws ValidationException, MailOperationException {
    getIdService().sendAccounts(email, getMarketplaceId());
    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_USER_ACCOUNTS_SENT, new String[] {email});
    return OUTCOME_SUCCESS;
  }

  public void setSessionBean(SessionBean sessionBean) {
    this.sessionBean = sessionBean;
  }

  public SessionBean getSessionBean() {
    return sessionBean;
  }

  public OrganizationBean getOrganizationBean() {
    return organizationBean;
  }

  public void setOrganizationBean(OrganizationBean organizationBean) {
    this.organizationBean = organizationBean;
  }

  public String addUser() {
    if (getUserFromSession().isRemoteLdapActive()) {
      return OUTCOME_IMPORT_USER;
    }
    return OUTCOME_ADD_USER;
  }

  public String getAddUserLabelKey() {
    if (getUserFromSession().isRemoteLdapActive()) {
      return LABEL_IMPORT_USER;
    }
    return LABEL_ADD_USER;
  }

  public String editLdapSettings() {
    return OUTCOME_EDIT_LDAP;
  }

  public boolean isSelfRegistrationAllowed() {
    SessionBean sb = getSessionBean();
    if (sb.getSelfRegistrationEnabled() == null) {
      ConfigurationService service = getConfigurationService();
      VOConfigurationSetting setting =
          service.getVOConfigurationSetting(
              ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED, Configuration.GLOBAL_CONTEXT);
      sb.setSelfRegistrationEnabled(Boolean.valueOf(setting.getValue()));
    }
    return sb.getSelfRegistrationEnabled().booleanValue();
  }

  boolean isServiceProvider() {
    ConfigurationService service = getConfigurationService();
    return service.isServiceProvider();
  }

  public boolean isInternalAuthMode() {
    if (appBean == null) {
      appBean = ui.findBean(APPLICATION_BEAN);
    }
    return appBean.isInternalAuthMode();
  }

  public String redirectToIDP() {
    // use getRedirect to store originally requested page in session
    // confirmedRedirect variable is set
    HttpSession session = getSession();
    getLoginRedirect(getRequest(), session, false);

    // an error message is displayed if called during registration process
    if (SELF_REGISTRATION.equals(confirmedRedirect)) {
      ui.handleError(null, ERROR_COMPLETE_REGISTRATION);
      return OUTCOME_SHOW_REGISTRATION;
    }
    storeRelayStateInSession(confirmedRedirect);

    return handleAuthentication(session);
  }

  private String handleAuthentication(HttpSession session) {
    try {
      return getAuthenticationHandler().handleAuthentication(true, session);
    } catch (SAML2AuthnRequestException e) {
      ui.handleError(null, BaseBean.ERROR_GENERATE_AUTHNREQUEST);
    } catch (NotExistentTenantException | ObjectNotFoundException | MarketplaceRemovedException e) {
      ui.handleError(null, BaseBean.ERROR_MISSING_TENANTID);
    } catch (WrongTenantConfigurationException e) {
      ui.handleError(null, BaseBean.ERROR_TENANT_SETTINGS_MISSING);
    }
    return OUTCOME_MARKETPLACE_ERROR_PAGE;
  }

  protected AuthenticationSettings getAuthenticationSettings() {
    if (authenticationSettings == null) {
      authenticationSettings = new AuthenticationSettings(tenantService, getConfigurationService());
    }
    return authenticationSettings;
  }

  protected AuthenticationHandler getAuthenticationHandler()
      throws ObjectNotFoundException, NotExistentTenantException, WrongTenantConfigurationException,
          MarketplaceRemovedException {
    AuthenticationSettings authenticationSettings = getAuthenticationSettings();
    authenticationSettings.init(sessionBean.getTenantID());
    return new AuthenticationHandler(getRequest(), getResponse(), authenticationSettings);
  }

  public Part getUserImport() {
    return userImport;
  }

  public void setUserImport(Part userImport) {
    this.userImport = userImport;
  }

  public void importUsersOnPortal() {
    importUsers(null);
  }

  public void importUsersOnMarketplace() {
    importUsers(ui.getMarketplaceId());
  }

  void importUsers(String marketplaceId) {
    if (userImport == null) {
      SaaSApplicationException ex = new SaaSApplicationException();
      ex.setMessageKey("error.upload.fileNotNullNorEmpty");
      ui.handleException(ex);
      return;
    }
    try {
      byte[] buffer = PartHandler.getBuffer(userImport);
      getUserService().importUsersInOwnOrganization(buffer, marketplaceId);
      ui.handle("info.user.importStarted", userImport.getSubmittedFileName());
    } catch (SaaSApplicationException ex) {
      ui.handleException(ex);
    } catch (IOException ex) {
      throw new SaaSSystemException(ex);
    }
  }

  public String getAdminPortalAddress() {
    return appBean.getServerBaseUrl();
  }
}
