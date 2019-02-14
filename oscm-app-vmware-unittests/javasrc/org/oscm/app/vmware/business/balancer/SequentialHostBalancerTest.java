/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.vmware.LoggerMocking;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

        SequentialHostBalancer balancer = getSequentialHostBalancer();

       
    

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

    static SequentialHostBalancer getSequentialHostBalancer() throws Exception {
        
        final String hostlist = "elm3,elm2,elm1,elm4";
        return getSequentialHostBalancer(hostlist);
    }
        
    static SequentialHostBalancer getSequentialHostBalancer(String hostlist) throws Exception {
        SequentialHostBalancer balancer = new SequentialHostBalancer();
        
        
        StringBuffer doc = new StringBuffer();
        doc.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n");
        doc.append("<ess:essvcenter xmlns:ess=\"http://oscm.org/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://oscm.org/xsd ../../oscm-app-vmware\\resources\\XSD\\Loadbalancer_schema.xsd\">" + "\r\n");
        
        doc.append("<balancer class=\"org.oscm.app.vmware.business.balancer.SequentialHostBalancer\" ");
        
        doc.append(String.format("hosts=\"%s\" " + "/>\r\n", hostlist));
        
      
        
        for (String host : hostlist.split(",")) {
            doc.append(String.format("    <host enabled=\"true\" limit=\"85\" name=\"%s\"/>\r\n", host));
        }
        doc.append("</ess:essvcenter>");
        Document xmlDoc = XMLHelper.convertToDocument(doc.toString(), true);
        

        Node xmlConfiguration = xmlDoc.getElementsByTagName("balancer").item(0);
        balancer.setConfiguration(xmlConfiguration);
        return balancer;
    }

    @Test
    public void testBalancerStorageSequentialMultiple() throws Exception {

        SequentialHostBalancer balancer = getSequentialHostBalancer("elm3,elm2,elm1,elm4");
       
        
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
