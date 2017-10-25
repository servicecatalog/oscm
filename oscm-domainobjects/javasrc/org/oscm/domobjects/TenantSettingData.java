/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: Paulina Badziak
 *
 *  Creation Date: 30.08.2016
 *
 *  Completion Time: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.domobjects;

import org.oscm.domobjects.converters.IdpSettingTypeConverter;
import org.oscm.internal.types.enumtypes.IdpSettingType;

import javax.persistence.*;

@Embeddable
public class TenantSettingData extends DomainDataContainer {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 6900999015801280393L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdpSettingType name;

    private String value;

    public IdpSettingType getName() {
        return name;
    }

    public void setName(IdpSettingType name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
