/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link DataSet.DataRow}
 * 
 * @author barzu
 */
public class DataRowTest {

    @Test
    public void dataRow() {
        // given
        List<Object> objects = Arrays.asList((Object) "a");

        // when
        DataSet.DataRow row = new DataSet.DataRow(objects);

        // then
        assertEquals(objects, row.values);
    }

    @Test
    public void getObject() {
        // given
        DataSet.DataRow row = new DataSet.DataRow(Arrays.asList((Object) "a"));

        // when
        Object object = row.getObject(1);

        // then
        assertEquals("a", object);
    }

}
