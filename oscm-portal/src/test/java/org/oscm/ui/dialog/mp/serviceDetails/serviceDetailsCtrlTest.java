/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: July 11, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.serviceDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.Service;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author sun
 * 
 */
public class serviceDetailsCtrlTest {

    private ServiceDetailsModel model;
    private ServiceDetailsCtrl ctrl;

    private PartnerService partnerService;
    private final String mpl = "marketPlaceId";
    private Response response;
    private VOUserDetails voUserDetails;
    private HttpServletRequest requestMock;
    private SessionBean sessionBean;

    @Before
    public void setup() throws Exception {
        ctrl = spy(new ServiceDetailsCtrl() {
            @Override
            void redirectToAccessDeniedPage() {
                return;
            }
        });
        ctrl.userGroupService = mock(UserGroupService.class);
        ctrl.model = mock(ServiceDetailsModel.class);
        model = ctrl.model;
        sessionBean = mock(SessionBean.class);
        partnerService = mock(PartnerService.class);
        ctrl.ui = spy(new UiDelegate() {
            @Override
            public Locale getViewLocale() {
                return new Locale("en");
            }

            @Override
            public String getMarketplaceId() {
                return mpl;
            }

            @Override
            public String getText(String key, Object... params) {
                return "";
            }

            @Override
            public SessionBean findSessionBean() {
                return sessionBean;
            }
        });
        requestMock = mock(HttpServletRequest.class);
        doReturn(requestMock).when(ctrl.ui).getRequest();
        doReturn(partnerService).when(ctrl.ui)
                .findService(PartnerService.class);
        initServiceInServer();
        initCurrentUser();
    }

    @Test
    public void getInitialize_redirectToAccessDeniedPage() throws Exception {
        // given
        doReturn(Arrays.asList(Long.valueOf(10001)))
                .when(ctrl.userGroupService).getInvisibleProductKeysForUser(1L);
        doReturn(Arrays.asList(Long.valueOf(10001))).when(model)
                .getInvisibleProductKeys();

        // when
        ctrl.getInitialize();

        // then
        verify(ctrl, times(1)).redirectToAccessDeniedPage();
    }

    @Test
    public void getInitialize() throws Exception {
        // given
        VOService voService = new VOService();
        voService.setKey(1l);
        Service service = new Service(voService);
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setKey(2L);
        voPriceModel.setType(PriceModelType.FREE_OF_CHARGE);
        service.setPriceModel(new PriceModel(voPriceModel));
        doReturn(service).when(model).getSelectedService();
        doReturn(service).when(model).getService();
        doReturn(Arrays.asList(Long.valueOf(10001)))
                .when(ctrl.userGroupService).getInvisibleProductKeysForUser(1L);

        // when
        ctrl.getInitialize();

        // then
        verify(ctrl, never()).redirectToAccessDeniedPage();
    }

    @Test
    public void isServiceAccessible_anonymousUser() {
        // given
        doReturn(null).when(ctrl.ui).getUserFromSessionWithoutException();

        // when
        boolean result = ctrl.isServiceAccessible(10001);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isServiceAccessible_organizationAdmin() {
        // given
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        voUserDetails.setUserRoles(userRoles);

        // when
        boolean result = ctrl.isServiceAccessible(10001);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    private void initServiceInServer() throws Exception {
        response = new Response();
        model.selectedServiceKey = "10001";
        doReturn(response).when(partnerService)
                .getAllServiceDetailsForMarketplace(
                        Long.valueOf(model.selectedServiceKey).longValue(),
                        "en", mpl);
        response.setReturnCodes(new ArrayList<ReturnCode>());
        List<Object> responseObjects = new ArrayList<Object>();
        responseObjects.add(new VOServiceEntry());
        responseObjects.add(new VODiscount());
        responseObjects.add(new VOService());
        response.setResults(responseObjects);
    }

    private void initCurrentUser() {
        voUserDetails = new VOUserDetails();
        voUserDetails.setKey(1L);
        doReturn(voUserDetails).when(ctrl.ui)
                .getUserFromSessionWithoutException();
    }
}
