/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.vmware.business.balancer.DynamicEquipartitionStorageBalancer;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.business.model.VMwareVirtualMachine;
import org.oscm.app.vmware.remote.vmware.ManagedObjectAccessor;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;

/**
 * @author Dirk Bernsau
 *
 */
public class VMwareDatacenterInventoryTest {

    private VMwareDatacenterInventory inventory;
    private ManagedObjectAccessor serviceUtil;

    @Before
    public void setup() throws Exception {
        inventory = new VMwareDatacenterInventory();
        serviceUtil = Mockito.mock(ManagedObjectAccessor.class);
        Mockito.when(serviceUtil.getDynamicProperty(
                Matchers.any(ManagedObjectReference.class),
                Matchers.anyString())).thenReturn("hostname");
    }

    @Test
    public void testAddDatastore() {

        List<DynamicProperty> properties = createDataStoreProperties("ds1",
                "100", "50");
        VMwareStorage storage = inventory.addStorage("host", properties);
        assertNotNull(storage);
        assertEquals("ds1", storage.getName());
        assertTrue(0.5 == storage.getLevel());

        storage = inventory.getStorage("ds1");
        assertNotNull(storage);
        assertEquals("ds1", storage.getName());
        assertTrue(0.5 == storage.getLevel());

        assertNull(inventory.getStorage("ds2"));
    }

    @Test
    public void testAddDatastore_cap0() {

        List<DynamicProperty> properties = createDataStoreProperties("ds1", "0",
                "50");
        VMwareStorage storage = inventory.addStorage("host", properties);
        assertNotNull(storage);
        assertTrue(1 == storage.getLevel());
        properties = createDataStoreProperties("ds1", "1", "-1");
        storage = inventory.addStorage("host", properties);
        assertNotNull(storage);
        assertTrue(1 == storage.getLevel());
    }

    @Test
    public void testAddDatastore_noProps() {
        assertNull(inventory.addStorage("host", null));
        assertNull(
                inventory.addStorage("host", new ArrayList<DynamicProperty>()));
    }

    @Test
    public void testAddDatastore_nullValueProps() {
        List<DynamicProperty> properties = createNullValueProperties(
                "summary.name", "summary.capacity", "summary.freeSpace");
        VMwareStorage storage = inventory.addStorage("host", properties);
        assertNotNull(storage);
        assertNull(storage.getName());
    }

    @Test
    public void testAddHostSystem() {
        // given
        List<DynamicProperty> properties = createHostSystemProperties("host1",
                "512", "2");

        // when
        VMwareHost host = inventory.addHostSystem(properties);

        // then
        assertEquals("host1", host.getName());
        assertTrue(512 == host.getMemorySizeMB());
        assertTrue(2 == host.getCpuCores());

        host = inventory.getHost("host1");
        assertEquals("host1", host.getName());
        assertTrue(512 == host.getMemorySizeMB());
        assertTrue(2 == host.getCpuCores());
        assertTrue(host
                .getBalancer() instanceof DynamicEquipartitionStorageBalancer);
    }

    @Test
    public void testAddHostSystem_noProps() {
        assertNull(inventory.addHostSystem(null));
        assertNull(inventory.addHostSystem(new ArrayList<DynamicProperty>()));
    }

    @Test
    public void testAddHostSystem_nullValueProps() {
        List<DynamicProperty> properties = createNullValueProperties("name",
                "summary.hardware.memorySize", "summary.hardware.numCpuCores");
        VMwareHost host = inventory.addHostSystem(properties);
        assertNotNull(host);
        assertNull(host.getName());
    }

    @Test
    public void testAddVM() throws Exception {

        List<DynamicProperty> properties = createVMProperties("vm1", "512", "2",
                "host1");
        VMwareVirtualMachine vm = inventory.addVirtualMachine(properties,
                serviceUtil);
        assertNotNull(vm);
        assertEquals("vm1", vm.getName());
        assertTrue(vm.getMemorySizeMB() == 512);
        assertTrue(vm.getNumCpu() == 2);

        inventory.addVirtualMachine(properties, serviceUtil);
        Mockito.verify(serviceUtil, Mockito.times(1)).getDynamicProperty(
                Matchers.any(ManagedObjectReference.class),
                Matchers.anyString());
    }

