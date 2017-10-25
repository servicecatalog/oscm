/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;

/**
 * Data loader for a service selector.
 * 
 * @author barzu
 */
@Remote
public interface ServiceSelector {

    /**
     * Retrieves the services that are no service copies and that have the
     * status ACTIVE or INACTIVE.
     * <p>
     * Required role: platform operator
     * 
     * if successful return the List of services ({@link POService}) in Result
     * 
     * @return Response object containing a list of {@link POService} instances
     */
    public Response getTemplateServices();

}
