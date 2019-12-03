/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SessionService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

public class SessionServiceWSTest {

  private static SessionService sessionService;
  private static SubscriptionService subscriptionService;
  private static SubscriptionService subscriptionServiceForSubManager;
  private static SessionService sessionServiceForSubManager;
  private static VOSubscription subscription;
  private static VOSubscription subscriptionSubManagerOwner;
  private static VOUserDetails subscriptionManager;
  private static VOOrganization customerOrg;
  private static VOFactory factory = new VOFactory();

  @Before
  public void setUp() throws Exception {
    WebserviceTestBase.getOperator().addCurrency("EUR");

    WebserviceTestSetup setup = new WebserviceTestSetup();

    // Create a supplier
    VOOrganization supplier = setup.createSupplier("Supplier");
    // Create a technical service
    setup.createTechnicalService();

    MarketplaceService mpSrvOperator =
        ServiceFactory.getDefault()
            .getMarketPlaceService();

    // Create a local marketplace
    VOMarketplace mpLocal =
        mpSrvOperator.createMarketplace(
            factory.createMarketplaceVO(supplier.getOrganizationId(), false, "Local Marketplace"));

    // Create a chargeable service
    VOService chargeableService = setup.createAndActivateService("Service", mpLocal);

    // Create a customer
    customerOrg = setup.createCustomer("Customer");
    VOUserDetails customerUser = setup.getCustomerUser();

    // Fetch the subscription service of this customer
    subscriptionService =
        ServiceFactory.getDefault()
            .getSubscriptionService(
                String.valueOf(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    // Create a subscription
    subscription = factory.createSubscriptionVO("sub_" + WebserviceTestBase.createUniqueKey());

    // Subscribe to the chargeable service
    subscription =
        subscriptionService.subscribeToService(
            subscription,
            chargeableService,
            null,
            setup.getCustomerPaymentInfo(),
            setup.getCustomerBillingContact(),
            new ArrayList<VOUda>());

    // Get the session service of this customer
    sessionService =
        ServiceFactory.getDefault()
            .getSessionService(
                Long.toString(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    IdentityService identityService =
        ServiceFactory.getDefault()
            .getIdentityService(
                String.valueOf(customerUser.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    subscriptionManager = createVOUser();
    subscriptionManager =
        identityService.createUser(
            subscriptionManager,
            Arrays.asList(UserRoleType.SUBSCRIPTION_MANAGER),
            mpLocal.getMarketplaceId());
    subscriptionManager.setKey(
        Long.parseLong(WebserviceTestBase.readLastMailAndSetCommonPassword()));

    subscriptionServiceForSubManager =
        ServiceFactory.getDefault()
            .getSubscriptionService(
                String.valueOf(subscriptionManager.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    sessionServiceForSubManager =
        ServiceFactory.getDefault()
            .getSessionService(
                String.valueOf(subscriptionManager.getKey()), WebserviceTestBase.DEFAULT_PASSWORD);

    // Create a subscription
    subscriptionSubManagerOwner =
        factory.createSubscriptionVO("sub_" + WebserviceTestBase.createUniqueKey());

    subscriptionSubManagerOwner =
        subscriptionServiceForSubManager.subscribeToService(
            subscriptionSubManagerOwner,
            chargeableService,
            null,
            setup.getCustomerPaymentInfo(),
            setup.getCustomerBillingContact(),
            new ArrayList<VOUda>());
  }

  private static VOUserDetails createVOUser() throws Exception {
    VOUserDetails voUser = factory.createUserVO(Long.toHexString(System.currentTimeMillis()));
    voUser.setOrganizationId(customerOrg.getOrganizationId());
    voUser.setAdditionalName("additionalName");
    voUser.setAddress("address");
    voUser.setFirstName("firstName");
    voUser.setLastName("lastName");
    voUser.setPhone("08154711");
    voUser.setSalutation(Salutation.MR);
    return voUser;
  }

  @Test
  public void testCreateAndDeletePlatformSession() throws Exception {
    String sessionId = String.valueOf(System.currentTimeMillis());

    // Create a platform session
    sessionService.createPlatformSession(sessionId);

    // Delete this session
    int numOfDeletedSessions = sessionService.deletePlatformSession(sessionId);
    assertEquals(1, numOfDeletedSessions);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void testCreateServiceSession_SubBelongsToOtherOrg() throws Exception {
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";
    SessionService defaultSessionService = ServiceFactory.getDefault().getSessionService();
    defaultSessionService.createServiceSession(subscription.getKey(), sessionId, userToken);
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testCreateServiceSession_SubNotFound() throws Exception {
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";

    long nonExistingSubkey = 1000L;
    sessionService.createServiceSession(nonExistingSubkey, sessionId, userToken);
  }

  @Test
  public void testCreateAndDeleteServiceSession() throws Exception {

    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";

    // create a service session
    sessionService.createServiceSession(subscription.getKey(), sessionId, userToken);
    List<Long> subKeys = sessionService.getSubscriptionKeysForSessionId(sessionId);
    assertEquals(1, subKeys.size());
    assertEquals(Long.valueOf(subscription.getKey()), subKeys.get(0));
    assertNotNull(sessionService.resolveUserToken(subscription.getKey(), sessionId, userToken));

    // delete this session
    sessionService.deleteServiceSession(subscription.getKey(), sessionId);
    assertNull(sessionService.resolveUserToken(subscription.getKey(), sessionId, userToken));
  }

  public void testDeleteSessionsForSessionId() throws Exception {
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";

    // Create one platform session and one service session with the same
    // sessionId.
    sessionService.createPlatformSession(sessionId);
    sessionService.createServiceSession(subscription.getKey(), sessionId, userToken);

    // Delete all sessions with this id.
    sessionService.deleteSessionsForSessionId(sessionId);

    // check that no platform session and no service session exists
    assertNull(sessionService.resolveUserToken(subscription.getKey(), sessionId, userToken));
    assertEquals(0, sessionService.deletePlatformSession(sessionId));
  }

  @Test(expected = OperationNotPermittedException.class)
  public void testCreateServiceSession_SubDeactivated() throws Exception {
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";

    subscriptionService.unsubscribeFromService(subscription.getSubscriptionId());

    sessionService.createServiceSession(subscription.getKey(), sessionId, userToken);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void deleteServiceSessionsForSubscription_SubscriptionManagerNoPermission()
      throws Exception {
    // given
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";
    sessionService.createServiceSession(subscription.getKey(), sessionId, userToken);

    // when
    sessionServiceForSubManager.deleteServiceSessionsForSubscription(subscription.getKey());
  }

  @Test
  public void deleteServiceSessionsForSubscription_SubscriptionManagerPermission()
      throws Exception {
    // given
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";
    sessionService.createServiceSession(subscriptionSubManagerOwner.getKey(), sessionId, userToken);

    // when
    sessionServiceForSubManager.deleteServiceSessionsForSubscription(
        subscriptionSubManagerOwner.getKey());

    // then
    List<Long> subKeys = sessionService.getSubscriptionKeysForSessionId(sessionId);
    assertEquals(0, subKeys.size());
  }

  @Test(expected = OperationNotPermittedException.class)
  public void getNumberOfServiceSessions_SubscriptionManagerNoPermission() throws Exception {
    // given
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";
    sessionService.createServiceSession(subscription.getKey(), sessionId, userToken);

    // when
    sessionServiceForSubManager.getNumberOfServiceSessions(subscription.getKey());
  }

  @Test
  public void getNumberOfServiceSessions_SubscriptionManagerPermission() throws Exception {
    // given
    String sessionId = "session" + WebserviceTestBase.createUniqueKey();
    String userToken = "someToken";
    sessionService.createServiceSession(subscriptionSubManagerOwner.getKey(), sessionId, userToken);

    // when
    int numOfSessions =
        sessionServiceForSubManager.getNumberOfServiceSessions(
            subscriptionSubManagerOwner.getKey());

    // then
    assertEquals(1, numOfSessions);
  }
}
