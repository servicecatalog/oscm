/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 18, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.oscm.rest.common.GsonMessageProvider;
import org.oscm.rest.common.Representation;

import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

/**
 * Unit test for GsonMessageProvider
 * 
 * @author miethaner
 */
public class GsonMessageProviderTest {

    private static final long ID = 1L;
    private static final String OLD_ID = "old";
    private static final String NEW_ID = "new";
    private static final String JSON = "{\"id\": \"" + ID + "\", \"id1\": \""
            + OLD_ID + "\"}";
    private static final int V1 = 1;
    private static final int V2 = 2;

    @SuppressWarnings("unused")
    private class MockRepresentation extends Representation {
        @Until(V2)
        private String id1;
        @Since(V2)
        private String id2;

        public String getId1() {
            return id1;
        }

        public void setId1(String id1) {
            this.id1 = id1;
        }

        public String getId2() {
            return id2;
        }

        public void setId2(String id2) {
            this.id2 = id2;
        }

        @Override
        public void validateContent() throws WebApplicationException {
        }

        @Override
        public void update() {
        }

        @Override
        public void convert() {
        }
    }

    @Test
    public void testJSONToRepresentationPositive() {
        GsonMessageProvider provider = new GsonMessageProvider();

        InputStream stream = new ByteArrayInputStream(JSON.getBytes());

        MockRepresentation rep = null;
        try {
            rep = (MockRepresentation) provider.readFrom(Representation.class,
                    MockRepresentation.class,
                    MockRepresentation.class.getAnnotations(),
                    MediaType.APPLICATION_JSON_TYPE, null, stream);
        } catch (WebApplicationException | IOException e) {
            fail();
            return;
        }

        assertEquals(new Long(ID), rep.getId());
        assertEquals(OLD_ID, rep.getId1());
    }

    @Test
    public void testJSONToRepresentationNegative() {
        GsonMessageProvider provider = new GsonMessageProvider();

        InputStream stream = new ByteArrayInputStream("$%&/".getBytes());

        try {
            provider.readFrom(Representation.class, MockRepresentation.class,
                    MockRepresentation.class.getAnnotations(),
                    MediaType.APPLICATION_JSON_TYPE, null, stream);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(),
                    e.getResponse().getStatus());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testRepresentationToJSON() {
        GsonMessageProvider provider = new GsonMessageProvider();

        MockRepresentation rep = new MockRepresentation();
        rep.setVersion(new Integer(V1));
        rep.setId1(OLD_ID);
        rep.setId2(NEW_ID);

        OutputStream stream = new ByteArrayOutputStream();

        try {
            provider.writeTo(rep, Representation.class, rep.getClass(),
                    rep.getClass().getAnnotations(),
                    MediaType.APPLICATION_JSON_TYPE, null, stream);
        } catch (WebApplicationException | IOException e) {
            fail();
        }

        String json = stream.toString();

        assertTrue(json.contains(OLD_ID));
        assertFalse(json.contains(NEW_ID));

        rep.setVersion(new Integer(V2));

        stream = new ByteArrayOutputStream();

        try {
            provider.writeTo(rep, Representation.class, rep.getClass(),
                    rep.getClass().getAnnotations(),
                    MediaType.APPLICATION_JSON_TYPE, null, stream);
        } catch (WebApplicationException | IOException e) {
            fail();
        }

        json = stream.toString();

        assertTrue(json.contains(NEW_ID));
        assertFalse(json.contains(OLD_ID));
    }

}
