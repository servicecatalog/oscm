/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.domobjects.UserGroup;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUser;

/**
 * @author malhotra
 * 
 */
public abstract class IntegrationTestSetup {

    public final static String SUPPLIER_NAME_PREFIX = "supplier";
    public final static String BROKER_NAME_PREFIX = "broker";
    public final static String RESELLER_NAME_PREFIX = "reseller";
    public final static String ROLE_ORGANIZATION_ADMIN = UserRoleType.ORGANIZATION_ADMIN
            .name();
    public final static String ROLE_PLATFORM_OPERATOR = UserRoleType.PLATFORM_OPERATOR
            .name();
    public final static String ROLE_TECHNOLOGY_MANAGER = UserRoleType.TECHNOLOGY_MANAGER
            .name();
    public final static String ROLE_SERVICE_MANAGER = UserRoleType.SERVICE_MANAGER
            .name();
    public final static String ROLE_MARKETPLACE_OWNER = UserRoleType.MARKETPLACE_OWNER
            .name();

    protected final TestOrganizationSetup orgSetup;
    protected final TestServiceSetup serviceSetup;
    protected final TestSubscriptionSetup subscrSetup;
    protected final TestPaymentSetup paymentSetup;
    protected final TestContainer container;
    protected final TestSharesSetup sharesSetup;

    private int oldCutOffDay;

    protected final TestBasicSetup basicSetup;
    protected final VOTechnicalService technicalService;
    protected final VOTechnicalService technicalServiceAsync;
    protected final VOMarketplace supplierMarketplace;

    public IntegrationTestSetup() {
        container = BillingIntegrationTestBase.getContainer();

        orgSetup = new TestOrganizationSetup(container);
        serviceSetup = new TestServiceSetup(container);
        subscrSetup = new TestSubscriptionSetup(container);
        paymentSetup = new TestPaymentSetup(container);
        sharesSetup = new TestSharesSetup(container);

        basicSetup = BillingIntegrationTestBase.getBasicSetup();
        technicalService = basicSetup.getTechnicalService();
        technicalServiceAsync = basicSetup.getTechnicalServiceAsync();
        supplierMarketplace = basicSetup.getSupplierMarketplace();
    }

    protected void setDateFactory(long date) {
        BillingIntegrationTestBase.setDateFactoryInstance(date);
    }

    protected void setDateFactory(String date) {
        BillingIntegrationTestBase.setDateFactoryInstance(date);
    }

    protected void cacheTestData(String testName, TestData testData) {
        BillingIntegrationTestBase.addToTestDataCache(testName, testData);
    }

    /**
     * Login a user with roles. The DateFactoryInstance is set to the provided
     * login time.
     */
    public void login(String loginTime, long userkey, String... roles) {
        BillingIntegrationTestBase.setDateFactoryInstance(loginTime);
        container.login(userkey, roles);
    }

