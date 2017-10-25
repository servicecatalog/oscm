/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author weiser
 * 
 */
@Embeddable
public class OrganizationRefToPaymentTypeData extends DomainDataContainer {

    private static final long serialVersionUID = 6058048318727536357L;

    @Column(nullable = false)
    private boolean usedAsDefault;

    @Column(nullable = false)
    private boolean usedAsServiceDefault;

    public boolean isUsedAsDefault() {
        return usedAsDefault;
    }

    public void setUsedAsDefault(boolean usedAsDefault) {
        this.usedAsDefault = usedAsDefault;
    }

    public boolean isUsedAsServiceDefault() {
        return usedAsServiceDefault;
    }

    public void setUsedAsServiceDefault(boolean usedAsServiceDefault) {
        this.usedAsServiceDefault = usedAsServiceDefault;
    }

}
