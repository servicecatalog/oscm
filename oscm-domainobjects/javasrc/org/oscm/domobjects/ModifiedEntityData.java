/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-12-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.METConverter;
import org.oscm.domobjects.enums.ModifiedEntityType;

/**
 * Data container for the domain object <code>ModifiedEntity</code>.
 * 
 * @author Qiu
 */
@Embeddable
public class ModifiedEntityData extends DomainDataContainer {

    private static final long serialVersionUID = -2287436301012462948L;
    @Column(nullable = false)
    private long targetObjectKey;

    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModifiedEntityType targetObjectType;

    public long getTargetObjectKey() {
        return targetObjectKey;
    }

    public void setTargetObjectKey(long targetObjectKey) {
        this.targetObjectKey = targetObjectKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ModifiedEntityType getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(ModifiedEntityType targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

}
