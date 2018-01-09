/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.persistence;

/**
 * A bag for network settings that belong to an IP address and have been
 * retrieved from the database.
 *
 * @author petrovski
 *
 */
public class VMwareNetwork {

    private String subnetMask;
    private String gateway;
    private String dnsServer;
    private String dnsSuffix;

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getDnsServer() {
        return dnsServer;
    }

    public void setDnsServer(String dnsServer) {
        this.dnsServer = dnsServer;
    }

    public String getDnsSuffix() {
        return dnsSuffix;
    }

    public void setDnsSuffix(String dnsSuffix) {
        this.dnsSuffix = dnsSuffix;
    }

}
