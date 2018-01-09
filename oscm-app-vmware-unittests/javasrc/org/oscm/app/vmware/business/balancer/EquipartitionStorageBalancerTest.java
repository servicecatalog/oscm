/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareDatacenterInventoryTest;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.model.VMwareStorage;

/**
 * @author Dirk Bernsau
 * 
 */
public class EquipartitionStorageBalancerTest {

    private static final DecimalFormat DF = new DecimalFormat("#0.##");

    private VMPropertyHandler properties;

    @Before
    public void setup() throws Exception {
        properties = Mockito.mock(VMPropertyHandler.class);
    }

    @Test(expected = APPlatformException.class)
    public void testEmpty() throws Exception {
        // given
        EquipartitionStorageBalancer balancer = new EquipartitionStorageBalancer();

        // when
        balancer.next(properties);
    }

    @Test
    public void testBalancer_nullConfig() throws Exception {
        // no configuration should not create an exception
        new EquipartitionStorageBalancer().setConfiguration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalancer_noStorageObject() throws Exception {
        // no storage elements should create an exception
        HierarchicalConfiguration configuration = Mockito
                .mock(HierarchicalConfiguration.class);
        // the mock shall not return a list on getList call
        Mockito.when(configuration.getList(Matchers.anyString())).thenReturn(
                null);
        new EquipartitionStorageBalancer().setConfiguration(configuration);
    }

    @Test
    public void testBalancer1() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createStorage(inventory, "store1", true, 160, 120, "90%");

        EquipartitionStorageBalancer balancer = getBalancer("store1");
        balancer.setInventory(inventory);

        VMwareStorage storage = balancer.next(properties);
        assertEquals("store1", storage.getName());
    }

    @Test
    public void testBalancer2() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createStorage(inventory, "store1", true, 160, 120, "90%");
        createStorage(inventory, "store2", true, 160, 110, "90%");

        EquipartitionStorageBalancer balancer = getBalancer("store1,store2");
        balancer.setInventory(inventory);

        VMwareStorage storage = balancer.next(properties);
        assertNotNull(storage);
        assertEquals("store1", storage.getName());
    }

    @Test
    public void testBalancer3() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createStorage(inventory, "store1", false, 160, 120, "-16GB");
        createStorage(inventory, "store2", true, 160, 5, "144GB");
        createStorage(inventory, "store3", true, 160, 100, "90%");
        createStorage(inventory, "store4", true, 160, 120, "90%");
        createStorage(inventory, "store5", true, 160, 110, "90%");
        VMwareStorage store6 = createStorage(inventory, "store6", true, 160,
                120, "90%");

        // disable store6 by removing capacity afterwards
        store6.setCapacity(null);
        store6.setFreeStorage(null);
        assertTrue(store6.getLevel() == 1); // 100% full

        EquipartitionStorageBalancer balancer = getBalancer("store1,store2,store3,store5,store6");
        balancer.setInventory(inventory);

        VMwareStorage storage = balancer.next(properties);
        assertNotNull(storage);
        assertEquals("store5", storage.getName());
    }

    @Test
    public void testBalancer4() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        VMwareStorage storage1 = createStorage(inventory, "store1", true, 160,
                120, null);

        EquipartitionStorageBalancer balancer = getBalancer("store1");
        balancer.setInventory(inventory);

        VMwareStorage storage = balancer.next(properties);
        assertNotNull(storage);
        assertEquals("store1", storage.getName());
        double limit = storage.getLimit();

        storage1.setLimit(VMwareStorage.DEFAULT_STORAGE_LIMIT);
        storage = balancer.next(properties);
        assertNotNull(storage);
        assertEquals("store1", storage.getName());

        assertTrue(limit == storage.getLimit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBalancer_negativeStoreSize() throws Exception {
        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        createStorage(inventory, "store1", true, 160, 120, "90%");

        EquipartitionStorageBalancer balancer = getBalancer("store1");
        balancer.setInventory(inventory);

        Mockito.when(properties.getTemplateDiskSpaceMB())
                .thenReturn(new Double(-1));
        VMwareStorage storage = balancer.next(properties);
        assertNotNull(storage);
        assertEquals("store1", storage.getName());
    }

    private EquipartitionStorageBalancer getBalancer(String storages)
        throws ConfigurationException, IOException {
        EquipartitionStorageBalancer balancer = new EquipartitionStorageBalancer();
        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        xmlConfiguration.addProperty("[@storage]", storages);
        balancer.setConfiguration(xmlConfiguration);
        return balancer;
    }

    private VMwareStorage createStorage(VMwareDatacenterInventory inventory,
            String name, boolean enabled, long capacityGB, long freeGB,
            String limit) {
        ;
        VMwareStorage storage = inventory.addStorage(null,
                (VMwareDatacenterInventoryTest.createDataStoreProperties(name,
                        DF.format(VMwareValue.fromGigaBytes(capacityGB)
                                .getValue(Unit.BY)), DF.format(VMwareValue
                                .fromGigaBytes(freeGB).getValue(Unit.BY)))));
        storage.setEnabled(enabled);
        if (limit != null) {
            storage.setLimit(VMwareValue.parse(limit));
        } else {
            storage.setLimit(null);
        }
        return storage;
    }
}
