/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-19                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * 
 */
public class NATRule {

    private String internalIP;
    private String externalIP;
    private boolean isSnapt;

    public String getInternalIP() {
        return internalIP;
    }

    public void setInternalIP(String internalIP) {
        this.internalIP = internalIP;
    }

    public String getExternalIP() {
        return externalIP;
    }

    public void setExternalIP(String externalIP) {
        this.externalIP = externalIP;
    }

    public boolean isSnapt() {
        return isSnapt;
    }

    public void setSnapt(boolean isSnapt) {
        this.isSnapt = isSnapt;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("NATRule[");
        sb.append(getExternalIP());
        sb.append(isSnapt ? "<=>" : "=>");
        sb.append(getInternalIP());
        sb.append("]");
        return sb.toString();
    }
}
