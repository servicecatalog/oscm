/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.resources;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Implementation of the price model resource.
 * 
 */

@Path("/priceModel")
public class PriceModelResource {

    @QueryParam("LOCALES")
    private List<String> locales;

    @QueryParam("CONTEXT_KEYS")
    private List<String> contextKeys;

    @QueryParam("CONTEXT_VALUES")
    private List<String> contextValues;

    @Context
    private UriInfo info;

    /**
     * Get the price model data for the given context and locales
     * 
     * @return the price model data as an XML encoded list of strings
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPriceModel() {

        FileBilling fb = new FileBilling();
        splitParametersToList(contextKeys);
        splitParametersToList(contextValues);

        List<String> priceModelContent = fb.getPriceModel(contextKeys,
                contextValues, locales);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XMLEncoder xmlEncoder = new XMLEncoder(bos)) {
            xmlEncoder.writeObject(priceModelContent);
        }

        String serializedList = "";
        try {
            serializedList = bos.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
        }
        return Response.ok(serializedList).build();
    }

    private void splitParametersToList(List<String> contextList) {
        if (contextList != null && !contextList.isEmpty()) {
            String contextKeysTab[] = contextList.get(0).split(",");
            contextList.clear();
            for (int i = 0; i < contextKeysTab.length; i++) {
                String key = contextKeysTab[i].replaceAll("]", "");
                key = key.replaceAll("\\[", "");
                contextList.add(key.trim());
            }
        }
    }
}
