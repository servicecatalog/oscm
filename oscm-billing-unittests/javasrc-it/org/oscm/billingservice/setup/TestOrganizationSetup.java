/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 15.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UserGroup;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.test.ejb.TestContainer;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.accountmgmt.AccountServiceManagement;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.VatService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.internal.vo.VOVatRate;

/**
 * Setup of test services
 * 
 * @author baumann
 */
public class TestOrganizationSetup {

    public static final String ORGANIZATION_DOMICILE_DE = "DE";
    public static final String ORGANIZATION_DOMICILE_JP = "JP";
    public static final String ORGANIZATION_DOMICILE_UK = "GB";

    private final TestContainer container;
    private final OperatorService operatorService;
    private final AccountService accountService;
    private final IdentityService identityService;
    private final IdentityServiceLocal identityServiceLocal;
    private final AccountServiceManagement accountServiceManagement;
    private final MarketplaceService marketplaceService;
    private final VatService vatService;
    private final UserGroupServiceLocalBean userGroupService;
    private final DataService dataService;

    public TestOrganizationSetup(TestContainer container) {
        this.container = container;
        operatorService = container.get(OperatorService.class);
        accountService = container.get(AccountService.class);
        accountServiceManagement = container
                .get(AccountServiceManagement.class);
        identityService = container.get(IdentityService.class);
        identityServiceLocal = container.get(IdentityServiceLocal.class);
        marketplaceService = container.get(MarketplaceService.class);
        vatService = container.get(VatService.class);
        userGroupService = container.get(UserGroupServiceLocalBean.class);
        dataService = container.get(DataService.class);
    }

    public VOOrganization createOrganization(String userId, String name,
            String domicileCountry, OrganizationRoleType... rolesToGrant)
            throws Exception {
        return createOrganization(userId, name, domicileCountry,
                BigDecimal.ZERO, rolesToGrant);
    }

    public VOOrganization createOrganization(String userId, String name,
            String domicileCountry, BigDecimal operatorShare,
            OrganizationRoleType... rolesToGrant) throws Exception {
        VOOrganization voOrganization = newVOOrganization(name, domicileCountry);
        if (Arrays.asList(rolesToGrant).contains(OrganizationRoleType.SUPPLIER)) {
            voOrganization.setOperatorRevenueShare(operatorShare);
        }

        VOUserDetails userDetails = newVOUserDetails(userId, null);
        VOOrganization createdOrg = operatorService.registerOrganization(
                voOrganization, null, userDetails, null, null, rolesToGrant);

        return createdOrg;
    }

    public VOOrganization createOrganization(long platformAdminKey,
            String userId, String name, String domicileCountry,
            OrganizationRoleType... rolesToGrant) throws Exception {
        container
                .login(platformAdminKey, UserRoleType.PLATFORM_OPERATOR.name());

        return createOrganization(userId, name, domicileCountry, rolesToGrant);
    };

    public VOOrganization createOrganization(long platformAdminKey,
            String userId, String name, String domicileCountry,
            BigDecimal operatorShare, OrganizationRoleType... rolesToGrant)
            throws Exception {
        container
                .login(platformAdminKey, UserRoleType.PLATFORM_OPERATOR.name());

        return createOrganization(userId, name, domicileCountry, operatorShare,
                rolesToGrant);
    }

    public VOUserDetails createUser(String userId, String organizationId,
            List<UserRoleType> roles, String marketplaceId) throws Exception {
        VOUserDetails user = newVOUserDetails(userId, organizationId);
        user = identityService.createUser(user, roles, marketplaceId);
        return user;
    }

    public UserGroup createUnit(final long adminKey, final String unitName,
            final String referenceID, final VOService visibleService,
            final String marketplaceId) throws Exception {

        return BillingIntegrationTestBase.runTX(new Callable<UserGroup>() {

            @Override
            public UserGroup call() throws Exception {
                UserGroup unit = new UserGroup();
                unit.setName(unitName + System.nanoTime());
                unit.setReferenceId(referenceID);

                Product visibleProduct = dataService.getReference(
                        Product.class, visibleService.getKey());
                container.login(adminKey,
                        UserRoleType.ORGANIZATION_ADMIN.name());
                unit = userGroupService.createUserGroup(unit,
                        Arrays.asList(visibleProduct), null, marketplaceId);
                return unit;

            }
        });
    }

    public VOOrganization registerCustomer(String orgName,
            String domicileCountry, String adminId, String marketplaceId,
            String sellerId) throws Exception {
        VOOrganization customerOrg = newVOOrganization(orgName, domicileCountry);
        VOUserDetails admin = newVOUserDetails(adminId, null);
        return accountService.registerCustomer(customerOrg, admin, "admin",
                null, marketplaceId, sellerId);
    }

    /**
     * Update the customer discount for the given customer organization.
     * 
     * Don't update the discount for a customer that is used by other testcases!
     * 
     * @param organization
     *            a customer organization
     * @param value
     *            discount value in percent
     * @param startTime
     *            discount start time
     * @param endTime
     *            discount end time
     * @return the updated organization
     * @throws Exception
     */
    public VOOrganization updateCustomerDiscount(VOOrganization organization,
            BigDecimal value, long startTime, long endTime) throws Exception {
        VODiscount discount = organization.getDiscount();
        if (discount == null) {
            discount = new VODiscount();
            organization.setDiscount(discount);
        }

        discount.setValue(value);
        discount.setStartTime(Long.valueOf(startTime));
        discount.setEndTime(Long.valueOf(endTime));

        return accountService.updateCustomerDiscount(organization);
    }

