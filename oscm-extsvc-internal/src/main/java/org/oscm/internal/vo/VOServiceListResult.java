/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.vo.VOService;

/**
 * Represents a page of the filtered, paged, and sorted results of a search for
 * services, including information on the total number of services found.
 * 
 */
public class VOServiceListResult implements Serializable {

    private static final long serialVersionUID = 4913915074444382585L;

    private List<VOService> services = new ArrayList<VOService>();
    private int resultSize;

    /**
     * Returns the services that make up the content of this result page.
     * 
     * @return the list of services
     */
    public List<VOService> getServices() {
        return services;
    }

    /**
     * Sets the services that make up the content of this result page.
     * 
     * @param services
     *            the list of services
     */
    public void setServices(List<VOService> services) {
        this.services = services;
    }

    /**
     * Returns the total number of services found by the search. This number is
     * greater than or equal to the number of services on this result page.
     * 
     * @return the number of results
     */
    public int getResultSize() {
        return resultSize;
    }

    /**
     * Sets the total number of services found by the search. This number is
     * greater than or equal to the number of services on this result page.
     * 
     * @param resultSize
     *            the number of results
     */
    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }
}
