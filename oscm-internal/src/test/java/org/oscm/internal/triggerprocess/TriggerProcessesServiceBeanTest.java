/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 27, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.triggerprocess;

import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.POSubscriptionForList;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUser;

/**
 * @author zankov
 * 
 */
public class TriggerProcessesServiceBeanTest {

    TriggerProcessesServiceBean service;
    DataService dm;
    PlatformUser organizationAdmin;
    PlatformUser subscriptionMgr;
    PlatformUser customer;

    @Before
    public void setup() {
        service = new TriggerProcessesServiceBean();
        dm = mock(DataService.class);

        organizationAdmin = new PlatformUser();
        setAdminRole(organizationAdmin, UserRoleType.ORGANIZATION_ADMIN);
        organizationAdmin.setKey(123456);

        subscriptionMgr = new PlatformUser();
        setAdminRole(subscriptionMgr, UserRoleType.SUBSCRIPTION_MANAGER);
        subscriptionMgr.setKey(123457);

        customer = new PlatformUser();
        customer.setKey(123458);

        when(dm.getCurrentUser()).thenReturn(organizationAdmin);

        service.dm = dm;
    }

    @Test
    public void getAllWaitingForApprovalSubscriptions_NoTriggerProcesses() {
        // given
        noTriggerProcesses();

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    @Test
    public void getAllWaitingForApprovalSubscriptions_NoCorrespondingResults() {
        // given
        oneApprovedTriggerProcess();

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    @Test
    public void getAllWaitingForApprovalSubscriptions() {
        // given
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasOneItem());
    }

    @Test
    public void getAllTriggerSubscriptions_DummyUserAssignment() {
        // given
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertEquals(1, response.getResultList(POSubscriptionForList.class)
                .get(0).getNumberOfAssignedUsers());
    }

    @Test
    public void getAllTriggerSubscriptions_SubManager() {
        // given
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);
        loginAs(subscriptionMgr);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    @Test
    public void getAllTriggerSubscriptions_OrgAdminManager() {
        // given
        oneTriggerProcess(subMgr(), TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasOneItem());
    }

    @Test
    public void getAllTriggerSubscriptions_Customer() {
        // given
        oneTriggerProcess(subMgr(), TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);
        loginAs(customer);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    @Test
    public void getAllTriggerSubscriptions_NotSubscribeToService() {
        // given waiting for approval trigger for unsusbcribe
        oneTriggerProcess(subMgr(), TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.UNSUBSCRIBE_FROM_SERVICE, true);
        loginAs(customer);

        // when
        Response response = service.getAllWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    /**
     * @return
     */
    private VOUser subMgr() {
        VOUser user = new VOUser();
        user.setKey(subscriptionMgr.getKey());
        return user;
    }

    private void loginAs(PlatformUser user) {
        when(dm.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions_OtherUser() {
        // given current user do not have subscriptions in
        // "Waiting for approval" status
        oneTriggerProcess(someOtherUser(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscription.class), hasNoItems());
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions_NotSubscribeToService() {
        // given trigger process in "Waiting for approval" status but not for
        // subscribe
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.UPGRADE_SUBSCRIPTION, true);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscription.class), hasNoItems());
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions_NoAutoAssignment() {
        // given no auto assignment option chosen
        // but the status is "Waiting for approval"
        oneTriggerProcess(someOtherUser(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, false);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscription.class), hasNoItems());
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions_NoCorrespondingResults() {
        oneTriggerProcess(organizationAdmin(), TriggerProcessStatus.APPROVED,
                TriggerType.SUBSCRIBE_TO_SERVICE, false);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscriptionForList.class),
                hasNoItems());
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertThat(response.getResultList(POSubscription.class), hasOneItem());
    }

    @Test
    public void getMyWaitingForApprovalSubscriptions_DummyAssignment() {
        // given
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service.getMyWaitingForApprovalSubscriptions();

        // expected
        assertEquals(1, response.getResultList(POSubscription.class).get(0)
                .getNumberOfAssignedUsers());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_MODIFY_SUBSCRIPTION() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.MODIFY_SUBSCRIPTION, true);

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(1, response.getResultList(VOTriggerProcess.class).size());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_UPGRADE_SUBSCRIPTION() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.UPGRADE_SUBSCRIPTION, true);

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(1, response.getResultList(VOTriggerProcess.class).size());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_UNSUBSCRIBE_FROM_SERVICE() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.UNSUBSCRIBE_FROM_SERVICE, true);

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(1, response.getResultList(VOTriggerProcess.class).size());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_ADD_REVOKE_USER() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.ADD_REVOKE_USER, true);

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(1, response.getResultList(VOTriggerProcess.class).size());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_SUBSCRIBE_TO_SERVICE() {
        oneTriggerProcess(organizationAdmin(),
                TriggerProcessStatus.WAITING_FOR_APPROVAL,
                TriggerType.SUBSCRIBE_TO_SERVICE, true);

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(1, response.getResultList(VOTriggerProcess.class).size());
    }

    @Test
    public void getAllWaitingForApprovalTriggerProcessesBySubId_noTriggerProcess() {
        noTriggerProcesses();

        // when
        Response response = service
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId("subId");

        // expected
        assertEquals(0, response.getResultList(VOTriggerProcess.class).size());
    }

    private VOUser someOtherUser() {
        VOUser result = new VOUser();
        result.setKey(2L);
        return result;
    }

    private VOUser organizationAdmin() {
        VOUser user = new VOUser();
        user.setKey(organizationAdmin.getKey());
        return user;
    }

    private void noTriggerProcesses() {
        TriggerService triggerService = mock(TriggerService.class);
        TriggerServiceLocal triggerServiceLocal = mock(TriggerServiceLocal.class);
        service.triggerService = triggerService;
        service.triggerServiceLocal = triggerServiceLocal;
        when(triggerService.getAllActionsForOrganization()).thenReturn(
                new ArrayList<VOTriggerProcess>());
        when(triggerServiceLocal.getAllActionsForSubscription(anyString()))
                .thenReturn(new ArrayList<VOTriggerProcess>());

    }

    private void oneApprovedTriggerProcess() {
        TriggerService triggerService = mock(TriggerService.class);
        TriggerServiceLocal triggerServiceLocal = mock(TriggerServiceLocal.class);
        service.triggerService = triggerService;
        service.triggerServiceLocal = triggerServiceLocal;

        ArrayList<VOTriggerProcess> result = new ArrayList<VOTriggerProcess>();
        VOTriggerProcess triggerProcess = new VOTriggerProcess();
        triggerProcess.setStatus(TriggerProcessStatus.APPROVED);
        result.add(triggerProcess);

        when(triggerService.getAllActionsForOrganization()).thenReturn(result);
        when(triggerServiceLocal.getAllActionsForSubscription(anyString()))
                .thenReturn(result);
    }

    private void oneTriggerProcess(VOUser user, TriggerProcessStatus status,
            TriggerType type, boolean autoAssignment) {
        TriggerService triggerService = mock(TriggerService.class);
        TriggerServiceLocal triggerServiceLocal = mock(TriggerServiceLocal.class);
        service.triggerService = triggerService;
        service.triggerServiceLocal = triggerServiceLocal;

        ArrayList<VOTriggerProcess> result = new ArrayList<VOTriggerProcess>();
        VOTriggerProcess triggerProcess = new VOTriggerProcess();
        triggerProcess.setTriggerDefinition(triggerDefinition(type));
        triggerProcess.setStatus(status);
        triggerProcess.setSubscription(givenSubscription());
        triggerProcess.setService(givenService(autoAssignment));
        triggerProcess.setUser(user);
        result.add(triggerProcess);

        when(triggerService.getAllActionsForOrganization()).thenReturn(result);
        when(triggerServiceLocal.getAllActionsForSubscription(anyString()))
                .thenReturn(result);
    }

    private VOTriggerDefinition triggerDefinition(TriggerType type) {
        VOTriggerDefinition result = new VOTriggerDefinition();
        result.setType(type);
        return result;
    }

    private VOService givenService(boolean autoAssignment) {
        VOService result = new VOService();
        result.setKey(1L);
        result.setSellerName("sellerName");
        result.setAutoAssignUserEnabled(new Boolean(autoAssignment));
        return result;
    }

    private VOSubscription givenSubscription() {
        VOSubscription result = new VOSubscription();
        result.setSubscriptionId("subId1");
        result.setKey(1L);
        return result;
    }

    private void setAdminRole(PlatformUser user, UserRoleType type) {
        Set<RoleAssignment> grantedRoles = new HashSet<RoleAssignment>();
        RoleAssignment assignedRole = new RoleAssignment();
        UserRole role = new UserRole();
        role.setRoleName(type);
        assignedRole.setRole(role);
        grantedRoles.add(assignedRole);
        user.setAssignedRoles(grantedRoles);
    }
}
