/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 2019-02-22
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.util.Iterator;
import java.util.List;

import org.oscm.app.v2_0.intf.ServerInformation;

public class Server implements ServerInformation{

    /**
     * 
     */
    private static final long serialVersionUID = 9104320751159296413L;
    private String id;
    private String name;
    private String status;
    private String type;
    private List<String> privateIP;
    private List<String> publicIP;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
        
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
        
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
        
    }

    @Override
    public List<String> getPublicIP() {
        return publicIP;
    }

    @Override
    public void setPublicIP(List<String> publicIP) {
        this.publicIP = publicIP;
    }

    @Override
    public List<String> getPrivateIP() {
        return privateIP;
    }

    @Override
    public void setPrivateIP(List<String> privateIP) {
        this.privateIP = privateIP;
        
    }

    @Override
    public String getPrivateIPasString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = privateIP.iterator(); iterator
                .hasNext();) {
            String ip = iterator.next();
            sb.append(ip);
            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String getPublicIPasString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = publicIP.iterator(); iterator
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
