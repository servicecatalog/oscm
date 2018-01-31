/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid                                        
 *                                                                              
 *  Creation Date: 03.03.2009                                                      
 *                                                                              
 *  Completion Time:                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.accountservice.assembler.PaymentInfoAssembler;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserSubscription;
import org.oscm.serviceprovisioningservice.assembler.PriceModelAssembler;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.assembler.RoleAssembler;
import org.oscm.serviceprovisioningservice.assembler.TechnicalProductOperationAssembler;
import org.oscm.vo.BaseAssembler;

/**
 * @author schmid
 * 
 */
public class SubscriptionAssembler extends BaseAssembler {

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @param facade
     *            the localizer facade
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOSubscription toVOSubscription(Subscription subscription,
            LocalizerFacade facade) {
        return toVOSubscription(subscription, facade,
                PerformanceHint.ALL_FIELDS);
    }

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @param facade
     *            the localizer facade
     * @param scope
     *            the amount of data to be included in the transfer object
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOSubscription toVOSubscription(Subscription subscription,
            LocalizerFacade facade, PerformanceHint scope) {
        if (subscription == null) {
            return null;
        }
        VOSubscription voSubscription = new VOSubscription();
        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
            fillIdentifyingFields(voSubscription, subscription);
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            fillBaseFields(voSubscription, subscription);
            break;
        default:
            fillAllFields(voSubscription, subscription, facade);
        }
        updateValueObject(voSubscription, subscription);
        return voSubscription;
    }

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param facade
     *            the localizer facade
     * @param platformUser
     *            The domain object containing the values to be set.
     * 
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOSubscriptionDetails toVOSubscriptionDetails(
            Subscription subscription, LocalizerFacade facade) {
        if (subscription == null) {
            return null;
        }
        VOSubscriptionDetails voSubDet = new VOSubscriptionDetails();
        fillAllFields(voSubDet, subscription, facade);
        fillVOSubscriptionDetails(voSubDet, subscription, facade);
        updateValueObject(voSubDet, subscription);
        return voSubDet;
    }

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @param facade
     *            the localizer facade
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOUserSubscription toVOUserSubscription(
            Subscription subscription, PlatformUser user,
            LocalizerFacade facade) {
        return toVOUserSubscription(subscription, user, facade,
                PerformanceHint.ALL_FIELDS);
    }

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @param facade
     *            the localizer facade
     * @param scope
     *            the amount of data to be loaded
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOUserSubscription toVOUserSubscription(
            Subscription subscription, PlatformUser user,
            LocalizerFacade facade, PerformanceHint scope) {
        if (subscription == null) {
            return null;
        }
        VOUserSubscription voUsrSub = new VOUserSubscription();
        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
            fillIdentifyingFields(voUsrSub, subscription);
            break;
        case ONLY_FIELDS_FOR_LISTINGS:
            fillBaseFields(voUsrSub, subscription);
            break;
        default:
            fillAllFields(voUsrSub, subscription, facade);
        }
        fillVOUserSubscription(voUsrSub, subscription, user, facade);
        updateValueObject(voUsrSub, subscription);
        return voUsrSub;
    }

    /**
     * Sets the key and identifier in the transfer object
     */
    private static void fillIdentifyingFields(VOSubscription voSubscription,
            Subscription subscription) {
        voSubscription.setSubscriptionId(subscription.getSubscriptionId());
    }

    /**
     * Sets the most important attributes in the transfer object. Usually, these
     * fields are required for listings.
     */
    static void fillBaseFields(VOSubscription voSubscription,
            Subscription subscription) {
        fillIdentifyingFields(voSubscription, subscription);

        voSubscription.setCreationDate(subscription.getCreationDate());
        voSubscription.setActivationDate(subscription.getActivationDate());
        voSubscription.setDeactivationDate(subscription.getDeactivationDate());
        voSubscription.setTimeoutMailSent(subscription.isTimeoutMailSent());
        voSubscription.setStatus(subscription.getStatus());
        voSubscription.setOrganizationId(subscription.getOrganization().getOrganizationId());
        if (subscription.getUserGroup() != null) {
            voSubscription.setUnitKey(subscription.getUserGroup().getKey());
            voSubscription.setUnitName(subscription.getUserGroup().getName());
        }
    }

    /**
     * Sets all attributes in the transfer object. Warning: a lot of data must
     * be loaded from the database. This will result in slow performance, if
     * called for multiple subscriptions.
     */
    private static void fillAllFields(VOSubscription voSubscription,
            Subscription subscription, LocalizerFacade facade) {

        fillBaseFields(voSubscription, subscription);

        if (subscription.getOwner() != null) {
            voSubscription.setOwnerId(subscription.getOwner().getUserId());
        }

        voSubscription.setNumberOfAssignedUsers(
                subscription.getUsageLicenses().size());
        Product product = subscription.getProduct();
        // use the identifier of the template, if the current product is just a
        // copy
        if (product.getTemplate() == null) {
            voSubscription.setServiceId(product.getProductId());
            voSubscription.setServiceKey(product.getKey());
            if (product.getVendor() != null) {
                String supplierName = product.getVendor().getName() != null
                        && product.getVendor().getName().trim().length() > 0
                                ? product.getVendor().getName()
                                : product.getVendor().getOrganizationId();

                voSubscription.setSellerName(supplierName);
            }
        } else {
            voSubscription.setServiceId(ProductAssembler.getProductId(product));
            voSubscription.setServiceKey(product.getTemplate().getKey());
            if (product.getTemplate().getVendor() != null) {
                String supplierName = product.getTemplate().getVendor()
                        .getName() != null
                        && product.getTemplate().getVendor().getName().trim()
                                .length() > 0
                                        ? product.getTemplate().getVendor()
                                                .getName()
                                        : product.getTemplate().getVendor()
                                                .getOrganizationId();

                voSubscription.setSellerName(supplierName);
            }
            voSubscription.setCustomTabName(facade.getText(
                    subscription.getProduct().getTemplate().getKey(),
                    LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME));
            voSubscription
                    .setCustomTabUrl(product.getTemplate().getCustomTabUrl());
            voSubscription.setOrganizationId(
                    subscription.getOrganization().getOrganizationId());
        }

        TechnicalProduct techProd = product.getTechnicalProduct();
        voSubscription.setServiceAccessType(techProd.getAccessType());
        if (subscription.getAccessInfo() != null) {
            voSubscription.setServiceAccessInfo(subscription.getAccessInfo());
        } else {
            voSubscription.setServiceAccessInfo(facade.getText(
                    techProd.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        }
        if (subscription.getBaseURL() == null) {
            voSubscription.setServiceBaseURL(techProd.getBaseURL());
        } else {
            voSubscription.setServiceBaseURL(subscription.getBaseURL());
        }
        if (subscription.getLoginPath() == null) {
            voSubscription.setServiceLoginPath(techProd.getLoginPath());
        } else {
            voSubscription.setServiceLoginPath(subscription.getLoginPath());
        }

        voSubscription
                .setServiceInstanceId(subscription.getProductInstanceId());
        voSubscription
                .setPurchaseOrderNumber(subscription.getPurchaseOrderNumber());
        voSubscription.setProvisioningProgress(facade.getText(
                subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS));
        voSubscription.setTechnicalServiceOperations(
                TechnicalProductOperationAssembler
                        .toVOTechnicalServiceOperations(
                                techProd.getTechnicalProductOperations(),
                                facade));
        String message = subscription.getSuccessMessage();
        voSubscription.setSuccessInfo(message == null ? "" : message);
        
        voSubscription.setProvisioningError(facade.getText(
                subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_ERROR));
    }

    private static void fillVOSubscriptionDetails(
            VOSubscriptionDetails voSubDet, Subscription subscription,
            LocalizerFacade facade) {
        ArrayList<VOUsageLicense> voLicenses = new ArrayList<VOUsageLicense>();
        for (UsageLicense lic : subscription.getUsageLicenses()) {
            voLicenses.add(toVOUsageLicense(lic, facade));
        }
        voSubDet.setUsageLicenses(voLicenses);
        voSubDet.setPriceModel(PriceModelAssembler
                .toVOPriceModel(subscription.getPriceModel(), facade));
        if (subscription.getBillingContact() != null) {
            voSubDet.setBillingContact(BillingContactAssembler
                    .toVOBillingContact(subscription.getBillingContact()));
        }
        if (subscription.getPaymentInfo() != null) {
            voSubDet.setPaymentInfo(PaymentInfoAssembler
                    .toVOPaymentInfo(subscription.getPaymentInfo(), facade));
        }
        voSubDet.setSubscribedService(ProductAssembler
                .toVOProduct(subscription.getProduct(), facade));
    }

    private static void fillVOUserSubscription(VOUserSubscription voUsrSub,
            Subscription subscription, PlatformUser usr,
            LocalizerFacade facade) {
        if (usr == null)
            return;
        for (UsageLicense lic : subscription.getUsageLicenses()) {
            if (usr.equals(lic.getUser())) {
                voUsrSub.setLicense(toVOUsageLicense(lic, facade));
                return;
            }
        }
    }

    /**
     * Preloads data required to construct a transfer object. Preloading
     * increases performance, by replacing many small SQL requests with one
     * large request. Subsequent DB request will be avoided because of the
     * internal caches in Hibernate and LocalizerFacade.
     */
    public static void prefetchData(List<Subscription> subscription,
            LocalizerFacade facade) {
        List<Long> objectKeys = new ArrayList<Long>();
        for (Subscription sub : subscription) {
            objectKeys.add(Long.valueOf(sub.getKey()));
            TechnicalProduct techProd = sub.getProduct().getTechnicalProduct();
            objectKeys.add(Long.valueOf(techProd.getKey()));
            for (TechnicalProductOperation opperation : techProd
                    .getTechnicalProductOperations()) {
                objectKeys.add(Long.valueOf(opperation.getKey()));
            }
        }
        facade.prefetch(objectKeys,
                Arrays.asList(new LocalizedObjectTypes[] {
                        LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME,
                        LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION,
                        LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS,
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC }));
    }

    public static VOUsageLicense toVOUsageLicense(UsageLicense lic,
            LocalizerFacade facade) {
        VOUsageLicense voLic = new VOUsageLicense();
        voLic.setKey(lic.getKey());
        voLic.setVersion(lic.getVersion());
        voLic.setUser(UserDataAssembler.toVOUser(lic.getUser()));
        voLic.setApplicationUserId(lic.getApplicationUserId());
        voLic.setRoleDefinition(RoleAssembler
                .toVORoleDefinition(lic.getRoleDefinition(), facade));
        return voLic;
    }

}
