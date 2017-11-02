/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageusers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.oscm.string.Strings;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.DataTableHandler;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.profile.FieldData;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.UserManagementService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
@ManagedBean(name="manageUsersCtrl")
@RequestScoped
public class ManageUsersCtrl extends BaseBean {

    static final String MANAGE_USERS_MODEL = "manageUsersModel";
    static final String APPLICATION_BEAN = "appBean";
    static final String FIRSTNAME = "firstName";
    static final String LASTNAME = "lastName";

    private static final String ROLE_NAME_TEMPLATE = "UserRoleType.%s.enum";
    private static final Set<UserAccountStatus> LOCKED = Collections
            .unmodifiableSet(EnumSet
                    .of(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS));

    private List<String> dataTableHeaders = new ArrayList<String>();

    transient ManageUsersModel model;
    transient ApplicationBean appBean;
    
    @ManagedProperty(value="#{sessionBean}")
    private SessionBean sessionBean;

    boolean resetListOnly;

    private int usersSize = 0;

    public String getInitialize() {

        ManageUsersModel m = getModel();
        if (!m.isInitialized()) {
            m.setResetPasswordButtonVisible(!getUserManagementService()
                    .isOrganizationLDAPManaged()
                    && getApplicationBean().isInternalAuthMode());
            List<POUser> users = getUserService().getUsers();

            if (Strings.isEmpty(sessionBean.getSelectedUserId())
                    && Strings.isEmpty(m.getSelectedUserId())) {
                initWithoutSelection(m, users);
            } else {
                initWithSelection(m, users);
            }
            m.setInitialized(true);
        }
        if (resetListOnly) {
            List<POUser> users = getUserService().getUsers();
            getModel().setUsers(users);
        }
        if (m.getUsers() != null) {
            usersSize = m.getUsers().size();
        } else {
            usersSize = 0;
        }
        return "";
    }

    public List<String> getDataTableHeaders() {
        if (dataTableHeaders == null || dataTableHeaders.isEmpty()) {
            try {
                dataTableHeaders = reverseNameSequence(DataTableHandler
                        .getTableHeaders(POUser.class.getName()));
            } catch (Exception e) {
                throw new SaaSSystemException(e);
            }
        }
        return dataTableHeaders;
    }

    private List<String> reverseNameSequence(List<String> dataTableHeaders) {
        if (sessionBean.getNameSequenceReversed()) {
            int firstName = dataTableHeaders.indexOf(FIRSTNAME);
            int lastName = dataTableHeaders.indexOf(LASTNAME);
            dataTableHeaders.set(firstName, LASTNAME);
            dataTableHeaders.set(lastName, FIRSTNAME);
        }
        return dataTableHeaders;
    }

    void initWithoutSelection(ManageUsersModel m, List<POUser> users) {

        m.setUsers(users);
        m.setEmail(new FieldData<String>(null, true, true));
        m.setFirstName(new FieldData<String>(null, true));
        m.setLastName(new FieldData<String>(null, true));
        m.setLocale(new FieldData<String>(null, true, true));
        m.setSalutation(new FieldData<String>(null, true));
        m.setUserId(new FieldData<String>(null, true, true));
        m.setSelectedUserId(null);
        m.setKey(0);
        m.setVersion(0);
    }

    void initWithSelection(ManageUsersModel m, List<POUser> users) {

        if (!Strings.isEmpty(sessionBean.getSelectedUserId()))
            m.setSelectedUserId(sessionBean.getSelectedUserId());

        POUserDetails u = getUserDetailsAndValidateLocale(m.getSelectedUserId());
        if (u == null) {
            initWithoutSelection(m, users);
            sessionBean.setSelectedUserId(null);
        } else {
            m.setUsers(users);
            updateSelectedUser(m, u);
        }

    }

