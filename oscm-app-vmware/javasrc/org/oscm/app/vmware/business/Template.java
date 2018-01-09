/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.text.DecimalFormat;
import java.util.List;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.balancer.LoadBalancerConfiguration;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.vmware.ManagedObjectAccessor;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.CustomizationAdapterMapping;
import com.vmware.vim25.CustomizationDhcpIpGenerator;
import com.vmware.vim25.CustomizationFixedIp;
import com.vmware.vim25.CustomizationGlobalIPSettings;
import com.vmware.vim25.CustomizationGuiRunOnce;
import com.vmware.vim25.CustomizationGuiUnattended;
import com.vmware.vim25.CustomizationIPSettings;
import com.vmware.vim25.CustomizationIdentification;
import com.vmware.vim25.CustomizationLinuxOptions;
import com.vmware.vim25.CustomizationLinuxPrep;
import com.vmware.vim25.CustomizationPassword;
import com.vmware.vim25.CustomizationSpec;
import com.vmware.vim25.CustomizationSysprep;
import com.vmware.vim25.CustomizationUserData;
import com.vmware.vim25.CustomizationVirtualMachineName;
import com.vmware.vim25.CustomizationWinOptions;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;

public class Template {

    private static final Logger logger = LoggerFactory
            .getLogger(Template.class);
    private static final int DEFAULT_TIMEZONE = 110;
    protected VMwareClient vmw;

