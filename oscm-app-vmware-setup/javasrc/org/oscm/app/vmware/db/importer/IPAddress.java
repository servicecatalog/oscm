/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

/**
 * Reflects a single IP address configuration.
 */
public class IPAddress {

    private boolean isInUse;
    private String ipaddress;

    public IPAddress() {
        this.isInUse = false;
        this.ipaddress = "";
    }

    public String getIPAddress() {
        return ipaddress;
    }

    public void setIPAddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public boolean isInUse() {
        return isInUse;
    }

    public void setInUse(boolean inUse) {
        isInUse = inUse;
    }
}
