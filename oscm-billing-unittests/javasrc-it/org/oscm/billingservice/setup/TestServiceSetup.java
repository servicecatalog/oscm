/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 15.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.service.POServiceForPublish;
import org.oscm.internal.service.PublishService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Setup of test services
 * 
 * @author baumann
 */
public class TestServiceSetup {

    private final TestContainer container;
    private final ServiceProvisioningService srvProvService;
    private final MarketplaceService mplService;
    private final PublishService publishService;

    public TestServiceSetup(TestContainer container) {
        this.container = container;
        srvProvService = container.get(ServiceProvisioningService.class);
        mplService = container.get(MarketplaceService.class);
        publishService = container.get(PublishService.class);
    }

    public VOServiceDetails createPublishAndActivateMarketableService(
            long supplierKey, String serviceId, TestService testService,
            TestPriceModel testPriceModel, VOTechnicalService technicalService,
            VOMarketplace marketplace) throws Exception {

        VOServiceDetails voServiceDetails = createAndPublishMarketableService(
                supplierKey, serviceId, testService, testPriceModel,
                technicalService, marketplace);

        return activateMarketableService(voServiceDetails);
    }

    public VOServiceDetails createPublishAndActivateMarketableService(
            long supplierKey, String serviceId, TestService testService,
            TestPriceModel testPriceModel, int freePeriod,
            VOTechnicalService technicalService, VOMarketplace marketplace)
            throws Exception {

        VOServiceDetails voServiceDetails = createAndPublishMarketableService(
                supplierKey, serviceId, testService, testPriceModel,
                freePeriod, technicalService, marketplace);

        return activateMarketableService(voServiceDetails);
    }

    public VOServiceDetails createAndPublishMarketableService(long supplierKey,
            String serviceId, TestService testService,
            TestPriceModel testPriceModel, VOTechnicalService technicalService,
            VOMarketplace marketplace) throws Exception {
        return createAndPublishMarketableService(supplierKey, serviceId,
                testService, testPriceModel, null, technicalService,
                marketplace);
    }

    public VOServiceDetails createAndPublishMarketableService(long supplierKey,
            String serviceId, TestService testService,
            TestPriceModel testPriceModel, int freePeriod,
            VOTechnicalService technicalService, VOMarketplace marketplace)
            throws Exception {
        return createAndPublishMarketableService(supplierKey, serviceId,
                testService, testPriceModel, new Integer(freePeriod),
                technicalService, marketplace);
    }

    private VOServiceDetails createAndPublishMarketableService(
            long supplierKey, String serviceId, TestService testService,
            TestPriceModel testPriceModel, Integer freePeriod,
            VOTechnicalService technicalService, VOMarketplace marketplace)
            throws Exception {
        container.login(supplierKey, UserRoleType.ORGANIZATION_ADMIN.name(),
                UserRoleType.SERVICE_MANAGER.name());

        VOService voService = VOServiceFactory.createVOService(serviceId,
                testService, technicalService);
        VOServiceDetails voServiceDetails = srvProvService.createService(
                technicalService, voService, null);

        VOPriceModel voPriceModel = VOPriceModelFactory.createVOPriceModel(
                testPriceModel, voServiceDetails);
        if (freePeriod != null) {
            voPriceModel.setFreePeriod(freePeriod.intValue());
        }
        voServiceDetails = srvProvService.savePriceModel(voServiceDetails,
                voPriceModel);

        return publishToMarketplace(voServiceDetails, true, marketplace);
    }

    private VOServiceDetails publishToMarketplace(VOService service,
            boolean isPublic, VOMarketplace marketplace) throws Exception {
        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setAnonymousVisible(isPublic);
        voCE.setMarketplace(marketplace);
        return mplService.publishService(service, Arrays.asList(voCE));
    }

    /**
     * Publish a marketable service and activate it
     * 
     * @param vendorData
     *            the vendor of this service
     * @param marketplace
     *            the marketplace to publish the service
     * @param service
     *            the service
     * @return the service details
     * @throws Exception
     */
    public VOServiceDetails publishAndActivateMarketableService(
            VendorData vendorData, VOMarketplace marketplace, VOService service)
            throws Exception {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        publishToMarketplace(service, true, marketplace);
        return activateMarketableService(service);
    }

    /**
     * Publish a supplier service and update its resale permissions. If the
     * service is already published, only the resale permissions are updated.
     * 
     * @param supplierKey
     *            the supplier key
     * @param supplierService
     *            a service of that supplier
     * @param grantedOrgs
     *            the broker/reseller organizations that should have a resale
     *            permission
     * @param revokedOrgs
     *            the broker/reseller organizations that should not have a
     *            resale permission
     * @throws Exception
     */
    public void updateAndPublishService(long supplierKey,
            VOService supplierService, List<VendorData> grantedOrgs,
            List<VendorData> revokedOrgs) throws Exception {
        container.login(supplierKey, UserRoleType.ORGANIZATION_ADMIN.name(),
                UserRoleType.SERVICE_MANAGER.name());

        Response response = publishService.getServiceDetails(supplierService
                .getKey());
        POServiceForPublish serviceForPublish = response
                .getResult(POServiceForPublish.class);

        publishService.updateAndPublishService(serviceForPublish,
                createResalePermissionList(supplierService, grantedOrgs),
                createResalePermissionList(supplierService, revokedOrgs));
    }

    private List<POResalePermissionDetails> createResalePermissionList(
            VOService supplierService, List<VendorData> partnerOrgs) {
        List<POResalePermissionDetails> resalePermissions = new ArrayList<POResalePermissionDetails>();
        for (VendorData partner : partnerOrgs) {
            resalePermissions.add(newPOResalePermissionDetails(supplierService,
                    partner));
        }
        return resalePermissions;
    }

