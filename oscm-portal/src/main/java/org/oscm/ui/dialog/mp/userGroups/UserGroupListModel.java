/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.internal.usergroupmgmt.POUserGroup;

/**
 * @author mao
 * 
 */
@ViewScoped
@ManagedBean(name="userGroupListModel")
public class UserGroupListModel extends BaseModel {

    private static final long serialVersionUID = 7803451754585858737L;

    private String selectedTab;

    private String deleteMessage;

    private List<POUserGroup> groups;

    private POUserGroup selectedGroup;

    /**
     * @return the groups
     */
    public List<POUserGroup> getGroups() {
        return groups;
    }

    /**
     * @param groups
     *            the groups to set
     */
    public void setGroups(List<POUserGroup> groups) {
        this.groups = groups;
    }

    /**
     * @return the selectedGroup
     */
    public POUserGroup getSelectedGroup() {
        return selectedGroup;
    }

    /**
     * @param selectedGroup
     *            the selectedGroup to set
     */
    public void setSelectedGroup(POUserGroup selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    /**
     * @return the selectedTab
     */
    public String getSelectedTab() {
        return selectedTab;
    }

    /**
     * @param selectedTab
     *            the selectedTab to set
     */
    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    /**
     * @return the deleteMessage
     */
    public String getDeleteMessage() {
        return deleteMessage;
    }

    /**
     * @param deleteMessage
     *            the deleteMessage to set
     */
    public void setDeleteMessage(String deleteMessage) {
        this.deleteMessage = deleteMessage;
    }

}
