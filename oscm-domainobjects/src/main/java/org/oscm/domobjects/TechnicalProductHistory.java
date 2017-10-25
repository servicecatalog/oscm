/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * History-Object of Product, used for auditing. Will be automatically created
 * during persist, save or remove operations (if performed via DataManager)
 * 
 * @author schmid
 */
@Entity
@NamedQuery(name = "TechnicalProductHistory.findByObject", query = "select c from TechnicalProductHistory c where c.objKey=:objKey order by objversion")
public class TechnicalProductHistory extends
        DomainHistoryObject<TechnicalProductData> {

    private static final long serialVersionUID = -5737872211049291049L;

    public TechnicalProductHistory() {
        dataContainer = new TechnicalProductData();
    }

    /**
     * Constructs TechnicalProductHistory from a TechnicalProduct domain object
     * 
     * @param c
     *            - the technical product
     */
    public TechnicalProductHistory(TechnicalProduct c) {
        super(c);
        if (c.getOrganization() != null) {
            setOrganizationObjKey(c.getOrganization().getKey());
        }
    }

    /**
     * Reference to the Organization (only id)
     */
    private long organizationObjKey;

    public void setOrganizationObjKey(long organization_objid) {
        this.organizationObjKey = organization_objid;
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    /*
     * Delegate Methods
     */

    public String getBaseURL() {
        return dataContainer.getBaseURL();
    }

}
