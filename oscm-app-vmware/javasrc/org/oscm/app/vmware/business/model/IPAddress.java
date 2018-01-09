/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.model;

/**
 * Reflects a single IP address configuration.
 */
public class IPAddress {

    private String ipaddress = "";

    public String getIPAddress() {
        return ipaddress;
    }

    public void setIPAddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

}