    /**
     * Creates a new VMware instance based on a given template.
     * 
     * @param vmw
     *            connected VMware client entity
     * @param paramHandler
     *            entity which holds all properties of the instance.
     * @return name of the created instance
     */
    public TaskInfo cloneVM(VMPropertyHandler paramHandler) throws Exception {
        logger.info("cloneVMFromTemplate() template: "
                + paramHandler.getTemplateName());

        String datacenter = paramHandler.getTargetDatacenter();
        String cluster = paramHandler.getTargetCluster();
        String template = paramHandler.getTemplateName();
        logger.debug("Datacenter: " + datacenter + " Cluster: " + cluster
                + " Template: " + template);

        ManagedObjectReference vmDataCenter = vmw.getServiceUtil()
                .getDecendentMoRef(null, "Datacenter", datacenter);
        if (vmDataCenter == null) {
            logger.error("Datacenter not found. dataCenter: " + datacenter);
            throw new APPlatformException(Messages.get(
                    paramHandler.getLocale(), "error_invalid_datacenter",
                    new Object[] { datacenter }));
        }

        ManagedObjectReference vmTpl = vmw.getServiceUtil().getDecendentMoRef(
                vmDataCenter, "VirtualMachine", template);
        if (vmTpl == null) {
            logger.error("Template not found in datacenter. datacenter: "
                    + datacenter + " template: " + template);
            throw new APPlatformException(Messages.get(
                    paramHandler.getLocale(), "error_invalid_template",
                    new Object[] { template }));
        }

        Long templateDiskSpace = (Long) vmw.getServiceUtil()
                .getDynamicProperty(vmTpl, "summary.storage.unshared");
        if (templateDiskSpace == null) {
            logger.error("Missing disk size in template. template: " + template);
            throw new APPlatformException(Messages.get(
                    paramHandler.getLocale(), "error_missing_template_size"));
        }

        VirtualMachineConfigInfo configSpec = (VirtualMachineConfigInfo) vmw
                .getServiceUtil().getDynamicProperty(vmTpl, "config");

        double tplDiskSpace = VMwareValue.fromBytes(
                templateDiskSpace.longValue()).getValue(Unit.MB);
        if (paramHandler.getConfigDiskSpaceMB() != .0) {
            double requestedDiskSpace = paramHandler.getConfigDiskSpaceMB();
            List<VirtualDevice> devices = configSpec.getHardware().getDevice();
            long capacityInKB = DiskManager.getSystemDiskCapacity(devices,
                    configSpec.getName());
            double requestedDiskSpaceKB = requestedDiskSpace * 1024.0;
            logger.debug("Requested disk space: " + requestedDiskSpaceKB
                    + "Template disk space: " + capacityInKB + " template: "
                    + template);
            if (requestedDiskSpaceKB < capacityInKB) {
                String minValExp = new DecimalFormat("#0.#")
                        .format(VMwareValue
                                .fromMegaBytes(capacityInKB / 1024.0).getValue(
                                        Unit.GB));
                logger.error("Requested disk space is smaller than template disk space. template: "
                        + template);
                throw new APPlatformException(Messages.get(
                        paramHandler.getLocale(), "error_invalid_diskspace",
                        new Object[] { minValExp }));
            }

            paramHandler.setTemplateDiskSpaceMB(requestedDiskSpace);
        } else {
            logger.debug("Use template disk space. template: " + template);
            paramHandler.setTemplateDiskSpaceMB(tplDiskSpace);
        }

        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        VirtualMachineRelocateSpec relocSpec = setHostAndStorage(vmw,
                paramHandler, vmDataCenter, datacenter, cluster);
        cloneSpec.setLocation(relocSpec);
        cloneSpec.setPowerOn(false);
        cloneSpec.setTemplate(false);

        CustomizationSpec custSpec = getCustomizationSpec(configSpec,
                paramHandler);
        cloneSpec.setCustomization(custSpec);

        VirtualMachineConfigSpec vmConfSpec = new VirtualMachineConfigSpec();

        String respPerson = paramHandler
                .getServiceSetting(VMPropertyHandler.TS_RESPONSIBLE_PERSON);

        String reqUser = paramHandler
                .getServiceSetting(VMPropertyHandler.REQUESTING_USER);

        String systemvariante = "";

        String comment = Messages.get(paramHandler.getLocale(), "vm_comment",
                new Object[] {
                        paramHandler.getSettings().getOrganizationName(),
                        paramHandler.getSettings().getSubscriptionId(),
                        reqUser, respPerson, systemvariante });
        vmConfSpec.setAnnotation(comment);
        cloneSpec.setConfig(vmConfSpec);

        String targetFolder = paramHandler.getTargetFolder();
        ManagedObjectReference moRefTargetFolder = null;

        if (targetFolder != null) {
            moRefTargetFolder = vmw.getServiceUtil().getDecendentMoRef(null,
                    "Folder", targetFolder);
        } else {
            moRefTargetFolder = (ManagedObjectReference) vmw.getServiceUtil()
                    .getDynamicProperty(vmTpl, "parent");
        }

        if (moRefTargetFolder == null) {
            logger.error("Target folder " + targetFolder + " not found.");
            throw new APPlatformException(Messages.get(
                    paramHandler.getLocale(), "error_invalid_target_folder",
                    new Object[] { targetFolder }));
        }

        String newInstanceName = paramHandler.getInstanceName();
        logger.debug("Call vSphere API: cloneVMTask() instancename: "
                + newInstanceName + " targetfolder: "
                + paramHandler.getTargetFolder());
        VimPortType service = vmw.getConnection().getService();
        ManagedObjectReference cloneTask = service.cloneVMTask(vmTpl,
                moRefTargetFolder, newInstanceName, cloneSpec);

        return (TaskInfo) vmw.getServiceUtil().getDynamicProperty(cloneTask,
                "info");
    }

