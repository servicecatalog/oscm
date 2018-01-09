/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareStorage;

/**
 * @author Oliver Soehnges
 * 
 */
public class SequentialStorageBalancerTest {

    private VMPropertyHandler properties;

    @Before
    public void setup() throws Exception {
        properties = Mockito.mock(VMPropertyHandler.class);
    }

    @Test(expected = APPlatformException.class)
    public void testEmpty() throws Exception {
        // given
        SequentialStorageBalancer balancer = new SequentialStorageBalancer();

        // when
        balancer.next(properties);
    }

    @Test(expected = APPlatformException.class)
    public void testNoneEnabled() throws Exception {
        // given
        SequentialStorageBalancer balancer = new SequentialStorageBalancer();
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        VMwareStorage elm = inventory.addStorage("host",
                VMwareDatacenterInventoryTest.createDataStoreProperties("elm1",
                        "100", "100"));
        balancer.datastoreNames.add("elm1");
        elm.setEnabled(false);
        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm2", "100", "100"));
        balancer.datastoreNames.add("elm2");
        elm.setEnabled(false);
        balancer.setInventory(inventory);

        // when
        elm = balancer.next(properties);

        // then exception expected
    }

    @Test
    public void testSingle() throws Exception {

        SequentialStorageBalancer balancer = new SequentialStorageBalancer();

        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@storage]", "elm1");
        balancer.setConfiguration(xmlConfiguration);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        VMwareStorage elm = inventory.addStorage("host",
                VMwareDatacenterInventoryTest.createDataStoreProperties("elm1",
                        "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));

        balancer.setInventory(inventory);

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm1", elm.getName());

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm1", elm.getName());
    }

    @Test
    public void testMultiple() throws Exception {

        SequentialStorageBalancer balancer = new SequentialStorageBalancer();

        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@storage]", "elm1,elm2,elm3,elm4");
        balancer.setConfiguration(xmlConfiguration);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        // elm1 does not provide enough resources with respect to configured
        // limit
        VMwareStorage elm = inventory.addStorage("host",
                VMwareDatacenterInventoryTest.createDataStoreProperties("elm1",
                        "100", "40"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("50%"));

        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm2", "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));

        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm3", "100", "100"));
        elm.setEnabled(false);
        elm.setLimit(VMwareValue.parse("90%"));

        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm4", "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));

        balancer.setInventory(inventory);

        // getting elm2 since elm1 is not applicable
        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm2", elm.getName());

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm2", elm.getName());

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm2", elm.getName());

    }

    @Test
    public void testMultipleReduce() throws Exception {

        SequentialStorageBalancer balancer = new SequentialStorageBalancer();

        String balancerConfig = "<host><balancer storage=\"elm1,elm2,elm3\" /></host>";
        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@storage]", "elm1,elm2,elm3");
        balancer.setConfiguration(xmlConfiguration);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        VMwareStorage elm = inventory.addStorage("host",
                VMwareDatacenterInventoryTest.createDataStoreProperties("elm1",
                        "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));

        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm2", "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));

        balancer.setInventory(inventory);

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm1", elm.getName());

        // Now shorten list, so "elm2" is not longer next element
        inventory = new VMwareDatacenterInventory();

        elm = inventory.addStorage("host", VMwareDatacenterInventoryTest
                .createDataStoreProperties("elm3", "100", "100"));
        elm.setEnabled(true);
        elm.setLimit(VMwareValue.parse("90%"));
        balancer.setInventory(inventory);

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm3", elm.getName());
    }

}
