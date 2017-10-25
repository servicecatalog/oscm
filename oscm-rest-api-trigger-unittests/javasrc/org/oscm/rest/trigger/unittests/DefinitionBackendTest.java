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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.rest.common.Representation;
import org.oscm.rest.common.RepresentationCollection;
import org.oscm.rest.common.RequestParameters;
import org.oscm.rest.trigger.DefinitionBackend;
import org.oscm.rest.trigger.data.DefinitionRepresentation;

/**
 * Unit test for TriggerBackend
 * 
 * @author miethaner
 */
public class DefinitionBackendTest {

    @Test
    public void testGetItem() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();
        params.setId(id);

        VOTriggerDefinition trigger = new VOTriggerDefinition();
        trigger.setKey(id);
        trigger.setOrganization(new VOOrganization());

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);

        when(service.getTriggerDefinition(params.getId())).thenReturn(trigger);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        Representation result = backend.getItem().get(params);

        assertEquals(id, result.getId());
        Mockito.verify(service).getTriggerDefinition(params.getId());

        when(service.getTriggerDefinition(params.getId()))
                .thenThrow(new ObjectNotFoundException());

        try {
            backend.getItem().get(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        params.setId(2L);

        when(service.getTriggerDefinition(params.getId()))
                .thenThrow(new OperationNotPermittedException());

        try {
            backend.getItem().get(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        params.setId(3L);

        when(service.getTriggerDefinition(params.getId()))
                .thenThrow(new javax.ejb.EJBAccessException());

        try {
            backend.getItem().get(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testGetCollection() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();
        params.setId(id);

        VOTriggerDefinition trigger = new VOTriggerDefinition();
        trigger.setKey(id);
        trigger.setOrganization(new VOOrganization());

        List<VOTriggerDefinition> col = new ArrayList<>();
        col.add(trigger);

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);

        when(service.getTriggerDefinitions()).thenReturn(col);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        RepresentationCollection<DefinitionRepresentation> result = backend
                .getCollection().get(params);

        assertEquals(id,
                result.getItems().toArray(new DefinitionRepresentation[] {})[0]
                        .getId());
        Mockito.verify(service).getTriggerDefinitions();

        when(service.getTriggerDefinitions())
                .thenThrow(new javax.ejb.EJBAccessException());

        try {
            backend.getCollection().get(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testPostCollection() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();

        DefinitionRepresentation trigger = new DefinitionRepresentation();

        VOTriggerDefinition definition = new VOTriggerDefinition();
        definition.setKey(id);

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);

        when(service.createTriggerDefinition(definition)).thenReturn(id);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        backend.postCollection().post(trigger, params);

        Mockito.verify(service).createTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class));

        when(service.createTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class)))
                        .thenThrow(new ValidationException());

        try {
            backend.postCollection().post(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        when(service.createTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class)))
                        .thenThrow(new TriggerDefinitionDataException());

        try {
            backend.postCollection().post(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        when(service.createTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class)))
                        .thenThrow(new javax.ejb.EJBAccessException());

        try {
            backend.postCollection().post(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testPutItemWithoutEtag() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();
        params.setId(id);

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setId(id);
        trigger.setETag(1L);
        trigger.setAction("SUBSCRIBE_TO_SERVICE");
        trigger.setDescription("desc");
        trigger.setTargetURL("abc");
        trigger.setType("REST_SERVICE");
        trigger.setSuspending(Boolean.TRUE);

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        backend.putItem().put(trigger, params);

        Mockito.verify(service, Mockito.never()).getTriggerDefinition(id);
        Mockito.verify(service).updateTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class));

        Mockito.doThrow(new ObjectNotFoundException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new ValidationException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new ConcurrentModificationException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new TriggerDefinitionDataException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new OperationNotPermittedException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new javax.ejb.EJBAccessException()).when(service)
                .updateTriggerDefinition(
                        Mockito.any(VOTriggerDefinition.class));

        try {
            backend.putItem().put(trigger, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @Test
    public void testPutItemWithEtag() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();
        params.setId(id);
        params.setMatch("3");

        DefinitionRepresentation trigger = new DefinitionRepresentation();
        trigger.setId(id);

        VOTriggerDefinition definition = new VOTriggerDefinition();
        definition.setKey(id);
        definition.setVersion(3);

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);
        when(service.getTriggerDefinition(id)).thenReturn(definition);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        backend.putItem().put(trigger, params);

        Mockito.verify(service).getTriggerDefinition(id);
        Mockito.verify(service).updateTriggerDefinition(
                Mockito.any(VOTriggerDefinition.class));
    }

    @Test
    public void testDeleteItem() throws Exception {

        Long id = 1L;

        RequestParameters params = new RequestParameters();
        params.setId(id);

        TriggerDefinitionService service = Mockito
                .mock(TriggerDefinitionService.class);

        DefinitionBackend backend = spy(new DefinitionBackend());
        when(backend.getService()).thenReturn(service);
        backend.deleteItem().delete(params);

        Mockito.verify(service).deleteTriggerDefinition(id);

        Mockito.reset(service);

        Mockito.doThrow(new ObjectNotFoundException()).when(service)
                .deleteTriggerDefinition(params.getId());

        try {
            backend.deleteItem().delete(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new DeletionConstraintException()).when(service)
                .deleteTriggerDefinition(params.getId());

        try {
            backend.deleteItem().delete(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new OperationNotPermittedException()).when(service)
                .deleteTriggerDefinition(params.getId());

        try {
            backend.deleteItem().delete(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new javax.ejb.EJBAccessException()).when(service)
                .deleteTriggerDefinition(params.getId());

        try {
            backend.deleteItem().delete(params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }
}
