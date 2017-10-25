/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.07.2011                                                      
 *                                                                              
 *  Completion Time: 12.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.permission;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException;
import org.oscm.internal.vo.VOSubscription;

/**
 * @author weiser
 * 
 */
public class PermissionCheck {

    private PermissionCheck() {

    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link BillingContact} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param bc
     *            the {@link BillingContact} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @throws OperationNotPermittedException
     */
    public static void owns(BillingContact bc, Organization org,
            Log4jLogger logger) throws OperationNotPermittedException {
        if (bc.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access billing contact '%s' that is owned by a different organization.",
                            org.getOrganizationId(), Long.valueOf(bc.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_BILLING_CONTACT_ACCESS,
                        org.getOrganizationId(), String.valueOf(bc.getKey()));
            }
            throw e;
        }
    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link PaymentInfo} and throws an {@link OperationNotPermittedException}
     * if this is not the case.
     * 
     * @param pi
     *            the {@link PaymentInfo} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @throws OperationNotPermittedException
     */
    public static void owns(PaymentInfo pi, Organization org, Log4jLogger logger)
            throws OperationNotPermittedException {
        if (pi.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access payment info '%s' that is owned by a different organization",
                            org.getOrganizationId(), Long.valueOf(pi.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_PAYMENT_INFO_ACCESS,
                        org.getOrganizationId(), String.valueOf(pi.getKey()));
            }
            throw e;
        }
    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link Subscription} and throws an {@link OperationNotPermittedException}
     * if this is not the case.
     * 
     * @param sub
     *            the {@link Subscription} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @throws OperationNotPermittedException
     */
    public static void owns(Subscription sub, Organization org,
            Log4jLogger logger) throws OperationNotPermittedException {
        if (sub.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access subscription '%s' that is owned by a different organization",
                            org.getOrganizationId(), Long.valueOf(sub.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_SUBSCRIPTION_ACCESS,
                        org.getOrganizationId(), String.valueOf(sub.getKey()));
            }
            throw e;
        }
    }

    /**
     * Checks if the provided {@link PlatformUser} is the owner of the provided
     * {@link Subscription}, is administrator of unit assigned to subscription
     * or  organization administrator. In case the user is not administrator,
     * unit administrator and not subscription owner, the method throws an
     * {@link OperationNotPermittedException}.
     * 
     * @param subscription
     *            the {@link Subscription} to check the ownership for
     * @param currentUser
     *            the {@link PlatformUser} to check if it is the owner
     * @param administratedUserGroups
     *            the {@link List<UserGroup>} contains all units administrated
     *            by current user
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @throws OperationNotPermittedException
     */
    public static void owns(Subscription subscription,
            PlatformUser currentUser, List<UserGroup> administratedUserGroups,
            Log4jLogger logger) throws OperationNotPermittedException {

        if (currentUser.isOrganizationAdmin()) {
            Organization customer = subscription.getOrganization();
            if (currentUser.getOrganization().getKey() == customer.getKey()) {
                return;
            }
        }

        if (currentUser.equals(subscription.getOwner())) {
            return;
        }

        if (administratedUserGroups != null
                && administratedUserGroups
                        .contains(subscription.getUserGroup())) {
            return;
        }

        String message = String
                .format("Current user %s is not organization admin or owner of subscription %s",
                        currentUser.getUserId(),
                        subscription.getSubscriptionId());
        OperationNotPermittedException onp = new OperationNotPermittedException(
                message);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                LogMessageIdentifier.WARN_SUBSCRIPTION_OWNERSHIP_CHECK_FAILED);
        throw onp;

    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link UdaDefinition} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param def
     *            the {@link UdaDefinition} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void owns(UdaDefinition def, Organization org,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (def.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access uda definition '%s' that is owned by a different organization",
                            org.getOrganizationId(), Long.valueOf(def.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_UDA_DEFINITION_ACCESS,
                        org.getOrganizationId(), String.valueOf(def.getKey()));
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided supplier {@link Organization} is supplier of the
     * provided customer {@link Organization} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param sup
     *            the {@link Organization} to check if it is supplier of the
     *            passed customer {@link Organization}
     * @param cust
     *            the {@link Organization} to check if it is customer of the
     *            passed supplier {@link Organization}
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void supplierOfCustomer(Organization sup, Organization cust,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        List<Organization> customers = sup.getCustomersOfSupplier();
        if (!customers.contains(cust)) {
            String message = String.format(
                    "Organization '%s' is not supplier of customer '%s'",
                    sup.getOrganizationId(), cust.getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_NO_SUPPLIER_OF_CUSTOMER,
                        sup.getOrganizationId(), cust.getOrganizationId());
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided reseller {@link Organization} is a broker of the
     * provided customer {@link Organization} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param broker
     *            the {@link Organization} to check if it is a broker of the
     *            passed customer {@link Organization}
     * @param cust
     *            the {@link Organization} to check if it is customer of the
     *            passed broker {@link Organization}
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void brokerOfCustomer(Organization broker, Organization cust,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        List<Organization> customers = broker.getCustomersOfBroker();
        if (!customers.contains(cust)) {
            String message = String.format(
                    "Organization '%s' is not broker of customer '%s'",
                    broker.getOrganizationId(), cust.getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_NO_BROKER_OF_CUSTOMER,
                        broker.getOrganizationId(), cust.getOrganizationId());
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided reseller {@link Organization} is reseller of the
     * provided customer {@link Organization} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param reseller
     *            the {@link Organization} to check if it is reseller of the
     *            passed customer {@link Organization}
     * @param cust
     *            the {@link Organization} to check if it is customer of the
     *            passed reseller {@link Organization}
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void resellerOfCustomer(Organization reseller,
            Organization cust, Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        List<Organization> customers = reseller.getCustomersOfReseller();
        if (!customers.contains(cust)) {
            String message = String.format(
                    "Organization '%s' is not reseller of customer '%s'",
                    reseller.getOrganizationId(), cust.getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_NO_RESELLER_OF_CUSTOMER,
                        reseller.getOrganizationId(), cust.getOrganizationId());
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided seller {@link Organization} is supplier, broker or
     * reseller of the provided customer {@link Organization} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param seller
     *            the {@link Organization} to check if it is seller of the
     *            passed customer {@link Organization}
     * @param cust
     *            the {@link Organization} to check if it is customer of the
     *            passed seller {@link Organization}
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void sellerOfCustomer(Organization seller, Organization cust,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (seller.hasRole(OrganizationRoleType.BROKER)) {
            brokerOfCustomer(seller, cust, logger, context);
        } else if (seller.hasRole(OrganizationRoleType.RESELLER)) {
            resellerOfCustomer(seller, cust, logger, context);
        } else {
            supplierOfCustomer(seller, cust, logger, context);
        }
    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link Marketplace} and throws an {@link OperationNotPermittedException}
     * if this is not the case.
     * 
     * @param mp
     *            the {@link Marketplace} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void owns(Marketplace mp, Organization org,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (mp.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access marketplace '%s' that is owned by a different organization",
                            org.getOrganizationId(), Long.valueOf(mp.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_MARKETPLACE_ACCESS,
                        org.getOrganizationId(), String.valueOf(mp.getKey()));
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link Product} and throws an {@link OperationNotPermittedException} if
     * this is not the case.
     * 
     * @param prod
     *            the {@link Product} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void owns(Product prod, Organization org, Log4jLogger logger,
            SessionContext context) throws OperationNotPermittedException {
        if (prod.getVendor() != org) {
            String message = String
                    .format("Organization '%s' tried to access service '%s' that is owned by a different organization",
                            org.getOrganizationId(),
                            Long.valueOf(prod.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_SERVICE_ACCESS,
                        org.getOrganizationId(), String.valueOf(prod.getKey()));
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided supplier {@link Organization} owns the price model
     * of the provided {@link Product} and throws an
     * {@link OperationNotPermittedException} if this is not the case. ndor
     * 
     * @param product
     *            the {@link Product} to check the ownership for
     * @param supplier
     *            the supplier {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void ownsPriceModel(Product product, Organization supplier,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        // If the product is a broker subscription copy, ensure that the
        // product template belongs to the supplier. Otherwise ensure that
        // the product itself belongs to the supplier.
        if (product.getType().equals(ServiceType.PARTNER_SUBSCRIPTION)
                && product.getVendor().getGrantedRoleTypes()
                        .contains(OrganizationRoleType.BROKER)) {
            owns(product.getProductTemplate(), supplier, logger, context);
        } else {
            owns(product, supplier, logger, context);
        }
    }

    /**
     * Checks if the provided {@link Organization} is the owner of the provided
     * {@link TechnicalProduct} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param tp
     *            the {@link TechnicalProduct} to check the ownership for
     * @param org
     *            the {@link Organization} to check if it is the owner
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     */
    public static void owns(TechnicalProduct tp, Organization org,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (tp.getOrganization() != org) {
            String message = String
                    .format("Organization '%s' tried to access technical service '%s' that is owned by a different organization",
                            org.getOrganizationId(), Long.valueOf(tp.getKey()));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_TECH_SERVICE_ACCESS,
                        org.getOrganizationId(), String.valueOf(tp.getKey()));
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    /**
     * Checks if the provided supplier {@link Organization} is allowed to
     * publish services on the provided {@link Marketplace} For non-opened
     * marketplaces a {@link MarketplaceToOrganization} connecting both with
     * publishing access granted must exist. If not, a
     * {@link OperationNotPermittedException} will be thrown. For open
     * marketplaces it is allowed to publish if a
     * {@link MarketplaceToOrganization} with publish access denied does not
     * exist. If it exists, a {@link OperationNotPermittedException} will be
     * thrown.
     * 
     * @param mp
     *            the {@link Marketplace} to publish on
     * @param sup
     *            the supplier {@link Organization} that wants to publish
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @param context
     *            if not <code>null</code>,
     *            {@link SessionContext#setRollbackOnly()} will called.
     * @throws OperationNotPermittedException
     *             in case the supplier is not allowed to publish services on
     *             the provided marketplace
     */
    public static void canPublish(Marketplace mp, Organization sup,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {

        List<MarketplaceToOrganization> list = mp
                .getMarketplaceToOrganizations();
        boolean denied = false;
        for (MarketplaceToOrganization mto : list) {
            if (sup == mto.getOrganization()
                    && PublishingAccess.PUBLISHING_ACCESS_GRANTED.equals(mto
                            .getPublishingAccess())) {
                return;
            }
            if (sup == mto.getOrganization()
                    && PublishingAccess.PUBLISHING_ACCESS_DENIED.equals(mto
                            .getPublishingAccess())) {
                denied = true;
                break;
            }
        }
        if (!denied && mp.isOpen()) {
            return;
        }
        String message = String
                .format("Organization '%s' tried to publish on marketplace '%s' but is not allowed.",
                        sup.getOrganizationId(), mp.getMarketplaceId());
        PublishingToMarketplaceNotPermittedException e = new PublishingToMarketplaceNotPermittedException(
                message);
        if (logger != null) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_PUBLISH_ON_MARKETPLACE,
                    sup.getOrganizationId(), mp.getMarketplaceId());
        }
        if (context != null) {
            context.setRollbackOnly();
        }
        throw e;
    }

    /**
     * Checks if the supplier has been granted the permission to sell the
     * technical product - a corresponding marketing permission must exist.
     * 
     * @param technicalProduct
     *            the permission check is done against this technical product
     * @param supplier
     *            for which the permission check is done
     * @param ds
     *            data service, used to execute sql queries
     * @param logger
     *            if not <code>null</code> a thrown
     *            <code>ObjectNotFoundException</code> will be logged as warning
     *            to the system log
     * @throws OperationNotPermittedException
     *             thrown if no or multiple marketing permissions are found.
     */
    public static void hasMarketingPermission(
            TechnicalProduct technicalProduct, Organization supplier,
            DataService ds, Log4jLogger logger)
            throws OperationNotPermittedException {

        Query query = ds
                .createNamedQuery("MarketingPermission.findForSupplierIds");
        query.setParameter("tp", technicalProduct);
        List<String> searchList = new ArrayList<>();
        searchList.add(supplier.getOrganizationId());
        query.setParameter("orgIds", searchList);
        query.setParameter("refType",
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);

        try {
            query.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            logAndThrowMarketingPermissionException(logger,
                    String.valueOf(technicalProduct.getKey()),
                    supplier.getOrganizationId());
        }
    }

    public static void same(Organization org1, Organization org2,
            Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (org1 != org2) {
            String message = String
                    .format("Organization '%s' tried to access organization '%s' but is not allowed to.",
                            org1.getOrganizationId(), org2.getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_INSUFFICIENT_AUTH_BY_ORGANIZATION_ACCESS,
                        org1.getOrganizationId(), org2.getOrganizationId());
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    public static void sameUdaTarget(Organization caller, Uda uda,
            long targetKey, Log4jLogger logger, SessionContext context)
            throws OperationNotPermittedException {
        if (uda.getTargetObjectKey() != targetKey) {
            String message = String
                    .format("Organization '%s' tried to change uda '%s' from target '%s' to target '%s'.",
                            caller.getOrganizationId(),
                            Long.toString(uda.getKey()),
                            Long.toString(uda.getTargetObjectKey()),
                            Long.toString(targetKey));
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_UNPERMITTED_UDA_TARGET_SWITCH,
                        caller.getOrganizationId(),
                        Long.toString(uda.getKey()),
                        Long.toString(uda.getTargetObjectKey()),
                        Long.toString(targetKey));
            }
            if (context != null) {
                context.setRollbackOnly();
            }
            throw e;
        }
    }

    private static void logAndThrowMarketingPermissionException(
            Log4jLogger logger, String tpKey, String orgId)
            throws OperationNotPermittedException {

        OperationNotPermittedException onfe = new OperationNotPermittedException(
                String.format(
                        "MarketingPermission for technical service '%s' and supplier with identifer '%s' does not exist.",
                        tpKey, orgId));

        if (logger != null) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_FOUND,
                    tpKey, orgId);
        }

        throw onfe;
    }

    /**
     * Checks if the {@link Organization} of the calling {@link PlatformUser} is
     * the owner of the provided {@link PlatformUser} and throws an
     * {@link OperationNotPermittedException} if this is not the case.
     * 
     * @param caller
     *            the calling {@link PlatformUser} to check if the other user
     *            belongs to its {@link Organization}
     * @param u
     *            the {@link PlatformUser} to check if it belongs to the same
     *            {@link Organization} as the caller
     * @param logger
     *            the optional logger - if not <code>null</code> it logs the
     *            created exception as warning to the system log
     * @throws OperationNotPermittedException
     */
    public static void sameOrg(PlatformUser caller, PlatformUser u,
            Log4jLogger logger) throws OperationNotPermittedException {
        if (u.getOrganization() != caller.getOrganization()) {
            String message = String
                    .format("User '%s' does not belong to organization '%s'.",
                            u.getUserId(), caller.getOrganization()
                                    .getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            if (logger != null) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        e, LogMessageIdentifier.WARN_USER_ACCESS_USER_FAILED,
                        caller.getUserId(), u.getUserId());
            }
            throw e;
        }
    }

    /**
     *
     * @param subscription new values
     * @param subFromDB old values
     * @param currentUser user who invokes the method
     * @return True if user can proceed with owner modification or not
     */
    public static boolean shouldWeProceedWithUpdatingSubscription(VOSubscription subscription, Subscription subFromDB, PlatformUser currentUser) {
        boolean isOrganizationAdmin = currentUser.isOrganizationAdmin();
        boolean isUnitAdmin = currentUser.isUnitAdmin();
        boolean isOwnerUpdated = isOwnerModified(subscription, subFromDB);
        return isOrganizationAdmin || !isOwnerUpdated || isUnitAdmin;
    }

    private static boolean isOwnerModified(VOSubscription subscription, Subscription subFromDB) {
        boolean result = false;
        boolean oldNull = subFromDB.getOwner() == null;
        boolean newNull = subscription.getOwnerId() == null;
        boolean bothNotNull = !oldNull && !newNull;
        boolean oneNullSecondNotNull = oldNull && !newNull || !oldNull && newNull;
        if (bothNotNull) {
            result = !subFromDB.getOwner().getUserId().equals(subscription.getOwnerId());
        } else if (oneNullSecondNotNull) {
            result = true;
        }
        return result;
    }
}
