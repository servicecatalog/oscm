/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas.intf;

import javax.ejb.Local;

import org.oscm.app.iaas.PropertyHandler;

/**
 * Local interface encapsulating the virtual disk related API calls.
 */
@Local
public interface VDiskCommunication {

    /**
     * Creates a VDISK for the given VSYS with the given name.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return the VServer id
     * @throws Exception
     */
    public String createVDisk(PropertyHandler paramHandler) throws Exception;

    /**
     * Checks if the VDISK associated with the VSYS with the given id is in
     * deployed state.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the disk is deployed otherwise
     *         <code>false</code>
     * @throws Exception
     */
    public boolean isVDiskDeployed(PropertyHandler paramHandler)
            throws Exception;

    /**
     * attach a VDISK to virtual server for the given VSYS.
     * 
     * @param paramHandler
     *            the parameter handler
     * 
     * @throws Exception
     */
    public void attachVDisk(PropertyHandler paramHandler) throws Exception;

    /**
     * Checks if the VDISK associated with the VSYS with the given id is in
     * attached state to a virtual server.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the disk is attached otherwise
     *         <code>false</code>
     * @throws Exception
     */
    public boolean isVDiskAttached(PropertyHandler paramHandler)
            throws Exception;

    public boolean isAdditionalDiskSelected(PropertyHandler paramHandler)
            throws Exception;

    /**
     * Checks if the VDISKS associated with the virtual server are in detached
     * state.
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the disks are detached otherwise
     *         <code>false</code>
     * @throws Exception
     */

    public boolean areVDisksDetached(PropertyHandler paramHandler)
            throws Exception;

    public boolean isAttachedVDisksFound(PropertyHandler paramHandler)
            throws Exception;

    public void detachVDisks(PropertyHandler paramHandler) throws Exception;

    /**
     * Checks if the VDISKS associated with the virtual server are destroyed.
     * 
     * 
     * @param paramHandler
     *            the parameter handler
     * @return <code>true</code> if the disks are detached otherwise
     *         <code>false</code>
     * @throws Exception
     */

    public boolean areVDisksDestroyed(PropertyHandler paramHandler)
            throws Exception;

    public void destroyVDisks(PropertyHandler paramHandler) throws Exception;

}
