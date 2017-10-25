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

/**
 * DomainObjectWithHistory is the base class for all domain objects with a
 * history which is automatically stored in a special history table.
 * 
 * @author schmid
 * 
 */
public abstract class DomainObjectWithHistory<D extends DomainDataContainer>
        extends DomainObjectWithVersioning<D> {

    private static final long serialVersionUID = 7478027746157365276L;

    /**
     * All subclasses have a history.
     */
    public boolean hasHistory() {
        return true;
    }

}
