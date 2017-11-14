/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.test.NullArgumentTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.intf.IdentityService;

public class IdentityServiceBeanNullArgumentTest extends
        NullArgumentTestBase<IdentityService> {

    public IdentityServiceBeanNullArgumentTest() {
        super(IdentityService.class);
        addNullAllowed("createUser", "marketplaceId");
        addNullAllowed("confirmAccount", "marketplaceId");
        addNullAllowed("importLdapUsers", "marketplaceId");
        addNullAllowed("deleteUser", "marketplaceId");
        addNullAllowed("lockUserAccount", "marketplaceId");
        addNullAllowed("requestResetOfUserPassword", "marketplaceId");
        addNullAllowed("sendAccounts", "marketplaceId");
        addNullAllowed("unlockUserAccount", "marketplaceId");
        addNullAllowed("importUsersInOwnOrganization", "marketplaceId");
        addNullAllowed("importUsers", "marketplaceId");
    }

    @Override
    protected IdentityService createInstance(TestContainer container)
            throws Exception {
        container.login("me", UserRoleType.ORGANIZATION_ADMIN.name(),
                UserRoleType.SERVICE_MANAGER.name(),
                UserRoleType.PLATFORM_OPERATOR.name());
        container.enableInterfaceMocking(true);
        container.addBean(new IdentityServiceBean());
        final org.oscm.intf.IdentityService service = new IdentityServiceWS();
        ((IdentityServiceWS) service).WS_LOGGER = mock(WebServiceLogger.class);
        ((IdentityServiceWS) service).delegate = container
                .get(org.oscm.internal.intf.IdentityService.class);
        container.addBean(service);
        DataService ds = container.get(DataService.class);
        when(ds.getCurrentUser()).thenReturn(newCurrentUser());
        when(ds.getCurrentUserIfPresent()).thenReturn(newCurrentUser());
        return service;
    }

    public PlatformUser newCurrentUser() {
        PlatformUser user = new PlatformUser();
        Organization organization = new Organization();
        organization.setOrganizationId("organizationId");
        user.setOrganization(organization);
        return user;
    }

}
