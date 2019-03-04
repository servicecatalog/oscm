/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.Script.OS;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineSummary;
import com.vmware.vim25.VirtualMachineTicket;

public class VM extends Template {

    private static final Logger LOG = LoggerFactory.getLogger(VM.class);

    private static final String GUEST_STATE_RUNNING = "running";
    private static final String TOOLS_RUNNING_STATE = "guestToolsRunning";

    private ManagedObjectReference vmInstance;
    private VirtualMachineConfigInfo configSpec;
    private ManagedObjectReference folder;
    private GuestInfo guestInfo;
    private String instanceName;
    private VirtualMachineSummary virtualMachineSummary;
    private VirtualMachineSnapshotInfo virtualMachineSnapshotInfo;
    VirtualMachineTicket vmTicket;

    public VM(VMwareClient vmw, String instanceName) throws Exception {
        this.vmw = vmw;
        this.instanceName = instanceName;

        vmInstance = vmw.getServiceUtil().getDecendentMoRef(null,
                "VirtualMachine", instanceName);
        configSpec = (VirtualMachineConfigInfo) vmw.getServiceUtil()
                .getDynamicProperty(vmInstance, "config");
        folder = (ManagedObjectReference) vmw.getServiceUtil()
                .getDynamicProperty(vmInstance, "parent");
        guestInfo = (GuestInfo) vmw.getServiceUtil()
                .getDynamicProperty(vmInstance, "guest");
        virtualMachineSummary = (VirtualMachineSummary) vmw.getServiceUtil()
                .getDynamicProperty(vmInstance, "summary");
        virtualMachineSnapshotInfo = (VirtualMachineSnapshotInfo) vmw
                .getServiceUtil().getDynamicProperty(vmInstance, "snapshot");
        
        vmTicket = vmw.getConnection().getService().acquireTicket(vmInstance, "guestControl");
        

        if (vmInstance == null || configSpec == null || folder == null
                || guestInfo == null) {
            LOG.warn("failed to retrieve VM");
            throw new Exception(
                    "Failed to retrieve information of VM " + instanceName);
        }
    }
    
   public String createVMURL(VMPropertyHandler ph){
       
       StringBuilder URL = new StringBuilder();
       URL.append("https://");
       URL.append(ph.getTargetVCenterServer());
       URL.append(":9443");
       URL.append("/vsphere-client/webconsole.html?vmId=");
       URL.append(vmInstance.getValue().toString());
       URL.append("&vmName=");
       URL.append(configSpec.getName());
       URL.append("&serverGuid=");
       URL.append(configSpec.getInstanceUuid());
       URL.append("&locale=en");
       URL.append(ph.getTargetVCenterServer());
       URL.append(":443");
       URL.append("&sessionTicket=");
       URL.append(vmTicket.getTicket().toString());
       URL.append("&thumbprint=");
       URL.append(vmTicket.getSslThumbprint());
                
       
       return URL.toString();
   }

    public List<String> getSnashotsAsList() {
        List<String> snapshots = new ArrayList<String>();
        if (virtualMachineSnapshotInfo != null) {
            virtualMachineSnapshotInfo.getCurrentSnapshot();
            List<VirtualMachineSnapshotTree> snap = virtualMachineSnapshotInfo
                    .getRootSnapshotList();
            snapshots.addAll(getSnapshots(snap, new ArrayList<String>(), ""));
        }
        return snapshots;
    }

    private List<String> getSnapshots(List<VirtualMachineSnapshotTree> vmst,
            ArrayList<String> snaps, String indent) {
        
        vmInstance.getValue().toString();
        for (Iterator<VirtualMachineSnapshotTree> iterator = vmst
                .iterator(); iterator.hasNext();) {
            VirtualMachineSnapshotTree snap = iterator.next();
            snaps.add(indent + "Snapshot: " + snap.getName());
            getSnapshots(snap.getChildSnapshotList(), snaps, indent + " ");
            
            
            
        }
        return snaps;
    }

    public Integer getGuestMemoryUsage() {
        return virtualMachineSummary.getQuickStats().getGuestMemoryUsage();
    }

    public Integer getOverallCpuUsage() {
        return virtualMachineSummary.getQuickStats().getOverallCpuUsage();
    }

    public Integer getUptimeSeconds() {
        return virtualMachineSummary.getQuickStats().getUptimeSeconds();
    }

    public String getStatus() {
        return guestInfo.getGuestState();
    }

    public String getGuestFullName() {
        return configSpec.getGuestFullName();
    }

