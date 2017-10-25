/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 11, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model;

/**
 * Generates unique object ids for report data objects
 * 
 * @author kulle
 * 
 */
public class RdoIdGenerator {

    private int value = -1;

    public int nextValue() {
        if (value == Integer.MAX_VALUE) {
            value = -1;
        }
        return ++value;
    }

}
