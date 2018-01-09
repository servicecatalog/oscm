/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: May 12, 2016
 *
 *******************************************************************************/

package org.oscm.rest.trigger;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.oscm.rest.common.*;
import org.oscm.rest.trigger.config.TriggerCommonParams;
import org.oscm.rest.trigger.data.ActionRepresentation;
import org.oscm.rest.trigger.data.DefinitionRepresentation;
import org.oscm.rest.trigger.data.ProcessRepresentation;


/**
 * Rest root resource for trigger component
 *
 * @author miethaner
 */
@Path(CommonParams.PATH_VERSION)
@Stateless
public class RestTriggerResource extends RestResource {

    @EJB
    private DefinitionBackend triggerBackend;

    @EJB
    private ProcessBackend processBackend;

    /**
     * Endpoint class for trigger definition
     *
     * @author miethaner
     */
    public class Definition implements
            RestFrontend.Crud<DefinitionRepresentation, RequestParameters> {

        /**
         * Gets the trigger definition for the given id.
         */
        @Since(CommonParams.VERSION_1)
        @GET
        @Path(CommonParams.PATH_ID)
        @Produces(MediaType.APPLICATION_JSON)
        @Override
        public Response getItem(@Context UriInfo uriInfo,
                                @BeanParam RequestParameters params) throws Exception {
            return get(uriInfo, getTriggerBackend().getItem(), params, true);
        }

        /**
         * Gets all trigger definitions of the callers organization.
         */
        @Since(CommonParams.VERSION_1)
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Override
        public Response getCollection(@Context UriInfo uriInfo,
                                      @BeanParam RequestParameters params) throws Exception {
            return get(uriInfo, getTriggerBackend().getCollection(), params, false);
        }

        /**
         * Creates a new trigger definition from the given data. Returns new
         * location via response header
         */
        @Since(CommonParams.VERSION_1)
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Override
        public Response postCollection(@Context UriInfo uriInfo,
                                       DefinitionRepresentation content,
                                       @BeanParam RequestParameters params) throws Exception {
            return post(uriInfo, getTriggerBackend().postCollection(), content,
                    params);
        }

        /**
         * Updates the trigger definition with the given id.
         */
        @Since(CommonParams.VERSION_1)
        @PUT
        @Path(CommonParams.PATH_ID)
        @Consumes(MediaType.APPLICATION_JSON)
        @Override
        public Response putItem(@Context UriInfo uriInfo,
                                DefinitionRepresentation content,
                                @BeanParam RequestParameters params) throws Exception {
            return put(uriInfo, getTriggerBackend().putItem(), content, params);
        }

        /**
         * Deletes the trigger definition with the given id.
         */
        @Since(CommonParams.VERSION_1)
        @DELETE
        @Path(CommonParams.PATH_ID)
        @Override
        public Response deleteItem(@Context UriInfo uriInfo,
                                   @BeanParam RequestParameters params) throws Exception {
            return delete(uriInfo, getTriggerBackend().deleteItem(), params);
        }

    }

    /**
     * Redirects to trigger definition endpoints
     *
     * @return the trigger definition endpoints
     */
    @Path(TriggerCommonParams.PATH_DEFINITIONS)
    public Definition redirectToTrigger() {
        return new Definition();
    }

    /**
     * Endpoint class for trigger action
     *
     * @author miethaner
     */
    public class Action implements RestFrontend.Get<RequestParameters> {

        @Override
        public Response getItem(@Context UriInfo uriInfo,
                                @BeanParam RequestParameters params)
                throws WebApplicationException {
            return null;
        }

        /**
         * Gets all available trigger actions
         */
        @Since(CommonParams.VERSION_1)
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Override
        public Response getCollection(@Context UriInfo uriInfo,
                                      @BeanParam RequestParameters params) throws Exception {

            RestBackend.Get<RepresentationCollection<ActionRepresentation>, RequestParameters> backend;
            backend = params1 -> {

                Collection<ActionRepresentation> col = new ArrayList<>();
                col.add(new ActionRepresentation(null,
                        ActionRepresentation.Action.SUBSCRIBE_TO_SERVICE));
                col.add(new ActionRepresentation(null,
                        ActionRepresentation.Action.UNSUBSCRIBE_FROM_SERVICE));
                col.add(new ActionRepresentation(null,
                        ActionRepresentation.Action.MODIFY_SUBSCRIPTION));

                return new RepresentationCollection<>(col);
            };

            return get(uriInfo, backend, params, false);
        }
    }

    /**
     * Redirects to trigger action endpoints
     *
     * @return the trigger action endpoints
     */
    @Path(TriggerCommonParams.PATH_ACTIONS)
    public Action redirectToAction() {
        return new Action();
    }

    /**
     * Endpoint class for trigger process
     *
     * @author miethaner
     */
    public class Process {

        /**
         * Approves the process with the given id and forwards the given
         * comment.
         *
         * @param uriInfo
         *            the request context
         * @param params
         *            the request parameters
         * @return the response without content
         * @throws WebApplicationException
         */
        @Since(CommonParams.VERSION_1)
        @PUT
        @Path(CommonParams.PATH_ID + TriggerCommonParams.PATH_TRIGGER_APPROVE)
        public Response putApprove(@Context UriInfo uriInfo,
                                   @BeanParam RequestParameters params) throws Exception {
            ProcessRepresentation process = new ProcessRepresentation();
            process.setComment("");
            return put(uriInfo, getProcessBackend().putApprove(), process, params);
        }

        /**
         * Rejects the process with the given id and forwards the given comment.
         *
         * @param uriInfo
         *            the uriInfo context
         * @param content
         *            the representation with the comment
         * @param params
         *            the request parameters
         * @return the response without content
         * @throws WebApplicationException
         */
        @Since(CommonParams.VERSION_1)
        @PUT
        @Path(CommonParams.PATH_ID + TriggerCommonParams.PATH_TRIGGER_REJECT)
        @Consumes(MediaType.APPLICATION_JSON)
        public Response putReject(@Context UriInfo uriInfo,
                                  ProcessRepresentation content,
                                  @BeanParam RequestParameters params) throws Exception {
            return put(uriInfo, getProcessBackend().putReject(), content, params);
        }

    }

    /**
     * Redirects to trigger process endpoints
     *
     * @return the trigger process endpoints
     */
    @Path(TriggerCommonParams.PATH_PROCESSES)
    public Process redirectToProcess() {
        return new Process();
    }

    public DefinitionBackend getTriggerBackend() {
        return triggerBackend;
    }

    public ProcessBackend getProcessBackend() {
        return processBackend;
    }
}
