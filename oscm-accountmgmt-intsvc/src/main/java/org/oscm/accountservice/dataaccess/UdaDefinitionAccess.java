/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess;

import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.apache.commons.lang3.StringUtils;
import org.oscm.accountservice.assembler.UdaAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * @author weiser
 * 
 */
public class UdaDefinitionAccess {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UdaDefinitionAccess.class);

    DataService ds;
    SessionContext ctx;
    LocalizerServiceLocal localizer;

    public UdaDefinitionAccess(DataService ds, SessionContext sc) {
        this.ds = ds;
        this.ctx = sc;
    }

    public UdaDefinitionAccess(DataService ds, SessionContext sc,
            LocalizerServiceLocal localizer) {
        this.ds = ds;
        this.ctx = sc;
        this.localizer = localizer;
    }

    /**
     * Returns all {@link UdaDefinition}s owned by the passed
     * {@link Organization}.
     * 
     * @param owner
     *            the owner {@link Organization}
     * @return the list of {@link UdaDefinition}s
     */
    public List<UdaDefinition> getOwnUdaDefinitions(Organization owner) {

        List<UdaDefinition> list = owner.getUdaDefinitions();

        return list;
    }

    /**
     * Returns all {@link UdaDefinition}s that are readable for the passed
     * {@link OrganizationRoleType} depending on their
     * {@link UdaConfigurationType}
     * 
     * @param supplier
     *            the supplier to get the {@link UdaDefinition}s from
     * @param role
     *            the {@link OrganizationRoleType} the definition must be
     *            readable for
     * @return
     */
    public List<UdaDefinition> getReadableUdaDefinitionsFromSupplier(
            Organization supplier, OrganizationRoleType role) {

        List<UdaDefinition> udaDefinitions = supplier
                .getReadableUdaDefinitions(role);

        return udaDefinitions;
    }

    /**
     * Tries to save the passed list of {@link VOUdaDefinition}s. Checks if the
     * passed values are valid and permitted to be accessed.
     * 
     * @param defs
     *            the {@link VOUdaDefinition}s to save
     * @param caller
     *            the calling (owning) {@link Organization}
     * @throws ValidationException
     *             in case of an invalid {@link VOUdaDefinition}
     * @throws OrganizationAuthoritiesException
     *             in case the calling {@link Organization} has insufficient
     *             roles to create {@link UdaDefinition}s of the set
     *             {@link UdaTargetType}.
     * @throws NonUniqueBusinessKeyException
     *             in case a {@link UdaDefinition} with the passed id and target
     *             type already exists for the owning {@link Organization}
     * @throws OperationNotPermittedException
     *             in case it was tries to update a {@link UdaDefinition} owned
     *             by another {@link Organization}.
     * @throws ConcurrentModificationException
     *             in case the {@link UdaDefinition} to update was concurrently
     *             changed
     * @throws ObjectNotFoundException
     *             in case on of the {@link UdaDefinition}s to update was not
     *             found
     */
    public void saveUdaDefinitions(List<VOUdaDefinition> defs,
            Organization caller)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            ConcurrentModificationException, ObjectNotFoundException {

        for (VOUdaDefinition voDef : defs) {
            // convert and validate
            UdaDefinition def;
            try {
                def = UdaAssembler.toUdaDefinition(voDef);
                def.setOrganization(caller);
            } catch (ValidationException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_INVALID_UDA_DEFINITION,
                        voDef.getUdaId());
                ctx.setRollbackOnly();
                throw e;
            }
            // check if target type is allowed for organization
            UdaTargetType type = def.getTargetType();
            if (!type.canSaveDefinition(caller.getGrantedRoleTypes())) {
                String roles = rolesToString(type.getRoles());
                OrganizationAuthoritiesException e = new OrganizationAuthoritiesException(
                        "Insufficient authorization. Required role(s) '" + roles
                                + "'.",
                        new Object[] { roles });
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        e, LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
                        Long.toString(caller.getKey()), roles);
                ctx.setRollbackOnly();
                throw e;
            }
            if (voDef.getKey() > 0) {
                updateDefinition(voDef, caller);
            } else {
                createDefinition(def);
            }
            UdaDefinition storedUda = (UdaDefinition) ds.find(def);
            if (storedUda == null) {
                return;
            }
            storeLocalizedAttributeName(storedUda.getKey(), voDef.getName(),
                    voDef.getLanguage());
        }

    }

    private void storeLocalizedAttributeName(long key, String attributeName,
            String language) {
        if (language == null) {
            return;
        }
        if (StringUtils.isBlank(attributeName)) {
            localizer.removeLocalizedValue(key,
                    LocalizedObjectTypes.CUSTOM_ATTRIBUTE_NAME, language);
        } else {
            localizer.storeLocalizedResource(language, key,
                    LocalizedObjectTypes.CUSTOM_ATTRIBUTE_NAME, attributeName);
        }
    }

    /**
     * Persists that passed {@link UdaDefinition} and checks the business key
     * uniqueness.
     * 
     * @param def
     *            the {@link UdaDefinition} to persist
     * @throws NonUniqueBusinessKeyException
     *             in case a {@link UdaDefinition} with the same id and target
     *             type exist for the owning {@link Organization}
     */
    void createDefinition(UdaDefinition def)
            throws NonUniqueBusinessKeyException {

        try {
            ds.persist(def);
        } catch (NonUniqueBusinessKeyException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_NON_UNIQUE_BUSINESS_KEY_UDA_DEFINITION);
            ctx.setRollbackOnly();
            throw e;
        }

    }

    /**
     * Updates an existing {@link UdaDefinition} - if it was not found, nothing
     * will be done. Checks if the caller is the owner, performs business key
     * uniqueness check if the id has changed and validates the passed
     * {@link VOUdaDefinition}.
     * 
     * @param voDef
     *            the updated {@link VOUdaDefinition}
     * @param owner
     *            the owning {@link Organization}
     * @throws OperationNotPermittedException
     *             in case the calling {@link Organization} is not the owner
     * @throws ValidationException
     *             in case the passed {@link VOUdaDefinition} is invalid
     * @throws ConcurrentModificationException
     *             in case the {@link UdaDefinition} to update has been changed
     *             concurrently
     * @throws NonUniqueBusinessKeyException
     *             in case the change leads to a non-unique business key
     * @throws ObjectNotFoundException
     *             in case the {@link UdaDefinition} to update was not found
     */
    void updateDefinition(VOUdaDefinition voDef, Organization owner)
            throws OperationNotPermittedException, ValidationException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        UdaDefinition existing = ds.getReference(UdaDefinition.class,
                voDef.getKey());
        PermissionCheck.owns(existing, owner, logger, ctx);
        // target type and encryption flag must not be changed as it will cause
        // inconsistencies for all depending UDAs

        voDef.setTargetType(existing.getTargetType().name());
        voDef.setEncrypted(existing.isEncrypted());

        // verify business key uniqueness
        UdaDefinition tempForUniquenessCheck = null;
        tempForUniquenessCheck = UdaAssembler.toUdaDefinition(voDef);

        tempForUniquenessCheck.setOrganization(owner);
        tempForUniquenessCheck.setKey(existing.getKey());
        try {
            ds.validateBusinessKeyUniqueness(tempForUniquenessCheck);
            UdaAssembler.updateUdaDefinition(existing, voDef);
        } catch (NonUniqueBusinessKeyException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_NON_UNIQUE_BUSINESS_KEY_UDA_DEFINITION);
            ctx.setRollbackOnly();
            throw e;
        }

    }

    /**
     * Deletes the passed list of {@link VOUdaDefinition}s - ignores the ones
     * that are not found. For the found ones, access permission and concurrent
     * modification checks will be performed.
     * 
     * @param defs
     *            the {@link VOUdaDefinition} to delete
     * @param caller
     *            the calling {@link Organization}
     * @throws OperationNotPermittedException
     * @throws ConcurrentModificationException
     */
    public void deleteUdaDefinitions(List<VOUdaDefinition> defs,
            Organization caller) throws OperationNotPermittedException,
            ConcurrentModificationException {

        for (VOUdaDefinition voDef : defs) {
            UdaDefinition existing = ds.find(UdaDefinition.class,
                    voDef.getKey());
            if (existing == null) {
                // already deleted
                continue;
            }
            PermissionCheck.owns(existing, caller, logger, ctx);
            UdaAssembler.verifyVersionAndKey(existing, voDef);
            // cascade rule will cause deletion of udas as well
            ds.remove(existing);
        }

    }

    /**
     * Converts the role set to a comma separated string
     * 
     * @param types
     *            the role set to convert
     * @return the resulting string
     */
    String rolesToString(Set<OrganizationRoleType> types) {
        String tmp = types.toString();
        return tmp.substring(1, tmp.length() - 1);
    }
}
