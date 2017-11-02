/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 29.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.BaseBean.Vo2ModelMapper;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.model.User;
import org.oscm.ui.model.UserRole;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Backing bean to import users from a remote LDAP system
 * 
 */
@ManagedBean
@ViewScoped
public class UserImportBean implements Serializable {

    static final int BASE_WIDTH = 96;
    static final String ERROR_IMPORT_EMAIL = "error.import.email";

    private static final long serialVersionUID = -4566148086333753974L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserImportBean.class);

    private List<User> users;

    private String userIdPattern;
    private String columnWidth;
    private List<UserRole> userRolesForNewUser;

    /**
     * EJB injected through setter.
     */
    private IdentityService identityService;
    private ConfigurationService configurationService;
    
    private BaseBean baseBean = new BaseBean();
    @ManagedProperty(value = "#{tableState}")
    private TableState tableState;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> ldapUsers) {
        this.users = ldapUsers;
    }

    public String getUserIdPattern() {
        return userIdPattern;
    }

    public void setUserIdPattern(String userIdPattern) {
        this.userIdPattern = userIdPattern;
    }

    /**
     * Perform an LDAP search.
     * 
     * @return the logical outcome.
     * @throws ValidationException
     *             if not all mandatory LDAP parameters can be resolved for the
     *             underlying LDAP managed organization
     */
    public String searchUsers() throws ValidationException {

        Vo2ModelMapper<VOUserDetails, User> mapper = new Vo2ModelMapper<VOUserDetails, User>() {
            @Override
            public User createModel(VOUserDetails vo) {
                return new User(vo);
            }
        };
        users = mapper.map(getIdentityService().searchLdapUsers(userIdPattern));
        ConfigurationService service = getConfigurationService();
        VOConfigurationSetting conf = service.getVOConfigurationSetting(
                ConfigurationKey.LDAP_SEARCH_LIMIT,
                Configuration.GLOBAL_CONTEXT);
        int searchLimit = getIntValue(conf.getValue(),
                ConfigurationKey.LDAP_SEARCH_LIMIT.getFallBackValue());

        boolean overSearchLimit = identityService
                .searchLdapUsersOverLimit(userIdPattern);
        if (overSearchLimit) {
            baseBean.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_USER_LDAP_SEARCH_LIMIT_EXCEEDED,
                    new Object[] { String.valueOf(searchLimit) });
        }

        return BaseBean.OUTCOME_SUCCESS;
    }

    private int getIntValue(String value, String fallback) {
        if (value != null) {
            try {
                return Integer.decode(value).intValue();
            } catch (NumberFormatException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_VALUE_MUST_BE_NUMBER, value);
            }
        }
        if (fallback != null) {
            try {
                return Integer.decode(fallback).intValue();
            } catch (NumberFormatException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_FALLBACK_MUST_BE_NUMBER,
                        fallback);
            }
        }
        return 0;
    }

    /**
     * Import the selected users
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     */
    public String importUsers() throws SaaSApplicationException {

        Set<UserRoleType> userRoles = new HashSet<>();
        if (null != userRolesForNewUser) {
            for (UserRole role : userRolesForNewUser) {
                if (role.isSelected()) {
                    userRoles.add(role.getUserRoleType());
                }
            }
        }

        if (users != null) {
            List<VOUserDetails> list = new ArrayList<>();
            for (User user : users) {
                if (user.isSelected()) {
                    user.getVOUserDetails().setUserRoles(userRoles);
                    list.add(user.getVOUserDetails());
                }
            }
            try {
                getIdentityService().importLdapUsers(list,
                        getMarketplaceIdForUserImport());
                baseBean.addMessage(null, FacesMessage.SEVERITY_INFO,
                        BaseBean.INFO_USER_IMPORTED,
                        new Object[]{String.valueOf(list.size())});
                // reset user table paging if users were imported
                tableState.resetActivePages();
                searchUsers();
            } catch (ValidationException e) {
                if (ReasonEnum.EMAIL == e.getReason()) {
                    baseBean.addMessage(null, FacesMessage.SEVERITY_ERROR,
                            ERROR_IMPORT_EMAIL, e.getMessageParams()[0]);
                } else {
                    throw e;
                }
            }
        }

        return BaseBean.OUTCOME_SUCCESS;
    }

    /**
     * Return the ID of the of the current marketplace, if called from
     * marketplace context, otherwise return <code>null</code>.
     * 
     * @return <code>marketplace ID</code> or <code>null</code>
     */
    String getMarketplaceIdForUserImport() {
        String result = null;
        if (baseBean.isMarketplaceSet(baseBean.getRequest())) {
            result = baseBean.getMarketplaceId();
        }
        return result;
    }

    /**
     * Calculates the column width in percent depending on which which field is
     * mapped to an LDAP attribute.
     * 
     * @return the column width
     */
    public String getColumnWidth() {
        if (columnWidth == null) {
            // ldap uid, userId and mail is always visible
            int num = 3;
            User user = baseBean.getUserFromSession();
            if (user.isFirstNameDisabled()) {
                num++;
            }
            if (user.isLastNameDisabled()) {
                num++;
            }
            if (user.isLocaleDisabled()) {
                num++;
            }
            if (user.isAdditionalNameDisabled()) {
                num++;
            }
            num = (BASE_WIDTH / num);
            // double % = escaped %
            columnWidth = String.format("%s%%", Integer.valueOf(num));
        }
        return columnWidth;
    }

    /**
     * Returns <code>true</code> if at least one user is selected for import.
     * Otherwise <code>false</code> will be returned.
     * 
     * @return if the import button is disabled or not.
     */
    public boolean isImportDisabled() {
        List<User> list = getUsers();
        if (list == null) {
            return true;
        }
        for (User u : list) {
            if (u.isSelected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get list of roles which the new user can be.
     * 
     * @return list with user's roles of the selected user
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private List<UserRole> getUserRoles() throws ObjectNotFoundException,
            OperationNotPermittedException {

        List<UserRole> result = new ArrayList<>();
        List<UserRoleType> availableRoles = getIdentityService()
                .getAvailableUserRoles(getIdentityService().getCurrentUserDetails());
        Set<UserRoleType> selectedRoles = new HashSet<>();
        if (userRolesForNewUser != null) {
            for (UserRole role : userRolesForNewUser) {
                if (role.isSelected()) {
                    selectedRoles.add(role.getUserRoleType());
                }
            }
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

    public boolean isAnyUserRoleSelected() {
        boolean anyUserRoleSelected = false;
        for (UserRole r : userRolesForNewUser) {
            if (anyUserRoleSelected || r.isSelected()) {
                anyUserRoleSelected = true;
                break;
            }
        }
        return anyUserRoleSelected;
    }

    public List<UserRole> getUserRolesForNewUser()
            throws ObjectNotFoundException, OperationNotPermittedException {
        if (userRolesForNewUser == null) {
            userRolesForNewUser = getUserRoles();
        }
        return userRolesForNewUser;
    }

    public void setUserRolesForNewUser(List<UserRole> userRolesForNewUser) {
        this.userRolesForNewUser = userRolesForNewUser;
    }

    @EJB
    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public IdentityService getIdentityService() {
        if (identityService == null) {
            ServiceLocator sl = new ServiceLocator();
            identityService = sl.findService(IdentityService.class);
        }
        return identityService;
    }

    @EJB
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            ServiceLocator sl = new ServiceLocator();
            configurationService = sl.findService(ConfigurationService.class);
        }
        return configurationService;
    }

    public void setTableState(TableState tableState) {
        this.tableState = tableState;
    }

    public void setBaseBean(BaseBean baseBean) {
        this.baseBean = baseBean;
    }
}
