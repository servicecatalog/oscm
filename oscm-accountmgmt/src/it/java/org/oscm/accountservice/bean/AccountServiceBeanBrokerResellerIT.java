/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class AccountServiceBeanBrokerResellerIT extends EJBTestBase {

    private AccountService accountService;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new AccountServiceBean());
        container.addBean(new IdentityServiceBean());

        ds = container.get(DataService.class);
        accountService = container.get(AccountService.class);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void udateAccountInformation_OtherAccount_AsAdminBroker()
            throws Exception {
        // given a broker user who is admin
        PlatformUser brokerUser = createUserAndLogin(true,
                OrganizationRoleType.BROKER);
        VOOrganization voOrganization = givenOrganization(brokerUser);
        VOUserDetails voUser = givenOtherUser();

        // when updating the account information of another user
        // an OperationNotPermittedException is expected.
        accountService.updateAccountInformation(voOrganization, voUser,
                "FUJITSU", null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void udateAccountInformation_OtherAccount_AsAdminReseller()
            throws Exception {
        // given a reseller user who is admin
        PlatformUser resellerUser = createUserAndLogin(true,
                OrganizationRoleType.RESELLER);
        VOOrganization voOrganization = givenOrganization(resellerUser);
        VOUserDetails voUser = givenOtherUser();

        // when updating the account information of another user
        // an OperationNotPermittedException is expected.
        accountService.updateAccountInformation(voOrganization, voUser,
                "FUJITSU", null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateAccountInformation_AsNonAdminBroker() throws Exception {
        // given a broker user who is not admin
        PlatformUser brokerUser = createUserAndLogin(false,
                OrganizationRoleType.BROKER);
        VOOrganization voOrganization = givenOrganization(brokerUser);
        VOUserDetails voUser = givenOwnUser(brokerUser);

        // when updating the account information
        // an OperationNotPermittedException is expected.
        accountService.updateAccountInformation(voOrganization, voUser, null,
                null);

    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateAccountInformation_AsNonAdminReseller() throws Exception {
        // given a reseller user who is not admin
        PlatformUser resellerUser = createUserAndLogin(false,
                OrganizationRoleType.RESELLER);
        VOOrganization voOrganization = givenOrganization(resellerUser);
        VOUserDetails voUser = givenOwnUser(resellerUser);

        // when updating the account information
        // an OperationNotPermittedException is expected.
        accountService.updateAccountInformation(voOrganization, voUser, null,
                null);

    }

    private VOOrganization givenOrganization(final PlatformUser platformUser) {
        VOOrganization organization = new VOOrganization();
        organization.setOrganizationId(
                platformUser.getOrganization().getOrganizationId());
        organization.setEmail("admin@organization.com");
        organization.setPhone("123456");
        organization.setUrl("http://www.example.com");
        organization.setName("example");
        organization.setAddress("an address");
        organization.setLocale(platformUser.getLocale());
        organization.setVersion(platformUser.getOrganization().getVersion());
        organization.setKey(platformUser.getOrganization().getKey());
        organization.setDomicileCountry("DE");
        organization.setNameSpace("http://oscm.org/xsd/2.0");
        return organization;
    }

    private VOUserDetails givenOtherUser() {
        VOUserDetails user = new VOUserDetails();
        user.setKey(1L);
        return user;
    }

    private VOUserDetails givenOwnUser(final PlatformUser platformUser) {
        VOUserDetails user = new VOUserDetails();
        user.setKey(platformUser.getKey());
        user.setUserId(platformUser.getUserId());
        user.setEMail("admin@organization.com");
        user.setOrganizationId(
                platformUser.getOrganization().getOrganizationId());
        user.setLocale(platformUser.getLocale());

        return user;
    }

    private PlatformUser createUserAndLogin(final boolean isAdmin,
            final OrganizationRoleType orgRoleType) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        orgRoleType);

                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        isAdmin, "admin");

                UserRoleType userRole = orgRoleType.correspondingUserRole();
                PlatformUsers.grantRoles(ds, user, userRole);

                if (isAdmin) {
                    container.login(String.valueOf(user.getKey()),
                            ROLE_ORGANIZATION_ADMIN, userRole.name());
                } else {
                    container.login(String.valueOf(user.getKey()),
                            userRole.name());
                }

                return user;
            }
        });
    }

}