    /**
     * Generates customization specification for OS specific deployment.
     * 
     * @param custProps
     *            customization specific parameters
     * @return filled VMware customization block
     */
    private CustomizationSpec getCustomizationSpec(
            VirtualMachineConfigInfo configSpec, VMPropertyHandler paramHandler)
            throws APPlatformException {

        String guestid = configSpec.getGuestId();
        if (guestid == null) {
            throw new APPlatformException(
                    "Operatingsystem not defined in Guest-Id.");
        }

        boolean isLinux = guestid.startsWith("cent")
                || guestid.startsWith("debian")
                || guestid.startsWith("freebsd")
                || guestid.startsWith("oracle")
                || guestid.startsWith("other24xLinux")
                || guestid.startsWith("other26xLinux")
                || guestid.startsWith("otherLinux")
                || guestid.startsWith("coreos") || guestid.startsWith("redhat")
                || guestid.startsWith("rhel") || guestid.startsWith("sles")
                || guestid.startsWith("suse") || guestid.startsWith("ubuntu");
        boolean isWindows = guestid.startsWith("win");

        logger.debug("isLinux: " + isLinux + " isWindows: " + isWindows
                + " guestid: " + configSpec.getGuestId() + " OS: "
                + configSpec.getGuestFullName());

        if (!isLinux && !isWindows) {
            logger.error("GuestId cannot be interpreted. guestid: "
                    + configSpec.getGuestId() + " OS: "
                    + configSpec.getGuestFullName());
            throw new APPlatformException("Unsupported operating system "
                    + configSpec.getGuestFullName());
        }

        CustomizationSpec cspec = new CustomizationSpec();
        CustomizationGlobalIPSettings gIP = new CustomizationGlobalIPSettings();
        cspec.setGlobalIPSettings(gIP);

        if (isLinux) {
            String[] dnsserver = paramHandler.getDNSServer(1).split(",");
            for (String server : dnsserver) {
                logger.debug("Linux -> CustomizationGlobalIPSettings -> DNS server: "
                        + server);
                gIP.getDnsServerList().add(server.trim());
            }

            String[] dnssuffix = paramHandler.getDNSSuffix(1).split(",");
            for (String suffix : dnssuffix) {
                logger.debug("Linux -> CustomizationGlobalIPSettings -> DNS suffix: "
                        + suffix);
                gIP.getDnsSuffixList().add(suffix.trim());
            }

            CustomizationLinuxPrep sprep = new CustomizationLinuxPrep();

            String domain = paramHandler
                    .getServiceSetting(VMPropertyHandler.TS_DOMAIN_NAME);
            logger.debug("Linux domain name: " + domain);
            if (domain != null) {
                sprep.setDomain(domain);
            }

            sprep.setHostName(new CustomizationVirtualMachineName());
            sprep.setTimeZone("363");

            sprep.setHwClockUTC(Boolean.TRUE);
            cspec.setIdentity(sprep);
            cspec.setOptions(new CustomizationLinuxOptions());
        }

        if (isWindows) {
            CustomizationSysprep sprep = new CustomizationSysprep();
            CustomizationGuiUnattended guiUnattended = new CustomizationGuiUnattended();
            guiUnattended.setAutoLogon(false);
            guiUnattended.setAutoLogonCount(0);
            guiUnattended.setTimeZone(DEFAULT_TIMEZONE);

            if (paramHandler
                    .isServiceSettingTrue(VMPropertyHandler.TS_WINDOWS_DOMAIN_JOIN)) {
                CustomizationIdentification identification = new CustomizationIdentification();
                String domainName = paramHandler
                        .getServiceSettingValidated(VMPropertyHandler.TS_DOMAIN_NAME);
                String domainAdmin = paramHandler
                        .getServiceSettingValidated(VMPropertyHandler.TS_WINDOWS_DOMAIN_ADMIN);
                String domainAdminPwd = paramHandler
                        .getServiceSettingValidated(VMPropertyHandler.TS_WINDOWS_DOMAIN_ADMIN_PWD);

                logger.debug("Join Domain " + domainName + " admin: "
                        + domainAdmin + " pwd: " + domainAdminPwd);

                identification.setJoinDomain(domainName);
                identification.setDomainAdmin(domainAdmin);
                CustomizationPassword password = new CustomizationPassword();
                password.setValue(domainAdminPwd);
                password.setPlainText(true);
                identification.setDomainAdminPassword(password);
                sprep.setIdentification(identification);
            } else {
                CustomizationIdentification identification = new CustomizationIdentification();
                String workgroup = paramHandler
                        .getServiceSettingValidated(VMPropertyHandler.TS_WINDOWS_WORKGROUP);
                identification.setJoinWorkgroup(workgroup);
                sprep.setIdentification(identification);
                logger.debug("Create workgroup " + workgroup);
            }

            String adminPwd = paramHandler
                    .getServiceSetting(VMPropertyHandler.TS_WINDOWS_LOCAL_ADMIN_PWD);

            if ((adminPwd == null || adminPwd.length() == 0)
                    && !paramHandler
                            .isServiceSettingTrue(VMPropertyHandler.TS_WINDOWS_DOMAIN_JOIN)) {
                logger.error("The VM is not joining a Windows domain. A local administrator password is required but not set.");
                throw new APPlatformException(
                        "The VM is not joining a Windows domain. A local administrator password is required but not set.");
            } else if (adminPwd != null && adminPwd.length() > 0) {
                CustomizationPassword password = new CustomizationPassword();
                password.setValue(adminPwd);
                password.setPlainText(true);
                guiUnattended.setPassword(password);
                logger.debug("Set Windows local administrator pwd: " + adminPwd);
            }

            sprep.setGuiUnattended(guiUnattended);

            String command = paramHandler
                    .getServiceSetting(VMPropertyHandler.TS_SYSPREP_RUNONCE_COMMAND);

            if (command != null) {
                logger.debug("sysprep runonce command: " + command);
                CustomizationGuiRunOnce guiRunOnce = new CustomizationGuiRunOnce();
                guiRunOnce.getCommandList().add(command);
                sprep.setGuiRunOnce(guiRunOnce);
            }

            CustomizationUserData userData = new CustomizationUserData();
            userData.setComputerName(new CustomizationVirtualMachineName());

            String fullname = paramHandler
                    .getResponsibleUserAsString(paramHandler.getLocale());
            if (fullname == null) {
                fullname = "No responsible user defined";
            }
            logger.debug("CustomizationUserData.fullName: " + fullname);

            userData.setFullName(fullname);
            userData.setOrgName("Created by OSCM");

            String licenseKey = paramHandler
                    .getServiceSetting(VMPropertyHandler.TS_WINDOWS_LICENSE_KEY);

            if (licenseKey != null && licenseKey.trim().length() > 0) {
                userData.setProductId(licenseKey);
            } else {
                userData.setProductId("");
            }

            sprep.setUserData(userData);

            cspec.setIdentity(sprep);

            CustomizationWinOptions options = new CustomizationWinOptions();
            options.setChangeSID(true);
            options.setDeleteAccounts(false);
            cspec.setOptions(options);
        }

        int numberOfNICs = Integer.parseInt(paramHandler
                .getServiceSetting(VMPropertyHandler.TS_NUMBER_OF_NICS));
        logger.debug("Number of NICs in template: " + numberOfNICs);
        for (int i = 1; i <= numberOfNICs; i++) {
            CustomizationAdapterMapping networkAdapter = new CustomizationAdapterMapping();
            CustomizationIPSettings ipSettings = new CustomizationIPSettings();
            if (paramHandler.isAdapterConfiguredByDhcp(i)) {
                CustomizationDhcpIpGenerator publicDhcpIp = new CustomizationDhcpIpGenerator();
                ipSettings.setIp(publicDhcpIp);
            } else {
                logger.debug("NIC" + i + " IP:" + paramHandler.getIpAddress(i));
                CustomizationFixedIp newip = new CustomizationFixedIp();
                newip.setIpAddress(paramHandler.getIpAddress(i));
                ipSettings.setIp(newip);

                String[] gateways = paramHandler.getGateway(i).split(",");
                for (String gw : gateways) {
                    logger.debug("NIC" + i + " Gateway:" + gw);
                    ipSettings.getGateway().add(gw.trim());
                }

                if (isWindows) {
                    String[] dnsserver = paramHandler.getDNSServer(i)
                            .split(",");
                    for (String server : dnsserver) {
                        logger.debug("NIC" + i + " DNS server:" + server);
                        ipSettings.getDnsServerList().add(server.trim());
                    }
                }

                logger.debug("NIC" + i + " Subnetmask:"
                        + paramHandler.getSubnetMask(i));
                ipSettings.setSubnetMask(paramHandler.getSubnetMask(i).trim());
            }
            networkAdapter.setAdapter(ipSettings);
            cspec.getNicSettingMap().add(networkAdapter);
        }

        return cspec;
    }

