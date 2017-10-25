/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author kulle
 * 
 */
public class DataSet {

    static class DataRow {
        List<Object> values = new ArrayList<Object>();

        public DataRow(List<Object> values) {
            this.values = values;
        }

        public Object getObject(int columnIndex) {
            return values.get(columnIndex - 1);
        }
    }

    public static class MetaData {
        List<ColumnMetaData> columns = new ArrayList<ColumnMetaData>();

        public int getColumnCount() {
            return columns.size();
        }

        public String getColumnName(int columnIndex) {
            return columns.get(columnIndex - 1).columnName;
        }

        public String getColumnTypeName(int columnIndex) {
            return columns.get(columnIndex - 1).columnTypeName;
        }

        public int getColumnType(int columnIndex) {
            return columns.get(columnIndex - 1).columnType;
        }

        int columnIndex(String columnName) {
            for (ColumnMetaData column : columns) {
                if (column.columnName.equals(columnName)) {
                    return column.columnIndex;
                }
            }
            throw new IllegalArgumentException(columnName);
        }

        public void add(int index, String columnName, String columnTypeName,
                int columnType) {
            ColumnMetaData column = new ColumnMetaData();
            column.columnIndex = index;
            column.columnName = columnName;
            column.columnTypeName = columnTypeName;
            column.columnType = columnType;
            columns.add(column);
        }

    }

    static class ColumnMetaData {
        int columnIndex;
        String columnName;
        String columnTypeName;
        int columnType;
    }

    Iterator<DataRow> rowIterator;
    DataRow currentRow;
    List<DataRow> rows = new ArrayList<DataRow>();
    MetaData metaData = new MetaData();

    public boolean next() {
        if (rowIterator == null) {
            rowIterator = rows.iterator();
        }

        if (rowIterator.hasNext()) {
            currentRow = rowIterator.next();
            return true;
        } else {
            currentRow = null;
            return false;
        }
    }

    public BigDecimal getBigDecimal(int columnIndex) {
        return ((BigDecimal) currentRow.getObject(columnIndex));
    }

    public long getLong(int columnIndex) {
        return ((Long) currentRow.getObject(columnIndex)).longValue();
    }

    public long getLong(String columnName) {
        return getLong(metaData.columnIndex(columnName));
    }

    public String getString(int columnIndex) {
        return ((String) currentRow.getObject(columnIndex));
    }

    public String getString(String columnName) {
        return getString(metaData.columnIndex(columnName));
    }

    public Object getObject(int columnIndex) {
        return currentRow.getObject(columnIndex);
    }

    public Object getObject(String columnName) {
        return getObject(metaData.columnIndex(columnName));
    }

    public Date getDate(int columnIndex) {
        return (Date) currentRow.getObject(columnIndex);
    }

    public Date getDate(String columnName) {
        return getDate(metaData.columnIndex(columnName));
    }

    public void addRow(List<Object> values) {
        rows.add(new DataRow(values));
    }

    public MetaData getMetaData() {
        return metaData;
    }

}
