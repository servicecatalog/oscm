/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 29.09.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;

import java.util.Iterator;
import java.util.List;

import org.oscm.app.v2_0.intf.ServerInformation;

/**
 * @author tateiwamext
 *
 */
public class Server implements ServerInformation {
    /**
     * 
     */
    private static final long serialVersionUID = 12051120577966326L;
    private String id;
    private String name;
    private String status;
    private String flavor;
    private List<String> floatingIP;
    private List<String> fixedIP;

    /**
     * @param serverId
     */
    public Server(String id) {
        this.id = id;
    }

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

    /*
     * (non-Javadoc)
     * 
     * @see org.oscm.app.common.intf.ServerInformation#getPrivateIPasString()
     */
    @Override
    public String getPrivateIPasString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = fixedIP.iterator(); iterator
                .hasNext();) {
            String ip = iterator.next();
            sb.append(ip);
            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.oscm.app.common.intf.ServerInformation#getPublicIPasString()
     */
    @Override
    public String getPublicIPasString() {

        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = floatingIP.iterator(); iterator
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
