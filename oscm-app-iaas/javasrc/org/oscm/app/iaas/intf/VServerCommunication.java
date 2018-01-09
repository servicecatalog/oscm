/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.02.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.iaas.intf;

import javax.ejb.Local;

import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.data.FlowState;

/**
 * Interface for communication with IaaS-API regarding tasks related to virtual
 * servers.
 */
@Local
public interface VServerCommunication {

    /**
     * Creates a VSERVER for the given VSYS with the given name.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the VServer id
     * @throws Exception
     */
    public String createVServer(PropertyHandler paramHandler) throws Exception;

    /**
     * Destroys the VSERVER with the given id - by doing that all data is lost.
     * 
     * 
     * @param paramHandler
     *            the parameter handler
     * @throws Exception
     */
    public void destroyVServer(PropertyHandler paramHandler) throws Exception;

    /**
     * modify a VSERVER for the given id with the given values.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the new FlowState according to necessary action
     * 
     * @throws Exception
     */
    public FlowState modifyVServerAttributes(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Starts the VServer associated with the VSYS with the given id.
     * 
     * 
     * @param paramHandler
     *            the parameter handler
     * @throws Exception
     */

    public boolean startVServer(PropertyHandler paramHandler) throws Exception;

    /**
     * Retrieves the current VServer status.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the string representation of the current status
     * @throws Exception
     */
    public String getVServerStatus(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Retrieves the current VServer status and throws a SuspendException if the
     * VServer is in an error state.
     * 
     * @param ph
     *            The parameter handler
     * @return the string representation of the current status
     * @throws Exception
     */
    public String getNonErrorVServerStatus(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Stops the VServers associated with the VSYS with the given id.
     * 
     * @param paramHandler
     *            the parameter handler
     * @throws Exception
     */

    public void stopVServer(PropertyHandler paramHandler) throws Exception;

    /**
     * Checks if the virtual system ID is valid.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the VSYS id is available otherwise
     *         <code>false</code>
     * @throws Exception
     */

    public boolean isVSysIdValid(PropertyHandler paramHandler) throws Exception;

    /**
     * Checks if the network ID is valid within the given virtual system.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the network id is available otherwise
     *         <code>false</code>
     * @throws Exception
     */
    public boolean isNetworkIdValid(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Checks if the virtual server type exists for the given disk image ID.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the server type is available otherwise
     *         <code>false</code>
     * @throws Exception
     */
    public boolean isServerTypeValid(PropertyHandler paramHandler)
            throws Exception;

    public boolean isVServerDestroyed(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Checks if the virtual server private IP exists.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the internal private IP
     * @throws Exception
     *             thrown if the IP does not exist
     */
    public String getInternalIp(PropertyHandler paramHandler) throws Exception;

    /**
     * Retrieve the virtual server initial password.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the initial password
     * @throws Exception
     *             throw if the password does not exist
     */
    public String getVServerInitialPassword(PropertyHandler paramHandler)
            throws Exception;

}
