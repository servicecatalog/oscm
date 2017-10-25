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

import javax.persistence.Embeddable;

/**
 * DomainDataContainer serves as basic type for all data containers used by
 * domain objects.
 * 
 * @author schmid
 */
@Embeddable
public class DomainDataContainer implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
