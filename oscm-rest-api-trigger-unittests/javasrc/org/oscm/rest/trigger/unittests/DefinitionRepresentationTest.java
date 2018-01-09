/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 10, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.oscm.rest.trigger.data.DefinitionRepresentation;

/**
 * Unit test for TriggerRepresentation
 * 
 * @author miethaner
 */
public class DefinitionRepresentationTest {

    @Test
    public void testValidationPositive() throws Exception {

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setTargetURL("http://abc.de/asdf");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        DefinitionRepresentation.Owner owner = new DefinitionRepresentation.Owner();
        owner.setId(1L);
        owner.setName("abc");
        trigger.setOwner(owner);
        trigger.setOwnerId(1L);

        trigger.validateContent();
        assertEquals(1L, trigger.getOwnerId().intValue());
        assertEquals(1L, trigger.getOwner().getId().intValue());

    }

    @Test
    public void testValidationNegativeDescription() throws Exception {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            sb.append("1234567890");
        }

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setDescription(sb.toString());
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setTargetURL("http://abc.de/asdf");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }

        trigger = new DefinitionRepresentation();
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setTargetURL("http://abc.de/asdf");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }

    @Test
    public void testValidationNegativeURL() throws Exception {

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setTargetURL("<http://");
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }

        trigger = new DefinitionRepresentation();
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }

    @Test
    public void testValidationNegativeSuspend() throws Exception {

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setAction("SUBSCRIBE_TO_SERVICE");
        trigger.setTargetURL("<http://");
        trigger.setDescription("abc");
        trigger.setType("REST_SERVICE");
        trigger.setAction("SUBSCRIBE_TO_SERVICE");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }

    @Test
    public void testValidationNegativeAction() throws Exception {

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setAction("SUB");
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setTargetURL("http://abc.de/asdf");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }

        trigger = new DefinitionRepresentation();
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_SERVICE");
        trigger.setTargetURL("http://abc.de/asdf");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }

    @Test
    public void testValidationNegativeType() throws Exception {

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setAction("SUBSCRIBE_TO_SERVICE");
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setType("REST_");
        trigger.setTargetURL("http://abc.de/asdf");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }

        trigger = new DefinitionRepresentation();
        trigger.setAction("SUBSCRIBE_TO_SERVICE");
        trigger.setDescription("abc");
        trigger.setSuspending(Boolean.TRUE);
        trigger.setTargetURL("http://abc.de/asdf");

        try {
            trigger.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }

}
