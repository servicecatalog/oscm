/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Setup class for test data related to trigger definitions.
 * 
 * @author barzu
 */
public class TriggerDefinitions {

    public static TriggerDefinition createSuspendingTriggerDefinition(DataService mgr,
            Organization org, TriggerType triggerType) throws Exception {
        return createTriggerDefinition(mgr, org, triggerType, true);
    }

    public static TriggerDefinition createTriggerDefinition(DataService mgr,
            Organization org, TriggerType triggerType, boolean suspending)
            throws Exception {
        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setOrganization(org);
        triggerDefinition
                .setTarget("http://estbesdev1:8680/oscm-integrationtests-mockproduct/NotificationService?wsdl");
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setType(triggerType);
        triggerDefinition.setSuspendProcess(suspending);
        triggerDefinition.setName("Trigger_" + System.currentTimeMillis());
        mgr.persist(triggerDefinition);
        return triggerDefinition;
    }

}
