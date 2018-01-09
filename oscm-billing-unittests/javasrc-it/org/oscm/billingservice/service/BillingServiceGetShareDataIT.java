/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Calendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.business.model.brokershare.BrokerRevenueShareResult;
import org.oscm.billingservice.business.model.mpownershare.MarketplaceOwnerRevenueShareResult;
import org.oscm.billingservice.business.model.resellershare.ResellerRevenueShareResult;
import org.oscm.billingservice.business.model.suppliershare.SupplierRevenueShareResult;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.communicationservice.bean.CommunicationServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.validation.Invariants;

@SuppressWarnings("boxing")
public class BillingServiceGetShareDataIT extends EJBTestBase {
    final static long PERIOD_START_MONTH1 = getDate(2011, 0, 1);
    final static long PERIOD_END_MONTH1 = getDate(2011, 0, 31);
    final static long PERIOD_START_MONTH2 = getDate(2011, 1, 1);
    final static long PERIOD_END_MONTH2 = getDate(2011, 1, 28);

    protected DataService dm;

    protected PlatformUser brokerUser;
    protected PlatformUser resellerUser;
    protected PlatformUser supplierUser;
    protected PlatformUser platformOperatorUser;

    protected Organization brokerOrg;
    protected Organization resellerOrg;
    protected Organization supplierOrg;
    protected Organization platformOperatorOrg;

    private BillingService bs;

    @Override
    public void setup(TestContainer container) throws Exception {
        container.login("1");

        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new SharesDataRetrievalServiceBean());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(new SharesCalculatorBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new CommunicationServiceBean());
        container.addBean(new UserLicenseDao());
        container.addBean(new BillingServiceBean());

