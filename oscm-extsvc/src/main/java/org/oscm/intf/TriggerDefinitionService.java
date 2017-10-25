/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-20                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.TriggerDefinitionDataException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOTriggerDefinition;

/**
 * Remote interface for managing the definitions of triggers which are used to
 * interact with external process control systems.
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
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
     * @throws TriggerDefinitionDataException
     *             if a trigger definition already exists for the action
     *             specified in the value object
     * @throws ValidationException
     *             if the validation of the value object fails
     */
    @WebMethod
    public void createTriggerDefinition(
            @WebParam(name = "trigger") VOTriggerDefinition trigger)
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
    @WebMethod
    public void deleteTriggerDefinition(
            @WebParam(name = "triggerKey") long triggerKey)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException;

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
    @WebMethod
    public void updateTriggerDefinition(
            @WebParam(name = "trigger") VOTriggerDefinition trigger)
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
    @WebMethod
    public List<VOTriggerDefinition> getTriggerDefinitions();

    /**
     * Retrieves the actions for which triggers can be defined by the calling
     * user's organization.
     * <p>
     * Required role: administrator of the organization, or operator of the
     * platform operator organization
     * 
     * @return the list of actions
     */
    @WebMethod
    public List<TriggerType> getTriggerTypes();
}
