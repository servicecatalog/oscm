/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import static org.junit.Assert.*;

import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.vmware.LoggerMocking;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareHost;

/**
 * @author Dirk Bernsau
 *
 */
public class SequentialHostBalancerTest {

    private VMPropertyHandler properties;

    @Before
    public void setup() throws Exception {
        properties = Mockito.mock(VMPropertyHandler.class);
        LoggerMocking.setDebugEnabledFor(HostBalancer.class);
    }

    @Test
    public void testBalancerStorageSequentialEmpty() throws Exception {
        SequentialHostBalancer balancer = new SequentialHostBalancer();
        VMwareHost elm = balancer.next(properties);
        assertNull(elm);
    }

    @Test
    public void testBalancerStorageSequentialNoneEnabled() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        VMwareHost elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm1", "128", "1")));
        elm.setEnabled(false);

        elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm2", "128", "1")));
        elm.setEnabled(false);

        balancer.setInventory(inventory);
        elm = balancer.next(properties);
        assertNull(elm);
    }

    @Test
    public void testBalancerStorageSequentialSingle() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();

        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@balancer]","elm1");

        balancer.setConfiguration(xmlConfiguration);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        VMwareHost elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm1", "128", "1")));
        elm.setEnabled(true);

        balancer.setInventory(inventory);

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm1", elm.getName());

        elm = balancer.next(properties);
        assertNotNull(elm);
        assertEquals("elm1", elm.getName());
    }

    @Test
    public void testBalancerStorageSequentialMultiple() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();

        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@hosts]", "elm3,elm2,elm1,elm4");
        balancer.setConfiguration(xmlConfiguration);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        VMwareHost elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm1", "128", "1")));
        elm.setEnabled(true);

        elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm2", "128", "1")));
        elm.setEnabled(true);

        elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm3", "128", "1")));
        elm.setEnabled(false);

        elm = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("elm4", "128", "1")));
        elm.setEnabled(true);

        balancer.setInventory(inventory);

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
    public void testBalancerStorage_NoHost() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();
        assertFalse(balancer.isValid(null, properties));
    }

    @Test
    public void testBalancerStorage_Enablement() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        VMwareHost host = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("host1", "128", "1")));
        host.setEnabled(true);
        assertTrue(balancer.isValid(host, properties));

        host.setEnabled(false);
        assertFalse(balancer.isValid(host, properties));
    }

    @Test
    public void testBalancerStorage_Limits() throws Exception {

        SequentialHostBalancer balancer = new SequentialHostBalancer();

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        VMwareHost host = inventory.addHostSystem((VMwareDatacenterInventoryTest
                .createHostSystemProperties("host1", "128", "1")));
        host.setEnabled(true);
        host.setAllocatedVMs(0);
        host.setVMLimit(VMwareValue.parse("10"));
        assertTrue(balancer.isValid(host, properties));

        host.setAllocatedVMs(10);
        assertFalse(balancer.isValid(host, properties));

        host.setAllocatedVMs(0);
        host.setCPULimit(VMwareValue.parse("10"));
        host.setAllocatedCPUs(0);
        assertTrue(balancer.isValid(host, properties));
        host.setAllocatedCPUs(20);
        assertFalse(balancer.isValid(host, properties));

        host.setAllocatedCPUs(0);
        host.setMemoryLimit(VMwareValue.parse("1024"));
        host.setAllocatedMemoryMB(0);
        assertTrue(balancer.isValid(host, properties));
        host.setAllocatedMemoryMB(4096);
        assertFalse(balancer.isValid(host, properties));
    }
}
