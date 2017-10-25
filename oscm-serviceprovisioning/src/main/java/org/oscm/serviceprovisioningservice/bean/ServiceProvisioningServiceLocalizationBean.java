/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogCollector;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocalizationLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.validator.BLValidator;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOServiceLocalization;

@Stateless
@Local(ServiceProvisioningServiceLocalizationLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ServiceProvisioningServiceLocalizationBean implements
        ServiceProvisioningServiceLocalizationLocal {

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB
    ServiceAuditLogCollector serviceAudit;

    @EJB
    SubscriptionAuditLogCollector subscriptionAudit;

    @EJB
    PriceModelAuditLogCollector priceModelAudit;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean checkIsAllowedForLocalizingService(final long serviceKey)
            throws ObjectNotFoundException {

        final Product product = ds.getReference(Product.class, serviceKey);
        final PlatformUser currentUser = ds.getCurrentUser();
        final Organization currentOrganization = currentUser.getOrganization();
        final long currentOrganizationKey = currentOrganization.getKey();

        Organization organization = null;

        if (currentOrganization.hasRole(OrganizationRoleType.SUPPLIER)
                || currentOrganization.hasRole(OrganizationRoleType.RESELLER)
                || currentOrganization.hasRole(OrganizationRoleType.BROKER)) {
            organization = product.getVendor();
            if (organization.getKey() == currentOrganizationKey) {
                return true;
            }
        }

        if (currentOrganization.hasRole(OrganizationRoleType.CUSTOMER)) {
            // 1 current organization is organization of subscription
            Subscription subscription = product.getOwningSubscription();
            if (subscription != null) {
                organization = subscription.getOrganization();
                if (organization.getKey() == currentOrganizationKey) {
                    return true;
                }
            }

            // 2 current organization is target customer
            organization = product.getTargetCustomer();
            if ((organization != null)
                    && (organization.getKey() == currentOrganizationKey)) {
                return true;
            }
            organization = product.getVendor();
            List<Organization> suppliersOfCustomer = currentOrganization
                    .getSuppliersOfCustomer();
            if (suppliersOfCustomer.contains(organization)) {
                return true;
            }
            if (product.getType() == ServiceType.PARTNER_SUBSCRIPTION
                    && currentOrganization
                            .hasRole(OrganizationRoleType.SUPPLIER)
                    && product.getTemplate().getVendor()
                            .hasRole(OrganizationRoleType.BROKER)) {
                organization = product.getTemplate().getTemplate().getVendor();
                if (suppliersOfCustomer.contains(organization)) {
                    return true;
                }
            }
        }

        return false;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOServiceLocalization getServiceLocalization(Product service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        if (!checkIsAllowedForLocalizingService(service.getKey())) {
            throw new OperationNotPermittedException(
                    "No rights for getting product localizations.");
        }

        final VOServiceLocalization l = new VOServiceLocalization();
        l.setNames(localizer.getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME));
        l.setShortDescriptions(localizer.getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION));
        l.setDescriptions(localizer.getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC));
        l.setCustomTabNames(localizer.getLocalizedValues(service.getKey(),
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME));

        return l;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void saveServiceLocalization(long serviceKey,
            VOServiceLocalization localization) throws ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            ConcurrentModificationException {

        if (!checkIsAllowedForLocalizingService(serviceKey)) {
            throw new OperationNotPermittedException(
                    "No rights for setting product localizations.");
        }

        for (VOLocalizedText name : localization.getNames()) {
            BLValidator.isName(ProductAssembler.FIELD_NAME_NAME,
                    name.getText(), false);
        }

        Product product = ds.getReference(Product.class, serviceKey);
        VOServiceLocalization storedServiceLocalization = getServiceLocalization(product);
        if (ds.getCurrentUser().hasRole(UserRoleType.SERVICE_MANAGER)) {
            localizer.setLocalizedValues(serviceKey,
                    LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                    localization.getDescriptions());
            localizer.setLocalizedValues(serviceKey,
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                    localization.getNames());
            localizer.setLocalizedValues(serviceKey,
                    LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                    localization.getShortDescriptions());
            localizer.setLocalizedValues(serviceKey,
                    LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                    localization.getCustomTabNames());
        }
        serviceAudit.localizeService(ds, product, storedServiceLocalization,
                localization);

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void savePriceModelLocalizationForReseller(long serviceKey,
            boolean isChargable, VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {

        Product product = ds.getReference(Product.class, serviceKey);
        if (!checkIsAllowedForLocalizingService(serviceKey)) {
            throw new OperationNotPermittedException(
                    "No rights for setting price model localizations.");
        }
        Organization customer = product.getTargetCustomer();
        VOPriceModelLocalization storedLocalization = getPriceModelLocalization(serviceKey);
        VOPriceModelLocalization newLocalization = new VOPriceModelLocalization();
        newLocalization = savePriceModelLocalizationAsReseller(serviceKey,
                localization);
        auditPriceModelLocalization(product, customer, storedLocalization,
                newLocalization);
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void savePriceModelLocalizationForSupplier(long priceModelKey,
            boolean isChargable, VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {
        Product product = ds.getReference(PriceModel.class, priceModelKey)
                .getProduct();

        if (product == null) {
            throw new OperationNotPermittedException(
                    "No rights for setting price model localizations, price model has no product assigned.");
        }

        long serviceKey = product.getKey();
        if (!checkIsAllowedForLocalizingService(serviceKey)) {
            throw new OperationNotPermittedException(
                    "No rights for setting price model localizations.");
        }
        Organization customer = product.getTargetCustomer();
        VOPriceModelLocalization storedLocalization = getPriceModelLocalization(serviceKey);
        VOPriceModelLocalization newLocalization = new VOPriceModelLocalization();
        newLocalization = savePriceModelLocalizationAsSupplier(priceModelKey,
                isChargable, localization);
        auditPriceModelLocalization(product, customer, storedLocalization,
                newLocalization);
    }

    void auditPriceModelLocalization(Product product, Organization customer,
            VOPriceModelLocalization storedLocalization,
            VOPriceModelLocalization newLocalization) {

        if (ServiceType.isSubscription(product.getType())) {
            subscriptionAudit.localizePriceModel(ds,
                    product.getOwningSubscription(), storedLocalization,
                    newLocalization);
        }
        if (ServiceType.isTemplate(product.getType())) {
            priceModelAudit.localizePriceModel(ds, product, customer,
                    storedLocalization, newLocalization);
        }
    }

    private VOPriceModelLocalization savePriceModelLocalizationAsSupplier(
            long priceModelKey, boolean isChargable,
            VOPriceModelLocalization localization)
            throws ConcurrentModificationException {
        VOPriceModelLocalization localizationTobeUpdate = new VOPriceModelLocalization();
        if (isChargable) {
            localizer.setLocalizedValues(priceModelKey,
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                    localization.getDescriptions());
            localizationTobeUpdate.setDescriptions(localization
                    .getDescriptions());
        } else {
            localizer.removeLocalizedValues(priceModelKey,
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            localizationTobeUpdate
                    .setDescriptions(new ArrayList<VOLocalizedText>());
        }
        localizer.setLocalizedValues(priceModelKey,
                LocalizedObjectTypes.PRICEMODEL_LICENSE,
                localization.getLicenses());
        localizationTobeUpdate.setLicenses(localization.getLicenses());
        return localizationTobeUpdate;
    }

    VOPriceModelLocalization savePriceModelLocalizationAsReseller(
            long serviceKey, VOPriceModelLocalization localization) {
        for (VOLocalizedText text : localization.getLicenses()) {
            if (text.getText() == null) {
                text.setText("");
            }
            localizer.storeLocalizedResource(text.getLocale(), serviceKey,
                    LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE,
                    text.getText());
        }
        return localization;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOPriceModelLocalization getPriceModelLocalization(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException {

        if (!checkIsAllowedForLocalizingService(serviceKey)) {
            throw new OperationNotPermittedException(
                    "No rights for getting price model localizations.");
        }

        Product product = ds.getReference(Product.class, serviceKey);
        Organization vendor;
        if (ServiceType.isSubscription(product.getType())) {
            vendor = product.getTemplate().getVendor();
        } else {
            vendor = product.getVendor();
        }
        boolean reseller = vendor.hasRole(OrganizationRoleType.RESELLER);
        VOPriceModelLocalization result = new VOPriceModelLocalization();
        if (!reseller) {
            result = getPriceModelLocalizationForSupplierOrBroker(product,
                    vendor);
        } else {

            result = getPriceModelLocalizationForReseller(product, serviceKey);
        }

        return result;
    }

    VOPriceModelLocalization getPriceModelLocalizationForReseller(
            Product product, long serviceKey) {

        PriceModel priceModel = product.getTemplateOrSelf().getPriceModel();
        VOPriceModelLocalization localization = setDescriptions(priceModel,
                LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE, serviceKey);

        return localization;
    }

    VOPriceModelLocalization getPriceModelLocalizationForSupplierOrBroker(
            Product product, Organization vendor)
            throws ObjectNotFoundException {

        long tempKey;
        PriceModel priceModel = new PriceModel();
        if (!ServiceType.isSubscription(product.getType())
                && vendor.hasRole(OrganizationRoleType.BROKER)) {
            tempKey = product.getTemplateOrSelf().getKey();
            priceModel = product.getTemplateOrSelf().getPriceModel();
        } else {
            tempKey = product.getKey();
            priceModel = product.getPriceModel();
        }
        long key = ds.getReference(Product.class, tempKey).getPriceModel()
                .getKey();

        VOPriceModelLocalization localization = setDescriptions(priceModel,
                LocalizedObjectTypes.PRICEMODEL_LICENSE, key);

        return localization;
    }

    private VOPriceModelLocalization setDescriptions(PriceModel model,
            LocalizedObjectTypes type, long key) {
        long priceModelKey = -1;
        if (model != null) {
            priceModelKey = model.getKey();
        }
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        localization.setDescriptions(localizer.getLocalizedValues(
                priceModelKey, LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));
        localization.setLicenses(localizer.getLocalizedValues(key, type));

        return localization;
    }
}
