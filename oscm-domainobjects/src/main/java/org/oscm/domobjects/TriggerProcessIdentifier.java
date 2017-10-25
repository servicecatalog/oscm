/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.types.enumtypes.TriggerProcessIdentifierName;

/**
 * @author barzu
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "TriggerProcessIdentifier.isActivateDeactivateServicePending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type IN (:triggerTypes)"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi.triggerProcess = tp"
                + " AND tppi.dataContainer.name = :serviceKeyName"
                + " AND tppi.dataContainer.value = :serviceKey"),
        @NamedQuery(name = "TriggerProcessIdentifier.getPendingAddRevokeUsers", query = "SELECT tppi3"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi1, TriggerProcessIdentifier tppi2, TriggerProcessIdentifier tppi3"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type = :triggerType"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi1.triggerProcess = tp"
                + " AND tppi1.dataContainer.name = :orgKeyName"
                + " AND tppi1.dataContainer.value = :orgKey"
                + " AND tppi2.triggerProcess = tp"
                + " AND tppi2.dataContainer.name = :subscriptionIdName"
                + " AND tppi2.dataContainer.value = :subscriptionId"
                + " AND tppi3.triggerProcess = tp"
                + " AND tppi3.dataContainer.name IN (:userNames)"
                + " AND tppi3.dataContainer.value IN (:users)"),
        @NamedQuery(name = "TriggerProcessIdentifier.isRegisterCustomerForSupplierPending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type = :triggerType"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi.triggerProcess = tp"
                + " AND ((tppi.dataContainer.name = :userIdName"
                + " AND tppi.dataContainer.value = :userId)"
                + " OR (tppi.dataContainer.name = :userEmailName"
                + " AND tppi.dataContainer.value = :userEmail))"),
        @NamedQuery(name = "TriggerProcessIdentifier.isSavePaymentConfigurationPending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type = :triggerType"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi.triggerProcess = tp"
                + " AND tppi.dataContainer.name = :orgKeyName"
                + " AND tppi.dataContainer.value = :orgKey"),
        @NamedQuery(name = "TriggerProcessIdentifier.isModifyOrUpgradeSubscriptionPending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type IN (:triggerTypes)"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi.triggerProcess = tp"
                + " AND tppi.dataContainer.name = :subscriptionKeyName"
                + " AND tppi.dataContainer.value = :subscriptionKey"),
        @NamedQuery(name = "TriggerProcessIdentifier.isSubscribeOrUnsubscribeServicePending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi1, TriggerProcessIdentifier tppi2"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type IN (:triggerTypes)"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi1.triggerProcess = tp"
                + " AND tppi1.dataContainer.name = :orgKeyName"
                + " AND tppi1.dataContainer.value = :orgKey"
                + " AND tppi2.triggerProcess = tp"
                + " AND tppi2.dataContainer.name = :subscriptionIdName"
                + " AND tppi2.dataContainer.value = :subscriptionId"),

        @NamedQuery(name = "TriggerProcessIdentifier.isRegisterOwnUserPending", query = "SELECT count(tp)"
                + " FROM TriggerProcess tp, TriggerDefinition td, TriggerProcessIdentifier tppi1, TriggerProcessIdentifier tppi2"
                + " WHERE tp.dataContainer.status IN (:pendingStates)"
                + " AND tp.triggerDefinition = td"
                + " AND td.dataContainer.type = :triggerType"
                + " AND td.dataContainer.suspendProcess = true"
                + " AND tppi1.triggerProcess = tp"
                + " AND tppi1.dataContainer.name = :orgKeyName"
                + " AND tppi1.dataContainer.value = :orgKey"
                + " AND tppi2.triggerProcess = tp"
                + " AND tppi2.dataContainer.name = :userIdName"
                + " AND tppi2.dataContainer.value = :userId") })
public class TriggerProcessIdentifier extends
        DomainObjectWithVersioning<TriggerProcessIdentifierData> {

    private static final long serialVersionUID = 7796500832821217722L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TriggerProcess triggerProcess;

    public TriggerProcessIdentifier() {
        super();
        dataContainer = new TriggerProcessIdentifierData();
    }

    public TriggerProcessIdentifier(TriggerProcessIdentifierName name,
            String value) {
        this();
        setName(name);
        setValue(value);
    }

    public TriggerProcess getTriggerProcess() {
        return triggerProcess;
    }

    public void setTriggerProcess(TriggerProcess triggerProcess) {
        this.triggerProcess = triggerProcess;
    }

    public TriggerProcessIdentifierName getName() {
        return dataContainer.getName();
    }

    public void setName(TriggerProcessIdentifierName name) {
        dataContainer.setName(name);
    }

    public String getValue() {
        return dataContainer.getValue();
    }

    public void setValue(String value) {
        dataContainer.setValue(value);
    }
}