        dm = container.get(DataService.class);
        bs = container.get(BillingService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(dm);
                createPaymentTypes(dm);
                createSupportedCurrencies(dm);
                SupportedCountries.createSomeSupportedCountries(dm);
                dm.flush();
                platformOperatorOrg = Organizations.createPlatformOperator(dm);
                platformOperatorUser = Organizations.createUserForOrg(dm,
                        platformOperatorOrg, true, "platformOperatorUser");

                brokerOrg = Organizations.createOrganization(dm,
                        OrganizationRoleType.BROKER);
                brokerOrg.setName("brokerOrg");
                brokerUser = Organizations.createUserForOrg(dm, brokerOrg, true,
                        "brokerUser");
                PlatformUsers.grantRoles(dm, brokerUser,
                        UserRoleType.BROKER_MANAGER);

                resellerOrg = Organizations.createOrganization(dm,
                        OrganizationRoleType.RESELLER);
                resellerOrg.setName("resellerOrg");
                resellerUser = Organizations.createUserForOrg(dm, resellerOrg,
                        true, "resellerUser");
                PlatformUsers.grantRoles(dm, resellerUser,
                        UserRoleType.RESELLER_MANAGER);

                supplierOrg = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                supplierOrg.setName("supplierOrg");
                supplierUser = Organizations.createUserForOrg(dm, supplierOrg,
                        true, "supplierUser");
                PlatformUsers.grantRoles(dm, supplierUser,
                        UserRoleType.SERVICE_MANAGER,
                        UserRoleType.MARKETPLACE_OWNER);

                Marketplaces.createGlobalMarketplace(supplierOrg,
                        "supplierMarketplaceId", dm);

                return null;
            }
        });
    }

    private static final BillingSharesResult createBillingData(DataService mgr,
            long orgKey, long periodStartTime, long periodEndTime, String xml,
            BillingSharesResultType resultType)
            throws NonUniqueBusinessKeyException {
        BillingSharesResult br = new BillingSharesResult();
        br.setCreationTime(System.currentTimeMillis());
        br.setOrganizationTKey(orgKey);
        br.setPeriodStartTime(periodStartTime);
        br.setPeriodEndTime(periodEndTime);
        br.setResultXML(xml);
        br.setResultType(resultType);
        mgr.persist(br);
        mgr.flush();
        return br;
    }

    private static final long getDate(int year, int month, int date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(year, month, date, 0, 0, 0);
        return cal.getTimeInMillis();
    }

    private static final String expectedXml(long start, long end, long orgKey) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<XxxxRevenueShareResult>");
        buffer.append("<Period endDate=\"");
        buffer.append(start);
        buffer.append("\" startDate=\"");
        buffer.append(end);
        buffer.append("\" />");
        if (orgKey != 0) {
            buffer.append("<Test>");
            buffer.append("<Test id=\"");
            buffer.append(orgKey);
            buffer.append("\" />");
            buffer.append("</Test>");
        }
        buffer.append("</XxxxRevenueShareResult>");
        return buffer.toString();
    }

    private static void verifySchema(String xml,
            BillingSharesResultType resultType) {
        switch (resultType) {
        case BROKER:
            assertTrue(xml.contains(BrokerRevenueShareResult.SCHEMA));
            break;
        case RESELLER:
            assertTrue(xml.contains(ResellerRevenueShareResult.SCHEMA));
            break;
        case MARKETPLACE_OWNER:
            assertTrue(xml.contains(MarketplaceOwnerRevenueShareResult.SCHEMA));
            break;
        case SUPPLIER:
            assertTrue(xml.contains(SupplierRevenueShareResult.SCHEMA));
            break;
        default:
            Invariants.assertTrue(false,
                    "ERROR: unkown value of BillingSharesResultType");
            break;
        }
    }

    private void createRevenuShareDbEntriesForTwoMonth(
            final BillingSharesResultType type, final long orgKey)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createBillingData(dm, orgKey, PERIOD_START_MONTH1,
                        PERIOD_END_MONTH1, expectedXml(PERIOD_START_MONTH1,
                                PERIOD_END_MONTH1, orgKey),
                        type);

                createBillingData(dm, orgKey, PERIOD_START_MONTH2,
                        PERIOD_END_MONTH2, expectedXml(PERIOD_START_MONTH2,
                                PERIOD_END_MONTH2, orgKey),
                        type);
                return null;
            }
        });
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void getRevenueShareData_NoFromDate() throws Exception {
        // given
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        try {
            bs.getRevenueShareData(null, Long.valueOf(PERIOD_END_MONTH1),
                    BillingSharesResultType.BROKER);
        } catch (EJBException ex) {
            // then
            throw ex.getCausedByException();
        }
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void getRevenueShareData_NoToDate() throws Exception {
        // given
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        try {
            bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1), null,
                    BillingSharesResultType.BROKER);
        } catch (EJBException ex) {
            // then
            throw ex.getCausedByException();
        }
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void getRevenueShareData_NoType() throws Exception {
        // given
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        try {
            bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                    Long.valueOf(PERIOD_END_MONTH1), null);
            fail();
        } catch (EJBException ex) {
            // then
            throw ex.getCausedByException();
        }
    }

    @Test(expected = ValidationException.class)
    public void getRevenueShareData_InvalidDateRange() throws Exception {
        // given
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_END_MONTH1),
                Long.valueOf(PERIOD_START_MONTH1),
                BillingSharesResultType.BROKER);

        // then exception
    }

    @Test(expected = EJBAccessException.class)
    public void getRevenueShareData_invalidRoleTechnologyManager()
            throws Exception {
        // given no revenue share result entries. Login with invalid user role
        container.login(brokerUser.getKey(), ROLE_TECHNOLOGY_MANAGER);

        // when
        try {
            bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                    Long.valueOf(PERIOD_END_MONTH1),
                    BillingSharesResultType.BROKER);
            fail();
        } catch (EJBException e) {
            throw (EJBAccessException) e.getCause();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void getRevenueShareData_InvalidRoleOrganizationAdmin()
            throws Exception {
        // given no revenue share result entries. Login with invalid user role
        container.login(brokerUser.getKey(), ROLE_ORGANIZATION_ADMIN);

        // when
        try {
            bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                    Long.valueOf(PERIOD_END_MONTH1),
                    BillingSharesResultType.BROKER);
            fail();
        } catch (EJBException e) {
            throw (EJBAccessException) e.getCause();
        }
    }

    @Test
    public void getRevenueShareData_TwoMonth() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.BROKER;
        createRevenuShareDbEntriesForTwoMonth(type, brokerOrg.getKey());
        container.login(brokerUser.getKey(), ROLE_BROKER_MANAGER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH2), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        System.out.println(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, brokerOrg.getKey())));
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH2,
                PERIOD_END_MONTH2, brokerOrg.getKey())));
    }

    @Test
    public void getRevenueShareData_RoleBroker() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.BROKER;
        createRevenuShareDbEntriesForTwoMonth(type, brokerOrg.getKey());
        container.login(brokerUser.getKey(), ROLE_BROKER_MANAGER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, brokerOrg.getKey())));
        verifySchema(result, type);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getRevenueShareData_RoleBrokerMissing() throws Exception {
        // given user with missing role BROKER_MANAGER
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(dm, brokerOrg, true,
                        "brokerUser" + System.currentTimeMillis());
            }
        });
        container.login(user.getKey(), ROLE_BROKER_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH2),
                BillingSharesResultType.BROKER);

        // then exception
    }

    @Test
    public void getRevenueShareData_RoleMarketplaceOwner() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.MARKETPLACE_OWNER;
        createRevenuShareDbEntriesForTwoMonth(type, supplierOrg.getKey());
        container.login(supplierUser.getKey(), ROLE_MARKETPLACE_OWNER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, supplierOrg.getKey())));
        verifySchema(result, type);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getRevenueShareData_RoleMpOwnerMissing() throws Exception {
        // given user with missing role MARKETPLACE_OWNER
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(dm, supplierOrg, true,
                        "mpOwnerUser" + System.currentTimeMillis());
            }
        });
        container.login(user.getKey(), ROLE_MARKETPLACE_OWNER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH2),
                BillingSharesResultType.MARKETPLACE_OWNER);

        // then exception
    }

    @Test
    public void getRevenueShareData_RoleReseller() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.RESELLER;
        createRevenuShareDbEntriesForTwoMonth(type, resellerOrg.getKey());
        container.login(resellerUser.getKey(), ROLE_RESELLER_MANAGER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, resellerOrg.getKey())));
        verifySchema(result, type);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getRevenueShareData_RoleResellerMissing() throws Exception {
        // given user with missing role RESELLER_MANAGER
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(dm, resellerOrg, true,
                        "resellerUser" + System.currentTimeMillis());
            }
        });
        container.login(user.getKey(), ROLE_RESELLER_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH2),
                BillingSharesResultType.RESELLER);

        // then exception
    }

    @Test
    public void getRevenueShareData_RoleSupplier() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.SUPPLIER;
        createRevenuShareDbEntriesForTwoMonth(type, supplierOrg.getKey());
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, supplierOrg.getKey())));
        verifySchema(result, type);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getRevenueShareData_RoleSupplierMissing() throws Exception {
        // given user role SERVICE_MANAGER is missing
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(dm, supplierOrg, true,
                        "supplierUser" + System.currentTimeMillis());
            }
        });
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH2),
                BillingSharesResultType.SUPPLIER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_MpoAndTypeBroker() throws Exception {
        // given organization with CUSTOMER, MARKETPLACE_OWNER, SUPPLIER role
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.BROKER);

        // then exception, supplier has no organization role Broker
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_MpoAndTypeReseller() throws Exception {
        // given organization with CUSTOMER, MARKETPLACE_OWNER, SUPPLIER role
        container.login(supplierUser.getKey(), ROLE_RESELLER_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.RESELLER);

        // then exception, supplier has no organization role Reseller
    }

    @Test
    public void getRevenueShareData_MpoAndTypeSupplier() throws Exception {
        // given organization with CUSTOMER, MARKETPLACE_OWNER, SUPPLIER role
        container.login(supplierUser.getKey(), ROLE_MARKETPLACE_OWNER);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.SUPPLIER);

        // then
        assertEquals(0, data.length);
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_BrokerAndTypeReseller() throws Exception {
        // given organization with CUSTOMER, BROKER role
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.RESELLER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_BrokerAndTypeSupplier() throws Exception {
        // given organization with CUSTOMER, BROKER role
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.SUPPLIER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_BrokerAndTypeMpo() throws Exception {
        // given organization with CUSTOMER, BROKER role
        container.login(brokerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.MARKETPLACE_OWNER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_ResellerAndTypeBroker() throws Exception {
        // given organization with CUSTOMER, RESELLER role
        container.login(resellerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.BROKER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_ResellerAndTypeSupplier() throws Exception {
        // given organization with CUSTOMER, RESELLER role
        container.login(resellerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.SUPPLIER);

        // then exception
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getRevenueShareData_ResellerAndTypeMpo() throws Exception {
        // given organization with CUSTOMER, RESELLER role
        container.login(resellerUser.getKey(), ROLE_SERVICE_MANAGER);

        // when
        bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1),
                BillingSharesResultType.MARKETPLACE_OWNER);

        // then exception, reseller has no organization role MarketplaceOwner
    }

    @Test
    public void getRevenueShareData_PoAndTypeBroker() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.BROKER;
        createRevenuShareDbEntriesForTwoMonth(type,
                platformOperatorOrg.getKey());
        container.login(platformOperatorUser.getKey(), ROLE_PLATFORM_OPERATOR);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, platformOperatorOrg.getKey())));
        verifySchema(result, type);
    }

    @Test
    public void getRevenueShareData_PoAndTypeReseller() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.RESELLER;
        createRevenuShareDbEntriesForTwoMonth(type,
                platformOperatorOrg.getKey());
        container.login(platformOperatorUser.getKey(), ROLE_PLATFORM_OPERATOR);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, platformOperatorOrg.getKey())));
        verifySchema(result, type);
    }

    @Test
    public void getRevenueShareData_PoAndTypeMpo() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.MARKETPLACE_OWNER;
        createRevenuShareDbEntriesForTwoMonth(type,
                platformOperatorOrg.getKey());
        container.login(platformOperatorUser.getKey(), ROLE_PLATFORM_OPERATOR);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, platformOperatorOrg.getKey())));
        verifySchema(result, type);
    }

    @Test
    public void getRevenueShareData_PoAndTypeSupplier() throws Exception {
        // given
        BillingSharesResultType type = BillingSharesResultType.SUPPLIER;
        createRevenuShareDbEntriesForTwoMonth(type,
                platformOperatorOrg.getKey());
        container.login(platformOperatorUser.getKey(), ROLE_PLATFORM_OPERATOR);

        // when
        byte[] data = bs.getRevenueShareData(Long.valueOf(PERIOD_START_MONTH1),
                Long.valueOf(PERIOD_END_MONTH1), type);

        // then
        String result = new String(data, "UTF-8");
        assertNotNull(result);
        assertTrue(result, result.contains(expectedXml(PERIOD_START_MONTH1,
                PERIOD_END_MONTH1, platformOperatorOrg.getKey())));
        verifySchema(result, type);
    }

    @Test
    public void getRevenueShareData_verifyPlatformOperator() throws Exception {
        // given
        BillingServiceBean service = new BillingServiceBean();
        service.sdr = mock(SharesDataRetrievalServiceLocal.class);
        service.dm = mock(DataService.class);
        doReturn(getTestUser(UserRoleType.PLATFORM_OPERATOR)).when(service.dm)
                .getCurrentUser();

        // when
        service.loadBillingSharesResult(0L, 1L,
                BillingSharesResultType.SUPPLIER, 5L);

        // then
        verify(service.sdr, times(1)).loadBillingSharesResult(
                eq(BillingSharesResultType.SUPPLIER), eq(0L), eq(1L));
    }

    private PlatformUser getTestUser(UserRoleType... userRoleTypes) {
        PlatformUser user = new PlatformUser();

        user.setAssignedRoles(new HashSet<RoleAssignment>());
        for (UserRoleType role : userRoleTypes) {
            RoleAssignment roleAssignment = new RoleAssignment();
            roleAssignment.setRole(new UserRole(role));
            user.getAssignedRoles().add(roleAssignment);
        }
        return user;
    }

    @Test
    public void getRevenueShareData_verifyNotPlatformOperator()
            throws Exception {
        // given
        BillingServiceBean service = new BillingServiceBean();
        service.sdr = mock(SharesDataRetrievalServiceLocal.class);
        service.dm = mock(DataService.class);
        doReturn(getTestUser(UserRoleType.SERVICE_MANAGER)).when(service.dm)
                .getCurrentUser();
        long organizationKey = 5L;

        // when
        service.loadBillingSharesResult(0L, 1L,
                BillingSharesResultType.SUPPLIER, organizationKey);

        // then
        verify(service.sdr, times(1)).loadBillingSharesResultForOrganization(
                eq(organizationKey), eq(BillingSharesResultType.SUPPLIER),
                eq(0L), eq(1L));
    }

}
