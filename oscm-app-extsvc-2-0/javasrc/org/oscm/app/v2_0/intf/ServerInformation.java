/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/11/11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.intf;

import java.io.Serializable;
import java.util.List;

/**
 * @author tateiwamext
 *
 */
public interface ServerInformation extends Serializable {

    /**
     * @return the id of server
     */
    public String getId();

    /**
     * @param id
     *            the id of server to set
     */
    public void setId(String id);

    /**
     * @return the name of server
     */
    public String getName();

    /**
     * @param name
     *            the name of server to set
     */
    public void setName(String name);

    /**
     * @return the status of server
     */
    public String getStatus();

    /**
     * @param status
     *            the status of server to set
     */
    public void setStatus(String status);

    /**
     * @return the type of server
     */
    public String getType();

    /**
     * @param type
     *            the type of server to set
     */
    public void setType(String type);

    /**
     * @return the publicIP list of server
     */
    public List<String> getPublicIP();

    /**
     * @param publicIP
     *            the publicIP list of server to set
     */
    public void setPublicIP(List<String> publicIP);

    /**
     * @return the privateIP list of server
     */
    public List<String> getPrivateIP();

    /**
     * @param privateIP
     *            the privateIP list of server to set
     */
    public void setPrivateIP(List<String> privateIP);

    /**
     * @return
     */
    public String getPrivateIPasString();

    /**
     * @return
     */
    public String getPublicIPasString();
}
