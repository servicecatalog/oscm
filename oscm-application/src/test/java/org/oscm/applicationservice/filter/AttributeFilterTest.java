/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 25, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.filter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.oscm.domobjects.ModifiedUda;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Unit test for AttributeFilter.
 * 
 * @author miethaner
 */
public class AttributeFilterTest {

    @Test
    public void testGetCustomerAttributeList() {

        Subscription sub = setupSubscription("default", "value", false, "abc");

        List<ServiceAttribute> list = AttributeFilter
                .getCustomAttributeList(sub);

        assertEquals(2, list.size());
        assertEquals("default", list.get(0).getValue());
        assertEquals("value", list.get(1).getValue());
        assertEquals(false, list.get(1).isEncrypted());
        assertEquals("abc", list.get(0).getControllerId());

    }

    @Test
    public void testGetSubscriptionAttributeListWithoutDS() {

        Subscription sub = setupSubscription("default", "value", false, "abc");

        List<ServiceAttribute> list = AttributeFilter
                .getSubscriptionAttributeList(sub,
                        Collections.<ModifiedUda> emptyList());

        assertEquals(2, list.size());
        assertEquals("default", list.get(0).getValue());
        assertEquals("value", list.get(1).getValue());
        assertEquals(false, list.get(1).isEncrypted());
        assertEquals("abc", list.get(0).getControllerId());
    }

    @Test
    public void testGetSubscriptionAttributeListWithModUDA() {

        Subscription sub = setupSubscription("default", "value", false, "abc");

        ModifiedUda mod = new ModifiedUda();
        mod.setTargetObjectKey(2L);
        mod.setValue("mod");

        List<ServiceAttribute> list = AttributeFilter
                .getSubscriptionAttributeList(sub, Arrays.asList(mod));

        assertEquals(2, list.size());
        assertEquals("default", list.get(0).getValue());
        assertEquals("mod", list.get(1).getValue());
        assertEquals(false, list.get(1).isEncrypted());
        assertEquals("abc", list.get(0).getControllerId());
    }

    private Subscription setupSubscription(String defaultValue, String value,
            boolean encrypted, String controllerId) {

        long custKey = 100L;
        long subKey = 200L;

        Subscription sub = new Subscription();
        Product prod = new Product();
        Organization vendor = new Organization();

        UdaDefinition udaDefC1 = new UdaDefinition();
        UdaDefinition udaDefC2 = new UdaDefinition();
        UdaDefinition udaDefS1 = new UdaDefinition();
        UdaDefinition udaDefS2 = new UdaDefinition();

        Uda udaC = new Uda();
        Uda udaS = new Uda();

        sub.setKey(subKey);
        sub.setProduct(prod);
        prod.setVendor(vendor);
        sub.setOrganizationKey(custKey);

        vendor.setUdaDefinitions(
                Arrays.asList(udaDefC1, udaDefC2, udaDefS1, udaDefS2));

        udaDefC1.setUdaId("udaDefC1");
        udaDefC1.setTargetType(UdaTargetType.CUSTOMER);
        udaDefC1.setDefaultValue(defaultValue);
        udaDefC1.setEncrypted(encrypted);
        udaDefC1.getDataContainer().setControllerId(controllerId);

        udaDefC2.setUdaId("udaDefC2");
        udaDefC2.setUdas(Arrays.asList(udaC));
        udaDefC2.setTargetType(UdaTargetType.CUSTOMER);
        udaDefC2.setDefaultValue(defaultValue);
        udaDefC2.setEncrypted(encrypted);
        udaDefC2.getDataContainer().setControllerId(controllerId);

        udaDefS1.setUdaId("udaDefS1");
        udaDefS1.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        udaDefS1.setDefaultValue(defaultValue);
        udaDefS1.setEncrypted(encrypted);
        udaDefS1.getDataContainer().setControllerId(controllerId);

        udaDefS2.setUdaId("udaDefS2");
        udaDefS2.setUdas(Arrays.asList(udaS));
        udaDefS2.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        udaDefS2.setDefaultValue(defaultValue);
        udaDefS2.setEncrypted(encrypted);
        udaDefS2.getDataContainer().setControllerId(controllerId);

        udaC.setKey(1L);
        udaC.setUdaValue(value);
        udaC.setTargetObjectKey(custKey);
        udaS.setKey(2);
        udaS.setUdaValue(value);
        udaS.setTargetObjectKey(subKey);

        return sub;
    }

}
