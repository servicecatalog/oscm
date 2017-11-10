/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.oscm.reportingservice.dao.CustomerEventData;
import org.oscm.reportingservice.dao.EventDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author kulle
 * 
 */
public class CustomerEventReportTest {

    private static final String ORGANIZATION_ID = "OrganizationId";
    private static final List<Long> UNIT_KEYS = Arrays.asList(
            Long.valueOf(100L), Long.valueOf(200L));

    private CustomerEventReport reporting;
    private EventDao eventDao;
    private SubscriptionDao subscriptionDao;
    private UnitDao unitDao;

    private final String subscriptionid = "subscription_id";
    private final String subscriptiontkey = "100";

    @Before
    public void setup() {
        eventDao = mock(EventDao.class);
        subscriptionDao = mock(SubscriptionDao.class);
        unitDao = mock(UnitDao.class);
        reporting = new CustomerEventReport(eventDao, subscriptionDao, unitDao);
        doReturn(UNIT_KEYS).when(unitDao).retrieveUnitKeysForUnitAdmin(
                anyLong());
    }

    private PlatformUser givenUser(boolean unitAdmin, boolean orgAdmin,
            String locale, OrganizationRoleType... roles) {
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
        user.setLocale(locale);

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

    @SuppressWarnings("boxing")
    private void mockReportData(String columnName, Object value, int type) {
        ReportResultData resultData = new ReportResultData();
        resultData.setColumnCount(1);
        resultData.setColumnName(Arrays.asList(columnName));
        resultData.setColumnType(Arrays.asList(type));
        resultData.setColumnValue(Arrays.asList(value));
        doReturn(Arrays.asList(resultData)).when(subscriptionDao)
                .retrieveSubscriptionReportData(anyString());
        Map<String, String> map = new HashMap<String, String>();
        map.put(subscriptionid, (String) value);
        doReturn(map).when(subscriptionDao)
                .retrieveLastValidSubscriptionIdMap();
    }

    private CustomerEventData newLocalizedEventData(String locale,
            String eventDescription, String productId) {
        CustomerEventData eventData = new CustomerEventData();
        eventData.setActor("actor");
        eventData.setLocale(locale);
        eventData.setEventdescription(eventDescription);
        eventData.setProductid(productId);
        eventData.setSubscriptionid(subscriptionid);
        eventData.setSubscriptiontkey(new BigDecimal(subscriptiontkey));
        return eventData;
    }

    private CustomerEventData newEventData(String productId) {
        CustomerEventData eventData = new CustomerEventData();
        eventData.setActor("actor");
        eventData.setLocale(null);
        eventData.setEventdescription(null);
        eventData.setProductid(productId);
        eventData.setSubscriptionid(subscriptionid);
        eventData.setSubscriptiontkey(new BigDecimal(subscriptiontkey));
        return eventData;
    }

    private static Element getFirstElement(List<Object> data) {
        Element dataElement = (Element) data.get(0);
        dataElement.getOwnerDocument().appendChild(
                dataElement.getOwnerDocument().adoptNode(dataElement));
        return dataElement;
    }

    @Test
    public void buildReport_NoLocalizedDescription() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                Locale.JAPANESE.getLanguage(), OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionid", subscriptionid, Types.VARCHAR);
        doReturn(Arrays.asList(newEventData("productId#123"))).when(eventDao)
                .retrieveCustomerEventData(anyString());
        doReturn(Arrays.asList(newLocalizedEventData("", "", "productId#123")))
                .when(eventDao).retrieveLocalizedCustomerEventData(anyString(),
                        anyString());

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(reportResult, user, Locale.ITALIAN.getLanguage());

        // then
        verify(eventDao, times(1)).retrieveCustomerEventData(
                user.getOrganization().getOrganizationId());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.JAPANESE.getLanguage());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.ITALIAN.getLanguage());
        verify(subscriptionDao, times(1)).retrieveLastValidSubscriptionIdMap();
        Element dataElement = getFirstElement(data);
        assertEquals("", dataElement.getLastChild().getFirstChild()
                .getNodeValue());
        assertEquals(subscriptiontkey,
                dataElement.getElementsByTagName("SUBSCRIPTIONTKEY").item(0)
                        .getTextContent());
    }

    @Test
    public void buildReport_UserLocaleEqualsDefaultLocale() throws Exception {
        // given
        PlatformUser user = givenUser(false, true,
                Locale.JAPANESE.getLanguage(), OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionid", subscriptionid, Types.VARCHAR);
        doReturn(Arrays.asList(newEventData("productId#123"))).when(eventDao)
                .retrieveCustomerEventData(anyString());
        doReturn(
                Arrays.asList(newLocalizedEventData("ja", "desc",
                        "productId#123"))).when(eventDao)
                .retrieveLocalizedCustomerEventData(anyString(), anyString());

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting
                .buildReport(reportResult, user, Locale.JAPANESE.getLanguage());

        // then
        verify(eventDao, times(1)).retrieveCustomerEventData(
                user.getOrganization().getOrganizationId());
        verify(eventDao, times(2)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.JAPANESE.getLanguage());
        verify(subscriptionDao, times(1)).retrieveLastValidSubscriptionIdMap();
        Element dataElement = getFirstElement(data);
        assertEquals("ja", dataElement.getLastChild().getFirstChild()
                .getNodeValue());
    }

    @Test
    public void buildReport_UserLocale() throws Exception {
        // given
        PlatformUser user = givenUser(false, true, Locale.GERMAN.getLanguage(),
                OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionid", subscriptionid, Types.VARCHAR);
        doReturn(Arrays.asList(newEventData("productId#123"))).when(eventDao)
                .retrieveCustomerEventData(anyString());
        doReturn(
                Arrays.asList(newLocalizedEventData("de", "de desc",
                        "productId#123")))
                .doReturn(
                        Arrays.asList(newLocalizedEventData("en", "en desc",
                                "productId#123"))).when(eventDao)
                .retrieveLocalizedCustomerEventData(anyString(), anyString());

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);

        reporting.buildReport(reportResult, user, Locale.ENGLISH.getLanguage());

        // assert
        verify(eventDao, times(1)).retrieveCustomerEventData(
                user.getOrganization().getOrganizationId());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.GERMAN.getLanguage());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.ENGLISH.getLanguage());
        verify(subscriptionDao, times(1)).retrieveLastValidSubscriptionIdMap();
        Element dataElement = getFirstElement(data);
        assertEquals("de", dataElement.getLastChild().getFirstChild()
                .getNodeValue());
    }

    @Test
    public void buildReport_DefaultLocale() throws Exception {
        PlatformUser user = givenUser(false, true, Locale.GERMAN.getLanguage(),
                OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionid", subscriptionid, Types.VARCHAR);
        doReturn(Arrays.asList(newEventData("productId#123"))).when(eventDao)
                .retrieveCustomerEventData(anyString());
        doReturn(
                Arrays.asList(newLocalizedEventData(null, null, "productId#123")))
                .doReturn(
                        Arrays.asList(newLocalizedEventData("en", "en desc",
                                "productId#123"))).when(eventDao)
                .retrieveLocalizedCustomerEventData(anyString(), anyString());

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(reportResult, user, Locale.ENGLISH.getLanguage());

        // assert
        verify(eventDao, times(1)).retrieveCustomerEventData(
                user.getOrganization().getOrganizationId());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.GERMAN.getLanguage());
        verify(eventDao, times(1)).retrieveLocalizedCustomerEventData(
                user.getOrganization().getOrganizationId(),
                Locale.ENGLISH.getLanguage());
        verify(subscriptionDao, times(1)).retrieveLastValidSubscriptionIdMap();
        Element dataElement = getFirstElement(data);
        assertEquals("en", dataElement.getLastChild().getFirstChild()
                .getNodeValue());
    }

    @Test
    public void buildReport_AsCustomerWithSubscriptionId() throws Exception {
        PlatformUser user = givenUser(false, true,
                Locale.ENGLISH.getLanguage(), OrganizationRoleType.CUSTOMER);
        mockReportData("subscriptionid", "testSubId", Types.VARCHAR);
        doReturn(Arrays.asList(newEventData("productId#123"))).when(eventDao)
                .retrieveCustomerEventData(anyString());
        doReturn(
                Arrays.asList(newLocalizedEventData("en", "desc en",
                        "productId#123"))).when(eventDao)
                .retrieveLocalizedCustomerEventData(anyString(), anyString());

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(reportResult, user, Locale.ENGLISH.getLanguage());

        // then
        assertEquals(1, data.size());
        Element dataElement = getFirstElement(data);
        String result = XMLConverter.getNodeTextContentByXPath(
                dataElement.getOwnerDocument(), "/row/SUBSCRIPTIONID/text()");
        assertEquals("testSubId", result);
    }

    @Test
    public void buildReportUnitAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, false,
                Locale.ENGLISH.getLanguage(), OrganizationRoleType.CUSTOMER);

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(reportResult, user, Locale.ENGLISH.getLanguage());

        // then
        Mockito.verify(eventDao).retrieveCustomerEventData(eq(ORGANIZATION_ID),
                eq(UNIT_KEYS));
    }

    @Test
    public void buildReportOrganizationAdmin() throws Exception {
        // given
        PlatformUser user = givenUser(true, true, Locale.ENGLISH.getLanguage(),
                OrganizationRoleType.CUSTOMER);

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(reportResult, user, Locale.ENGLISH.getLanguage());

        // then
        Mockito.verify(eventDao).retrieveCustomerEventData(eq(ORGANIZATION_ID));
    }

}