    public boolean isLinux() {
        String guestid = configSpec.getGuestId();
        boolean isLinux = guestid.startsWith("cent")
                || guestid.startsWith("debian") || guestid.startsWith("freebsd")
                || guestid.startsWith("oracle")
                || guestid.startsWith("other24xLinux")
                || guestid.startsWith("other26xLinux")
                || guestid.startsWith("otherLinux")
                || guestid.startsWith("redhat") || guestid.startsWith("rhel")
                || guestid.startsWith("sles") || guestid.startsWith("suse")
                || guestid.startsWith("ubuntu");

        LOG.debug("instanceName: " + instanceName + " isLinux: " + isLinux
                + " guestid: " + configSpec.getGuestId() + " OS: "
                + configSpec.getGuestFullName());

        return isLinux;
    }

    public void updateServiceParameter(VMPropertyHandler paramHandler)
            throws Exception {
        LOG.debug("instanceName: " + instanceName);
        int key = getDataDiskKey();
        if (key != -1) {
            paramHandler.setDataDiskKey(1, key);
        }

        if (!paramHandler
                .isServiceSettingTrue(VMPropertyHandler.TS_IMPORT_EXISTING_VM)
                && !paramHandler.getInstanceName()
                        .equals(guestInfo.getHostName())) {
            throw new Exception(
                    "Instancename and hostname do not match. Hostname: "
                            + guestInfo.getHostName() + "  Instancename: "
                            + paramHandler.getInstanceName());
        }

        String targetFolder = (String) vmw.getServiceUtil()
                .getDynamicProperty(folder, "name");

        Integer ramMB = (Integer) vmw.getServiceUtil()
                .getDynamicProperty(vmInstance, "summary.config.memorySizeMB");
        paramHandler.setSetting(VMPropertyHandler.TS_AMOUNT_OF_RAM,
                ramMB.toString());
        paramHandler.setSetting(VMPropertyHandler.TS_NUMBER_OF_CPU,
                Integer.toString(getNumCPU()));
        paramHandler.setSetting(VMPropertyHandler.TS_TARGET_FOLDER,
                targetFolder);

        paramHandler.setSetting(VMPropertyHandler.TS_DISK_SIZE,
                getDiskSizeInGB(1));

        paramHandler.setSetting(
                VMPropertyHandler.TS_DATA_DISK_SIZE.replace("#", "1"),
                getDiskSizeInGB(2));
        paramHandler.setSetting(VMPropertyHandler.TS_NUMBER_OF_NICS,
                Integer.toString(getNumberOfNICs()));

        int i = 1;
        List<GuestNicInfo> nicList = guestInfo.getNet();
        for (GuestNicInfo info : nicList) {
            if (info.getIpAddress() != null && info.getIpAddress().size() > 0) {
                paramHandler.setSetting("NIC" + i + "_IP_ADDRESS",
                        info.getIpAddress().get(0));
                if (info.getNetwork() != null) {
                    paramHandler.setSetting("NIC" + i + "_NETWORK_ADAPTER",
                            info.getNetwork());
                }
                i++;
            }
        }
    }

    public OS detectOs() {
        if (configSpec.getGuestId().startsWith("win")) {
            return OS.WINDOWS;
        }
        return OS.LINUX;
    }

    public boolean isRunning() throws Exception {
        VirtualMachineRuntimeInfo vmRuntimeInfo = (VirtualMachineRuntimeInfo) vmw
                .getServiceUtil().getDynamicProperty(vmInstance, "runtime");

        boolean isRunning = false;
        if (vmRuntimeInfo != null) {
            isRunning = !VirtualMachinePowerState.POWERED_OFF
                    .equals(vmRuntimeInfo.getPowerState());
            LOG.debug(Boolean.toString(isRunning));
        } else {
            LOG.warn("Failed to retrieve runtime information from VM "
                    + instanceName);
        }

        return isRunning;
    }

    public boolean isStopped() throws Exception {
        VirtualMachineRuntimeInfo vmRuntimeInfo = (VirtualMachineRuntimeInfo) vmw
                .getServiceUtil().getDynamicProperty(vmInstance, "runtime");

        if (vmRuntimeInfo != null) {
            return VirtualMachinePowerState.POWERED_OFF
                    .equals(vmRuntimeInfo.getPowerState());
        }
        LOG.warn("Failed to retrieve runtime information from VM "
                + instanceName);
        return false;

    }

