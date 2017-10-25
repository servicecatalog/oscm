/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptiondetails;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

@Remote
public interface SubscriptionDetailsService {

    Response getSubscriptionDetails(String id, String language)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException;
    
    Response getSubscriptionDetails(long key, String language)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException;

    Response getServiceForSubscription(long serviceKey, String language)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthoritiesException;

    boolean isUserAssignedToTheSubscription(long userKey,
            long subscriptionKey) throws ObjectNotFoundException;

    Response loadSubscriptionStatus(long subscriptionKey)
            throws ObjectNotFoundException;

}
