/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOTriggerProcess;

/**
 * Local interface providing all functionality to retrieve and manipulate
 * TriggerProcess data.
 * 
 * @author pock
 * 
 */
@Local
public interface TriggerServiceLocal {

    /**
     * Changes the state of the transaction in a new transaction.
     * 
     * @param triggerProcessKey
     *            The key of the TriggerProcess for which the status is set.
     * @param status
     *            The new status. Thrown if the TriggerProcess for the given key
     *            cannot be found.
     */
    public void setStatus(long triggerProcessKey, TriggerProcessStatus status)
            throws ObjectNotFoundException;

    /**
     * Save the reason of the TRIGGER_PROCESS_REASON in a new transaction.
     * 
     * @param triggerProcessKey
     *            The key of the TriggerProcess for which the status is set.
     * @param value
     *            The new reason.
     * @param localeString
     *            The information of local.
     */
    public void saveReason(long triggerProcessKey, String value,
            String localeString) throws ObjectNotFoundException;

    /**
     * Returns all trigger processes for actions which were related to
     * subscription.
     * <p>
     * 
     * @return the trigger processes
     */

    public List<VOTriggerProcess> getAllActionsForSubscription(
            String subscriptionId);
}
