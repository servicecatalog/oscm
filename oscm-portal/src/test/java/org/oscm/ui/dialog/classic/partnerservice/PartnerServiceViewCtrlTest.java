/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.partnerservice;

import static org.oscm.ui.stubs.UiDelegateStub.hasSuccessMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceInternalBean;
import org.oscm.ui.beans.MarketplaceBean;
import org.oscm.ui.beans.PriceModelBean;
import org.oscm.ui.beans.ServiceBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.ui.model.Service;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;
import org.oscm.ui.stubs.UIComponentStub;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.partnerservice.POPartnerServiceDetails;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author afschar
 */
@SuppressWarnings("boxing")
public class PartnerServiceViewCtrlTest {
    private final PartnerServiceViewCtrl ctrl = new PartnerServiceViewCtrl();
    private final VOUserDetails user = new VOUserDetails();
    private UiDelegateStub webContainer;
    private VOPriceModel voPriceModel;

    @BeforeClass
    public static void setupClass() {
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                // the implementation of the stub throws an
                // OperationNotSupportedException
            }
        };
    }

    @Before
    public void setup() {
        ctrl.model = new PartnerServiceViewModel();
        voPriceModel = new VOPriceModel();
        VOPricedRole pr = new VOPricedRole();
        pr.setPricePerUser(new BigDecimal(5));
        VORoleDefinition role = new VORoleDefinition();
        role.setName("role");
        role.setRoleId("roleId");
        pr.setRole(role);
        voPriceModel.setRoleSpecificUserPrices(Arrays.asList(pr));

        webContainer = new UiDelegateStub() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T findBean(String beanName) {
                if ("partnerServiceViewModel".equals(beanName)) {
                    return (T) new PartnerServiceViewModel();
                }
                if ("serviceBean".equals(beanName)) {
                    ServiceBean serviceBean = new ServiceBean();
                    serviceBean.setSessionBean(new SessionBean());
                    serviceBean.setMarketplaceBean(new MarketplaceBean());
                    return (T) serviceBean;
                }
                if ("userBean".equals(beanName)) {
                    return (T) new UserBean() {

                        private static final long serialVersionUID = 2L;

                        @Override
                        public VOUserDetails getUserFromSessionWithoutException() {
                            return user;
                        }
                    };
                }
                if ("priceModelBean".equals(beanName)) {
                    final PriceModelBean pm = new PriceModelBean() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected HttpSession getSession() {
                            return new HttpSessionStub(Locale.ENGLISH);
                        }

                        @Override
                        protected HttpServletRequest getRequest() {
                            return new HttpServletRequestStub() {
                                @Override
                                public String getServletPath() {
                                    return "/service/view.jsf";
                                }
                            };
                        }

                        @Override
                        public VOPriceModel getPriceModel() {
                            return voPriceModel;
                        };

                        @Override
                        protected ServiceProvisioningServiceInternal getProvisioningServiceInternal() {
                            ServiceProvisioningServiceInternal spsi = new ServiceProvisioningServiceInternalBean() {
                                @Override
                                public java.util.List<org.oscm.internal.vo.VOService> getSuppliedServices(
                                        PerformanceHint performanceHint) {
                                    return null;
                                };
                            };
                            return spsi;
                        };

                        @Override
                        public List<Service> getServices() {
                            return new ArrayList<>();
                        }
                    };
                    pm.setSessionBean(new SessionBean());
                    return (T) pm;
                }
                return super.findBean(beanName);
            }
        };
        ctrl.ui = webContainer;
    }

    @Test
    public void save() throws Exception {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.partnerServices = mock(PartnerService.class);
        when(
                ctrl.partnerServices
                        .updatePartnerServiceDetails(any(POPartnerServiceDetails.class)))
                .thenReturn(new Response());

        // when
        ctrl.save();

        // then
        verify(ctrl.partnerServices, times(1)).updatePartnerServiceDetails(
                any(POPartnerServiceDetails.class));
        assertThat(webContainer,
                hasSuccessMessage("info.service.saved", (Object[]) null));
    }

    @Test
    public void save_Failure() throws Exception {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.partnerServices = mock(PartnerService.class);
        doThrow(new ServiceStateException())
                .when(ctrl.partnerServices)
                .updatePartnerServiceDetails(any(POPartnerServiceDetails.class));

        // when
        ctrl.save();

        // then
        assertTrue(webContainer.hasErrors());
    }

    @Test
    public void setModel() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();

        // when
        ctrl.setModel(m);

        // then
        assertTrue(m == ctrl.getModel());
    }

    @Test
    public void getModel() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();
        ctrl.setModel(m);

        // when
        PartnerServiceViewModel m2 = ctrl.getModel();

        // then
        assertTrue(m == m2);
    }

    @Test
    public void isDisabled_ServiceKeyZero() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(0);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ServiceKeyNegative() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(-1);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ServiceKeyPositive() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(1);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ResellerServiceKeyPositive() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(1);
        user.getUserRoles().add(UserRoleType.RESELLER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertFalse(b);
    }

    @Test
    public void isDisabled_ResellerServiceKeyNegative() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(-1);
        user.getUserRoles().add(UserRoleType.RESELLER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ResellerServiceKeyZero() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(0);
        user.getUserRoles().add(UserRoleType.RESELLER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_BrokerServiceKeyPositive() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(1);
        user.getUserRoles().add(UserRoleType.BROKER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_BrokerServiceKeyNegative() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(-1);
        user.getUserRoles().add(UserRoleType.BROKER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_BrokerServiceKeyZero() {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setSelectedServiceKey(0);
        user.getUserRoles().add(UserRoleType.BROKER_MANAGER);

        // when
        boolean b = ctrl.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void serviceChanged() throws Exception {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.partnerServices = mock(PartnerService.class);
        POPartnerServiceDetails s = new POPartnerServiceDetails();
        s.setServiceKey(4L);
        s.setAutoAssignUserEnabled(true);
        when(ctrl.partnerServices.getServiceDetails(3L)).thenReturn(
                new Response(s));

        // when
        ctrl.serviceChanged(new ValueChangeEvent(new UIComponentStub(null),
                null, Long.valueOf(3)));

        // then
        assertEquals(3L, ctrl.getModel().getSelectedServiceKey());
        assertEquals(4L, ctrl.getModel().getPartnerServiceDetails()
                .getServiceKey());
        assertEquals(true, ctrl.getModel().getPartnerServiceDetails()
                .isAutoAssignUserEnabled());
        List<RoleSpecificPrice> rolePrices = ctrl.getModel().getRolePrices();
        assertEquals(1, rolePrices.size());
        RoleSpecificPrice rp = rolePrices.get(0);
        assertEquals(new BigDecimal(5), rp.getPrice());
        assertEquals(voPriceModel.getRoleSpecificUserPrices().get(0).getRole(),
                rp.getRole());
    }

    @Test
    public void serviceChanged_Error() throws Exception {
        // given
        ctrl.initializePartnerServiceView();
        ctrl.partnerServices = mock(PartnerService.class);
        doThrow(new ServiceStateException()).when(ctrl.partnerServices)
                .getServiceDetails(3L);

        // when
        ctrl.serviceChanged(new ValueChangeEvent(new UIComponentStub(null),
                null, Long.valueOf(3)));

        // then
        assertEquals(0L, ctrl.getModel().getSelectedServiceKey());
        assertEquals(0L, ctrl.getModel().getPartnerServiceDetails()
                .getServiceKey());
        List<RoleSpecificPrice> rolePrices = ctrl.getModel().getRolePrices();
        assertEquals(new ArrayList<RoleSpecificPrice>(), rolePrices);
    }

    @Test
    public void isRolesRendered() {
        ctrl.initializePartnerServiceView();
        ctrl.getModel().setRolePrices(null);

        boolean rolesRendered = ctrl.isRolesRendered();

        assertEquals(false, rolesRendered);
    }

    @Test
    public void isRolesRendered_RolePrices() {
        ctrl.initializePartnerServiceView();

        boolean rolesRendered = ctrl.isRolesRendered();

        assertEquals(true, rolesRendered);
    }
}