    void updateSelectedUser(ManageUsersModel m, POUserDetails u) {

        boolean isLocked = LOCKED.contains(u.getStatus());
        Set<SettingType> set = u.getMappedAttributes();
        m.setEmail(new FieldData<String>(u.getEmail(), set
                .contains(SettingType.LDAP_ATTR_EMAIL) || isLocked, true));
        m.setFirstName(new FieldData<String>(u.getFirstName(), set
                .contains(SettingType.LDAP_ATTR_FIRST_NAME) || isLocked));
        m.setLastName(new FieldData<String>(u.getLastName(), set
                .contains(SettingType.LDAP_ATTR_LAST_NAME) || isLocked));
        m.setLocale(new FieldData<String>(u.getLocale(), isLocked, true));
        m.setUserId(new FieldData<String>(u.getUserId(),
                set.contains(SettingType.LDAP_ATTR_UID)
                        || isLocked
                        || (Strings.areStringsEqual(ui.getMyUserId(),
                                u.getUserId()) && !getApplicationBean()
                                .isInternalAuthMode()), true));
        String sal = null;
        if (u.getSalutation() != null) {
            sal = u.getSalutation().name();
        }
        m.setSalutation(new FieldData<String>(sal, isLocked));
        m.setKey(u.getKey());
        m.setVersion(u.getVersion());
        m.setRoles(initUserRoles(u));
        m.setLocked(isLocked);

    }

    POUserDetails getUserDetailsAndValidateLocale(String selectedUserId) {

        POUserDetails result = null;
        try {
            result = getUserService().getUserDetails(selectedUserId, sessionBean.getTenantID());
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        if (result != null && result.getLocale() != null) {
            getApplicationBean().checkLocaleValidation(result.getLocale());
        }
        return result;
    }

    ApplicationBean getApplicationBean() {

        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }

        return appBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    UserManagementService getUserManagementService() {

        UserManagementService userManagementService = sl
                .findService(UserManagementService.class);

        return userManagementService;
    }

    public String checkSelectedUser() {
        ManageUsersModel m = getModel();
        if (m.isInitialized() && getModel().getSelectedUserId() == null)
            return OUTCOME_USER_LIST;
        else
            return "";
    }

    public void setSelectedUserId(String userId) {

        ManageUsersModel m = getModel();
        POUserDetails u = getUserDetailsAndValidateLocale(userId);
        if (u != null) {
            updateSelectedUser(m, u);
        } else {
            userId = null;
        }
        sessionBean.setSelectedUserId(userId);
        m.setSelectedUserId(userId);
    }

    public String getSelectedUserId() {

        String userId = getModel().getSelectedUserId();

        return userId;
    }

    /**
     * @return the usersSize
     */
    public int getUsersSize() {
        return usersSize;
    }

    /**
     * @param usersSize
     *            the usersSize to set
     */
    public void setUsersSize(int usersSize) {
        this.usersSize = usersSize;
    }

    ManageUsersModel getModel() {

        if (model == null) {
            model = ui.findBean(MANAGE_USERS_MODEL);
            model.setSelectedUserId(sessionBean.getSelectedUserId());
        }

        return model;
    }

    List<UserRole> initUserRoles(POUserDetails u) {

        List<UserRole> result = new ArrayList<UserRole>();
        Set<UserRoleType> roles = u.getAvailableRoles();
        for (UserRoleType r : roles) {
            String name = ui
                    .getText(String.format(ROLE_NAME_TEMPLATE, r.name()));
            UserRole role = new UserRole(r, name, u.getAssignedRoles()
                    .contains(r));
            result.add(role);
        }

        return result;
    }

    Set<UserRoleType> getSelectedUserRoles(List<UserRole> roles) {

        Set<UserRoleType> result = new HashSet<UserRoleType>();
        for (UserRole r : roles) {
            if (r.isSelected()) {
                result.add(r.getType());
            }
        }

        return result;
    }

    public String save() throws SaaSApplicationException {

        ManageUsersModel m = getModel();
        if (!m.isTokenValid()) {

            return null;
        }
        POUserDetails ud = toPOUserDetails(m);
        try {
            Response r = getUserService().saveUser(ud);
            if (Strings
                    .areStringsEqual(ui.getMyUserId(), m.getSelectedUserId())) {
                ui.handle(r, BaseBean.INFO_USER_SAVED_ITSELF, ud.getUserId());
                refreshUser();
                ui.updateAndVerifyViewLocale();
            } else {
                ui.handle(r, BaseBean.INFO_USER_SAVED, ud.getUserId());
            }
            m.resetToken();
            m.setInitialized(false);
            m.setSelectedUserId(ud.getUserId());
            sessionBean.setSelectedUserId(ud.getUserId());
        } catch (ConcurrentModificationException e) {
            ui.handleException(e);
            resetListOnly = true;
        }

        return null;
    }

    public String delete() throws SaaSApplicationException {

        ManageUsersModel m = getModel();
        if (!m.isTokenValid()) {

            return null;
        }
        POUser user = toPOUser(m);
        try {
            Response r = getUserService().deleteUser(user,
                    ui.getMarketplaceId(), sessionBean.getTenantID());
            ui.handle(r, BaseBean.INFO_USER_DELETED, user.getUserId());
        } catch (TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION);
            return "";
        }
        // reset user table paging if user was deleted
        TableState ts = ui.findBean(TableState.BEAN_NAME);
        ts.resetActivePages();
        m.setInitialized(false);
        m.setSelectedUserId(null); // remove selection as user has been deleted
        sessionBean.setSelectedUserId(null);
        m.getRoles().clear();
        m.resetToken();

        return null;
    }

