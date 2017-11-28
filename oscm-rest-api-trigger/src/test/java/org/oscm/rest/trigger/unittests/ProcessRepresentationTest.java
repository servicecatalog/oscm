/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
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
import org.oscm.rest.trigger.data.ProcessRepresentation;

/**
 * Unit test for ProcessRepresentation
 * 
 * @author miethaner
 */
public class ProcessRepresentationTest {

    @Test
    public void testValidationPositive() throws Exception {

        ProcessRepresentation process = new ProcessRepresentation();
        process.setComment("abc");

        process.validateContent();
    }

    @Test
    public void testValidationNegative() throws Exception {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            sb.append("1234567890");
        }

        ProcessRepresentation process = new ProcessRepresentation();
        process.setComment(sb.toString());

        try {
            process.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }

        process = new ProcessRepresentation();

        try {
            process.validateContent();
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse()
                    .getStatus());
        }
    }
}