    /**
     * If host and storage are not defined as technical service parameter then
     * the load balancing mechanism is used to determine host and storage
     */
    private VirtualMachineRelocateSpec setHostAndStorage(VMwareClient vmw,
            VMPropertyHandler paramHandler,
            ManagedObjectReference vmDataCenter, String datacenter,
            String cluster) throws Exception {
        logger.debug("datacenter: " + datacenter + " cluster: " + cluster);
        String xmlData = paramHandler.getHostLoadBalancerConfig();
        VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();

        String storageName = paramHandler
                .getServiceSetting(VMPropertyHandler.TS_TARGET_STORAGE);
        String hostName = paramHandler
                .getServiceSetting(VMPropertyHandler.TS_TARGET_HOST);
        if (hostName == null || hostName.trim().length() == 0) {
            logger.debug("target host not set. get host and storage from loadbalancer");
            VMwareDatacenterInventory inventory = readDatacenterInventory(vmw,
                    datacenter, cluster);
            LoadBalancerConfiguration balancerConfig = new LoadBalancerConfiguration(
                    xmlData, inventory);
            VMwareHost host = balancerConfig.getBalancer().next(paramHandler);
            hostName = host.getName();
            paramHandler.setSetting(VMPropertyHandler.TS_TARGET_HOST, hostName);
            VMwareStorage storage = host.getNextStorage(paramHandler);
            storageName = storage.getName();
        } else {
            if (storageName == null || storageName.trim().length() == 0) {
                logger.debug("target storage not set. get host and storage from loadbalancer");
                VMwareDatacenterInventory inventory = readDatacenterInventory(
                        vmw, datacenter, cluster);
                VMwareHost host = inventory.getHost(hostName);
                VMwareStorage storage = host.getNextStorage(paramHandler);
                storageName = storage.getName();
            }
        }

        logger.info("Target Host: " + hostName + " Target Storage: "
                + storageName);
        ManagedObjectReference vmHost = vmw.getServiceUtil().getDecendentMoRef(
                vmDataCenter, "HostSystem", hostName);
        if (vmHost == null) {
            logger.error("Target host " + hostName + " not found");
            throw new APPlatformException(Messages.getAll("error_invalid_host",
                    new Object[] { hostName }));
        }

        ManagedObjectReference vmHostCluster = (ManagedObjectReference) vmw
                .getServiceUtil().getDynamicProperty(vmHost, "parent");
        ManagedObjectReference vmPool = vmw.getServiceUtil().getDecendentMoRef(
                vmHostCluster, "ResourcePool", "Resources");
        if (vmPool == null) {
            logger.error("Resourcepool not found");
            throw new APPlatformException(Messages.getAll("error_invalid_pool",
                    new Object[] { hostName }));
        }

        ManagedObjectReference vmDatastore = null;
        Object vmHostDatastores = vmw.getServiceUtil().getDynamicProperty(
                vmHost, "datastore");
        if (vmHostDatastores instanceof List<?>) {
            for (Object vmHostDatastore : (List<?>) vmHostDatastores) {
                if (vmHostDatastore instanceof ManagedObjectReference) {
                    String dsname = (String) vmw.getServiceUtil()
                            .getDynamicProperty(
                                    (ManagedObjectReference) vmHostDatastore,
                                    "summary.name");
                    if (dsname.equalsIgnoreCase(storageName)) {
                        vmDatastore = (ManagedObjectReference) vmHostDatastore;
                        break;
                    }
                } else {
                    logger.warn("Expected datastore information as 'ManagedObjectReference' but recieved object of type "
                            + (vmHostDatastore == null ? "[null]"
                                    : vmHostDatastore.getClass()
                                            .getSimpleName()));
                }
            }
        }
        if (vmDatastore == null) {
            logger.error("Target datastore " + storageName + " not found");
            throw new APPlatformException(Messages.getAll(
                    "error_invalid_datastore", new Object[] { storageName,
                            hostName }));
        }

        relocSpec.setDatastore(vmDatastore);
        relocSpec.setPool(vmPool);
        relocSpec.setHost(vmHost);
        return relocSpec;
    }

