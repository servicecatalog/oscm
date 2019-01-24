/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileLocator.FileLocatorBuilder;
import org.apache.commons.configuration2.tree.ImmutableNode;
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

import com.sun.org.apache.xerces.internal.parsers.XML11Configuration;

/**
 * XML parser for the vCenter configuration file
 * 
 * @author soehnges
 */
public class LoadBalancerConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(LoadBalancerConfiguration.class);

	private static final String ELEMENT_HOST = "host";
	private static final String ELEMENT_STORAGE = "storage";
	private static final String ELEMENT_BALANCER = "balancer";

	private List<VMwareHost> hostList;
	private List<VMwareStorage> storageList;
	private VMwareBalancer<VMwareHost> balancer;
	private XMLConfiguration xmlConfig;

	public LoadBalancerConfiguration(String xmlData, VMwareDatacenterInventory inventory) throws Exception {
		initialize(xmlData, inventory);
	}

	/**
	 * Parses the given XML configuration and adds the information to the inventory.
	 */
	private synchronized void initialize(String xmlData, VMwareDatacenterInventory inventory) throws Exception {

		hostList = new ArrayList<VMwareHost>();
		storageList = new ArrayList<VMwareStorage>();
		inventory.disableHostsAndStorages();

		final File file = createFile(xmlData);


			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			        .newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList  hosts1 = document.getElementsByTagName(ELEMENT_HOST);
			NodeList  balancers1 = document.getElementsByTagName(ELEMENT_BALANCER);
			NodeList  storages1 = document.getElementsByTagName(ELEMENT_STORAGE);
			hosts1.item(0).getAttributes().getNamedItem("name");

		// xmlConfig.read(new StringReader(xmlData));
			for (int i = 0; i < hosts1.getLength(); i++) {
			String name = hosts1.item(i).getAttributes().getNamedItem("name").getTextContent();
			VMwareHost vmHost = inventory.getHost(name);
			
			Node memory_limit =	hosts1.item(i).getAttributes().getNamedItem("memory_limit");
			Node cpu_limit =	hosts1.item(i).getAttributes().getNamedItem("cpu_limit");
			Node vm_limit =	hosts1.item(i).getAttributes().getNamedItem("vm_limit");
			String enabled = hosts1.item(i).getAttributes().getNamedItem("enabled").getTextContent();

			if (vmHost == null) {
				logger.warn("The configured host " + name + " is not available in the inventory.");
			} else {
				
				if(enabled.equalsIgnoreCase("true")) {
					vmHost.setEnabled(true);
				}
				else {
					vmHost.setEnabled(false);
				}
			}

				

			if (memory_limit != null) {
				vmHost.setMemoryLimit(VMwareValue.parse(memory_limit.getTextContent()));
			}
			if (cpu_limit != null) {
				vmHost.setCPULimit(VMwareValue.parse(cpu_limit.getTextContent()));
			}
			if (vm_limit != null) {
				vmHost.setVMLimit(VMwareValue.parse(vm_limit.getTextContent()));
			}
			hostList.add(vmHost);
			
			Node host = hosts1.item(i).getChildNodes().item(1);
			
			if (host == null ) {
				logger.warn("The configured host " + name + " has a wrong balancer configuration.");
			}

				VMwareBalancer<VMwareStorage> stb = parseBalancer(host, StorageBalancer.class,
						SequentialStorageBalancer.class, inventory);
				vmHost.setBalancer(stb);
			}
		

			for (int i = 0; i < storages1.getLength(); i++) {
			String name = storages1.item(i).getAttributes().getNamedItem("name").getTextContent();
			String enabled = storages1.item(i).getAttributes().getNamedItem("enabled").getTextContent();
			Node limit = storages1.item(i).getAttributes().getNamedItem("limit");
			VMwareStorage vmStorage = inventory.getStorage(name);
			if (vmStorage == null) {
				logger.warn("The configured storage " + name + " is not available in the inventory.");
			} else {
				if(enabled.equalsIgnoreCase("true")) {
					vmStorage.setEnabled(true);
				}
				else {
					vmStorage.setEnabled(false);
				}
				
				if (limit != null) {
					vmStorage.setLimit(VMwareValue.parse(limit.getTextContent()));
				}
				
				
				storageList.add(vmStorage);
			}
		}
	//	balancer = parseBalancer(xmlConfig, HostBalancer.class, EquipartitionHostBalancer.class, inventory);
	}

	private File createFile(String xmlData) throws IOException, UnsupportedEncodingException {
		Path p = Files.createTempFile("vmware", "props");
		File tmp = p.toFile();

		byte[] b = xmlData.getBytes("UTF-8");

		Files.write(p, b, StandardOpenOption.CREATE);

		tmp.deleteOnExit();
		return tmp;
	}

	@SuppressWarnings("unchecked")
	private <E extends VMwareBalancer<?>> E parseBalancer(Node host, Class<StorageBalancer> class1,
			Class<SequentialStorageBalancer> class2, VMwareDatacenterInventory inventory) throws Exception {

		String balancerClass = host.getAttributes().getNamedItem("class").getTextContent();
		Class<?> loadedClass = null;
		if (balancerClass != null) {
			loadedClass = this.getClass().getClassLoader().loadClass(balancerClass);
			if (!class1.isAssignableFrom(loadedClass)) {
				loadedClass = null;
				logger.warn("The configured balancer '" + balancerClass + "' is not of type "
						+ class1.getSimpleName());
			}
		}
		if (loadedClass == null) {
			balancerClass = class2.getName();
			host.getAttributes().getNamedItem("class").setTextContent(balancerClass);
//			host.addProperty("balancer[@class]", balancerClass);
			loadedClass = class2;
		}
		Object balancerConfig = host.getAttributes().getNamedItem("class").getTextContent();
		E balancer = (E) loadedClass.newInstance();
		balancer.setConfiguration((HierarchicalConfiguration) balancerConfig);
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

		throw new APPlatformException(Messages.getAll("error_unknown_host", new Object[] { hostname }));
	}
}
