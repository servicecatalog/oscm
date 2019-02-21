package org.oscm.app.vmware.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.vmware.VMClientPool;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VMwareProcessor {
    private static final Logger logger = LoggerFactory
            .getLogger(VMwareProcessor.class);
    public List<Server> getServersDetails(VMPropertyHandler ph) {

        String instanceName = "";
        String vcenter = ph
                .getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
        VMwareClient vmClient = null;
        ArrayList<String> publicIp= new ArrayList<String>();
        List<Server> servers = new ArrayList<>();
        ArrayList<String> VMDetails = new ArrayList<String>();
        ArrayList<String> serverInformation = new ArrayList<String>();
        try {
            vmClient = VMClientPool.getInstance().getPool()
                    .borrowObject(vcenter);
            instanceName = ph.getInstanceName();
            VMDetails.add("CPUs: " + String.valueOf(ph.getConfigCPUs()));
            VMDetails.add("Disc Space MB: " + String.valueOf(ph.getConfigDiskSpaceMB()));
            VMDetails.add("Memory MB: " + String.valueOf(ph.getConfigMemoryMB()));
            serverInformation.add("Cluster: " + ph.getTargetCluster());
            serverInformation.add("Datacenter: " + ph.getTargetDatacenter());
            serverInformation.add("VCenter: " + ph.getTargetVCenterServer());
            
            VM vm = new VM(vmClient, instanceName);
            Server server = new Server();
            server.setStatus(vm.getState(ph).name());
            server.setId(instanceName);
            server.setName(vm.getGuestFullName());
            publicIp.add(ph.getAccessInfo().toString().replace(instanceName + ", ", ""));
            server.setPublicIP(serverInformation);
            server.setPrivateIP(VMDetails);
            server.setType(getStringFromList(publicIp));
            servers.add(server);
            
        } catch (Exception e) {
            logger.error("Failed to create serverlist");
        }
        return servers;
    }
    
    public String getStringFromList(List list) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = list.iterator(); iterator
                .hasNext();) {
            String ip = iterator.next();
            sb.append(ip);
            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
