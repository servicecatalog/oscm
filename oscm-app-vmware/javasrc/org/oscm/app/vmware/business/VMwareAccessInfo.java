/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestNicInfo;

/**
 * Custom generation of access information output.
 */
public class VMwareAccessInfo {
    
    private static final Logger logger = LoggerFactory
            .getLogger(VMPropertyHandler.class);

    private static final String PATTERN_IP = "${IP}";
    private static final String PATTERN_HOST = "${HOST}";
    private static final String PATTERN_CPU = "${CPU}";
    private static final String PATTERN_MEM = "${MEM}";
    private static final String PATTERN_DISKS = "${DISK}";
    private static final String PATTERN_RESPUSER = "${RESPUSER}";

    private VMPropertyHandler paramHandler;

    public VMwareAccessInfo(VMPropertyHandler paramHandler) {
        this.paramHandler = paramHandler;
    }

    /**
     * Returns the generated access info for the given VM.
     */
    public String generateAccessInfo(GuestInfo guestInfo)
            throws APPlatformException {

        String myHOST = guestInfo.getHostName();
        String hostName;
        if (myHOST != null) {
            hostName = guestInfo.getHostName().split("\\.", 2)[0];
        } else {
            hostName = "Unknown hostname (probably missing vmware tools).\nInstance name "
                    + paramHandler.getInstanceName() + ".";
            myHOST = "Unknown (InstanceName " + paramHandler.getInstanceName()
                    + ")";
        }

        String accessInfoPattern = paramHandler.getAccessInfo();
        if (accessInfoPatternUndefined(accessInfoPattern)) {
            logger.debug("No access info pattern defined.");
            return hostName;
        }

        String accessInfo = accessInfoPattern.replace(PATTERN_IP,
                getIpAddress(guestInfo));
        accessInfo = accessInfo.replace(PATTERN_HOST, myHOST);
        accessInfo = accessInfo.replace(PATTERN_CPU,
                Integer.toString(paramHandler.getConfigCPUs()));
        accessInfo = accessInfo.replace(PATTERN_MEM,
                paramHandler.formatMBasGB(paramHandler.getConfigMemoryMB()));
        accessInfo = accessInfo.replace(PATTERN_DISKS,
                paramHandler.getDataDisksAsString());

        accessInfo = accessInfo.replace(PATTERN_RESPUSER, getResponsibleUser());
        accessInfo = accessInfo.replace("<br>", "<br>\r\n");
        return accessInfo;
    }

    private boolean accessInfoPatternUndefined(String accessInfoPattern) {
        return accessInfoPattern == null
                || accessInfoPattern.trim().length() == 0;
    }

    private String getIpAddress(GuestInfo guestInfo) {
        String iplist = getIpAddressFromConfiguredNetAdapters(guestInfo);
        if (iplist.length() == 0) {
            logger.debug("No matching adapters found for " + guestInfo.getHostName());
            return guestInfo.getIpAddress();
        }
        return iplist;
    }

    private String getIpAddressFromConfiguredNetAdapters(GuestInfo guestInfo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= paramHandler.getNumberOfNetworkAdapter(); i++) {
            GuestNicInfo info = getNicInfo(guestInfo,
                    paramHandler.getNetworkAdapter(i));
            if (info != null) {
                logger.debug("Taking info from adapter " +  paramHandler.getNetworkAdapter(i));
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                if (paramHandler.getNetworkAdapter(i) != null && !paramHandler
                        .getNetworkAdapter(i).trim().isEmpty()) {
                    sb.append(paramHandler.getNetworkAdapter(i) + ": ");
                }
                sb.append(info.getIpAddress());

            }
        }
        return sb.toString();
    }

    GuestNicInfo getNicInfo(GuestInfo guestInfo, String adapter) {
        for (GuestNicInfo info : guestInfo.getNet()) {
            if (info != null && adapter.equals(info.getNetwork())) {
                return info;
            }
        }
        return null;
    }

    private String getResponsibleUser() {
        String respuser = paramHandler
                .getResponsibleUserAsString(paramHandler.getLocale());
        if (respuser == null) {
            respuser = "";
        }
        return respuser;
    }

}
