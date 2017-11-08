/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-10-7                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author zhaohang
 * 
 */
public class AccountServiceBeanEditProfileTest {

    private AccountServiceBean asb;
    private final PlatformUser user = new PlatformUser();
    String marketplaceId = "marketplaceId";
    Marketplace marketplace = new Marketplace();
    List<PlatformUser> platformUsers = new LinkedList<PlatformUser>();

    @Before
    public void setup() throws Exception {
        asb = new AccountServiceBean();
        asb.dm = mock(DataService.class);
        asb.cs = mock(CommunicationServiceLocal.class);
        asb.im = mock(IdentityServiceLocal.class);

        Organization org = new Organization();
        user.setOrganization(org);
        user.setEmail(null);
        platformUsers.add(user);
        doReturn(marketplace).when(asb.dm).getReferenceByBusinessKey(
                any(Marketplace.class));
        doReturn(user).when(asb.dm).getCurrentUser();
        doReturn(new SendMailStatus<PlatformUser>()).when(asb.cs).sendMail(
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class), any(PlatformUser[].class));
    }

    @Test
    public void updateAccountInformation_OrganizationAdmin() throws Exception {
        // given
        VOUserDetails voUser = new VOUserDetails();
        Organization org = new Organization();
        doReturn(null).when(asb.im).modifyUserData(user, voUser, true, false);
        setAdminRole(UserRoleType.ORGANIZATION_ADMIN);

        // when
        asb.updateAccountInformation(org, voUser, marketplaceId);

        // then
        verify(asb.cs, times(1)).sendMail(EmailType.ORGANIZATION_UPDATED, null,
                marketplace,
                platformUsers.toArray(new PlatformUser[platformUsers.size()]));
    }

    @Test
    public void updateAccountInformation_NotOrganizationAdmin()
            throws Exception {
        // given
        VOUserDetails voUser = new VOUserDetails();
        doReturn(null).when(asb.im).modifyUserData(user, voUser, true, false);
        setAdminRole(UserRoleType.ORGANIZATION_ADMIN);

        // when
        asb.updateAccountInformation(null, voUser, marketplaceId);

        // then
        verify(asb.cs, times(1)).sendMail(EmailType.ORGANIZATION_UPDATED, null,
                marketplace,
                platformUsers.toArray(new PlatformUser[platformUsers.size()]));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateAccountInformation_OperationNotPermitted()
            throws Exception {
        // given
        VOUserDetails voUser = new VOUserDetails();
        Organization org = new Organization();
        doReturn(null).when(asb.im).modifyUserData(user, voUser, true, false);
        setAdminRole(UserRoleType.SUBSCRIPTION_MANAGER);

        // when
        asb.updateAccountInformation(org, voUser, marketplaceId);
    }

    private void setAdminRole(UserRoleType type) {
        Set<RoleAssignment> grantedRoles = new HashSet<RoleAssignment>();
        RoleAssignment assignedRole = new RoleAssignment();
        UserRole role = new UserRole();
        role.setRoleName(type);
        assignedRole.setRole(role);
        grantedRoles.add(assignedRole);
        user.setAssignedRoles(grantedRoles);
    }
}
