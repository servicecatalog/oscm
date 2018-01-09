/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.oscm.billing.external.data.FileBilling;

/**
 * Implementation of the billing application resource.
 * 
 */
@Path("/application")
public class BillingAppResource {

    @Context
    private UriInfo info;

    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        FileBilling fb = new FileBilling();
        return Response.ok(fb.getSuccessfulConnectionMsg()).build();
    }
}
