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

import java.io.Serializable;

import javax.persistence.*;

import org.oscm.domobjects.converters.STConverter;
import org.oscm.domobjects.converters.ServiceSConverter;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * DataContainer for domain object Product
 * 
 * @author schmid
 */
@Embeddable
public class ProductData extends DomainDataContainer implements Serializable {

    private static final long serialVersionUID = -2142477886431601646L;

    /**
     * Short name uniquely identifying the Product in the whole platform
     * (Business key)
     */
    @Column(nullable = false)
    private String productId;

    /**
     * Date of provisioning of the product
     */
    @Column(nullable = false)
    private long provisioningDate;

    /**
     * Date of deprovisioning of the product. Will be <code>null</code> as long
     * as the Product is active.
     */
    private Long deprovisioningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType type;

    /**
     * Flag indicating if auto-assign users when subscribing service.
     */
    @Column(nullable = true)
    private Boolean autoAssignUserEnabled;

    /**
     * URL of the external configuration tool.
     */
    @Column(nullable = true)
    private String configuratorUrl;

    /**
     * URL of the custom tab on my subscriptions page
     */
    @Column(nullable = true)
    private String customTabUrl;

    public String getConfiguratorUrl() {
        return configuratorUrl;
    }

    public void setConfiguratorUrl(String configuratorUrl) {
        this.configuratorUrl = configuratorUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * If the product id contains a '#' the part before will be returned.
     */
    public String getCleanProductId() {
        if (getProductId() == null) {
            return null;
        }
        String id = getProductId();
        String[] split = id.split("#");
        if (split.length == 0) {
            return "";
        }
        return split[0].trim();
    }

    public long getProvisioningDate() {
        return provisioningDate;
    }

    public void setProvisioningDate(long provisioningDate) {
        this.provisioningDate = provisioningDate;
    }

    public Long getDeprovisioningDate() {
        return deprovisioningDate;
    }

    public void setDeprovisioningDate(Long deprovisioningDate) {
        this.deprovisioningDate = deprovisioningDate;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public Boolean isAutoAssignUserEnabled() {
        return autoAssignUserEnabled;
    }

    public void setAutoAssignUserEnabled(Boolean autoAssignUserEnabled) {
        this.autoAssignUserEnabled = autoAssignUserEnabled;
    }

    public String getCustomTabUrl() {
        return customTabUrl;
    }

    public void setCustomTabUrl(String customTabUrl) {
        this.customTabUrl = customTabUrl;
    }
}
