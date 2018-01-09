/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 20, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.billing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.intf.MarketplaceService;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOUser;

/**
 * Creates data relevant for billing.
 * 
 * @author barzu
 */
public class BillingSetupTest {

    private VOFactory factory = new VOFactory();
    private WebserviceTestSetup setup;
    private MarketplaceService mpSrvOperator;
    private VOMarketplace mpDE;
    private MarketplaceService mpSrvAsDEOwner;
    private VOMarketplace mpJP;
    private MarketplaceService mpSrvAsJPOwner;

    @Before
    public void setup() throws Exception {

        setup = new WebserviceTestSetup();

        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();

        // add currencies
        WebserviceTestBase.getOperator().addCurrency("EUR");
        WebserviceTestBase.getOperator().addCurrency("USD");
        WebserviceTestBase.getOperator().addCurrency("JPY");

        mpSrvOperator = ServiceFactory.getDefault().getMarketPlaceService(
                WebserviceTestBase.getPlatformOperatorKey(),
                WebserviceTestBase.getPlatformOperatorPassword());

        createSupplierWith3MP1Currency();
        createSupplierWith1MP2Currencies();
        createSupplierWith3MP2Currencies();

        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();
    }

    private void createSupplierWith3MP1Currency() throws Exception {
        VOOrganization supplier = setup.createSupplier("Sicher-und-Heil");
        mpSrvAsDEOwner = setup.getMpSrvAsSupplier();

        // create "Local" marketplace
        VOMarketplace mpLocal = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(supplier.getOrganizationId(), false,
                        "Local Marketplace of Sicher-und-Heil"));

        VOUser admin = setup.getIdentitySrvAsSupplier().getCurrentUserDetails();
        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.MARKETPLACE_OWNER);
        roles.add(UserRoleType.ORGANIZATION_ADMIN);
        roles.add(UserRoleType.SERVICE_MANAGER);
        roles.add(UserRoleType.TECHNOLOGY_MANAGER);
        setup.getIdentitySrvAsSupplier().setUserRoles(admin, roles);

        // create technical service
        setup.createTechnicalService();

        // add services to "Local" marketplace
        VOServiceDetails serviceDetails = setup.createService("ExampleTrial",
                mpLocal);
        VOPriceModel priceModel = factory.createPriceModelVO("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(5.99));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(10));
        VOService service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("John_Customer");
        setup.createSubscription("ExampleTrial_Sub", service);

        // create "Marketplace DE"
        mpDE = mpSrvOperator.createMarketplace(factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "Marketplace DE"));
        // add services to global "Marketplace DE"
        serviceDetails = setup.createService("MegaOffice", mpDE);
        priceModel = factory.createPriceModelVO("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(15));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(20));
        service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Hans_Customer");
        setup.createSubscription("MegaOffice_Sub", service);

        // create "Marketplace AT"
        VOMarketplace mpAT = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(supplier.getOrganizationId(), false,
                        "Marketplace AT"));
        // add services to global "Marketplace AT"
        serviceDetails = setup.createService("Guat", mpAT);
        priceModel = factory.createPriceModelVO("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(29));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(30));
        service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Schorsch_Customer");
        setup.createSubscription("BigBrother_Sub", service);
    }

    private void createSupplierWith1MP2Currencies() throws Exception {
        VOOrganization supplier = setup.createSupplier("Kamikaze-and-Co");
        mpSrvAsJPOwner = setup.getMpSrvAsSupplier();

        // create "Marketplace JP"
        mpJP = mpSrvOperator.createMarketplace(factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "Marketplace JP"));

        VOUser admin = setup.getIdentitySrvAsSupplier().getCurrentUserDetails();
        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.MARKETPLACE_OWNER);
        roles.add(UserRoleType.ORGANIZATION_ADMIN);
        roles.add(UserRoleType.SERVICE_MANAGER);
        roles.add(UserRoleType.TECHNOLOGY_MANAGER);
        setup.getIdentitySrvAsSupplier().setUserRoles(admin, roles);

        // create technical service
        setup.createTechnicalService();

        // add service and subscription in JPY on "Marketplace JP"
        VOServiceDetails serviceDetails = setup.createService("Sushi_Service",
                mpJP);
        VOPriceModel priceModel = factory.createPriceModelVO("JPY");
        priceModel.setOneTimeFee(BigDecimal.valueOf(100));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(40));
        VOService service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Tosihiro_Customer");
        setup.createSubscription("SushiService_Sub", service);

        // add service and subscription in USD on "Marketplace JP"
        serviceDetails = setup.createService("McDonalds_Service", mpJP);
        priceModel = factory.createPriceModelVO("USD");
        priceModel.setOneTimeFee(BigDecimal.valueOf(5));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(60));
        service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("FatJoe_Customer");
        setup.createSubscription("McDonaldsService_Sub", service);
    }

    private void createSupplierWith3MP2Currencies() throws Exception {
        VOOrganization supplier = setup.createSupplier("Yamamoto-Line-Ltd");

        // create "Local" marketplace
        VOMarketplace mpLocal = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(supplier.getOrganizationId(), false,
                        "Local Marketplace of Yamamoto-Line-Ltd"));

        // create technical service
        setup.createTechnicalService();

        // add service and subscription in JPY on local marketplace
        VOServiceDetails serviceDetails = setup.createService("Sake_Service",
                mpLocal);
        VOPriceModel priceModel = factory.createPriceModelVO("JPY");
        priceModel.setOneTimeFee(BigDecimal.valueOf(39));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(70));
        VOService service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Kawasaki_Customer");
        setup.createSubscription("Sake_Service_Sub", service);

        // add the supplier to "Marketplace JP"
        mpSrvAsJPOwner.addOrganizationsToMarketplace(
                Collections.singletonList(supplier.getOrganizationId()),
                mpJP.getMarketplaceId());

        // add service and subscription in JPY on "Marketplace JP"
        serviceDetails = setup.createService("Tofu_Service", mpJP);
        priceModel = factory.createPriceModelVO("JPY");
        priceModel.setOneTimeFee(BigDecimal.valueOf(49));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(80));
        service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Musashi_Customer");
        setup.createSubscription("Tofu_Service_Sub", service);

        // add the supplier to "Marketplace DE"
        mpSrvAsDEOwner.addOrganizationsToMarketplace(
                Collections.singletonList(supplier.getOrganizationId()),
                mpDE.getMarketplaceId());

        // add service and subscription in EUR on "Marketplace DE"
        serviceDetails = setup.createService("Brezn_Service", mpDE);
        priceModel = factory.createPriceModelVO("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(9.99));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(100));
        service = setup.savePriceModelAndActivateService(priceModel,
                serviceDetails);
        setup.createCustomer("Horst_Customer");
        setup.createSubscription("Brezn_Service_Sub", service);
    }

    @Test
    public void testSetup() throws Exception {
    }

}
