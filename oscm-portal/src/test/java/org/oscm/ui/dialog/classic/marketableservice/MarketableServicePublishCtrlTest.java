/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.marketableservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.model.User;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.service.POPartner;
import org.oscm.internal.service.POServiceForPublish;
import org.oscm.internal.service.PublishService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author alfschar
 */
@SuppressWarnings("boxing")
public class MarketableServicePublishCtrlTest {

    private static final String MARKETPLACE_ID = "marketplaceId";
    private static final String PLEASE_CHOOSE = "- Please choose - ";

    private final MarketableServicePublishCtrl ms = new MarketableServicePublishCtrl();
    private final SessionBean sessionBean = new SessionBean();
    private UserBean userBean;
    private final Set<OrganizationRoleType> organizationRolesOfUser = new HashSet<OrganizationRoleType>();
    private final PORevenueShare operatorRevenueShare = new PORevenueShare();
    private boolean returnError = false;
    private MarketableServicePublishModel model;
    private final POPartner broker = new POPartner();

    @Before
    public void setup() throws Exception {
        
        new FacesContextStub(Locale.ENGLISH);
        
        operatorRevenueShare.setRevenueShare(BigDecimal.ONE);

        VOUserDetails details = new VOUserDetails();
        details.setOrganizationRoles(organizationRolesOfUser);
        organizationRolesOfUser.add(OrganizationRoleType.SUPPLIER);
        userBean = mock(UserBean.class);
        when(userBean.getUserFromSession()).thenReturn(new User(details));
        ms.pricingService = mock(PricingService.class);
        ms.categorizationService = mock(CategorizationService.class);

        doNothing().when(ms.categorizationService)
                .verifyCategoriesUpdated(anyListOf(VOCategory.class));

        ms.mplService = mock(MarketplaceService.class);
        ms.serviceProvisioning = mock(ServiceProvisioningService.class);

        ms.ui = new UiDelegateStub() {

            @Override
            public String getText(String key, Object... params) {
                return PLEASE_CHOOSE;
            }
        };
        model = new MarketableServicePublishModel();
        model.setSupplier(false);
        ms.setModel(model);
        ms.setUserBean(userBean);
        ms.setSessionBean(sessionBean);

        ms.publishService = new PublishService() {
            @Override
            public Response updateAndPublishService(POServiceForPublish service,
                    List<POResalePermissionDetails> toGrant,
                    List<POResalePermissionDetails> toRevoke)
                            throws ValidationException {
                if (returnError) {
                    throw new ValidationException();
                }
                return new Response();
            }

            @Override
            public Response getServiceDetails(long serviceKey)
                    throws ServiceStateException {
                if (returnError) {
                    throw new ServiceStateException();
                }
                final POServiceForPublish s = new POServiceForPublish();
                s.setService(new VOServiceDetails());
                s.getService().setKey(serviceKey);
                s.setMarketplaceId(MARKETPLACE_ID);
                s.setPartOfUpgradePath(true);
                final POOperatorPriceModel opm = new POOperatorPriceModel();
                opm.setRevenueShare(operatorRevenueShare);
                return new Response(s, opm);
            }

            @Override
            public Response getCategoriesAndRvenueShare(String marketplaceId,
                    String locale) throws ObjectNotFoundException {
                if (returnError) {
                    throw new ObjectNotFoundException();
                }
                return new Response(
                        Arrays.asList(new VOCategory(), new VOCategory()));
            }

            @Override
            public Response getBrokers(long serviceKey) {
                List<POPartner> brokers = new ArrayList<POPartner>();
                broker.setKey(1L);
                broker.setSelected(false);
                brokers.add(broker);
                return new Response(brokers);
            }

            @Override
            public Response getResellers(long serviceKey) {
                return new Response();
            }

            @Override
            public Response getTemplateServices() {
                List<POServiceDetails> result = new ArrayList<POServiceDetails>();
                POServiceDetails poService = new POServiceDetails(3L, 0,
                        "Product 3");
                poService.setOrganizationId("123");
                result.add(new POServiceDetails(1L, 0, "Product 1"));
                result.add(new POServiceDetails(2L, 0, "Product 2"));
                result.add(poService);
                return new Response(result);
            }

        };
    }

    @Test
    public void getInitializePublish() {
        // given

        // when
        ms.getInitializePublish();

        // then
        assertEquals(0L, ms.getModel().getSelectedServiceKey());
        assertEquals("", ms.getModel().getInitialMarketplaceId());
    }

