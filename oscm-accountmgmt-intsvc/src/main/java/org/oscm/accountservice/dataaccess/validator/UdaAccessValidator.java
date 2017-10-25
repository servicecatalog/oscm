/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess.validator;

import javax.ejb.SessionContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * @author weiser
 * 
 */
public class UdaAccessValidator {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UdaAccessValidator.class);

    DataService ds;
    SessionContext ctx;

    MandatoryUdaValidator mandatoryValidator;

    public UdaAccessValidator(DataService ds, SessionContext ctx) {
        this.ds = ds;
        this.ctx = ctx;
        mandatoryValidator = new MandatoryUdaValidator();
    }

    /**
     * Checks if the calling organization is allowed to save a {@link Uda} for
     * the passed {@link UdaDefinition}. The caller must be a supplier for UDAs
     * of type {@link UdaConfigurationType#SUPPLIER} and a customer for the
     * other types. If the caller is a supplier, it must be the supplier of the
     * referenced organization or the supplier of the referenced subscriptions
     * owner.If the caller is a customer, it must be the referenced organization
     * or the owner of the referenced subscription.
     * 
     * @param def
     *            the {@link UdaDefinition} to save a {@link Uda} for
     * @param caller
     *            the calling {@link Organization}
     * @param targetObjectKey
     *            the key of the target object - depending on
     *            {@link UdaDefinition#getTargetType()} either a customer
     *            {@link Organization} or {@link Subscription}
     * @throws OperationNotPermittedException
     *             in case the caller is not allowed to access the target or the
     *             {@link UdaDefinition}
     * @throws ObjectNotFoundException
     *             if the customer {@link Organization} or {@link Subscription}
     *             referenced by the target object key wasn't found
     */
    public void canSaveUda(UdaDefinition def, Organization caller,
            long targetObjectKey) throws OperationNotPermittedException,
            ObjectNotFoundException {
        Organization customer = getTargetOrganizationReference(
                def.getTargetType(), targetObjectKey);
        checkWritePermission(def, caller, customer);
    }

    /**
     * Returns the target {@link Organization} or the target
     * {@link Subscription}s owner {@link Organization} if found.
     * 
     * @param type
     *            the target type defining if an {@link Organization} or a
     *            {@link Subscription} has to be read
     * @param targetObjectKey
     *            the target entities key
     * @return the read {@link Organization} or the {@link Subscription}s owning
     *         {@link Organization}
     * @throws ObjectNotFoundException
     *             in case the target is not found
     * @see #findTargetOrganization(UdaTargetType, long)
     */
    Organization getTargetOrganizationReference(UdaTargetType type,
            long targetObjectKey) throws ObjectNotFoundException {
        Organization customer = null;
        LogMessageIdentifier logId = LogMessageIdentifier.WARN_INEXISTENT_CUSTOMER;
        try {
            switch (type) {
            case CUSTOMER:
                customer = ds.getReference(Organization.class, targetObjectKey);
                break;
            case CUSTOMER_SUBSCRIPTION:
                logId = LogMessageIdentifier.WARN_INEXISTENT_SUBSCRIPTION;
                Subscription subscription = ds.getReference(Subscription.class,
                        targetObjectKey);
                customer = subscription.getOrganization();
                break;
            }
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e, logId,
                    String.valueOf(targetObjectKey));
            ctx.setRollbackOnly();
            throw e;
        }
        return customer;
    }

    /**
     * Checks if the passed {@link Uda} can be deleted by the calling
     * organization.
     * 
     * @param uda
     *            the {@link Uda} to delete
     * @param caller
     *            the calling organization
     * @throws OperationNotPermittedException
     *             in case the caller is not permitted to access the target
     *             entity
     * @throws MandatoryUdaMissingException
     *             in case the {@link Uda} cannot be deleted because its's
     *             mandatory
     */
    public void canDeleteUda(Uda uda, Organization caller)
            throws OperationNotPermittedException, MandatoryUdaMissingException {
        UdaDefinition def = uda.getUdaDefinition();
        Organization targetOrg = findTargetOrganization(def.getTargetType(),
                uda.getTargetObjectKey());
        if (targetOrg != null) {
            checkWritePermission(def, caller, targetOrg);
            mandatoryValidator.checkMandatory(def);
        }
        // if we don't find the referenced object anymore, the UDA can be
        // deleted
    }

    /**
     * Returns the target {@link Organization} or the target
     * {@link Subscription}s owner {@link Organization} if found. If it is not
     * found, <code>null</code> will be returned.
     * 
     * @param type
     *            the {@link UdaTargetType}
     * @param targetObjectKey
     *            the target object key
     * @return the {@link Organization} or <code>null</code>
     * @see #getTargetOrganizationReference(UdaTargetType, long)
     */
    Organization findTargetOrganization(UdaTargetType type, long targetObjectKey) {
        Organization result = null;
        switch (type) {
        case CUSTOMER:
            result = ds.find(Organization.class, targetObjectKey);
            break;
        case CUSTOMER_SUBSCRIPTION:
            Subscription sub = ds.find(Subscription.class, targetObjectKey);
            if (sub != null) {
                result = sub.getOrganization();
            }
            break;
        }
        return result;
    }

    /**
     * Checks if the calling organization can write (create/update/delete) an
     * {@link Uda} for the passed {@link UdaDefinition}.
     * 
     * @param def
     *            the {@link UdaDefinition} to create a {@link Uda} for
     * @param caller
     *            the calling organization
     * @param customer
     *            the {@link Organization} being (in case of
     *            {@link UdaTargetType#CUSTOMER} or owning (in case of
     *            {@link UdaTargetType#CUSTOMER_SUBSCRIPTION} the target entity
     * @throws OperationNotPermittedException
     *             if the caller is not the supplier of the customer and not
     *             owner of the {@link UdaDefinition} or if the caller is not
     *             the customer - depends on {@link UdaConfigurationType}
     */
    void checkWritePermission(UdaDefinition def, Organization caller,
            Organization customer) throws OperationNotPermittedException {
        UdaConfigurationType configType = def.getConfigurationType();
        if (configType.canWrite(OrganizationRoleType.CUSTOMER)) {
            // if the passed organization can write the UDA, it must be the
            // customer
            PermissionCheck.same(caller, customer, logger, ctx);
        } else {
            // otherwise it must be its supplier - then it must be its
            // definition...
            PermissionCheck.owns(def, caller, logger, ctx);
            // ...and its customer
            PermissionCheck.supplierOfCustomer(caller, customer, logger, ctx);
        }
    }

    /**
     * Checks if the target entity exists and if the calling
     * {@link Organization} is the supplier, broker or reseller of the
     * customer/subscription referenced by the target entity.
     * 
     * @param seller
     *            the seller {@link Organization}
     * @param type
     *            the {@link UdaTargetType} to get the {@link Uda}s for
     * @param targetObjectKey
     *            the target object key
     * @throws OperationNotPermittedException
     *             in case the caller is not the supplier
     * @throws ObjectNotFoundException
     *             in case the target object wasn't found
     */
    public void checkSellerReadPermission(Organization seller,
            UdaTargetType type, long targetObjectKey)
            throws OperationNotPermittedException, ObjectNotFoundException {
        Organization org = getTargetOrganizationReference(type, targetObjectKey);
        PermissionCheck.sellerOfCustomer(seller, org, logger, ctx);
    }

    /**
     * Checks if the target entity exists and if the calling
     * {@link Organization} is the owner of the customer/subscription referenced
     * by the target entity.
     * 
     * @param customer
     *            the customer {@link Organization}
     * @param type
     *            the {@link UdaTargetType} to get the {@link Uda}s for
     * @param targetObjectKey
     *            the target object key
     * @throws OperationNotPermittedException
     *             in case the caller is not the owner
     * @throws ObjectNotFoundException
     *             in case the target object wasn't found
     */
    public void checkCustomerReadPermission(Organization customer,
            UdaTargetType type, long targetObjectKey)
            throws OperationNotPermittedException, ObjectNotFoundException {
        Organization org = getTargetOrganizationReference(type, targetObjectKey);
        PermissionCheck.same(customer, org, logger, ctx);
    }
}
