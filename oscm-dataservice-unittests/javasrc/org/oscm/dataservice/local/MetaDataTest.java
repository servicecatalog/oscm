/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import static org.junit.Assert.assertEquals;

import java.sql.Types;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DataSet.MetaData}
 * 
 * @author barzu
 */
public class MetaDataTest {

    private DataSet data;

    @Before
    public void setup() {
        data = new DataSet();
        data.getMetaData().add(1, "strings", "VARCHAR", Types.VARCHAR);
        data.addRow(Arrays.asList(new Object[] { "a" }));
        data.getMetaData().add(2, "dates", "DATE", Types.DATE);
        data.addRow(Arrays.asList(new Object[] { new Date() }));
    }

    @Test
    public void columnIndex() {
        assertEquals(1, data.getMetaData().columnIndex("strings"));
        assertEquals(2, data.getMetaData().columnIndex("dates"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void columnIndex_inexistentColumn() {
        data.getMetaData().columnIndex("inexistentColumn");
    }

    @Test
    public void getColumnCount() {
        assertEquals(2, data.getMetaData().getColumnCount());
    }

    @Test
    public void getColumnName() {
        assertEquals("strings", data.getMetaData().getColumnName(1));
        assertEquals("dates", data.getMetaData().getColumnName(2));
    }

    @Test
    public void getColumnTypeName() {
        assertEquals("VARCHAR", data.getMetaData().getColumnTypeName(1));
        assertEquals("DATE", data.getMetaData().getColumnTypeName(2));
    }

    @Test
    public void getColumnType() {
        assertEquals(Types.VARCHAR, data.getMetaData().getColumnType(1));
        assertEquals(Types.DATE, data.getMetaData().getColumnType(2));
    }

}
