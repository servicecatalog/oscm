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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.exception.ExecutionTargetException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.rest.common.RequestParameters;
import org.oscm.rest.trigger.ProcessBackend;
import org.oscm.rest.trigger.data.ProcessRepresentation;

/**
 * @author miethaner
 *
 */
public class ProcessBackendTest {

    @Test
    public void testPutApprove() throws Exception {

        RequestParameters params = new RequestParameters();
        ProcessRepresentation process = new ProcessRepresentation();
        params.setId(new Long(1L));

        TriggerService service = Mockito.mock(TriggerService.class);

        ProcessBackend backend = spy(new ProcessBackend());
        when(backend.getService()).thenReturn(service);
        backend.putApprove().put(process, params);

        Mockito.verify(service).approveAction(params.getId().longValue());

        Mockito.reset(service);

        Mockito.doThrow(new ObjectNotFoundException()).when(service)
                .approveAction(params.getId().longValue());

        try {
            backend.putApprove().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new OperationNotPermittedException()).when(service)
                .approveAction(params.getId().longValue());

        try {
            backend.putApprove().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new TriggerProcessStatusException()).when(service)
                .approveAction(params.getId().longValue());

        try {
            backend.putApprove().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new ExecutionTargetException()).when(service)
                .approveAction(params.getId().longValue());

        try {
            backend.putApprove().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new javax.ejb.EJBAccessException()).when(service)
                .approveAction(params.getId().longValue());

        try {
            backend.putApprove().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutReject() throws Exception {

        RequestParameters params = new RequestParameters();
        ProcessRepresentation process = new ProcessRepresentation();
        process.setComment("comment");
        params.setId(new Long(1L));

        TriggerService service = Mockito.mock(TriggerService.class);

        ProcessBackend backend = spy(new ProcessBackend());
        when(backend.getService()).thenReturn(service);
        backend.putReject().put(process, params);

        Mockito.verify(service).rejectAction(
                Mockito.eq(params.getId().longValue()), Mockito.anyList());

        Mockito.reset(service);

        Mockito.doThrow(new ObjectNotFoundException()).when(service)
                .rejectAction(Mockito.eq(params.getId().longValue()),
                        Mockito.anyList());

        try {
            backend.putReject().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new OperationNotPermittedException()).when(service)
                .rejectAction(Mockito.eq(params.getId().longValue()),
                        Mockito.anyList());

        try {
            backend.putReject().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new TriggerProcessStatusException()).when(service)
                .rejectAction(Mockito.eq(params.getId().longValue()),
                        Mockito.anyList());

        try {
            backend.putReject().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new javax.ejb.EJBAccessException()).when(service)
                .rejectAction(Mockito.eq(params.getId().longValue()),
                        Mockito.anyList());

        try {
            backend.putReject().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPutCancel() throws Exception {

        RequestParameters params = new RequestParameters();
        ProcessRepresentation process = new ProcessRepresentation();
        process.setComment("comment");
        params.setId(new Long(1L));

        TriggerService service = Mockito.mock(TriggerService.class);

        ProcessBackend backend = spy(new ProcessBackend());
        when(backend.getService()).thenReturn(service);
        backend.putCancel().put(process, params);

        Mockito.verify(service).cancelActions(Mockito.anyList(),
                Mockito.anyList());

        Mockito.reset(service);

        Mockito.doThrow(new ObjectNotFoundException()).when(service)
                .cancelActions(Mockito.anyList(), Mockito.anyList());

        try {
            backend.putCancel().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.NOT_FOUND.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new OperationNotPermittedException()).when(service)
                .cancelActions(Mockito.anyList(), Mockito.anyList());

        try {
            backend.putCancel().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new TriggerProcessStatusException()).when(service)
                .cancelActions(Mockito.anyList(), Mockito.anyList());

        try {
            backend.putCancel().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.CONFLICT.getStatusCode(),
                    e.getResponse().getStatus());
        }

        Mockito.reset(service);

        Mockito.doThrow(new javax.ejb.EJBAccessException()).when(service)
                .cancelActions(Mockito.anyList(), Mockito.anyList());

        try {
            backend.putCancel().put(process, params);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(Status.FORBIDDEN.getStatusCode(),
                    e.getResponse().getStatus());
        }
    }
}
