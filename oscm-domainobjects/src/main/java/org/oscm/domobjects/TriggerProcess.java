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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * JPA managed entity representing a trigger process.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "TriggerProcess.getAllForOrganization", query = "SELECT o FROM TriggerProcess o WHERE o.triggerDefinition.organization.key = :organizationKey ORDER BY o.dataContainer.activationDate DESC"),
        @NamedQuery(name = "TriggerProcess.getAllForOrganizationRelatedSubscription", query = "SELECT o FROM TriggerProcess o WHERE o.triggerDefinition.organization.key = :organizationKey AND o.triggerDefinition.dataContainer.type IN (org.oscm.internal.types.enumtypes.TriggerType.MODIFY_SUBSCRIPTION, org.oscm.internal.types.enumtypes.TriggerType.SUBSCRIBE_TO_SERVICE, org.oscm.internal.types.enumtypes.TriggerType.SAVE_PAYMENT_CONFIGURATION, org.oscm.internal.types.enumtypes.TriggerType.UNSUBSCRIBE_FROM_SERVICE, org.oscm.internal.types.enumtypes.TriggerType.UPGRADE_SUBSCRIPTION, org.oscm.internal.types.enumtypes.TriggerType.ADD_REVOKE_USER)"),
        @NamedQuery(name = "TriggerProcess.getAllForUser", query = "SELECT o FROM TriggerProcess o WHERE o.user.key = :userKey AND o.triggerDefinition.organization.key = :organizationKey ORDER BY o.dataContainer.activationDate DESC"),
        @NamedQuery(name = "TriggerProcess.getAllForTriggerDefinition", query = "SELECT o FROM TriggerProcess o WHERE o.triggerDefinition.key = :triggerDefinitionKey ORDER BY o.dataContainer.activationDate DESC"),
        @NamedQuery(name = "TriggerProcess.getAllForTriggerDefinitionWithStatus", query = "SELECT o FROM TriggerProcess o WHERE o.triggerDefinition.key = :triggerDefinitionKey AND o.dataContainer.status IN (:triggerProcessStatus) ORDER BY o.dataContainer.activationDate DESC") })
public class TriggerProcess extends
        DomainObjectWithVersioning<TriggerProcessData> {

    private static final long serialVersionUID = 3306614951053331464L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.TRIGGER_PROCESS_REASON));

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TriggerDefinition triggerDefinition;

    @OneToMany(mappedBy = "triggerProcess", cascade = { CascadeType.REMOVE,
            CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @OrderBy
    private List<TriggerProcessParameter> triggerProcessParameters = new ArrayList<TriggerProcessParameter>();

    @OneToMany(mappedBy = "triggerProcess", cascade = { CascadeType.REMOVE,
            CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @OrderBy
    private List<TriggerProcessIdentifier> triggerProcessIdentifiers = new ArrayList<TriggerProcessIdentifier>();

    @ManyToOne(fetch = FetchType.LAZY)
    private PlatformUser user;

    public TriggerProcess() {
        super();
        dataContainer = new TriggerProcessData();
    }

    public void setTriggerDefinition(TriggerDefinition triggerDefinition) {
        this.triggerDefinition = triggerDefinition;
    }

    public TriggerDefinition getTriggerDefinition() {
        return triggerDefinition;
    }

    public List<TriggerProcessParameter> getTriggerProcessParameters() {
        return triggerProcessParameters;
    }

    public List<TriggerProcessIdentifier> getTriggerProcessIdentifiers() {
        return triggerProcessIdentifiers;
    }

    public void setTriggerProcessIdentifiers(
            List<TriggerProcessIdentifier> triggerProcessIdentifiers) {
        this.triggerProcessIdentifiers = triggerProcessIdentifiers;
        for (TriggerProcessIdentifier identifier : triggerProcessIdentifiers) {
            identifier.setTriggerProcess(this);
        }
    }

    public void setTriggerProcessParameters(
            List<TriggerProcessParameter> triggerProcessParameters) {
        this.triggerProcessParameters = triggerProcessParameters;
    }

    public void setUser(PlatformUser user) {
        this.user = user;
    }

    public PlatformUser getUser() {
        return user;
    }

    // -----------------------
    // dataContainer delegates

    public TriggerProcessStatus getStatus() {
        return dataContainer.getStatus();
    }

    public void setState(TriggerProcessStatus status) {
        dataContainer.setState(status);
    }

    public long getActivationDate() {
        return dataContainer.getActivationDate();
    }

    public void setActivationDate(long activationDate) {
        dataContainer.setActivationDate(activationDate);
    }

    /**
     * Add a TriggerProcessParameter to the TriggerProcessParameter list of this
     * TriggerProcess.
     * 
     * @param name
     *            The name of the TriggerProcessParameter.
     * @param value
     *            The value of the TriggerProcessParameter.
     */
    public TriggerProcessParameter addTriggerProcessParameter(
            TriggerProcessParameterName name, Object value) {
        TriggerProcessParameter param = new TriggerProcessParameter();
        param.setTriggerProcess(this);
        param.setName(name);
        param.setValue(value);
        getTriggerProcessParameters().add(param);
        return param;
    }

    /**
     * Creates a new TriggerProcessIdentifier instance and adds it to the
     * TriggerProcessIdentifier list of this TriggerProcess.
     * 
     * @param name
     *            The name of the TriggerProcessIdentifier.
     * @param value
     *            The value of the TriggerProcessIdentifier.
     * @return The newly created TriggerProcessIdentifier instance.
     */
    public TriggerProcessIdentifier addTriggerProcessIdentifier(
            TriggerProcessIdentifierName name, String value) {
        TriggerProcessIdentifier identifier = new TriggerProcessIdentifier();
        identifier.setTriggerProcess(this);
        identifier.setName(name);
        identifier.setValue(value);
        getTriggerProcessIdentifiers().add(identifier);
        return identifier;
    }

    /**
     * Returns the modification data for the attribute with the given name.
     * 
     * @param name
     *            The name of the attribute.
     * @return The modification data.
     */
    public TriggerProcessParameter getParamValueForName(
            TriggerProcessParameterName name) {
        for (TriggerProcessParameter tpm : getTriggerProcessParameters()) {
            if (tpm.getName() == name) {
                return tpm;
            }
        }
        return null;
    }

    /**
     * Returns a list of identifier instances for the specified parameter
     * identifier name
     * 
     * @param name
     *            The name of the identifier
     * @return The list of identifier instance, which is empty if no identifier
     *         found for the specified name.
     */
    public List<TriggerProcessIdentifier> getIdentifierValuesForName(
            TriggerProcessIdentifierName name) {
        List<TriggerProcessIdentifier> identifiers = new ArrayList<TriggerProcessIdentifier>();
        for (TriggerProcessIdentifier tppi : getTriggerProcessIdentifiers()) {
            if (tppi.getName() == name) {
                identifiers.add(tppi);
            }
        }
        return identifiers;
    }

    /**
     * Returns the set of trigger process status for the trigger process which
     * is unfinished.
     * 
     * @return The set of trigger process status
     */
    public static Set<TriggerProcessStatus> getUnfinishedStatus() {
        Set<TriggerProcessStatus> statusSet = new HashSet<TriggerProcessStatus>();
        statusSet.add(TriggerProcessStatus.INITIAL);
        statusSet.add(TriggerProcessStatus.WAITING_FOR_APPROVAL);
        return statusSet;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }
}
