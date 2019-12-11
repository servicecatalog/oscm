/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                            
 *                                                                              
 *  Creation Date: 27.10.2011                                                      
 *                                                                              
 *  Completion Time: 27.10.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.converter.api.VOConverter;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.intf.AccountService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ServiceProvisioningService;
import org.oscm.types.enumtypes.PaymentInfoType;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOServiceDetails;
import org.oscm.vo.VOTechnicalService;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.PaymentTypeFactory;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.TSXMLForWebService;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/** @author kulle */
@Ignore
public class SavePaymentConfigurationWSTest {

  private static final int COUNT_CUSTOMER = 2;
  private static VOFactory factory = new VOFactory();
  private static final String NAME_SUPPLIER = "supplier_" + System.currentTimeMillis();
  private static final String NAME_MARKETPLACE = "marketplace_" + System.currentTimeMillis();

  private VOOrganization supplierOrganization;
  private String supplierAdminKey;
  private VOMarketplace marketplace;
  private WebserviceTestSetup setup;
  private static AccountService accountService_Reseller;
  private List<VOOrganization> customers = new ArrayList<VOOrganization>();

  @Before
  public void setup() throws MessagingException, Exception {
    WebserviceTestBase.getMailReader().deleteMails();

    // add currency
    WebserviceTestBase.getOperator().addCurrency(WebserviceTestBase.CURRENCY_EUR);
    PaymentTypeFactory.preparePaymentType();
    // clean the mails
    setup = new WebserviceTestSetup();

    // Create reseller with DefaultPaymentType with
    // PaymentInfoType.CREDIT_CARD
    setup.createReseller("Rs1");
    accountService_Reseller =
        ServiceFactory.getDefault()
            .getAccountService(setup.getResellerUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);

    // OPERATOR: create supplier
    VOUserDetails supplierAdmin = factory.createUserVO("admin" + System.currentTimeMillis());
    supplierOrganization = factory.createOrganizationVO();
    supplierOrganization.setName(NAME_SUPPLIER);
    org.oscm.internal.vo.VOOrganization internalVOOrg =
        VOConverter.convertToUp(supplierOrganization);
    internalVOOrg.setOperatorRevenueShare(BigDecimal.ZERO);

    supplierOrganization =
        VOConverter.convertToApi(
            WebserviceTestBase.getOperator()
                .registerOrganization(
                    internalVOOrg,
                    null,
                    VOConverter.convertToUp(supplierAdmin),
                    null,
                    null,
                    org.oscm.internal.types.enumtypes.OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    org.oscm.internal.types.enumtypes.OrganizationRoleType.SUPPLIER));
    supplierAdminKey = WebserviceTestBase.readLastMailAndSetCommonPassword();
    supplierAdmin.setKey(Long.parseLong(supplierAdminKey));

    VOPSPAccount pspAccount = new VOPSPAccount();
    pspAccount.setPspIdentifier("psp1");
    pspAccount.setPsp(WebserviceTestBase.getOperator().getPSPs().get(0));
    WebserviceTestBase.getOperator()
        .savePSPAccount(VOConverter.convertToUp(supplierOrganization), pspAccount);

    // OPERATOR: enable payment type invoice for suppliers
    Set<String> types = new HashSet<String>();
    types.add(PaymentInfoType.INVOICE.name());
    types.add(PaymentInfoType.CREDIT_CARD.name());
    types.add(PaymentInfoType.DIRECT_DEBIT.name());
    ServiceFactory.getDefault()
        .getOperatorService()
        .addAvailablePaymentTypes(VOConverter.convertToUp(supplierOrganization), types);

    // SUPPLIER: enable payment type invoice
    AccountService accountService =
        ServiceFactory.getDefault()
            .getAccountService(supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
    Set<VOPaymentType> defaultPaymentTypes = accountService.getDefaultPaymentConfiguration();
    VOPaymentType voPaymentType =
        WebserviceTestBase.getPaymentTypeVO(
            accountService.getAvailablePaymentTypesForOrganization(), PaymentInfoType.INVOICE);
    defaultPaymentTypes.add(voPaymentType);
    VOOrganizationPaymentConfiguration c = new VOOrganizationPaymentConfiguration();
    c.setEnabledPaymentTypes(defaultPaymentTypes);
    c.setOrganization(supplierOrganization);
    accountService.savePaymentConfiguration(
        defaultPaymentTypes, Collections.singletonList(c), defaultPaymentTypes, null);

    // OPERATOR: create "Marketplace"
    MarketplaceService srvMarketplace =
        ServiceFactory.getDefault()
            .getMarketPlaceService();
    marketplace =
        srvMarketplace.createMarketplace(
            factory.createMarketplaceVO(
                supplierOrganization.getOrganizationId(), false, NAME_MARKETPLACE));

    // SUPPLIER: create technical service
    ServiceProvisioningService serviceProvisioningService =
        ServiceFactory.getDefault()
            .getServiceProvisioningService(supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
    String tsxml =
        TSXMLForWebService.createTSXMLWithAllowingOnBehalfActingConnectable(
            Boolean.FALSE.toString());
    VOTechnicalService techSrv =
        WebserviceTestBase.createTechnicalService(tsxml, serviceProvisioningService);

    // SUPPLIER: create marketable service
    VOServiceDetails serviceDetails =
        serviceProvisioningService.createService(
            techSrv,
            factory.createMarketableServiceVO(
                "service" + "_" + WebserviceTestBase.createUniqueKey()),
            null);
    VOPriceModel priceModel = factory.createPriceModelVO(WebserviceTestBase.CURRENCY_EUR);
    priceModel.setOneTimeFee(BigDecimal.valueOf(100));
    priceModel.setPricePerPeriod(BigDecimal.valueOf(40));
    serviceDetails = serviceProvisioningService.savePriceModel(serviceDetails, priceModel);

    // SUPPLIER: publish service & activate
    srvMarketplace =
        ServiceFactory.getDefault()
            .getMarketPlaceService(supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);
    WebserviceTestBase.publishToMarketplace(serviceDetails, true, srvMarketplace, marketplace);
    serviceProvisioningService.activateService(serviceDetails);

    // SUPPLIER:register customers
    for (int i = 0; i < COUNT_CUSTOMER; i++) {
      VOUserDetails customerAdmin = factory.createUserVO("admin" + System.currentTimeMillis());
      VOOrganization customerOrganization = factory.createOrganizationVO();
      customerOrganization.setName("customer" + i + "_" + System.currentTimeMillis());
      customers.add(
          accountService.registerKnownCustomer(
              customerOrganization, customerAdmin, null, marketplace.getMarketplaceId()));
    }
  }

  @Test
  public void testSavePaymentConfiguration() throws Exception {
    AccountService accountService =
        ServiceFactory.getDefault()
            .getAccountService(supplierAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);

    Set<VOPaymentType> availablePaymentTypes = accountService.getAvailablePaymentTypes();

    // custom organization configurations
    List<VOOrganizationPaymentConfiguration> customerConfigs =
        new ArrayList<VOOrganizationPaymentConfiguration>();
    for (VOOrganization c : customers) {
      VOOrganizationPaymentConfiguration voCustOrgConf = new VOOrganizationPaymentConfiguration();
      voCustOrgConf.setEnabledPaymentTypes(availablePaymentTypes);
      voCustOrgConf.setOrganization(c);
      customerConfigs.add(voCustOrgConf);
    }

    Set<VOPaymentType> customerConf = new HashSet<VOPaymentType>();
    Set<VOPaymentType> serviceConf = new HashSet<VOPaymentType>();
    Iterator<VOPaymentType> iterator = availablePaymentTypes.iterator();
    VOPaymentType voPaymentType = iterator.next();
    customerConf.add(voPaymentType);
    serviceConf.add(voPaymentType);
    customerConf.add(iterator.next());

    accountService.savePaymentConfiguration(customerConf, customerConfigs, serviceConf, null);
  }

  @Test
  public void registerKnownCustomer_WithDefaultPaymentType() throws Exception {
    VOUserDetails customerAdmin = factory.createUserVO("admin" + System.currentTimeMillis());
    VOOrganization customerOrganization = factory.createOrganizationVO();
    VOOrganization customer =
        accountService_Reseller.registerKnownCustomer(
            customerOrganization, customerAdmin, null, marketplace.getMarketplaceId());
    List<VOOrganizationPaymentConfiguration> confs =
        accountService_Reseller.getCustomerPaymentConfiguration();
    for (VOOrganizationPaymentConfiguration conf : confs) {
      if (conf.getOrganization().getKey() == customer.getKey()) {
        assertEquals(1, conf.getEnabledPaymentTypes().size());
        for (VOPaymentType paymentType : conf.getEnabledPaymentTypes()) {
          assertEquals(PaymentInfoType.CREDIT_CARD.name(), paymentType.getPaymentTypeId());
        }
      }
    }
  }
}
