/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContactHistory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.test.DateTimeHandling;

//Test functionality of BillingDataRetrievalService
@RunWith(MockitoJUnitRunner.class)
public class BillingDataRetrievalServiceBeanTest {

    @Mock
    private DataService dm;

    private Query query;

    @InjectMocks
    private BillingDataRetrievalServiceBean bdr = spy(new BillingDataRetrievalServiceBean());

    private static final String PAYMENT_TYPE_ID = "paymentTypeId";

    @Before
    public void setup() {
        query = mock(Query.class);
        doReturn(query).when(dm).createNamedQuery(anyString());
        doReturn(query).when(dm).createNativeQuery(anyString());
    }

    @Test
    public void testloadOrganizationBillingDataFromHistory_withNullOrganizationHistory() {

        // given
        long orgKey = 10000;
        long subKey = 1002;
        doReturn(null).when(dm).findLastHistory(any(Organization.class));

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals(null, addressData);
    }

    @Test
    public void testloadOrganizationBillingDataFromHistory_fromOrganizationHistory() {

        // given
        long orgKey = 10000;
        long subKey = 1002;

        OrganizationHistory history = new OrganizationHistory();
        history.getDataContainer().setOrganizationId("some_org");
        history.getDataContainer().setName("some_org");
        history.getDataContainer().setEmail("test@www.com");
        history.getDataContainer().setAddress("some_address");

        doReturn(history).when(dm).findLastHistory(any(Organization.class));
        doReturn(PAYMENT_TYPE_ID).when(bdr).loadPaymentTypeIdForSubscription(
                anyLong());
        doReturn(null).when(bdr).loadBillingContact(anyLong());

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals(history.getEmail(), addressData.getEmail());
        assertEquals(history.getOrganizationName(),
                addressData.getOrganizationName());
        assertEquals(history.getAddress(), addressData.getAddress());
        assertEquals(PAYMENT_TYPE_ID, addressData.getPaymentTypeId());
    }

    @Test
    public void testloadOrganizationBillingDataFromHistory_fromBillingContactHistory() {

        // given
        long orgKey = 10000;
        long subKey = 1002;

        OrganizationHistory orghistory = getSimpleOrgHistory();
        BillingContactHistory billingContactHistory = getSimpleBillingContactHistory();

        doReturn(orghistory).when(dm).findLastHistory(any(Organization.class));
        doReturn(PAYMENT_TYPE_ID).when(bdr).loadPaymentTypeIdForSubscription(
                anyLong());
        doReturn(billingContactHistory).when(bdr).loadBillingContact(anyLong());

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals(billingContactHistory.getEmail(), addressData.getEmail());
        assertEquals(billingContactHistory.getCompanyName(),
                addressData.getOrganizationName());
        assertEquals(billingContactHistory.getAddress(),
                addressData.getAddress());
        assertEquals(PAYMENT_TYPE_ID, addressData.getPaymentTypeId());
    }

    @Test
    public void testloadOrganizationBillingDataFromHistoryFromBillingContactHistoryNullable() {

        // given
        long orgKey = 10000;
        long subKey = 1002;

        OrganizationHistory orghistory = getNullOrgHistory();
        BillingContactHistory billingContactHistory = getSimpleBillingContactHistoryNull();

        doReturn(orghistory).when(dm).findLastHistory(any(Organization.class));
        doReturn(PAYMENT_TYPE_ID).when(bdr).loadPaymentTypeIdForSubscription(
                anyLong());
        doReturn(billingContactHistory).when(bdr).loadBillingContact(anyLong());

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals("", addressData.getEmail());
        assertEquals("",
                addressData.getOrganizationName());
        assertEquals("", addressData.getAddress());
        assertEquals(PAYMENT_TYPE_ID, addressData.getPaymentTypeId());
    }

    @Test
    public void testloadOrganizationBillingDataFromHistory_forceOrgAddressUsed() {

        // given
        long orgKey = 10000;
        long subKey = 1002;

        OrganizationHistory orgHistory = getSimpleOrgHistory();
        BillingContactHistory billingContactHistory = getSimpleBillingContactHistory();
        billingContactHistory.getDataContainer().setOrgAddressUsed(true);

        doReturn(orgHistory).when(dm).findLastHistory(any(Organization.class));
        doReturn(PAYMENT_TYPE_ID).when(bdr).loadPaymentTypeIdForSubscription(
                anyLong());
        doReturn(billingContactHistory).when(bdr).loadBillingContact(anyLong());

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals(orgHistory.getEmail(), addressData.getEmail());
        assertEquals(orgHistory.getOrganizationName(),
                addressData.getOrganizationName());
        assertEquals(orgHistory.getAddress(), addressData.getAddress());
        assertEquals(PAYMENT_TYPE_ID, addressData.getPaymentTypeId());
    }

