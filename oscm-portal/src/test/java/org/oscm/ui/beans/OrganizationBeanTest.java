/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Florian Walker                                                   
 *                                                                              
 *  Creation Date: 01.12.2011                                                      
 *                                                                              
 *  Completion Time: 01.12.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.event.ValueChangeEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.TechnicalService;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;

public class OrganizationBeanTest {

    private String messageKey;
    private static final String APPLICATION_BEAN = "appBean";
    private static final String GROUP_NAME_A = "Group_A";
    private static final String GROUP_NAME_B = "Group_B";
    private static final String GROUP_NAME_C = "Group_C";
    private OrganizationBean organizationBean;
    private TechServiceBean techServiceBeanMock;
    private AccountService accountServiceMock;
    private UdaBean udaBean;
    private SessionBean sessionBean;
    private Organization organization;
    private VOOrganization voOrganization;
    private ServiceProvisioningService provisioningServiceMock;
    private UserGroupService userGroupServiceMock;
    private ApplicationBean appBean;

    private final List<VOUdaDefinition> voUdaDefinitions = new ArrayList<VOUdaDefinition>();
    private final List<VOUda> voUdas = new ArrayList<VOUda>();
    private final List<Organization> organizations = new ArrayList<Organization>();

    @Captor
    ArgumentCaptor<List<VOUda>> voUdaCaptor;

    @Captor
    ArgumentCaptor<VOOrganization> voOrgCaptor;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        udaBean = mock(UdaBean.class);
        sessionBean = mock(SessionBean.class);
        techServiceBeanMock = mock(TechServiceBean.class);
        accountServiceMock = mock(AccountService.class);
        userGroupServiceMock = mock(UserGroupService.class);
        appBean = mock(ApplicationBean.class);

        provisioningServiceMock = mock(ServiceProvisioningService.class);

        organizationBean = spy(new OrganizationBean());
        organizationBean.ui = mock(UiDelegate.class);
        organizationBean.setTechServiceBean(techServiceBeanMock);
        when(organizationBean.ui.findBean(eq(APPLICATION_BEAN))).thenReturn(
                appBean);
        doReturn(accountServiceMock).when(organizationBean)
                .getAccountingService();
        doReturn(provisioningServiceMock).when(organizationBean)
                .getProvisioningService();
        doReturn(userGroupServiceMock).when(organizationBean)
                .getUserGroupService();

        doNothing().when(organizationBean).addMessage(anyString(),
                any(FacesMessage.Severity.class), anyString(), anyString());

        doNothing().when(organizationBean).addMessage(anyString(),
                any(FacesMessage.Severity.class), anyString());

        doNothing().when(sessionBean).setSelectedCustomerId(anyString());

        organizationBean.setUdaBean(udaBean);
        organizationBean.setSessionBean(sessionBean);
        doNothing().when(organizationBean).concurrentModification();

        organization = mock(Organization.class);
        when(organization.getOrganizationId()).thenReturn("123");
        organizations.add(organization);
        doReturn(organizations).when(organizationBean).getCustomers();

