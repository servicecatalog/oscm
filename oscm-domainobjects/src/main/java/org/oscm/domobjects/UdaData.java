/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                            
 *                                                                              
 *  Creation Date: 13.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The data container for a uda instance - holds the information about the value
 * and the key of the object the instance is attached on.
 * 
 * @author weiser
 * 
 */
@Embeddable
public class UdaData extends DomainDataContainer {

    private static final long serialVersionUID = 6763669079651899136L;

    @Column(nullable = false)
    private long targetObjectKey;

    private String udaValue;

    public long getTargetObjectKey() {
        return targetObjectKey;
    }

    public void setTargetObjectKey(long targetObjectKey) {
        this.targetObjectKey = targetObjectKey;
    }

    public String getUdaValue() {
        return udaValue;
    }

    public void setUdaValue(String udaValue) {
        this.udaValue = udaValue;
    }
}