    @Test
    public void testloadOrganizationBillingDataFromHistoryForceOrgAddressUsedNullable() {

        // given
        long orgKey = 10000;
        long subKey = 1002;

        OrganizationHistory orgHistory = getNullOrgHistory();
        BillingContactHistory billingContactHistory = getSimpleBillingContactHistoryNull();
        billingContactHistory.getDataContainer().setOrgAddressUsed(true);

        doReturn(orgHistory).when(dm).findLastHistory(any(Organization.class));
        doReturn(PAYMENT_TYPE_ID).when(bdr).loadPaymentTypeIdForSubscription(
                anyLong());
        doReturn(billingContactHistory).when(bdr).loadBillingContact(anyLong());

        // when
        OrganizationAddressData addressData = bdr
                .loadOrganizationBillingDataFromHistory(orgKey, subKey);

        // then
        assertEquals("", addressData.getEmail());
        assertEquals("",
                addressData.getOrganizationName());
        assertEquals("", addressData.getAddress());
        assertEquals(PAYMENT_TYPE_ID, addressData.getPaymentTypeId());
    }

    @Test
    public void testGetSubscriptionsForBilling() {

        // given
        long effectiveBillingEndDate = DateTimeHandling
                .calculateMillis("2015-06-15 00:00:00");
        long cutoffBillingEndDate = DateTimeHandling
                .calculateMillis("2015-06-30 23:59:59");
        long cutoffDeactivationDate = DateTimeHandling
                .calculateMillis("2015-07-30 12:00:00");

        Object[] obj1 = new Object[] { 10, 12, 14, 16 };
        List<Object[]> results = new ArrayList<>();
        results.add(obj1);

        doReturn(results).when(query).getResultList();

        // when
        List<BillingSubscriptionData> subscriptionsForBilling = bdr
                .getSubscriptionsForBilling(effectiveBillingEndDate,
                        cutoffBillingEndDate, cutoffDeactivationDate);

        // then
        verify(query).setParameter(eq("effectiveBillingEndDate"),
                eq(effectiveBillingEndDate));
        verify(query).setParameter(eq("cutoffBillingEndDate"),
                eq(cutoffBillingEndDate));
        verify(query).setParameter(eq("cutoffDeactivationDate"),
                eq(cutoffDeactivationDate));

        assertEquals(10, subscriptionsForBilling.get(0).getSubscriptionKey());

    }

    @Test
    public void testGetHistoriesForSubscriptionsAndBillingPeriod() {

        // given
        List<Long> subscriptionKeys = Arrays.asList(Long.valueOf(1200L),
                Long.valueOf(1300L));
        long startDate = DateTimeHandling
                .calculateMillis("2015-03-12 00:00:00");
        long endDate = DateTimeHandling.calculateMillis("2015-04-12 00:00:00");

        // when
        bdr.loadSubscriptionHistoriesForBillingPeriod(subscriptionKeys,
                startDate, endDate);

        // then
        verify(query).setParameter(eq("external"), eq(true));
        verify(query).setParameter(eq("startDate"), eq(new Date(startDate)));
        verify(query).setParameter(eq("endDate"), eq(new Date(endDate)));
        verify(query)
                .setParameter(eq("subscriptionKeys"), eq(subscriptionKeys));
    }

    private OrganizationHistory getSimpleOrgHistory() {

        OrganizationHistory history = new OrganizationHistory();
        history.getDataContainer().setOrganizationId("some_org");
        history.getDataContainer().setName("some_org");
        history.getDataContainer().setEmail("test@www.com");
        history.getDataContainer().setAddress("some_address");

        return history;
    }

    private OrganizationHistory getNullOrgHistory() {

        OrganizationHistory history = new OrganizationHistory();
        history.getDataContainer().setOrganizationId("some_org");
        history.getDataContainer().setName(null);
        history.getDataContainer().setEmail(null);
        history.getDataContainer().setAddress(null);

        return history;
    }

    private BillingContactHistory getSimpleBillingContactHistory() {

        BillingContactHistory history = new BillingContactHistory();
        history.getDataContainer().setOrgAddressUsed(false);
        history.getDataContainer().setCompanyName("some_company");
        history.getDataContainer().setEmail("test2@www.com");
        history.getDataContainer().setAddress("some_address2");

        return history;
    }

    private BillingContactHistory getSimpleBillingContactHistoryNull() {

        BillingContactHistory history = new BillingContactHistory();
        history.getDataContainer().setOrgAddressUsed(false);
        history.getDataContainer().setCompanyName(null);
        history.getDataContainer().setEmail(null);
        history.getDataContainer().setAddress(null);

        return history;
    }

}
