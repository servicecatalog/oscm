/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * History object of the {@link OrganizationReference}, used for auditing.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQuery(name = "OrganizationReferenceHistory.findByObject", query = "select h from OrganizationReferenceHistory h where h.objKey=:objKey order by objversion")
public class OrganizationReferenceHistory extends
        DomainHistoryObject<OrganizationReferenceData> {

    private static final long serialVersionUID = 8902366157116526046L;

    public OrganizationReferenceHistory() {
        dataContainer = new OrganizationReferenceData();
    }

    public OrganizationReferenceHistory(OrganizationReference orgReference) {
        super(orgReference);
        if (orgReference.getSource() != null) {
            sourceObjKey = orgReference.getSource().getKey();
        }
        if (orgReference.getTarget() != null) {
            targetObjKey = orgReference.getTarget().getKey();
        }
    }

    /**
     * Reference to the technology provider (key only).
     */
    private long sourceObjKey;

    /**
     * Reference to the supplier (key only).
     */
    private long targetObjKey;

    public long getSourceObjKey() {
        return sourceObjKey;
    }

    public long getTargetObjKey() {
        return targetObjKey;
    }

}
