/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 09.12.2011                                                      
 *                                                                              
 *  Completion Time: 09.12.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;

public class MarketingPermissions {

    public static void createMarketingPermission(final long tpKey,
            final long sourceOrgKey, final long targetOrgKey,
            final DataService mgr) throws Exception {
        TechnicalProduct technicalProduct = mgr.getReference(
                TechnicalProduct.class, tpKey);
        Organization source = mgr
                .getReference(Organization.class, sourceOrgKey);
        Organization target = mgr
                .getReference(Organization.class, targetOrgKey);

        OrganizationReference template = new OrganizationReference(source,
                target,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        OrganizationReference orgRef = (OrganizationReference) mgr
                .find(template);
        if (orgRef == null) {
            mgr.persist(template);
            orgRef = template;
        }

        MarketingPermission permission = new MarketingPermission();
        permission.setOrganizationReference(orgRef);
        permission.setTechnicalProduct(technicalProduct);
        mgr.persist(permission);
    }
}
