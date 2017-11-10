/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.stubs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Mike J&auml;ger
 * 
 */
public class ResultSetStub implements ResultSet {

    private ResultSetMetaData rsmd;
    // one 'objects' array per stubbed statement
    private List<List<Object[]>> resultSets = new ArrayList<List<Object[]>>();
    private int position = -1;
    private File file;
    private List<String> columns = new ArrayList<String>();

    public ResultSetStub(String sql) {
        setSQLColumns(sql);
    }

    public File getFile() {
        return file;
    }

    public List<List<Object[]>> getResultSets() {
        return resultSets;
    }

    public void setSQLColumns(String sql) {
        if (sql != null && sql.indexOf(' ') > -1
                && sql.toLowerCase().indexOf(" from ") > -1) {
            sql = sql.substring(sql.indexOf(' '),
                    sql.toLowerCase().lastIndexOf(" from ")).trim();
            while (sql.indexOf('(') > 0 && sql.indexOf(')') > sql.indexOf('(')) {
                sql = sql.substring(0, sql.indexOf('('))
                        + sql.substring(sql.indexOf(')') + 1);
            }
            sql = sql.trim().toLowerCase();
            this.columns.clear();
            final String[] columns = sql.split(",");
            for (String column : columns) {
                if (column.indexOf(" as ") > -1) {
                    column = column.substring(column.lastIndexOf(" as ") + 4);
                }
                this.columns.add(column.trim());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#absolute(int)
     */
    @Override
    public boolean absolute(int row) throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#afterLast()
     */
    @Override
    public void afterLast() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#beforeFirst()
     */
    @Override
    public void beforeFirst() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#cancelRowUpdates()
     */
    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#clearWarnings()
     */
    @Override
    public void clearWarnings() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#close()
     */
    @Override
    public void close() throws SQLException {
        // remove the results of the corresponding statement
        position = -1;
        if (resultSets.size() > 0) {
            resultSets.remove(0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#deleteRow()
     */
    @Override
    public void deleteRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#findColumn(java.lang.String)
     */
    @Override
    public int findColumn(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#first()
     */
    @Override
    public boolean first() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getArray(int)
     */
    @Override
    public Array getArray(int i) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getArray(java.lang.String)
     */
    @Override
    public Array getArray(String colName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getAsciiStream(int)
     */
    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
     */
    @Override
    public InputStream getAsciiStream(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
     */
    @Override
    public BigDecimal getBigDecimal(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBigDecimal(int, int)
     */
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
     */
    @Override
    public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBinaryStream(int)
     */
    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new SQLException(e.getMessage());
            }
        }
        return new ByteArrayInputStream(((String) resultSets.get(0).get(
                position)[columnIndex - 1]).getBytes());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
     */
    @Override
    public InputStream getBinaryStream(String columnName) throws SQLException {
        return getBinaryStream(getColumnIndex(columnName));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBlob(int)
     */
    @Override
    public Blob getBlob(int i) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBlob(java.lang.String)
     */
    @Override
    public Blob getBlob(String colName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBoolean(int)
     */
    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String columnName) throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getByte(int)
     */
    @Override
    public byte getByte(int columnIndex) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getByte(java.lang.String)
     */
    @Override
    public byte getByte(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBytes(int)
     */
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getBytes(java.lang.String)
     */
    @Override
    public byte[] getBytes(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getCharacterStream(int)
     */
    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
     */
    @Override
    public Reader getCharacterStream(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getClob(int)
     */
    @Override
    public Clob getClob(int i) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getClob(java.lang.String)
     */
    @Override
    public Clob getClob(String colName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getConcurrency()
     */
    @Override
    public int getConcurrency() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getCursorName()
     */
    @Override
    public String getCursorName() throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDate(int)
     */
    @Override
    public Date getDate(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDate(java.lang.String)
     */
    @Override
    public Date getDate(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
     */
    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
     */
    @Override
    public Date getDate(String columnName, Calendar cal) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDouble(int)
     */
    @Override
    public double getDouble(int columnIndex) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getDouble(java.lang.String)
     */
    @Override
    public double getDouble(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getFetchDirection()
     */
    @Override
    public int getFetchDirection() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getFetchSize()
     */
    @Override
    public int getFetchSize() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getFloat(int)
     */
    @Override
    public float getFloat(int columnIndex) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getFloat(java.lang.String)
     */
    @Override
    public float getFloat(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getInt(int)
     */
    @Override
    public int getInt(int columnIndex) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getInt(java.lang.String)
     */
    @Override
    public int getInt(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getLong(int)
     */
    @Override
    public long getLong(int columnIndex) throws SQLException {
        return ((Long) resultSets.get(0).get(position)[columnIndex - 1])
                .longValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getLong(java.lang.String)
     */
    @Override
    public long getLong(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getMetaData()
     */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return rsmd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getObject(int)
     */
    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return resultSets.get(0).get(position)[columnIndex - 1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getObject(java.lang.String)
     */
    @Override
    public Object getObject(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getObject(int, java.util.Map)
     */
    @Override
    public Object getObject(int i, Map<String, Class<?>> map)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
     */
    @Override
    public Object getObject(String colName, Map<String, Class<?>> map)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getRef(int)
     */
    @Override
    public Ref getRef(int i) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getRef(java.lang.String)
     */
    @Override
    public Ref getRef(String colName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getRow()
     */
    @Override
    public int getRow() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getShort(int)
     */
    @Override
    public short getShort(int columnIndex) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getShort(java.lang.String)
     */
    @Override
    public short getShort(String columnName) throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getStatement()
     */
    @Override
    public Statement getStatement() throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getString(int)
     */
    @Override
    public String getString(int columnIndex) throws SQLException {
        return (String) resultSets.get(0).get(position)[columnIndex - 1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getString(java.lang.String)
     */
    @Override
    public String getString(String columnName) throws SQLException {
        return getString(getColumnIndex(columnName));
    }

    private int getColumnIndex(String column) {
        if (column == null) {
            return -1;
        }
        column = column.trim().toLowerCase();
        int i = columns.indexOf(column);
        if (i < 0) {
            if (column.indexOf('.') > -1) {
                i = columns.indexOf(column.substring(column.indexOf('.') + 1));
            } else {
                for (int i2 = 0; i2 < columns.size(); i2++) {
                    if (columns.get(i2).indexOf('.') > 0
                            && columns.get(i2).endsWith('.' + column)) {
                        i = i2;
                        break;
                    }
                }
            }
        }
        return i > -1 ? i + 1 : -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTime(int)
     */
    @Override
    public Time getTime(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTime(java.lang.String)
     */
    @Override
    public Time getTime(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
     */
    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
     */
    @Override
    public Time getTime(String columnName, Calendar cal) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTimestamp(int)
     */
    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTimestamp(java.lang.String)
     */
    @Override
    public Timestamp getTimestamp(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
     */
    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getTimestamp(java.lang.String,
     * java.util.Calendar)
     */
    @Override
    public Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getType()
     */
    @Override
    public int getType() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getURL(int)
     */
    @Override
    public URL getURL(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getURL(java.lang.String)
     */
    @Override
    public URL getURL(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getUnicodeStream(int)
     */
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
     */
    @Override
    public InputStream getUnicodeStream(String columnName) throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#getWarnings()
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#insertRow()
     */
    @Override
    public void insertRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#isAfterLast()
     */
    @Override
    public boolean isAfterLast() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#isBeforeFirst()
     */
    @Override
    public boolean isBeforeFirst() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#isFirst()
     */
    @Override
    public boolean isFirst() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#isLast()
     */
    @Override
    public boolean isLast() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#last()
     */
    @Override
    public boolean last() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#moveToCurrentRow()
     */
    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#moveToInsertRow()
     */
    @Override
    public void moveToInsertRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#next()
     */
    @Override
    public boolean next() throws SQLException {
        return resultSets.size() > 0 && ++position < resultSets.get(0).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#previous()
     */
    @Override
    public boolean previous() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#refreshRow()
     */
    @Override
    public void refreshRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#relative(int)
     */
    @Override
    public boolean relative(int rows) throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#rowDeleted()
     */
    @Override
    public boolean rowDeleted() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#rowInserted()
     */
    @Override
    public boolean rowInserted() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#rowUpdated()
     */
    @Override
    public boolean rowUpdated() throws SQLException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#setFetchDirection(int)
     */
    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#setFetchSize(int)
     */
    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
     */
    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
     */
    @Override
    public void updateArray(String columnName, Array x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
     */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
     * java.io.InputStream, int)
     */
    @Override
    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
     */
    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBigDecimal(java.lang.String,
     * java.math.BigDecimal)
     */
    @Override
    public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
     */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
     * java.io.InputStream, int)
     */
    @Override
    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
     */
    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
     */
    @Override
    public void updateBlob(String columnName, Blob x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBoolean(int, boolean)
     */
    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
     */
    @Override
    public void updateBoolean(String columnName, boolean x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateByte(int, byte)
     */
    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
     */
    @Override
    public void updateByte(String columnName, byte x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBytes(int, byte[])
     */
    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
     */
    @Override
    public void updateBytes(String columnName, byte[] x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
     */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
     * java.io.Reader, int)
     */
    @Override
    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
     */
    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
     */
    @Override
    public void updateClob(String columnName, Clob x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
     */
    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
     */
    @Override
    public void updateDate(String columnName, Date x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateDouble(int, double)
     */
    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
     */
    @Override
    public void updateDouble(String columnName, double x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateFloat(int, float)
     */
    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
     */
    @Override
    public void updateFloat(String columnName, float x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateInt(int, int)
     */
    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateInt(java.lang.String, int)
     */
    @Override
    public void updateInt(String columnName, int x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateLong(int, long)
     */
    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateLong(java.lang.String, long)
     */
    @Override
    public void updateLong(String columnName, long x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateNull(int)
     */
    @Override
    public void updateNull(int columnIndex) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateNull(java.lang.String)
     */
    @Override
    public void updateNull(String columnName) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
     */
    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
     */
    @Override
    public void updateObject(String columnName, Object x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
     */
    @Override
    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object,
     * int)
     */
    @Override
    public void updateObject(String columnName, Object x, int scale)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
     */
    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
     */
    @Override
    public void updateRef(String columnName, Ref x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateRow()
     */
    @Override
    public void updateRow() throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateShort(int, short)
     */
    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateShort(java.lang.String, short)
     */
    @Override
    public void updateShort(String columnName, short x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateString(int, java.lang.String)
     */
    @Override
    public void updateString(int columnIndex, String x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
     */
    @Override
    public void updateString(String columnName, String x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
     */
    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
     */
    @Override
    public void updateTime(String columnName, Time x) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
     */
    @Override
    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#updateTimestamp(java.lang.String,
     * java.sql.Timestamp)
     */
    @Override
    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.ResultSet#wasNull()
     */
    @Override
    public boolean wasNull() throws SQLException {

        return false;
    }

    public void setResultSetMetaData(ResultSetMetaData rsmd) {
        this.rsmd = rsmd;
    }

    /**
     * value represents a whole result set which will be removed when close is
     * called. That way several resultsets may be preset, which will
     * sequentially be returned.
     * 
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void addResultSet(Object value) {
        if (value instanceof List) {
            resultSets.add((List<Object[]>) value);
        } else {
            final List<Object[]> list = new ArrayList<Object[]>();
            if (value instanceof Object[]) {
                list.add((Object[]) value);
            } else {
                list.add(new Object[] { value });
            }
            resultSets.add(list);
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {

        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {

        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {

        return false;
    }

    @Override
    public void updateNString(int columnIndex, String nString)
            throws SQLException {

    }

    @Override
    public void updateNString(String columnLabel, String nString)
            throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob)
            throws SQLException {

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject)
            throws SQLException {

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject)
            throws SQLException {

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {

        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {

        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length)
            throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length)
            throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length)
            throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length)
            throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x,
            long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader,
            long length) throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length)
            throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream,
            long length) throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length)
            throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length)
            throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length)
            throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length)
            throws SQLException {

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x)
            throws SQLException {

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader)
            throws SQLException {

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x)
            throws SQLException {

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x)
            throws SQLException {

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x)
            throws SQLException {

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x)
            throws SQLException {

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x)
            throws SQLException {

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader)
            throws SQLException {

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream)
            throws SQLException {

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream)
            throws SQLException {

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(String columnLabel, Reader reader)
            throws SQLException {

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader)
            throws SQLException {

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {

        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type)
            throws SQLException {

        return null;
    }
}
