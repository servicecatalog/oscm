/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.filter;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Created by Marcin Maciaszczyk on 2015-09-11.
 */
public class RoleBasedFilterConfigEntry {

    private String page;
    private Set<String> rolesAllowed;

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    @XmlElementWrapper(name = "rolesAllowed")
    @XmlElement(name="role")
    public Set<String> getRolesAllowed() {
        return rolesAllowed;
    }

    public void setRolesAllowed(Set<String> rolesAllowed) {
        this.rolesAllowed = rolesAllowed;
    }

}