    public TaskInfo start() throws Exception {
        LOG.debug("instanceName: " + instanceName);
        ManagedObjectReference startTask = vmw.getConnection().getService()
                .powerOnVMTask(vmInstance, null);

        TaskInfo tInfo = (TaskInfo) vmw.getServiceUtil()
                .getDynamicProperty(startTask, "info");
        return tInfo;
    }

    public TaskInfo stop(boolean forceStop) throws Exception {
        LOG.debug("instanceName: " + instanceName + " forceStop: " + forceStop);
        TaskInfo tInfo = null;

        if (forceStop) {
            LOG.debug("Call vSphere API: powerOffVMTask() instanceName: "
                    + instanceName);
            ManagedObjectReference stopTask = vmw.getConnection().getService()
                    .powerOffVMTask(vmInstance);
            tInfo = (TaskInfo) vmw.getServiceUtil().getDynamicProperty(stopTask,
                    "info");
        } else {

            if (isRunning()) {
                LOG.debug("Call vSphere API: shutdownGuest() instanceName: "
                        + instanceName);
                vmw.getConnection().getService().shutdownGuest(vmInstance);
            }
        }

        return tInfo;
    }

    public ManagedObjectReference getFolder() {
        return folder;
    }

    public void runScript(VMPropertyHandler paramHandler) throws Exception {
        LOG.debug("instanceName: " + instanceName);

        String scriptURL = paramHandler
                .getServiceSetting(VMPropertyHandler.TS_SCRIPT_URL);
        if (scriptURL != null) {
            Script script = new Script(paramHandler, detectOs());
            script.execute(vmw, vmInstance);
        }
    }

    public int getNumberOfNICs() throws Exception {
        return NetworkManager.getNumberOfNICs(vmw, vmInstance);
    }

    public String getNetworkName(int numNic) throws Exception {
        return NetworkManager.getNetworkName(vmw, vmInstance, numNic);
    }

    /**
     * Reconfigures VMware instance. Memory, CPU, disk space and network
     * adapter. The VM has been created and must be stopped to reconfigure the
     * hardware.
     */
    public TaskInfo reconfigureVirtualMachine(VMPropertyHandler paramHandler)
            throws Exception {
        LOG.debug("instanceName: " + instanceName);

        VimPortType service = vmw.getConnection().getService();
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

        vmConfigSpec
                .setMemoryMB(Long.valueOf(paramHandler.getConfigMemoryMB()));
        vmConfigSpec.setNumCPUs(Integer.valueOf(paramHandler.getConfigCPUs()));

        String reqUser = paramHandler
                .getServiceSetting(VMPropertyHandler.REQUESTING_USER);

        String comment = Messages.get(paramHandler.getLocale(), "vm_comment",
                new Object[] { paramHandler.getSettings().getOrganizationName(),
                        paramHandler.getSettings().getSubscriptionId(),
                        reqUser });
        String annotation = vmConfigSpec.getAnnotation();
        comment = updateComment(comment, annotation);
        vmConfigSpec.setAnnotation(comment);

        DiskManager diskManager = new DiskManager(vmw, paramHandler);
        diskManager.reconfigureDisks(vmConfigSpec, vmInstance);

        NetworkManager.configureNetworkAdapter(vmw, vmConfigSpec, paramHandler,
                vmInstance);

        LOG.debug("Call vSphere API: reconfigVMTask()");
        ManagedObjectReference reconfigureTask = service
                .reconfigVMTask(vmInstance, vmConfigSpec);

        return (TaskInfo) vmw.getServiceUtil()
                .getDynamicProperty(reconfigureTask, "info");
    }

    public TaskInfo updateCommentField(String comment) throws Exception {
        LOG.debug("instanceName: " + instanceName + " comment: " + comment);
        VimPortType service = vmw.getConnection().getService();
        VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
        String annotation = vmConfigSpec.getAnnotation();
        comment = updateComment(comment, annotation);
        vmConfigSpec.setAnnotation(comment);
        LOG.debug("Call vSphere API: reconfigVMTask()");
        ManagedObjectReference reconfigureTask = service
                .reconfigVMTask(vmInstance, vmConfigSpec);

        return (TaskInfo) vmw.getServiceUtil()
                .getDynamicProperty(reconfigureTask, "info");
    }

