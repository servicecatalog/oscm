/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: afschar                                                      
 *                                                                              
 *  Creation Date: 05.03.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.dialog.classic.operator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.usermanagement.POUserAndOrganization;
import org.oscm.internal.usermanagement.UserManagementService;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.operator.BaseOperatorBean;
import org.oscm.ui.beans.operator.OperatorSelectOrgBean;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.PartHandler;
import org.oscm.ui.model.Marketplace;
import org.oscm.validation.ArgumentValidator;


/**
 * Controller for operator manage users.
 *
 * @author afschar
 */
@ManagedBean(name = "operatorManageUsersCtrl")
@ViewScoped
public class OperatorManageUsersCtrl extends BaseOperatorBean implements
        Serializable {

    /**
     *
     */
    private static final String NO_SELECTION = "0";
    public static final String APPLICATION_BEAN = "appBean";
    private static final long serialVersionUID = -9126265695343363133L;

    transient ApplicationBean appBean;
    private String selectedUserKey;
    private List<String> dataTableHeaders = new ArrayList<>();
    private List<POUserAndOrganization> userAndOrganizations = new ArrayList<>();
    private List<Marketplace> marketplaces = new ArrayList<>();
    private boolean isInternalAuthMode;
    private Long maxRegisteredUsersCount;

    @ManagedProperty(value = "#{operatorManageUsersModel}")
    OperatorManageUsersModel model;

    @PostConstruct
    public void getInitialize() {
        isInternalAuthMode = getApplicationBean().isInternalAuthMode();
        initModel();
    }

    long getMaxRegisteredUsersCount() {
        if (maxRegisteredUsersCount == null) {
            VOConfigurationSetting configurationSetting = getConfigurationService()
                    .getVOConfigurationSetting(
                            ConfigurationKey.MAX_NUMBER_ALLOWED_USERS,
                            Configuration.GLOBAL_CONTEXT);
            maxRegisteredUsersCount = Long.valueOf(configurationSetting
                    .getValue());
        }
        return maxRegisteredUsersCount.longValue();
    }

    void initModel() {
        model.setMarketplaces(getSelectableMarketplaces());
        model.setMaxNumberOfRegisteredUsers(getMaxRegisteredUsersCount());
        model.setNumberOfRegisteredUsers(getAccountingService()
                .countRegisteredUsers());
        model.setInitialized(true);
    }

    void reinitUser() throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException {
        model.setUser(null);
        if (!isBlank(model.getUserId())) {
            VOUser reqUser = new VOUser();
            reqUser.setUserId(model.getUserId());
            model.setUser(getIdService().getUser(reqUser));
        }
    }

    ApplicationBean getApplicationBean() {
        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }
        return appBean;
    }

    List<POUserAndOrganization> getUsersList() {
        try {
            final List<VOUserDetails> users = getOperatorService().getUsers();
            List<POUserAndOrganization> poUsers = new ArrayList<>();
            for (VOUserDetails voUser : users) {
                POUserAndOrganization poUser = voUserToPO(voUser);
                poUsers.add(poUser);
            }
            return poUsers;
        } catch (OrganizationAuthoritiesException e) {
            ExceptionHandler.execute(e);
        }
        return Collections.emptyList();
    }

    private POUserAndOrganization voUserToPO(VOUserDetails voUser) {
        POUserAndOrganization poUser = new POUserAndOrganization();
        poUser.setKey(voUser.getKey());
        poUser.setUserId(voUser.getUserId());
        poUser.setEmail(voUser.getEMail());
        poUser.setOrganizationName(voUser.getOrganizationName());
        poUser.setOrganizationId(voUser.getOrganizationId());
        poUser.setStatus(voUser.getStatus());
        return poUser;
    }

    public List<String> getDataTableHeaders() {
        if (dataTableHeaders == null || dataTableHeaders.isEmpty()) {
            try {
                dataTableHeaders = Arrays.asList("userId", "email", "organizationName", "organizationId");
            } catch (Exception e) {
                throw new SaaSSystemException(e);
            }
        }
        return dataTableHeaders;
    }

    public int getUsersListSize() {
        return getUsersList().size();
    }

    public boolean isCheckResetPasswordSupported() {
        if (model.isUserIdChanged()) {
            try {
                reinitUser();
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        return getApplicationBean().isInternalAuthMode();
    }

    public boolean isExceedMaxNumberOfUsers() {
        if (model.getNumberOfRegisteredUsers() <= model
                .getMaxNumberOfRegisteredUsers())
            return false;
        else {
            return true;
        }
    }

    public String resetPasswordForUser() throws ObjectNotFoundException,
            OperationNotPermittedException, MailOperationException,
            OrganizationAuthoritiesException {

        VOUser user = model.getUser();

        if (user == null) {
            return OUTCOME_ERROR;
        }

        if (validateLdapUser(user)) {
            return OUTCOME_ERROR;
        }

        getOperatorService().resetPasswordForUser(user.getUserId());

        return getOutcome(true);
    }

    public String lockUser() throws ObjectNotFoundException,
            ValidationException, OrganizationAuthoritiesException {

        VOUser user = model.getUser();
        if (user != null) {
            getOperatorService().setUserAccountStatus(user,
                    UserAccountStatus.LOCKED);
            model.getUser().setStatus(UserAccountStatus.LOCKED);
            model.setUser(null);
        }

        return getOutcome(true);
    }

    public String unlockUser() throws ObjectNotFoundException,
            ValidationException, OrganizationAuthoritiesException {

        VOUser user = model.getUser();
        if (user != null) {
            getOperatorService().setUserAccountStatus(user,
                    UserAccountStatus.ACTIVE);
            model.getUser().setStatus(UserAccountStatus.ACTIVE);
            model.setUser(null);
        }

        return getOutcome(true);
    }

    public void updateSelectedUser() throws OperationNotPermittedException, ObjectNotFoundException, OrganizationRemovedException {
        for (POUserAndOrganization poUser : userAndOrganizations) {
            if (poUser.getKey() == Long.valueOf(selectedUserKey)) {
                VOUserDetails voUser = poUserToVO(poUser);
                model.setUser(voUser);
                return;
            }
        }
    }

    private VOUserDetails poUserToVO(POUserAndOrganization poUser) {
        VOUserDetails voUser = new VOUserDetails();
        voUser.setKey(poUser.getKey());
        voUser.setUserId(poUser.getUserId());
        voUser.setEMail(poUser.getEmail());
        voUser.setOrganizationName(poUser.getOrganizationName());
        voUser.setOrganizationId(poUser.getOrganizationId());
        voUser.setStatus(poUser.getStatus());
        return voUser;
    }

    public OperatorManageUsersModel getModel() {
        return model;
    }

    public void setModel(OperatorManageUsersModel model) {
        this.model = model;
    }

    public String importUsers() {
        if (!model.isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        ArgumentValidator.notNull("userImport", model.getUserImport());
        try {
            byte[] buffer = PartHandler.getBuffer(model.getUserImport());
            getUserService().importUsers(buffer, getSelectedOrganization(),
                    getSelectedMarketplace());
            model.resetToken();
            ui.handle("info.user.importStarted.updateHint", model.getUserImport()
                    .getSubmittedFileName());
        } catch (SaaSApplicationException ex) {
            ui.handleException(ex);
        } catch (IOException ex) {
            throw new SaaSSystemException(ex);
        }
        return OUTCOME_SUCCESS;
    }

    public void setSelectedUserKey(String userKey) {
        this.selectedUserKey = userKey;
    }

    public String getSelectedUserKey() {
        return selectedUserKey;
    }

    String getSelectedMarketplace() {
        if (model.getSelectedMarketplace().equals(NO_SELECTION)) {
            return null;
        } else {
            return model.getSelectedMarketplace();
        }
    }

    String getSelectedOrganization() {
        return ((OperatorSelectOrgBean) ui.findBean("operatorSelectOrgBean"))
                .getOrganizationId();
    }

    List<Marketplace> getSelectableMarketplaces() {
        if (isLoggedInAndPlatformOperator()) {
            if (marketplaces.isEmpty()) {
                for (VOMarketplace mp : getMarketplaceService()
                        .getMarketplacesForOperator()) {
                    marketplaces.add(new Marketplace(mp));
                }
                return marketplaces;
            }
        }
        return Collections.emptyList();
    }

    public boolean isPwdButtonEnabled() {
        return selectedUserKey != null && Long.valueOf(selectedUserKey) != 0L;
    }

    public boolean isLockButtonEnabled() throws ValidationException, ObjectNotFoundException {
        if (model.getUser() == null) {
            return false;
        }
        return isUserActive();
    }

    public boolean isUnlockButtonEnabled() throws ValidationException, ObjectNotFoundException {
        if (model.getUser() == null) {
            return false;
        }
        return isUserLocked();
    }

    private boolean isUserLocked() {
        if (model.getUser().getStatus().equals(UserAccountStatus.LOCKED)) {
            return true;
        }
        return false;
    }

    private boolean isUserActive() {
        if (model.getUser().getStatus().equals(UserAccountStatus.ACTIVE)) {
            return true;
        }
        return false;
    }

    private boolean validateLdapUser(VOUser user)
            throws ObjectNotFoundException {
        UserManagementService service = sl
                .findService(UserManagementService.class);

        String organizationId;
        organizationId = user.getOrganizationId();
        if (service.isOrganizationLDAPManaged(organizationId)) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_LDAPUSER_RESETPASSWORD);
            return true;
        }
        return false;
    }

    public List<POUserAndOrganization> getUserAndOrganizations() {
        if (userAndOrganizations.isEmpty()) {
            userAndOrganizations = getUsersList();
        }
        return userAndOrganizations;
    }

    public void setUserAndOrganizations(List<POUserAndOrganization> userAndOrganizations) {
        this.userAndOrganizations = userAndOrganizations;
    }

    public boolean isInternalAuthMode() {
        return isInternalAuthMode;
    }

    public void setInternalAuthMode(boolean internalAuthMode) {
        isInternalAuthMode = internalAuthMode;
    }
}
