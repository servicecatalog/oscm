/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 30.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paginator;

/**
 * Sort order ascending, descending, unsorted
 */
public enum SortOrder {

    ASC, DESC, UNSORTED;

    public boolean isSorted() {
        return !UNSORTED.equals(this);
    }
}
