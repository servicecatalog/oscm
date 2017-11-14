/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: zhaohang                                                     
 *                                                                              
 *  Creation Date: 03.06.2013                                                      
 *                                                                              
 *  Completion Time: 04.06.2013                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * Tests for subscription attribute edit action
 * 
 * @author zhaohang
 * 
 */
public class SubscriptionServiceBeanEditSubscriptionAttributeTest {
    private SubscriptionServiceBean service;

    private static final String UDA_ID1 = "uda_id1";
    private static final String UDA_VALUE1 = "uda_value1";
    private static final String UDA_VALUE2 = "uda_value2";
    private static final String DEFAULT_VALUE1 = "default_value1";
    private final static String ORGANIZATION_ID = "organization_id";
    private final static String ORGANIZATION_NAME = "organization_name";

    @Before
    public void setup() {
        service = spy(new SubscriptionServiceBean());
        service.dataManager = mock(DataService.class);
        service.audit = mock(SubscriptionAuditLogCollector.class);
        Organization org = new Organization();
        org.setOrganizationId(ORGANIZATION_ID);
        org.setName(ORGANIZATION_NAME);
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        doReturn(user).when(service.dataManager).getCurrentUser();
    }

    @Test
    public void logSubscriptionAttribute_Update() {
        // given
        Subscription sub = givenSubscription();
        List<VOUda> udas = givenVoUdas(UDA_VALUE1, DEFAULT_VALUE1);

        // when
        service.logSubscriptionAttributeForEdit(sub, udas);

        // then
        verify(service.audit, times(1))
                .editSubscriptionAndCustomerAttributeByCustomer(
                        any(DataService.class), any(Organization.class),
                        eq(sub), eq(UDA_ID1), eq(UDA_VALUE1), anyString());
    }

    @Test
    public void logSubscriptionAttribute_CreateValueSameAsDefault() {
        // given
        Subscription sub = givenSubscription();
        List<VOUda> udas = givenVoUdas(DEFAULT_VALUE1, DEFAULT_VALUE1);
        List<VOUda> udas2 = new ArrayList<VOUda>();

        // when
        service.logSubscriptionAttributeForCreation(sub, udas, udas2);

        // then
        verify(service.audit, never())
                .editSubscriptionAndCustomerAttributeByCustomer(
                        any(DataService.class), any(Organization.class),
                        any(Subscription.class), anyString(), anyString(),
                        anyString());
    }

    @Test
    public void logSubscriptionAttribute_CreateValueDifferentFromDefault() {
        // given
        Subscription sub = givenSubscription();
        List<VOUda> udas = givenVoUdas(UDA_VALUE1, DEFAULT_VALUE1);
        List<VOUda> udas2 = new ArrayList<VOUda>();
        // when
        service.logSubscriptionAttributeForCreation(sub, udas, udas2);

        // then
        verify(service.audit, times(1))
                .editSubscriptionAndCustomerAttributeByCustomer(
                        any(DataService.class), any(Organization.class),
                        eq(sub), eq(UDA_ID1), eq(UDA_VALUE1), anyString());
    }

    @Test
    public void getUpdatedSubscriptionAttributes_Updated() {
        // given
        List<Uda> udas = givenUdas();
        List<VOUda> voUdas = givenVoUdas(UDA_VALUE2, DEFAULT_VALUE1);

        // when
        List<VOUda> result = service.getUpdatedSubscriptionAttributes(voUdas,
                udas);

        // then
        assertEquals(1, result.size());
        assertEquals(UDA_ID1, result.get(0).getUdaDefinition().getUdaId());
        assertEquals(UDA_VALUE2, result.get(0).getUdaValue());
    }

    @Test
    public void getUpdatedSubscriptionAttributes_NotUpdated() {
        // given
        List<Uda> udas = givenUdas();
        List<VOUda> voUdas = givenVoUdas(UDA_VALUE1, DEFAULT_VALUE1);

        // when
        List<VOUda> result = service.getUpdatedSubscriptionAttributes(voUdas,
                udas);

        // then
        assertEquals(0, result.size());
    }

    private Subscription givenSubscription() {
        Subscription sub = new Subscription();
        sub.setProduct(new Product());
        return sub;
    }

    private List<Uda> givenUdas() {
        List<Uda> udas = new ArrayList<Uda>();
        Uda uda = new Uda();
        UdaDefinition definition = new UdaDefinition();
        definition.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        definition.setUdaId(UDA_ID1);
        uda.setUdaDefinition(definition);
        uda.setUdaValue(UDA_VALUE1);
        udas.add(uda);
        return udas;
    }

    private List<VOUda> givenVoUdas(String value, String defaultValue) {
        VOUda uda1 = new VOUda();
        VOUdaDefinition definition1 = new VOUdaDefinition();
        definition1.setUdaId(UDA_ID1);
        definition1.setDefaultValue(defaultValue);
        uda1.setUdaDefinition(definition1);
        uda1.setUdaValue(value);

        List<VOUda> udas = new ArrayList<VOUda>();
        udas.add(uda1);
        return udas;
    }
}
