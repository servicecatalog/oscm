/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

public class TriggerProcessIdentifiersTest {

    private static final long USER_KEY = 99;
    private static final long ORG_KEY = 199;

    private DataService ds;
    private VOService service;
    private VOSubscription sub;

    @Before
    public void setup() throws Exception {
        ds = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setKey(USER_KEY);
        Organization org = new Organization();
        user.setOrganization(org);
        org.setKey(ORG_KEY);
        service = new VOService();
        service.setKey(51);
        sub = new VOSubscription();
        sub.setKey(12);
        Mockito.when(ds.getCurrentUser()).thenReturn(user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Service_InvalidTriggerType() throws Exception {
        TriggerProcessIdentifiers.createDeactivateService(ds,
                TriggerType.MODIFY_SUBSCRIPTION, service);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Service_NullDataService() throws Exception {
        TriggerProcessIdentifiers.createDeactivateService(null,
                TriggerType.ACTIVATE_SERVICE, service);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Service_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createDeactivateService(ds, null, service);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Service_NullService() throws Exception {
        TriggerProcessIdentifiers.createDeactivateService(ds,
                TriggerType.ACTIVATE_SERVICE, (VOService) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Service_ServiceKeyNotSet() throws Exception {
        TriggerProcessIdentifiers.createDeactivateService(ds,
                TriggerType.ACTIVATE_SERVICE, new VOService());
    }

    @Test
    public void getIdentifiers_Service_Activate() throws Exception {
        VOService service = new VOService();
        service.setKey(15);
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createDeactivateService(ds, TriggerType.ACTIVATE_SERVICE,
                        service);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result, TriggerProcessIdentifierName.SERVICE_KEY,
                "15");
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test
    public void getIdentifiers_Service_Deactivate() throws Exception {
        VOService service = new VOService();
        service.setKey(25);
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createDeactivateService(ds, TriggerType.ACTIVATE_SERVICE,
                        service);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result, TriggerProcessIdentifierName.SERVICE_KEY,
                "25");
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    /**
     * Validates that the only value existing for the given name among the
     * available identifiers has the specified value.
     * 
     * @param identifiers
     *            The identifiers.
     * @param name
     *            The name to look for.
     * @param expectedValue
     *            The expected value to check against.
     */
    private void validateSingleParam(
            List<TriggerProcessIdentifier> identifiers,
            TriggerProcessIdentifierName name, String expectedValue) {
        TriggerProcess tp = new TriggerProcess();
        tp.setTriggerProcessIdentifiers(identifiers);
        List<TriggerProcessIdentifier> ids = tp
                .getIdentifierValuesForName(name);
        assertEquals(1, ids.size());
        assertEquals(expectedValue, ids.get(0).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Subscribing_InvalidTriggerType()
            throws Exception {
        TriggerProcessIdentifiers.createUnsubscribeFromService(ds,
                TriggerType.MODIFY_SUBSCRIPTION, "subId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Subscribing_NullDataService() throws Exception {
        TriggerProcessIdentifiers.createUnsubscribeFromService(null,
                TriggerType.SUBSCRIBE_TO_SERVICE, "subId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Subscribing_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createUnsubscribeFromService(ds, null,
                "subId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Subscribing_NullSubId() throws Exception {
        TriggerProcessIdentifiers.createUnsubscribeFromService(ds,
                TriggerType.SUBSCRIBE_TO_SERVICE, (String) null);
    }

    @Test
    public void getIdentifiers_Subscribing_SubscribeToService()
            throws Exception {
        String subId = "subId1";
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createUnsubscribeFromService(ds,
                        TriggerType.SUBSCRIBE_TO_SERVICE, subId);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, subId);
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test
    public void getIdentifiers_Subscribing_UnsubscribeFromService()
            throws Exception {
        String subId = "subId2";
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createUnsubscribeFromService(ds,
                        TriggerType.UNSUBSCRIBE_FROM_SERVICE, subId);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, subId);
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ChangeSub_InvalidTriggerType() throws Exception {
        TriggerProcessIdentifiers.createUpgradeSubscription(ds,
                TriggerType.ADD_REVOKE_USER, sub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ChangeSub_NullDataService() throws Exception {
        TriggerProcessIdentifiers.createUpgradeSubscription(null,
                TriggerType.MODIFY_SUBSCRIPTION, sub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ChangeSub_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createUpgradeSubscription(ds, null, sub);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ChangeSub_NullSub() throws Exception {
        TriggerProcessIdentifiers.createUpgradeSubscription(ds,
                TriggerType.MODIFY_SUBSCRIPTION, (VOSubscription) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ChangeSub_ServiceKeyNotSet() throws Exception {
        TriggerProcessIdentifiers.createUpgradeSubscription(ds,
                TriggerType.MODIFY_SUBSCRIPTION, new VOSubscription());
    }

    @Test
    public void getIdentifiers_ChangeSub_ModifySub() throws Exception {
        VOSubscription sub = new VOSubscription();
        sub.setKey(985);
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createUpgradeSubscription(ds, TriggerType.MODIFY_SUBSCRIPTION,
                        sub);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY,
                String.valueOf(sub.getKey()));
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test
    public void getIdentifiers_ChangeSub_UpgradeSub() throws Exception {
        VOSubscription sub = new VOSubscription();
        sub.setKey(756);
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createUpgradeSubscription(ds,
                        TriggerType.UPGRADE_SUBSCRIPTION, sub);
        assertNotNull(result);
        assertEquals(2, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY,
                String.valueOf(sub.getKey()));
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Register_InvalidTriggerType() throws Exception {
        TriggerProcessIdentifiers.createRegisterCustomerForSupplier(ds,
                TriggerType.ADD_REVOKE_USER, new VOUserDetails());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Register_NullDataService() throws Exception {
        TriggerProcessIdentifiers
                .createRegisterCustomerForSupplier(null,
                        TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        new VOUserDetails());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Register_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createRegisterCustomerForSupplier(ds, null,
                new VOUserDetails());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Register_NullUser() throws Exception {
        TriggerProcessIdentifiers.createRegisterCustomerForSupplier(ds,
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                (VOUserDetails) null);
    }

    @Test
    public void getIdentifiers_Register_RegisterCustomer() throws Exception {
        VOUserDetails user = new VOUserDetails();
        user.setUserId("userId");
        user.setEMail("user@server.tv");
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createRegisterCustomerForSupplier(ds,
                        TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER, user);
        assertNotNull(result);
        assertEquals(3, result.size());
        validateSingleParam(result, TriggerProcessIdentifierName.USER_ID,
                user.getUserId());
        validateSingleParam(result, TriggerProcessIdentifierName.USER_EMAIL,
                user.getEMail());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_InvalidTriggerType() throws Exception {
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.SUBSCRIBE_TO_SERVICE, "subId",
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_NullDataService() throws Exception {
        TriggerProcessIdentifiers.createAddRevokeUser(null,
                TriggerType.ADD_REVOKE_USER, "subId",
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createAddRevokeUser(ds, null, "subId",
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_NullSubId() throws Exception {
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.ADD_REVOKE_USER, null,
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test
    public void getIdentifiers_ARU_NullUsageLicenses() throws Exception {
        List<TriggerProcessIdentifier> list = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        null, new ArrayList<VOUser>());
        assertNotNull(list);
        // org and subscription
        assertEquals(2, list.size());
    }

    @Test
    public void getIdentifiers_ARU_NullUsers() throws Exception {
        List<TriggerProcessIdentifier> list = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        new ArrayList<VOUsageLicense>(), null);
        assertNotNull(list);
        // org and subscription
        assertEquals(2, list.size());
    }

    @Test
    public void getIdentifiers_ARU_UsageLicenses0Key() throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        VOUsageLicense license = new VOUsageLicense();
        VOUser user = new VOUser();
        user.setKey(5);
        license.setUser(user);
        usersToAdd.add(license);
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.ADD_REVOKE_USER, "subId", usersToAdd,
                new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_UsageLicensesNoUser() throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        VOUsageLicense license = new VOUsageLicense();
        license.setKey(1);
        usersToAdd.add(license);
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.ADD_REVOKE_USER, "subId", usersToAdd,
                new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_UsageLicensesInvalidUserKey()
            throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        VOUsageLicense license = new VOUsageLicense();
        license.setUser(new VOUser());
        usersToAdd.add(license);
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.ADD_REVOKE_USER, "subId", usersToAdd,
                new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_ARU_UsersToRemoveInvalidUserKey()
            throws Exception {
        ArrayList<VOUser> usersToRemove = new ArrayList<VOUser>();
        usersToRemove.add(new VOUser());
        TriggerProcessIdentifiers.createAddRevokeUser(ds,
                TriggerType.ADD_REVOKE_USER, "subId",
                new ArrayList<VOUsageLicense>(), usersToRemove);
    }

    @Test
    public void getIdentifiers_ARU_NoDeletions() throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        usersToAdd.add(createLicense(1, 5, "admin1"));
        usersToAdd.add(createLicense(2, 9, "user1"));
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        usersToAdd, new ArrayList<VOUser>());
        assertNotNull(result);
        assertEquals(4, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        Set<String> idsForUsersToAdd = getValuesForNonUniqueParams(result,
                TriggerProcessIdentifierName.USER_TO_ADD);
        assertEquals(2, idsForUsersToAdd.size());
        assertTrue(idsForUsersToAdd.contains("admin1"));
        assertTrue(idsForUsersToAdd.contains("user1"));
    }

    @Test
    public void getIdentifiers_ARU_NoDeletionsDuplicateUserId()
            throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        usersToAdd.add(createLicense(1, 5, "admin1"));
        usersToAdd.add(createLicense(2, 9, "user1"));
        usersToAdd.add(createLicense(3, 10, "user1"));
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        usersToAdd, new ArrayList<VOUser>());
        assertNotNull(result);
        assertEquals(4, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        Set<String> idsForUsersToRevoke = getValuesForNonUniqueParams(result,
                TriggerProcessIdentifierName.USER_TO_ADD);
        assertEquals(2, idsForUsersToRevoke.size());
        assertTrue(idsForUsersToRevoke.contains("admin1"));
        assertTrue(idsForUsersToRevoke.contains("user1"));
    }

    @Test
    public void getIdentifiers_ARU_NoAdditions() throws Exception {
        ArrayList<VOUser> usersToRemove = new ArrayList<VOUser>();
        usersToRemove.add(createUser(5, "admin1"));
        usersToRemove.add(createUser(9, "user1"));
        usersToRemove.add(createUser(11, "user2"));
        usersToRemove.add(createUser(12, "user2"));
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        new ArrayList<VOUsageLicense>(), usersToRemove);
        assertNotNull(result);
        assertEquals(5, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        Set<String> idsForUsersToAdd = getValuesForNonUniqueParams(result,
                TriggerProcessIdentifierName.USER_TO_REVOKE);
        assertEquals(3, idsForUsersToAdd.size());
        assertTrue(idsForUsersToAdd.contains("admin1"));
        assertTrue(idsForUsersToAdd.contains("user1"));
        assertTrue(idsForUsersToAdd.contains("user2"));
    }

    @Test
    public void getIdentifiers_ARU_AdditionsAndRevocations() throws Exception {
        ArrayList<VOUsageLicense> usersToAdd = new ArrayList<VOUsageLicense>();
        usersToAdd.add(createLicense(1, 5, "toAdd1"));
        usersToAdd.add(createLicense(2, 9, "toAdd2"));
        usersToAdd.add(createLicense(3, 10, "toAdd5"));
        ArrayList<VOUser> usersToRevoke = new ArrayList<VOUser>();
        usersToRevoke.add(createUser(6, "admin1"));
        usersToRevoke.add(createUser(7, "user1"));
        usersToRevoke.add(createUser(12, "user2"));
        usersToRevoke.add(createUser(17, "user2"));
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createAddRevokeUser(ds, TriggerType.ADD_REVOKE_USER, "subId",
                        usersToAdd, usersToRevoke);
        assertNotNull(result);
        assertEquals(8, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
        validateSingleParam(result,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        Set<String> idsForUsersToAdd = getValuesForNonUniqueParams(result,
                TriggerProcessIdentifierName.USER_TO_ADD);
        assertEquals(3, idsForUsersToAdd.size());
        assertTrue(idsForUsersToAdd.contains("toAdd1"));
        assertTrue(idsForUsersToAdd.contains("toAdd2"));
        assertTrue(idsForUsersToAdd.contains("toAdd5"));
        Set<String> idsForUsersToRevoke = getValuesForNonUniqueParams(result,
                TriggerProcessIdentifierName.USER_TO_REVOKE);
        assertEquals(3, idsForUsersToRevoke.size());
        assertTrue(idsForUsersToRevoke.contains("admin1"));
        assertTrue(idsForUsersToRevoke.contains("user1"));
        assertTrue(idsForUsersToRevoke.contains("user2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Payment_InvalidTriggerType() throws Exception {
        TriggerProcessIdentifiers.createSavePaymentConfiguration(ds,
                TriggerType.MODIFY_SUBSCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Payment_NullDataService() throws Exception {
        TriggerProcessIdentifiers.createSavePaymentConfiguration(null,
                TriggerType.SAVE_PAYMENT_CONFIGURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdentifiers_Payment_NullTriggerType() throws Exception {
        TriggerProcessIdentifiers.createSavePaymentConfiguration(ds, null);
    }

    @Test
    public void getIdentifiers_Payment_Save() throws Exception {
        List<TriggerProcessIdentifier> result = TriggerProcessIdentifiers
                .createSavePaymentConfiguration(ds,
                        TriggerType.SAVE_PAYMENT_CONFIGURATION);
        assertNotNull(result);
        assertEquals(1, result.size());
        validateSingleParam(result,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ORG_KEY));
    }

    /**
     * Retrieves the trigger process identifiers for the given name and stores
     * their values in a set.
     * 
     * @param identifiers
     *            The identifiers.
     * @param name
     *            The identifier name.
     * @return The set of values found.
     */
    private Set<String> getValuesForNonUniqueParams(
            List<TriggerProcessIdentifier> identifiers,
            TriggerProcessIdentifierName name) {
        TriggerProcess tp = new TriggerProcess();
        tp.setTriggerProcessIdentifiers(identifiers);
        List<TriggerProcessIdentifier> ids = tp
                .getIdentifierValuesForName(name);
        Set<String> result = new HashSet<String>();
        for (TriggerProcessIdentifier id : ids) {
            result.add(id.getValue());
        }
        return result;
    }

    /**
     * Creates a usage license object corresponding to the specified settings.
     * 
     * @param licenseKey
     *            The license key.
     * @param userKey
     *            The user key.
     * @param userId
     *            The identifier of the user.
     * @return The license.
     */
    private VOUsageLicense createLicense(long licenseKey, long userKey,
            String userId) {
        VOUsageLicense license = new VOUsageLicense();
        license.setKey(licenseKey);
        VOUser user = createUser(userKey, userId);
        license.setUser(user);
        return license;
    }

    /**
     * Creates a user object corresponding to the specified settings.
     * 
     * @param userKey
     *            The user key.
     * @param userId
     *            The identifier of the user.
     * @return The user.
     */
    private VOUser createUser(long userKey, String userId) {
        VOUser user = new VOUser();
        user.setUserId(userId);
        user.setKey(userKey);
        return user;
    }
}
