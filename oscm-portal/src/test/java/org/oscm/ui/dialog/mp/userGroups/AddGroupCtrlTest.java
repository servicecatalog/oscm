/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.stubs.FacesContextStub;

/**
 * @author mao
 * 
 */
public class AddGroupCtrlTest {

    static final String APPLICATION_BEAN = "appBean";
    public static final String OUTCOME_NEWGROUP = "addNewGroup";
    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_ERROR = "error";
    private ManageGroupModel model;
    private AddGroupCtrl ctrl;
    private UserGroupService userGroupService;
    private SearchServiceInternal searchServiceInternal;
    private ExternalContext exContext;
    private TableState tableStatus;

    @Before
    public void setup() throws Exception {
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        tableStatus = mock(TableState.class);
        given(viewRoot.getLocale()).willReturn(Locale.ENGLISH);
        new FacesContextStub(Locale.ENGLISH).setViewRoot(viewRoot);
        searchServiceInternal = mock(SearchServiceInternal.class);
        userGroupService = mock(UserGroupService.class);
        exContext = mock(ExternalContext.class);
        model = spy(new ManageGroupModel());
        ctrl = spy(new AddGroupCtrl());
        ctrl.setUserGroupService(userGroupService);
        ctrl.setSearchServiceInternal(searchServiceInternal);
        ctrl.setManageGroupModel(model);
        ctrl.setTableState(tableStatus);
        ctrl.setUi(spy(new UiDelegate() {
            @Override
            public void handleException(SaaSApplicationException ex) {

            }

            @Override
            public String getText(String key, Object... params) {
                return "modalName";
            }

            @Override
            public ExternalContext getExternalContext() {
                return exContext;
            }
        }));

    }

    @Test
    public void getInitListCriteria_CorrectParam() {
        // when
        ListCriteria result = ctrl.getInitListCriteria();
        // then
        assertEquals("", result.getFilter());
        assertEquals(0, result.getOffset());
        assertEquals(-1, result.getLimit());
    }

    @Test
    public void create_Null() {
        model.setSelectedGroup(null);
        String result = ctrl.create();
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void create() throws Exception {
        model.setSelectedGroup(new POUserGroup());
        String result = ctrl.create();
        verify(userGroupService, times(1)).createGroup(any(POUserGroup.class),
                anyString());
        verify(tableStatus, times(1)).resetActiveAddPage();
        assertEquals(OUTCOME_SUCCESS, result);
    }

    @Test
    public void create_ValidationException() throws Exception {
        model.setSelectedGroup(new POUserGroup());
        doThrow(new ValidationException()).when(userGroupService).createGroup(
                any(POUserGroup.class), anyString());
        String result = ctrl.create();
        verify(userGroupService, times(1)).createGroup(any(POUserGroup.class),
                anyString());
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void create_NonUniqueBusinessKeyException() throws Exception {
        // given
        model.setSelectedGroup(new POUserGroup());
        doThrow(new NonUniqueBusinessKeyException()).when(userGroupService)
                .createGroup(any(POUserGroup.class), anyString());
        // when
        String result = ctrl.create();
        // then
        verify(userGroupService, times(1)).createGroup(any(POUserGroup.class),
                anyString());
        assertEquals(0, model.getSelectedGroup().getUsers().size());
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void create_OperationNotPermittedException() throws Exception {
        // given
        model.setSelectedGroup(new POUserGroup());
        doThrow(new OperationNotPermittedException()).when(userGroupService)
                .createGroup(any(POUserGroup.class), anyString());
        // when
        String result = ctrl.create();
        // then
        verify(userGroupService, times(1)).createGroup(any(POUserGroup.class),
                anyString());
        assertEquals(OUTCOME_ERROR, result);
    }


}
