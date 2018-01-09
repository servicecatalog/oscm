/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 25, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.sample.controller;

import java.util.List;

import org.oscm.app.v2_0.intf.ServerInformation;

/**
 * Data class for custom tab.
 * 
 * @author miethaner
 */
public class Server implements ServerInformation {

    private static final long serialVersionUID = 1227697097068163229L;

    private String id;
    private String name;
    private String status;
    private String flavor;
    private List<String> floatingIP;
    private List<String> fixedIP;

    /**
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the status
     */
    @Override
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the flavor
     */
    @Override
    public String getType() {
        return flavor;
    }

    /**
     * @param flavor
     *            the flavor to set
     */
    @Override
    public void setType(String flavor) {
        this.flavor = flavor;
    }

    /**
     * @return the floatingIP
     */
    @Override
    public List<String> getPublicIP() {
        return floatingIP;
    }

    /**
     * @param floatingIP
     *            the floatingIP to set
     */
    @Override
    public void setPublicIP(List<String> floatingIP) {
        this.floatingIP = floatingIP;
    }

    /**
     * @return the fixedIP
     */
    @Override
    public List<String> getPrivateIP() {
        return fixedIP;
    }

    /**
     * @param fixedIP
     *            the fixedIP to set
     */
    @Override
    public void setPrivateIP(List<String> fixedIP) {
        this.fixedIP = fixedIP;
    }

    @Override
    public String getPrivateIPasString() {
        StringBuilder sb = new StringBuilder();
        for (String ip : fixedIP) {
            sb.append(ip);
            sb.append(",");
        }

        return sb.toString();
    }

    @Override
    public String getPublicIPasString() {
        StringBuilder sb = new StringBuilder();
        for (String ip : floatingIP) {
            sb.append(ip);
            sb.append(",");
        }

        return sb.toString();
    }
}
