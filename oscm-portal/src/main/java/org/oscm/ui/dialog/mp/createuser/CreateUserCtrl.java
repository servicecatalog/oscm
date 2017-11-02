/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.createuser;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.POUserAndSubscriptions;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.string.Strings;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.profile.FieldData;

/**
 * @author weiser
 * 
 */
@ManagedBean
@ViewScoped
public class CreateUserCtrl {

    UiDelegate ui = new UiDelegate();

    @ManagedProperty(value = "#{createUserModel}")
    private CreateUserModel model;
    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{tableState}")
    private TableState tableState;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    /**
     * EJB injected through setters.
     */
    private UserService userService;
    private UserGroupService userGroupService;

    transient Boolean rolesColumnVisible;

    @PostConstruct
    public void init() {
        POUserAndSubscriptions data = getUserService().getNewUserData();
        CreateUserModel m = model;
        m.setEmail(new FieldData<String>(null, false, true));
        m.setFirstName(new FieldData<String>(null, false));
        m.setLastName(new FieldData<String>(null, false));
        m.setLocale(new FieldData<>(data.getLocale(), false, true));
        m.setUserId(new FieldData<String>(null, false, true));
        m.setSalutation(new FieldData<String>(null, false));
    }

    public String create() throws SaaSApplicationException {
    
        CreateUserModel m = model;
        POUserAndSubscriptions user = toPOUserAndSubscriptions(m);
        String outcome = BaseBean.OUTCOME_SHOW_DETAILS;
        
        try {
            Response response = getUserService().createNewUser(user,
                    ui.getMarketplaceId());
            if (response.getReturnCodes().size() > 0) {
                outcome = BaseBean.OUTCOME_PENDING;
            } else {
                // reset user table paging if user was created
                TableState ts = tableState;
                ts.resetActivePages();
            }
            ui.handle(response, BaseBean.INFO_USER_CREATED, user.getUserId());
        } catch (ObjectNotFoundException ex) {
            ui.handleException(ex);
            outcome = BaseBean.OUTCOME_ERROR;
        } catch (MailOperationException e) {
            if (applicationBean.isInternalAuthMode()) {
                ui.handleError(null, BaseBean.ERROR_USER_CREATE_MAIL);
            } else {
                ui.handleError(null,
                        BaseBean.ERROR_USER_CREATE_MAIL_NOT_INTERNAL);
            }
            outcome = BaseBean.OUTCOME_ERROR;
        }
        
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        req.setAttribute(CreateUserModel.ATTRIBUTE_USER_ID, user.getUserId());
        
        return outcome;
    }

    POUserAndSubscriptions toPOUserAndSubscriptions(CreateUserModel m) throws MarketplaceRemovedException {

        POUserAndSubscriptions uas = new POUserAndSubscriptions();
        uas.setEmail(m.getEmail().getValue());
        uas.setFirstName(m.getFirstName().getValue());
        uas.setLastName(m.getLastName().getValue());
        uas.setLocale(m.getLocale().getValue());
        uas.setTenantId(sessionBean.getTenantID());
        String sal = m.getSalutation().getValue();
        if (!Strings.isEmpty(sal)) {
            uas.setSalutation(Salutation.valueOf(sal));
        }
        uas.setUserId(m.getUserId().getValue());

        return uas;
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = ui.findService(UserService.class);
        }
        return userService;
    }

    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
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

    public TableState getTableState() {
        return tableState;
    }

    public void setTableState(TableState tableState) {
        this.tableState = tableState;
    }

    public void setModel(CreateUserModel model) {
        this.model = model;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public CreateUserModel getModel() {
        return model;
    }

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
}