    @SuppressWarnings("unchecked")
    private VMwareDatacenterInventory readDatacenterInventory(
            VMwareClient appUtil, String datacenter, String cluster)
            throws Exception {
        logger.debug("datacenter: " + datacenter + " cluster: " + cluster);

        ManagedObjectAccessor serviceUtil = appUtil.getServiceUtil();

        ManagedObjectReference dcMoRef = serviceUtil.getDecendentMoRef(null,
                "Datacenter", datacenter);

        ManagedObjectReference clusterMoRef = serviceUtil.getDecendentMoRef(
                dcMoRef, "ClusterComputeResource", cluster);

        List<ManagedObjectReference> hostMoRefs = (List<ManagedObjectReference>) serviceUtil
                .getDynamicProperty(clusterMoRef, "host");

        VMwareDatacenterInventory inventory = new VMwareDatacenterInventory();
        for (ManagedObjectReference hostRef : hostMoRefs) {
            List<DynamicProperty> dps = serviceUtil.getDynamicProperty(hostRef,
                    new String[] { "name", "summary.hardware.memorySize",
                            "summary.hardware.numCpuCores" });
            String host = "";
            for (DynamicProperty dp : dps) {
                String key = dp.getName();
                if ("name".equals(key) && dp.getVal() != null) {
                    host = dp.getVal().toString();
                }
            }
            logger.debug("addHostSystem host: " + host);
            inventory.addHostSystem(dps);

            List<ManagedObjectReference> storageRefs = (List<ManagedObjectReference>) serviceUtil
                    .getDynamicProperty(hostRef, "datastore");
            for (ManagedObjectReference storageRef : storageRefs) {
                dps = serviceUtil.getDynamicProperty(storageRef,
                        new String[] { "summary.name", "summary.capacity",
                                "summary.freeSpace" });

                String storageName = "";
                for (DynamicProperty dp : dps) {
                    String key = dp.getName();
                    if ("summary.name".equals(key) && dp.getVal() != null) {
                        storageName = dp.getVal().toString();
                    }
                }

                List<DatastoreHostMount> hostMounts = (List<DatastoreHostMount>) serviceUtil
                        .getDynamicProperty(storageRef, "host");

                for (DatastoreHostMount hm : hostMounts) {
                    ManagedObjectReference hostMor = hm.getKey();
                    String hostThatLinksToThisStoarge = (String) serviceUtil
                            .getDynamicProperty(hostMor, "name");
                    if (host.equals(hostThatLinksToThisStoarge)
                            && hm.getMountInfo().isAccessible().booleanValue()
                            && hm.getMountInfo().isMounted().booleanValue()
                            && !hm.getMountInfo().getAccessMode()
                                    .equals("readOnly")) {

                        logger.debug("addStorage host: " + host + " storage: "
                                + storageName);
                        inventory.addStorage(host, dps);
                    }
                }
            }

            List<ManagedObjectReference> vmRefs = (List<ManagedObjectReference>) serviceUtil
                    .getDynamicProperty(hostRef, "vm");
            for (ManagedObjectReference vmRef : vmRefs) {
                dps = serviceUtil.getDynamicProperty(vmRef, new String[] {
                        "name", "summary.config.memorySizeMB",
                        "summary.config.numCpu", "runtime.host" });
                inventory.addVirtualMachine(dps, serviceUtil);
            }

        }
        inventory.initialize();
        return inventory;
    }

}
