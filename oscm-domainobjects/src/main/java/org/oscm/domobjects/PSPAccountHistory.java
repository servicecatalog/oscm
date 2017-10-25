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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * The history object for a PSP account instance.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQuery(name = "PSPAccountHistory.findByObject", query = "SELECT c FROM PSPAccountHistory c WHERE c.objKey=:objKey ORDER BY objversion")
public class PSPAccountHistory extends DomainHistoryObject<PSPAccountData> {

    private static final long serialVersionUID = -5341601464399088498L;

    @Column(nullable = false)
    private long pspObjKey;

    @Column(nullable = false)
    private long organizationObjKey;

    public PSPAccountHistory() {
        dataContainer = new PSPAccountData();
    }

    /**
     * Constructs PSPAccountHistory from a PSPAccount domain object
     * 
     * @param c
     *            - the psp account
     */
    public PSPAccountHistory(PSPAccount c) {
        super(c);
        if (c.getOrganization() != null) {
            setOrganizationObjKey(c.getOrganization().getKey());
        }
        if (c.getPsp() != null) {
            setPspObjKey(c.getPsp().getKey());
        }
    }

    public void setPspIdentifier(String pspIdentifier) {
        dataContainer.setPspIdentifier(pspIdentifier);
    }

    public String getPspIdentifier() {
        return dataContainer.getPspIdentifier();
    }

    public void setPspObjKey(long pspObjKey) {
        this.pspObjKey = pspObjKey;
    }

    public long getPspObjKey() {
        return pspObjKey;
    }

    /**
     * @param organizationObjKey
     *            the organizationObjKey to set
     */
    public void setOrganizationObjKey(long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

    /**
     * @return the organizationObjKey
     */
    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

}
