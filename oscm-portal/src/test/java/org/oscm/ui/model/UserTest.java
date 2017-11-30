/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
public class UserTest {

    private User user;
    private ValueChangeEvent event;

    @Before
    public void setup() {
        user = new User(new VOUserDetails());
        event = mock(ValueChangeEvent.class);
    }

    @Test
    public void isCheckBoxRendered() {
        assertTrue(user.isCheckBoxRendered());
    }

    @Test
    public void isCheckBoxRendered_KeySet() {
        user.getVOUserDetails().setKey(1234);
        assertFalse(user.isCheckBoxRendered());
    }

    @Test
    public void isImageRendered() {
        assertFalse(user.isImageRendered());
    }

    @Test
    public void isImageRendered_KeySet() {
        user.getVOUserDetails().setKey(1234);
        assertTrue(user.isImageRendered());
    }

    @Test
    public void isEmailLabelRendered() {
        user.setEmail("mail@host.de");
        assertTrue(user.isEmailLabelRendered());
    }

    @Test
    public void isEmailLabelRendered_EmailNull() {
        assertFalse(user.isEmailLabelRendered());
    }

    @Test
    public void isEmailLabelRendered_EmailEmpty() {
        user.setEmail("   ");
        assertFalse(user.isEmailLabelRendered());
    }

    @Test
    public void isEmailInputRendered() {
        user.setEmail("mail@host.de");
        assertFalse(user.isEmailInputRendered());
    }

    @Test
    public void isEmailInputRendered_EmailNull() {
        assertTrue(user.isEmailInputRendered());
    }

    @Test
    public void isUserInRole_UnitAdmin() {
        User user = prepareUserWithRole(UserRoleType.UNIT_ADMINISTRATOR);
        assertTrue(user.isUnitAdministrator());
    }

    @Test
    public void isUserInRole_SubscriptionManager() {
        User user = prepareUserWithRole(UserRoleType.SUBSCRIPTION_MANAGER);
        assertTrue(user.isSubscriptionManager());
    }

    private User prepareUserWithRole(UserRoleType userRoleType) {
        VOUserDetails voUserDetails = new VOUserDetails();
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(userRoleType);
        voUserDetails.setUserRoles(userRoles);
        User user = new User(voUserDetails);
        return user;
    }

    @Test
    public void roleKeyChanged() {
        // given
        Long roleKey = Long.valueOf(10010L);
        when(event.getNewValue()).thenReturn(roleKey);
        // when
        user.roleKeyChanged(event);
        // then
        assertEquals(roleKey.longValue(), user.getRoleKey());
    }

}
