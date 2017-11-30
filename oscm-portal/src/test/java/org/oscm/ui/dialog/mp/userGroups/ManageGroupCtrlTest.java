/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POService;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.POUserGroupToInvisibleProduct;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceListResult;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.stubs.FacesContextStub;

/**
 * @author mao
 * 
 */
public class ManageGroupCtrlTest {

    static final String APPLICATION_BEAN = "appBean";

    public static final String OUTCOME_ERROR = "error";
    private ManageGroupModel model;
    private ManageGroupCtrl ctrl;
    private UserGroupService userGroupService;
    private SessionBean sessionBean;
    private IdentityService identityService;
    private ExternalContext exContext;
    private TableState tableStatus;
    private UserBean userBean;

    private POUserGroup selectedGroup;
    private SearchServiceInternal searchServiceInternal;

    @Before
    public void setup() throws Exception {
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        tableStatus = mock(TableState.class);
        given(viewRoot.getLocale()).willReturn(Locale.ENGLISH);
        new FacesContextStub(Locale.ENGLISH).setViewRoot(viewRoot);

        List<VOService> servicesList = getServicesList();
        VOServiceListResult voServiceListResult = new VOServiceListResult();
        voServiceListResult.setServices(servicesList);
        userGroupService = mock(UserGroupService.class);
        sessionBean = mock(SessionBean.class);
        exContext = mock(ExternalContext.class);
        identityService = mock(IdentityService.class);
        searchServiceInternal = mock(SearchServiceInternal.class);
        userBean = mock(UserBean.class);
        ctrl = spy(new ManageGroupCtrl());
        doNothing().when(ctrl).redirectToGroupListPage();
        doReturn("1000").when(ctrl).getSelectedGroupId();
        ctrl.setUserGroupService(userGroupService);
        ctrl.setSearchServiceInternal(searchServiceInternal);
        ctrl.setIdService(identityService);
        model = new ManageGroupModel();
        ctrl.setManageGroupModel(model);
        UiDelegate uiDel = spy(new UiDelegate() {
            @Override
            public void handleException(SaaSApplicationException ex) {

            }

            @Override
            public void handleError(String clientId, String messageKey,
                                    Object... params) {

            }

            @Override
            public String getText(String key, Object... params) {
                return "";
            }

            @Override
            public SessionBean findSessionBean() {
                return sessionBean;
            }

            @Override
            public ExternalContext getExternalContext() {
                return exContext;
            }
        });
        ctrl.setUi(uiDel);
        doReturn(new ArrayList<POUserGroup>()).when(userGroupService)
                .getGroupsForOrganization();
        doReturn(voServiceListResult).when(searchServiceInternal)
                .getServicesByCriteria(anyString(), anyString(),
                        any(ListCriteria.class), any(PerformanceHint.class));
        doReturn(voServiceListResult).when(searchServiceInternal)
                .getAccesibleServices(anyString(), anyString(),
                        any(ListCriteria.class), any(PerformanceHint.class));
        doReturn(tableStatus).when(uiDel)
                .findBean(eq(TableState.BEAN_NAME));
        when(userBean.getUserFromSessionWithoutException())
                .thenReturn(initOrgAdmin());
        doReturn(userBean).when(uiDel).findUserBean();
        initModelData();
    }

    @Test
    public void getInitialize_test() throws ObjectNotFoundException {
        //given
        POUserGroup userGroup = new POUserGroup();
        userGroup.setUsersAssignedToUnit(new ArrayList<POUserInUnit>());
        ctrl.getManageGroupModel().setSelectedGroup(userGroup);
        ctrl.getManageGroupModel().setSelectedGroupId("10000");
        doReturn(userGroup).when(userGroupService).getUserGroupDetailsWithUsers(anyLong());
        ObjectNotFoundException e = new ObjectNotFoundException();
        doThrow(e).when(userGroupService).getUserGroupDetailsForList(anyLong());

        //when
        ctrl.getInitialize();

        //then
        assertNull(ctrl.getManageGroupModel().getSelectedGroup());
        assertNull(ctrl.getManageGroupModel().getSelectedGroupId());
    }

    @Test
    public void save_nullSelectedUserGroup() throws Exception {
        // given
        model.setSelectedGroupId(null);

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
    }

