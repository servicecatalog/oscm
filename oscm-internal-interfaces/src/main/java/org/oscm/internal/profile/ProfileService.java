/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Service bean to handle profile related operations.
 * 
 * @author jaeger
 * 
 */
@Remote
public interface ProfileService {

    /**
     * Determines the profile data of the currently logged in user and returns
     * it. The organization data will only be provided in case the user is an
     * administrative user of the organization.
     * 
     * @return The profile data.
     */
    public POProfile getProfile();

    /**
     * Stores the profile data as specified.
     * 
     * @param profile
     *            The data to store.
     * @param marketplaceId
     *            The identifier of the current marketplace
     * @throws ImageException
     * @throws ConcurrentModificationException
     * @throws DistinguishedNameException
     * @throws TechnicalServiceOperationException
     * @throws TechnicalServiceNotAliveException
     * @throws OperationNotPermittedException
     * @throws ValidationException
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     */
    public void saveProfile(POProfile profile, String marketplaceId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException, ImageException;

}
