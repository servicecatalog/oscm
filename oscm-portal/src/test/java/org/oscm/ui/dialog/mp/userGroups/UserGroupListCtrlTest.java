/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.junit.Before;
import org.junit.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author mao
 * 
 */
public class UserGroupListCtrlTest extends BaseBean {

    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_ERROR = "error";
    private UserGroupListModel model;
    private UserGroupListCtrl ctrl;
    private UserGroupService userGroupService;

    @Before
    public void setup() throws Exception {
        userGroupService = mock(UserGroupService.class);
        model = spy(new UserGroupListModel());
        ctrl = spy(new UserGroupListCtrl() {
            @Override
            public void addMessage(FacesMessage.Severity severity, String msgKey, Object... params) {

            }
        });
        ctrl.setUserGroupService(userGroupService);
        ctrl.setModel(model);
        ctrl.ui = spy(new UiDelegate() {
            @Override
            public void handleException(SaaSApplicationException ex) {

            }
        });
        List<POUserGroup> groups = new ArrayList<POUserGroup>();
        groups.add(new POUserGroup());
        doReturn(groups).when(userGroupService).getGroupListForOrganization();
        doReturn(Long.valueOf(1L)).when(userGroupService).getUserCountForGroup(
                anyLong(), anyBoolean());
    }

    @Test
    public void deleteUserGroup_Null() throws Exception {
        // given
        model.setSelectedGroup(null);

        // when
        String result = ctrl.deleteUserGroup();

        // then
        verify(userGroupService, times(0)).deleteGroup(any(POUserGroup.class));
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void deleteUserGroup() throws Exception {
        // given
        model.setSelectedGroup(new POUserGroup());

        // when
        String result = ctrl.deleteUserGroup();

        // then
        verify(userGroupService, times(1)).deleteGroup(any(POUserGroup.class));
        assertEquals(OUTCOME_REFRESH, result);
    }

    @Test
    public void deleteUserGroup_ValidationException() throws Exception {
        // given
        doThrow(new ValidationException()).when(userGroupService).deleteGroup(
                any(POUserGroup.class));
        model.setSelectedGroup(new POUserGroup());

        // when
        String result = ctrl.deleteUserGroup();

        // then
        verify(userGroupService, times(1)).deleteGroup(any(POUserGroup.class));
        assertEquals(OUTCOME_REFRESH, result);
    }

    @Test
    public void deleteUserGroup_OperationNotPermittedException()
            throws Exception {
        // given
        doThrow(new OperationNotPermittedException()).when(userGroupService)
                .deleteGroup(any(POUserGroup.class));
        model.setSelectedGroup(new POUserGroup());

        // when
        String result = ctrl.deleteUserGroup();

        // then
        verify(userGroupService, times(1)).deleteGroup(any(POUserGroup.class));
        assertEquals(OUTCOME_REFRESH, result);
    }
}
