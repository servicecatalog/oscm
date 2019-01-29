/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 25.01.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.balancer.LoadBalancerConfiguration;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.i18n.Messages;

/**
 * @author goebel
 *
 */
public class LoadBalancerConfigurationTest {

    private VMPropertyHandler properties;

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;

    private final static long _256_GB = (1024L * 1024L * 1024L * 256);
    private final static long _220_GB = (1024L * 1024L * 1024L * 220L);
    final String xml = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<essvcenter>"
            + "    <balancer class=\"org.oscm.app.vmware.business.balancer.EquipartitionHostBalancer\" cpuWeight=\"0.5\" memoryWeight=\"1\" vmWeight=\"1\"/>"
            + "<host enabled=\"true\" name=\"estvmwdev1.intern.est.fujitsu.com\">"
            + "<balancer class=\"org.oscm.app.vmware.business.balancer.EquipartitionStorageBalancer\" storage=\"VMdev0,VMdev1\"/>"
            + "</host>"
            + "<host enabled=\"true\" name=\"estvmwdev2.intern.est.fujitsu.com\">"
            + "<balancer class=\"org.oscm.app.vmware.business.balancer.EquipartitionStorageBalancer\" storage=\"VMdev0,VMdev1\"/>"
            + "</host>"
            + "<storage enabled=\"true\" limit=\"85%\" name=\"VMdev0\"/>"
            + "<storage enabled=\"true\" limit=\"85%\" name=\"VMdev1\"/>"
            + "</essvcenter>";

    @Before
    public void setup() throws Exception {

        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings,
                Messages.DEFAULT_LOCALE);
        properties = new VMPropertyHandler(settings);
    }

    @Test
    public void testXML() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();

        createHost(inventory, "estvmwdev1.intern.est.fujitsu.com", true, 4096,
                2048, 4, 2, 4);
        createHost(inventory, "estvmwdev2.intern.est.fujitsu.com", true, 4096,
                2048, 4, 2, 4);

        VMwareStorage vmdev0 = createStorage(inventory,
                "estvmwdev1.intern.est.fujitsu.com", "VMdev0", true, "85");
        VMwareStorage vmdev1 = createStorage(inventory,
                "estvmwdev2.intern.est.fujitsu.com", "VMdev1", true, "85");

        // when
        LoadBalancerConfiguration lbc = new LoadBalancerConfiguration(xml,
                inventory);

        VMwareHost h = lbc.getHostByName("estvmwdev1.intern.est.fujitsu.com");

        assertNotNull(h);

        // Request 246 GB, 4 CPUs
        setCreateParameters(256, 4);

        // when
        final String selectedStorage = h.getNextStorage(properties).getName();

        // then
        assertEquals(vmdev0.getName(), selectedStorage);

    }

    private VMwareHost createHost(VMwareDatacenterInventory inventory,
            String name, boolean enabled, int memoryAvail, int memoryAlloc,
            int cpuAvail, int cpuAlloc, int vmAlloc) {
        VMwareHost host = inventory.addHostSystem(
                (VMwareDatacenterInventoryTest.createHostSystemProperties(name,
                        "" + memoryAvail, "" + cpuAvail)));
        host.setEnabled(enabled);
        host.setAllocatedCPUs(cpuAlloc);
        host.setAllocatedMemoryMB(memoryAlloc);
        host.setAllocatedVMs(vmAlloc);
        host.setMemoryLimit(VMwareValue.parse("100%"));
        return host;
    }

    private VMwareStorage createStorage(VMwareDatacenterInventory inventory,
            String hostName, String name, boolean enabled, String limit) {

        VMwareStorage elm = inventory.addStorage(hostName,
                VMwareDatacenterInventoryTest.createDataStoreProperties(name,
                        String.valueOf(_256_GB), String.valueOf(_220_GB)));

        elm.setLimit(VMwareValue.parse(limit));
        elm.setEnabled(enabled);
        return elm;
    }

    /**
     * Configures the test setup with requested memory size and CPU cores.
     * 
     * @param mem
     * @param cpu
     */
    private void setCreateParameters(int mem, int cpu) {
        parameters.put(VMPropertyHandler.TS_NUMBER_OF_CPU, new Setting(
                VMPropertyHandler.TS_NUMBER_OF_CPU, Integer.toString(cpu)));
        parameters.put(VMPropertyHandler.TS_AMOUNT_OF_RAM, new Setting(
                VMPropertyHandler.TS_AMOUNT_OF_RAM, Integer.toString(mem)));
        parameters.put(VMPropertyHandler.TS_DISK_SIZE, new Setting(
                VMPropertyHandler.TS_DISK_SIZE, Integer.toString(20)));
    }
}
