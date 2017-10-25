/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Product;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.assembler.CatalogEntryAssembler;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.validation.ArgumentValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

@Stateless
@Remote(PublishService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PublishServiceBean implements PublishService {

    @EJB(beanInterface = ServiceProvisioningService.class)
    ServiceProvisioningService sps;

    @EJB(beanInterface = ServiceProvisioningServiceLocal.class)
    ServiceProvisioningServiceLocal spsl;

    @EJB(beanInterface = ServiceProvisioningPartnerServiceLocal.class)
    ServiceProvisioningPartnerServiceLocal partnerSrvProv;

    @EJB(beanInterface = MarketplaceService.class)
    MarketplaceService ms;

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    MarketplaceServiceLocal msl;

    @EJB(beanInterface = CategorizationService.class)
    CategorizationService cs;

    @EJB(beanInterface = PricingService.class)
    PricingService ps;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    private OrganizationDao organizationDao;

    @Resource
    SessionContext sessionCtx;

    @PostConstruct
    public void initBean() {
        organizationDao = new OrganizationDao(ds);
    }

    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public Response getServiceDetails(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException {
        final VOService srv = new VOService();
        srv.setKey(serviceKey);
        final VOServiceDetails voServiceDetails = sps.getServiceDetails(srv);
        final POServiceForPublish service = new POServiceForPublish();
        service.setService(voServiceDetails);
        service.setPartOfUpgradePath(spsl.isPartOfUpgradePath(serviceKey));
        final List<VOCatalogEntry> catalogEntries = ms
                .getMarketplacesForService(srv);
        if (!catalogEntries.isEmpty()) {
            service.setCatalogEntry(catalogEntries.get(0));
        }
        Response response = ps
                .getPartnerRevenueShareForService(new POServiceForPricing(
                        serviceKey, 0));
        final POPartnerPriceModel partnerPriceModel = response
                .getResult(POPartnerPriceModel.class);
        response = ps.getOperatorRevenueShare(serviceKey);
        final POOperatorPriceModel operatorPriceModel = response
                .getResult(POOperatorPriceModel.class);
        return new Response(service, partnerPriceModel, operatorPriceModel);
    }

    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public Response updateAndPublishService(POServiceForPublish service,
            List<POResalePermissionDetails> permissionsToGrant,
            List<POResalePermissionDetails> permissionsToRevoke)
            throws ValidationException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            ServiceOperationException, ConcurrentModificationException,
            OrganizationAuthorityException, ServiceStateException {
        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("service.catalogentry",
                service.getCatalogEntry());
        ArgumentValidator.notNull("service.catalogentry.marketplace", service
                .getCatalogEntry().getMarketplace());
        // read public flag from service beforehand since create method will
        // always return false
        cleanupParameter(service.getService());
        VOCatalogEntry voCe = toVOCatalogEntry(service);
        CatalogEntry ce = CatalogEntryAssembler.toCatalogEntry(voCe);

        try {
            checkIfServiceIsModified(service.getService());
            msl.publishServiceWithPermissions(service.getService().getKey(),
                    ce, voCe.getCategories(), permissionsToGrant,
                    permissionsToRevoke);
        } catch (ValidationException | ObjectNotFoundException
                | ServiceOperationException | NonUniqueBusinessKeyException
                | ServiceStateException | OrganizationAuthorityException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return new Response();
    }

    private void checkIfServiceIsModified(VOServiceDetails service)
            throws ObjectNotFoundException, ConcurrentModificationException {
        Product product = ds.getReference(Product.class, service.getKey());
        BaseAssembler.verifyVersionAndKey(product, service);
    }

    private static VOCatalogEntry toVOCatalogEntry(POServiceForPublish service) {
        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setAnonymousVisible(service.getCatalogEntry().isAnonymousVisible());
        ce.setCategories(service.getCatalogEntry().getCategories());
        ce.setKey(service.getCatalogEntry().getKey());
        if (service.getCatalogEntry().getMarketplace().getMarketplaceId() != null) {
            ce.setMarketplace(service.getCatalogEntry().getMarketplace());
        }
        ce.setService(service.getCatalogEntry().getService());
        ce.setVersion(service.getCatalogEntry().getVersion());
        ce.setVisibleInCatalog(service.getCatalogEntry().isVisibleInCatalog());
        return ce;
    }

    public Response getCategoriesAndRvenueShare(String marketplaceId,
            String locale) throws ObjectNotFoundException {
        final List<VOCategory> list = cs.getCategories(marketplaceId, locale);
        final Response response = ps.getMarketplaceRevenueShares(marketplaceId);
        final POMarketplacePriceModel priceModel = response
                .getResult(POMarketplacePriceModel.class);
        final Response response2 = ps
                .getPartnerRevenueSharesForMarketplace(marketplaceId);
        return new Response(list, priceModel,
                response2.getResult(POPartnerPriceModel.class));
    }

    private static void cleanupParameter(VOServiceDetails service) {
        if (service != null) {
            final Iterator<VOParameter> it = service.getParameters().iterator();
            while (it.hasNext()) {
                final VOParameter p = it.next();
                if (!p.isConfigurable()
                        && (p.getValue() == null || p.getValue().trim()
                                .length() == 0)) {
                    it.remove();
                }
            }
        }
    }

    @RolesAllowed("SERVICE_MANAGER")
    public Response getBrokers(long serviceKey) {
        return new Response(organizationDao.getOrganizations(OrganizationRoleType.BROKER,
                serviceKey));
    }

    @RolesAllowed("SERVICE_MANAGER")
    public Response getResellers(long serviceKey) {
        return new Response(organizationDao.getOrganizations(OrganizationRoleType.RESELLER,
                serviceKey));
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public Response getTemplateServices() {
        List<POServiceDetails> result = new ArrayList<>();
        for (Product p : partnerSrvProv.loadSuppliedTemplateServices()) {
            String OrganizationId = (p.getTemplate() == null ? null : p
                    .getTemplate().getVendor().getOrganizationId());
            result.add(new POServiceDetails(p.getKey(), p.getVersion(),
                    OrganizationId, ProductAssembler.getProductId(p)));
        }
        return new Response(result);
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }
}