        doNothing().when(organizationBean).updateSelectedCustomer(anyString(),
                any(VOOrganization.class));
    }

    @Test
    public void testEmail() throws Exception {
        // given
        VOOrganization organization = new VOOrganization();
        organization.setEmail("nobody@mail.com");
        organizationBean.organization = organization;
        User currentUser = new User(new VOUserDetails());
        currentUser.setOrganizationId("1");
        doReturn(currentUser).when(organizationBean).getUserFromSession();

        // when
        organizationBean.save();

        // then
        verify(accountServiceMock).updateAccountInformation(
                voOrgCaptor.capture(), any(VOUserDetails.class), anyString(),
                any(VOImageResource.class));
        assertEquals("nobody@mail.com", voOrgCaptor.getValue().getEmail());
    }

    /**
     * Invalid token (due to back or F5)
     */
    @Test
    public void testAddSuppliersForTechnicalService_inValidToken()
            throws Exception {
        doReturn(Boolean.FALSE).when(organizationBean).isTokenValid();
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.addSuppliersForTechnicalService());

        verify(accountServiceMock, never()).addSuppliersForTechnicalService(
                any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * No technical service selected.
     */
    @Test
    public void testAddSuppliersForTechnicalService_noSelectedTs()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();

        organizationBean.setSupplierIdToAdd("id");

        // techservice mock returns null
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.addSuppliersForTechnicalService());

        verify(accountServiceMock, never()).addSuppliersForTechnicalService(
                any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * No id was selected.
     */
    @Test
    public void testAddSuppliersForTechnicalService_noSelectedId()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();
        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        // supplier id is null
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.addSuppliersForTechnicalService());

        organizationBean.setSupplierIdToAdd("");

        // supplier id is empty
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.addSuppliersForTechnicalService());

        verify(accountServiceMock, never()).addSuppliersForTechnicalService(
                any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * Regular execution.
     */
    @Test
    public void testAddSuppliersForTechnicalService_ok() throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();
        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        String id = "id";
        organizationBean.setSupplierIdToAdd(id);

        // supplier id is null
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.addSuppliersForTechnicalService());

        verify(organizationBean).getAccountingService();

        List<String> ids = new ArrayList<String>();
        ids.add(id);

        verify(accountServiceMock).addSuppliersForTechnicalService(
                selectedService, ids);

        verify(organizationBean).addMessage(null, FacesMessage.SEVERITY_INFO,
                BaseBean.INFO_SUPPLIER_ADDED, id);
    }

    /**
     * Invalid token (due to back or F5)
     */
    @Test
    public void testRemoveSuppliersFromTechnicalService_inValidToken()
            throws Exception {
        doReturn(Boolean.FALSE).when(organizationBean).isTokenValid();
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.removeSuppliersFromTechnicalService());

        verify(accountServiceMock, never())
                .removeSuppliersFromTechnicalService(
                        any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * No technical service selected.
     */
    @Test
    public void testRemoveSuppliersFromTechnicalService_noSelectedTs()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();

        organizationBean.setSupplierIdToAdd("id");

        // techservice mock returns null
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.removeSuppliersFromTechnicalService());

        verify(accountServiceMock, never())
                .removeSuppliersFromTechnicalService(
                        any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * No org ids was selected.
     */
    @Test
    public void testRemoveSuppliersFromTechnicalService_noSelectedIds()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();
        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        doReturn(new String[0]).when(organizationBean)
                .getSelectedOrganizationIds();

        // no supplier isd available
        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.removeSuppliersFromTechnicalService());

        verify(accountServiceMock, never())
                .removeSuppliersFromTechnicalService(
                        any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * Not confirmed instance message (first call => user must confirm)
     */
    @Test
    public void testRemoveSuppliersFromTechnicalService_notConfirmed()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();
        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        String[] ids = new String[] { "orgid" };
        doReturn(ids).when(organizationBean).getSelectedOrganizationIds();

        doReturn(Boolean.FALSE).when(organizationBean).isConfirmed();

        ArrayList<String> instanceIds = new ArrayList<String>();
        doReturn(instanceIds).when(provisioningServiceMock)
                .getInstanceIdsForSellers(Arrays.asList(ids));

        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.removeSuppliersFromTechnicalService());

        verify(provisioningServiceMock).getInstanceIdsForSellers(
                Arrays.asList(ids));

        verify(accountServiceMock, never())
                .removeSuppliersFromTechnicalService(
                        any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * User confirmed the deletion
     */
    @Test
    public void testRemoveSuppliersFromTechnicalService_confirmed()
            throws Exception {
        doReturn(Boolean.TRUE).when(organizationBean).isTokenValid();
        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        String[] ids = new String[] { "orgid" };
        doReturn(ids).when(organizationBean).getSelectedOrganizationIds();

        doReturn(Boolean.TRUE).when(organizationBean).isConfirmed();

        Assert.assertEquals(BaseBean.OUTCOME_SUCCESS,
                organizationBean.removeSuppliersFromTechnicalService());

        verify(organizationBean).resetToken();
        verify(accountServiceMock).removeSuppliersFromTechnicalService(
                any(VOTechnicalService.class), anyListOf(String.class));
    }

    /**
     * No ts selected, suppliers list is empty
     */
    @Test
    public void testGetSuppliersForTechnicalService_noTsSelected()
            throws Exception {

        Assert.assertEquals(null,
                organizationBean.getSuppliersForTechnicalService());

        verify(accountServiceMock, never()).getSuppliersForTechnicalService(
                any(VOTechnicalService.class));
    }

    /**
     * Ts was selected, function was called the first time => acount service
     * call to fill the cache.
     */
    @Test
    public void testGetSuppliersForTechnicalService_tsSelected()
            throws Exception {

        VOTechnicalService selectedService = new VOTechnicalService();
        TechnicalService ts = new TechnicalService(selectedService);
        doReturn(ts).when(techServiceBeanMock).getSelectedTechnicalService();

        VOOrganization voOrganization = new VOOrganization();
        List<VOOrganization> suppliers = new ArrayList<VOOrganization>();
        suppliers.add(voOrganization);

        doReturn(suppliers).when(accountServiceMock)
                .getSuppliersForTechnicalService(selectedService);

        List<Organization> resultList = organizationBean
                .getSuppliersForTechnicalService();
        Assert.assertNotNull(resultList);
        Assert.assertEquals(1, resultList.size());
    }

    @Test
    public void isDeleteSupplierEnabled_withSelection() throws Exception {
        // given
        List<Organization> suppliers = givenSuppliersSelectedForDeletion();
        doReturn(suppliers).when(organizationBean)
                .getSuppliersForTechnicalService();

        // when
        boolean result = organizationBean.isDeleteSupplierEnabled();

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void isDeleteSupplierEnabled_NoSelection() throws Exception {
        // given
        List<Organization> suppliers = givenSuppliersNotSelected();

        doReturn(suppliers).when(organizationBean)
                .getSuppliersForTechnicalService();

        // when
        boolean result = organizationBean.isDeleteSupplierEnabled();

        // then
        Assert.assertFalse(result);

    }

    @Test
    public void isDeleteSupplierEnabled_OperationNotPermitted()
            throws Exception {
        // given
        doReturn(anyTechnicalService()).when(techServiceBeanMock)
                .getSelectedTechnicalService();
        changeTechnicalService();

        // when
        doThrow(new OperationNotPermittedException()).when(organizationBean)
                .getSuppliersForTechnicalService();

        // then
        Assert.assertFalse(organizationBean.isDeleteSupplierEnabled());
        verify(organizationBean).concurrentModification();
    }

    @Test
    public void isDeleteSupplierEnabled_ObjectNotFound() throws Exception {

        // given
        doReturn(anyTechnicalService()).when(techServiceBeanMock)
                .getSelectedTechnicalService();

        // when
        doThrow(new ObjectNotFoundException()).when(organizationBean)
                .getSuppliersForTechnicalService();

        // then
        Assert.assertFalse(organizationBean.isDeleteSupplierEnabled());
        verify(organizationBean).concurrentModification();
    }

    @Test
    public void isCustomerOrganization_Reseller() throws Exception {
        // given
        final User u = new User(new VOUserDetails());
        u.getVOUserDetails().setOrganizationRoles(
                Collections.singleton(OrganizationRoleType.RESELLER));
        doReturn(u).when(organizationBean).getCurrentUser();

        // when
        boolean customer = organizationBean.isCustomerOrganization();

        // then
        Assert.assertFalse(customer);
    }

    @Test
    public void isCustomerOrganization_Broker() throws Exception {
        // given
        final User u = new User(new VOUserDetails());
        u.getVOUserDetails().setOrganizationRoles(
                Collections.singleton(OrganizationRoleType.BROKER));
        doReturn(u).when(organizationBean).getCurrentUser();

        // when
        boolean customer = organizationBean.isCustomerOrganization();

        // then
        Assert.assertFalse(customer);
    }

    @Test
    public void isCustomerOrganization_Supplierer() throws Exception {
        // given
        final User u = new User(new VOUserDetails());
        u.getVOUserDetails().setOrganizationRoles(
                Collections.singleton(OrganizationRoleType.SUPPLIER));
        doReturn(u).when(organizationBean).getCurrentUser();

        // when
        boolean customer = organizationBean.isCustomerOrganization();

        // then
        Assert.assertFalse(customer);
    }

    @Test
    public void isCustomerOrganization() throws Exception {
        // given
        final User u = new User(new VOUserDetails());
        doReturn(u).when(organizationBean).getCurrentUser();

        // when
        boolean customer = organizationBean.isCustomerOrganization();

        // then
        Assert.assertTrue(customer);
    }

    /**
     * The user selects another TS.
     */
    @Test
    public void testGetSuppliersForTechnicalService_tsChanged()
            throws Exception {

        VOTechnicalService selectedService1 = new VOTechnicalService();
        TechnicalService ts1 = new TechnicalService(selectedService1);
        doReturn(ts1).when(techServiceBeanMock).getSelectedTechnicalService();
        VOOrganization voOrganization1 = new VOOrganization();
        voOrganization1.setName("1");
        List<VOOrganization> suppliers1 = new ArrayList<VOOrganization>();
        suppliers1.add(voOrganization1);
        doReturn(suppliers1).when(accountServiceMock)
                .getSuppliersForTechnicalService(selectedService1);

        organizationBean.getSuppliersForTechnicalService();
        organizationBean.getSuppliersForTechnicalService();

        changeTechnicalService();

        organizationBean.getSuppliersForTechnicalService();

        // Must be called 2 time NOT 3
        verify(accountServiceMock, times(2)).getSuppliersForTechnicalService(
                any(VOTechnicalService.class));
    }

    @Test
    public void updateCustomer_FilterSubscriptionUdas() throws Exception {
        // given
        // create three different type of Udas
        createUdaWithConfigurationType(UdaConfigurationType.SUPPLIER, 123);
        createUdaWithConfigurationType(
                UdaConfigurationType.USER_OPTION_MANDATORY, 1234);
        createUdaWithConfigurationType(
                UdaConfigurationType.USER_OPTION_OPTIONAL, 12345);
        List<UdaRow> organizationUdaRows = UdaRow.getUdaRows(voUdaDefinitions,
                voUdas);
        // initial for organizationBean.selectedCustomer
        voOrganization = mock(VOOrganization.class);
        when(organization.getVOOrganization()).thenReturn(voOrganization);
        when(voOrganization.getDiscount()).thenReturn(null);
        when(udaBean.getCustomerUdas(anyLong()))
                .thenReturn(organizationUdaRows);
        voOrganization.setKey(123);

        when(
                accountServiceMock
                        .updateCustomerDiscount(any(VOOrganization.class)))
                .thenReturn(voOrganization);
        organizationBean.setSelectedCustomerId("123");
        organizationBean.getOrganizationUdas();
        // when
        organizationBean.updateCustomer();
        // then
        verify(accountServiceMock, times(1)).saveUdas(voUdaCaptor.capture());
        assertEquals(1, voUdaCaptor.getValue().size());
        assertEquals(123, voUdaCaptor.getValue().get(0).getKey());
        verify(organizationBean, times(1)).addMessage(anyString(),
                any(Severity.class), eq(BaseBean.INFO_ORGANIZATION_UPDATED),
                anyString());
    }

    @Test
    public void updateCustomer_DiscountDateBefore() throws Exception {
        // given
        Long startTime = Long
                .valueOf(System.currentTimeMillis() + 1370070000000L);
        Long endTime = Long.valueOf(System.currentTimeMillis());
        prepareTestUpdateCustomer("20.0", startTime, endTime);
        // when
        String result = organizationBean.updateCustomer();
        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(organizationBean, times(1)).addMessage(anyString(),
                any(Severity.class), eq(BaseBean.ERROR_DISCOUNT_DATE_BEFORE));
    }

    @Test
    public void updateCustomer_DiscountDateFuture() throws Exception {
        // given
        prepareTestUpdateCustomer("20.0", Long.valueOf(0),
                Long.valueOf(System.currentTimeMillis() - 1370070000000L));
        // when
        String result = organizationBean.updateCustomer();
        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(organizationBean, times(1)).addMessage(anyString(),
                any(Severity.class), eq(BaseBean.ERROR_DISCOUNT_DATE_FUTURE));
    }

    @Test
    public void updateCustomer_OK() throws Exception {
        // given
        Long startTime = Long.valueOf(System.currentTimeMillis());
        Long endTime = Long
                .valueOf(System.currentTimeMillis() + 1370070000000L);
        prepareTestUpdateCustomer("20.0", startTime, endTime);
        // when
        String result = organizationBean.updateCustomer();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(organizationBean, times(1)).addMessage(anyString(),
                any(Severity.class), eq(BaseBean.INFO_ORGANIZATION_UPDATED),
                anyString());
    }

    @Test
    public void isInternalAuthMode_Yes() {
        givenAuthMode(true);
        boolean result = organizationBean.isInternalAuthMode();
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isInternalAuthMode_No() {
        givenAuthMode(false);
        boolean result = organizationBean.isInternalAuthMode();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void technicalServiceChanged() {
        // when
        changeTechnicalService();

        // then
        verify(techServiceBeanMock, times(1))
                .setSelectedTechnicalServiceKeyWithExceptionAndRefresh(
                        anyLong());
    }

    public void getInitialize_invalidLocale() {
        // given
        final User u = new User(new VOUserDetails());
        u.setLocale("ja");
        doReturn(u).when(organizationBean).getCurrentUser();
        List<String> localesStr = new ArrayList<String>();
        localesStr.add("en");
        localesStr.add("de");
        doReturn(localesStr).when(appBean).getActiveLocales();
        doReturn(appBean).when(organizationBean).getApplicationBean();

        // when
        organizationBean.getInitialize();

        // then
        verify(appBean, times(1)).checkLocaleValidation("ja");
    }

    @Test
    public void initializeGroups() {
        // given
        doReturn(givenPOUserGroups()).when(userGroupServiceMock)
                .getUserGroupsForUserWithoutDefault(anyLong());
        organizationBean.currentUser = new User(new VOUserDetails());
        organizationBean.currentUser.setUserId("UserId");

        // when
        organizationBean.initializeGroups();

        // then
        boolean groups = organizationBean.getCurrentUser().getGroupsToDisplay()
                .contains(GROUP_NAME_A);
        assertEquals(Boolean.TRUE, Boolean.valueOf(groups));
    }

    @Test
    public void initializeGroups_WhiteSpace() {
        // given
        doReturn(givenPOUserGroups()).when(userGroupServiceMock)
                .getUserGroupsForUserWithoutDefault(anyLong());
        organizationBean.currentUser = new User(new VOUserDetails());
        organizationBean.currentUser.setUserId("UserId");

        // when
        organizationBean.initializeGroups();

        // then
        assertEquals("Group_A, Group_B, Group_C", organizationBean
                .getCurrentUser().getGroupsToDisplay());
    }

    @Test
    public void delete_TechnicalServiceNotAliveException() throws Exception {
        // given
        organizationBean = prepareOrganizationBean();
        organizationBean.accountingService = accountServiceMock;
        doThrow(new TechnicalServiceNotAliveException()).when(
                organizationBean.accountingService).deregisterOrganization();
        // when
        String result = organizationBean.delete();

        // then
        assertEquals(BaseBean.ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION,
                messageKey);
        assertEquals("", result);
    }

    @Test
    public void delete_TechnicalServiceOperationException() throws Exception {
        // given
        organizationBean = prepareOrganizationBean();
        organizationBean.accountingService = accountServiceMock;
        doThrow(new TechnicalServiceOperationException()).when(
                organizationBean.accountingService).deregisterOrganization();
        // when
        String result = organizationBean.delete();

        // then
        assertEquals(BaseBean.ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION,
                messageKey);
        assertEquals("", result);
    }

    private List<POUserGroup> givenPOUserGroups() {
        List<POUserGroup> groups = new ArrayList<POUserGroup>();
        POUserGroup groupA = new POUserGroup();
        groupA.setGroupName(GROUP_NAME_A);
        POUserGroup groupB = new POUserGroup();
        groupB.setGroupName(GROUP_NAME_B);
        POUserGroup groupC = new POUserGroup();
        groupC.setGroupName(GROUP_NAME_C);
        groups.add(groupA);
        groups.add(groupB);
        groups.add(groupC);
        return groups;
    }

    private void prepareTestUpdateCustomer(String value, Long startTime,
            Long endTime) throws Exception {
        voOrganization = new VOOrganization();
        when(organization.getVOOrganization()).thenReturn(voOrganization);
        voOrganization.setOrganizationId("123");
        VODiscount voDiscount = createVODiscount(value, startTime, endTime);
        voOrganization.setDiscount(voDiscount);
        when(
                accountServiceMock
                        .updateCustomerDiscount(any(VOOrganization.class)))
                .thenReturn(voOrganization);
        organizationBean.setSelectedCustomerId("123");

    }

    private VODiscount createVODiscount(String value, Long startTime,
            Long endTime) {
        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(new BigDecimal(value));
        voDiscount.setStartTime(startTime);
        voDiscount.setEndTime(endTime);
        return voDiscount;
    }

    private void createUdaWithConfigurationType(UdaConfigurationType type,
            long key) {
        VOUdaDefinition voUdaDefinition = new VOUdaDefinition();
        VOUda voUda = new VOUda();

        voUdaDefinition.setConfigurationType(type);
        voUdaDefinition.setKey(key);
        voUda.setUdaDefinition(voUdaDefinition);
        voUda.setKey(key);
        voUdas.add(voUda);
        voUdaDefinitions.add(voUdaDefinition);
    }

    private List<Organization> givenOrganization(boolean selected) {
        List<Organization> orgs = new ArrayList<Organization>();
        Organization org = new Organization(new VOOrganization());
        org.setSelected(selected);
        orgs.add(org);
        return orgs;
    }

    private List<Organization> givenSuppliersSelectedForDeletion() {
        return givenOrganization(true);
    }

    private List<Organization> givenSuppliersNotSelected() {
        return givenOrganization(false);
    }

    private TechnicalService anyTechnicalService() {
        return new TechnicalService(new VOTechnicalService());
    }

    private void changeTechnicalService() {
        ValueChangeEvent vcEvent = mock(ValueChangeEvent.class);
        doReturn(Long.valueOf("2")).when(vcEvent).getNewValue();
        organizationBean.technicalServiceChanged(vcEvent);
    }

    private void givenAuthMode(boolean internalAuthMode) {
        doReturn(Boolean.valueOf(internalAuthMode)).when(appBean)
                .isInternalAuthMode();
    }

    private OrganizationBean prepareOrganizationBean() {
        return organizationBean = new OrganizationBean() {
            /**
             * 
             */
            private static final long serialVersionUID = 8837356005032690342L;

            @Override
            public void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
                messageKey = key;
            }
        };
    }
}
