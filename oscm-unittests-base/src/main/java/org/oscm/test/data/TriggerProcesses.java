/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;

/**
 * Setup class for test data related to trigger processes.
 * 
 * @author barzu
 */
public class TriggerProcesses {

    public static TriggerProcess createPendingTriggerProcess(DataService mgr,
            PlatformUser user, TriggerDefinition triggerDefinition)
            throws Exception {
        return createTriggerProcess(mgr, user, triggerDefinition,
                TriggerProcessStatus.WAITING_FOR_APPROVAL);
    }

    public static TriggerProcess createTriggerProcess(DataService mgr,
            PlatformUser user, TriggerDefinition triggerDefinition,
            TriggerProcessStatus status) throws Exception {
        TriggerProcess triggerProcess = new TriggerProcess();
        triggerProcess.setState(status);
        triggerProcess.setUser(user);
        triggerProcess.setTriggerDefinition(triggerDefinition);
        mgr.persist(triggerProcess);
        return triggerProcess;
    }

    /**
     * Retrieves all currently available trigger process identifier objects from
     * the database.
     * 
     * @param tpKeys
     *            the trigger process keys to filter for. If none is specified,
     *            all identifiers are returned.
     * 
     * @return The trigger process identifiers.
     * @throws Exception
     */
    public static List<TriggerProcessIdentifier> getProcessIdentifiers(
            DataService mgr,
            final Long... tpKeys) throws Exception {
        String queryString = "SELECT tpi FROM TriggerProcessIdentifier tpi";
        List<Long> keys = null;
        if (tpKeys.length > 0) {
            queryString += " WHERE tpi.triggerProcess.key IN (:keys)";
            keys = Arrays.asList(tpKeys);
        }
        Query query = mgr.createQuery(queryString);
        if (keys != null) {
            query.setParameter("keys", keys);
        }
        return ParameterizedTypes.list(query.getResultList(),
                TriggerProcessIdentifier.class);
    }
}
