/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Nov 2, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.beans.XMLDecoder;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author kulle
 * 
 */
public class MigrateTriggersTest {

    MigrateTriggers dbtask;

    @Before
    public void setup() {
        dbtask = spy(new MigrateTriggers());
    }

    @Test
    public void getObjectFromXML_closeStream() throws Exception {
        // given
        ResultSet rs = mock(ResultSet.class);
        InputStream is = mock(InputStream.class);
        doReturn(is).when(rs).getBinaryStream(eq("value"));
        XMLDecoder decoder = mock(XMLDecoder.class);
        doReturn(decoder).when(dbtask).newXmlDecoder(any(InputStream.class));

        // when
        dbtask.getObjectFromXML(rs, String.class);

        // then
        verify(dbtask).closeStream(is);
    }

    @Test
    public void getObjectFromXML_closeStream_sqlException() throws Exception {
        // given
        ResultSet rs = mock(ResultSet.class);
        doThrow(new SQLException()).when(rs).getBinaryStream(eq("value"));

        // when
        try {
            dbtask.getObjectFromXML(rs, String.class);
            fail();
        } catch (SQLException e) {

            // then
            assertNotNull(e);
            verify(dbtask).closeStream(any(InputStream.class));
        }

    }

}