    public VOOrganization updateCustomerDiscount(long supplierAdminKey,
            VOOrganization organization, BigDecimal value, long startTime,
            long endTime) throws Exception {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        return updateCustomerDiscount(organization, value, startTime, endTime);
    }

    public VOUser getUser(final String userID, boolean openTransaction)
            throws Exception {
        if (openTransaction) {
            return getUserWithNewTransaction(userID);
        } else {
            return getUser(userID);
        }
    }

    private VOUser getUser(final String userID) throws Exception {
        PlatformUser platformUser = identityServiceLocal.getPlatformUser(
                userID, false);
        return UserDataAssembler.toVOUser(platformUser);
    }

    private VOUser getUserWithNewTransaction(final String userID)
            throws Exception {
        return BillingIntegrationTestBase.runTX(new Callable<VOUser>() {
            @Override
            public VOUser call() throws Exception {
                return getUser(userID);
            }
        });
    }

    private VOOrganization newVOOrganization(String name, String domicileCountry) {
        VOOrganization voOrganization = new VOOrganization();
        voOrganization.setName(name);
        voOrganization.setDomicileCountry(domicileCountry);

        voOrganization.setEmail("info@est.fujitsu.com");
        voOrganization.setAddress("address");
        voOrganization.setLocale("en");
        voOrganization.setPhone("+49 89 000000");
        voOrganization.setUrl("http://de.fujitsu.com");
        voOrganization.setSupportEmail("info@est.fujitsu.com");

        if (domicileCountry != null && domicileCountry.trim().length() > 0) {
            voOrganization.setDomicileCountry(domicileCountry);
        }

        return voOrganization;
    }

    private VOUserDetails newVOUserDetails(String userId, String organizationId) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId(userId);
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail("test@est.fujitsu.de");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone("(089) 123 456 78");
        userDetails.setLocale("de");
        userDetails.setOrganizationId(organizationId);
        return userDetails;
    }

    /**
     * Update the cutoff day for the current organization
     * 
     * @param cutOffDay
     * @return the cutoff day that was set before the update
     * @throws Exception
     */
    public int updateCutOffDay(final int cutOffDay) throws Exception {
        VOOrganization organizationData = accountService.getOrganizationData();

        int oldCutoffDay = accountServiceManagement
                .getCutOffDayOfOrganization();
        accountServiceManagement.setCutOffDayOfOrganization(cutOffDay,
                organizationData);
        return oldCutoffDay;
    }

    public VOMarketplace createMarketplace(String name, boolean open,
            VOOrganization ownerOrganization) throws Exception {

        VOMarketplace marketplace = newVOMarketplace(name, open,
                ownerOrganization);
        return marketplaceService.createMarketplace(marketplace);
    }

    public VOMarketplace createMarketplace(long platformAdminKey, String name,
            boolean open, VOOrganization ownerOrganization) throws Exception {

        container
                .login(platformAdminKey, UserRoleType.PLATFORM_OPERATOR.name());

        return createMarketplace(name, open, ownerOrganization);
    };

    private VOMarketplace newVOMarketplace(String name, boolean open,
            VOOrganization ownerOrganization) {
        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setName(name);
        marketplace.setOpen(open);
        marketplace.setOwningOrganizationId(ownerOrganization
                .getOrganizationId());
        return marketplace;
    }

    public void createMarketingPermission(long techProviderKey,
            String supplierOrgId, VOTechnicalService technicalService)
            throws Exception {
        container
                .login(techProviderKey, UserRoleType.TECHNOLOGY_MANAGER.name());
        accountService.addSuppliersForTechnicalService(technicalService,
                Arrays.asList(new String[] { supplierOrgId }));
    }

    public void saveAllVats(long supplierAdminKey, VOVatRate defaultVat,
            List<VOCountryVatRate> countryVats,
            List<VOOrganizationVatRate> organizationVats) throws Exception {
        container.login(supplierAdminKey, UserRoleType.SERVICE_MANAGER.name());
        vatService.saveAllVats(defaultVat, countryVats, organizationVats);
    }

    public VOBillingContact saveBillingContact(long customerAdminKey,
            VOBillingContact billingContact) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return accountService.saveBillingContact(billingContact);
    }

    public VOBillingContact saveNewBillingContact(long customerAdminKey)
            throws Exception {
        VOBillingContact billingContact = newVOBillingContact();
        return saveBillingContact(customerAdminKey, billingContact);
    }

    private VOBillingContact newVOBillingContact() {
        VOBillingContact voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("str 123");
        voBillingContact.setCompanyName("fujitsu");
        voBillingContact.setEmail("test@mail.de");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setId("billingId" + TestBasicSetup.createUniqueKey());
        return voBillingContact;
    }

    public void deleteBillingContacts(long customerAdminKey) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        List<VOBillingContact> billingContacts = accountService
                .getBillingContacts();
        for (VOBillingContact billingContact : billingContacts) {
            accountService.deleteBillingContact(billingContact);
        }
    }

}
