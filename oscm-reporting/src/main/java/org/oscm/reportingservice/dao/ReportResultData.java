/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kulle
 * 
 */
public class ReportResultData {

    private int columnCount;
    private List<Integer> columnType = new ArrayList<Integer>();
    private List<String> columnName = new ArrayList<String>();
    private List<Object> columnValue = new ArrayList<Object>();

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public List<Integer> getColumnType() {
        return columnType;
    }

    public void setColumnType(List<Integer> columnType) {
        this.columnType = columnType;
    }

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }

    public List<Object> getColumnValue() {
        return columnValue;
    }

    public void setColumnValue(List<Object> columnValue) {
        this.columnValue = columnValue;
    }

}
