/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.filter;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Marcin Maciaszczyk on 2015-09-11.
 */
@XmlRootElement
public class RoleBasedFilterConfig {

    private Set<RoleBasedFilterConfigEntry> entries;

    @XmlElement(name = "configEntry")
    public Set<RoleBasedFilterConfigEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<RoleBasedFilterConfigEntry> entries) {
        this.entries = entries;
    }

}
