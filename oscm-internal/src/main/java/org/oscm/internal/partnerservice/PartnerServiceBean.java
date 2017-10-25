/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.partnerservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocalizationLocal;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.ProductValidator;
import org.oscm.internal.assembler.POServiceFeedbackAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceEntry;

@Stateless
@Remote(PartnerService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PartnerServiceBean implements PartnerService {

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = ServiceProvisioningServiceLocalizationLocal.class)
    ServiceProvisioningServiceLocalizationLocal spsLocalizer;

    @EJB(beanInterface = ServiceProvisioningService.class)
    ServiceProvisioningService sps;

    @EJB(beanInterface = ServiceProvisioningServiceLocal.class)
    ServiceProvisioningServiceLocal spsLocal;

    @EJB(beanInterface = ServiceProvisioningServiceInternal.class)
    ServiceProvisioningServiceInternal spsi;

    @EJB(beanInterface = DiscountService.class)
    DiscountService discountService;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal slService;

    @EJB(beanInterface = MarketplaceService.class)
    MarketplaceService marketplaceService;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @Resource
    SessionContext sessionCtx;

    public static final Set<SubscriptionStatus> USABLE_SUBSCRIPTION_STATUS = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.PENDING_UPD,
                    SubscriptionStatus.SUSPENDED_UPD));

    @Override
    @RolesAllowed({ "BROKER_MANAGER", "RESELLER_MANAGER" })
    public Response getServiceDetails(long serviceKey)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException {

        Product storedService = ds.getReference(Product.class, serviceKey);
        ProductValidator.validateResalePermission(
                ProductAssembler.getProductId(storedService),
                storedService.getStatus());

        VOServiceDetails voServiceDetails = loadServiceDetails(serviceKey);

        POPartnerServiceDetails serviceDetails = new POPartnerServiceDetails();
        serviceDetails.setServiceDescription(voServiceDetails.getDescription());
        serviceDetails.setServiceShortDescription(voServiceDetails
                .getShortDescription());
        serviceDetails.setServiceKey(voServiceDetails.getKey());
        serviceDetails.setServiceName(voServiceDetails.getName());
        serviceDetails.setStatus(voServiceDetails.getStatus());
        serviceDetails.setPriceModel(swapPriceModelLicense(
                voServiceDetails.getPriceModel(), serviceKey));
        serviceDetails.setAutoAssignUserEnabled(voServiceDetails
                .isAutoAssignUserEnabled().booleanValue());
        return new Response(serviceDetails);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public Response getPriceModelLocalization(VOServiceDetails voServiceDetails)
            throws ObjectNotFoundException, OperationNotPermittedException {

        Response r = new Response(
                spsLocalizer.getPriceModelLocalization(voServiceDetails
                        .getKey()));

        return r;
    }

    VOPriceModel swapPriceModelLicense(VOPriceModel priceModel, long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        String licenseForLocale = getLocalizedTextForLocale(ds.getCurrentUser()
                .getLocale(), spsLocalizer
                .getPriceModelLocalization(serviceKey).getLicenses());
        priceModel.setLicense(licenseForLocale);
        return priceModel;
    }

    private String getLocalizedTextForLocale(String locale,
            List<VOLocalizedText> priceModelLocalization) {
        for (VOLocalizedText text : priceModelLocalization) {
            if (text.getLocale().equals(locale)) {
                return text.getText();
            }
        }
        return null;
    }

    private VOServiceDetails loadServiceDetails(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        VOService srv = new VOService();
        srv.setKey(serviceKey);
        return sps.getServiceDetails(srv);
    }

    @Override
    @RolesAllowed("RESELLER_MANAGER")
    public Response updatePartnerServiceDetails(POPartnerServiceDetails details)
            throws ObjectNotFoundException, ServiceStateException,
            OperationNotPermittedException, ConcurrentModificationException {
        ArgumentValidator.notNull("service", details);
        Product storedService = ds.getReference(Product.class,
                details.getServiceKey());
        ProductValidator.validateInactiveOrSuspended(
                ProductAssembler.getProductId(storedService),
                storedService.getStatus());
        try {
            spsLocalizer.savePriceModelLocalizationForReseller(
                    details.getServiceKey(),
                    details.getPriceModel().isChargeable(),
                    buildPriceModelLocalization(
                            ds.getCurrentUser().getLocale(), details));
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return new Response();
    }

    private VOPriceModelLocalization buildPriceModelLocalization(String locale,
            POPartnerServiceDetails details) {
        VOPriceModelLocalization localization = new VOPriceModelLocalization();
        localization.setLicenses(Arrays.asList(new VOLocalizedText(locale,
                details.getPriceModel().getLicense())));
        return localization;
    }

    @Override
    public Response getServiceForMarketplace(long serviceKey, String locale)
            throws ObjectNotFoundException {
        ArgumentValidator.notNull("locale", locale);

        Product storedService = ds.getReference(Product.class, serviceKey);
        PlatformUser currentUser = ds.getCurrentUserIfPresent();
        if (storedService.getType() == ServiceType.CUSTOMER_TEMPLATE
                && (currentUser == null || storedService.getTargetCustomer()
                        .getKey() != currentUser.getOrganization().getKey())) {
            storedService = storedService.getTemplate();
        } else if (currentUser != null
                && storedService.getType() == ServiceType.TEMPLATE) {
            Query query = ds
                    .createNamedQuery("Product.getSpecificCustomerProduct");
            query.setParameter("template", storedService);
            query.setParameter("customer", currentUser.getOrganization());
            @SuppressWarnings("unchecked")
            List<Product> custProducts = query.getResultList();
            if (custProducts.size() > 0) {
                storedService = custProducts.get(0);
            }
        }
        if (currentUser == null) {
            // visible for anonymous?
            List<CatalogEntry> catalogEntry = storedService.getCatalogEntries();
            if (catalogEntry == null || catalogEntry.isEmpty()
                    || !catalogEntry.get(0).isAnonymousVisible()) {
                return new Response();
            }
        }

        boolean subscriptionLimitReached = spsLocal
                .isSubscriptionLimitReached(storedService);
        VOServiceEntry service = ProductAssembler.toVOServiceEntry(
                storedService, new LocalizerFacade(localizer, locale),
                subscriptionLimitReached);
        return new Response(service);
    }

    @Override
    public Response getAllServiceDetailsForMarketplace(long serviceKey,
            String locale, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        // load service
        Response response = getServiceForMarketplace(serviceKey, locale);

        checkServiceAccessibleInMarketplace(serviceKey, marketplaceId);

        checkActiveOrSuspendedWithMarketplaceOwner(
                response.getResult(VOServiceEntry.class), marketplaceId);

        loadReviewsAndRating(serviceKey, response);

        loadDiscount(serviceKey, response);

        VOServiceEntry service = response.getResult(VOServiceEntry.class);
        if (service != null) {
            loadSupplierData(serviceKey, response, locale);
            loadServicePartnerData(serviceKey, response, locale);
        }

        loadRelatedServices(response, marketplaceId, locale);
        return response;
    }

    private void loadRelatedServices(Response response, String marketplaceId,
            String locale) throws ObjectNotFoundException {
        VOService baseService = response.getResult(VOServiceEntry.class);

        List<VOService> relatedServicesForMarketplace = sps
                .getRelatedServicesForMarketplace(baseService, marketplaceId,
                        locale);
        response.getResults().add(relatedServicesForMarketplace);
    }

    private void loadServicePartnerData(long serviceKey, Response response,
            String locale) {
        Map<String, POVendorAddress> addressMap = getAddressMap(response);

        try {
            VOOrganization servicePartner = spsi.getPartnerForService(
                    serviceKey, locale);
            if (servicePartner != null) {
                localizeDomicileCountry(servicePartner, locale);
                addressMap.put(POVendorAddress.SERVICE_SERVICE_PARTNER,
                        new POVendorAddress(servicePartner));
            }
        } catch (ObjectNotFoundException e) {
            // should not occur
        }
    }

    private void loadSupplierData(long serviceKey, Response response,
            String locale) {
        Map<String, POVendorAddress> addressMap = getAddressMap(response);
        try {
            VOOrganization serviceSupplier = sps.getServiceSeller(serviceKey,
                    locale);
            if (serviceSupplier != null) {
                localizeDomicileCountry(serviceSupplier, locale);
                addressMap.put(POVendorAddress.SERVICE_SELLER_SUPPLIER,
                        new POVendorAddress(serviceSupplier));
            }
        } catch (ObjectNotFoundException e) {
            // should not occur
        }
    }

    private void localizeDomicileCountry(VOOrganization org, String locale) {
        Locale l = new Locale("", org.getDomicileCountry());
        org.setDomicileCountry(l.getDisplayCountry(new Locale(locale)));
    }

    Map<String, POVendorAddress> getAddressMap(Response response) {
        if (response.getResult(HashMap.class) == null) {
            Map<String, POVendorAddress> addressMap = new HashMap<String, POVendorAddress>();
            response.getResults().add(addressMap);
            return addressMap;
        } else {
            @SuppressWarnings("unchecked")
            Map<String, POVendorAddress> map = response
                    .getResult(HashMap.class);
            return map;
        }
    }

    void checkServiceAccessibleInMarketplace(long serviceKey,
            String marketplaceId) throws OperationNotPermittedException,
            ObjectNotFoundException {
        Product storedService = ds.getReference(Product.class, serviceKey);
        if (!isServiceAccessibleInMarketplace(storedService, marketplaceId)) {
            throw new OperationNotPermittedException();
        }
    }

    /**
     * if the service is suspended and the user's organization is not the
     * marketplaceOwner the service is not visible to the user hence
     * OperationNotPermittedException is thrown
     * 
     * @param service
     * @param marketplaceId
     * @throws OperationNotPermittedException
     */
    void checkActiveOrSuspendedWithMarketplaceOwner(VOServiceEntry service,
            String marketplaceId) throws OperationNotPermittedException {
        if (service.getStatus() == ServiceStatus.ACTIVE) {
            return;
        }
        if (service.getStatus() == ServiceStatus.SUSPENDED) {
            if (!isMarketplaceOwnedByCurrentUser(marketplaceId)) {
                throw new OperationNotPermittedException();
            }
        }
    }

    boolean isServiceAccessibleInMarketplace(Product product,
            String marketplaceId) {
        if (ServiceType.isSubscription(product.getType())) {
            return false;
        }

        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        if (ServiceType.isTemplateOrPartnerTemplate(product.getType())) {
            catalogEntries.addAll(product.getCatalogEntries());
        } else if (ServiceType.isCustomerTemplate(product.getType())) {
            catalogEntries.addAll(product.getTemplate().getCatalogEntries());
        }
        for (CatalogEntry catalogEntry : catalogEntries) {
            Marketplace mpl = catalogEntry.getMarketplace();
            if (mpl != null && mpl.getMarketplaceId().equals(marketplaceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    private boolean isMarketplaceOwnedByCurrentUser(String mpId) {
        try {
            final VOMarketplace mp = marketplaceService
                    .getMarketplaceById(mpId);
            PlatformUser currentUser = ds.getCurrentUserIfPresent();
            if (currentUser != null
                    && currentUser.getOrganization().getOrganizationId()
                            .equals(mp.getOwningOrganizationId())) {
                return true;
            }
        } catch (ObjectNotFoundException e) {
            return false;
        }
        return false;
    }

    private void loadReviewsAndRating(long serviceKey, Response r) {
        try {
            Product product = ds.getReference(Product.class, serviceKey);
            PlatformUser user = ds.getCurrentUserIfPresent();
            boolean subscriptionsExist = false;
            if (user != null) {
                subscriptionsExist = slService
                        .isUsableSubscriptionExistForTemplate(user,
                                USABLE_SUBSCRIPTION_STATUS,
                                product.getTemplateOrSelfForReview());
            }
            POServiceFeedback selectedServiceFeedback = POServiceFeedbackAssembler
                    .toPOServiceFeedback(product, user, subscriptionsExist);
            r.getResults().add(selectedServiceFeedback);
        } catch (ObjectNotFoundException e) {
            // nothing: Rating is optional
        }
    }

    private void loadDiscount(long serviceKey, Response r) {

        try {
            VODiscount voDiscount = discountService
                    .getDiscountForService(serviceKey);
            if (voDiscount != null) {
                if (isDiscountActive(voDiscount)) {
                    r.getResults().add(voDiscount);
                }
            }
        } catch (ObjectNotFoundException ex) {
            // nothing: Discount is optional
        }

    }

    private boolean isDiscountActive(VODiscount discount) {
        if (discount == null) {
            return false;
        }

        long currentTimeMonthYear = getTimeInMillisForFirstDay(System
                .currentTimeMillis());
        if (discount.getStartTime() == null
                || discount.getStartTime().longValue() > currentTimeMonthYear
                || (discount.getEndTime() != null && discount.getEndTime()
                        .longValue() < currentTimeMonthYear)) {

            return false;
        } else {
            return true;
        }
    }

    /**
     * Getting millisecond of the first day in month.
     * 
     * @param timeInMilis
     *            Time of any day of month.
     * @return First millisecond of month.
     */
    private long getTimeInMillisForFirstDay(long timeInMilis) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(timeInMilis);

        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        return currentCalendar.getTimeInMillis();
    }

}
