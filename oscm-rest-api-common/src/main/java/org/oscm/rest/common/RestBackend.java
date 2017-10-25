/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

/**
 * Interfaces for backend commands.
 * 
 * @author miethaner
 */
public interface RestBackend {

    /**
     * Interface for GET commands.
     * 
     * @author miethaner
     *
     * @param <R>
     *            a representation
     * @param <P>
     *            request parameters
     */
    interface Get<R extends Representation, P extends RequestParameters> {

        /**
         * Backend command called by GET frontend methods. Reads the entities
         * specified by the parameters and returns them.
         * 
         * @param params
         *            the request parameters
         * @return the requested representation or -collection
         * @throws Exception
         */
        R get(P params) throws Exception;
    }

    /**
     * Interface for GET commands to return collection.
     * 
     * @author weiser
     *
     * @param <R>
     *            the representation type contained in the collection
     * @param <P>
     *            request parameters
     */
    interface GetCollection<R extends Representation, P extends RequestParameters> {

        /**
         * Backend command called by GET frontend methods. Reads the entities
         * specified by the parameters and returns them.
         * 
         * @param params
         *            the request parameters
         * @return the requested representation collection
         * @throws Exception
         */
        RepresentationCollection<R> getCollection(P params) throws Exception;
    }

    /**
     * Interface for POST commands.
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
         * Backend command called by POST frontend methods. Creates the entity
         * specified by the representation and parameters and returns the its
         * new id.
         * 
         * @param content
         *            the representation to create
         * @param params
         *            the request parameters
         * @return the id object or <code>null</code> if the resource creation
         *         is executed asynchronously or suspended by a trigger
         * @throws Exception
         */
        Object post(R content, P params) throws Exception;
    }

    /**
     * Interface for PUT commands.
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
         * Backend command called by PUT frontend methods. Updates the entity
         * specified by the representation and parameters.
         * 
         * @param content
         *            the representation to update
         * @param params
         *            the request parameters
         * @return <code>true</code> on immediate PUT, <code>false</code> on
         *         asynchronous/suspended PUT
         * @throws Exception
         */
        boolean put(R content, P params) throws Exception;
    }

    /**
     * Interface for DELETE commands.
     * 
     * @author miethaner
     *
     * @param <P>
     *            request parameters
     */
    interface Delete<P extends RequestParameters> {

        /**
         * Backend command called by DELETE frontend methods. Deletes the entity
         * specified by the parameters.
         * 
         * @param params
         *            the request parameters
         * @return <code>true</code> on immediate DELETE, <code>false</code> on
         *         asynchronous/suspended DELETE
         * @throws Exception
         */
        boolean delete(P params) throws Exception;
    }

}
