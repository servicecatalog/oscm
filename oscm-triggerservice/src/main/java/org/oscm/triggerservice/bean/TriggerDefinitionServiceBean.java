/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock
 *                                                                              
 *  Creation Date: 18.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.TriggerDefinitionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.TriggerDefinitionDataException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.triggerservice.assembler.TriggerDefinitionAssembler;
import org.oscm.triggerservice.local.TriggerDefinitionServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;

/**
 * Session Bean implementation class of TriggerDefinitionService
 * 
 * 
 * 
 */
@Stateless
@Remote(TriggerDefinitionService.class)
@Local(TriggerDefinitionServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class TriggerDefinitionServiceBean implements TriggerDefinitionService {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(TriggerDefinitionServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    private static TriggerType[] allowedTriggersForSupplier = {
            TriggerType.ACTIVATE_SERVICE, TriggerType.DEACTIVATE_SERVICE,
            TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
            TriggerType.SAVE_PAYMENT_CONFIGURATION,
            TriggerType.START_BILLING_RUN, TriggerType.SUBSCRIPTION_CREATION,
            TriggerType.SUBSCRIPTION_MODIFICATION,
            TriggerType.SUBSCRIPTION_TERMINATION, TriggerType.REGISTER_OWN_USER };

    private static TriggerType[] allowedTriggersForCustomer = {
            TriggerType.ADD_REVOKE_USER, TriggerType.MODIFY_SUBSCRIPTION,
            TriggerType.SUBSCRIBE_TO_SERVICE,
            TriggerType.UNSUBSCRIBE_FROM_SERVICE,
            TriggerType.UPGRADE_SUBSCRIPTION, TriggerType.START_BILLING_RUN,
            TriggerType.REGISTER_OWN_USER };

    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteTriggerDefinitionInt(long triggerDefinitionKey)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException {

        TriggerDefinition triggerDefinition = dm.getReference(
                TriggerDefinition.class, triggerDefinitionKey);

        checkOrgAuthority(triggerDefinition);

        // check if there are trigger processes exist for current trigger
        // definition.
        // excepts the triggerDefinition can not be deleted
        Query query = dm
                .createNamedQuery("TriggerProcess.getAllForTriggerDefinition");
        query.setParameter("triggerDefinitionKey",
                Long.valueOf(triggerDefinitionKey));
        List<TriggerProcess> triggerProcessList = ParameterizedTypes.list(
                query.getResultList(), TriggerProcess.class);
        if (triggerProcessList.size() > 0) {
            DeletionConstraintException sdce = new DeletionConstraintException(
                    ClassEnum.TRIGGER_DEFINITION,
                    String.valueOf(triggerDefinitionKey),
                    ClassEnum.TRIGGER_PROCESS);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sdce,
                    LogMessageIdentifier.WARN_TRIGGER_DELETION_FAILED);
            throw sdce;
        }
        dm.remove(triggerDefinition);

    }

    private void checkOrgAuthority(TriggerDefinition triggerDefinition)
            throws OperationNotPermittedException {
        if (getOwnOrganization().getKey() != triggerDefinition
                .getOrganization().getKey()) {
            OperationNotPermittedException ex = new OperationNotPermittedException(
                    "The client has no authority for the operation.");
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.ERROR_NO_AUTHORITY_TO_APPROVE,
                    ex.getMessage());
            throw ex;
        }
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public Long createTriggerDefinition(VOTriggerDefinition trigger)
            throws TriggerDefinitionDataException, ValidationException {

        ArgumentValidator.notNull("trigger", trigger);
        VOTriggerDefinition vo = trigger;

        Organization organization = getOwnOrganization();

        // check if trigger type is allowed for that organization
        if (!isAllowedTriggertype(vo.getType())) {
            throw new ValidationException(ReasonEnum.TRIGGER_TYPE_NOT_ALLOWED,
                    vo.getType().name(), null);
        }

        if (vo.isSuspendProcess()
                && organization.getSuspendingTriggerDefinition(vo.getType()) != null) {
            TriggerDefinitionDataException e = new TriggerDefinitionDataException(
                    "A suspending trigger definition does already exists for the type '"
                            + vo.getType() + "'");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_SUSPENDING_TRIGGER_ALREADY_EXISTS_FOR_TYPE,
                    String.valueOf(vo.getType()));
            throw e;
        }

        try {
            // default name
            if (vo.getName() == null) {
                vo.setName(vo.getType().name());
            }
            TriggerDefinition triggerDefinition = TriggerDefinitionAssembler
                    .toTriggerDefinition(vo);
            triggerDefinition.setOrganization(organization);

            dm.persist(triggerDefinition);
            dm.flush();

            return new Long(triggerDefinition.getKey());
        } catch (NonUniqueBusinessKeyException e) {
            // should not happen as the saved object doesn't have a business
            // key
            SaaSSystemException se = new SaaSSystemException(
                    "TriggerDefinition has no business key.", e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    se,
                    LogMessageIdentifier.ERROR_TRIGGER_DEFINITION_HAS_NO_BUSINESS_KEY);
            throw se;
        }
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public void deleteTriggerDefinition(long triggerKey)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException {

        this.deleteTriggerDefinitionInt(triggerKey);

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public void deleteTriggerDefinition(VOTriggerDefinition vo)
            throws ObjectNotFoundException, DeletionConstraintException,
            OperationNotPermittedException, ConcurrentModificationException {
        ArgumentValidator.notNull("trigger", vo);
        TriggerDefinition triggerDefinition = dm.getReference(
                TriggerDefinition.class, vo.getKey());
        TriggerDefinitionAssembler.verifyVersionAndKey(triggerDefinition, vo);
        this.deleteTriggerDefinitionInt(triggerDefinition.getKey());
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public void updateTriggerDefinition(VOTriggerDefinition vo)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, TriggerDefinitionDataException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("trigger", vo);

        TriggerDefinition triggerDefinition = dm.getReference(
                TriggerDefinition.class, vo.getKey());

        checkOrgAuthority(triggerDefinition);

        // check if triggertype is allowed for that organization
        if (!isAllowedTriggertype(vo.getType())) {
            throw new ValidationException();
        }

        if (vo.isSuspendProcess()) {
            Organization organization = triggerDefinition.getOrganization();
            TriggerDefinition existingTriggerDefinition = organization
                    .getSuspendingTriggerDefinition(vo.getType());
            if (existingTriggerDefinition != null
                    && vo.getKey() != existingTriggerDefinition.getKey()) {
                TriggerDefinitionDataException e = new TriggerDefinitionDataException(
                        "A suspending trigger definition does already exists for the type '"
                                + vo.getType() + "'");
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_SUSPENDING_TRIGGER_ALREADY_EXISTS_FOR_TYPE,
                        String.valueOf(vo.getType()));
                throw e;
            }
        }
        checkTriggerDefinitionChangeAllowed(vo, triggerDefinition);
        TriggerDefinitionAssembler.updateTriggerDefinition(triggerDefinition,
                vo);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public List<VOTriggerDefinition> getTriggerDefinitions() {

        return getTriggerDefinitionsForOrganizationInt(getOwnOrganization()
                .getOrganizationId());
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public VOTriggerDefinition getTriggerDefinition(Long id)
            throws ObjectNotFoundException, OperationNotPermittedException {

        if (id == null) {
            throw new ObjectNotFoundException("no key with value null");
        }

        TriggerDefinition definition = dm.getReference(TriggerDefinition.class,
                id.longValue());

        checkOrgAuthority(definition);

        return TriggerDefinitionAssembler.toVOTriggerDefinition(definition);
    }

    private Organization getOwnOrganization() {
        Organization orgOfCurrentUser = dm.getCurrentUser().getOrganization();
        return orgOfCurrentUser;

    }

    // check if triggertype is in allowed types of triggertypes for
    // organization
    private boolean isAllowedTriggertype(TriggerType triggerType) {
        Organization org = dm.getCurrentUser().getOrganization();
        Set<OrganizationRoleType> roles = org.getGrantedRoleTypes();
        for (OrganizationRoleType role : roles) {
            Set<TriggerType> allowedRoles = getTriggerTypesForRole(role);
            if (allowedRoles.contains(triggerType)) {
                return true;
            }
        }
        return false;
    }

    private List<VOTriggerDefinition> getTriggerDefinitionsForOrganizationInt(
            String organizationId) {

        Organization organization;
        try {
            organization = getOrganizationInt(organizationId);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException saasEx = new SaaSSystemException(
                    "An organization must exist");
            throw saasEx;
        }

        List<VOTriggerDefinition> result = new ArrayList<VOTriggerDefinition>();
        for (TriggerDefinition triggerDefinition : organization
                .getTriggerDefinitions()) {
            result.add(TriggerDefinitionAssembler.toVOTriggerDefinition(
                    triggerDefinition,
                    hasTriggerProcess(triggerDefinition.getKey())));
        }
        return result;
    }

    private boolean hasTriggerProcess(long triggerDefinitionKey) {

        Query query = dm
                .createNamedQuery("TriggerProcess.getAllForTriggerDefinition");
        query.setParameter("triggerDefinitionKey",
                Long.valueOf(triggerDefinitionKey));
        List<TriggerProcess> triggerProcesses = ParameterizedTypes.list(
                query.getResultList(), TriggerProcess.class);

        return !triggerProcesses.isEmpty();
    }

    private Organization getOrganizationInt(String organizationId)
            throws ObjectNotFoundException {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) dm
                .getReferenceByBusinessKey(organization);
        return organization;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "PLATFORM_OPERATOR" })
    public List<TriggerType> getTriggerTypes() {

        Organization org = this.getOwnOrganization();
        Set<OrganizationRoleType> orgRoles = org.getGrantedRoleTypes();
        Set<TriggerType> triggerTypesSet = new HashSet<TriggerType>();

        if (orgRoles != null) {
            for (OrganizationRoleType orgRole : orgRoles) {
                triggerTypesSet.addAll(getTriggerTypesForRole(orgRole));
            }
        }
        List<TriggerType> triggerTypesList = new ArrayList<TriggerType>();
        triggerTypesList.addAll(triggerTypesSet);
        return triggerTypesList;
    }

    private Set<TriggerType> getTriggerTypesForRole(OrganizationRoleType role) {
        Set<TriggerType> triggerTypes = new HashSet<TriggerType>();

        switch (role) {
        case SUPPLIER:
            triggerTypes.addAll(Arrays.asList(allowedTriggersForSupplier));
            break;
        case CUSTOMER:
            triggerTypes.addAll(Arrays.asList(allowedTriggersForCustomer));
            break;
        default:
            triggerTypes = Collections.emptySet();
        }
        return triggerTypes;
    }

    private void checkTriggerDefinitionChangeAllowed(VOTriggerDefinition vo,
            TriggerDefinition triggerDefinition)
            throws OperationNotPermittedException {
        if (!TriggerDefinitionAssembler
                .isOnlyNameChanged(vo, triggerDefinition)
                && hasTriggerProcess(triggerDefinition.getKey())) {
            OperationNotPermittedException ex = new OperationNotPermittedException(
                    "There are already trigger processes based on this trigger definition.");
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TRIGGER_MODIFICATION_FAILED);
            throw ex;
        }
    }

}
