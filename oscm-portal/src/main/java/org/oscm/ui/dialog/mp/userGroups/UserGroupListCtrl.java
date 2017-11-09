/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author mao
 * 
 */
@ManagedBean
@ViewScoped
public class UserGroupListCtrl implements Serializable {

    private static final long serialVersionUID = 7739555891075054965L;
    public UiDelegate ui = new UiDelegate();
    private static final String REQUEST_PARAM_GROUPKEY_TO_DELETE = "groupKeyToDelete";
    private static final String REQUEST_PARAM_GROUPNAME_TO_DELETE = "groupNameToDelete";

    @ManagedProperty(value = "#{userGroupListModel}")
    private UserGroupListModel model;
    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean appBean;

    /**
     * EJB injected through setters.
     */
    private UserGroupService userGroupService;

    @PostConstruct
    public void initGroups() {
        UserGroupListModel m = getModel();
        VOUserDetails user = getCurrentUser();
        List<POUserGroup> groups = new ArrayList<>();
        if (user.hasAdminRole()) {
            groups = getUserGroupService().getGroupListForOrganization();
        } else if (user.hasUnitAdminRole()) {
            groups = getUserGroupService().getUserGroupsForUserWithRole(
                    getCurrentUser().getKey(),
                    UnitRoleType.ADMINISTRATOR.getKey());
        }
        m.setGroups(groups);
        for (POUserGroup group : groups) {
            group.setUserNum(getUserGroupService().getUserCountForGroup(
                    group.getKey(), group.isDefault()));
        }
    }
    

    ApplicationBean getApplicationBean() {
        return appBean;
    }

    public UserGroupListModel getModel() {
        return model;
    }

    public String deleteUserGroup() {
        UserGroupListModel m = getModel();
        POUserGroup groupToDelete = m.getSelectedGroup();
        if (groupToDelete == null) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_INVALID_GROUP);
            return BaseBean.OUTCOME_ERROR;
        }
        try {
            getUserGroupService().deleteGroup(groupToDelete);
            addMessage(FacesMessage.SEVERITY_INFO, BaseBean.INFO_GROUP_DELETED,
                    m.getSelectedGroup().getGroupName());

        } catch (ObjectNotFoundException e) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_GROUP_DELETED, m.getSelectedGroup()
                            .getGroupName());
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        } finally {
            getModel().setGroups(null);
        }

        return BaseBean.OUTCOME_REFRESH;
    }

    public void addMessage(FacesMessage.Severity severity, String msgKey,
            Object... params) {
        JSFUtils.addMessage(null, severity, msgKey, params);
    }

    public String determineDeleteGroup() {
        UserGroupListModel m = getModel();
        List<POUserGroup> groups = m.getGroups();
        String deleteGroupKey = ui.getExternalContext()
                .getRequestParameterMap().get(REQUEST_PARAM_GROUPKEY_TO_DELETE);
        String deleteGroupName = ui.getExternalContext()
                .getRequestParameterMap()
                .get(REQUEST_PARAM_GROUPNAME_TO_DELETE);

        if (deleteGroupKey != null && deleteGroupName != null) {
            for (POUserGroup group : groups) {
                if (String.valueOf(group.getKey()).equals(deleteGroupKey)
                        && group.getGroupName().equals(deleteGroupName)) {
                    m.setSelectedGroup(group);
                    m.setDeleteMessage(
                            (ui.getText(BaseBean.INFO_GROUP_DELETE_MSG_KEY,
                                    deleteGroupName)));
                }
            }
        }
        return null;
    }

    public void setModel(UserGroupListModel model) {
        this.model = model;
    }

    public ApplicationBean getAppBean() {
        return appBean;
    }

    public UserGroupService getUserGroupService() {
        if (userGroupService == null) {
            userGroupService = ui.findService(UserGroupService.class);
        }
        return userGroupService;
    }

    @EJB
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    private VOUserDetails getCurrentUser() {
        return BaseBean.getUserFromSessionWithoutException(FacesContext.getCurrentInstance());
    }
}
