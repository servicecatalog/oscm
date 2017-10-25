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
 * DomainObjectWithEmptyDataContainer is the base class for all domain objects
 * with a history table but with an empty data container.
 * 
 * @author pock
 * 
 */
public abstract class DomainObjectWithEmptyDataContainer extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = 2526059227931738025L;

    public EmptyDataContainer getDataContainer() {
        return new EmptyDataContainer();
    }

}