    @Test
    public void getInitializePublish_ServiceSet() {
        // given
        sessionBean.setSelectedServiceKeyForSupplier(Long.valueOf(3));

        // when
        ms.getInitializePublish();

        // then
        assertEquals(3L, ms.getModel().getSelectedServiceKey());
        assertEquals(3L,
                ms.getModel().getServiceDetails().getService().getKey());
        assertEquals(operatorRevenueShare,
                ms.getModel().getOperatorPriceModel().getRevenueShare());
        assertEquals(MARKETPLACE_ID, ms.getModel().getInitialMarketplaceId());
    }

    @Test
    public void getInitializePublish_Error() {
        // given
        ms.getModel().setSelectedServiceKey(3L);
        returnError = true;

        // when
        ms.getInitializePublish();

        // then
        assertEquals(0L, ms.getModel().getSelectedServiceKey());
        assertEquals(0L,
                ms.getModel().getServiceDetails().getService().getKey());
        assertEquals(null,
                ms.getModel().getOperatorPriceModel().getRevenueShare());
        assertFalse(ms.ui.hasErrors());
    }

    @Test
    public void getInitializePublish_ErrorOnSave() {
        // given
        ms.getModel().setSelectedServiceKey(3L);
        returnError = false;
        ms.ui.handleException(new SaaSApplicationException("save failed"));

        // when
        ms.getInitializePublish();

        // then
        assertEquals(0L, ms.getModel().getSelectedServiceKey());
        assertEquals(0L,
                ms.getModel().getServiceDetails().getService().getKey());
        assertEquals(null,
                ms.getModel().getOperatorPriceModel().getRevenueShare());
        assertTrue(ms.ui.hasErrors());
    }

    @Test
    public void initializeModel_handleExceptions() {
        // given
        ms.getModel().setSelectedServiceKey(3L);
        returnError = true;

        // when
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);

