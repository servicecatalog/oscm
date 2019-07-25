/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 11.10.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.samlsp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.vo.VOTenant;
import org.oscm.intf.IdentityService;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.intf.SubscriptionService;
import org.oscm.samlsp.ws.base.WebserviceSAMLSPTestSetup;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;

public class TenantRelatedSampleWSTest {

    private final static String USER_ID_FOR_TENANT = "IntegrationTest_UserId_1000";
    private final static String USER_ID = "IntegrationTest_UserId_2000";

    private static TenantService tenantService;
    private static ConfigurationService configurationService;

    private static final String SSO_STS_URL = "SSO_STS_URL";
    private static final String SSO_STS_ENCKEY_LEN = "SSO_STS_ENCKEY_LEN";
    private static final String SSO_STS_METADATA_URL = "SSO_STS_METADATA_URL";

    private static VOTenant sampleTenant;
    private static VOOrganization sampleTenantOrg;

    @BeforeClass
    public static void setUp() throws Exception {
        new WebserviceSAMLSPTestSetup();

        tenantService = ServiceFactory.getDefault().getTenantService();
        configurationService = ServiceFactory.getDefault()
                .getConfigurationService();

        sampleTenant = createTenantWithSettings("sampleTenent");

        sampleTenantOrg = WebserviceTestBase.createOrganization(
                USER_ID_FOR_TENANT, "sampleTenantOrg", sampleTenant.getKey(),
                OrganizationRoleType.SUPPLIER);

        IdentityService sampleTenantIS = ServiceFactory
                .getSTSServiceFactory("sampleTenent", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUserDetails sampleUser = prepareUser(USER_ID,
                sampleTenantOrg.getOrganizationId());

        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        roles.add(UserRoleType.SERVICE_MANAGER);

        sampleTenantIS.createUser(sampleUser, roles, null);
    }

    @Test
    public void testGetUserDetails() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("qwerty10");

        VOOrganization orgWithTenant = WebserviceTestBase.createOrganization(
                USER_ID_FOR_TENANT, "qwerty10org", tenant.getKey(),
                OrganizationRoleType.SUPPLIER);

        IdentityService isWithTenant = ServiceFactory
                .getSTSServiceFactory("qwerty10", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUser user = prepareUser(USER_ID_FOR_TENANT);

        // when
        VOUserDetails userDetails = isWithTenant.getUserDetails(user);

        // then
        assertEquals(orgWithTenant.getOrganizationId(),
                userDetails.getOrganizationId());
    }

    @Test
    public void testGrantUserRoles() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("qwerty20");

        WebserviceTestBase.createOrganization(USER_ID_FOR_TENANT, "qwerty20org",
                tenant.getKey(), OrganizationRoleType.SUPPLIER);

        IdentityService isWithTenant = ServiceFactory
                .getSTSServiceFactory("qwerty20", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUser user = prepareUser(USER_ID_FOR_TENANT);

        // when
        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        roles.add(UserRoleType.SERVICE_MANAGER);

        isWithTenant.grantUserRoles(user, roles);

        // then
        VOUserDetails userDetails = isWithTenant.getUserDetails(user);
        Set<UserRoleType> userRoles = userDetails.getUserRoles();

        assertTrue(userRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
        assertTrue(userRoles.contains(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void testAddRevokeUserUnitAssignment() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("tenant100");

        VOOrganization organization = WebserviceTestBase.createOrganization(
                USER_ID_FOR_TENANT, "tenant100org", tenant.getKey(),
                OrganizationRoleType.SUPPLIER);

        OrganizationalUnitService unitService = ServiceFactory
                .getSTSServiceFactory("tenant100", null)
                .getOrganizationalUnitService(USER_ID_FOR_TENANT, "secret");

        unitService.createUnit("tenant100Unit", "description", "refNumId");

        IdentityService isWithTenant = ServiceFactory
                .getSTSServiceFactory("tenant100", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUserDetails user = prepareUser(USER_ID,
                organization.getOrganizationId());

        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        roles.add(UserRoleType.SERVICE_MANAGER);

        isWithTenant.createUser(user, roles, null);

        // when
        List<VOUser> usersToBeAdded = new ArrayList<>();
        usersToBeAdded.add(user);

        isWithTenant.addRevokeUserUnitAssignment("tenant100Unit",
                usersToBeAdded, null);

        // then
        // user is assigned - no ObjectNotFoundException thrown
    }

    @Test
    public void testGetUser() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("tenant200");

        VOOrganization organization = WebserviceTestBase.createOrganization(
                USER_ID_FOR_TENANT, "tenant200org", tenant.getKey(),
                OrganizationRoleType.SUPPLIER);

        IdentityService identityService = ServiceFactory
                .getSTSServiceFactory("tenant200", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUserDetails userToBeCreated = prepareUser(USER_ID,
                organization.getOrganizationId());

        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        roles.add(UserRoleType.SERVICE_MANAGER);

        VOUserDetails createdUser = identityService.createUser(userToBeCreated,
                roles, null);

        // when
        VOUser adminUser = identityService.getUser(prepareUser(
                USER_ID_FOR_TENANT, organization.getOrganizationId()));
        VOUser user = identityService.getUser(userToBeCreated);

        // then
        assertEquals(organization.getOrganizationId(),
                adminUser.getOrganizationId());
        assertEquals(createdUser.getKey(), user.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetUserNoOrgSet() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("tenant1000");

        WebserviceTestBase.createOrganization(USER_ID_FOR_TENANT,
                "tenant1000org", tenant.getKey(),
                OrganizationRoleType.SUPPLIER);

        IdentityService identityService = ServiceFactory
                .getSTSServiceFactory("tenant1000", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        // when
        identityService.getUser(prepareUser(USER_ID_FOR_TENANT));

        // then
        // ObjectNotFoundException expected
    }

    @Ignore
    // TODO - security exception is thrown
    public void testUnitGrantUserRoles() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("tenant999");

        WebserviceTestBase.createOrganization(USER_ID_FOR_TENANT,
                "tenant999org", tenant.getKey(), OrganizationRoleType.SUPPLIER);

        IdentityService identityService = ServiceFactory
                .getSTSServiceFactory("tenant999", null)
                .getIdentityService(USER_ID_FOR_TENANT, "secret");

        VOUserDetails user = prepareUser(USER_ID, "tenant999org");
        List<UserRoleType> roles = new ArrayList<UserRoleType>();
        roles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        roles.add(UserRoleType.SERVICE_MANAGER);

        identityService.createUser(user, roles, null);

        OrganizationalUnitService unitService = ServiceFactory
                .getSTSServiceFactory("tenant999", null)
                .getOrganizationalUnitService(USER_ID_FOR_TENANT, "secret");

        VOOrganizationalUnit unit = unitService.createUnit("tenant999unit",
                "desc", "refId");

        identityService.addRevokeUserUnitAssignment("tenant999unit",
                Collections.<VOUser> singletonList(user),
                Collections.<VOUser> emptyList());

        List<UnitRoleType> unitRoles = new ArrayList<UnitRoleType>();
        unitRoles.add(UnitRoleType.ADMINISTRATOR);

        // when
        unitService.grantUserRoles(user, unitRoles, unit);

        // then
    }

    @Test
    public void testGetSubscriptionsForUser() throws Exception {

        // given
        VOTenant tenant = createTenantWithSettings("tenant00");

        WebserviceTestBase.createOrganization(USER_ID_FOR_TENANT, "tenant00org",
                tenant.getKey(), OrganizationRoleType.SUPPLIER);

        SubscriptionService subscriptionService = ServiceFactory
                .getSTSServiceFactory("tenant00", null)
                .getSubscriptionService(USER_ID_FOR_TENANT, "secret");

        VOUserDetails user = prepareUser(USER_ID_FOR_TENANT, "tenant00org");

        // when
        subscriptionService.getSubscriptionsForUser(user);

        // then
        // no ObjectNotFoundException thrown

    }

    private static VOTenant createTenantWithSettings(String tenantId)
            throws Exception {
        WebserviceSAMLSPTestSetup.createTenant(tenantId);
        VOTenant tenant = tenantService.getTenantByTenantId(tenantId);
        return tenant;
    }

  
    private static VOUserDetails prepareUser(String userId) throws Exception {
        return prepareUser(userId, null);
    }

    private static VOUserDetails prepareUser(String userId,
            String organizationId) throws Exception {

        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        user.setOrganizationId(organizationId);
        String mailAddress = WebserviceTestBase.getMailReader()
                .getMailAddress();
        user.setEMail(mailAddress);
        user.setLocale("en");

        return user;
    }
}