    private POResalePermissionDetails newPOResalePermissionDetails(
            VOService supplierService, VendorData partner) {
        POResalePermissionDetails permission = new POResalePermissionDetails();
        permission.setGrantor(newPOOrganization(supplierService.getSellerId(),
                supplierService.getSellerName()));
        permission.setGrantee(newPOOrganization(partner.getOrganizationId(),
                partner.getOrganizationName()));
        permission.setService(newPOServiceDetails(supplierService));
        permission.setOfferingType(partner.getOfferingType());
        return permission;
    }

    private POOrganization newPOOrganization(String organizationId,
            String organizationName) {
        POOrganization poOrg = new POOrganization();
        poOrg.setOrganizationId(organizationId);
        poOrg.setOrganizationName(organizationName);
        return poOrg;
    }

    private POServiceDetails newPOServiceDetails(VOService service) {
        POServiceDetails serviceDetails = new POServiceDetails();
        serviceDetails.setKey(service.getKey());
        serviceDetails.setServiceId(service.getServiceId());
        return serviceDetails;
    }

    public VOServiceDetails activateMarketableService(VOService service)
            throws Exception {
        return srvProvService.getServiceDetails(srvProvService
                .activateService(service));
    }

    public VOServiceDetails deactivateMarketableService(VOService service)
            throws Exception {
        return srvProvService.getServiceDetails(srvProvService
                .deactivateService(service));
    }

    public VOServiceDetails deactivateMarketableService(long supplierKey,
            VOService service) throws Exception {
        container.login(supplierKey, UserRoleType.SERVICE_MANAGER.name());
        return deactivateMarketableService(service);
    }

    /**
     * Deactivates a marketable service and deletes it
     * 
     * @param service
     *            the marketable service
     */
    public void deleteMarketableService(VOService service) throws Exception {
        service = deactivateMarketableService(service);
        srvProvService.deleteService(service);
    }

    public VOServiceDetails savePriceModelForCustomer(
            VOServiceDetails serviceDetails, TestPriceModel testPriceModel,
            VOOrganization customer) throws Exception {
        VOPriceModel priceModel = VOPriceModelFactory.createVOPriceModel(
                testPriceModel, serviceDetails);

        return srvProvService.savePriceModelForCustomer(serviceDetails,
                priceModel, customer);
    }

    /**
     * Deactivate the given supplier service and register compatible one's.
     * 
     * @param supplierKey
     * @param service
     * @param compatibleService
     * @throws Exception
     */
    public VOServiceDetails registerCompatibleServices(long supplierKey,
            VOService service, VOService... compatibleServices)
            throws Exception {
        container.login(supplierKey, UserRoleType.SERVICE_MANAGER.name());
        service = srvProvService.deactivateService(service);
        srvProvService.setCompatibleServices(service,
                Arrays.asList(compatibleServices));
        srvProvService.activateService(service);
        return getServiceDetails(supplierKey, service);
    }

    public VOServiceDetails getServiceDetails(long supplierKey,
            VOService service) throws Exception {
        container.login(supplierKey, UserRoleType.SERVICE_MANAGER.name());
        return srvProvService.getServiceDetails(service);
    }

    public VOServiceDetails getServiceDetails(VendorData vendorData,
            VOService service) throws Exception {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        return srvProvService.getServiceDetails(service);
    }

    /**
     * Imports the technical services from the given XML
     * 
     * @param tsXml
     *            the technical service XML
     */
    public void importTechnicalServices(String tsXml) throws Exception {
        VOTechServiceFactory.importTechnicalServices(srvProvService, tsXml);
    }

    public VOTechnicalService getTechnicalService(String technicalServiceId)
            throws Exception {
        List<VOTechnicalService> techServices = srvProvService
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        for (VOTechnicalService techService : techServices) {
            if (techService.getTechnicalServiceId().equals(technicalServiceId)) {
                return techService;
            }
        }
        return null;
    }

    public void deleteTechnicalService(VOTechnicalService techService)
            throws Exception {
        srvProvService.deleteTechnicalService(techService);
    }

    /**
     * Retrieves a list of the marketable services provided by the vendor's
     * organization.
     * 
     * @param vendorData
     * @return a VOService list
     */
    public List<VOService> getSuppliedServices(VendorData vendorData) {
        container.login(vendorData.getAdminKey(),
                vendorData.getAdminUserRoles());
        List<VOService> suppliedServices = srvProvService.getSuppliedServices();
        return suppliedServices;
    }

    /**
     * Get the resale copy of the given supplier service for the given partner
     * organization
     * 
     * @param supplierService
     *            supplier service
     * @param partnerData
     *            broker/reseller data
     * @return
     */
    public VOServiceDetails getResaleCopy(VOService supplierService,
            VendorData partnerData) throws Exception {
        for (VOService service : getSuppliedServices(partnerData)) {
            if (service.getServiceId().equals(supplierService.getServiceId())
                    && service.getTechnicalId().equals(
                            supplierService.getTechnicalId())) {
                return getServiceDetails(partnerData, service);
            }
        }
        return null;
    }

    public VOServiceDetails updateMarketableService(long supplierKey,
            VOServiceDetails service) throws Exception {
        container.login(supplierKey, UserRoleType.ORGANIZATION_ADMIN.name(),
                UserRoleType.SERVICE_MANAGER.name());
        VOService deactivatedService = srvProvService
                .deactivateService(service);
        service.setVersion(deactivatedService.getVersion());
        service.setStatus(deactivatedService.getStatus());
        srvProvService.updateService(service, null);
        srvProvService.activateService(service);
        return getServiceDetails(supplierKey, service);
    }

}
