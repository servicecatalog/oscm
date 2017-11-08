/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exceptions.NoBilingSharesDataAvailableException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

@SuppressWarnings("boxing")
public class ExportBillingDataServiceTest {

    private static final String TEST_ORGANIZATION_NAME = "test organization";
    private static final String TEST_ORGANIZATION_ID = "abcd1234";
    private static final String TEST_ADDRESS = "test address";
    ExportBillingDataServiceBean exportBillingService;

    @Before
    public void setup() {

        exportBillingService = new ExportBillingDataServiceBean();
        exportBillingService.accountService = mock(AccountService.class);

        exportBillingService.idService = mock(IdentityService.class);
        exportBillingService.billingService = mock(BillingService.class);

        exportBillingService.dm = mock(DataService.class);
        createAndSetCurrentOrg();
        Query query = mock(Query.class);
        doReturn(query).when(exportBillingService.dm).createQuery(anyString());
    }

    /**
     * Empty list is returned in case of exception List<POOrganization>
     * getCustomers()
     */
    @Test
    public void getCustomers_exception() throws Exception {

        // given
        given(exportBillingService.accountService.getMyCustomers()).willThrow(
                new OrganizationAuthoritiesException());

        // when
        List<POOrganization> pos = exportBillingService.getCustomers();

        // then
        assertThat(pos, hasNoItems());

    }

