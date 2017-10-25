/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.UUID;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.converters.ETConverter;
import org.oscm.domobjects.converters.LBRTConverter;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;

/**
 * @author iversen
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "LocalizedBillingResource.findByBusinessKey", query = "SELECT lbr FROM LocalizedBillingResource lbr WHERE lbr.objectId = :objectId AND lbr.locale = :locale AND lbr.resourceType = :resourceType"),
        @NamedQuery(name = "LocalizedBillingResource.findPriceModelByBusinessKey", query = "SELECT lbr FROM LocalizedBillingResource lbr WHERE lbr.objectId = :objectId AND lbr.locale = :locale AND lbr.resourceType IN (org.oscm.domobjects.enums.LocalizedBillingResourceType.PRICEMODEL_SERVICE, org.oscm.domobjects.enums.LocalizedBillingResourceType.PRICEMODEL_CUSTOMER, org.oscm.domobjects.enums.LocalizedBillingResourceType.PRICEMODEL_SUBSCRIPTION)") })
@BusinessKey(attributes = { "objectId", "locale", "resourceType" })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "objectId",
        "locale", "resourceType" }))
public class LocalizedBillingResource extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = -3989498822730347942L;

    @Type(type = "uuid-char")
    private UUID objectId;

    private String locale;

    @Enumerated(EnumType.STRING)
    private LocalizedBillingResourceType resourceType;

    @Column(nullable = false)
    private String dataType;

    @Column(nullable = false)
    private byte[] value;

    public LocalizedBillingResource() {
        super();
    }

    public LocalizedBillingResource(UUID adapterObjectId, String locale,
            LocalizedBillingResourceType resourceType) {
        super();
        this.objectId = adapterObjectId;
        this.locale = locale;
        this.resourceType = resourceType;
    }

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public LocalizedBillingResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(LocalizedBillingResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

}