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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.vmware.LoggerMocking;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.impl.SimpleLogger;

/**
 * @author Dirk Bernsau
 * 
 */
public class EquipartitionHostBalancerTest {

    private VMPropertyHandler properties;
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private SimpleLogger mogger;

    @Before
    public void setup() throws Exception {
        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings,
                Messages.DEFAULT_LOCALE);
        properties = new VMPropertyHandler(settings);
        mogger = LoggerMocking
                .setDebugEnabledFor(EquipartitionHostBalancer.class);
    }

    @Test
    public void testSpreadEmpty() {
        double[] spread = EquipartitionHostBalancer.calculateSpread(
                new double[0], 0, new double[0]);
        assertTrue(spread.length == 0);
    }

    @Test
    public void testSpreadSingle() {
        double[] values = { 1 };
        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                new double[0]);
        assertTrue(spread.length == values.length);
        assertTrue(spread[0] == 0);
    }

    @Test
    public void testSpreadEven() {
        double[] values = { 1, 1, 1, 1, 1, 1 };
        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                new double[0]);
        assertTrue(spread.length == values.length);
        for (int i = 0; i < spread.length; i++) {
            assertTrue(spread[i] == 0);
        }
    }

    @Test
    public void testSpreadZero() {
        double[] values = { 0, 0, 0 };
        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                new double[0]);
        assertTrue(spread.length == values.length);
        for (int i = 0; i < spread.length; i++) {
            assertTrue(spread[i] == 0);
        }
    }

    @Test
    public void testSpreadUneven() {
        double[] values = { 1, 3 };
        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                new double[0]);
        assertTrue(spread.length == values.length);
        for (int i = 0; i < spread.length; i++) {
            assertTrue(spread[i] == 0.5);
        }
        double[] newValues = { 1, 2 };
        spread = EquipartitionHostBalancer.calculateSpread(newValues, 1,
                new double[0]);
        assertTrue(spread.length == values.length);
        // adding 1 to 1 is better than adding 1 to 2
        assertTrue(spread[0] == 0);
        assertTrue(spread[1] == 0.5);
    }

    @Test
    public void testSpreadNormalizerAllSame() {

        // two different norms, but both with same ratio
        double[] norm1 = { 1, 1 };
        double[] norm2 = { 3.4, 3.4 };

        // first set of values
        double[] values = { 1, 2 };

        double[] spread1 = EquipartitionHostBalancer.calculateSpread(values, 1,
                norm1);
        double[] spread2 = EquipartitionHostBalancer.calculateSpread(values, 1,
                norm2);
        assertTrue(spread1.length == spread2.length);
        for (int i = 0; i < spread1.length; i++) {
            // allow a little calculation drift
            assertTrue(Math.abs(spread1[i] - spread2[i]) < 0.000000001);
        }

        // now different values
        values[0] = 2;
        values[1] = 7;
        spread1 = EquipartitionHostBalancer.calculateSpread(values, 1, norm1);
        spread2 = EquipartitionHostBalancer.calculateSpread(values, 1, norm2);
        assertTrue(spread1.length == spread2.length);
        for (int i = 0; i < spread1.length; i++) {
            // allow a little calculation drift
            assertTrue(Math.abs(spread1[i] - spread2[i]) < 0.000000001);
        }
    }

    @Test
    public void testSpreadNormalizerDouble() {

        double[] norm = { 6, 8 };
        double[] values = { 3, 4 };

        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                norm);
        assertTrue(spread.length == values.length);
        // both systems are half full
        assertTrue(spread[0] == 0);
        assertTrue(spread[1] == 0);
    }

    @Test
    public void testSpreadNormalizerWrongArgs() {

        // negative and zero norms should be ignored
        double[] norm = { -1, 0 };
        double[] values = { 2, 2 };

        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 0,
                norm);
        assertTrue(spread.length == values.length);
        // both spreads should be 0 because both systems are equally filled
        assertTrue(spread[0] == 0);
        assertTrue(spread[1] == 0);
    }

    @Test
    public void testSpreadNormalizerFill() {

        double[] norm = { 4, 8 }; // second system is twice as strong
        double[] values = { 2, 2 }; // same current load

        double[] spread = EquipartitionHostBalancer.calculateSpread(values, 2,
                norm);
        assertTrue(spread.length == values.length);
        System.out.println(spread[0] + "   " + spread[1]);
        assertTrue(spread[0] == 0.6);
        // when adding 2 to the 2nd, both systems are 50% filled => 0 spread
        assertTrue(spread[1] == 0);
    }

    @Test(expected = RuntimeException.class)
    public void testAssess_wrongLength() {
        double[] values1 = { 1, 3 };
        double[] values2 = { 1 };
        double[] weights = { 1, 1 };
        EquipartitionHostBalancer.assess(new double[][] { values1, values2 },
                weights);
    }

    @Test
    public void testAssess_equalFirst() {
        // simulate a case where debugging is disabled
        Mockito.when(new Boolean(mogger.isDebugEnabled())).thenReturn(
                Boolean.FALSE);
        double[] values1 = { 1, 1 };
        double[] values2 = { 1, 1 };
        double[] weights = {}; // no weights default to 1
        int result = EquipartitionHostBalancer.assess(new double[][] { values1,
                values2 }, weights);
        assertTrue(result == 0);
    }

    @Test
    public void testAssess_secondLower() {
        double[] values1 = { 1, 0.4 };
        double[] values2 = { 1, 1 };
        double[] weights = {}; // no weights default to 1
        int result = EquipartitionHostBalancer.assess(new double[][] { values1,
                values2 }, weights);
        assertTrue(result == 1);
    }

    @Test
    public void testAssess_overWeight() {
        double[] values1 = { 0.6, 1 };
        double[] values2 = { 1, 0.6 };
        double[] weights = { 2, 1 };
        int result = EquipartitionHostBalancer.assess(new double[][] { values1,
                values2 }, weights);
        assertTrue(result == 0);
    }

    @Test
    public void testAssess_underWeight() {
        double[] values1 = { 0.6, 1 };
        double[] values2 = { 1, 0.6 };
        double[] weights = { 1, 2 };
        int result = EquipartitionHostBalancer.assess(new double[][] { values1,
                values2 }, weights);
        assertTrue(result == 1);
    }

    @Test
    public void testAssess_weightWins() {
        double[] values1 = { 0.2, 0.1 };
        double[] values2 = { 0.1, 0.29 };
        double[] weights = { 2, 1 };
        int result = EquipartitionHostBalancer.assess(new double[][] { values1,
                values2 }, weights);
        assertTrue(result == 1);
    }

    @Test
    public void testBalancer1() throws Exception {

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", true, 4096, 2048, 4, 2, 4);
        createHost(inventory, "host2", true, 4096, 1024, 4, 2, 4);
        createHost(inventory, "host3", true, 4096, 512, 4, 2, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 1, 1);
        balancer.setInventory(inventory);

        setCreateParameters(1024, 4);

        VMwareHost result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host3", result.getName());
    }

    @Test
    public void testBalancer2() throws Exception {

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", true, 4096, 2048, 4, 4, 4);
        createHost(inventory, "host2", true, 4096, 2048, 4, 1, 4);
        createHost(inventory, "host3", false, 4096, 2048, 4, 2, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 1, 1);
        balancer.setInventory(inventory);

        setCreateParameters(1024, 4);

        VMwareHost result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host2", result.getName());
    }

    @Test
    public void testBalancer3() throws Exception {

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", true, 4096, 1900, 16, 8, 4);
        createHost(inventory, "host2", true, 4096, 2048, 16, 6, 4);
        createHost(inventory, "host3", true, 4096, 2048, 16, 9, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 0.5, 0.25);
        balancer.setInventory(inventory);

        setCreateParameters(256, 4);

        VMwareHost result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host2", result.getName());

        setCreateParameters(1024, 1);

        // simulate a case where debugging is disabled
        Mockito.when(new Boolean(mogger.isDebugEnabled())).thenReturn(
                Boolean.FALSE);

        result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host1", result.getName());
    }

    @Test
    public void testBalancer_noValidHost() throws Exception {

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", false, 4096, 1900, 16, 8, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 0.5, 0.25);
        balancer.setInventory(inventory);

        setCreateParameters(256, 4);

        VMwareHost result = balancer.next(properties);
        assertNull(result);
    }

    @Test
    public void testBalancer_nEq1() throws Exception {

        // activate to have console debug output
        // LoggerMocking.addConsoleDebug(LoggerMocking
        // .setDebugEnabledFor(HostBalancer.class));
        // LoggerMocking.addConsoleDebug(mogger);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", true, 8192, 2048, 16, 8, 4);
        createHost(inventory, "host2", true, 2048, 1024, 16, 8, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 0.5, 0.25);
        balancer.setInventory(inventory);

        setCreateParameters(256, 4);

        VMwareHost result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host1", result.getName());

        setCreateParameters(1024, 1);

        // simulate a case where debugging is disabled
        Mockito.when(new Boolean(mogger.isDebugEnabled())).thenReturn(
                Boolean.FALSE);

        result = balancer.next(properties);
        assertNotNull(result);
        assertEquals("host1", result.getName());
    }

    @Test
    public void testBalancer_nEq2() throws Exception {

        // activate to have console debug output
        // LoggerMocking.addConsoleDebug(LoggerMocking
        // .setDebugEnabledFor(HostBalancer.class));
        // LoggerMocking.addConsoleDebug(mogger);

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createHost(inventory, "host1", true, 8192, 2048, 4, 8, 4);
        createHost(inventory, "host2", true, 4096, 1024, 8, 8, 4);

        EquipartitionHostBalancer balancer = getBalancer(1, 0.5, 0.25);
        balancer.setInventory(inventory);

        // CPU intensive VM
        setCreateParameters(256, 4);

        VMwareHost result = balancer.next(properties);
        assertNotNull(result);
        // host2 has more relative CPU capacity available
        assertEquals("host2", result.getName());

        // Memory intensive VM
        setCreateParameters(1024, 2);
        result = balancer.next(properties);
        assertNotNull(result);
        // host1 has more relative memory capacity available
        assertEquals("host1", result.getName());

        setCreateParameters(1024, 3);
        result = balancer.next(properties);
        assertNotNull(result);
        // with 3 requested CPUs host2 is better
        assertEquals("host2", result.getName());
    }

    @Test
    public void testBalancer_wrongConfig() throws Exception {
        // wrong configuration values should simply be ignored
        EquipartitionHostBalancer balancer = new EquipartitionHostBalancer();
        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@hosts]", "host1,host2,host3,host4,host5");
        xmlConfiguration.addProperty("[@memoryWeight]", "wrong");
        xmlConfiguration.addProperty("[@cpuWeight]", "wrong");
        xmlConfiguration.addProperty("[@vmWeight]", "");
        balancer.setConfiguration(xmlConfiguration);
    }

    @Test
    public void testBalancer_nullConfig() throws Exception {
        // no configuration should not create an exception
        new EquipartitionHostBalancer().setConfiguration(null);
    }

    @Test
    public void testBalancer_noHostsObject() throws Exception {
        // no host elements should not create an exception
        HierarchicalConfiguration configuration = Mockito
                .mock(HierarchicalConfiguration.class);
        // the mock shall not return a list on getList call
        Mockito.when(configuration.getList(Matchers.anyString())).thenReturn(
                null);
        new EquipartitionHostBalancer().setConfiguration(configuration);
        // no exception must occur
    }

    private EquipartitionHostBalancer getBalancer(double memWeight,
            double cpuWeight, double vmWeight) throws IOException, ConfigurationException {
        EquipartitionHostBalancer balancer = new EquipartitionHostBalancer();
        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@hosts]", "host1,host2,host3,host4,host5");
        xmlConfiguration.addProperty("[@memoryWeight]", memWeight);
        xmlConfiguration.addProperty("[@cpuWeight]", cpuWeight);
        xmlConfiguration.addProperty("[@vmWeight]", vmWeight);
        balancer.setConfiguration(xmlConfiguration);
        return balancer;
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

    private VMwareHost createHost(VMwareDatacenterInventory inventory,
            String name, boolean enabled, int memoryAvail, int memoryAlloc,
            int cpuAvail, int cpuAlloc, int vmAlloc) {
        VMwareHost host = inventory
                .addHostSystem((VMwareDatacenterInventoryTest
                        .createHostSystemProperties(name, "" + memoryAvail, ""
                                + cpuAvail)));
        host.setEnabled(enabled);
        host.setAllocatedCPUs(cpuAlloc);
        host.setAllocatedMemoryMB(memoryAlloc);
        host.setAllocatedVMs(vmAlloc);
        host.setMemoryLimit(VMwareValue.parse("100%"));
        return host;
    }
}
