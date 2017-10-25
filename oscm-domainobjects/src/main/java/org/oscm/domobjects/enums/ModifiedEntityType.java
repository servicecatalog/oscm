/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-4                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.enums;

import java.util.HashSet;
import java.util.Set;

/**
 * ModifiedEntityType indicates the type of ModifiedEntity, corresponding a
 * specified entity.
 * 
 * @author Zhou
 */
public enum ModifiedEntityType {

    // Store uda value for subscription into ModifiedEntity when it has been
    // asynchronous updated.
    UDA_VALUE,

    // Store subscriptionId of subscription into ModifiedEntity when it has
    // been asynchronous updated.
    SUBSCRIPTION_SUBSCRIPTIONID,

    // Store organizationId of subscription into ModifiedEntity when it has
    // been asynchronous updated.
    SUBSCRIPTION_ORGANIZATIONID,

    // Store purchaseOrderNumber of subscription into ModifiedEntity when it
    // has been asynchronous updated.
    SUBSCRIPTION_PURCHASEORDERNUMBER,

    // Store ownerId of subscription into ModifiedEntity when it has been
    // asynchronous updated.
    SUBSCRIPTION_OWNERID,

    // Store paymentInfo key of subscription into ModifiedEntity when it has
    // been asynchronous updated.
    SUBSCRIPTION_PAYMENTINFO,

    // Store billingContact key of subscription into ModifiedEntity when it has
    // been asynchronous updated.
    SUBSCRIPTION_BILLINGCONTACT,

    // Store unit name of a unit the subscription is assigned to into
    // ModifiedEntity when it has been asynchronous updated.
    SUBSCRIPTION_UNIT;

    public enum TargetEntity {
        UDA, SUBSCRIPTION
    }

    private TargetEntity targetEntity;

    private ModifiedEntityType() {
        String[] s = name().split("_");
        this.targetEntity = TargetEntity.valueOf(s[0].substring(0,
                s[0].length()));
    }

    public TargetEntity getTargetEntity() {
        return targetEntity;
    }

    public static Set<ModifiedEntityType> getModifiedEntityTypes(TargetEntity en) {
        Set<ModifiedEntityType> set = new HashSet<ModifiedEntityType>();
        for (ModifiedEntityType enType : ModifiedEntityType.values()) {
            if (enType.getTargetEntity() == en) {
                set.add(enType);
            }
        }
        return set;
    }
}
