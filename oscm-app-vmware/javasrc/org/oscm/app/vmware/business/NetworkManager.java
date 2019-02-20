/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.HostSystemInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NetworkSummary;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;

public class NetworkManager {

    private static final Logger logger = LoggerFactory
            .getLogger(NetworkManager.class);

    public static int getNumberOfNICs(VMwareClient vmw,
            ManagedObjectReference vmwInstance) throws Exception {
        logger.debug("");

        VirtualMachineConfigInfo configInfo = (VirtualMachineConfigInfo) vmw
                .getServiceUtil().getDynamicProperty(vmwInstance, "config");
        List<VirtualEthernetCard> vmNics = getNetworkAdapter(configInfo);
        return vmNics.size();
    }

    public static String getNetworkName(VMwareClient vmw,
            ManagedObjectReference vmwInstance, int numNic) throws Exception {
        logger.debug("");

        List<ManagedObjectReference> networkList = (List<ManagedObjectReference>) vmw
                .getServiceUtil().getDynamicProperty(vmwInstance, "network");

        String name = null;
        if (networkList != null && networkList.size() >= numNic) {
            name = (String) vmw.getServiceUtil()
                    .getDynamicProperty(networkList.get(numNic - 1), "name");
            NetworkSummary summary = (NetworkSummary) vmw.getServiceUtil()
                    .getDynamicProperty(networkList.get(numNic - 1), "summary");

            logger.debug(
                    "name: " + name + " ipPoolId: " + summary.getIpPoolName()
                            + " ipPoolName: " + summary.getName());

        }

        if (name == null) {
            throw new Exception(
                    "Failed to retrieve network name from template.");
        }
        return name;
    }

    /**
     * Replaces the NICs in the given VM.
     *
     * @param vmw
     *            connected VMware client entity
     * @param paramHandler
     *            entity which holds all properties of the instance
     * @param vmwInstance
     *            the virtual machine that gets reconfigured
     */
    public static void configureNetworkAdapter(VMwareClient vmw,
            VirtualMachineConfigSpec vmConfigSpec,
            VMPropertyHandler paramHandler, ManagedObjectReference vmwInstance)
            throws Exception {
        logger.debug("");

        VirtualMachineConfigInfo configInfo = (VirtualMachineConfigInfo) vmw
                .getServiceUtil().getDynamicProperty(vmwInstance, "config");
        List<VirtualEthernetCard> vmNics = getNetworkAdapter(configInfo);

        int numberOfNICs = Integer.parseInt(paramHandler
                .getServiceSetting(VMPropertyHandler.TS_NUMBER_OF_NICS));

        if (numberOfNICs != vmNics.size()) {
            throw new Exception(
                    "the number of NICs in virtual machine does not match the service parameter. VM: "
                            + configInfo.getName() + " NICs: " + vmNics.size()
                            + " " + VMPropertyHandler.TS_NUMBER_OF_NICS + ": "
                            + numberOfNICs);
        }

        for (int i = 1; i <= numberOfNICs; i++) {
            String newNetworkName = paramHandler.getNetworkAdapter(i);
            String newGroup = paramHandler.getPortGroup(i);
            String switchUIID = paramHandler.getSwitchUUID(i);

            logger.info(String.format("NIC%s_SWITCH_UUID: %s",
                    String.valueOf(i), switchUIID));
            logger.info(String.format("NIC%s_PORTGROUP: %s",
                    String.valueOf(i), newGroup));

            if (switchUIID != null && switchUIID.length() > 0) {
                if (newGroup == null || newGroup.length() == 0)
                    throw new Exception(String.format(
                            "Parameter NIC%s_PORTGROUP has to be specified for using the switch %s. Expecting generated UUID of the portgroup.",
                            String.valueOf(i), switchUIID));

            }

            VirtualEthernetCard vmNic = vmNics.get(i - 1);

            String vmNetworkName = getNetworkName(vmw, vmwInstance, i);
            if (newNetworkName != null && newNetworkName.length() > 0
                    && !newNetworkName.equals(vmNetworkName)) {

                ManagedObjectReference newNetworkRef = getNetworkFromHost(vmw,
                        vmwInstance, newNetworkName, vmNic, switchUIID,
                        newGroup);

                replaceNetworkAdapter(vmConfigSpec, vmNic, newNetworkRef,
                        newNetworkName);

            } else {
                connectNIC(vmConfigSpec, vmNic, vmNetworkName);
            }
        }
    }

