/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * Represents the history entries for the PSP domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQuery(name = "PSPHistory.findByObject", query = "SELECT c FROM PSPHistory c WHERE c.objKey=:objKey ORDER BY objversion")
public class PSPHistory extends DomainHistoryObject<PSPData> {

    private static final long serialVersionUID = -907768430215359484L;

    public PSPHistory() {
        dataContainer = new PSPData();
    }

    /**
     * Constructs PSPHistory from a Product domain object
     * 
     * @param c
     *            - the product
     */
    public PSPHistory(PSP c) {
        super(c);
    }

    public String getIdentifier() {
        return dataContainer.getIdentifier();
    }

    public String getWsdlUrl() {
        return dataContainer.getWsdlUrl();
    }

}