    String updateComment(String comment, String annotation) {
        if (annotation == null) {
            annotation = "";
        }
        Pattern pattern = Pattern.compile(
                ".*" + "CT-MG \\{" + "[\\r\\n]+(.*?)[\\r\\n]+" + "\\}" + ".*",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(annotation);
        if (matcher.find()) {
            return annotation.replace(matcher.group(1), comment);
        }

        if (annotation.trim().length() == 0) {
            return "CT-MG {\n".concat(comment).concat("\n}");
        }
        return annotation.concat("\n").concat("CT-MG {\n").concat(comment)
                .concat("\n}");
    }

    /**
     * Delete VMware instance on vSphere server.
     * 
     * @param vmw
     *            connected VMware client entity
     * @param instanceId
     *            id of the instance
     */
    public TaskInfo delete() throws Exception {
        LOG.debug("Call vSphere API: destroyTask() instanceName: "
                + instanceName);
        ManagedObjectReference startTask = vmw.getConnection().getService()
                .destroyTask(vmInstance);

        return (TaskInfo) vmw.getServiceUtil().getDynamicProperty(startTask,
                "info");
    }

    public VMwareGuestSystemStatus getState(VMPropertyHandler properties)
            throws Exception {

        boolean networkCardsConnected = areNetworkCardsConnected();
        boolean validHostname = isValidHostname();
        boolean validIp = isValidIp(properties);

        if (isLinux()) {
            boolean firstStart = isNotEmpty(guestInfo.getHostName()) && !validIp
                    && isGuestSystemRunning() && areGuestToolsRunning()
                    && networkCardsConnected;

            boolean secondStart = validHostname && validIp
                    && isGuestSystemRunning() && areGuestToolsRunning()
                    && networkCardsConnected;

            if (firstStart || secondStart) {
                LOG.debug("firstStart: " + firstStart + " secondStart: "
                        + secondStart);
                return VMwareGuestSystemStatus.GUEST_READY;
            }

            LOG.debug(createLogForGetState(validHostname, properties,
                    networkCardsConnected, validIp));
            return VMwareGuestSystemStatus.GUEST_NOTREADY;
        }

        if (validHostname && networkCardsConnected && validIp
                && isGuestSystemRunning() && areGuestToolsRunning()) {
            return VMwareGuestSystemStatus.GUEST_READY;
        }

        LOG.debug(createLogForGetState(validHostname, properties,
                networkCardsConnected, validIp));
        return VMwareGuestSystemStatus.GUEST_NOTREADY;
    }

    private String createLogForGetState(boolean validHostname,
            VMPropertyHandler configuration, boolean isConnected,
            boolean validIp) {

        StringBuilder sb = new StringBuilder();
        sb.append("Guest system is not ready yet ");
        sb.append("[");
        sb.append("hostname (" + validHostname + ") =" + guestInfo.getHostName()
                + ", ");
        sb.append("ipReady=" + validIp + ", ");
        for (int i = 1; i <= configuration.getNumberOfNetworkAdapter(); i++) {
            GuestNicInfo info = getNicInfo(configuration, i);
            if (info != null) {
                sb.append(info.getNetwork() + "=");
                sb.append(info.getIpAddress());
                sb.append(",");
            }
        }
        sb.append("guestState=" + guestInfo.getGuestState() + ", ");
        sb.append("toolsState=" + guestInfo.getToolsStatus() + ", ");
        sb.append("toolsRunning=" + guestInfo.getToolsRunningStatus() + ", ");
        sb.append("isConnected=" + isConnected);
        sb.append("]");
        String logStatement = sb.toString();
        return logStatement;
    }

    private boolean isNotEmpty(String validate) {
        return validate != null && validate.length() > 0;
    }

    boolean areGuestToolsRunning() {
        return TOOLS_RUNNING_STATE.equals(guestInfo.getToolsRunningStatus());
    }

    boolean isGuestSystemRunning() {
        return GUEST_STATE_RUNNING.equals(guestInfo.getGuestState());
    }

    boolean areNetworkCardsConnected() {
        boolean isConnected = false;
        if (guestInfo.getNet() != null && !guestInfo.getNet().isEmpty()) {
            isConnected = true;
        }
        for (GuestNicInfo nicInfo : guestInfo.getNet()) {
            isConnected = isConnected && nicInfo.isConnected();
        }
        return isConnected;
    }

    boolean isValidHostname() {
        String hostname = guestInfo.getHostName();
        return hostname != null && hostname.length() > 0 && hostname
                .toUpperCase().startsWith(instanceName.toUpperCase());
    }

    boolean isValidIp(VMPropertyHandler configuration) {
        for (int i = 1; i <= configuration.getNumberOfNetworkAdapter(); i++) {
            GuestNicInfo info = getNicInfo(configuration, i);
            if (info == null) {
                return false;
            }

            if (configuration.isAdapterConfiguredManually(i)) {
                if (!containsIpAddress(info, configuration.getIpAddress(i))) {
                    return false;
                }
            } else {
                if (!ipAddressExists(info)) {
                    return false;
                }
            }
        }

        return true;
    }

    GuestNicInfo getNicInfo(VMPropertyHandler configuration, int i) {
        if (configuration.getNetworkAdapter(i) == null) {
            return null;
        }
        for (GuestNicInfo info : guestInfo.getNet()) {
            if (configuration.isAdapterConfiguredByDhcp(i)) {
                return info;
            }
            if (configuration.getNetworkAdapter(i).equals(info.getNetwork())) {
                return info;
            }
        }
        return null;
    }

    boolean containsIpAddress(GuestNicInfo info, String address) {
        return info.getIpAddress().contains(address);
    }

    boolean guestInfoContainsNic(String adapter) {
        for (GuestNicInfo info : guestInfo.getNet()) {
            if (info.getNetwork().equals(adapter)) {
                return true;
            }
        }

        return false;
    }

    boolean ipAddressExists(GuestNicInfo info) {
        if (info.getIpAddress().isEmpty()) {
            return false;
        }

        for (String ip : info.getIpAddress()) {
            if (ip == null || ip.trim().length() == 0) {
                return false;
            }
        }

        return true;
    }

    public String generateAccessInfo(VMPropertyHandler paramHandler)
            throws Exception {

        VMwareAccessInfo accInfo = new VMwareAccessInfo(paramHandler);
        String accessInfo = accInfo.generateAccessInfo(guestInfo);
        LOG.debug("Generated access information for service instance '"
                + instanceName + "':\n" + accessInfo);
        return accessInfo;
    }

    private int getDataDiskKey() throws Exception {
        List<VirtualDevice> devices = configSpec.getHardware().getDevice();
        int countDisks = 0;
        int key = -1;
        for (VirtualDevice vdInfo : devices) {
            if (vdInfo instanceof VirtualDisk) {
                countDisks++;
                if (countDisks == 2) {
                    key = ((VirtualDisk) vdInfo).getKey();
                    break;
                }
            }
        }

        return key;
    }

    private String getDiskSizeInGB(int disk) throws Exception {
        String size = "";
        List<VirtualDevice> devices = configSpec.getHardware().getDevice();
        int countDisks = 0;
        for (VirtualDevice vdInfo : devices) {
            if (vdInfo instanceof VirtualDisk) {
                countDisks++;
                if (countDisks == disk) {
                    long gigabyte = ((VirtualDisk) vdInfo).getCapacityInKB()
                            / 1024 / 1024;
                    size = Long.toString(gigabyte);
                    break;
                }
            }
        }

        return size;
    }

    public String getTotalDiskSizeInMB() throws Exception {
        long megabyte = 0;
        List<VirtualDevice> devices = configSpec.getHardware().getDevice();
        for (VirtualDevice vdInfo : devices) {
            if (vdInfo instanceof VirtualDisk) {
                megabyte = megabyte
                        + (((VirtualDisk) vdInfo).getCapacityInKB() / 1024);
            }
        }

        return Long.toString(megabyte);
    }

    public Integer getNumCPU() {
        return configSpec.getHardware().getNumCPU();
    }

    public Integer getCoresPerCPU() {
        return configSpec.getHardware().getNumCoresPerSocket();
    }

    public String getCPUModel(VMPropertyHandler paramHandler) throws Exception {
        String datacenter = paramHandler.getTargetDatacenter();
        ManagedObjectReference dataCenterRef = vmw.getServiceUtil()
                .getDecendentMoRef(null, "Datacenter", datacenter);
        if (dataCenterRef == null) {
            LOG.error("Datacenter not found. dataCenter: " + datacenter);
            throw new APPlatformException(Messages.get(paramHandler.getLocale(),
                    "error_invalid_datacenter", new Object[] { datacenter }));
        }

        String hostName = paramHandler
                .getServiceSetting(VMPropertyHandler.TS_TARGET_HOST);
        ManagedObjectReference hostRef = vmw.getServiceUtil()
                .getDecendentMoRef(dataCenterRef, "HostSystem", hostName);
        if (hostRef == null) {
            LOG.error("Target host " + hostName + " not found");
            throw new APPlatformException(Messages.getAll("error_invalid_host",
                    new Object[] { hostName }));
        }

        return (String) vmw.getServiceUtil().getDynamicProperty(hostRef,
                "summary.hardware.cpuModel");
    }

    /**
     * @return fully qualified domain name
     */
    public String getFQDN() {
        // TODO do not remove method. Please implement, return FQDN
        return "";
    }
}
