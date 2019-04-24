/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2019-04-16
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.app.v2_0.exceptions.APPlatformException;

public class PortgroupIpSettings {

    private VMPropertyHandler ph;
    private List<String> ips;
    private static Map<String, Boolean> inUse;

    PortgroupIpSettings(VMPropertyHandler ph) {
        this.ph = ph;
        this.ips = getIpList();
        if(inUse == null) {
            inUse = new HashMap<String, Boolean>();
        }
    }

    private List<String> getIpList() {
        String ippools = ph.getIpPoolForPortgroup();
        String[] ips = ippools.split(",");
        return Arrays.asList(ips);
    }

    public String getIpAdressFromIpPool() throws APPlatformException {
        for (int i = 0; ips.size() > i; i++) {
            if (!ipIsInUse(ips.get(i))) {
                useIpAdress(ips.get(i));
                return ips.get(i);
            }
        }
        throw new APPlatformException(
                "No IP address found Check whether you have specified an IP pool for the port group in the Technical Service. If so, there may not be IPs left in this pool");
    }
    
    private boolean ipIsInUse(String ip) {
        boolean ipExists = inUse.containsKey(ip);
        if (ipExists) {
            return inUse.get(ip);
        } else {
            inUse.put(ip, false);
            return false;
        }
    }

    private void useIpAdress(String ip) throws IllegalArgumentException {
        boolean ipExists = inUse.containsKey(ip);
        if (!ipExists) {
            throw new IllegalArgumentException(
                    "The ip adress " + ip + " is already used");
        } else {
            inUse.put(ip, true);
        }
    }

    
    public void returnIpAdressToIpPool(String ip) throws IllegalArgumentException {
        boolean ipExists = inUse.containsKey(ip);
        if (ipExists) {
            inUse.put(ip, false);
        } else {
            throw new IllegalArgumentException(
                    "The ip adress " + ip + " can not be returned. The Ip does not exist");
        }
    }
    
}
