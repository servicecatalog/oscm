/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: badziakp
 *
 *  Creation Date: 29.08.2016
 *
 *  Completion Time:
 *
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class TenantData extends DomainDataContainer implements
    Serializable {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 4658562180120496892L;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String description;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
