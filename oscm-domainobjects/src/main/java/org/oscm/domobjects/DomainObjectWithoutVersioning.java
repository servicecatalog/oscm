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

/**
 * Base class for the domain objects that don't consider versioning and do not
 * take care of concurrent modification control. If a concrete domain object
 * extends this class, there must be very good reasons for it. Refer to class
 * DomainObjectWithVersioning instead.
 * 
 * <p>
 * The object supports historization nevertheless. The version attribute will
 * remain 0.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@MappedSuperclass
public abstract class DomainObjectWithoutVersioning<D extends DomainDataContainer>
        extends DomainObject<D> {

    private static final long serialVersionUID = -974587508514289663L;

    /**
     * Version number to satisfy domain object definition.
     */
    @Column(nullable = false)
    private int version;

    public int getVersion() {
        return version;
    }

    /**
     * All subclasses have a history.
     */
    public boolean hasHistory() {
        return true;
    }

}
