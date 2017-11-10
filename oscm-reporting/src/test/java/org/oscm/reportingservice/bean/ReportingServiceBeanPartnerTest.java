/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author kulle
 * 
 */
public class ReportingServiceBeanPartnerTest {

    private ReportingServiceBean bean;
    private ReportingServiceBeanLocal beanLocal;

    private PlatformUser user;

    @Before
    public void setup() {
        bean = spy(new ReportingServiceBean());
        beanLocal = spy(new ReportingServiceBeanLocal());
        bean.delegate = beanLocal;
    }

    private void givenUser(UserRoleType roleType) {
        Organization o = new Organization();
        o.setKey(1L);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganization(o);
        otr.setOrganizationRole(new OrganizationRole(OrganizationRoleType
                .correspondingOrgRoleForUserRole(roleType)));
        Set<OrganizationToRole> orgRoles = new HashSet<OrganizationToRole>();
        orgRoles.add(otr);
        o.setGrantedRoles(orgRoles);

        user = new PlatformUser();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setRole(new UserRole(roleType));
        ra.setUser(user);
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        roles.add(ra);
        user.setOrganization(o);
        user.setAssignedRoles(roles);
    }

    @Test
    public void getBrokerRevenueShareReport_reseller() throws Exception {
        // given
        givenUser(UserRoleType.RESELLER_MANAGER);
        doReturn(user).when(beanLocal).loadUser(anyString());

        // when
        RDOPartnerReport rdoPartnerReport = bean.getBrokerRevenueShareReport(
                "sessionId", 13, 2012);

        // then
        assertEquals("", rdoPartnerReport.getPeriodStart());
        assertEquals("", rdoPartnerReport.getPeriodEnd());
        assertTrue(rdoPartnerReport.getCurrencies().isEmpty());
    }

}
