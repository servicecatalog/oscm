/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 01.07.15 10:45
 *
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.usergroupmgmt.UserGroupService;

@ManagedBean
@ViewScoped
public class SelectGroupCtrl {

    public UiDelegate ui = new UiDelegate();

    @ManagedProperty(value = "#{manageGroupModel}")
    private ManageGroupModel model;

    private UserGroupService service;

    public void setModel(ManageGroupModel model) {
        this.model = model;
    }

    public ManageGroupModel getModel() {
        return model;
    }

    public UserGroupService getService() {
        if (service == null) {
            service = ui.findService(UserGroupService.class);
        }
        return service;
    }

    @EJB
    public void setService(UserGroupService service) {
        this.service = service;
    }

}
