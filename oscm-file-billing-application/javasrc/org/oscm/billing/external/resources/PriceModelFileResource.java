/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.resources;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.oscm.billing.external.data.FileBilling;

/**
 * Implementation of the price model file resource.
 * 
 */

@Path("/priceModelFile")
public class PriceModelFileResource {

    @QueryParam("FILENAME")
    private String fileName;

    @Context
    private UriInfo info;

    /**
     * Get the context of the price model file with the given name
     * 
     * @return the price model data as an octet stream or an empty 200_OK
     *         response if the price model file was not found
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPriceModelFile() {

        FileBilling fb = new FileBilling();
        File priceModelFile = fb.getPriceModelFile(fileName);
        return Response.ok(priceModelFile).build();
    }

}
