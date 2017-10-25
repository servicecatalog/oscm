/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 14.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * JPA managed entity representing a trigger definition.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQueries({ @NamedQuery(name = "TriggerDefinition.getAllForOrganizationAndName", query = "SELECT td FROM TriggerDefinition td WHERE td.organization.key = :organizationKey AND td.dataContainer.type = :type") })
public class TriggerDefinition extends
        DomainObjectWithVersioning<TriggerDefinitionData> {

    private static final long serialVersionUID = 7568098325172286511L;

    private static final transient Log4jLogger logger = LoggerFactory
            .getLogger(TriggerDefinition.class);

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Organization organization;

    public TriggerDefinition() {
        super();
        dataContainer = new TriggerDefinitionData();
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }

    // -----------------------
    // dataContainer delegates

    public TriggerType getType() {
        return dataContainer.getType();
    }

    public void setType(TriggerType type) {
        dataContainer.setType(type);
    }

    public String getTarget() {
        return dataContainer.getTarget();
    }

    public void setTarget(String target) {
        dataContainer.setTarget(target);
    }

    public TriggerTargetType getTargetType() {
        return dataContainer.getTargetType();
    }

    public void setTargetType(TriggerTargetType type) {
        dataContainer.setTargetType(type);
    }

    public boolean isSuspendProcess() {
        return dataContainer.getType().isSuspendProcess()
                && dataContainer.isSuspendProcess();
    }

    public void setName(String name) {
        dataContainer.setName(name);
    }

    public String getName() {
        return dataContainer.getName();
    }

    public void setSuspendProcess(boolean suspendProcess) {
        if (suspendProcess && !getType().isSuspendProcess()) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "The trigger type" + getType()
                            + " doesn't support process suspending!");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_TRIGGER_TYPE_NOT_SUPPORTED_PROCESS_SUSPENDING,
                    String.valueOf(getType()));
            throw e;
        }
        dataContainer.setSuspendProcess(suspendProcess);
    }

}
