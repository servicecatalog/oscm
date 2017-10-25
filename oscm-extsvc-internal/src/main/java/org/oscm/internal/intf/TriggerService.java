/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-02                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;
import org.oscm.internal.types.exception.ExecutionTargetException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TriggerProcessStatusException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOTriggerProcessParameter;

/**
 * Remote interface for handling trigger processes used to interact with
 * external process control systems.
 */
@Remote
public interface TriggerService {

    /**
     * Confirms a pending action, which leads to a continuation of the suspended
     * business logic.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param actionKey
     *            the key identifying the trigger process for the action
     * @throws ObjectNotFoundException
     *             if the trigger process with the given key is not found
     * @throws OperationNotPermittedException
     *             if the trigger process with the given key does not belong to
     *             the calling user's organization
     * @throws TriggerProcessStatusException
     *             if the status of the trigger process with the given key is
     *             not <code>WAITING_FOR_APPROVAL</code> or <code>INITIAL</code>
     * @throws ExecutionTargetException
     *             if an internal call to another service method fails; refer to
     *             the causing exception for details
     */

    public void approveAction(long actionKey)
            throws OperationNotPermittedException, ObjectNotFoundException,
            TriggerProcessStatusException, ExecutionTargetException;

    /**
     * Cancels actions which are in the <code>WAITING_FOR_APPROVAL</code>
     * status.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param actionKeys
     *            the keys identifying the trigger processes for the actions to
     *            cancel
     * @param reason
     *            optionally, the reason why the actions are canceled, or
     *            <code>null</code> to specify no reason
     * @throws ObjectNotFoundException
     *             if a trigger process with one of the given keys is not found
     * @throws OperationNotPermittedException
     *             if a trigger process with one of the given keys does not
     *             belong to the calling user's organization
     * @throws TriggerProcessStatusException
     *             if the status of a trigger process with one of the given keys
     *             is not <code>WAITING_FOR_APPROVAL</code> or
     *             <code>INITIAL</code>
     */

    public void cancelActions(List<Long> actionKeys,
            List<VOLocalizedText> reason) throws ObjectNotFoundException,
            OperationNotPermittedException, TriggerProcessStatusException;

    /**
     * Deletes trigger processes for actions from the database.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param actionKeys
     *            the keys identifying the trigger processes to delete
     * @throws ObjectNotFoundException
     *             if a trigger process with one of the given keys is not found
     * @throws OperationNotPermittedException
     *             if a trigger process with one of the given keys does not
     *             belong to the calling user's organization
     * @throws TriggerProcessStatusException
     *             if the status of a trigger process with one of the given keys
     *             differs from the following: <code>APPROVED</code>,
     *             <code>CANCELLED</code>, <code>ERROR</code>,
     *             <code>FAILED</code>, <code>REJECTED</code>,
     *             <code>NOTIFIED</code>
     */

    public void deleteActions(List<Long> actionKeys)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException;

    /**
     * Rejects a pending action.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param actionKey
     *            the key identifying the trigger process for the action
     * @param reason
     *            optionally, the reason why the action is rejected, or
     *            <code>null</code> to specify no reason
     * @throws ObjectNotFoundException
     *             if the trigger process with the given key is not found
     * @throws OperationNotPermittedException
     *             if the trigger process with the given key does not belong to
     *             the calling user's organization
     * @throws TriggerProcessStatusException
     *             if the status of the trigger process with the given key is
     *             not <code>WAITING_FOR_APPROVAL</code>
     */

    void rejectAction(long actionKey, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException;

    /**
     * Modifies the values of service parameters for the trigger process of a
     * <code>SUBSCRIBE_TO_SERVICE</code> action, which is waiting for approval.
     * After approval, the new subscription is created with the changed
     * parameter values.
     * <p>
     * You can only change the values of the service parameters. Changes to
     * other service properties are ignored.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param actionKey
     *            the key identifying the trigger process of the
     *            <code>SUBSCRIBE_TO_SERVICE</code> action for which the service
     *            parameter values are to be changed
     * @param parameters
     *            a list with the trigger process parameter specifying the new
     *            service parameter values
     * @throws ObjectNotFoundException
     *             if the trigger process with the given key is not found
     * @throws OperationNotPermittedException
     *             if the trigger process with the given key does not belong to
     *             the calling user's organization, or if the action (
     *             <code>TriggerType</code>) is not
     *             <code>SUBSCRIBE_TO_SERVICE</code>
     * @throws TriggerProcessStatusException
     *             if the status of the trigger process with the given key is
     *             not <code>WAITING_FOR_APPROVAL</code>
     * @throws ValidationException
     *             if the validation of the service parameters fails
     * 
     */
    void updateActionParameters(long actionKey,
            List<VOTriggerProcessParameter> parameters)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TriggerProcessStatusException, ValidationException;

    /**
     * Returns the trigger process parameter with the given type for the
     * specified trigger process.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param actionKey
     *            the key identifying the trigger process for which the
     *            parameter is to be returned
     * @param paramType
     *            the type of the trigger process parameter to be returned
     * @return the trigger process parameter
     * @throws ObjectNotFoundException
     *             if the trigger process with the given key or the trigger
     *             process parameter with the given type is not found
     * @throws OperationNotPermittedException
     *             if the trigger process with the given key does not belong to
     *             the calling user's organization
     */
    VOTriggerProcessParameter getActionParameter(long actionKey,
            TriggerProcessParameterType paramType)
            throws OperationNotPermittedException, ObjectNotFoundException;

    /**
     * Retrieves all trigger definitions of the calling user's organization.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the trigger definitions
     */

    public List<VOTriggerDefinition> getAllDefinitions();

    /**
     * Returns all trigger processes for actions which were initiated by the
     * calling user.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the trigger processes
     */

    public List<VOTriggerProcess> getAllActions();

    /**
     * Returns all trigger processes for actions which were initiated by a user
     * of the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the trigger processes
     */

    public List<VOTriggerProcess> getAllActionsForOrganization();

    /**
     * Returns all trigger processes for actions which were initiated by a user
     * of the calling user's organization and which are related to the subscription.
     * <p>
     * Required role: administrator of the organization
     *
     * @return the trigger processes
     */
    public List<VOTriggerProcess> getAllActionsForOrganizationRelatedSubscription();

}
