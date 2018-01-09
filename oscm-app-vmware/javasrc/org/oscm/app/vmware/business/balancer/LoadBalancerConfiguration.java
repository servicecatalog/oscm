/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML parser for the vCenter configuration file
 * 
 * @author soehnges
 */
public class LoadBalancerConfiguration {

    private static final Logger logger = LoggerFactory
            .getLogger(LoadBalancerConfiguration.class);

    private static final String ELEMENT_HOST = "host";
    private static final String ELEMENT_STORAGE = "storage";
    private static final String ELEMENT_BALANCER = "balancer";

    private List<VMwareHost> hostList;
    private List<VMwareStorage> storageList;
    private VMwareBalancer<VMwareHost> balancer;
    private XMLConfiguration xmlConfig;

    public LoadBalancerConfiguration(String xmlData,
            VMwareDatacenterInventory inventory) throws Exception {
        initialize(xmlData, inventory);
    }

    /**
     * Parses the given XML configuration and adds the information to the
     * inventory.
     */
    private void initialize(String xmlData, VMwareDatacenterInventory inventory)
            throws Exception {

        hostList = new ArrayList<VMwareHost>();
        storageList = new ArrayList<VMwareStorage>();
        inventory.disableHostsAndStorages();
        xmlConfig = new XMLConfiguration();
        xmlConfig.read(new StringReader(xmlData));

        List<HierarchicalConfiguration<ImmutableNode>> hosts = xmlConfig
                .configurationsAt(ELEMENT_HOST);

        for (HierarchicalConfiguration host : hosts) {

            String name = host.getString("[@name]");
            VMwareHost vmHost = inventory.getHost(name);
            if (vmHost == null) {
                logger.warn("The configured host " + name
                        + " is not available in the inventory.");
            } else {
                vmHost.setEnabled(host.getBoolean("[@enabled]", false));
                vmHost.setMemoryLimit(VMwareValue.parse(host
                        .getString("[@memory_limit]")));
                vmHost.setCPULimit(VMwareValue.parse(host
                        .getString("[@cpu_limit]")));
                vmHost.setVMLimit(VMwareValue.parse(host
                        .getString("[@vm_limit]")));
                hostList.add(vmHost);

                VMwareBalancer<VMwareStorage> stb = parseBalancer(host,
                        StorageBalancer.class, SequentialStorageBalancer.class,
                        inventory);
                vmHost.setBalancer(stb);
            }
        }

        List<HierarchicalConfiguration<ImmutableNode>> storages = xmlConfig
                .configurationsAt(ELEMENT_STORAGE);
        for (HierarchicalConfiguration storage : storages) {
            String name = storage.getString("[@name]");
            VMwareStorage vmStorage = inventory.getStorage(name);
            if (vmStorage == null) {
                logger.warn("The configured storage " + name
                        + " is not available in the inventory.");
            } else {
                vmStorage.setEnabled(storage.getBoolean("[@enabled]", false));
                vmStorage.setLimit(VMwareValue.parse(storage.getString(
                        "[@limit]", "90%")));
                storageList.add(vmStorage);
            }
        }
        balancer = parseBalancer(xmlConfig, HostBalancer.class,
                EquipartitionHostBalancer.class, inventory);
    }

    @SuppressWarnings("unchecked")
    private <E extends VMwareBalancer<?>> E parseBalancer(
            HierarchicalConfiguration parent, Class<E> targetClass,
            Class<? extends E> defaultBalancer,
            VMwareDatacenterInventory inventory) throws Exception {

        String balancerClass = parent.getString("balancer[@class]");
        Class<?> loadedClass = null;
        if (balancerClass != null) {
            loadedClass = this.getClass().getClassLoader()
                    .loadClass(balancerClass);
            if (!targetClass.isAssignableFrom(loadedClass)) {
                loadedClass = null;
                logger.warn("The configured balancer '" + balancerClass
                        + "' is not of type " + targetClass.getSimpleName());
            }
        }
        if (loadedClass == null) {
            balancerClass = defaultBalancer.getName();
            parent.addProperty("balancer[@class]", balancerClass);
            loadedClass = defaultBalancer;
        }
        HierarchicalConfiguration balancerConfig = parent
                .configurationAt(ELEMENT_BALANCER);
        E balancer = (E) loadedClass.newInstance();
        balancer.setConfiguration(balancerConfig);
        balancer.setInventory(inventory);
        return balancer;
    }

    public VMwareBalancer<VMwareHost> getBalancer() {
        return balancer;
    }

    public VMwareHost getHostByName(String hostname) throws Exception {
        for (VMwareHost host : hostList) {
            if (host.getName().equals(hostname)) {
                return host;
            }
        }

        throw new APPlatformException(Messages.getAll("error_unknown_host",
                new Object[] { hostname }));
    }
}
