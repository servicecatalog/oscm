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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Represents an account of an organization in a PSP system.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
public class PSPAccount extends DomainObjectWithHistory<PSPAccountData> {

    private static final long serialVersionUID = 8154142747523341856L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PSP psp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organization organization;

    public PSPAccount() {
        super();
        dataContainer = new PSPAccountData();
    }

    public void setPspIdentifier(String pspIdentifier) {
        dataContainer.setPspIdentifier(pspIdentifier);
    }

    public String getPspIdentifier() {
        return dataContainer.getPspIdentifier();
    }

    public void setPsp(PSP psp) {
        this.psp = psp;
    }

    public PSP getPsp() {
        return psp;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }
}