    public String resetPassword() throws SaaSApplicationException {

        ManageUsersModel m = getModel();
        if (!m.isTokenValid()) {

            return null;
        }
        POUser user = toPOUser(m);
        Response r = getUserService().resetUserPassword(user,
                ui.getMarketplaceId());
        ui.handle(r, BaseBean.INFO_USER_PWD_RESET);
        m.setInitialized(false);
        m.resetToken();

        return null;
    }

    POUserDetails toPOUserDetails(ManageUsersModel m) {

        POUserDetails ud = new POUserDetails();
        ud.setAssignedRoles(getSelectedUserRoles(m.getRoles()));
        ud.setEmail(m.getEmail().getValue());
        ud.setFirstName(m.getFirstName().getValue());
        ud.setKey(m.getKey());
        ud.setLastName(m.getLastName().getValue());
        ud.setLocale(m.getLocale().getValue());
        String sal = m.getSalutation().getValue();
        if (!Strings.isEmpty(sal)) {
            ud.setSalutation(Salutation.valueOf(sal));
        }
        ud.setUserId(m.getUserId().getValue());
        ud.setVersion(m.getVersion());

        return ud;
    }

    POUser toPOUser(ManageUsersModel m) {

        POUser u = new POUser();
        u.setEmail(m.getEmail().getValue());
        u.setFirstName(m.getFirstName().getValue());
        u.setKey(m.getKey());
        u.setLastName(m.getLastName().getValue());
        // delete and resetPassword read the user by id - changes must be
        // ignored - so taking id from selection and not from model (may be
        // changed by input)
        u.setUserId(m.getSelectedUserId());
        u.setVersion(m.getVersion());

        return u;
    }

    public boolean isSaveDisabled() {

        ManageUsersModel m = getModel();
        boolean result = Strings.isEmpty(m.getSelectedUserId()) || m.isLocked();

        return result;
    }

    public boolean isResetPasswordDisabled() {

        ManageUsersModel m = getModel();
        boolean result = Strings.isEmpty(m.getSelectedUserId())
                || Strings.areStringsEqual(ui.getMyUserId(),
                        m.getSelectedUserId());

        return result;
    }

    public boolean isDeleteDisabled() {

        ManageUsersModel m = getModel();
        boolean result = Strings.isEmpty(m.getSelectedUserId())
                || Strings.areStringsEqual(ui.getMyUserId(),
                        m.getSelectedUserId());

        return result;
    }

    public boolean isRolesDisabled() {

        ManageUsersModel m = getModel();
        boolean result = Strings.isEmpty(m.getSelectedUserId()) || m.isLocked();

        return result;
    }

    public boolean isResetPasswordVisible() {

        ManageUsersModel m = getModel();
        boolean result = m.isResetPasswordButtonVisible();

        return result;
    }

    void refreshUser() {
        VOUserDetails u = sl.findService(IdentityService.class)
                .getCurrentUserDetails();
        ui.getSession(true).setAttribute(Constants.SESS_ATTR_USER, u);
    }

}
