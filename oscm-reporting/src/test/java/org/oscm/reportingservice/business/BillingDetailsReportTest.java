/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;

public class BillingDetailsReportTest {

    private static final long BILLING_RESULT_KEY = 100L;
    private static final long ORGANIZATION_KEY = 1L;
    private static final List<Long> UNIT_KEYS = Arrays.asList(
            Long.valueOf(100L), Long.valueOf(200L));

    private BillingDetailsReport reporting;
    private BillingDao billingDao;
    private UnitDao unitDao;

    @Before
    public void setup() {
        billingDao = mock(BillingDao.class);
        unitDao = mock(UnitDao.class);
        reporting = new BillingDetailsReport(billingDao, unitDao, null);
        doReturn(UNIT_KEYS).when(unitDao).retrieveUnitKeysForUnitAdmin(anyLong());
    }

    private PlatformUser givenUser(boolean unitAdmin, boolean orgAdmin,
            OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setKey(ORGANIZATION_KEY);
        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType roleType : roles) {
            OrganizationToRole otr = new OrganizationToRole();
            otr.setOrganizationRole(new OrganizationRole(roleType));
            otr.setOrganization(o);
            grantedRoles.add(otr);
        }
        o.setGrantedRoles(grantedRoles);

        PlatformUser user = new PlatformUser();
        user.setKey(10L);
        user.setOrganization(o);
        user.setLocale("en");

        if (orgAdmin) {
            RoleAssignment roleAssignment = new RoleAssignment();
            roleAssignment
                    .setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
            roleAssignment.setUser(user);
            user.getAssignedRoles().add(roleAssignment);
        }

        if (unitAdmin) {
            RoleAssignment roleAssignment = new RoleAssignment();
            roleAssignment
                    .setRole(new UserRole(UserRoleType.UNIT_ADMINISTRATOR));
            roleAssignment.setUser(user);
            user.getAssignedRoles().add(roleAssignment);
            System.out.println(user.isUnitAdmin());
        }

        return user;
    }

    @Test
    public void buildReportUnitAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, false,
                OrganizationRoleType.CUSTOMER);

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(user, BILLING_RESULT_KEY);

        // then
        verify(billingDao).retrieveBillingDetails(eq(BILLING_RESULT_KEY),
                eq(ORGANIZATION_KEY), eq(UNIT_KEYS));
    }

    @Test
    public void buildReportOrganizationAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, true, OrganizationRoleType.CUSTOMER);

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(user, BILLING_RESULT_KEY);

        // then
        verify(billingDao).retrieveBillingDetails(eq(BILLING_RESULT_KEY),
                eq(ORGANIZATION_KEY));
    }

}
