/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import static org.oscm.ui.beans.BaseBean.ERROR_GROUP_EXIST;
import static org.oscm.ui.beans.BaseBean.INFO_GROUP_CREATED;
import static org.oscm.ui.beans.BaseBean.OUTCOME_ERROR;
import static org.oscm.ui.beans.BaseBean.OUTCOME_SUCCESS;
import static org.oscm.ui.beans.BaseBean.getMarketplaceIdStatic;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.dialog.state.TableState;

/**
 * @author mao
 * 
 */
@ManagedBean
@ViewScoped
public class AddGroupCtrl extends UserGroupBaseCtrl implements Serializable {
    private static final String NEW_GROUP = "New Group";
    private static final long serialVersionUID = 3961718802529537580L;

    /**
     * EJB injected through setters.
     */
    private UserGroupService userGroupService;

    @ManagedProperty(value = "#{tableState}")
    private TableState tableState;

    @ManagedProperty(value = "#{manageGroupModel}")
    private ManageGroupModel manageGroupModel;

    @PostConstruct
    public void getInitialize() {
        manageGroupModel.setSelectedGroup(new POUserGroup());
        manageGroupModel.setModalTitle(getUi().getText(ManageGroupCtrl.ASSIGN_USERS_MODAL_TITLE, NEW_GROUP));
    }

    public String create() {
        if (manageGroupModel.getSelectedGroup() == null) {
            return OUTCOME_ERROR;
        }
        POUserGroup userGroup = null;
        try {
            userGroup = getUserGroupService().createGroup(manageGroupModel.getSelectedGroup(),
                    getMarketplaceIdStatic());
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO, INFO_GROUP_CREATED,
                    new Object[]{manageGroupModel.getSelectedGroup().getGroupName()});
            TableState ts = getTableState();
            ts.resetActiveAddPage();
        } catch (NonUniqueBusinessKeyException e) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_GROUP_EXIST,
                    new Object[]{manageGroupModel.getSelectedGroup().getGroupName()});
            return OUTCOME_ERROR;
        } catch (SaaSApplicationException e) {
            getUi().handleException(e);
            return OUTCOME_ERROR;
        }
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (userGroup != null) {
            req.setAttribute(ManageGroupModel.ATTRIBUTE_GROUP_ID, String.valueOf(userGroup.getKey()));
        }
        return OUTCOME_SUCCESS;
    }


    public UserGroupService getUserGroupService() {
        if (userGroupService == null) {
            userGroupService = getUi().findService(UserGroupService.class);
        }
        return userGroupService;
    }

    @EJB
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    public String getSelectedGroupId() {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return req.getParameter("groupId");
    }

    @Override
    public ManageGroupModel getManageGroupModel() {
        return manageGroupModel;
    }

    @Override
    public void setManageGroupModel(ManageGroupModel model) {
        this.manageGroupModel = model;
    }

    public void setTableState(TableState tableState) {
        this.tableState = tableState;
    }

    public TableState getTableState() {
        if (tableState == null) {
            tableState = getUi().findBean(TableState.BEAN_NAME);
        }
        return tableState;
    }
}
