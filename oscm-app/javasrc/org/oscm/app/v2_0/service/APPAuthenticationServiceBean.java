/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class APPAuthenticationServiceBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(APPAuthenticationServiceBean.class);

    @EJB
    protected APPConfigurationServiceBean configService;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @EJB
    protected BesDAO besDAO;

    public VOUserDetails authenticateAdministrator(PasswordAuthentication auth)
            throws APPlatformException, AuthenticationException,
            ConfigurationException {

        if (auth == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        return authenticateUser(null, null, auth,
                UserRoleType.ORGANIZATION_ADMIN, Optional.empty());
    }

    public VOUserDetails authenticateTMForInstance(String controllerId,
            String instanceId, PasswordAuthentication auth)
            throws APPlatformException {
        if (instanceId == null || auth == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }

        ServiceInstance serviceInstance = null;
        try {
            serviceInstance = instanceDAO.getInstanceById(controllerId,
                    instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            throw new APPlatformException(e.getMessage(), e);
        }
        return authenticateUser(serviceInstance, null, auth,
                UserRoleType.TECHNOLOGY_MANAGER, Optional.of(controllerId));
    }

    public void authenticateTMForController(String controllerId,
            PasswordAuthentication auth) throws APPlatformException {
        if (controllerId == null || auth == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        try {
            PasswordAuthentication storedAuthentication = configService
                    .getAuthenticationForBESTechnologyManager(controllerId,
                            null, null);
            if (auth.equals(storedAuthentication)) {
                return;
            }
        } catch (ConfigurationException e) {
            // ignore, there is no stored configuration
        }
        // this is a different technology manager, do the full check
        getAuthenticatedTMForController(controllerId, auth);
    }

    public VOUserDetails getAuthenticatedTMForController(String controllerId,
            PasswordAuthentication auth) throws APPlatformException {
        if (controllerId == null || auth == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        HashMap<String, Setting> controllerSettings = configService
                .getControllerConfigurationSettings(controllerId);
        Setting organizationId = controllerSettings
                .get(ControllerConfigurationKey.BSS_ORGANIZATION_ID.name());
        if (organizationId == null || organizationId.getValue() == null) {
            ConfigurationException ce = new ConfigurationException(
                    "No organization configured for controller " + controllerId);
            LOGGER.debug("No organization configured for controller {}",
                    controllerId);
            throw ce;
        }
        return authenticateUser(null, organizationId.getValue(), auth,
                UserRoleType.TECHNOLOGY_MANAGER, Optional.of(controllerId));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    VOUserDetails authenticateUser(ServiceInstance serviceInstance,
                                   String organizationId, PasswordAuthentication auth,
                                   UserRoleType role, Optional<String> controllerId) throws APPlatformException {
        // check that either user key or user id is specified
        if (Strings.isEmpty(auth.getUserName())) {
            throw new IllegalArgumentException("User id must be specified");
        }

        VOUser user = new VOUser();
        VOUserDetails userDetails = null;
        user.setUserId(auth.getUserName());
        try {
            user.setKey(Long.parseLong(auth.getUserName()));
        } catch (NumberFormatException e) {
            // ignore
        }
        String password = auth.getPassword();

        // if no explicit organizationID is set, the user to be authenticated
        // must come from the organization currently set in the identityService
        if (organizationId == null) {
            VOUserDetails currentUserDetails = besDAO.getUserDetails(
                    serviceInstance, null, null, controllerId);
            if (currentUserDetails != null) {
                organizationId = currentUserDetails.getOrganizationId();
                // check if current web service user equals requesting user
                if ((user.getKey() == 0 || currentUserDetails.getKey() == user
                        .getKey())
                        && (user.getUserId() == null || user.getUserId()
                                .equals(currentUserDetails.getUserId()))) {
                    PasswordAuthentication pwAuth = configService
                            .getWebServiceAuthentication(serviceInstance, null, controllerId);
                    String existingPW = String.valueOf(pwAuth.getPassword());
                    if (existingPW.equals(password)) {
                        user = userDetails = currentUserDetails;
                    }
                }
            }
        }

        Map<String, Setting> settings = configService
                .getAllProxyConfigurationSettings();
        boolean isSsoMode = "SAML_SP".equals(settings
                .get(PlatformConfigurationKey.BSS_AUTH_MODE.name()));

        if (user.getUserId() == null && isSsoMode) {
            // in SSO mode the userId must always be present since no
            // lookup of ID by key is possible via IdentityService
            throw new AuthenticationException(
                    "The provisioning platform is configured to authenticate using STS. Therefore in all requests the VOUser must have set a valid userId");
        }

        if (user.getKey() == 0 && !isSsoMode) {
            // if we do not yet have the required user key
            // available we first have to get it from BSS platform
            user = besDAO.getUser(serviceInstance, user, controllerId);
        }

        try {
            if (userDetails == null) {
                user = userDetails = besDAO.getUserDetails(serviceInstance,
                        user, password, controllerId);
            }
        } catch (Exception e) {
            AuthenticationException ae = new AuthenticationException(
                    e.getMessage(), e);
            LOGGER.debug(
                    "User {} could not be authenticated => call to retrieve himself failed",
                    user.getUserId());
            throw ae;
        }

        // check organization
        if (user.getOrganizationId() == null
                || !user.getOrganizationId().equals(organizationId)) {
            AuthenticationException ae = new AuthenticationException(
                    "User does not belong to the correct organization.");
            LOGGER.debug(
                    "User {} does not belong to the correct organization {}",
                    user.getUserId(), organizationId);
            throw ae;
        }

        // check role
        Set<UserRoleType> roles = user.getUserRoles();
        if (roles == null || !roles.contains(role)) {
            AuthenticationException ae = new AuthenticationException(
                    "User does not have the required role");
            LOGGER.debug("User {} does not have the required role",
                    user.getUserId());
            throw ae;
        }
        return userDetails;
    }

}
