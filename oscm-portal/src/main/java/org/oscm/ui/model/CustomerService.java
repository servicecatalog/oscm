/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOCustomerService;

public class CustomerService extends Service {

    private static final long serialVersionUID = -9216724391383944522L;

    private VOCustomerService vo;

    private Service template;

    public CustomerService(VOCustomerService vo, Service template) {
        super(vo);
        this.vo = vo;
        this.template = template;
    }

    @Override
    public String getOrganizationDisplayName() {
        String orgId = getOrganizationId();
        String orgName = getOrganizationName();
        if (orgName != null && orgName.trim().length() > 0) {
            StringBuffer rc = new StringBuffer(orgName);
            if (orgId != null && orgId.trim().length() > 0) {
                rc.append(" (");
                rc.append(orgId);
                rc.append(')');
            }
            return rc.toString();
        }
        return orgId;
    }

    @Override
    public String getOrganizationName() {
        return vo.getOrganizationName();
    }

    @Override
    public String getOrganizationId() {
        return vo.getOrganizationId();
    }

    @Override
    public Long getOrganizationKey() {
        return vo.getOrganizationKey();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getServiceId() {
        return null;
    }

    @Override
    public boolean isNoMarketplaceAssigned() {
        return template.isNoMarketplaceAssigned();
    }

}
