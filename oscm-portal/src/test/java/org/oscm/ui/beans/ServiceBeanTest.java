/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 18.05.2011                                                      
 *                                                                              
 *  Completion Time: 18.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceDetails;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceLocalization;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class ServiceBeanTest {

    private ServiceBean bean;
    private String storedMarketplaceId;

    private MarketplaceBean mb;
    private MenuBean menuBean;
    private ServiceProvisioningService sps;
    private final VOServiceDetails resultServiceDetails = new VOServiceDetails();

    @Before
    public void setup() throws Exception {
        VOTechnicalService ts = new VOTechnicalService();
        ts.setKey(1234);
        ts.setTechnicalServiceDescription("technicalServiceDescription");
        ts.setTechnicalServiceId("id");
        List<VOTechnicalService> list = new ArrayList<VOTechnicalService>();
        list.add(ts);

        resultServiceDetails.setKey(1);
        resultServiceDetails.setTechnicalService(ts);
        resultServiceDetails.setAutoAssignUserEnabled(true);
        resultServiceDetails.setConfiguratorUrl("configUrl");
        resultServiceDetails.setServiceId("serviceId");

        sps = mock(ServiceProvisioningService.class);
        when(sps.getServiceDetails(any(VOService.class))).thenReturn(
                resultServiceDetails);
        when(sps.getTechnicalServices(any(OrganizationRoleType.class)))
                .thenReturn(list);
        when(Boolean.valueOf(sps.isPartOfUpgradePath(any(VOService.class))))
                .thenReturn(Boolean.TRUE);
        when(
                sps.createService(any(VOTechnicalService.class),
                        any(VOService.class), any(VOImageResource.class)))
                .thenReturn(resultServiceDetails);

        when(sps.copyService(any(VOService.class), anyString())).thenReturn(
                resultServiceDetails);
        when(
                sps.updateService(any(VOServiceDetails.class),
                        any(VOImageResource.class))).thenReturn(
                resultServiceDetails);

        final VOService s = new VOService();
        s.setKey(1);

        bean = spy(new ServiceBean());
        doReturn(sps).when(bean).getProvisioningService();
        doReturn(Arrays.asList(s)).when(bean).getServices();

        bean.setSessionBean(new SessionBean());

        storedMarketplaceId = null;
        menuBean = mock(MenuBean.class);
        mb = mock(MarketplaceBean.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                storedMarketplaceId = (String) args[0];
                return null;
            }
        }).when(mb).setMarketplaceId(anyString());

        bean.setMarketplaceBean(mb);
        when(
                mb.publishService(any(ServiceDetails.class),
                        anyListOf(VOCategory.class))).thenReturn(
                resultServiceDetails);
        bean.setMenuBean(menuBean);
        FacesContextStub facesContextStub = new FacesContextStub(Locale.ENGLISH);

        UIViewRoot viewRoot = mock(UIViewRoot.class);
        when(viewRoot.getChildren()).thenReturn(new ArrayList<UIComponent>());
        facesContextStub.setViewRoot(viewRoot);
    }

    @Test
    public void testSetSelectedTechServiceKey_DescriptionInitialization()
            throws Exception {
        VOTechnicalService ts = bean.getAvailableTechServices().get(0);
        bean.setSelectedTechServiceKey(ts.getKey());
        ServiceDetails service = bean.getServiceForCreation();
        Assert.assertEquals(ts.getTechnicalServiceDescription(),
                service.getDescription());
    }

    @Test
    public void testSetSelectedServiceKey() throws Exception {
        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setMarketplaceId("mp1");

        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setAnonymousVisible(true);
        entry.setMarketplace(marketplace);

        List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
        entries.add(entry);

        when(mb.getMarketplacesForService(any(VOServiceDetails.class)))
                .thenReturn(entries);

        bean.setSelectedServiceKey(1);
        assertEquals(1, bean.getSelectedService().getKey());
        assertEquals(true, bean.getSelectedService().isPublicService());
        assertEquals("mp1", storedMarketplaceId);
    }

    @Test
    public void testSetSelectedServiceKey_noCatalogEntry() throws Exception {

        List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();

        when(mb.getMarketplacesForService(any(VOServiceDetails.class)))
                .thenReturn(entries);
        bean.setSelectedServiceKey(1);
        assertEquals(true, bean.getSelectedService().isPublicService());
        assertEquals(null, storedMarketplaceId);
    }

    @Test
    public void testSetSelectedServiceKey_NotPublicCatalog() throws Exception {
        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setMarketplaceId("mp1");

        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setAnonymousVisible(false);
        entry.setMarketplace(marketplace);

        List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
        entries.add(entry);

        when(mb.getMarketplacesForService(any(VOServiceDetails.class)))
                .thenReturn(entries);
        bean.setSelectedServiceKey(1);
        assertEquals(false, bean.getSelectedService().isPublicService());
        assertEquals("mp1", storedMarketplaceId);
    }

    @Test
    public void testSetSelectedServiceKey_noMarketplaceAndPublicCatalog()
            throws Exception {
        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setAnonymousVisible(true);
        entry.setMarketplace(null);

        List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
        entries.add(entry);

        when(mb.getMarketplacesForService(any(VOServiceDetails.class)))
                .thenReturn(entries);
        bean.setSelectedServiceKey(1);
        assertEquals(true, bean.getSelectedService().isPublicService());
        assertEquals(null, storedMarketplaceId);
    }

    @Test
    public void testSetSelectedServiceKey_noMarketplaceAndNotPublicCatalog()
            throws Exception {

        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setAnonymousVisible(false);
        entry.setMarketplace(null);

        List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
        entries.add(entry);

        when(mb.getMarketplacesForService(any(VOServiceDetails.class)))
                .thenReturn(entries);
        bean.setSelectedServiceKey(1);
        assertEquals(false, bean.getSelectedService().isPublicService());
        assertEquals(null, storedMarketplaceId);
    }

    @Test
    public void isPartOfUpgradePath() throws Exception {
        bean.setSelectedServiceKey(1);
        verify(sps, times(1)).getServiceDetails(any(VOService.class));
        boolean b = bean.isPartOfUpgradePath();
        assertTrue(b);
        verify(sps, times(1)).isPartOfUpgradePath(any(VOService.class));

        b = bean.isPartOfUpgradePath();
        assertTrue(b);
        verifyNoMoreInteractions(sps);
    }

    @Test
    public void isPartOfUpgradePath_NoServiceSelected() throws Exception {
        boolean b = bean.isPartOfUpgradePath();
        assertFalse(b);
        verifyNoMoreInteractions(sps);
    }

    /**
     * We have a customer service key that is not in the service list of the
     * ones that can be edited in 'edit service'
     * 
     * @throws Exception
     */
    @Test
    public void getSelectedService_CustomerServiceInSession() throws Exception {
        VOService svc = new VOService();
        svc.setKey(1234);

        SessionBean sessionBean = new SessionBean();
        sessionBean.setSelectedServiceKeyForSupplier(Long.valueOf(4321));

        ServiceBean sb = spy(new ServiceBean());
        sb.setSessionBean(sessionBean);

        doReturn(Collections.singletonList(svc)).when(sb).getServiceNames();
        doNothing().when(sb).setSelectedServiceKey(anyLong());

        sb.getSelectedService();

        verify(sb, never()).setSelectedServiceKey(anyLong());
        assertNull(sessionBean.getSelectedServiceKeyForSupplier());
    }

    @Test
    public void getSelectedService_TemplateInSession() throws Exception {
        VOService svc = new VOService();
        svc.setKey(1234);

        SessionBean sessionBean = new SessionBean();
        sessionBean
                .setSelectedServiceKeyForSupplier(Long.valueOf(svc.getKey()));

        ServiceBean sb = spy(new ServiceBean());
        sb.setSessionBean(sessionBean);

        doReturn(Collections.singletonList(svc)).when(sb).getServiceNames();
        doNothing().when(sb).setSelectedServiceKey(anyLong());

        sb.getSelectedService();

        verify(sb, times(1)).setSelectedServiceKey(eq(svc.getKey()));
        assertNotNull(sessionBean.getSelectedServiceKeyForSupplier());
        assertEquals(Long.valueOf(svc.getKey()),
                sessionBean.getSelectedServiceKeyForSupplier());
    }

    /**
     * The following unit tests test the isShowConfirm() method which is used to
     * display a confirmation dialog in case the selected service and the
     * service to upgrade have both a "per time unit" price model (see bug #9949
     * for more details)
     * 
     * @throws Exception
     */
    @Test
    public void isShowConfirm_NullSelectedService() throws Exception {
        // given a null selectedService

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void isShowConfirm_NullCompatibleServices() throws Exception {
        // given a service which is per time unit
        // and the service to upgrade pro rata
        initSelectedService(PriceModelType.PER_UNIT);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void isShowConfirm_FreeSelectedService() throws Exception {
        // given a selected service which is free of charge
        // and the service to upgrade per unit
        initSelectedService(PriceModelType.FREE_OF_CHARGE);

        initUpgradeService(PriceModelType.PER_UNIT, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void isShowConfirm_FreeUpgradeService() throws Exception {
        // given a selected service which is per time unit
        // and the service to upgrade free of charge
        initSelectedService(PriceModelType.PER_UNIT);

        initUpgradeService(PriceModelType.FREE_OF_CHARGE, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void isShowConfirm_ProRataSelectedService() throws Exception {
        // given a selected service which is pro rata
        // and the service to upgrade per unit
        initSelectedService(PriceModelType.PRO_RATA);

        initUpgradeService(PriceModelType.PER_UNIT, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then show confirmation dialog
        assertTrue(isShowConfirm);
    }

    @Test
    public void isShowConfirm_ProRataUpgradeService() throws Exception {
        // given a selected service which is per time unit
        // and the service to upgrade pro rata
        initSelectedService(PriceModelType.PER_UNIT);

        initUpgradeService(PriceModelType.PRO_RATA, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then show confirmation dialog
        assertTrue(isShowConfirm);
    }

    @Test
    public void isShowConfirm_BothServicesProRata() throws Exception {
        // given a selected service which is pro rata
        // and the service to upgrade pro rata
        initSelectedService(PriceModelType.PRO_RATA);

        initUpgradeService(PriceModelType.PRO_RATA, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void isShowConfirm_PerUnitUpgradeService() throws Exception {
        // given a selected service which is per time unit
        // and the service to upgrade also per time unit
        initSelectedService(PriceModelType.PER_UNIT);

        initUpgradeService(PriceModelType.PER_UNIT, true);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then show confirmation dialog
        assertTrue(isShowConfirm);
    }

    @Test
    public void isShowConfirm_PerUnitUpgradeServiceNotSelected()
            throws Exception {
        // given a selected service which is per time unit
        // and the service to upgrade also per time unit, but which is not
        // selected
        initSelectedService(PriceModelType.PER_UNIT);

        initUpgradeService(PriceModelType.PER_UNIT, false);

        // when
        boolean isShowConfirm = bean.isShowConfirm();

        // then do not show confirmation dialog
        assertFalse(isShowConfirm);
    }

    @Test
    public void createService() throws Exception {
        // given
        bean.setSelectedTechServiceKey(1234L);
        // when
        String result = bean.create();

        // then
        verify(sps, times(1)).createService(any(VOTechnicalService.class),
                any(VOService.class), any(VOImageResource.class));
        assertEquals("success", result);
        assertEquals(true, bean.getSelectedService().isAutoAssignUserEnabled());
        assertEquals(resultServiceDetails.getConfiguratorUrl(), bean
                .getSelectedService().getConfiguratorUrl());
    }

    @Test
    public void copyService() throws Exception {
        // given
        initSelectedService(PriceModelType.PER_UNIT);

        // when
        String result = bean.copy();

        // then
        verify(sps, times(1)).copyService(any(VOService.class), anyString());
        assertEquals("success", result);
        assertEquals(true, bean.getSelectedService().isAutoAssignUserEnabled());
    }

    @Test
    public void updateService() throws Exception {
        // given
        initSelectedService(PriceModelType.PER_UNIT);

        // when
        String result = bean.update();

        // then
        verify(sps, times(1)).updateService(any(VOServiceDetails.class),
                any(VOImageResource.class));
        assertEquals("success", result);
        assertEquals(true, bean.getSelectedService().isAutoAssignUserEnabled());
        assertEquals(resultServiceDetails.getConfiguratorUrl(), bean
                .getSelectedService().getConfiguratorUrl());
    }

    @Test
    public void getLocalization() throws Exception {
        // given
        VOServiceDetails details = new VOServiceDetails();
        details.setName("test");
        VOServiceLocalization localization = new VOServiceLocalization();
        initSelectedServiceAndLocalization(localization, details);

        // when
        localization = bean.getLocalization();

        // when
        assertEquals(localization.getDescriptions().get(1).getLocale(), "de");
        assertEquals(localization.getNames().get(1).getLocale(), "de");
        assertEquals(localization.getShortDescriptions().get(1).getLocale(),
                "de");
    }

    @Test
    public void localize_SetLocalizationToNull() throws Exception {
        // given

        VOServiceDetails details = new VOServiceDetails();
        details.setName("test");
        VOServiceLocalization localization = new VOServiceLocalization();
        bean.setSelectedServiceKey(1234);
        initSelectedServiceAndLocalization(localization, details);

        // when
        bean.localize();

        // when
        verify(sps, times(1)).getServiceLocalization(eq(details));
    }

    private void initSelectedService(PriceModelType type) {
        VOService voSelectedService = new VOService();
        voSelectedService.setKey(1234);
        voSelectedService.setAutoAssignUserEnabled(true);
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(type);

        bean.setSelectedServiceKey(1234);
        bean.getSelectedService().setPriceModel(priceModel);
    }

    private void initUpgradeService(PriceModelType type, boolean selected) {
        VOService voServicePerUnit = new VOService();
        voServicePerUnit.setKey(1234);

        VOPriceModel priceModelPerUnit = new VOPriceModel();
        priceModelPerUnit.setType(type);
        voServicePerUnit.setPriceModel(priceModelPerUnit);

        Service serviceToUpgrade = new Service(voServicePerUnit);
        serviceToUpgrade.setSelected(selected);

        doReturn(Collections.singletonList(serviceToUpgrade)).when(bean)
                .getPossibleCompatibleServices();
    }

    private void initSelectedServiceAndLocalization(
            VOServiceLocalization localization, VOServiceDetails details)
            throws Exception {
        ServiceDetails serviceDetails = mock(ServiceDetails.class);
        doReturn(serviceDetails).when(bean).getSelectedService();
        doReturn(details).when(serviceDetails).getVoServiceDetails();

        List<VOLocalizedText> voDescrptionLocalizedText = new ArrayList<VOLocalizedText>();
        voDescrptionLocalizedText.add(new VOLocalizedText("en", "descrption"));
        localization.setDescriptions(voDescrptionLocalizedText);
        List<VOLocalizedText> voNamesLocalizedText = new ArrayList<VOLocalizedText>();
        voNamesLocalizedText.add(new VOLocalizedText("en", "names"));
        localization.setNames(voNamesLocalizedText);
        List<VOLocalizedText> voShortsLocalizedText = new ArrayList<VOLocalizedText>();
        voShortsLocalizedText.add(new VOLocalizedText("en", "shortDescs"));
        localization.setShortDescriptions(voShortsLocalizedText);

        doReturn(localization).when(sps).getServiceLocalization(details);

        List<Locale> localeList = new ArrayList<Locale>();
        localeList.add(Locale.ENGLISH);
        localeList.add(Locale.GERMAN);

        bean.appBean = mock(ApplicationBean.class);
        doReturn(localeList).when(bean.appBean).getSupportedLocaleList();
    }

}
