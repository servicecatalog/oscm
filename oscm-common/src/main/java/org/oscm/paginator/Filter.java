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
 * This class describes the filter of the table. Based on column name and filter
 * expression filters data.
 */
public class Filter implements Serializable {

    private static final long serialVersionUID = 1699591001139459732L;
    private TableColumns column;
    private String expression;
    static final String EMPTY = "";

    public Filter(TableColumns column, String expression) {
        this.column = column;
        this.expression = expression;
    }

    public TableColumns getColumn() {
        return column;
    }

    public void setColumn(TableColumns column) {
        this.column = column;
    }

    public String getExpression() {
        return expression == null ? EMPTY : expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}