        // then
        assertTrue(ms.ui.hasErrors());
    }

    @Test
    public void initializeModel_doNotHandleExceptions() {
        // given
        ms.getModel().setSelectedServiceKey(3L);
        returnError = false;

        // when
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);

        // then
        assertFalse(ms.ui.hasErrors());
    }

    @Test
    public void initializeModel_supplier() {
        // given
        ms.getModel().setSelectedServiceKey(3L);
        returnError = false;

        // when
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);

        // then
        assertTrue(ms.getModel().isSupplier());
        assertTrue(ms.getModel().isOperatorShareVisible());
        assertTrue(ms.getModel().isBrokerShareVisible());
        assertTrue(ms.getModel().isResellerShareVisible());
    }

    @Test
    public void initializeModel_broker() {
        // given
        organizationRolesOfUser.clear();
        organizationRolesOfUser.add(OrganizationRoleType.BROKER);

        ms.getModel().setSelectedServiceKey(3L);
        returnError = false;

        // when
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);

        // then
        assertFalse(ms.getModel().isSupplier());
        assertFalse(ms.getModel().isOperatorShareVisible());
        assertTrue(ms.getModel().isBrokerShareVisible());
        assertFalse(ms.getModel().isResellerShareVisible());
    }

    @Test
    public void initializeModel_reseller() {
        // given
        organizationRolesOfUser.clear();
        organizationRolesOfUser.add(OrganizationRoleType.RESELLER);

        ms.getModel().setSelectedServiceKey(3L);
        returnError = false;

        // when
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);

        // then
        assertFalse(ms.getModel().isSupplier());
        assertFalse(ms.getModel().isOperatorShareVisible());
        assertFalse(ms.getModel().isBrokerShareVisible());
        assertTrue(ms.getModel().isResellerShareVisible());
    }

    @Test
    public void initializeServiceTemplates() {
        // given
        initialPage();
        // when
        ms.initializeServiceTemplates();
        ms.getModel().getServiceTemplates();
        // then
        assertEquals(4, ms.getModel().getServiceTemplates().size());
        assertEquals(Long.valueOf(0L),
                ms.getModel().getServiceTemplates().get(0).getValue());
        assertEquals(Long.valueOf(1L),
                ms.getModel().getServiceTemplates().get(1).getValue());
        assertEquals(Long.valueOf(2L),
                ms.getModel().getServiceTemplates().get(2).getValue());
        assertEquals(Long.valueOf(3L),
                ms.getModel().getServiceTemplates().get(3).getValue());
        assertEquals("Product 3  (123)",
                ms.getModel().getServiceTemplates().get(3).getLabel());
    }

    @Test
    public void setInitializePartnerServiceView() {
        // given

        // when
        ms.setInitializePartnerServiceView();

        // then: no exception
    }

    @Test
    public void serviceChanged() {
        // given
        initialPage();

        // when
        ms.serviceChanged(select(3L));

        // then
        assertEquals(3L, ms.getModel().getSelectedServiceKey());
        assertEquals(3L,
                ms.getModel().getServiceDetails().getService().getKey());
        assertEquals(operatorRevenueShare,
                ms.getModel().getOperatorPriceModel().getRevenueShare());
    }

    private ValueChangeEvent select(Long selectedServiceKey) {
        return new ValueChangeEvent(new UIComponentStub(null), null,
                selectedServiceKey);
    }

    private void initialPage() {
        ms.setModel(new MarketableServicePublishModel());

    }

    @Test
    public void marketplaceChanged() {
        // given
        ms.getModel().setServiceDetails(new POServiceForPublish());

        // when
        ms.marketplaceChanged(
                new ValueChangeEvent(new UIComponentStub(null), null, "123"));

        // then
        assertEquals(2, ms.getModel().getCategorySelection().size());
    }

    @Test
    public void marketplaceChanged_Error() {
        // given
        ms.getModel().setServiceDetails(new POServiceForPublish());
        returnError = true;

        // when
        ms.marketplaceChanged(
                new ValueChangeEvent(new UIComponentStub(null), null, "123"));

        // then
        assertEquals(0, ms.getModel().getCategorySelection().size());
        assertTrue(ms.ui.hasErrors());
    }

    @Test
    public void serviceChanged_Error() {
        // given
        returnError = true;

        // when
        ms.serviceChanged(select(3L));

        // then
        assertEquals(0L, ms.getModel().getSelectedServiceKey());
        assertEquals(0L,
                ms.getModel().getServiceDetails().getService().getKey());
        assertEquals(null,
                ms.getModel().getOperatorPriceModel().getRevenueShare());
        assertTrue(ms.ui.hasErrors());
    }

    @Test
    public void save() throws Exception {
        // given
        initForSave(false);
        when(ms.mplService.getMarketplaceById(anyString()))
                .thenReturn(getMarketplace("Mpl", false));

        // when
        String result = ms.save();

        // then: no exception
        // verify the cache is updated
        assertTrue(ms.getModel().assignedPermissions.get(broker.getKey()));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void save_ConcurrentModificationException() throws Exception {
        // given
        initForSave(false);
        when(ms.mplService.getMarketplaceById(anyString()))
                .thenReturn(getMarketplace("Mpl", false));
        doThrow(new ConcurrentModificationException())
                .when(ms.categorizationService)
                .verifyCategoriesUpdated(anyListOf(VOCategory.class));
        // when
        String result = ms.save();
        // then
        assertFalse(ms.getModel().assignedPermissions.get(broker.getKey()));
        assertEquals(BaseBean.OUTCOME_ERROR, result);
    }

    @Test
    public void save_Error() throws Exception {
        // given
        initForSave(false);
        returnError = true;
        when(ms.mplService.getMarketplaceById(anyString()))
                .thenReturn(getMarketplace("Mpl", false));
        // when
        ms.save();

        // then: no exception
        // verify the cache is not updated
        assertFalse(ms.getModel().assignedPermissions.get(broker.getKey()));
    }

    @Test
    public void save_ResellerIsNull() throws Exception {
        // given
        initForSave(true);
        when(ms.mplService.getMarketplaceById(anyString()))
                .thenReturn(getMarketplace("Mpl", false));
        // when
        ms.save();

        // then: no exception
    }

    @Test
    public void synchronizeUIWithObjects() {
        // when
        ms.synchronizeUIWithObjects();

        // then
        assertNull(ms.getModel().getServiceDetails());
    }

    @Test
    public void synchronizeUIWithObjects_DataSet() {
        // given
        ms.getModel().setServiceDetails(new POServiceForPublish());
        ms.getModel().initializeMarketplaceCategories(getVOCategoryList());
        ms.getModel().getCategorySelection().get(0).setSelected(true);

        // when
        ms.synchronizeUIWithObjects();

        // then
        assertNotNull(ms.getModel().getServiceDetails().getCatalogEntry());
        assertEquals(1, ms.getModel().getServiceDetails().getCatalogEntry()
                .getCategories().size());
        assertEquals(
                ms.getModel().getCategorySelection().get(0).getCategory()
                        .getKey(),
                ms.getModel().getServiceDetails().getCatalogEntry()
                        .getCategories().get(0).getKey());
    }

    @Test
    public void getChangedAndSelectedCategories_NoCategoryChanged() {
        // given
        List<VOCategory> categories1 = getVOCategoryList();

        // when
        List<VOCategory> result = ms
                .getChangedAndSelectedCategories(categories1, categories1);

        // then
        assertEquals(categories1.size(), result.size());
    }

    @Test
    public void getChangedAndSelectedCategories() {
        // given
        List<VOCategory> categories1 = getVOCategoryList();
        List<VOCategory> categories2 = getVOCategoryList();
        categories2.add(new VOCategory());
        // when
        List<VOCategory> result = ms
                .getChangedAndSelectedCategories(categories1, categories2);

        // then
        assertEquals((categories1.size() + 1), result.size());
    }

    @Test
    public void saveWithRestrictedMpl() throws Exception {
        // given
        initForSave(false);
        when(ms.mplService.getMarketplaceById(anyString()))
                .thenReturn(getMarketplace("Mpl", true));
        when(ms.serviceProvisioning
                .getServiceCustomerTemplates(any(VOService.class)))
                        .thenReturn(getVOCustomerServiceList());
        // when
        String result = ms.save();

        // then
        verify(ms.serviceProvisioning, times(1))
                .getServiceCustomerTemplates(any(VOService.class));
        verify(ms.mplService, times(2)).doesOrganizationHaveAccessMarketplace(
                anyString(), anyString());

        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    private void initForSave(boolean resellerIsNull) throws Exception {
        ms.getModel().setSelectedServiceKey(3L);
        ms.initializeModel(ms.getModel().getSelectedServiceKey(), null, true);
        assertFalse(ms.getModel().assignedPermissions.get(broker.getKey()));

        ms.getModel().setServiceDetails(new POServiceForPublish());
        List<POPartner> brokers = new ArrayList<POPartner>();
        broker.setSelected(true);
        brokers.add(broker);
        ms.getModel().setBrokers(brokers);

        List<POPartner> reseller = new ArrayList<POPartner>();
        if (resellerIsNull) {
            reseller = null;
        }
        ms.getModel().setResellers(reseller);

        mockGetMarketplaceRevenueShares();
        mockGetOperatorRevenueShare();
        mockGetPartnerRevenueShareForService();
        mockGetPartnerRevenueSharesForMarketplace();
    }

    private void mockGetMarketplaceRevenueShares() throws Exception {
        POMarketplacePriceModel marketplacePricing = new POMarketplacePriceModel();

        Response response = mock(Response.class);
        when(response.getResult(POMarketplacePriceModel.class))
                .thenReturn(marketplacePricing);
        when(ms.pricingService.getMarketplaceRevenueShares(anyString()))
                .thenReturn(response);
    }

    private void mockGetOperatorRevenueShare() throws Exception {
        POOperatorPriceModel partnerPriceModel = new POOperatorPriceModel();

        Response response = mock(Response.class);
        when(response.getResult(POOperatorPriceModel.class))
                .thenReturn(partnerPriceModel);
        when(ms.pricingService.getOperatorRevenueShare(3L))
                .thenReturn(response);
    }

    private void mockGetPartnerRevenueShareForService() throws Exception {
        POPartnerPriceModel partnerPriceModel = new POPartnerPriceModel();

        Response response = mock(Response.class);
        when(response.getResult(POPartnerPriceModel.class))
                .thenReturn(partnerPriceModel);

        POServiceForPricing service = new POServiceForPricing(3L, 0);
        when(ms.pricingService.getPartnerRevenueShareForService(service))
                .thenReturn(response);
    }

    private void mockGetPartnerRevenueSharesForMarketplace() throws Exception {
        POPartnerPriceModel marketplacePricing = new POPartnerPriceModel();

        Response response = mock(Response.class);
        when(response.getResult(POPartnerPriceModel.class))
                .thenReturn(marketplacePricing);
        when(ms.pricingService
                .getPartnerRevenueSharesForMarketplace(anyString()))
                        .thenReturn(response);
    }

    private List<VOCategory> getVOCategoryList() {
        final List<VOCategory> list = new ArrayList<VOCategory>();
        list.add(new VOCategory());
        list.get(0).setCategoryId("xyz");
        list.get(0).setKey(1L);
        list.add(new VOCategory());
        list.get(1).setCategoryId("abc");
        list.get(1).setKey(2L);
        return list;
    }

    private List<VOCustomerService> getVOCustomerServiceList() {
        final List<VOCustomerService> list = new ArrayList<VOCustomerService>();
        list.add(new VOCustomerService());
        list.add(new VOCustomerService());
        return list;
    }

    private VOMarketplace getMarketplace(String mplId, boolean restricted) {

        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setMarketplaceId(mplId);
        marketplace.setRestricted(restricted);

        return marketplace;
    }
}
