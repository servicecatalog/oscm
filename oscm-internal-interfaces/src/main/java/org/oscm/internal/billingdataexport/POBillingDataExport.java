/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import java.util.List;

public class POBillingDataExport extends POBillingExport {

    private static final long serialVersionUID = 4002065098301325133L;

    List<String> organizationIds;

    public List<String> getOrganizationIds() {
        return organizationIds;
    }

    public void setOrganizationIds(List<String> organizationIds) {
        this.organizationIds = organizationIds;
    }

}
