/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Element;

import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author kulle
 * 
 */
public class CustomerSubscriptionTest {

    private static final String ORGANIZATION_ID = "OrganizationId";
    private static final List<Long> UNIT_KEYS = Arrays.asList(
            Long.valueOf(100L), Long.valueOf(200L));

    private CustomerSubscriptionReport reporting;
    private SubscriptionDao subscriptionDao;
    private UnitDao unitDao;

    @Before
    public void setup() {
        subscriptionDao = mock(SubscriptionDao.class);
        unitDao = mock(UnitDao.class);
        reporting = new CustomerSubscriptionReport(subscriptionDao, unitDao);
        doReturn(UNIT_KEYS).when(unitDao).retrieveUnitKeysForUnitAdmin(
                anyLong());
    }

    private PlatformUser givenUser(boolean unitAdmin, boolean orgAdmin,
            OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setOrganizationId(ORGANIZATION_ID);
        o.setKey(1L);
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
    public void buildReportAsCustomerNonStringSubscriptionId() throws Exception {
        // given
        PlatformUser user = givenUser(false, false,
                OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionId");

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(user, reportResult);

        // then
        assertEquals(1, data.size());
        Element dataElement = getFirstElement(data);
        String result = XMLConverter.getNodeTextContentByXPath(
                dataElement.getOwnerDocument(), "/row/SUBSCRIPTIONID/text()");
        assertEquals("subscriptionId", result);
    }

    @Test
    public void buildReportUnitAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, false,
                OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionId");

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(user, reportResult);

        // then
        Mockito.verify(subscriptionDao).retrieveSubscriptionReportData(
                eq(ORGANIZATION_ID), eq(UNIT_KEYS));
    }

    @Test
    public void buildReportOrganizationAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, true, OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionId");

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(user, reportResult);

        // then
        Mockito.verify(subscriptionDao).retrieveSubscriptionReportData(
                eq(ORGANIZATION_ID));
    }

    @SuppressWarnings("boxing")
    private void mockReportData(Object value) {
        ReportResultData resultData = new ReportResultData();
        resultData.setColumnCount(1);
        resultData.setColumnName(Arrays.asList("SUBSCRIPTIONID"));
        resultData.setColumnType(Arrays.asList(Types.VARCHAR));
        resultData.setColumnValue(Arrays.asList(value));
        doReturn(Arrays.asList(resultData)).when(subscriptionDao)
                .retrieveSubscriptionReportData(anyString());
    }

    private static Element getFirstElement(List<Object> data) {
        Element dataElement = (Element) data.get(0);
        dataElement.getOwnerDocument().appendChild(
                dataElement.getOwnerDocument().adoptNode(dataElement));
        return dataElement;
    }

}
