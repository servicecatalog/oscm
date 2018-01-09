/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 03.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Setup of basic test data
 * 
 * @author baumann
 * 
 */

public class TestBasicSetup {

    public static final String CUSTOMER_ORG_NAME = "GreenPeaceCustomer";
    public static final String CUSTOMER_USER1_ID = "GreenPeaceCustomerUser1";
    public static final String CUSTOMER_USER2_ID = "GreenPeaceCustomerUser2";
    public static final String CUSTOMER_USER3_ID = "GreenPeaceCustomerUser3";
    public static final String CUSTOMER_USER4_ID = "GreenPeaceCustomerUser4";
    public static final String CUSTOMER_USER5_ID = "GreenPeaceCustomerUser5";
    public static final String CUSTOMER_USER6_ID = "GreenPeaceCustomerUser6";
    public static final String SECOND_CUSTOMER_ORG_NAME = "GreenPeaceSecondCustomer";
    public static final String SECOND_CUSTOMER_USER1_ID = "GreenPeaceSecondCustomerUser1";
    public static final String SECOND_CUSTOMER_USER2_ID = "GreenPeaceSecondCustomerUser2";

    private final DataService dataService;
    private final OperatorService operatorService;

    private Organization platformOperatorOrg;
    private long platformOperatorUserKey;

    private VOOrganization supplier;
    private VOUser supplierAdmin;
    private VOUser technologyProviderUser;
    private VOMarketplace supplierMarketplace;

    private VOOrganization customer;
    private VOUser customerAdmin;
    private VOUserDetails customerUser1;
    private VOUserDetails customerUser2;
    private VOUserDetails customerUser3;
    private VOUserDetails customerUser4;
    private VOUserDetails customerUser5;
    private VOUserDetails customerUser6;

    private VOOrganization secondCustomer;
    private VOUser secondCustomerAdmin;
    private VOUserDetails secondCustomerUser1;
    private VOUserDetails secondCustomerUser2;

    private VOTechnicalService technicalService;
    private VOTechnicalService technicalServiceAsync;

    private final TestOrganizationSetup orgSetup;
    private final TestPaymentSetup paymentSetup;
    private final TestServiceSetup serviceSetup;

    private static int uniqueKeySuffix = 0;

    public static synchronized String createUniqueKey() {
        uniqueKeySuffix++;
        return Long.toString(System.currentTimeMillis())
                + Integer.toString(uniqueKeySuffix);
    }

    public TestBasicSetup(TestContainer container) {
        dataService = container.get(DataService.class);
        operatorService = container.get(OperatorService.class);

        orgSetup = new TestOrganizationSetup(container);
        paymentSetup = new TestPaymentSetup(container);
        serviceSetup = new TestServiceSetup(container);
    }

    /**
     * Creates the basic data (OrganizationRoles,PaymentType,
     * SupportedCountries, and UserRoles)
     * 
     * @param basicSetupRequired
     *            true if the basic setup is required, otherwise false.
     */
    public void createBasicData(boolean basicSetupRequired)
            throws NonUniqueBusinessKeyException {
        if (basicSetupRequired) {
            EJBTestBase.createOrganizationRoles(dataService);
            EJBTestBase.createPaymentTypes(dataService);
            SupportedCountries.createSomeSupportedCountries(dataService);
            EJBTestBase.createUserRoles(dataService);
            EJBTestBase.createBillingAdapter(dataService);
        }
    }

    /**
     * Creates the operator organization test data and a user with platform
     * operator.
     */
    public void registerOperatorOrganisation() throws Exception,
            NonUniqueBusinessKeyException {
        platformOperatorOrg = Organizations.createPlatformOperator(dataService);
        PlatformUser platformOperatorUser = Organizations.createUserForOrg(
                dataService, platformOperatorOrg, true,
                "AdminPlatformOperatorOrg");
        platformOperatorUserKey = platformOperatorUser.getKey();

        PlatformUsers.grantRoles(dataService, platformOperatorUser,
                UserRoleType.PLATFORM_OPERATOR);
    }

    /**
     * Adds EUR, USD and JPY to the system.
     */
    public void addCurrencies() throws Exception {
        operatorService.addCurrency("EUR");
        operatorService.addCurrency("USD");
        operatorService.addCurrency("JPY");
    }

