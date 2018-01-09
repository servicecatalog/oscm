/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.business.model.VMwareVirtualMachine;
import org.oscm.app.vmware.remote.vmware.ManagedObjectAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;

/**
 * The data center inventory contains information about all resources available
 * in the vCenter. The inventory is filled by adding property sets obtained from
 * a VMware property collector.
 *
 * @author Dirk Bernsau
 *
 */
public class VMwareDatacenterInventory {

    private static final Logger logger = LoggerFactory
            .getLogger(VMwareDatacenterInventory.class);

    private HashMap<String, VMwareStorage> storages = new HashMap<String, VMwareStorage>();
    private HashMap<String, List<VMwareStorage>> storageByHost = new HashMap<String, List<VMwareStorage>>();
    private Collection<VMwareVirtualMachine> vms = new ArrayList<VMwareVirtualMachine>();
    private HashMap<String, VMwareHost> hostsSystems = new HashMap<String, VMwareHost>();

    private HashMap<Object, String> hostCache = new HashMap<Object, String>();

    /**
     * Adds a storage instance to the inventory based on given properties.
     *
     * @return the created storage instance
     */
    public VMwareStorage addStorage(String host,
            List<DynamicProperty> properties) {

        if (properties == null || properties.size() == 0) {
            return null;
        }

        VMwareStorage result = new VMwareStorage();
        for (DynamicProperty dp : properties) {
            String key = dp.getName();
            if ("summary.name".equals(key) && dp.getVal() != null) {
                result.setName(dp.getVal().toString());
            } else if ("summary.capacity".equals(key) && dp.getVal() != null) {
                result.setCapacity(VMwareValue
                        .fromBytes(Long.parseLong(dp.getVal().toString())));
            } else if ("summary.freeSpace".equals(key) && dp.getVal() != null) {
                result.setFreeStorage(VMwareValue
                        .fromBytes(Long.parseLong(dp.getVal().toString())));
            }
        }
        storages.put(result.getName(), result);

        if (storageByHost.containsKey(host)) {
            storageByHost.get(host).add(result);
        } else {
            List<VMwareStorage> storage = new ArrayList<VMwareStorage>();
            storage.add(result);
            storageByHost.put(host, storage);
        }
        return result;
    }

    /**
     * Adds a host instance to the inventory based on given properties.
     *
     * @return the created host instance
     */
    public VMwareHost addHostSystem(List<DynamicProperty> properties) {

        if (properties == null || properties.size() == 0) {
            return null;
        }

        VMwareHost result = new VMwareHost(this);
        for (DynamicProperty dp : properties) {
            String key = dp.getName();
            if ("name".equals(key) && dp.getVal() != null) {
                result.setName(dp.getVal().toString());
            } else if ("summary.hardware.memorySize".equals(key)
                    && dp.getVal() != null) {
                result.setMemorySizeMB(VMwareValue
                        .fromBytes(Long.parseLong(dp.getVal().toString()))
                        .getValue(Unit.MB));
            } else if ("summary.hardware.numCpuCores".equals(key)
                    && dp.getVal() != null) {
                result.setCpuCores(Integer.parseInt(dp.getVal().toString()));
            }
        }
        hostsSystems.put(result.getName(), result);
        return result;
    }

    /**
     * Adds a VM instance to the inventory based on given properties.
     *
     * @return the created VM instance
     */
    public VMwareVirtualMachine addVirtualMachine(
            List<DynamicProperty> properties, ManagedObjectAccessor serviceUtil)
            throws Exception {

        if (properties == null || properties.size() == 0) {
            return null;
        }

        VMwareVirtualMachine result = new VMwareVirtualMachine();
        for (DynamicProperty dp : properties) {
            String key = dp.getName();
            if ("name".equals(key) && dp.getVal() != null) {
                result.setName(dp.getVal().toString());
            } else if ("summary.config.memorySizeMB".equals(key)
                    && dp.getVal() != null) {
                result.setMemorySizeMB(
                        Integer.parseInt(dp.getVal().toString()));
            } else if ("summary.config.numCpu".equals(key)
                    && dp.getVal() != null) {
                result.setNumCpu(Integer.parseInt(dp.getVal().toString()));
            } else if ("runtime.host".equals(key)) {
                ManagedObjectReference mor = (ManagedObjectReference) dp
                        .getVal();
                Object cacheKey = mor == null ? null : mor.getValue();
                if (!hostCache.containsKey(cacheKey)) {
                    Object name = serviceUtil.getDynamicProperty(mor, "name");
                    if (name != null) {
                        hostCache.put(cacheKey, name.toString());
                    }
                }
                result.setHostName(hostCache.get(cacheKey));
            }
        }
        if (result.getHostName() != null) {
            vms.add(result);
        } else {
            logger.warn("Cannot determine host system for VM '"
                    + result.getName()
                    + "'. Check whether configured VMware API user host rights to access the host system.");
        }
        return result;
    }

    /**
     * Initializes the allocation data of the host by summing up all configured
     * (not the actual used) resources of all VMs deployed on each host.
     *
     */
    public void initialize() {
        for (VMwareHost hostSystem : hostsSystems.values()) {
            hostSystem.setAllocatedMemoryMB(0);
            hostSystem.setAllocatedCPUs(0);
            hostSystem.setAllocatedVMs(0);
        }
        for (VMwareVirtualMachine vm : vms) {
            VMwareHost hostSystem = hostsSystems.get(vm.getHostName());
            if (hostSystem != null) {
                long vmMemMBytes = vm.getMemorySizeMB();
                hostSystem.setAllocatedMemoryMB(
                        hostSystem.getAllocatedMemoryMB() + vmMemMBytes);
                hostSystem.setAllocatedCPUs(
                        hostSystem.getAllocatedCPUs() + vm.getNumCpu());
                hostSystem.setAllocatedVMs(hostSystem.getAllocatedVMs() + 1);
            }
        }
    }

    public VMwareStorage getStorage(String name) {
        return storages.get(name);
    }

    public List<VMwareStorage> getStorageByHost(String host) {
        return storageByHost.get(host);
    }

    public VMwareHost getHost(String name) {
        return hostsSystems.get(name);
    }

    public Collection<VMwareHost> getHosts() {
        return new ArrayList<VMwareHost>(hostsSystems.values());
    }

    /**
     * Reset the enabling information for all hosts and storages within the
     * inventory. The resources have late to be enabled one by when when reading
     * the configuration.
     */
    public void disableHostsAndStorages() {
        for (VMwareHost host : hostsSystems.values()) {
            host.setEnabled(false);
        }
        for (VMwareStorage storage : storages.values()) {
            storage.setEnabled(false);
        }
    }
}