    @Test
    public void save() throws Exception {
        // given
        POUserGroup userGroup = new POUserGroup();
        doReturn(userGroup).when(userGroupService).updateGroup(
                eq(selectedGroup), anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        userGroup.setKey(1L);

        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());

        ctrl.getManageGroupModel().setSelectedGroup(selectedGroup);
        doReturn(new ArrayList<POUserGroupToInvisibleProduct>())
                .when(userGroupService).getInvisibleProducts(anyLong());
        ctrl.getInitialize();

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(1, model.getSelectedGroup().getKey());
        verify(tableStatus, times(1)).resetActiveEditPage();
    }

    @Test
    public void save_ObjectNotFound() throws Exception {
        // given
        ObjectNotFoundException e = new ObjectNotFoundException();
        doThrow(e).when(userGroupService).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(ctrl.getUi(), times(1)).handleException(e);
    }

    @Test
    public void save_NotAvailableService() throws Exception {
        // given
        OperationNotPermittedException e = new OperationNotPermittedException();
        e.setMessageKey(BaseBean.ERROR_NOT_AVALIABLE_SERVICE);
        doThrow(e).when(userGroupService).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        verify(ctrl.getUi(), times(1)).handleException(e);
    }

    @Test
    public void save_UserGroupNotFound() throws Exception {
        // given
        ObjectNotFoundException e = new ObjectNotFoundException();
        e.setMessageKey(BaseBean.ERROR_USERGROUP_NOT_FOUND);
        doThrow(e).when(userGroupService).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();

        // when
        String result = ctrl.save();

        // then
        assertEquals(BaseBean.ERROR_USERGROUP_NOT_FOUND_EXCEPTION, result);
        verify(ctrl.getUi(), times(1)).handleException(e);
    }

    @Test
    public void assemblePOService_emptyName() {
        // given
        List<VOService> voServices = createVoServices(null);

        // when
        List<POService> result = ctrl.assemblePOService(voServices);

        // then
        assertEquals(1, result.size());
        assertEquals("", result.get(0).getServiceName());
    }

    @Test
    public void assemblePOService() {
        // given
        List<VOService> voServices = createVoServices("test");

        // when
        List<POService> result = ctrl.assemblePOService(voServices);

        // then
        assertEquals(1, result.size());
        assertEquals("test", result.get(0).getServiceName());
    }

    @Test
    public void toSortedServiceRows_B11124() throws Exception {
        // given
        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertEquals(2, result.size());
        assertListIsSorted(result);
    }

