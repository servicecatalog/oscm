/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    public LoadBalancerConfiguration(String xmlData,
            VMwareDatacenterInventory inventory) throws Exception {
        initialize(xmlData, inventory);
    }

    /**
     * Parses the given XML configuration and adds the information to the
     * inventory.
     */
    protected synchronized void initialize(String xmlData,
            VMwareDatacenterInventory inventory) throws Exception {

        hostList = new ArrayList<VMwareHost>();
        storageList = new ArrayList<VMwareStorage>();
        inventory.disableHostsAndStorages();

        Document document = XMLHelper.convertToDocument(xmlData);

        NodeList hosts1 = document.getElementsByTagName(ELEMENT_HOST);
        NodeList balancers1 = document.getElementsByTagName(ELEMENT_BALANCER);
        NodeList storages1 = document.getElementsByTagName(ELEMENT_STORAGE);

        // xmlConfig.read(new StringReader(xmlData));
        for (int i = 0; i < hosts1.getLength(); i++) {
            String name = hosts1.item(i).getAttributes().getNamedItem("name")
                    .getTextContent();
            VMwareHost vmHost = inventory.getHost(name);

            Node hostNode = hosts1.item(i);
            Node memory_limit = hostNode.getAttributes()
                    .getNamedItem("memory_limit");
            Node cpu_limit = hostNode.getAttributes().getNamedItem("cpu_limit");
            Node vm_limit = hosts1.item(i).getAttributes()
                    .getNamedItem("vm_limit");
            String enabled = hostNode.getAttributes().getNamedItem("enabled")
                    .getTextContent();

            if (vmHost == null) {
                logger.warn("The configured host " + name
                        + " is not available in the inventory.");
            } else {

                if (enabled.equalsIgnoreCase("true")) {
                    vmHost.setEnabled(true);
                } else {
                    vmHost.setEnabled(false);
                }

                if (memory_limit != null) {
                    vmHost.setMemoryLimit(
                            VMwareValue.parse(memory_limit.getTextContent()));
                }
                if (cpu_limit != null) {
                    vmHost.setCPULimit(
                            VMwareValue.parse(cpu_limit.getTextContent()));
                }
                if (vm_limit != null) {
                    vmHost.setVMLimit(
                            VMwareValue.parse(vm_limit.getTextContent()));
                }
                hostList.add(vmHost);

                List<Node> balencers = XMLHelper.getChildrenByTag(hostNode,
                        "balancer");

                if (balencers.isEmpty()) {
                    logger.warn("The configured host " + name
                            + " has a wrong balancer configuration.");
                } else {

                    VMwareBalancer<VMwareStorage> stb = parseBalancer(
                            balencers.get(0), StorageBalancer.class,
                            SequentialStorageBalancer.class, inventory);
                    vmHost.setBalancer(stb);
                }
            }
        }

        for (int i = 0; i < storages1.getLength(); i++) {
            String name = storages1.item(i).getAttributes().getNamedItem("name")
                    .getTextContent();
            String enabled = storages1.item(i).getAttributes()
                    .getNamedItem("enabled").getTextContent();
            Node limit = storages1.item(i).getAttributes()
                    .getNamedItem("limit");
            VMwareStorage vmStorage = inventory.getStorage(name);
            if (vmStorage == null) {
                logger.warn("The configured storage " + name
                        + " is not available in the inventory.");
            } else {
                if (enabled.equalsIgnoreCase("true")) {
                    vmStorage.setEnabled(true);
                } else {
                    vmStorage.setEnabled(false);
                }

                if (limit != null) {
                    vmStorage.setLimit(
                            VMwareValue.parse(limit.getTextContent()));
                }

                storageList.add(vmStorage);
            }

        }
        Node storage = balancers1.item(0);
        balancer = parseBalancer(storage, HostBalancer.class,
                EquipartitionHostBalancer.class, inventory);
    }

    @SuppressWarnings("unchecked")
    private <E extends VMwareBalancer<?>> E parseBalancer(Node balNode,
            Class<E> class1, Class<? extends E> class2,
            VMwareDatacenterInventory inventory) throws Exception {

        String balancerClass = balNode.getAttributes().getNamedItem("class")
                .getTextContent();
        Class<?> loadedClass = null;
        if (balancerClass != null) {
            loadedClass = this.getClass().getClassLoader()
                    .loadClass(balancerClass);
            if (!class1.isAssignableFrom(loadedClass)) {
                loadedClass = null;
                logger.warn("The configured balancer '" + balancerClass
                        + "' is not of type " + class1.getSimpleName());
            }
        }
        if (loadedClass == null) {
            balancerClass = class2.getName();
            balNode.getAttributes().getNamedItem("class")
                    .setTextContent(balancerClass);
            // host.addProperty("balancer[@class]", balancerClass);
            loadedClass = class2;
        }
        Node balancerConfig = balNode.getAttributes().getNamedItem("class");
        E balancer = (E) loadedClass.newInstance();
        balancer.setConfiguration(balNode);
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