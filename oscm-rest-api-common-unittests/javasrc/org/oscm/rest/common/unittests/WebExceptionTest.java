/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Aug 12, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.oscm.rest.common.WebException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author miethaner
 *
 */
public class WebExceptionTest {

    @Test
    public void testStatus() {

        assertEquals(Status.BAD_REQUEST.getStatusCode(),
                WebException.badRequest().build().getResponse().getStatus());
        assertEquals(Status.CONFLICT.getStatusCode(),
                WebException.conflict().build().getResponse().getStatus());
        assertEquals(Status.FORBIDDEN.getStatusCode(),
                WebException.forbidden().build().getResponse().getStatus());
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), WebException
                .internalServerError().build().getResponse().getStatus());
        assertEquals(Status.NOT_FOUND.getStatusCode(),
                WebException.notFound().build().getResponse().getStatus());
        assertEquals(Status.UNAUTHORIZED.getStatusCode(),
                WebException.unauthorized().build().getResponse().getStatus());
        assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
                WebException.unavailable().build().getResponse().getStatus());
    }

    @Test
    public void testFields() {

        WebApplicationException e = WebException.badRequest().error(1)
                .property("prop").message("message").moreInfo("moreInfo")
                .build();

        assertNotNull(e.getResponse());
        assertNotNull(e.getResponse().getEntity());

        Gson gson = new Gson();

        String entity = gson.toJson(e.getResponse().getEntity());

        JsonObject obj = gson.fromJson(entity, JsonObject.class);

        JsonElement element = obj.get("code");
        assertNotNull(element);
        assertEquals(400, element.getAsInt());

        element = obj.get("error");
        assertNotNull(element);
        assertEquals(1, element.getAsInt());

        element = obj.get("property");
        assertNotNull(element);
        assertEquals("prop", element.getAsString());

        element = obj.get("message");
        assertNotNull(element);
        assertEquals("message", element.getAsString());

        element = obj.get("moreInfo");
        assertNotNull(element);
        assertEquals("moreInfo", element.getAsString());
    }

}
