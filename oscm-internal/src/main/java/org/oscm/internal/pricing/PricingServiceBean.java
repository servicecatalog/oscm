/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

@Stateless
@Remote(PricingService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PricingServiceBean implements PricingService {

    static final String FIELD_MARKETPLACE_ID = "marketplace.id";
    static final String FIELD_PM_REVENUESHARE = "priceModel.revenueShare";
    static final String FIELD_PM_REVENUESHARE_REVENUESHARE = "priceModel.revenueShare.revenueShare";
    static boolean STATUS_CHECK_NEEDED = true;
    static boolean STATUS_CHECK_NOT_NEEDED = false;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    MarketplaceServiceLocal mpServiceLocal;

    @EJB(beanInterface = ServiceProvisioningPartnerServiceLocal.class)
    ServiceProvisioningPartnerServiceLocal spPartnerServiceLocal;

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER",
            "MARKETPLACE_OWNER", "PLATFORM_OPERATOR" })
    public Response getMarketplaceRevenueShares(String marketplaceId)
            throws ObjectNotFoundException {

        try {
            ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
            RevenueShareModel revenueShare = mpServiceLocal
                    .loadMarketplaceRevenueShare(marketplaceId);
            Response response = new Response();
            response.getResults().add(toPOMarketplacePriceModel(revenueShare));
            return response;
        } finally {

        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER",
            "MARKETPLACE_OWNER", "PLATFORM_OPERATOR" })
    public Response getPartnerRevenueSharesForMarketplace(String marketplaceId)
            throws ObjectNotFoundException {

        Response response = new Response();
        try {
            ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

            Marketplace marketplace = mpServiceLocal
                    .getMarketplace(marketplaceId);

            response.getResults().add(toPOPartnerPriceModel(marketplace));
        } finally {

        }
        return response;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER",
            "MARKETPLACE_OWNER", "PLATFORM_OPERATOR" })
    public Response getPartnerRevenueShareForService(POServiceForPricing service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException {

        ArgumentValidator.notNull("service", service);

        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = spPartnerServiceLocal
                .getRevenueShareModelsForProduct(service.getKey(),
                        STATUS_CHECK_NEEDED);

        Response response = getRevenueShareModels(revenueShareModels);

        return response;

    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER",
            "MARKETPLACE_OWNER", "PLATFORM_OPERATOR" })
    public Response getPartnerRevenueShareForAllStatesService(
            POServiceForPricing service) throws ObjectNotFoundException,
            OperationNotPermittedException, ServiceOperationException,
            ServiceStateException {

        ArgumentValidator.notNull("service", service);

        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = spPartnerServiceLocal
                .getRevenueShareModelsForProduct(service.getKey(),
                        STATUS_CHECK_NOT_NEEDED);

        Response response = getRevenueShareModels(revenueShareModels);

        return response;

    }

    Response getRevenueShareModels(
            Map<RevenueShareModelType, RevenueShareModel> revenueShareModels) {
        RevenueShareModel brokerPriceModel = revenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = revenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        Response response = new Response(assemblePartnerPriceModel(
                brokerPriceModel, resellerPriceModel));
        return response;
    }

    @Override
    @RolesAllowed({ "PLATFORM_OPERATOR", "SERVICE_MANAGER", "BROKER_MANAGER",
            "RESELLER_MANAGER" })
    public Response getOperatorRevenueShare(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException {

        RevenueShareModel operatorRevenueShare = spPartnerServiceLocal
                .getOperatorRevenueShare(serviceKey);
        RevenueShareModel defaultOperatorRevenueShare = spPartnerServiceLocal
                .getDefaultOperatorRevenueShare(serviceKey);

        POOperatorPriceModel operatorPriceModel = new POOperatorPriceModel();
        if (operatorRevenueShare != null) {
            operatorPriceModel
                    .setRevenueShare(toPORevenueShare(operatorRevenueShare));
        }
        if (defaultOperatorRevenueShare != null) {
            operatorPriceModel
                    .setDefaultRevenueShare(toPORevenueShare(defaultOperatorRevenueShare));
        }

        return new Response(operatorPriceModel);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response saveOperatorRevenueShare(long serviceKey,
            PORevenueShare revenueShare) throws ObjectNotFoundException,
            ValidationException, ServiceOperationException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("revenueShare", revenueShare);
        RevenueShareModel newRevenueShare = toRevenueShareModel(revenueShare);
        newRevenueShare
                .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        spPartnerServiceLocal.saveOperatorRevenueShare(serviceKey,
                newRevenueShare, revenueShare.getVersion());

        return new Response();
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response savePartnerRevenueSharesForServices(
            List<POServicePricing> pricingsList)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ServiceOperationException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("pricingsList", pricingsList);
        Response response = new Response();

        List<POServicePricing> results = new ArrayList<POServicePricing>();
        for (POServicePricing poServicePricing : pricingsList) {
            POServicePricing servicePricing = savePartnerRevenueSharesForService(
                    poServicePricing.getServiceForPricing(),
                    poServicePricing.getPartnerPriceModel());
            results.add(servicePricing);

        }
        response.getResults().add(results);

        return response;
    }

    private POServicePricing savePartnerRevenueSharesForService(
            POServiceForPricing serviceForPricing,
            POPartnerPriceModel poPriceModel) throws ValidationException,
            ObjectNotFoundException, NonUniqueBusinessKeyException,
            ServiceOperationException, ConcurrentModificationException {

        ArgumentValidator.notNull("serviceForPricing", serviceForPricing);
        ArgumentValidator.notNull("priceModel", poPriceModel);

        RevenueShareModel brokerRevenueShareNew = null;
        int brokerRevenueShareVersion = 0;
        if (poPriceModel.getRevenueShareBrokerModel() != null) {
            brokerRevenueShareNew = toRevenueShareModel(poPriceModel
                    .getRevenueShareBrokerModel());
            brokerRevenueShareVersion = poPriceModel
                    .getRevenueShareBrokerModel().getVersion();
        }

        RevenueShareModel resellerRevenueShareNew = null;
        int resellerRevenueShareVersion = 0;
        if (poPriceModel.getRevenueShareResellerModel() != null) {
            resellerRevenueShareNew = toRevenueShareModel(poPriceModel
                    .getRevenueShareResellerModel());
            resellerRevenueShareVersion = poPriceModel
                    .getRevenueShareResellerModel().getVersion();
        }

        Map<RevenueShareModelType, RevenueShareModel> updatedRevenueShareModels = spPartnerServiceLocal
                .saveRevenueShareModelsForProduct(serviceForPricing.getKey(),
                        brokerRevenueShareNew, resellerRevenueShareNew,
                        brokerRevenueShareVersion, resellerRevenueShareVersion);

        RevenueShareModel updatedBrokerRevenueShare = updatedRevenueShareModels
                .get(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel updatedResellerRevenueShare = updatedRevenueShareModels
                .get(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        POServicePricing servicePricing = assembleServicePricing(
                serviceForPricing, updatedBrokerRevenueShare,
                updatedResellerRevenueShare);

        return servicePricing;
    }

    private POServicePricing assembleServicePricing(
            POServiceForPricing serviceForPricing,
            RevenueShareModel brokerRevenueShare,
            RevenueShareModel resellerRevenueShare) {
        POServicePricing pricing = new POServicePricing();
        pricing.setServiceForPricing(serviceForPricing);

        POPartnerPriceModel partnerPriceModel = assemblePartnerPriceModel(
                brokerRevenueShare, resellerRevenueShare);

        pricing.setPartnerPriceModel(partnerPriceModel);

        return pricing;
    }

    public static RevenueShareModel toRevenueShareModel(
            PORevenueShare revenueShare) {
        ArgumentValidator.notNull("revenueShare", revenueShare);
        RevenueShareModel revenueShareToUpdate = new RevenueShareModel();
        revenueShareToUpdate.setKey(revenueShare.getKey());
        revenueShareToUpdate.setRevenueShare(revenueShare.getRevenueShare());
        return revenueShareToUpdate;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER",
            "PLATFORM_OPERATOR", "MARKETPLACE_OWNER" })
    public Response getPricingForMarketplace(String marketplaceId)
            throws ObjectNotFoundException {

        try {
            ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
            Marketplace marketplace = mpServiceLocal
                    .getMarketplace(marketplaceId);

            LocalizerFacade facade = new LocalizerFacade(localizer, dm
                    .getCurrentUser().getLocale());
            POMarketplacePricing pricing = toPOMarketplacePricing(facade,
                    marketplace);

            return new Response(pricing);
        } finally {

        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response getMarketplacePricingForService(POServiceForPricing service)
            throws ObjectNotFoundException, ServiceOperationException {

        ArgumentValidator.notNull("service", service);
        CatalogEntry ce = spPartnerServiceLocal
                .getCatalogEntryForProduct(service.getKey());
        if (ce != null && ce.getMarketplace() != null) {
            LocalizerFacade facade = new LocalizerFacade(localizer, dm
                    .getCurrentUser().getLocale());
            POMarketplacePricing pricing = toPOMarketplacePricing(facade,
                    ce.getMarketplace());
            return new Response(pricing);
        }

        return new Response();
    }

    public static POMarketplacePricing toPOMarketplacePricing(
            LocalizerFacade facade, Marketplace mp) {
        POMarketplacePricing pricing = new POMarketplacePricing();
        pricing.setMarketplace(toPOMarketplace(mp, facade));
        pricing.setMarketplacePriceModel(toPOMarketplacePriceModel(mp
                .getPriceModel()));
        pricing.setPartnerPriceModel(toPOPartnerPriceModel(mp));
        return pricing;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response getTemplateServices() {

        List<POServiceForPricing> templateServices = new ArrayList<POServiceForPricing>();
        List<Product> products = spPartnerServiceLocal.getTemplateProducts();
        for (Product product : products) {
            templateServices.add(assembleServiceForPricing(product));
        }

        return new Response(templateServices);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Response getPartnerServicesWithRevenueShareForTemplate(
            POServiceForPricing service) throws ObjectNotFoundException,
            ServiceOperationException {

        ArgumentValidator.notNull("service", service);
        List<POServicePricing> pricings = new ArrayList<POServicePricing>();
        List<Product> partnerProducts = spPartnerServiceLocal
                .getPartnerProductsForTemplate(service.getKey());
        for (Product partnerProduct : partnerProducts) {
            pricings.add(assembleServicePricing(partnerProduct));
        }

        return new Response(pricings);
    }

    public static POMarketplace toPOMarketplace(Marketplace marketplace,
            LocalizerFacade localizerFacade) {
        POMarketplace poMarketplace = new POMarketplace(
                marketplace.getMarketplaceId(), localizerFacade.getText(
                        marketplace.getKey(),
                        LocalizedObjectTypes.MARKETPLACE_NAME));
        poMarketplace.setKey(marketplace.getKey());
        poMarketplace.setVersion(marketplace.getVersion());
        return poMarketplace;
    }

    public static POMarketplacePriceModel toPOMarketplacePriceModel(
            RevenueShareModel model) {
        POMarketplacePriceModel poPriceModel = new POMarketplacePriceModel();

        PORevenueShare poRevenueShare = toPORevenueShare(model);
        poPriceModel.setRevenueShare(poRevenueShare);
        return poPriceModel;
    }

    public static POPartnerPriceModel toPOPartnerPriceModel(Marketplace mp) {
        RevenueShareModel brokerPriceModel = mp.getBrokerPriceModel();
        RevenueShareModel resellerPriceModel = mp.getResellerPriceModel();

        POPartnerPriceModel poPartnerPriceModel = new POPartnerPriceModel();

        PORevenueShare poBrokerRevenueShare = new PORevenueShare();
        PORevenueShare poResellerRevenueShare = new PORevenueShare();

        poBrokerRevenueShare.setKey(brokerPriceModel.getKey());
        poBrokerRevenueShare.setVersion(brokerPriceModel.getVersion());
        poBrokerRevenueShare
                .setRevenueShare(brokerPriceModel.getRevenueShare());

        poResellerRevenueShare.setKey(resellerPriceModel.getKey());
        poResellerRevenueShare.setVersion(resellerPriceModel.getVersion());
        poResellerRevenueShare.setRevenueShare(resellerPriceModel
                .getRevenueShare());

        poPartnerPriceModel.setRevenueShareBrokerModel(poBrokerRevenueShare);
        poPartnerPriceModel
                .setRevenueShareResellerModel(poResellerRevenueShare);
        return poPartnerPriceModel;
    }

    static POPartnerPriceModel assemblePartnerPriceModel(
            RevenueShareModel brokerPriceModel,
            RevenueShareModel resellerPriceModel) {

        POPartnerPriceModel poPartnerPriceModel = new POPartnerPriceModel();
        if (brokerPriceModel != null) {
            PORevenueShare poBrokerRevenueShare = toPORevenueShare(brokerPriceModel);
            poPartnerPriceModel
                    .setRevenueShareBrokerModel(poBrokerRevenueShare);
        }

        if (resellerPriceModel != null) {
            PORevenueShare poResellerRevenueShare = toPORevenueShare(resellerPriceModel);
            poPartnerPriceModel
                    .setRevenueShareResellerModel(poResellerRevenueShare);
        }
        return poPartnerPriceModel;
    }

    static POPartnerPriceModel assemblePOPricingForService(
            RevenueShareModel brokerPriceModel,
            RevenueShareModel resellerPriceModel) {

        POPartnerPriceModel poPartnerPriceModel = new POPartnerPriceModel();
        if (brokerPriceModel != null) {
            PORevenueShare poBrokerRevenueShare = toPORevenueShare(brokerPriceModel);
            poPartnerPriceModel
                    .setRevenueShareBrokerModel(poBrokerRevenueShare);
        }

        if (resellerPriceModel != null) {
            PORevenueShare poResellerRevenueShare = toPORevenueShare(resellerPriceModel);
            poPartnerPriceModel
                    .setRevenueShareResellerModel(poResellerRevenueShare);
        }
        return poPartnerPriceModel;
    }

    private static PORevenueShare toPORevenueShare(
            RevenueShareModel revenueShareModel) {
        ArgumentValidator.notNull("revenueShareModel", revenueShareModel);
        PORevenueShare poBrokerRevenueShare = new PORevenueShare();
        poBrokerRevenueShare.setKey(revenueShareModel.getKey());
        poBrokerRevenueShare.setVersion(revenueShareModel.getVersion());
        poBrokerRevenueShare.setRevenueShare(revenueShareModel
                .getRevenueShare());
        return poBrokerRevenueShare;
    }

    static POServiceForPricing assembleServiceForPricing(Product product) {
        POOrganization vendor = new POOrganization();
        vendor.setOrganizationId(product.getVendor().getOrganizationId());
        vendor.setOrganizationName(product.getVendor().getName());

        POServiceForPricing service = new POServiceForPricing();
        service.setKey(product.getKey());
        service.setVersion(product.getVersion());
        service.setServiceId(product.getProductId());
        service.setVendor(vendor);
        return service;
    }

    static POServicePricing assembleServicePricing(Product product) {
        POServicePricing pricing = new POServicePricing();
        pricing.setServiceForPricing(assembleServiceForPricing(product));
        CatalogEntry ce = product.getCatalogEntries().get(0);
        pricing.setPartnerPriceModel(assemblePOPricingForService(
                ce.getBrokerPriceModel(), ce.getResellerPriceModel()));
        return pricing;
    }

}
