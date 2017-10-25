/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                 
 *                                                                              
 *  Creation Date: 21.01.2011                                                      
 *                                                                              
 *  Completion Time: 21.01.2011                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Base class for the domain objects that consider versioning and thus
 * optimistic locking. If a concrete domain object does not support versioning,
 * there must be very good reasons for it. Almost always this class should be
 * used as base class.
 * 
 * @author Mike J&auml;ger
 * 
 */
@MappedSuperclass
public abstract class DomainObjectWithVersioning<D extends DomainDataContainer>
        extends DomainObject<D> {

    private static final long serialVersionUID = -974587508514289663L;

    /**
     * Version number for optimistic locking
     */
    @Version
    @Column(nullable = false)
    private int version;

    public int getVersion() {
        return version;
    }

}