    @Test
    public void initServiceRows() throws Exception {
        // given
        List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<POUserGroupToInvisibleProduct>();
        doReturn(invisibleProducts).when(userGroupService)
                .getInvisibleProducts(model.getSelectedGroup().getKey());

        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertListIsSorted(result);
        assertEquals(2, result.size());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(0).isSelected()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(1).isSelected()));
    }

    @Test
    public void initServiceRows_OrgAdmin() throws Exception {
        // given
        List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<>();
        invisibleProducts.add(prepareInvisibleProduct(1L, true));
        invisibleProducts.add(prepareInvisibleProduct(2L, false));
        doReturn(invisibleProducts).when(userGroupService)
                .getInvisibleProducts(model.getSelectedGroup().getKey());

        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertListIsSorted(result);
        assertEquals(2, result.size());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(0).isSelected()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.get(1).isSelected()));
    }

    @Test
    public void initServiceRows_UnitAdminWithTwoInvisibleProducts()
            throws Exception {
        // given
        List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<>();
        invisibleProducts.add(prepareInvisibleProduct(1L, true));
        invisibleProducts.add(prepareInvisibleProduct(2L, false));
        doReturn(invisibleProducts).when(userGroupService)
                .getInvisibleProducts(model.getSelectedGroup().getKey());

        when(userBean.getUserFromSessionWithoutException())
                .thenReturn(initUnitAdmin());
        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertListIsSorted(result);
        assertEquals(1, result.size());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.get(0).isSelected()));
    }

    @Test
    public void initServiceRows_UnitAdminWithOneInvisibleProductForAllUsers()
            throws Exception {
        // given
        List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<>();
        invisibleProducts.add(prepareInvisibleProduct(1L, true));
        doReturn(invisibleProducts).when(userGroupService)
                .getInvisibleProducts(model.getSelectedGroup().getKey());

        when(userBean.getUserFromSessionWithoutException())
                .thenReturn(initUnitAdmin());
        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertListIsSorted(result);
        assertEquals(1, result.size());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(0).isSelected()));
    }

    @Test
    public void initServiceRows_UnitAdminWithOneInvisibleProduct()
            throws Exception {
        // given
        List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<>();
        invisibleProducts.add(prepareInvisibleProduct(1L, false));
        doReturn(invisibleProducts).when(userGroupService)
                .getInvisibleProducts(model.getSelectedGroup().getKey());

        when(userBean.getUserFromSessionWithoutException())
                .thenReturn(initUnitAdmin());
        // when
        List<ServiceRow> result = ctrl.initServiceRows();

        // then
        assertListIsSorted(result);
        assertEquals(2, result.size());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(0).isSelected()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.get(1).isSelected()));
    }

    @Test
    public void setSelectedServices() {
        // given
        ServiceRow serviceRow1 = new ServiceRow(new POService(), false);
        ServiceRow serviceRow2 = new ServiceRow(new POService(), true);
        model.setServiceRows(Arrays.asList(serviceRow1, serviceRow2));

        // when
        ctrl.setSelectedServices();

        // then
        assertEquals(1, model.getSelectedGroup().getInvisibleServices().size());
    }

    @Test
    public void update() throws Exception {
        POUserGroup group = new POUserGroup();
        group.setGroupName("NAME");
        doReturn(group).when(userGroupService).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        model.setSelectedGroup(new POUserGroup());
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        String result = ctrl.save();
        verify(userGroupService, times(1)).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(tableStatus, times(1)).resetActiveEditPage();
    }

    @Test
    public void update_ValidationException() throws Exception {
        model.setSelectedGroup(new POUserGroup());
        doThrow(new ValidationException()).when(userGroupService).updateGroup(
                eq(selectedGroup), anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        String result = ctrl.save();
        verify(userGroupService, times(1)).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void update_ConcurrentModificationException() throws Exception {
        model.setSelectedGroup(new POUserGroup());
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        doThrow(new ConcurrentModificationException()).when(userGroupService)
                .updateGroup(eq(selectedGroup), anyString(),
                        anyListOf(POUserInUnit.class),
                        anyListOf(POUserInUnit.class),
                        anyListOf(POUserInUnit.class));
        String result = ctrl.save();
        verify(userGroupService, times(1)).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void update_OperationNotPermittedException() throws Exception {
        model.setSelectedGroup(new POUserGroup());
        doThrow(new OperationNotPermittedException()).when(userGroupService)
                .updateGroup(eq(selectedGroup), anyString(),
                        anyListOf(POUserInUnit.class),
                        anyListOf(POUserInUnit.class),
                        anyListOf(POUserInUnit.class));
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        doReturn(selectedGroup).when(userGroupService)
                .getUserGroupDetailsWithUsers(anyLong());
        ctrl.getInitialize();
        String result = ctrl.save();
        verify(userGroupService, times(1)).updateGroup(eq(selectedGroup),
                anyString(), anyListOf(POUserInUnit.class),
                anyListOf(POUserInUnit.class), anyListOf(POUserInUnit.class));
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void selectGroup() {

        String result = ctrl.selectGroup();

        assertEquals(BaseBean.OUTCOME_EDIT_GROUP, result);
    }

    @Test
    public void initSelectedGroup() throws Exception {
        // given
        POUserGroup group = new POUserGroup();
        doReturn(group).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        // when
        ctrl.initSelectedGroup();

        // then
        assertEquals(group, model.getSelectedGroup());
    }

    private void initModelData() {
        selectedGroup = new POUserGroup();
        model.setSelectedGroupId("1000");
        model.setSelectedGroup(selectedGroup);
        POUserDetails poUser = new POUserDetails();
        poUser.setUserId("assignedUser");
        List<POService> poServices = new ArrayList<POService>();
        POService poService = new POService();
        poService.setKey(1L);
        poServices.add(poService);
        selectedGroup.setUsers(Arrays.asList(poUser));
        selectedGroup.setGroupName("selectedGroup");
        selectedGroup.setInvisibleServices(poServices);
        POUserInUnit poUserInUnit = new POUserInUnit();
        poUserInUnit.setLocale("en");
        poUserInUnit.setPoUser(poUser);
        poUserInUnit.setRoleInUnit(UnitRoleType.USER.name());
        poUserInUnit.setSelected(true);
        selectedGroup.setUsersAssignedToUnit(Arrays.asList(poUserInUnit));

    }

    private List<VOService> createVoServices(String name) {
        List<VOService> voServices = new ArrayList<VOService>();
        VOService voService = new VOService();
        voService.setKey(1L);
        voService.setVersion(1);
        voService.setSellerName("sellerName");
        voService.setName(name);
        voServices.add(voService);
        return voServices;
    }

    private void assertListIsSorted(List<ServiceRow> rows) {
        String prev = null;
        boolean prevSelected = false;
        for (ServiceRow row : rows) {
            if (prev != null) {
                if ((row.isSelected() && prevSelected)
                        || (!row.isSelected() && !prevSelected)) {
                    assertEquals(Boolean.TRUE, Boolean.valueOf(prev.compareTo(
                            row.getService().getServiceName()) <= 0));
                } else if (row.isSelected() && !prevSelected) {
                    assertEquals(1, 0);
                } else {
                    assertEquals(1, 1);
                }
            } else {
                prev = row.getService().getServiceName();
                prevSelected = row.isSelected();
            }
        }

    }

    private List<VOService> getServicesList() {
        List<VOService> servicesList = new ArrayList<VOService>();
        VOService serviceFirst = new VOService();
        serviceFirst.setKey(1L);
        serviceFirst.setSellerName("chen");
        serviceFirst.setVersion(1000);
        serviceFirst.setName("voServiceName");
        VOService serviceSecond = new VOService();
        serviceSecond.setKey(2L);
        serviceSecond.setSellerName("zhang");
        serviceSecond.setVersion(1000);
        serviceSecond.setName("voServiceNameSecond");
        servicesList.add(serviceFirst);
        servicesList.add(serviceSecond);
        return servicesList;
    }

    @Test
    public void confirmIfUnitExists() throws Exception {
        // given
        ManageGroupCtrl mgc = new ManageGroupCtrl() {
            @Override
            public String getSelectedGroupId() {
                return "1000";
            }
        };
        mgc.setManageGroupModel(model);
        doReturn(model.getSelectedGroup()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());

        // when
        String result = ctrl.confirmIfUnitExists();

        // then
        assertEquals(BaseBean.OUTCOME_EDIT_GROUP, result);
    }

    @Test
    public void confirmIfUnitExists_EmptyId() throws Exception {
        // given
        ManageGroupCtrl mgc = new ManageGroupCtrl() {
            @Override
            public String getSelectedGroupId() {
                return "";
            }
        };
        mgc.setManageGroupModel(model);
        // when
        String result = mgc.confirmIfUnitExists();
        // then
        assertEquals(BaseBean.OUTCOME_REFRESH, result);
    }

    @Test
    public void confirmIfUnitExists_NullId() throws Exception {
        // given
        ManageGroupCtrl mgc = new ManageGroupCtrl() {
            @Override
            public String getSelectedGroupId() {
                return null;
            }
        };
        mgc.setManageGroupModel(model);
        // when
        String result = mgc.confirmIfUnitExists();
        // then
        assertEquals(BaseBean.OUTCOME_REFRESH, result);
    }

    @Test
    public void confirmIfUnitExists_ObjectNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(userGroupService)
                .getUserGroupDetailsForList(anyLong());
        // when
        String result = ctrl.confirmIfUnitExists();
        // then
        assertEquals(BaseBean.OUTCOME_REFRESH, result);
    }

    private VOUserDetails initOrgAdmin() {
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(1000L);
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        voUserDetails.setUserRoles(userRoles);
        return voUserDetails;
    }

    private VOUserDetails initUnitAdmin() {
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(2000L);
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.UNIT_ADMINISTRATOR);
        voUserDetails.setUserRoles(userRoles);
        return voUserDetails;
    }

    private POUserGroupToInvisibleProduct prepareInvisibleProduct(
            long servicekey, boolean forAllUsers) {
        POUserGroupToInvisibleProduct poUserGroupToInvisibleProduct = new POUserGroupToInvisibleProduct();
        poUserGroupToInvisibleProduct.setServiceKey(servicekey);
        poUserGroupToInvisibleProduct.setForAllUsers(forAllUsers);
        return poUserGroupToInvisibleProduct;
    }
}
