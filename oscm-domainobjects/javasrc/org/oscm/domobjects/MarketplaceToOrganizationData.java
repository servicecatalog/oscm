/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 15.02.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.LBRTConverter;
import org.oscm.domobjects.converters.PAConverter;
import org.oscm.domobjects.enums.PublishingAccess;

/**
 * @author stavreva
 * 
 */
@Embeddable
public class MarketplaceToOrganizationData extends DomainDataContainer {

    private static final long serialVersionUID = -8262011730647781904L;

    @Enumerated(EnumType.STRING)
    private PublishingAccess publishingAccess;

    public PublishingAccess getPublishingAccess() {
        return publishingAccess;
    }

    public void setPublishingAccess(PublishingAccess publishingAccess) {
        this.publishingAccess = publishingAccess;
    }

}
