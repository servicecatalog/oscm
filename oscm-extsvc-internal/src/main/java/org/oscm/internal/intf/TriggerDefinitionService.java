/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-20                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerDefinitionDataException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTriggerDefinition;

/**
 * Remote interface for managing the definitions of triggers which are used to
 * interact with external process control systems.
 */
@Remote
public interface TriggerDefinitionService {

    /**
     * Creates a trigger definition for the calling user's organization.
     * <p>
     * Required role: administrator of the organization, or operator of the
     * platform operator organization
     * 
     * @param trigger
     *            the value object containing the data of the new trigger
     *            definition
     * @return the key of the new definition
     * @throws TriggerDefinitionDataException
     *             if a trigger definition already exists for the action
     *             specified in the value object
     * @throws ValidationException
     *             if the validation of the value object fails
     */

    public Long createTriggerDefinition(VOTriggerDefinition trigger)
            throws TriggerDefinitionDataException, ValidationException;

    /**
     * Deletes the given trigger definition.
     * <p>
     * Required role: administrator of the organization that owns the trigger
     * definition, or operator of the platform operator organization
     * 
     * @param triggerKey
     *            the numeric key of the trigger definition to delete
     * @throws ObjectNotFoundException
     *             if the trigger definition is not found
     * @throws DeletionConstraintException
     *             if there are active trigger processes based on the trigger
     *             definition
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             trigger definition
     */

    public void deleteTriggerDefinition(long triggerKey)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException;

    /**
     * Deletes the given trigger definition.
     * <p>
     * Required role: administrator of the organization that owns the trigger
     * definition, or operator of the platform operator organization
     * 
     * @param triggerKey
     *            the numeric key of the trigger definition to delete
     * @throws ObjectNotFoundException
     *             if the trigger definition is not found
     * @throws DeletionConstraintException
     *             if there are active trigger processes based on the trigger
     *             definition
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             trigger definition
     * @throws ConcurrentModificationException
     *             if the trigger definition has been changed concurrently
     */
    public void deleteTriggerDefinition(VOTriggerDefinition triggerDefinition)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException, ConcurrentModificationException;

    /**
     * Updates the given trigger definition.
     * <p>
     * Required role: administrator of the organization that owns the trigger
     * definition, or operator of the platform operator organization
     * 
     * @param trigger
     *            the value object containing the new trigger definition
     * @throws ObjectNotFoundException
     *             if the trigger definition is not found
     * @throws ValidationException
     *             if the validation of the value object fails
     * @throws ConcurrentModificationException
     *             if the stored trigger definition is changed by another user
     *             in the time between reading and writing it
     * @throws TriggerDefinitionDataException
     *             if a trigger definition already exists for the action
     *             specified in the value object
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             trigger definition
     */

    public void updateTriggerDefinition(VOTriggerDefinition trigger)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, TriggerDefinitionDataException,
            OperationNotPermittedException;

    /**
     * Retrieves all trigger definitions of the calling user's organization.
     * <p>
     * Required role: administrator of the organization, or operator of the
     * platform operator organization
     * 
     * @return the list of trigger definitions
     */

    public List<VOTriggerDefinition> getTriggerDefinitions();

    /**
     * Retrieves the trigger definitions with the given id
     * <p>
     * Required role: administrator of the organization, or operator of the
     * platform operator organization
     * 
     * @throws ObjectNotFoundException
     *             if the trigger definition is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             trigger definition
     * 
     * @return the trigger definitions
     */

    public VOTriggerDefinition getTriggerDefinition(Long id)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the actions for which triggers can be defined by the calling
     * user's organization.
     * <p>
     * Required role: administrator of the organization, or operator of the
     * platform operator organization
     * 
     * @return the list of actions
     */

    public List<TriggerType> getTriggerTypes();
}