    @Test
    public void testAddVM_noProps() throws Exception {
        assertNull(inventory.addVirtualMachine(null, serviceUtil));
        assertNull(inventory.addVirtualMachine(new ArrayList<DynamicProperty>(),
                serviceUtil));
    }

    @Test
    public void testAddVM_nullValueProps() throws Exception {
        List<DynamicProperty> properties = createNullValueProperties("name",
                "summary.config.memorySizeMB", "summary.config.numCpu",
                "runtime.host");
        VMwareVirtualMachine vm = inventory.addVirtualMachine(properties,
                serviceUtil);
        assertNotNull(vm);
        assertNull(vm.getName());
    }

    @Test
    public void testAddVM_nullHostSystem() throws Exception {
        Mockito.when(serviceUtil.getDynamicProperty(
                Matchers.any(ManagedObjectReference.class),
                Matchers.anyString())).thenReturn(null);
        List<DynamicProperty> properties = createVMProperties("vm", "512", "4",
                "host1");
        VMwareVirtualMachine vm = inventory.addVirtualMachine(properties,
                serviceUtil);
        assertNotNull(vm);
        assertNull(vm.getHostName());
    }

    @Test
    public void testInitialize() throws Exception {

        Mockito.when(serviceUtil.getDynamicProperty(
                Matchers.any(ManagedObjectReference.class),
                Matchers.anyString())).thenReturn("hostname")
                .thenReturn("other");

        List<DynamicProperty> properties = createVMProperties("vm1", "512", "2",
                "hostname");
        inventory.addVirtualMachine(properties, serviceUtil);
        properties = createVMProperties("vm2", "4096", "4", "hostname");
        inventory.addVirtualMachine(properties, serviceUtil);
        properties = createVMProperties("vm3", "2048", "1", "otherhost");
        inventory.addVirtualMachine(properties, serviceUtil);
        properties = createHostSystemProperties("hostname", "8192", "8");
        VMwareHost host = inventory.addHostSystem(properties);
        inventory.initialize();
        assertNotNull(host);
        assertEquals(6, host.getAllocatedCPUs());
    }

    public static List<DynamicProperty> createHostSystemProperties(String name,
            String memory, String cpuCount) {
        return createProperties("name", name, "summary.hardware.memorySize",
                Long.toString(new Long(memory).longValue() * 1024 * 1024),
                "summary.hardware.numCpuCores", cpuCount);
    }

    private static List<DynamicProperty> createProperties(
            Object... properties) {
        LinkedList<DynamicProperty> result = new LinkedList<DynamicProperty>();
        DynamicProperty p = null;
        for (int i = 0; i < properties.length; i++) {
            if (p == null) { // key
                p = new DynamicProperty();
                p.setName(properties[i].toString());
            } else { // value
                p.setVal(properties[i]);
                result.add(p);
                p = null;
            }
        }
        return result;
    }

    private static List<DynamicProperty> createNullValueProperties(
            String... properties) {
        List<DynamicProperty> result = new ArrayList<DynamicProperty>();
        for (int i = 0; i < properties.length; i++) {
            DynamicProperty p = new DynamicProperty();
            p.setName(properties[i]);
            result.add(p);
        }
        return result;
    }

    public static List<DynamicProperty> createDataStoreProperties(String name,
            String capacity, String free) {
        return createProperties("summary.name", name, "summary.capacity",
                capacity, "summary.freeSpace", free);
    }

    public static List<DynamicProperty> createVMProperties(String name,
            String memory, String cpu, String host) {
        ManagedObjectReference mor = new ManagedObjectReference();
        mor.setValue(host);
        return createProperties("name", name, "summary.config.memorySizeMB",
                memory, "summary.config.numCpu", cpu, "runtime.host", mor);
    }

}