    private static ManagedObjectReference getNetworkFromHost(VMwareClient vmw,
            ManagedObjectReference vmwInstance, String networkName,
            VirtualEthernetCard card, String switchUUID, String portGroup)
            throws Exception {
        logger.debug("networkName: " + networkName);
        logger.debug("portGroup: " + portGroup);

        VirtualMachineRuntimeInfo vmRuntimeInfo = (VirtualMachineRuntimeInfo) vmw
                .getServiceUtil().getDynamicProperty(vmwInstance, "runtime");
        ManagedObjectReference hostRef = vmRuntimeInfo.getHost();
        HostSystemInfo i = new HostSystemInfo();

        if (switchUUID != null && switchUUID.trim().length() > 0) {

            ManagedObjectReference man = vmw.getConnection().getServiceContent()
                    .getDvSwitchManager();

            ManagedObjectReference sw = vmw.getConnection().getService()
                    .queryDvsByUuid(man, switchUUID);

            List<ManagedObjectReference> portGroups = (List<ManagedObjectReference>) vmw
                    .getServiceUtil().getDynamicProperty(sw, "portgroup");

            ManagedObjectReference portGrp = null;
            StringBuffer groups = new StringBuffer();
            for (ManagedObjectReference portgrpkRef : portGroups) {
                String groupkey = (String) vmw.getServiceUtil()
                        .getDynamicProperty(portgrpkRef, "key");

                groups.append(groupkey + " ");
                if (groupkey.equalsIgnoreCase(portGroup)) {
                    portGrp = portgrpkRef;
                    break;
                }
            }

            if (portGrp == null) {
                String hostName = (String) vmw.getServiceUtil()
                        .getDynamicProperty(hostRef, "name");

                StringBuffer b = new StringBuffer();
                b.append("PortGroup " + portGroup + " not found on host "
                        + hostName);
                b.append("\nExpecting generated UUID of the portgroup.");
                b.append("\nAvailable groups are: " + groups.toString());
                logger.error(b.toString());

            } else {
                prepareDvNicDevice(card, portGrp, switchUUID);

                return portGrp;
            }
        }

        List<ManagedObjectReference> networkRefList = (List<ManagedObjectReference>) vmw
                .getServiceUtil().getDynamicProperty(hostRef, "network");

        ManagedObjectReference netCard = null;
        StringBuffer networks = new StringBuffer();
        for (ManagedObjectReference networkRef : networkRefList) {
            String netCardName = (String) vmw.getServiceUtil()
                    .getDynamicProperty(networkRef, "name");

            networks.append(netCardName + " ");
            if (netCardName.equalsIgnoreCase(networkName)) {
                netCard = networkRef;
                break;
            }

        }

        if (netCard == null) {
            String hostName = (String) vmw.getServiceUtil()
                    .getDynamicProperty(hostRef, "name");
            logger.error("Network " + networkName + " not found on host "
                    + hostName);
            logger.error("available networks are: " + networks.toString());
            //
            throw new Exception("Network card " + networkName
                    + " not found on host " + hostName);
        }

        return netCard;
    }

    public static VirtualDevice prepareDvNicDevice(VirtualEthernetCard vmNic,
            ManagedObjectReference group, String switchUuid) throws Exception {
        logger.debug("switch: " + switchUuid);
        VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();

        final VirtualEthernetCardDistributedVirtualPortBackingInfo dvPortBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
        final DistributedVirtualSwitchPortConnection dvPortConnection = new DistributedVirtualSwitchPortConnection();
        
        dvPortConnection.setSwitchUuid(switchUuid);
        dvPortConnection.setPortgroupKey(group.getValue());
        dvPortBacking.setPort(dvPortConnection);
        vmNic.setBacking(dvPortBacking);
        return vmNic;
    }

    public static List<VirtualEthernetCard> getNetworkAdapter(
            VirtualMachineConfigInfo configInfo) {
        List<VirtualEthernetCard> nics = new ArrayList<VirtualEthernetCard>();
        List<VirtualDevice> devices = configInfo.getHardware().getDevice();
        for (VirtualDevice vd : devices) {
            if (vd instanceof VirtualEthernetCard) {
                nics.add((VirtualEthernetCard) vd);
            }
        }

        return nics;
    }

    private static void replaceNetworkAdapter(
            VirtualMachineConfigSpec vmConfigSpec, VirtualDevice oldNIC,
            ManagedObjectReference newNetworkRef, String newNetworkName)
            throws Exception {

        logger.debug("new network: " + newNetworkName);
        VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
        nicBacking.setDeviceName(newNetworkName);
        nicBacking.setNetwork(newNetworkRef);
        nicBacking.setUseAutoDetect(true);
        oldNIC.setBacking(nicBacking);

        VirtualDeviceConnectInfo info = new VirtualDeviceConnectInfo();
        info.setConnected(true);
        info.setStartConnected(true);
        info.setAllowGuestControl(true);
        oldNIC.setConnectable(info);
        // oldNIC.getConnectable().setConnected(true);
        // oldNIC.getConnectable().setStartConnected(true);
        VirtualDeviceConfigSpec vmDeviceSpec = new VirtualDeviceConfigSpec();
        vmDeviceSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
        vmDeviceSpec.setDevice(oldNIC);
        vmConfigSpec.getDeviceChange().add(vmDeviceSpec);
    }
    
    private static void connectNIC(VirtualMachineConfigSpec vmConfigSpec,
            VirtualDevice oldNIC, String vmNetworkName) throws Exception {

        logger.debug("networkName: " + vmNetworkName);

        VirtualDeviceConnectInfo info = new VirtualDeviceConnectInfo();
        info.setConnected(true);
        info.setStartConnected(true);
        info.setAllowGuestControl(true);
        oldNIC.setConnectable(info);
        VirtualDeviceConfigSpec vmDeviceSpec = new VirtualDeviceConfigSpec();
        vmDeviceSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
        vmDeviceSpec.setDevice(oldNIC);
        vmConfigSpec.getDeviceChange().add(vmDeviceSpec);
    }

}
