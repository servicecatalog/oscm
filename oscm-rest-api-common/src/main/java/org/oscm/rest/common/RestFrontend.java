/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 10, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Interfaces for REST endpoints
 * 
 * @author miethaner
 */
public interface RestFrontend {

    /**
     * Interface for HTTP GET methods.
     * 
     * @author miethaner
     *
     * @param <P>
     *            request parameters
     */
    interface Get<P extends RequestParameters> {

        /**
         * Gets the corresponding representation of the entry with the id in
         * params and wraps it in the response.
         *
         * @param uriInfo
         *            the request context
         * @param params
         *            the request parameters
         * @return the response with the representation
         * @throws Exception
         */
        Response getItem(UriInfo uriInfo, P params) throws Exception;

        /**
         * Gets all valid entry representations and wraps them in the response.
         * 
         * @param uriInfo
         *            the request context
         * @param params
         *            the request parameters
         * @return the response with the representations
         * @throws Exception
         */
        Response getCollection(UriInfo uriInfo, P params)
                throws Exception;
    }

    /**
     * Interface for HTTP POST methods.
     * 
     * @author miethaner
     *
     * @param <R>
     *            a representation
     * @param <P>
     *            request parameters
     */
    interface Post<R extends Representation, P extends RequestParameters> {

        /**
         * Creates a new entry from the given representation and returns its id
         * within the location header of the response.
         * 
         * @param uriInfo
         *            the request context
         * @param content
         *            the representation to create
         * @param params
         *            the request parameters
         * @return the response with the location
         * @throws Exception
         */
        Response postCollection(UriInfo uriInfo, R content, P params)
                throws Exception;
    }

    /**
     * Interface for HTTP PUT methods.
     * 
     * @author miethaner
     *
     * @param <R>
     *            a representation
     * @param <P>
     *            request parameters
     */
    interface Put<R extends Representation, P extends RequestParameters> {

        /**
         * Updates the entry with the id in params with the given
         * representation.
         * 
         * @param uriInfo
         *            the request context
         * @param content
         *            the representation to update
         * @param params
         *            the request parameters
         * @return the response without content
         * @throws Exception
         */
        Response putItem(UriInfo uriInfo, R content, P params)
                throws Exception;
    }

    /**
     * Interface for HTTP DELETE methods.
     * 
     * @author miethaner
     *
     * @param <P>
     *            request parameters
     */
    interface Delete<P extends RequestParameters> {

        /**
         * Deletes the entry with the id in params.
         * 
         * @param uriInfo
         *            the request context
         * @param params
         *            the request parameters
         * @return the response without content
         * @throws Exception
         */
        Response deleteItem(UriInfo uriInfo, P params) throws Exception;
    }

    /**
     * Interface for all standard methods.
     * 
     * @author miethaner
     *
     * @param <R>
     *            a representation
     * @param <P>
     *            request parameters
     */
    interface Crud<R extends Representation, P extends RequestParameters>
            extends Get<P>, Post<R, P>, Put<R, P>, Delete<P> {
    }

}
