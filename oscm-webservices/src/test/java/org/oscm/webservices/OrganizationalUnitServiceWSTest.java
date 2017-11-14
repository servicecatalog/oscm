/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 22.07.15 12:00
 *
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.ws.WebServiceContext;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOUser;

public class OrganizationalUnitServiceWSTest {

    OrganizationalUnitServiceWS service = new OrganizationalUnitServiceWS();

    @Before
    public void setup() {
        service.WS_LOGGER = mock(WebServiceLogger.class);
        service.dataService = mock(DataService.class);
        service.wsContext = mock(WebServiceContext.class);
        service.localService = mock(UserGroupServiceLocalBean.class);
    }

    @Test
    public void getOrganizationalUnits() throws OperationNotPermittedException,
            org.oscm.types.exceptions.OperationNotPermittedException {
        // given
        List<UserGroup> createdUnits = getUnitsMock(1);
        doReturn(createdUnits).when(service.localService)
                .getOrganizationalUnits(null);

        // when
        List<VOOrganizationalUnit> units = service.getOrganizationalUnits(null);

        // then
        Assert.assertNotNull(units);
        Assert.assertEquals(1, units.size());
        Assert.assertEquals(createdUnits.get(0).getName(),
                units.get(0).getName());
    }

    @Test
    public void createUnit() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException,
            org.oscm.types.exceptions.OperationNotPermittedException,
            org.oscm.types.exceptions.NonUniqueBusinessKeyException {
        // given
        final String unitName = "TestUnit";
        final String unitDesc = "TestDesc";
        final String unitRefId = "refId";

        UserGroup userGroup = spy(new UserGroup());

        userGroup.setName(unitName);
        userGroup.setDescription(unitDesc);
        userGroup.setReferenceId(unitRefId);
        userGroup.setOrganization(new Organization());

        when(service.localService.createUserGroup(unitName, unitDesc,
                unitRefId)).thenReturn(userGroup);

        // when
        VOOrganizationalUnit unit = service.createUnit(unitName, unitDesc,
                unitRefId);

        // then
        Assert.assertNotNull(unit);
        Assert.assertEquals(unitName, unit.getName());
        Assert.assertEquals(unitDesc, unit.getDescription());
        Assert.assertEquals(unitRefId, unit.getReferenceId());
    }

    @Test
    public void grantUserRoles() throws ObjectNotFoundException,
            org.oscm.types.exceptions.OperationNotPermittedException,
            org.oscm.types.exceptions.ObjectNotFoundException,
            OperationNotPermittedException {
        // given
        List<UnitRoleType> roleTypes = Collections
                .singletonList(UnitRoleType.ADMINISTRATOR);
        PlatformUser user = getUserMock();
        UserGroup group = getUnitsMock(1).get(0);

        doNothing().when(service.localService).grantUserRoles(user, roleTypes,
                group);

        // when
        service.grantUserRoles(toVOUser(user),
                Collections.singletonList(
                        org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR),
                toVOOrganizationalUnit(group));

        // then
        verify(service.localService, times(1))
                .grantUserRolesWithHandleUnitAdminRole(any(PlatformUser.class),
                        eq(roleTypes), any(UserGroup.class));
    }

    @Test
    public void revokeUserRoles() throws ObjectNotFoundException,
            org.oscm.types.exceptions.OperationNotPermittedException,
            org.oscm.types.exceptions.ObjectNotFoundException,
            OperationNotPermittedException {
        // given
        List<UnitRoleType> roleTypes = Collections
                .singletonList(UnitRoleType.ADMINISTRATOR);
        PlatformUser user = getUserMock();
        UserGroup group = getUnitsMock(1).get(0);

        doNothing().when(service.localService).revokeUserRoles(user, roleTypes,
                group);

        // when
        service.revokeUserRoles(toVOUser(user),
                Collections.singletonList(
                        org.oscm.types.enumtypes.UnitRoleType.ADMINISTRATOR),
                toVOOrganizationalUnit(group));

        // then
        verify(service.localService, times(1)).revokeUserRoles(
                any(PlatformUser.class), eq(roleTypes), any(UserGroup.class));
    }

    @Test
    public void deleteUnit() throws Exception {
        // given
        UserGroup group = getUnitsMock(1).get(0);

        doNothing().when(service.localService).deleteUserGroup(group.getName());

        // when
        service.deleteUnit(group.getName());

        // then
        verify(service.localService, times(1)).deleteUserGroup(group.getName());
    }

    private List<UserGroup> getUnitsMock(int number) {
        List<UserGroup> result = new ArrayList<>();

        UserGroup userGroup;

        for (int i = 0; i < number; i++) {
            userGroup = new UserGroup();

            userGroup.setName(randomString());
            userGroup.setOrganization(new Organization());

            result.add(userGroup);
        }

        return result;
    }

    private PlatformUser getUserMock() {
        PlatformUser user = new PlatformUser();

        user.setUserId("TestUser");

        return user;
    }

    private String randomString() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    private VOUser toVOUser(PlatformUser pUser) {
        VOUser voUser = new VOUser();

        voUser.setUserId(pUser.getUserId());

        return voUser;
    }

    private VOOrganizationalUnit toVOOrganizationalUnit(UserGroup group) {
        VOOrganizationalUnit unit = new VOOrganizationalUnit();

        unit.setName(group.getName());

        return unit;
    }
}
