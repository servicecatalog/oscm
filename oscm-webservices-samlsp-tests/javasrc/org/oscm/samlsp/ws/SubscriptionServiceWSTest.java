/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-1-13                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.samlsp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.samlsp.ws.base.WebserviceSAMLSPTestSetup;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOSubscription;

/**
 * Tests for secured SubscriptionService.
 * 
 * @author Mao
 * 
 */

public class SubscriptionServiceWSTest {

    private static final String namePrefix = "IntegrationTest_UserId";
    private static final String baseUrl = "http://localhost:8680/example";
    private static WebserviceSAMLSPTestSetup setup;
    private static VOInstanceInfo instance;
    private static VOOrganization supplier;
    private static SubscriptionService subscrServiceForSupplier;

    private VOSubscription createdSubscription = null;

    @BeforeClass
    public static void setUpOnce() throws Exception {

        WebserviceTestBase.getMailReader().deleteMails();
        WebserviceSAMLSPTestSetup.getOperator().addCurrency("EUR");

        setup = new WebserviceSAMLSPTestSetup();
        supplier = setup.createSupplier(namePrefix);
        subscrServiceForSupplier = ServiceFactory.getSTSServiceFactory()
                .getSubscriptionService(setup.getSupplierUserId(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void completeAsynModifySubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(baseUrl);
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");

        // when
        try {
            subscrServiceForSupplier.completeAsyncModifySubscription(
                    createdSubscription.getSubscriptionId(),
                    supplier.getOrganizationId(), instance);
        } catch (ObjectNotFoundException ex) {
            validateException(createdSubscription.getSubscriptionId(), ex);
            throw ex;
        }

    }

    @Test(expected = ObjectNotFoundException.class)
    public void abortAsynModifySubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(baseUrl);
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");

        // when
        try {
            subscrServiceForSupplier.abortAsyncModifySubscription(
                    createdSubscription.getSubscriptionId(),
                    supplier.getOrganizationId(), null);
        } catch (ObjectNotFoundException ex) {
            validateException(createdSubscription.getSubscriptionId(), ex);
            throw ex;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void completeAsynUpgradeSubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(baseUrl);
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");

        // when
        try {
            subscrServiceForSupplier.completeAsyncUpgradeSubscription(
                    createdSubscription.getSubscriptionId(),
                    supplier.getOrganizationId(), instance);
        } catch (ObjectNotFoundException ex) {
            validateException(createdSubscription.getSubscriptionId(), ex);
            throw ex;
        }

    }

    @Test(expected = ObjectNotFoundException.class)
    public void abortAsynUpgradeSubscription() throws Exception {
        // given
        createdSubscription = createSubscription();
        instance = new VOInstanceInfo();
        instance.setAccessInfo("PLATFORM");
        instance.setBaseUrl(baseUrl);
        instance.setInstanceId(createdSubscription.getSubscriptionId());
        instance.setLoginPath("/login");

        // when
        try {
            subscrServiceForSupplier.abortAsyncUpgradeSubscription(
                    createdSubscription.getSubscriptionId(),
                    supplier.getOrganizationId(), null);
        } catch (ObjectNotFoundException ex) {
            validateException(createdSubscription.getSubscriptionId(), ex);
            throw ex;
        }
    }

    protected static void validateException(String param,
            ObjectNotFoundException e) {
        assertEquals(ClassEnum.SUBSCRIPTION, e.getDomainObjectClassEnum());
        String[] params = e.getMessageParams();
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(param, params[0]);
        assertEquals("ex.ObjectNotFoundException.SUBSCRIPTION",
                e.getMessageKey());
    }

    private VOSubscription createSubscription() {
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        return subscription;
    }

}
