/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Nov 27, 2014
 *
 *******************************************************************************/
package org.oscm.ui.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Created by ChojnackiD on 2014-11-27.
 */
@ManagedBean
@SessionScoped
public class SelectOrganizationIncludeBean {
    private String organizationId;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