    /**
     * Sets the cutoffDay for the given organization and store the old
     * cutoffDay. Later on the old cutoff day can be restored via
     * resetCutOffDay.
     * 
     * @param adminKey
     *            key of a supplier or reseller admin
     * @param userRole
     *            UserRoleType.SERVICE_MANAGER or UserRoleType.RESELLER_MANAGER
     * @param cutoffDay
     *            cutoffDay to set
     */
    protected void setCutOffDay(long adminKey, UserRoleType userRole,
            int cutOffDay) throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN, userRole.name());
        oldCutOffDay = orgSetup.updateCutOffDay(cutOffDay);
    }

    /**
     * Sets the cutoffDay for the given supplier organization and store the old
     * cutoffDay. Later on the old cutoff day can be restored via
     * resetCutOffDay.
     * 
     * @param supplAdminKey
     *            key of a supplier admin
     * @param cutoffDay
     *            cutoffDay to set
     */
    protected void setCutOffDay(long supplAdminKey, int cutOffDay)
            throws Exception {
        setCutOffDay(supplAdminKey, UserRoleType.SERVICE_MANAGER, cutOffDay);
    }

    /**
     * Reset the cutoffDay for the given organization
     * 
     * @param adminKey
     *            key of a supplier or reseller admin
     * @param userRole
     *            UserRoleType.SERVICE_MANAGER or UserRoleType.RESELLER_MANAGER
     */
    protected void resetCutOffDay(long adminKey, UserRoleType userRole)
            throws Exception {
        container.login(adminKey, ROLE_ORGANIZATION_ADMIN, userRole.name());
        orgSetup.updateCutOffDay(oldCutOffDay);
    }

    /**
     * Reset the cutoffDay for the given supplier organization
     * 
     * @param supplAdminKey
     *            key of a supplier admin
     */
    protected void resetCutOffDay(long supplAdminKey) throws Exception {
        resetCutOffDay(supplAdminKey, UserRoleType.SERVICE_MANAGER);
    }

    protected VendorData setupNewSupplier(String date) throws Exception {
        return setupNewSupplier(date, BigDecimal.ZERO);
    }

    protected VendorData setupNewSupplier(String date, BigDecimal operatorShare)
            throws Exception {
        return setupNewVendor(SUPPLIER_NAME_PREFIX + "_" + System.nanoTime(),
                date, operatorShare, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
    }

    protected VendorData setupNewBroker(String date) throws Exception {
        return setupNewVendor(BROKER_NAME_PREFIX + "_" + System.nanoTime(),
                date, null, OrganizationRoleType.BROKER);
    }

    protected VendorData setupNewReseller(String date) throws Exception {
        return setupNewVendor(RESELLER_NAME_PREFIX + "_" + System.nanoTime(),
                date, null, OrganizationRoleType.RESELLER);
    }

    /**
     * Creates a vendor organization with a marketplace. If the organization is
     * a supplier or a reseller, payment info is also created.
     */
    private VendorData setupNewVendor(String orgName, String date,
            BigDecimal operatorShare, OrganizationRoleType... orgRoles)
            throws Exception {
        setDateFactory(date);

        VendorData vendorData = new VendorData();
        String adminUserId = orgName;

        VOOrganization vendor = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), adminUserId, orgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE, operatorShare,
                orgRoles);
        VOUser adminUser = orgSetup.getUser(adminUserId, true);
        VOMarketplace marketplace = orgSetup.createMarketplace(orgName, true,
                vendor);

        List<OrganizationRoleType> orgRoleList = Arrays.asList(orgRoles);
        if (orgRoleList.contains(OrganizationRoleType.SUPPLIER)) {
            paymentSetup.createPaymentForSupplier(
                    basicSetup.getPlatformOperatorUserKey(),
                    adminUser.getKey(), vendor);
        } else if (orgRoleList.contains(OrganizationRoleType.RESELLER)) {
            paymentSetup.createPaymentForReseller(
                    basicSetup.getPlatformOperatorUserKey(),
                    adminUser.getKey(), vendor);
        }

        vendorData.setOrganization(vendor);
        vendorData.setOrgRoles(orgRoles);
        vendorData.setAdminKey(adminUser.getKey());
        vendorData.addMarketplace(marketplace);
        return vendorData;
    }

    protected CustomerData registerCustomer(VendorData vendorData,
            String orgName, String adminId) throws Exception {
        VOMarketplace marketplace = vendorData.getMarketplace(0);
        VOOrganization customer = orgSetup.registerCustomer(orgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE, adminId,
                marketplace.getMarketplaceId(), vendorData.getOrganizationId());

        CustomerData customerData = new CustomerData();
        customerData.setOrganization(customer);
        customerData.setAdminUser(orgSetup.getUser(adminId, true));
        vendorData.addCustomers(customerData);

        return customerData;
    }

    protected CustomerData registerCustomer(VendorData vendorData)
            throws Exception {
        long time = System.nanoTime();
        return registerCustomer(vendorData, "customer_" + time, "admin" + time);
    }

    protected CustomerData registerCustomer(VendorData vendorData,
            String orgName) throws Exception {
        long time = System.nanoTime();
        return registerCustomer(vendorData, orgName + "_" + time, orgName
                + "Admin_" + time);
    }

    protected CustomerData registerCustomerWithDiscount(
            VendorData supplierData, BigDecimal discount, long startTime,
            long endTime) throws Exception {

        CustomerData customer = registerCustomer(supplierData);
        updateCustomerDiscount(supplierData.getAdminKey(), customer, discount,
                startTime, endTime);
        return customer;
    }

    protected CustomerData registerCustomerWithDiscount(
            VendorData supplierData, String orgName, BigDecimal discount,
            long startTime, long endTime) throws Exception {

        CustomerData customer = registerCustomer(supplierData, orgName);
        updateCustomerDiscount(supplierData.getAdminKey(), customer, discount,
                startTime, endTime);
        return customer;
    }

    protected void updateCustomerDiscount(long supplierAdminKey,
            CustomerData customer, BigDecimal discount, long startTime,
            long endTime) throws Exception {
        VOOrganization updatedCustomer = orgSetup.updateCustomerDiscount(
                supplierAdminKey, customer.getOrganization(), discount,
                startTime, endTime);
        customer.setOrganization(updatedCustomer);
    }

    protected VOSubscriptionDetails addSecondCustomerUser(
            VOSubscriptionDetails subscr, VOServiceDetails service, String date)
            throws Exception {
        setDateFactory(date);
        subscr = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                VOServiceFactory.getRole(service, "USER"),
                subscr.getSubscriptionId());
        BillingIntegrationTestBase.addToCache(subscr);
        return subscr;
    }

    protected VOServiceDetails createPublishActivateService(
            VendorData supplierData, TestPriceModel pm, String serviceId)
            throws Exception {
        return createPublishActivateService(supplierData, pm, serviceId,
                supplierData.getMarketplace(0));
    }

    protected VOServiceDetails createPublishService(VendorData supplierData,
            TestPriceModel pm, String serviceId) throws Exception {
        return createPublishService(supplierData, pm, serviceId,
                supplierData.getMarketplace(0));
    }

    protected VOServiceDetails createPublishActivateAsyncService(
            VendorData supplierData, TestPriceModel pm, String serviceId)
            throws Exception {
        return createPublishActivateAsyncService(supplierData, pm, serviceId,
                supplierData.getMarketplace(0));
    }

    protected VOServiceDetails createPublishActivateService(
            VendorData supplierData, TestPriceModel pm, String serviceId,
            VOMarketplace marketplace) throws Exception {

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplierData.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierData.getAdminKey(), serviceId,
                        TestService.EXAMPLE, pm, technicalService, marketplace);
        supplierData.addService(serviceDetails);
        supplierData.setMarketplaceForService(serviceDetails.getServiceId(),
                marketplace.getMarketplaceId());
        return serviceDetails;
    }

    protected VOServiceDetails createPublishService(VendorData supplierData,
            TestPriceModel pm, String serviceId, VOMarketplace marketplace)
            throws Exception {

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplierData.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createAndPublishMarketableService(supplierData.getAdminKey(),
                        serviceId, TestService.EXAMPLE, pm, technicalService,
                        marketplace);
        supplierData.addService(serviceDetails);
        supplierData.setMarketplaceForService(serviceDetails.getServiceId(),
                marketplace.getMarketplaceId());
        return serviceDetails;
    }

    protected VOServiceDetails createPublishActivateAsyncService(
            VendorData supplierData, TestPriceModel pm, String serviceId,
            VOMarketplace marketplace) throws Exception {

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplierData.getOrganizationId(), technicalServiceAsync);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierData.getAdminKey(), serviceId,
                        TestService.EXAMPLE_ASYNC, pm, technicalServiceAsync,
                        marketplace);
        supplierData.addService(serviceDetails);
        supplierData.setMarketplaceForService(serviceDetails.getServiceId(),
                marketplace.getMarketplaceId());
        return serviceDetails;
    }

    protected VOServiceDetails createPublishAndActivateAsyncService(
            long supplierAdminKey, TestPriceModel pm, String serviceId)
            throws Exception {
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(supplierAdminKey,
                        serviceId, TestService.EXAMPLE_ASYNC, pm,
                        technicalServiceAsync, supplierMarketplace);
        return serviceDetails;
    }

    protected VOServiceDetails publishActivateService(VendorData vendorData,
            VOService service) throws Exception {
        VOMarketplace marketplace = vendorData.getMarketplace(0);
        VOServiceDetails serviceDetails = serviceSetup
                .publishAndActivateMarketableService(vendorData, marketplace,
                        service);
        vendorData.addService(serviceDetails);
        vendorData.setMarketplaceForService(serviceDetails.getServiceId(),
                marketplace.getMarketplaceId());
        return serviceDetails;
    }

    protected VOServiceDetails updateService(VendorData supplierData,
            VOServiceDetails serviceDetails, String date) throws Exception {

        setDateFactory(date);
        VOServiceDetails newServiceDetails = serviceSetup
                .updateMarketableService(supplierData.getAdminKey(),
                        serviceDetails);
        supplierData.addService(newServiceDetails);
        return newServiceDetails;
    }

    protected VOServiceDetails updateServiceId(VendorData supplierData,
            VOService service, String newId, String date) throws Exception {
        VOServiceDetails serviceDetails = serviceSetup.getServiceDetails(
                supplierData.getAdminKey(), service);
        serviceDetails.setServiceId(newId);
        return updateService(supplierData, serviceDetails, date);
    }

    protected VOServiceDetails deactivateService(VendorData supplierData,
            VOService service, String date) throws Exception {
        setDateFactory(date);
        return deactivateService(supplierData, service);
    }

    protected VOServiceDetails deactivateService(VendorData supplierData,
            VOService service) throws Exception {
        return serviceSetup.deactivateMarketableService(
                supplierData.getAdminKey(), service);
    }

    protected VOServiceDetails grantResalePermission(VendorData supplierData,
            VOService supplierService, VendorData partnerData) throws Exception {
        serviceSetup.updateAndPublishService(supplierData.getAdminKey(),
                supplierService,
                Arrays.asList(new VendorData[] { partnerData }),
                new ArrayList<VendorData>());
        return serviceSetup.getResaleCopy(supplierService, partnerData);
    }

    protected VOSubscriptionDetails subscribe(VOUser user,
            String subscriptionId, long unitKey, VOServiceDetails service,
            String userRole) throws Exception {
        VOSubscriptionDetails subscr = subscrSetup.subscribeToService(
                user.getKey(), subscriptionId, unitKey, service, user,
                VOServiceFactory.getRole(service, userRole));

        BillingIntegrationTestBase.addToCache(subscr);
        return subscr;
    }

    protected VOSubscriptionDetails subscribe(VOUser user,
            String subscriptionId, String purchaseOrderNumber,
            VOServiceDetails service, String userRole) throws Exception {
        VOSubscriptionDetails subscr = subscrSetup.subscribeToService(
                user.getKey(), subscriptionId, purchaseOrderNumber, service,
                user, VOServiceFactory.getRole(service, userRole));

        BillingIntegrationTestBase.addToCache(subscr);
        return subscr;
    }

    protected VOSubscriptionDetails subscribe(VOUser user,
            String subscriptionId, VOServiceDetails service, String date,
            String userRole) throws Exception {
        setDateFactory(date);
        return subscribe(user, subscriptionId, 0L, service, userRole);
    }

    protected VOSubscriptionDetails subscribeWithAssignToUnit(VOUser user,
            String subscriptionId, long unitKey, VOServiceDetails service,
            long date, String userRole) throws Exception {
        setDateFactory(date);
        return subscribe(user, subscriptionId, unitKey, service, userRole);
    }

    protected VOSubscriptionDetails subscribe(VOUser user,
            String subscriptionId, VOServiceDetails service, long date,
            String userRole) throws Exception {
        setDateFactory(date);
        return subscribe(user, subscriptionId, 0L, service, userRole);
    }

    protected VOSubscriptionDetails subscribe(VOUser user,
            String subscriptionId, String purchaseOrderNumber,
            VOServiceDetails service, long date, String userRole)
            throws Exception {
        setDateFactory(date);
        return subscribe(user, subscriptionId, purchaseOrderNumber, service,
                userRole);
    }

    protected void unsubscribe(long customerAdminKey, String subscriptionId,
            String date) throws Exception {
        setDateFactory(date);
        subscrSetup.unsubscribeToService(customerAdminKey, subscriptionId);
    }

    protected void unsubscribe(VOUser user, String subscriptionId, String date)
            throws Exception {
        setDateFactory(date);
        subscrSetup.unsubscribeToService(user.getKey(), subscriptionId);
    }

    protected void unsubscribe(long customerAdminKey, String subscriptionId,
            long date) throws Exception {
        setDateFactory(date);
        subscrSetup.unsubscribeToService(customerAdminKey, subscriptionId);
    }

    protected VOSubscriptionDetails modifySubscription(VOUser user,
            VOSubscription voSubscription, List<VOParameter> parameters,
            long date) throws Exception {
        setDateFactory(date);
        VOSubscriptionDetails modifiedSubscr = subscrSetup.modifySubscription(
                user.getKey(), voSubscription, parameters);
        BillingIntegrationTestBase.addToCache(modifiedSubscr);
        return modifiedSubscr;
    }

    protected VOServiceDetails createAndRegisterCompatibleService(
            VendorData supplierData, TestPriceModel pm,
            VOServiceDetails service, String serviceId) throws Exception {

        VOServiceDetails upgrService = createPublishActivateService(
                supplierData, pm, serviceId);
        serviceSetup.registerCompatibleServices(supplierData.getAdminKey(),
                service, upgrService);
        return upgrService;
    }

    protected VOSubscriptionDetails upgrade(VOUser user,
            VOSubscriptionDetails subscr, VOServiceDetails upgrService)
            throws Exception {

        VOSubscriptionDetails subscr2 = subscrSetup.upgradeSubscription(
                user.getKey(), subscr, upgrService);
        BillingIntegrationTestBase.addToCache(subscr2);
        return subscr2;
    }

    protected VOSubscriptionDetails upgrade(VOUser user,
            VOSubscriptionDetails subscr, VOServiceDetails upgrService,
            String date) throws Exception {

        setDateFactory(date);
        return upgrade(user, subscr, upgrService);
    }

    protected VOSubscriptionDetails upgrade(VOUser user,
            VOSubscriptionDetails subscr, VOServiceDetails upgrService,
            long date) throws Exception {

        setDateFactory(date);
        return upgrade(user, subscr, upgrService);
    }

    protected VOSubscriptionDetails completeAsyncSubscription(
            long supplierAdminKey, VOUser user, VOSubscriptionDetails subscr,
            String date) throws Exception {

        setDateFactory(date);
        VOSubscriptionDetails completedSub = subscrSetup
                .completeAsyncSubscription(supplierAdminKey, user, subscr);

        BillingIntegrationTestBase.addToCache(completedSub);
        return completedSub;
    }

    protected VOSubscriptionDetails completeAsyncUpgradeSubscription(
            long supplierAdminKey, VOUser user, VOSubscriptionDetails subscr,
            String date) throws Exception {

        setDateFactory(date);
        VOSubscriptionDetails completedUpgradedSub = subscrSetup
                .completeAsyncUpgradeSubscription(supplierAdminKey, user,
                        subscr);

        BillingIntegrationTestBase.addToCache(completedUpgradedSub);
        return completedUpgradedSub;
    }

    protected VOMarketplace createMarketplace(VendorData supplierData)
            throws Exception {

        VOMarketplace mp = orgSetup.createMarketplace(
                basicSetup.getPlatformOperatorUserKey(),
                supplierData.getOrganizationName(), true,
                supplierData.getOrganization());
        supplierData.addMarketplace(mp);
        return mp;
    }

    protected VOSubscriptionDetails deleteBillingContactsAndUpdateSub(
            long customerAdminKey, VOSubscription subscription)
            throws Exception {
        orgSetup.deleteBillingContacts(customerAdminKey);
        return subscrSetup.getSubscriptionDetails(customerAdminKey,
                subscription.getSubscriptionId());
    }

    protected VOSubscriptionDetails restoreBillingContactForSubscription(
            long customerAdminKey, VOSubscription subscription)
            throws Exception {
        VOBillingContact billingContact = orgSetup
                .saveNewBillingContact(customerAdminKey);
        return subscrSetup.modifySubscriptionPaymentData(customerAdminKey,
                subscription, billingContact);
    }

    protected UserGroup createUnitAndAssignVisibleService(
            CustomerData customerData, String unitName, String referenceID,
            VOServiceDetails service, VOMarketplace marketplace)
            throws Exception {
        UserGroup unit = orgSetup.createUnit(customerData.getAdminKey(),
                unitName, referenceID, service, marketplace.getMarketplaceId());
        customerData.addUserGroup(unit);
        return unit;
    }

    protected VOSubscriptionDetails changeSubscriptionUnit(long adminKey,
            VOSubscription voSubscription, long unitKey, String date)
            throws Exception {
        setDateFactory(date);
        container.login(adminKey, UserRoleType.ORGANIZATION_ADMIN.name());

        VOSubscription clonedSub = (VOSubscription) ReflectiveClone
                .clone(voSubscription);
        clonedSub.setUnitKey(unitKey);
        if (unitKey == 0) {
            clonedSub.setUnitName(null);
        }

        VOSubscriptionDetails newSubscriptionDetails = subscrSetup
                .modifySubscription(clonedSub,
                        Collections.<VOParameter> emptyList());

        BillingIntegrationTestBase.addToCache(newSubscriptionDetails);
        return newSubscriptionDetails;
    }
}
