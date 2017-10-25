/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess;

import java.util.EnumSet;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.oscm.accountservice.assembler.UdaAssembler;
import org.oscm.accountservice.dataaccess.validator.MandatoryUdaValidator;
import org.oscm.accountservice.dataaccess.validator.UdaAccessValidator;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.vo.BaseAssembler;

/**
 * @author weiser
 * 
 */
public class UdaAccess {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UdaAccess.class);

    DataService ds;
    SessionContext ctx;

    MandatoryUdaValidator mandatoryUdaValidator;
    UdaAccessValidator udaAccessValidator;

    public UdaAccess(DataService ds, SessionContext sc) {
        this.ds = ds;
        this.ctx = sc;
        mandatoryUdaValidator = new MandatoryUdaValidator();
        udaAccessValidator = new UdaAccessValidator(ds, sc);
    }

    /**
     * Saves the passed list of {@link VOUda}s - the ones with
     * <code>{@link VOUda#getUdaValue()} == null</code> will be deleted; the
     * ones with <code>{@link VOUda#getKey()} &lt;= 0<code> will be created.
     * Other ones will be read and updated if possible.
     * 
     * @param udas
     *            the {@link VOUda}s to save
     * @param caller
     *            the calling {@link Organization}
     * @throws ValidationException
     *             in case of an invalid {@link VOUda}
     * @throws MandatorytionUdaMissingException
     *             if a mandatory UDA is tried to be deleted
     * @throws OperationNotPermittedException
     *             if the access to the {@link UdaDefinition} or the target
     *             {@link Organization} or {@link Subscription} is not permitted
     * @throws ObjectNotFoundException
     *             if a {@link Uda} to update, a {@link UdaDefinition} or the
     *             target {@link Organization} or {@link Subscription} cannot be
     *             found
     * @throws NonUniqueBusinessKeyException
     *             if a {@link Uda} should be created of a {@link UdaDefinition}
     *             that is already created for the target object
     * @throws ConcurrentModificationException
     *             if the {@link UdaDefinition} or the {@link Uda} to
     *             delete/update has been changed concurrently
     */
    public void saveUdas(List<VOUda> udas, Organization caller)
            throws ValidationException, MandatoryUdaMissingException,
            OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, ConcurrentModificationException {

        for (VOUda voUda : udas) {
            if (voUda.getUdaValue() == null) {
                deleteUda(voUda, caller);
                continue;
            }
            updateOrCreate(caller, voUda);
        }
    }

    private void updateOrCreate(Organization caller, VOUda voUda)
            throws ConcurrentModificationException,
            OperationNotPermittedException, ValidationException,
            NonUniqueBusinessKeyException, ObjectNotFoundException {
        final VOUdaDefinition voUdaDef = voUda.getUdaDefinition();
        try {
            UdaDefinition def = ds.getReference(UdaDefinition.class,
                    voUdaDef.getKey());
            BaseAssembler.verifyVersionAndKey(def, voUda.getUdaDefinition());
            udaAccessValidator.canSaveUda(def, caller,
                    voUda.getTargetObjectKey());
            if (voUda.getKey() > 0) {
                updateUda(voUda, def, caller);
            } else {
                Uda uda = UdaAssembler.toUdaWithDefinition(voUda, def);
                createUda(def, uda);
            }
        } catch (ObjectNotFoundException onfe) {
            if (onfe.getDomainObjectClassEnum() == ClassEnum.UDA_DEFINITION) {
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        voUdaDef);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                        LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION,
                        voUdaDef.getClass().getSimpleName());
                throw cme;
            }
            throw onfe;
        } catch (ValidationException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INVALID_UDA,
                    ((voUda.getUdaDefinition() == null) ? null
                            : voUda.getUdaDefinition().getUdaId()));
            ctx.setRollbackOnly();
            throw e;
        }
    }

    /**
     * Persists the passed {@link Uda} after setting the passed
     * {@link UdaDefinition}.
     * 
     * @param def
     *            the {@link UdaDefinition} the passed {@link Uda} is created
     *            for
     * @param uda
     *            the {@link Uda} to persist
     * @throws NonUniqueBusinessKeyException
     *             in case a {@link Uda} of the passed {@link UdaDefinition}
     *             already exists for the target object
     */
    void createUda(UdaDefinition def, Uda uda)
            throws NonUniqueBusinessKeyException {
        uda.setUdaDefinition(def);
        try {
            ds.persist(uda);
        } catch (NonUniqueBusinessKeyException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_NON_UNIQUE_BUSINESS_KEY_UDA);
            ctx.setRollbackOnly();
            throw e;
        }
    }

    /**
     * Updates an existing {@link Uda} with the value from the passed
     * {@link VOUda}.
     * 
     * @param voUda
     *            the {@link VOUda} with the value to update
     * @param def
     *            the existing {@link UdaDefinition} as defined by the passed
     *            {@link VOUda}
     * @param caller
     *            the calling {@link Organization}
     * @throws ObjectNotFoundException
     *             in case the existing {@link Uda} cannot be found
     * @throws ValidationException
     *             in case the passed {@link VOUda} is invalid
     * @throws ConcurrentModificationException
     *             in case the existing {@link Uda} has been changed
     *             concurrently
     * @throws OperationNotPermittedException
     *             in case the target object key is tried to be changed or if
     *             the {@link UdaDefinition} of the existing {@link Uda} is a
     *             different one than the one passed on the {@link VOUda}
     */
    void updateUda(VOUda voUda, UdaDefinition def, Organization caller)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException {
        Uda existing = null;
        try {
            existing = ds.getReference(Uda.class, voUda.getKey());
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INEXISTENT_UDA,
                    String.valueOf(voUda.getKey()));
            ctx.setRollbackOnly();
            throw e;
        }
        // permission check - is the definition of the existing uda the
        // same as provided as input?
        if (def.getKey() != existing.getUdaDefinition().getKey()) {
            String message = "Organization '%s' tried to modify UDA '%s' with an invalid UDA defintion.";
            message = String.format(message, caller.getOrganizationId(),
                    Long.valueOf(existing.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_WRONG_UDA_DEFINITION,
                    caller.getOrganizationId(),
                    String.valueOf(existing.getKey()));
            ctx.setRollbackOnly();
            throw e;
        }
        // target key must be the same otherwise the UDA belongs to a different
        // organization/subscription
        PermissionCheck.sameUdaTarget(caller, existing,
                voUda.getTargetObjectKey(), logger, ctx);
        UdaAssembler.updateUda(existing, voUda);
    }

    /**
     * Tries to delete an existing {@link Uda} - if it doesn't exist, nothing
     * will be done. If it exists, it will be checked if the caller is permitted
     * to do so and if it cannot be deleted because it's mandatory.
     * 
     * @param voUda
     *            the {@link VOUda} to delete
     * @param caller
     *            the calling {@link Organization}
     * @throws MandatoryUdaMissingException
     *             if it is a mandatory UDA
     * @throws OperationNotPermittedException
     *             in case the caller is not permitted to access the target
     *             object
     * @throws ConcurrentModificationException
     *             in case the {@link Uda} has been concurrently changed
     */
    void deleteUda(VOUda voUda, Organization caller)
            throws MandatoryUdaMissingException, OperationNotPermittedException,
            ConcurrentModificationException {
        Uda existing = ds.find(Uda.class, voUda.getKey());
        if (existing != null) {
            udaAccessValidator.canDeleteUda(existing, caller);
            mandatoryUdaValidator.checkMandatory(existing.getUdaDefinition());
            UdaAssembler.verifyVersionAndKey(existing, voUda);
            ds.remove(existing);
        }
    }

    /**
     * Saves the passed list of {@link VOUda}s in the context of the given
     * {@link Subscription}. depending on the {@link UdaTargetType} the target
     * object keys will be adapted to fit the {@link Organization} or
     * {@link Subscription}. Additionally it will be checked if all mandatory
     * {@link Uda}s are passed or already existing.
     * 
     * @param udas
     *            the {@link VOUda}s to save
     * @param supplier
     *            the supplier of the {@link Subscription}s product
     * @param sub
     *            the context {@link Subscription}
     * @throws MandatoryUdaMissingException
     *             if a mandatory UDA is neither passed nor existing
     * @throws ObjectNotFoundException
     *             in case the target entity doesn't exist (only theoretically
     *             possible)
     * @throws NonUniqueBusinessKeyException
     *             in case a UDA of the same definition already exists for the
     *             target object
     * @throws ValidationException
     *             in case of an invalid {@link VOUda}
     * @throws OperationNotPermittedException
     *             if the caller is not permitted to access the target object
     * @throws ConcurrentModificationException
     *             in case a UDA to update has been changed concurrently
     */
    public void saveUdasForSubscription(List<VOUda> udas, Organization supplier,
            Subscription sub) throws MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {
        validateUdaAndAdaptTargetKey(udas, supplier, sub);
        saveUdas(udas, sub.getOrganization());
    }

    /**
     * Validate the passed list of {@link VOUda}s in the context of the given
     * {@link Subscription}. depending on the {@link UdaTargetType} the target
     * object keys will be adapted to fit the {@link Organization} or
     * {@link Subscription}. Additionally it will be checked if all mandatory
     * {@link Uda}s are passed or already existing.
     * 
     * @param udas
     *            the {@link VOUda}s to save
     * @param supplier
     *            the supplier of the {@link Subscription}s product
     * @param sub
     *            the context {@link Subscription}
     * @throws MandatoryCustomerUdaMissingException
     *             if a mandatory customer UDA is neither passed nor existing
     * @throws MandatorySubscriptionUdaMissingException
     *             if a mandatory subscription UDA is neither passed nor
     *             existing
     * @throws ValidationException
     *             in case of an invalid {@link VOUda}
     */
    public void validateUdaAndAdaptTargetKey(List<VOUda> udas,
            Organization supplier, Subscription sub)
            throws MandatoryCustomerUdaMissingException,
            MandatoryUdaMissingException, ValidationException {
        // validate udas and adapt target keys
        for (VOUda voUda : udas) {
            // temporarily set the target key to a valid value (if 0 is passed
            // for new subscriptions) so that it validates
            voUda.setTargetObjectKey(1);

            UdaAssembler.validate(voUda);
            UdaTargetType type = UdaAssembler
                    .toUdaTargetType(voUda.getUdaDefinition().getTargetType());

            // now depending on the target type adapt the target keys
            switch (type) {
            case CUSTOMER:
                voUda.setTargetObjectKey(sub.getOrganizationKey());
                break;
            case CUSTOMER_SUBSCRIPTION:
                voUda.setTargetObjectKey(sub.getKey());
                break;
            }
        }
        mandatoryUdaValidator.checkAllRequiredUdasPassed(
                supplier.getMandatoryUdaDefinitions(),
                getExistingUdas(sub.getOrganization().getKey(), sub.getKey(),
                        supplier),
                udas);
    }

    /**
     * Reads all {@link Uda}s with the passed organization or subscription
     * target key and the according {@link UdaTargetType} that are accessible by
     * customers and their definitions owned by the passed supplier
     * {@link Organization}.
     * 
     * @param customerKey
     *            the organization target key
     * @param subscriptionKey
     *            the subscription target key
     * @param supplier
     *            the supplier {@link Organization}
     * @return the existing list of {@link Uda}s
     */
    public List<Uda> getExistingUdas(long customerKey, long subscriptionKey,
            Organization supplier) {
        Query q = ds.createNamedQuery("Uda.getAllForCustomerBySupplier");
        q.setParameter("subKey", Long.valueOf(subscriptionKey));
        q.setParameter("subType", UdaTargetType.CUSTOMER_SUBSCRIPTION);
        q.setParameter("custKey", Long.valueOf(customerKey));
        q.setParameter("custType", UdaTargetType.CUSTOMER);
        q.setParameter("configTypes",
                EnumSet.of(UdaConfigurationType.USER_OPTION_MANDATORY,
                        UdaConfigurationType.USER_OPTION_OPTIONAL));
        q.setParameter("supplierKey", Long.valueOf(supplier.getKey()));
        return ParameterizedTypes.list(q.getResultList(), Uda.class);
    }

    /**
     * Reads the existing {@link Uda}s for the passed target object key and
     * {@link UdaTargetType} for {@link UdaDefinition} created by the passed
     * supplier {@link Organization}. Checks if the seller can access the
     * referenced target objects.
     * 
     * @param targetObjectKey
     *            the target object key
     * @param type
     *            the {@link UdaTargetType}
     * @param seller
     *            the seller {@link Organization}
     * @param checkSeller
     *            boolean flag if the organization specified with the
     *            targetObjectKey is a customer of the seller organization.
     * @return the list of existing {@link Uda}s
     * @throws OperationNotPermittedException
     *             in case the supplier is not a supplier of the referenced
     *             target {@link Organization} or {@link Subscription}.
     * @throws ObjectNotFoundException
     *             if the referenced target {@link Organization} or
     *             {@link Subscription} wasn't found
     */
    public List<Uda> getUdasForTypeAndTarget(long targetObjectKey,
            UdaTargetType type, Organization seller, boolean checkSeller)
            throws OperationNotPermittedException, ObjectNotFoundException {
        if (checkSeller) {
            udaAccessValidator.checkSellerReadPermission(seller, type,
                    targetObjectKey);
        }
        return getUdas(targetObjectKey, type, seller);
    }

    /**
     * Reads the existing {@link Uda}s for the passed target object key and
     * {@link UdaTargetType} for {@link UdaDefinition} created by the passed
     * supplier {@link Organization}.
     * 
     * @param targetObjectKey
     *            the target object key
     * @param type
     *            the {@link UdaTargetType}
     * @param supplier
     *            the supplier {@link Organization}
     * @return the list of existing {@link Uda}s
     */
    List<Uda> getUdas(long targetObjectKey, UdaTargetType type,
            Organization supplier) {
        Query query = ds.createNamedQuery("Uda.getByTypeAndKeyForSupplier");
        query.setParameter("targetKey", Long.valueOf(targetObjectKey));
        query.setParameter("targetType", type);
        query.setParameter("supplierKey", Long.valueOf(supplier.getKey()));
        return ParameterizedTypes.list(query.getResultList(), Uda.class);
    }

    /**
     * Reads the existing {@link Uda}s for the passed target object key and
     * {@link UdaTargetType} for {@link UdaDefinition} created by the passed
     * supplier {@link Organization}. Checks if the customer
     * {@link Organization} can access the referenced target objects.
     * 
     * @param targetObjectKey
     *            the target object key
     * @param type
     *            the {@link UdaTargetType}
     * @param supplier
     *            the supplier {@link Organization}
     * @param customer
     *            the customer {@link Organization}
     * @return the list of existing {@link Uda}s
     * @throws OperationNotPermittedException
     *             in case the customer is not the referenced target
     *             {@link Organization} or owner of the referenced
     *             {@link Subscription}.
     * @throws ObjectNotFoundException
     *             if the referenced target {@link Organization} or
     *             {@link Subscription} wasn't found
     */
    public List<Uda> getUdasForTypeTargetAndCustomer(long targetObjectKey,
            UdaTargetType type, Organization supplier, Organization customer)
            throws ObjectNotFoundException, OperationNotPermittedException {
        udaAccessValidator.checkCustomerReadPermission(customer, type,
                targetObjectKey);
        List<Uda> result = null;
        switch (type) {
        case CUSTOMER:
            result = getExistingUdas(targetObjectKey, -1, supplier);
            break;
        case CUSTOMER_SUBSCRIPTION:
            result = getExistingUdas(-1, targetObjectKey, supplier);
            break;
        }
        return result;

    }
}