    /**
     * Creates the supplier organization test data and assign the user
     * technology provider, service and technology manager role.
     */
    public void registerDefaultSupplierOrganisation() throws Exception {
        String orgName = "GreenPeaceOrgSupplier";
        String supplierUserId = orgName + "_" + createUniqueKey();

        supplier = orgSetup.createOrganization(supplierUserId, orgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        supplierAdmin = orgSetup.getUser(supplierUserId, false);
    }

    /**
     * Creates the marketplace test data.
     */
    public void createSupplierMarketplace() throws Exception {
        supplierMarketplace = orgSetup.createMarketplace("MARKETPLACE_DE",
                true, supplier);
    }

    public void createTechnologyProviderUser() throws Exception {
        List<UserRoleType> userRoles = Arrays
                .asList(new UserRoleType[] { UserRoleType.SERVICE_MANAGER,
                        UserRoleType.TECHNOLOGY_MANAGER });

        technologyProviderUser = orgSetup.createUser("GreenPeaceOrgTPUser_"
                + createUniqueKey(), supplier.getOrganizationId(), userRoles,
                supplierMarketplace.getMarketplaceId());
    }

    public void registerTestCustomer() throws Exception {
        String adminId = CUSTOMER_ORG_NAME + "Admin_" + createUniqueKey();
        customer = orgSetup.registerCustomer(CUSTOMER_ORG_NAME,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE, adminId,
                supplierMarketplace.getMarketplaceId(), null);
        customerAdmin = orgSetup.getUser(adminId, false);
    }

    public void registerSecondTestCustomer() throws Exception {
        String adminId = SECOND_CUSTOMER_ORG_NAME + "Admin_"
                + createUniqueKey();
        secondCustomer = orgSetup.registerCustomer(SECOND_CUSTOMER_ORG_NAME,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE, adminId,
                supplierMarketplace.getMarketplaceId(), null);
        secondCustomerAdmin = orgSetup.getUser(adminId, false);
    }

    public void registerCustomerUsers() throws Exception {
        customerUser1 = orgSetup.createUser(CUSTOMER_USER1_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        customerUser2 = orgSetup.createUser(CUSTOMER_USER2_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        customerUser3 = orgSetup.createUser(CUSTOMER_USER3_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        customerUser4 = orgSetup.createUser(CUSTOMER_USER4_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        customerUser5 = orgSetup.createUser(CUSTOMER_USER5_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        customerUser6 = orgSetup.createUser(CUSTOMER_USER6_ID,
                getCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
    }

    public void registerSecondCustomerUsers() throws Exception {
        secondCustomerUser1 = orgSetup.createUser(SECOND_CUSTOMER_USER1_ID,
                getSecondCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
        secondCustomerUser2 = orgSetup.createUser(SECOND_CUSTOMER_USER2_ID,
                getSecondCustomerOrgID(), new ArrayList<UserRoleType>(),
                supplierMarketplace.getMarketplaceId());
    }

    public void savePSPAccount() throws ObjectNotFoundException,
            OrganizationAuthorityException, ConcurrentModificationException,
            ValidationException {
        paymentSetup.savePSPAccount(supplier, "psp1", paymentSetup.getPSP(0));
    }

    public void addAvailablePaymentTypeForSupplier() throws Exception {
        paymentSetup.addAvailablePaymentTypes(supplier,
                PaymentInfoType.INVOICE.name(),
                PaymentInfoType.CREDIT_CARD.name(),
                PaymentInfoType.DIRECT_DEBIT.name());
    }

    public void savePaymentConfigForSupplier() throws Exception {
        paymentSetup.savePaymentConfigForSeller(supplier);
    }

    /**
     * Creates a technical service with events, role definitions, parameter
     * definitions.
     * 
     * @return The created technical service.
     */
    public VOTechnicalService createTechnicalService(String tsXml)
            throws Exception {
        serviceSetup.importTechnicalServices(tsXml);
        technicalService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE_ID);
        return technicalService;
    }

    public VOTechnicalService createAsyncTechnicalService(String tsXml)
            throws Exception {
        serviceSetup.importTechnicalServices(tsXml);
        technicalServiceAsync = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE_ASYNC_ID);
        return technicalServiceAsync;
    }

    public Organization getPlatformOperatorOrg() {
        return platformOperatorOrg;
    }

    public long getPlatformOperatorUserKey() {
        return platformOperatorUserKey;
    }

    public VOUser getCustomerAdmin() {
        return customerAdmin;
    }

    public VOUser getSecondCustomerAdmin() {
        return secondCustomerAdmin;
    }

    public long getCustomerAdminKey() {
        return customerAdmin.getKey();
    }

    public long getSecondCustomerAdminKey() {
        return secondCustomerAdmin.getKey();
    }

    public VOTechnicalService getTechnicalService() {
        return technicalService;
    }

    public VOTechnicalService getTechnicalServiceAsync() {
        return technicalServiceAsync;
    }

    public VOMarketplace getSupplierMarketplace() {
        return supplierMarketplace;
    }

    public String getCustomerOrgID() {
        return customer.getOrganizationId();
    }

    public String getSecondCustomerOrgID() {
        return secondCustomer.getOrganizationId();
    }

    public VOOrganization getSupplier() {
        return supplier;
    }

    public String getSupplierOrgID() {
        return supplier.getOrganizationId();
    }

    public long getSupplierAdminKey() {
        return supplierAdmin.getKey();
    }

    public VOUser getTechnologyProviderUser() {
        return technologyProviderUser;
    }

    public VOOrganization getCustomer() {
        return customer;
    }

    public VOUserDetails getCustomerUser1() {
        return customerUser1;
    }

    public VOUserDetails getCustomerUser2() {
        return customerUser2;
    }

    public VOUserDetails getCustomerUser3() {
        return customerUser3;
    }

    public VOUserDetails getCustomerUser4() {
        return customerUser4;
    }

    public VOUserDetails getCustomerUser5() {
        return customerUser5;
    }

    public VOUserDetails getCustomerUser6() {
        return customerUser6;
    }

    public VOOrganization getSecondCustomer() {
        return secondCustomer;
    }

    public VOUserDetails getSecondCustomerUser1() {
        return secondCustomerUser1;
    }

    public VOUserDetails getSecondCustomerUser2() {
        return secondCustomerUser2;
    }

}
