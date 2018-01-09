/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * Represents a virtual network.
 */
public class Network {

    private String name;
    private String category;
    private String networkId;
    private int numOfMaxVm;

    public Network(String name, String networkCategory, String networkId,
            int numOfMaxVm) {
        this.name = name;
        this.category = networkCategory;
        this.networkId = networkId;
        this.numOfMaxVm = numOfMaxVm;
    }

    /**
     * Returns the name of the network.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the network.
     * 
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the category of the network.
     * 
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the network.
     * 
     * @param category
     *            the network category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the ID of the network.
     * 
     * @return the network ID
     */
    public String getId() {
        return networkId;
    }

    /**
     * Sets the network ID.
     * 
     * @param networkId
     *            the networkId to set
     */
    public void setId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * Sets the maximum number of VMs allowed in the network.
     * 
     * @param numOfMaxVm
     *            the maximum number of VMs
     */
    public void setNumOfMaxVm(int numOfMaxVm) {
        this.numOfMaxVm = numOfMaxVm;
    }

    /**
     * Returns the maximum number of VMs allowed in the network.
     * 
     * @return the maximum number of VMs
     */
    public int getNumOfMaxVm() {
        return numOfMaxVm;
    }
}
