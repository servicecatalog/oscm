/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paginator;

import java.io.Serializable;

/**
 * This class describes the sorting of a table, which column and which sorting
 * order.
 * 
 */
public class Sorting implements Serializable {

    private TableColumns column;
    private SortOrder order = SortOrder.UNSORTED;

    public Sorting(TableColumns column, SortOrder order) {
        this.column = column;
        this.order = order;
    }

    public TableColumns getColumn() {
        return column;
    }

    public void setColumn(TableColumns column) {
        this.column = column;
    }

    public SortOrder getOrder() {
        return order;
    }

    public void setOrder(SortOrder order) {
        this.order = order;
    }

}
