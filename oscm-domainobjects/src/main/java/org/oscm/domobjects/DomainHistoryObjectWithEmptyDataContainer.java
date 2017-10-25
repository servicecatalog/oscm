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
 * DomainHistoryObjectWithEmptyDataContainer is the base class of all history
 * objects with an empty data container.
 * 
 * @author pock
 */
public abstract class DomainHistoryObjectWithEmptyDataContainer extends
        DomainHistoryObject<EmptyDataContainer> {

    private static final long serialVersionUID = 1L;

    public DomainHistoryObjectWithEmptyDataContainer() {
    }

    public DomainHistoryObjectWithEmptyDataContainer(
            DomainObjectWithHistory<EmptyDataContainer> domobj) {
        super(domobj);
    }

    public EmptyDataContainer getDataContainer() {
        return new EmptyDataContainer();
    }

}
