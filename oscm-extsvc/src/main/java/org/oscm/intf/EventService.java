/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.DuplicateEventException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOGatheredEvent;

/**
 * Remote interface of the event management service.
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface EventService {

    /**
     * Stores the given event information in the database. This method can be
     * used if the key of the subscription for which the event is to be recorded
     * is not known.
     * <p>
     * Required role: any user role in the technology provider organization that
     * owns the specified technical service
     * 
     * @param technicalServiceId
     *            the ID of the technical service to which the event is related
     * @param instanceId
     *            the ID of the application instance created for the
     *            subscription to which the event is related
     * @param event
     *            the event to be stored
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization is not the owner of the
     *             technical service
     * @throws DuplicateEventException
     *             if an event with the same unique ID was already stored for
     *             the subscription found via the technical service ID and
     *             instance ID
     * @throws ObjectNotFoundException
     *             if no subscription is found via the technical service ID and
     *             instance ID, or if the technical service does not define
     *             events of the type specified as identifier for the event to
     *             be stored
     * @throws ValidationException
     *             if the actor or the unique ID of the event is longer than 255
     *             characters
     */
    @WebMethod
    public void recordEventForInstance(
            @WebParam(name = "technicalServiceId") String technicalServiceId,
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "event") VOGatheredEvent event)
            throws OrganizationAuthoritiesException, DuplicateEventException,
            ObjectNotFoundException, ValidationException;

    /**
     * Stores the given event information for the specified subscription in the
     * database.
     * <p>
     * Required role: any user role in the technology provider organization that
     * owns the technical service underlying to the subscription
     * 
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription for which to store the
     *            event
     * @param event
     *            the event to be stored
     * 
     * @throws DuplicateEventException
     *             if an event with the same unique ID was already stored for
     *             the subscription
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization is not the owner of the
     *             technical service on which the subscription is based
     * @throws ObjectNotFoundException
     *             if the subscription with the given key is not found, or if
     *             the technical service on which the subscription is based does
     *             not define events of the type specified as identifier for the
     *             event to be stored
     * @throws ValidationException
     *             if the actor or the unique ID of the event is longer than 255
     *             characters
     */
    @WebMethod
    public void recordEventForSubscription(
            @WebParam(name = "subscriptionKey") long subscriptionKey,
            @WebParam(name = "event") VOGatheredEvent event)
            throws DuplicateEventException, OrganizationAuthoritiesException,
            ObjectNotFoundException, ValidationException;

}
