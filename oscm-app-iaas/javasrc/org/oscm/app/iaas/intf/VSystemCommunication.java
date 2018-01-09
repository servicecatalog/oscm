/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.intf;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.data.AccessInformation;
import org.oscm.app.iaas.data.DiskImage;
import org.oscm.app.iaas.data.VSystemConfiguration;
import org.oscm.app.iaas.data.VSystemTemplate;
import org.oscm.app.iaas.data.VSystemTemplateConfiguration;

/**
 * Interface for communication with IaaS-API regarding tasks related to virtual
 * systems.
 * 
 * @author Dirk Bernsau
 * 
 */
@Local
public interface VSystemCommunication {

    public String createVSystem(PropertyHandler properties) throws Exception;

    public void destroyVSystem(PropertyHandler properties) throws Exception;

    public String getVSystemState(PropertyHandler properties) throws Exception;

    public boolean getCombinedVServerState(PropertyHandler properties,
            String targetState) throws Exception;

    public boolean startAllEFMs(PropertyHandler properties) throws Exception;

    public void startAllVServers(PropertyHandler properties) throws Exception;

    public void startVServers(PropertyHandler properties) throws Exception;

    public List<String> stopAllVServers(PropertyHandler properties)
            throws Exception;

    public List<String> getVServersForTemplate(String serverTemplateId,
            PropertyHandler properties) throws Exception;

    public String scaleUp(String masterTemplateId, String slaveTemplateId,
            PropertyHandler properties) throws Exception;

    /**
     * Retrieves a VSystemConfiguration by a given InstanceName 
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return the configuration
     * @throws Exception
     */
    public VSystemConfiguration getVSystemConfigurationByInstanceName(PropertyHandler properties)
            throws Exception;

    /**
     * Returns the public IP addresses of the virtual system (if present).
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return a list of all IP addresses of the system (may be empty but not
     *         <code>null</code>)
     * @throws Exception
     */
    public List<String> getPublicIps(PropertyHandler properties)
            throws Exception;

    /**
     * @param properties
     *            the property handler providing the request parameters
     * @return a list of all IP addresses and initial passwords of the system
     *         (may be empty but not <code>null</code>)
     * @throws Exception
     */
    public List<AccessInformation> getAccessInfo(PropertyHandler properties)
            throws Exception;

    /**
     * Retrieves all available configuration data of the virtual system
     * including basic information about virtual servers if applicable.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return the configuration
     * @throws Exception
     */
    public VSystemConfiguration getConfiguration(PropertyHandler properties)
            throws Exception;

    /**
     * Returns a list of available system templates.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return a list of system templates - may be empty but not
     *         <code>null</code>
     * @throws Exception
     */
    public List<VSystemTemplate> getVSystemTemplates(PropertyHandler properties)
            throws Exception;

    /**
     * Returns the details of the specified system template.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return the system templates details
     * @throws Exception
     */
    public VSystemTemplateConfiguration getVSystemTemplateConfiguration(
            PropertyHandler properties) throws Exception;

    /**
     * Returns a list of available disk images.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @return a list of disk images - may be empty but not <code>null</code>
     * @throws Exception
     */
    public List<DiskImage> getDiskImages(PropertyHandler properties)
            throws Exception;

    /**
     * Detach and free all public IP addresses registered in the virtual system.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @param externalIPs
     *            if present, the operation will affect only the given set of
     *            IPs
     * @return the highest of all states of the remaining public IP addresses or
     *         <code>null</code> if no more IPs exist
     * @throws Exception
     */
    public String freePublicIPs(PropertyHandler properties,
            Set<String> externalIPs) throws Exception;

    /**
     * Activates the given external IP addresses if necessary for use in NAT
     * settings.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @param externalIPs
     *            the set of IPs to be activated
     * @throws Exception
     */
    public void activatePublicIPs(PropertyHandler properties,
            Set<String> externalIPs) throws Exception;

    /**
     * Allocates an additional public IP address for use with the virtual
     * system.
     * 
     * @param properties
     *            the property handler providing the request parameters
     * @throws Exception
     */
    public void allocatePublicIP(PropertyHandler properties) throws Exception;
}
