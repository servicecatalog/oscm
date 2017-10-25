/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.DuplicateEventException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOGatheredEvent;

/**
 * Remote interface of the event management service.
 */
@Remote
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

    public void recordEventForInstance(String technicalServiceId,
            String instanceId, VOGatheredEvent event)
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

    public void recordEventForSubscription(long subscriptionKey,
            VOGatheredEvent event) throws DuplicateEventException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            ValidationException;

}
