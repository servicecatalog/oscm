/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.TriggerDefinitions;
import org.oscm.test.data.TriggerProcesses;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;

/**
 * Unit tests for <code>TriggerProcessValidator</code>
 * 
 * @author barzu
 */
public class TriggerProcessValidatorIT extends EJBTestBase {

    private static final long SERVICE_KEY = 100L;
    private static final String SUBSCRIPTION_ID = "sub1";
    private static final String USER_1 = "user1";
    private static final String USER_2 = "user2";
    private static final String USER_3 = "user3";
    private static final String USER_4 = "user4";
    private static final String USER_ID = "admin";
    private static final String USER_EMAIL = "a@b.com";
    private static final long SUBSCRIPTION_KEY = 1000L;

    private Organization supplier;
    private PlatformUser user;

    private DataService ds;
    private TriggerProcessValidator validator;

    /**
     * @see org.oscm.test.EJBTestBase#setup(org.oscm.test.ejb.TestContainer)
     */
    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        validator = new TriggerProcessValidator(ds);
    }

    private void initOrganization() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                user = PlatformUsers.createAdmin(ds, "triggerAdmin", supplier);
                container.login(String.valueOf(user.getKey()),
                        ROLE_ORGANIZATION_ADMIN);
                return null;
            }
        });
    }

    private TriggerProcess initTriggerProcess(final TriggerType triggerType,
            final boolean suspending, final TriggerProcessStatus status)
            throws Exception {
        initOrganization();
        return initTriggerProcess(triggerType, suspending, status, supplier,
                user);
    }

    private TriggerProcess initTriggerProcess(final TriggerType triggerType,
            final boolean suspending, final TriggerProcessStatus status,
            final Organization org, final PlatformUser user) throws Exception {
        return runTX(new Callable<TriggerProcess>() {
            @Override
            public TriggerProcess call() throws Exception {
                TriggerDefinition triggerDefinition = TriggerDefinitions
                        .createTriggerDefinition(ds, org, triggerType,
                                suspending);
                return TriggerProcesses.createTriggerProcess(ds, user,
                        triggerDefinition, status);
            }
        });
    }

    // ACTIVATE_SERVICE, DEACTIVATE_SERVICE

    @Test(expected = IllegalArgumentException.class)
    public void isActivateDeactivateServicePending_NullService()
            throws Exception {
        validator.isActivateOrDeactivateServicePending(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isActivateDeactivateServicePending_0ServiceKey()
            throws Exception {
        validator.isActivateOrDeactivateServicePending(new VOService());
    }

    private void createIdentifiers_ActivateDeactivateService(
            final TriggerProcess triggerProcess) throws Exception {
        createIdentifiers_ActivateDeactivateService(triggerProcess,
                SERVICE_KEY);
    }

    private void createIdentifiers_ActivateDeactivateService(
            final TriggerProcess triggerProcess, final long serviceKey)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.SERVICE_KEY,
                        String.valueOf(serviceKey));
                return null;
            }
        });
    }

    private Boolean isActivateOrDeactivateServicePending(final long serviceKey)
            throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                VOService service = new VOService();
                service.setKey(serviceKey);
                return Boolean.valueOf(validator
                        .isActivateOrDeactivateServicePending(service));
            }
        });
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_SuspendingWaiting()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SERVICE_KEY + 100L);
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_SuspendingInitial()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_SuspendingDone()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.REJECTED));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, false,
                        TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_NonSuspendingDone()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, false,
                        TriggerProcessStatus.ERROR));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_DifferentOrganization()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        initOrganization();
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Activate_DifferentService()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.ACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY + 1L));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_SuspendingWaiting()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_SuspendingInitial()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, true,
                        TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_SuspendingDone()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, true,
                        TriggerProcessStatus.APPROVED));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, false,
                        TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_NonSuspendingDone()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, false,
                        TriggerProcessStatus.FAILED));
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_DifferentOrganization()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        initOrganization();
        assertEquals(Boolean.TRUE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    @Test
    public void isActivateDeactivateServicePending_Deactivate_DifferentService()
            throws Exception {
        createIdentifiers_ActivateDeactivateService(
                initTriggerProcess(TriggerType.DEACTIVATE_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SERVICE_KEY + 1L);
        assertEquals(Boolean.FALSE,
                isActivateOrDeactivateServicePending(SERVICE_KEY));
    }

    // ADD_REVOKE_USER

    @Test(expected = IllegalArgumentException.class)
    public void getPendingAddRevokeUsers_NullSubscriptionId() throws Exception {
        validator.getPendingAddRevokeUsers(null,
                new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());
    }

    @Test
    public void getPendingAddRevokeUsers_NullUsersToAdd() throws Exception {
        assertTrue(validator
                .getPendingAddRevokeUsers("100", null, new ArrayList<VOUser>())
                .isEmpty());
    }

    @Test
    public void getPendingAddRevokeUsers_NullUsersToRevoke() throws Exception {
        assertTrue(
                validator
                        .getPendingAddRevokeUsers("100",
                                new ArrayList<VOUsageLicense>(), null)
                        .isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPendingAddRevokeUsers_NullUserToAdd() throws Exception {
        validator.getPendingAddRevokeUsers("100",
                Collections.singletonList((VOUsageLicense) null),
                new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPendingAddRevokeUsers_NullUserToRevoke() throws Exception {
        validator.getPendingAddRevokeUsers("100",
                new ArrayList<VOUsageLicense>(),
                Collections.singletonList((VOUser) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPendingAddRevokeUsers_0UserToAddKey() throws Exception {
        VOUsageLicense license = new VOUsageLicense();
        license.setUser(new VOUser());
        validator.getPendingAddRevokeUsers("100",
                Collections.singletonList(license), new ArrayList<VOUser>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPendingAddRevokeUsers_0UserToRevokeKey() throws Exception {
        validator.getPendingAddRevokeUsers("100",
                new ArrayList<VOUsageLicense>(),
                Collections.singletonList(new VOUser()));
    }

    private void createIdentifiers_AddRevokeUsers(
            final TriggerProcess triggerProcess) throws Exception {
        createIdentifiers_AddRevokeUsers(triggerProcess,
                new String[] { USER_1, USER_2 },
                new String[] { USER_3, USER_4 });
    }

    private void createIdentifiers_AddRevokeUsers(
            final TriggerProcess triggerProcess, final String[] usersToAdd,
            final String[] usersToRevoke) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                        SUBSCRIPTION_ID);
                for (int i = 0; i < usersToAdd.length; i++) {
                    tp.addTriggerProcessIdentifier(
                            TriggerProcessIdentifierName.USER_TO_ADD,
                            usersToAdd[i]);
                }
                for (int i = 0; i < usersToRevoke.length; i++) {
                    tp.addTriggerProcessIdentifier(
                            TriggerProcessIdentifierName.USER_TO_REVOKE,
                            usersToRevoke[i]);
                }
                return null;
            }
        });
    }

    private List<TriggerProcessIdentifier> getPendingAddRevokeUsers(
            final String subscriptionId, final String[] usersToAdd,
            final String... usersToRevoke) throws Exception {
        return runTX(new Callable<List<TriggerProcessIdentifier>>() {
            @Override
            public List<TriggerProcessIdentifier> call() throws Exception {
                List<VOUsageLicense> usersToAddList = new ArrayList<>();
                for (int i = 0; i < usersToAdd.length; i++) {
                    VOUser user = new VOUser();
                    user.setKey(1000L + i);
                    user.setUserId(usersToAdd[i]);
                    VOUsageLicense license = new VOUsageLicense();
                    license.setUser(user);
                    usersToAddList.add(license);
                }
                List<VOUser> usersToRevokeList = new ArrayList<>();
                for (int i = 0; i < usersToRevoke.length; i++) {
                    VOUser user = new VOUser();
                    user.setKey(1000L + i);
                    user.setUserId(usersToRevoke[i]);
                    usersToRevokeList.add(user);
                }
                return validator.getPendingAddRevokeUsers(subscriptionId,
                        usersToAddList, usersToRevokeList);
            }
        });
    }

    @Test
    public void getPendingAddRevokeUsers_DifferentOrg() throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        initOrganization();
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] {}, USER_3).size());
    }

    @Test
    public void getPendingAddRevokeUsers_DifferentSubscription()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(0,
                getPendingAddRevokeUsers("sub2", new String[] { USER_1 })
                        .size());
    }

    @Test
    public void getPendingAddRevokeUsers_DifferentUser() throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] { "root" }).size());
    }

    @Test
    public void getPendingAddRevokeUsers_AddUserSuspendingWaiting()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL, supplier,
                        user),
                new String[] { "root" }, new String[] { USER_4 });
        List<TriggerProcessIdentifier> identifiers = getPendingAddRevokeUsers(
                SUBSCRIPTION_ID, new String[] { USER_1 });
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_TO_ADD,
                identifiers.get(0).getName());
        assertEquals(USER_1, identifiers.get(0).getValue());
    }

    @Test
    public void getPendingAddRevokeUsers_AddUser_SuspendingInitial()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.INITIAL));
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, false,
                        TriggerProcessStatus.INITIAL, supplier, user),
                new String[] { USER_1 }, new String[] { "root" });
        List<TriggerProcessIdentifier> identifiers = getPendingAddRevokeUsers(
                SUBSCRIPTION_ID, new String[] { USER_1 });
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_TO_ADD,
                identifiers.get(0).getName());
        assertEquals(USER_1, identifiers.get(0).getValue());
    }

    @Test
    public void getPendingAddRevokeUsers_AddUser_SuspendingDone()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.APPROVED));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] { USER_1 }).size());
    }

    @Test
    public void getPendingAddRevokeUsers_AddUser_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, false,
                        TriggerProcessStatus.INITIAL));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] { USER_1 }).size());
    }

    @Test
    public void getPendingAddRevokeUsers_AddUser_NonSuspendingDone()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, false,
                        TriggerProcessStatus.FAILED));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] { USER_1 }).size());
    }

    @Test
    public void getPendingAddRevokeUsers_RevokeUser_SuspendingWaiting()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        List<TriggerProcessIdentifier> identifiers = getPendingAddRevokeUsers(
                SUBSCRIPTION_ID, new String[] {}, USER_3);
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_TO_REVOKE,
                identifiers.get(0).getName());
        assertEquals(USER_3, identifiers.get(0).getValue());
    }

    @Test
    public void getPendingAddRevokeUsers_RevokeUser_SuspendingInitial()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.INITIAL));
        List<TriggerProcessIdentifier> identifiers = getPendingAddRevokeUsers(
                SUBSCRIPTION_ID, new String[] {}, USER_3);
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_TO_REVOKE,
                identifiers.get(0).getName());
        assertEquals(USER_3, identifiers.get(0).getValue());
    }

    @Test
    public void getPendingAddRevokeUsers_RevokeUser_SuspendingDone()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.APPROVED));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] {}, USER_3).size());
    }

    @Test
    public void getPendingAddRevokeUsers_RevokeUser_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, false,
                        TriggerProcessStatus.INITIAL));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] {}, USER_3).size());
    }

    @Test
    public void getPendingAddRevokeUsers_RevokeUser_NonSuspendingDone()
            throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, false,
                        TriggerProcessStatus.NOTIFIED));
        assertEquals(0, getPendingAddRevokeUsers(SUBSCRIPTION_ID,
                new String[] {}, USER_3).size());
    }

    @Test
    public void getPendingAddRevokeUsers_EmptyUsers() throws Exception {
        createIdentifiers_AddRevokeUsers(
                initTriggerProcess(TriggerType.ADD_REVOKE_USER, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        List<TriggerProcessIdentifier> identifiers = getPendingAddRevokeUsers(
                SUBSCRIPTION_ID, new String[] {});
        assertEquals(0, identifiers.size());
    }

    // REGISTER_CUSTOMER_FOR_SUPPLIER

    @Test(expected = IllegalArgumentException.class)
    public void isRegisterCustomerForSupplierPending_NullUser()
            throws Exception {
        validator.isRegisterCustomerForSupplierPending(null);
    }

    private void createIdentifiers_RegisterCustomerForSupplier(
            final TriggerProcess triggerProcess) throws Exception {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID);
        user.setEMail(USER_EMAIL);
        createIdentifiers_RegisterCustomerForSupplier(triggerProcess, user);
    }

    private void createIdentifiers_RegisterCustomerForSupplier(
            final TriggerProcess triggerProcess, final VOUserDetails user)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.USER_ID, user.getUserId());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.USER_EMAIL,
                        user.getEMail());
                return null;
            }
        });
    }

    private Boolean isRegisterCustomerForSupplierPending(final String userId,
            final String mail) throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                VOUserDetails user = new VOUserDetails(100L, 0);
                user.setUserId(userId);
                user.setEMail(mail);
                return Boolean.valueOf(
                        validator.isRegisterCustomerForSupplierPending(user));
            }
        });
    }

    @Test
    public void isRegisterCustomerForSupplierPending_SuspendingWaiting()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.WAITING_FOR_APPROVAL));
        VOUserDetails user2 = new VOUserDetails();
        user2.setUserId("root");
        user2.setEMail("x@y.de");
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.WAITING_FOR_APPROVAL,
                        supplier, user),
                user2);
        assertEquals(Boolean.TRUE,
                isRegisterCustomerForSupplierPending(USER_ID, USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_SuspendingInitial()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.TRUE,
                isRegisterCustomerForSupplierPending(USER_ID, USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_SuspendingDone()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.CANCELLED));
        assertEquals(Boolean.FALSE,
                isRegisterCustomerForSupplierPending(USER_ID, USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        false, TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.FALSE,
                isRegisterCustomerForSupplierPending(USER_ID, USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_NonSuspendingDone()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        false, TriggerProcessStatus.NOTIFIED));
        assertEquals(Boolean.FALSE,
                isRegisterCustomerForSupplierPending(USER_ID, USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_DifferentUserId()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.TRUE, isRegisterCustomerForSupplierPending(
                USER_ID + "_222", USER_EMAIL));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_DifferentEmail()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.TRUE, isRegisterCustomerForSupplierPending(USER_ID,
                USER_EMAIL + ".de"));
    }

    @Test
    public void isRegisterCustomerForSupplierPending_DifferentUserAndEmail()
            throws Exception {
        createIdentifiers_RegisterCustomerForSupplier(
                initTriggerProcess(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                        true, TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.FALSE, isRegisterCustomerForSupplierPending(
                USER_ID + "_222", USER_EMAIL + ".de"));
    }

    // SAVE_PAYMENT_CONFIGURATION

    private void createIdentifiers_SavePaymentConfiguration(
            final TriggerProcess triggerProcess) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                return null;
            }
        });
    }

    private Boolean isSavePaymentConfigurationPending() throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean
                        .valueOf(validator.isSavePaymentConfigurationPending());
            }
        });
    }

    @Test
    public void isSavePaymentConfigurationPending_NoData() throws Exception {
        initOrganization();
        assertEquals(Boolean.FALSE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_SuspendingWaiting()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.TRUE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_SuspendingInitial()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.TRUE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_SuspendingDone()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.APPROVED));
        assertEquals(Boolean.FALSE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        false, TriggerProcessStatus.INITIAL));
        assertEquals(Boolean.FALSE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_NonSuspendingDone()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        false, TriggerProcessStatus.NOTIFIED));
        assertEquals(Boolean.FALSE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_DifferentOrg()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        initOrganization();
        assertEquals(Boolean.FALSE, isSavePaymentConfigurationPending());
    }

    @Test
    public void isSavePaymentConfigurationPending_MultipleProcesses()
            throws Exception {
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        createIdentifiers_SavePaymentConfiguration(
                initTriggerProcess(TriggerType.SAVE_PAYMENT_CONFIGURATION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL));
        assertEquals(Boolean.TRUE, isSavePaymentConfigurationPending());
    }

    // MODIFY_SUBSCRIPTION, UPGRADE_SUBSCRIPTION

    @Test(expected = IllegalArgumentException.class)
    public void isModifyOrUpgradeSubscriptionPending_NullSubscription()
            throws Exception {
        validator.isModifyOrUpgradeSubscriptionPending(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isModifyOrUpgradeSubscriptionPending_0SubscriptionKey()
            throws Exception {
        validator.isModifyOrUpgradeSubscriptionPending(new VOSubscription());
    }

    private void createIdentifiers_ModifyOrUpgradeSubscription(
            final TriggerProcess triggerProcess, final long subscriptionKey)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.SUBSCRIPTION_KEY,
                        String.valueOf(subscriptionKey));
                return null;
            }
        });
    }

    private Boolean isModifyOrUpgradeSubscriptionPending(
            final long subscriptionKey) throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                VOSubscription subscription = new VOSubscription();
                subscription.setKey(subscriptionKey);
                return Boolean.valueOf(validator
                        .isModifyOrUpgradeSubscriptionPending(subscription));
            }
        });
    }

    private Boolean isModifySubscriptionPending(final long subscriptionKey)
            throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(
                        validator.isModifySubscriptionPending(subscriptionKey));
            }
        });
    }

    private Boolean isUpgradeSubscriptionPending(final long subscriptionKey)
            throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(validator
                        .isUpgradeSubscriptionPending(subscriptionKey));
            }
        });
    }

    @Test
    public void isModifySubscriptionPending_NoData() throws Exception {
        // given
        initOrganization();

        // when
        Boolean result = isModifySubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void isModifySubscriptionPending_Modify_SuspendingWaiting()
            throws Exception {
        // given
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY);

        // when
        Boolean result = isModifySubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isModifySubscriptionPending_Modify_SuspendingInitial()
            throws Exception {
        // given
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        // when
        Boolean result = isModifySubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isUpgradeSubscriptionPending_Upgrade_SuspendingWaiting()
            throws Exception {
        // given
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY);

        // when
        Boolean result = isUpgradeSubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isUpgradeSubscriptionPending_Upgrade_SuspendingInitial()
            throws Exception {
        // given
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        // when
        Boolean result = isUpgradeSubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isUpgradeSubscriptionPending_Upgrade_SuspendingDone()
            throws Exception {
        // given
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.FAILED),
                SUBSCRIPTION_KEY);
        // when
        Boolean result = isUpgradeSubscriptionPending(SUBSCRIPTION_KEY);

        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_NoData() throws Exception {
        initOrganization();
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_SuspendingWaiting()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.TRUE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_SuspendingInitial()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.TRUE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_SuspendingDone()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.FAILED),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_NonSuspendingDone()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, false,
                        TriggerProcessStatus.ERROR),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Modify_DifferentSubscription()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, false,
                        TriggerProcessStatus.ERROR),
                SUBSCRIPTION_KEY + 100);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_SuspendingWaiting()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.TRUE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_SuspendingInitial()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.TRUE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_SuspendingDone()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.FAILED),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_NonSuspendingDone()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, false,
                        TriggerProcessStatus.ERROR),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_Upgrade_DifferentSubscription()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, false,
                        TriggerProcessStatus.ERROR),
                SUBSCRIPTION_KEY + 100);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_MultipleSuspending()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY);
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY + 10L);
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_KEY + 100L);
        assertEquals(Boolean.TRUE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    @Test
    public void isModifyOrUpgradeSubscriptionPending_MultipleNonSuspending()
            throws Exception {
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_KEY);
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.MODIFY_SUBSCRIPTION, false,
                        TriggerProcessStatus.INITIAL, supplier, user),
                SUBSCRIPTION_KEY);
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, false,
                        TriggerProcessStatus.NOTIFIED, supplier, user),
                SUBSCRIPTION_KEY);
        createIdentifiers_ModifyOrUpgradeSubscription(
                initTriggerProcess(TriggerType.UPGRADE_SUBSCRIPTION, false,
                        TriggerProcessStatus.NOTIFIED),
                SUBSCRIPTION_KEY);
        assertEquals(Boolean.FALSE,
                isModifyOrUpgradeSubscriptionPending(SUBSCRIPTION_KEY));
    }

    // SUBSCRIBE_TO_SERVICE, UNSUBSCRIBE_FROM_SERVICE

    @Test(expected = IllegalArgumentException.class)
    public void isSubscribeOrUnsubscribeServicePending_NullSubscription()
            throws Exception {
        validator.isSubscribeOrUnsubscribeServicePending(null);
    }

    private void createIdentifiers_SubscribeOrUnsubscribeService(
            final TriggerProcess triggerProcess, final String subscriptionId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcess.getKey());
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                        subscriptionId);
                return null;
            }
        });
    }

    private Boolean isSubscribeOrUnsubscribeServicePending(
            final String subscriptionId) throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(
                        validator.isSubscribeOrUnsubscribeServicePending(
                                subscriptionId));
            }
        });
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_NoData()
            throws Exception {
        initOrganization();
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_SuspendingWaiting()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.TRUE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_SuspendingInitial()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.TRUE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_SuspendingDone()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.APPROVED),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_NonSuspendingDone()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_DifferentOrganization()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        initOrganization();
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Subscribe_DifferentSubscription()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID + "_basic");
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_SuspendingWaiting()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.TRUE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_SuspendingInitial()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, true,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.TRUE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_SuspendingDone()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, true,
                        TriggerProcessStatus.APPROVED),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_NonSuspendingInitial()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_NonSuspendingDone()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_DifferentOrganization()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        initOrganization();
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_Unsubscribe_DifferentSubscription()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID + "_basic");
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_MultipleSuspending()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        // same subscription, other organization
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, true,
                        TriggerProcessStatus.WAITING_FOR_APPROVAL),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.TRUE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    @Test
    public void isSubscribeOrUnsubscribeServicePending_MultipleNonSuspending()
            throws Exception {
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED, supplier, user),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.INITIAL, supplier, user),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED, supplier, user),
                SUBSCRIPTION_ID);
        // different organization
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.INITIAL),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.SUBSCRIBE_TO_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED, supplier, user),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.INITIAL, supplier, user),
                SUBSCRIPTION_ID);
        createIdentifiers_SubscribeOrUnsubscribeService(
                initTriggerProcess(TriggerType.UNSUBSCRIBE_FROM_SERVICE, false,
                        TriggerProcessStatus.NOTIFIED, supplier, user),
                SUBSCRIPTION_ID);
        assertEquals(Boolean.FALSE,
                isSubscribeOrUnsubscribeServicePending(SUBSCRIPTION_ID));
    }

    private Boolean isRegisterOwnUserPendingTX(final String userId)
            throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean
                        .valueOf(validator.isRegisterOwnUserPending(userId));
            }
        });
    }

    private void createIdentifiers_RegisterOwnUser(final long triggerProcessKey,
            final String userId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerProcess tp = ds.getReference(TriggerProcess.class,
                        triggerProcessKey);
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.ORGANIZATION_KEY,
                        String.valueOf(supplier.getKey()));
                tp.addTriggerProcessIdentifier(
                        TriggerProcessIdentifierName.USER_ID, userId);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRegisterOwnUserPending_userIdNull() throws Exception {
        isRegisterOwnUserPendingTX(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRegisterOwnUserPending_userIdEmpty() throws Exception {
        isRegisterOwnUserPendingTX("");
    }

    @Test
    public void isRegisterOwnUserPending_userIdNoDbEntry() throws Exception {
        // given
        initOrganization();
        // when
        Boolean result = isRegisterOwnUserPendingTX("userIdNoDbEntry");
        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void isRegisterOwnUserPending_suspendingWaiting() throws Exception {
        // given
        TriggerProcess triggerProcess = initTriggerProcess(
                TriggerType.REGISTER_OWN_USER, true,
                TriggerProcessStatus.WAITING_FOR_APPROVAL);
        createIdentifiers_RegisterOwnUser(triggerProcess.getKey(),
                user.getUserId());
        // when
        Boolean result = isRegisterOwnUserPendingTX(user.getUserId());
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isRegisterOwnUserPending_suspendingInital() throws Exception {
        // given
        TriggerProcess triggerProcess = initTriggerProcess(
                TriggerType.REGISTER_OWN_USER, true,
                TriggerProcessStatus.INITIAL);
        createIdentifiers_RegisterOwnUser(triggerProcess.getKey(),
                user.getUserId());
        // when
        Boolean result = isRegisterOwnUserPendingTX(user.getUserId());
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isRegisterOwnUserPending_suspendingApproved() throws Exception {
        // given
        TriggerProcess triggerProcess = initTriggerProcess(
                TriggerType.REGISTER_OWN_USER, true,
                TriggerProcessStatus.APPROVED);
        createIdentifiers_RegisterOwnUser(triggerProcess.getKey(),
                user.getUserId());
        // when
        Boolean result = isRegisterOwnUserPendingTX(user.getUserId());
        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void isRegisterOwnUserPending_nonSuspendingInitial()
            throws Exception {
        // given
        TriggerProcess triggerProcess = initTriggerProcess(
                TriggerType.REGISTER_OWN_USER, false,
                TriggerProcessStatus.INITIAL);
        createIdentifiers_RegisterOwnUser(triggerProcess.getKey(),
                user.getUserId());
        // when
        Boolean result = isRegisterOwnUserPendingTX(user.getUserId());
        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void isRegisterOwnUserPending_nonSuspendingApproved()
            throws Exception {
        // given
        TriggerProcess triggerProcess = initTriggerProcess(
                TriggerType.REGISTER_OWN_USER, false,
                TriggerProcessStatus.APPROVED);
        createIdentifiers_RegisterOwnUser(triggerProcess.getKey(),
                user.getUserId());
        // when
        Boolean result = isRegisterOwnUserPendingTX(user.getUserId());
        // then
        assertEquals(Boolean.FALSE, result);
    }
}
