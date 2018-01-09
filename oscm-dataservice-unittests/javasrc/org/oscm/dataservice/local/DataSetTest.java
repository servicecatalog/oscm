/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class DataSetTest {

    private DataSet data;

    @Before
    public void setup() {
        data = new DataSet();
    }

    @Test
    public void getMetaData() {
        assertNotNull(data.getMetaData());
    }

    @Test
    public void next() {
        // given
        data.addRow(Arrays.asList(new Object[] { "a" }));

        // then
        assertTrue(data.next());
    }

    @Test
    public void next_Empty() {
        assertFalse(data.next());
    }

    @Test
    public void getString_byColumnIndex() {
        // given
        data.addRow(Arrays.asList(new Object[] { "a" }));
        data.next();

        // then
        assertEquals("a", data.getString(1));
    }

    @Test
    public void getString_byColumnName() {
        // given
        data.getMetaData().add(1, "strings", "VARCHAR", Types.VARCHAR);
        data.addRow(Arrays.asList(new Object[] { "a" }));
        data.next();

        // then
        assertEquals("a", data.getString("strings"));
    }

    @Test
    public void getLong_byColumnIndex() {
        // given
        data.addRow(Arrays.asList(new Object[] { Long.valueOf(1L) }));
        data.next();

        // then
        assertEquals(1L, data.getLong(1));
    }

    @Test
    public void getLong_byColumnName() {
        // given
        data.getMetaData().add(1, "longs", "NUMERIC", Types.NUMERIC);
        data.addRow(Arrays.asList(new Object[] { Long.valueOf(1L) }));
        data.next();

        // then
        assertEquals(1L, data.getLong("longs"));
    }

    @Test
    public void getDate_byColumnIndex() {
        // given
        Date date = new Date();
        data.addRow(Arrays.asList(new Object[] { date }));
        data.next();

        // then
        assertEquals(date, data.getDate(1));
    }

    @Test
    public void getDate_byColumnName() {
        // given
        Date date = new Date();
        data.getMetaData().add(1, "longs", "DATE", Types.DATE);
        data.addRow(Arrays.asList(new Object[] { date }));
        data.next();

        // then
        assertEquals(date, data.getDate("longs"));
    }

    @Test
    public void getObject_byColumnIndex() {
        // given
        Object object = new Object();
        data.addRow(Arrays.asList(new Object[] { object }));
        data.next();

        // then
        assertEquals(object, data.getObject(1));
    }

    @Test
    public void getObject_byColumnName() {
        // given
        Object object = new Object();
        data.getMetaData().add(1, "longs", "BLOB", Types.BLOB);
        data.addRow(Arrays.asList(new Object[] { object }));
        data.next();

        // then
        assertEquals(object, data.getObject("longs"));
    }

}