    @Test
    public void getOrganizationRoles_PlatformOperator() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setOrganizationRoles(orgRoles);

        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.PLATFORM_OPERATOR);
        voUserDetails.setUserRoles(userRoles);

        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(voUserDetails);

        // when
        List<BillingSharesResultType> returnedRoles = exportBillingService
                .getBillingShareResultTypes();

        // then
        assertTrue(returnedRoles
                .contains(BillingSharesResultType.MARKETPLACE_OWNER));
        assertTrue(returnedRoles.contains(BillingSharesResultType.RESELLER));
        assertTrue(returnedRoles.contains(BillingSharesResultType.BROKER));
        assertTrue(returnedRoles.contains(BillingSharesResultType.SUPPLIER));

    }

    @Test
    public void getBillingShareResultTypes_WithOrganizationRoleAndUserRole() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.BROKER);
        orgRoles.add(OrganizationRoleType.CUSTOMER);
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setOrganizationRoles(orgRoles);
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.BROKER_MANAGER);
        voUserDetails.setUserRoles(userRoles);

        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(voUserDetails);

        // when
        List<BillingSharesResultType> returnedRoles = exportBillingService
                .getBillingShareResultTypes();

        // then
        assertTrue(returnedRoles.contains(BillingSharesResultType.BROKER));
        assertFalse(returnedRoles
                .contains(BillingSharesResultType.MARKETPLACE_OWNER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.RESELLER));

        assertFalse(returnedRoles.contains(BillingSharesResultType.SUPPLIER));

    }

    @Test
    public void getOrganizationRoles_OrganizationRole_Without_UserRole() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.BROKER);
        orgRoles.add(OrganizationRoleType.CUSTOMER);
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setOrganizationRoles(orgRoles);

        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(voUserDetails);

        // when
        List<BillingSharesResultType> returnedRoles = exportBillingService
                .getBillingShareResultTypes();

        // then
        assertFalse(returnedRoles.contains(BillingSharesResultType.BROKER));
        assertFalse(returnedRoles
                .contains(BillingSharesResultType.MARKETPLACE_OWNER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.RESELLER));

        assertFalse(returnedRoles.contains(BillingSharesResultType.SUPPLIER));
    }

    @Test
    public void getBillingShareResultTypes_IntersectionOrgRoleAndUserRole() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.SUPPLIER);
        orgRoles.add(OrganizationRoleType.CUSTOMER);
        orgRoles.add(OrganizationRoleType.MARKETPLACE_OWNER);
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setOrganizationRoles(orgRoles);

        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.MARKETPLACE_OWNER);
        voUserDetails.setUserRoles(userRoles);

        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(voUserDetails);

        // when
        List<BillingSharesResultType> returnedRoles = exportBillingService
                .getBillingShareResultTypes();

        // then

        assertFalse(returnedRoles.contains(BillingSharesResultType.SUPPLIER));
        assertTrue(returnedRoles
                .contains(BillingSharesResultType.MARKETPLACE_OWNER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.RESELLER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.BROKER));

    }

    @Test
    public void getBillingShareResultTypes_SupplierAndMPO_BothUserRoles() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.SUPPLIER);
        orgRoles.add(OrganizationRoleType.CUSTOMER);
        orgRoles.add(OrganizationRoleType.MARKETPLACE_OWNER);
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setOrganizationRoles(orgRoles);

        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.MARKETPLACE_OWNER);
        userRoles.add(UserRoleType.SERVICE_MANAGER);
        voUserDetails.setUserRoles(userRoles);

        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(voUserDetails);

        // when
        List<BillingSharesResultType> returnedRoles = exportBillingService
                .getBillingShareResultTypes();

        // then

        assertTrue(returnedRoles.contains(BillingSharesResultType.SUPPLIER));
        assertTrue(returnedRoles
                .contains(BillingSharesResultType.MARKETPLACE_OWNER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.RESELLER));
        assertFalse(returnedRoles.contains(BillingSharesResultType.BROKER));

    }

    @Test
    public void exportBillingData_returnXML() throws Exception {
        // given
        byte[] xmlResult = { '1', '2', '3' };
        POBillingDataExport po = new POBillingDataExport();
        po.setFrom(new Date());
        po.setTo(new Date());
        po.setOrganizationIds(Arrays.asList("orgId"));
        given(
                exportBillingService.billingService.getCustomerBillingData(
                        Matchers.anyLong(), Matchers.anyLong(),
                        Matchers.anyListOf(String.class)))
                .willReturn(xmlResult);

        // when
        Response r = exportBillingService.exportBillingData(po);

        // then
        assertEquals(r.getResult(byte[].class), xmlResult);

    }

    @Test(expected = NoBilingSharesDataAvailableException.class)
    public void exportBillingShares_NodataFound() throws Exception {
        // given
        PORevenueShareExport po = new PORevenueShareExport();
        po.setFrom(new Date());
        po.setTo(new Date());
        po.setRevenueShareType(BillingSharesResultType.BROKER);
        byte[] empty = {};
        given(
                exportBillingService.billingService.getRevenueShareData(
                        Matchers.anyLong(), Matchers.anyLong(),
                        Matchers.any(BillingSharesResultType.class)))
                .willReturn(empty);

        // when
        exportBillingService.exportRevenueShares(po);
    }

    private void createAndSetCurrentOrg() {
        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        org.setKey(1000);
        user.setOrganization(org);
        doReturn(user).when(exportBillingService.dm).getCurrentUser();
    }

    @Test
    public void isPlatformOperator() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        orgRoles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setOrganizationRoles(orgRoles);
        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(userDetails);

        // when
        boolean isPlatformOperator = exportBillingService.isPlatformOperator();

        // then
        assertTrue(isPlatformOperator);
    }

    @Test
    public void isPlatformOperator_False() {
        // given
        Set<OrganizationRoleType> orgRoles = new HashSet<OrganizationRoleType>();
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setOrganizationRoles(orgRoles);
        given(exportBillingService.idService.getCurrentUserDetails())
                .willReturn(userDetails);

        // when
        boolean isPlatformOperator = exportBillingService.isPlatformOperator();

        // then
        assertFalse(isPlatformOperator);
    }

    @Test
    public void getCustomers_noCustomers()
            throws OrganizationAuthoritiesException {
        // given
        List<VOOrganization> emptyList = new ArrayList<VOOrganization>();
        given(exportBillingService.accountService.getMyCustomers()).willReturn(
                emptyList);
        // when
        List<POOrganization> customers = exportBillingService.getCustomers();
        // then
        assertEquals(customers.size(), 0);
    }

    @Test
    public void getCustomers() throws OrganizationAuthoritiesException {
        // given
        List<VOOrganization> organizationList = createOrganizationList();
        List<POOrganization> poList = createCustomerList();

        given(exportBillingService.accountService.getMyCustomersOptimization())
                .willReturn(organizationList);
        // when
        List<POOrganization> returnedCustomers = exportBillingService
                .getCustomers();
        // then
        assertEquals(poList, returnedCustomers);
        assertEquals(poList.get(0), returnedCustomers.get(0));
    }

    private List<VOOrganization> createOrganizationList() {
        List<VOOrganization> customerList = new ArrayList<VOOrganization>();
        VOOrganization vo = new VOOrganization();
        vo.setKey(1L);
        vo.setAddress(TEST_ADDRESS);
        vo.setOrganizationId(TEST_ORGANIZATION_ID);
        vo.setName(TEST_ORGANIZATION_NAME);
        customerList.add(vo);
        return customerList;
    }

    private List<POOrganization> createCustomerList() {
        List<POOrganization> poList = new ArrayList<POOrganization>();
        POOrganization testCustomer = new POOrganization();
        testCustomer.setKey(1L);
        testCustomer.setOrganizationAddress(TEST_ADDRESS);
        testCustomer.setOrganizationId(TEST_ORGANIZATION_ID);
        testCustomer.setOrganizationName(TEST_ORGANIZATION_NAME);
        poList.add(testCustomer);
        return poList;
    }
}
