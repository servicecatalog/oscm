/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 25.02.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import org.oscm.domobjects.enums.OrganizationReferenceType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author weiser
 * 
 */
@Embeddable
public class OrganizationReferenceData extends DomainDataContainer {

    private static final long serialVersionUID = -66626743587180388L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationReferenceType referenceType;

    public void setReferenceType(OrganizationReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    public OrganizationReferenceType getReferenceType() {
        return referenceType;
    }

}
