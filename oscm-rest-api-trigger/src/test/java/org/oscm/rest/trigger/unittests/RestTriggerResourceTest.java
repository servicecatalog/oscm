/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 10, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.unittests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.oscm.rest.common.CommonParams.PARAM_VERSION;
import static org.oscm.rest.common.CommonParams.VERSION_1;

import javax.ws.rs.core.*;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.rest.common.RepresentationCollection;
import org.oscm.rest.common.RequestParameters;
import org.oscm.rest.trigger.DefinitionBackend;
import org.oscm.rest.trigger.ProcessBackend;
import org.oscm.rest.trigger.RestTriggerResource;
import org.oscm.rest.trigger.data.DefinitionRepresentation;
import org.oscm.rest.trigger.data.ProcessRepresentation;

/**
 * Unit test for RestTriggerResource
 * 
 * @author miethaner
 */
public class RestTriggerResourceTest {
    @Test
    public void testAction() throws Exception {
        RestTriggerResource.Action action = new RestTriggerResource()
                .redirectToAction();

        RequestParameters params = new RequestParameters();
        params.setId(1L);

        UriInfo uri = Mockito.mock(UriInfo.class);
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.putSingle(PARAM_VERSION, "v" + VERSION_1);
        when(uri.getPathParameters()).thenReturn(map);

        Response response = action.getCollection(uri, params);
        assertThat(response.getEntity(),
                IsInstanceOf.instanceOf(RepresentationCollection.class));

        assertNull(action.getItem(uri, params));
    }

    @Test
    public void testDefinition() throws Exception {

        RestTriggerResource resource = spy(new RestTriggerResource());

        RestTriggerResource.Definition definition = resource
                .redirectToTrigger();

        DefinitionBackend backend = spy(new DefinitionBackend());
        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);
        when(resource.getTriggerBackend()).thenReturn(backend);
        when(backend.getService()).thenReturn(service);

        RequestParameters params = new RequestParameters();
        params.setId(1L);
        params.setMatch("1");

        UriInfo uri = Mockito.mock(UriInfo.class);
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.putSingle(PARAM_VERSION, "v" + VERSION_1);
        when(uri.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromPath(""));
        when(uri.getPathParameters()).thenReturn(map);

        Response response = definition.getCollection(uri, params);
        assertThat(response.getEntity(),
                IsInstanceOf.instanceOf(RepresentationCollection.class));

        response = definition.getItem(uri, params);
        assertThat(response.getEntity(),
                IsInstanceOf.instanceOf(DefinitionRepresentation.class));

        DefinitionRepresentation content = new DefinitionRepresentation();
        content.setId(1L);
        content.setETag(1L);
        content.setDescription("abc");
        content.setSuspending(Boolean.TRUE);
        content.setType("REST_SERVICE");
        content.setTargetURL("http://abc.de/asdf");
        content.setAction("SUBSCRIBE_TO_SERVICE");

        response = definition.postCollection(uri, content, params);
        assertEquals(Response.Status.CREATED.getStatusCode(),
                response.getStatus());

        response = definition.putItem(uri, content, params);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(),
                response.getStatus());
    }

    @Test
    public void testProcess() throws Exception {
        RestTriggerResource resource = spy(new RestTriggerResource());
        RestTriggerResource.Process process = resource.redirectToProcess();

        ProcessBackend backend = spy(new ProcessBackend());
        TriggerService service = Mockito.mock(TriggerService.class);
        when(backend.getService()).thenReturn(service);
        when(resource.getProcessBackend()).thenReturn(backend);

        RequestParameters params = new RequestParameters();
        params.setId(1L);

        UriInfo uri = Mockito.mock(UriInfo.class);
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        map.putSingle(PARAM_VERSION, "v" + VERSION_1);
        when(uri.getPathParameters()).thenReturn(map);

        ProcessRepresentation content = new ProcessRepresentation();
        content.setComment("abc");

        Response response = process.putApprove(uri, params);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(),
                response.getStatus());

        response = process.putReject(uri, content, params);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(),
                response.getStatus());
    }

}
