/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;

/**
 * @author Mao
 * 
 */
public class POSubscriptionAndCustomerAssemblerTest {

    private static String SUBSCRIPTION_ID = "subscription_Id";
    private static String CUSTOMER_NAME = "customerExample";
    private static String CUSTOMER_ORGID = "customerExampleID";
    private static String PRODUCT_ID = "product_Id#23c5b996-306c-4e12-b330-e7ca9bd0f49910000";
    private static String PRODUCT_ID_TO_DISPLAY = "product_Id";
    private static long ACTIVATION_DATE = 1383844091182L;

    private POSubscriptionAndCustomer poSubscriptionAndCustomer = new POSubscriptionAndCustomer();

    @Test
    public void toPOSubscriptionAndCustomer() {
        Subscription subscription = anySubscription();
        poSubscriptionAndCustomer = POSubscriptionAndCustomerAssembler
                .toPOSubscriptionAndCustomer(subscription);
        assertEquals(subscription.getSubscriptionId(),
                poSubscriptionAndCustomer.getSubscriptionId());
        assertEquals(subscription.getOrganization().getName(),
                poSubscriptionAndCustomer.getCustomerName());
        assertEquals(subscription.getOrganization().getOrganizationId(),
                poSubscriptionAndCustomer.getCustomerId());
        assertEquals(PRODUCT_ID_TO_DISPLAY,
                poSubscriptionAndCustomer.getServiceId());
        assertEquals(subscription.getActivationDate().toString(),
                poSubscriptionAndCustomer.getActivation());

    }

    @Test
    public void toPOSubscriptionAndCustomer_MandatoryFieldsOnly() {
        // given
        Subscription subscription = anyMinimumSubscription();

        // when
        POSubscriptionAndCustomerAssembler
                .toPOSubscriptionAndCustomer(subscription);

        // then
        assertNull(subscription.getActivationDate());
    }

    private Subscription anySubscription() {
        Subscription subscription = new Subscription();
        Organization customer = new Organization();
        Product product = new Product();
        product.setTemplate(product);
        customer.setOrganizationId(CUSTOMER_ORGID);
        customer.setName(CUSTOMER_NAME);
        product.setProductId(PRODUCT_ID);
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        subscription.setOrganization(customer);
        subscription.setProduct(product);
        subscription.setActivationDate(Long.valueOf(ACTIVATION_DATE));
        return subscription;
    }

    private Subscription anyMinimumSubscription() {
        Subscription subscription = anySubscription();
        Organization customer = subscription.getOrganization();
        customer.setName(null);
        subscription.setActivationDate(null);
        subscription.setOrganization(customer);
        return subscription;
    }
}
