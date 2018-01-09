/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.oscm.domobjects.converters.TPINConverter;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;

/**
 * JPA managed entity representing the trigger process parameter identifier
 * data.
 * 
 * @author barzu
 */
public class TriggerProcessIdentifierData extends DomainDataContainer {

    private static final long serialVersionUID = 4222357485919114514L;

    /**
     * The name of the parameter identifier.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerProcessIdentifierName name;

    /**
     * The value of the parameter identifier.
     */
    @Column(nullable = false)
    private String value;

    public TriggerProcessIdentifierName getName() {
        return name;
    }

    public void setName(TriggerProcessIdentifierName name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
