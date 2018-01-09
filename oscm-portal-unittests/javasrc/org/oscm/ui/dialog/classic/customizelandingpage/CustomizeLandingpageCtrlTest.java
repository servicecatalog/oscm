/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 29.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationService;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationServiceBean;
import org.oscm.internal.landingpageconfiguration.POPublicLandingpageConfig;
import org.oscm.internal.landingpageconfiguration.POService;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * @author weiser
 * 
 */
public class CustomizeLandingpageCtrlTest {

    private static final String TEXT = "text";
    private static final String PUBLIC_MARKETPLACE_ID = "marketplace_public";
    private static final String ENTERPRISE_MARKETPLACE_ID = "marketplace_enterprise";
    private static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";

    private CustomizeLandingpageCtrl ctrl;
    private CustomizeLandingpageModel model;

    @Before
    public void setup() {
        ctrl = spy(new CustomizeLandingpageCtrl());

        ctrl.ui = new UiDelegateStub() {
            @Override
            public String getText(String key, Object... params) {
                return TEXT;
            }
        };

        model = spy(new CustomizeLandingpageModel());
        ctrl.setModel(model);

        ctrl.configureLandingpage = new LandingpageConfigurationServiceBean();
    }

    @Test
    public void isDefaultLandingpageSelected_Public()
            throws ObjectNotFoundException {

        // given
        given(
                landingpageConfigurationService().loadLandingpageType(
                        "any_mp_id")).willReturn(LandingpageType.PUBLIC);

        // when
        boolean result = ctrl.isPublicLandingpageActivated("any_mp_id");

        // then
        assertTrue(result);
    }

    /**
     * Enterprise landing page is activated
     */
    @Test
    public void isDefaultLandingpageSelected_Enterprise()
            throws ObjectNotFoundException {

        // given
        given(
                landingpageConfigurationService().loadLandingpageType(
                        "any_mp_id")).willReturn(LandingpageType.ENTERPRISE);

        // when
        boolean result = ctrl.isPublicLandingpageActivated("any_mp_id");

        // then
        assertFalse(result);
    }

    /**
     * 
     */
    @Test
    public void getInitializeCustomizeLandingpage() {
        // given not initialized model
        model.setInitialized(false);
        model.setLandingpageType("any_mp_id");
        given(landingpageConfigurationService().getMarketplaceSelections())
                .willReturn(new ArrayList<POMarketplace>());

        // when
        ctrl.getInitializeCustomizeLandingpage();

        // then
        assertTrue(model.isInitialized());
        // but not landing page type
        // and not the rest
    }

    @Test
    public void switchToEnterpriseLandingpage() {

        // given some model for the public landing page
        model.setLandingpageConfig(new POPublicLandingpageConfig());
        model.setAvailableServices(new ArrayList<POService>());
        model.setFillinItems(new ArrayList<SelectItem>());

        // when switching
        ctrl.switchToEnterpriseLandingpage();

        // than public landing page model is reset
        assertNull(model.getLandingpageConfig());
        assertNull(model.getAvailableServices());
        assertNull(model.getFillinItems());
    }

    /**
     * Some controls for the public landing page are not shown in case of
     * enterprise landing page selected
     */
    @Test
    public void isHideManualChoicePanels_EnterpriseSelected() {

        // given
        model.setLandingpageType(LandingpageType.ENTERPRISE.name());

        // than
        assertTrue(ctrl.isHideManualChoicePanels());
    }

    /**
     * Some controls for the public landing page are not shown in case of
     * enterprise landing page selected
     */
    @Test
    public void isHideManualChoicePanels_PublicSelected() {

        // given public landingpage selected
        model.setLandingpageType(LandingpageType.PUBLIC.name());

        // than
        assertFalse(ctrl.isHideManualChoicePanels());
    }

    /**
     * Test if the default landingpage data is loaded
     * 
     */
    @Test
    public void resetToDefault() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, OperationNotPermittedException {

        // given a selected marketple
        model.setSelectedMarketplace("any_mp_id");
        when(landingpageConfigurationService().resetLandingPage("any_mp_id"))
                .thenReturn(anyResponse());

        // when
        ctrl.resetToDefault();

        // then
        verify(ctrl.configureLandingpage, times(1)).resetLandingPage(
                "any_mp_id");
    }

    /**
     * Test if the public landingpage data is loaded
     * 
     */
    @Test
    public void loadPublicLandingpage() throws Exception {

        // given public landing page is selected
        when(
                landingpageConfigurationService().loadPublicLandingpageConfig(
                        "any_mp_id")).thenReturn(anyResponse());

        // when
        ctrl.loadPublicLandingpage("any_mp_id");

        // then
        verify(ctrl.configureLandingpage, times(1))
                .loadPublicLandingpageConfig("any_mp_id");
    }

    /**
     * Test if the public landingpage data is saved
     */
    @Test
    public void save_PublicLandingpage() throws Exception {

        // given a selected public landing page
        setPublicLandingpageSelected();
        given(
                landingpageConfigurationService().savePublicLandingpageConfig(
                        any(POPublicLandingpageConfig.class))).willReturn(
                emptyResponse());

        // when
        ctrl.save();

        // than
        verify(ctrl.configureLandingpage, times(1))
                .savePublicLandingpageConfig(new POPublicLandingpageConfig());
    }

    Response emptyResponse() {
        Response response = new Response();
        response.getResults().add(new POPublicLandingpageConfig());
        response.getResults().add(new ArrayList<POService>());
        return response;
    }

    Response anyResponse() {
        return emptyResponse();
    }

    private void setPublicLandingpageSelected() {
        model.setLandingpageType(LandingpageType.PUBLIC.name());
        model.setSelectedMarketplace(PUBLIC_MARKETPLACE_ID);
        model.setLandingpageConfig(new POPublicLandingpageConfig());
        model.setAvailableServices(new ArrayList<POService>());
        model.setFillinItems(new ArrayList<SelectItem>());
    }

    /**
     * Test if the enterprise landingpage data is saved
     */
    @Test
    public void save_EnterpriseLandingpage() throws Exception {

        // given a selected enterprise landing page
        setEntpriseLandingpageSelected();
        given(
                landingpageConfigurationService()
                        .saveEnterpriseLandingpageConfig("any_id")).willReturn(
                emptyResponse());

        // when
        ctrl.save();

        // than
        verify(ctrl.configureLandingpage, times(1))
                .saveEnterpriseLandingpageConfig(ENTERPRISE_MARKETPLACE_ID);
    }

    private String setEntpriseLandingpageSelected() {
        model.setLandingpageType(LandingpageType.ENTERPRISE.name());
        model.setSelectedMarketplace(ENTERPRISE_MARKETPLACE_ID);
        model.setLandingpageConfig(null);
        model.setAvailableServices(null);
        model.setFillinItems(null);

        return ENTERPRISE_MARKETPLACE_ID;
    }

    /**
     * Update public landing page model with data loaded from server
     */
    @Test
    public void updateModelLandingpageConfig() {

        // given loaded data from server
        List<POService> services = Arrays.asList(new POService());
        POPublicLandingpageConfig publicLandingpageConfig = new POPublicLandingpageConfig();
        publicLandingpageConfig.setFeaturedServices(services);

        // when
        ctrl.updateModelLandingpageConfig(publicLandingpageConfig);

        // then model is updated
        assertEquals(services, model.getFeaturedServices());
    }

    /**
     * Update public landing page model with data loaded from server
     */
    @Test
    public void updateModelAvailableServices() {

        // given loaded data from server
        List<POService> list = Arrays.asList(new POService());

        // when
        ctrl.updateModelAvailableServices(list);

        // then model is updated
        assertEquals(list, model.getAvailableServices());
    }

    /**
     * All UI fields are disabled if no marketplace is selected
     */
    @Test
    public void isFieldsDisabled() {

        // given
        model.setSelectedMarketplace(null);

        // than
        assertTrue(ctrl.isFieldsDisabled());
    }

    /**
     * Enable fields after mp is selected
     */
    @Test
    public void isFieldsDisabled_Public() {

        // given
        model.setSelectedMarketplace("any_mp_id");

        // than
        assertFalse(ctrl.isFieldsDisabled());
    }

    /**
     * test that landingpageconfig is updated to the model
     */
    @Test
    public void switchtToPublicLandingpage() {
        // given
        Response response = givenPublicLandingpageResponse();

        // when
        ctrl.switchtToPublicLandingpage(response);

        // than
        assertEquals(response.getResult(POPublicLandingpageConfig.class),
                model.getLandingpageConfig());
        assertEquals(response.getResultList(POService.class),
                model.getAvailableServices());
        assertEquals(FillinCriterion.ACTIVATION_DESCENDING, model
                .getFillinItems().get(0).getValue());
    }

    /**
     * Test the building of fillin itmes for the UI
     */
    @Test
    public void buildFillinItems() {
        // given
        List<FillinCriterion> fillinOptions = new ArrayList<FillinCriterion>();
        fillinOptions.add(FillinCriterion.ACTIVATION_DESCENDING);

        // when
        List<SelectItem> result = ctrl.buildFillinItems(fillinOptions);

        // than
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test that the fillinOptins are stored to the model
     */
    @Test
    public void loadFillinOptions() {
        // given
        List<FillinCriterion> fillinOptions = new ArrayList<FillinCriterion>();
        fillinOptions.add(FillinCriterion.ACTIVATION_DESCENDING);
        LandingpageConfigurationService service = landingpageConfigurationService();
        given(service.getFillinOptions("any_id")).willReturn(fillinOptions);

        // when
        ctrl.loadFillinOptions("any_id");

        // than
        assertEquals(FillinCriterion.ACTIVATION_DESCENDING, model
                .getFillinItems().get(0).getValue());
    }

    @Test
    public void initNumOfServicesRange() {
        // given
        LandingpageConfigurationService service = landingpageConfigurationService();
        List<Integer> result = Arrays.asList(new Integer(1));
        given(service.getNumOfServicesRange()).willReturn(result);

        // when
        ctrl.initNumOfServicesRange();

        // than
        assertNotNull(model.getNumOfServicesRange());
        assertEquals("1", (model.getNumOfServicesRange().get(0)).getValue());
    }

    /**
     * @return
     */
    private LandingpageConfigurationService landingpageConfigurationService() {
        LandingpageConfigurationService service = mock(LandingpageConfigurationService.class);
        ctrl.configureLandingpage = service;
        return service;
    }

    /**
     * @return
     */
    private Response givenPublicLandingpageResponse() {
        Response r = new Response();
        r.getResults().add(new POPublicLandingpageConfig());
        r.getResults().add(new ArrayList<POService>());
        r.getResults()
                .add(Arrays.asList(FillinCriterion.ACTIVATION_DESCENDING));
        return r;
    }

    @Test
    public void adaptServiceNames_NullName() {
        List<POService> list = Arrays.asList(new POService());

        list = ctrl.adaptServiceNames(list);

        assertEquals(TEXT, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames_EmptyName() {
        POService s = new POService();
        s.setServiceName("  ");
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(TEXT, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames() {
        String serviceName = "serviceName";
        POService s = new POService();
        s.setServiceName(serviceName);
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(serviceName, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames_WithBlank() {
        String serviceName = "service Name";
        POService s = new POService();
        s.setServiceName(serviceName);
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(serviceName, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames_WithTab() {
        String serviceName = "service\tName";
        POService s = new POService();
        s.setServiceName(serviceName);
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(serviceName, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames_WithCtrlChar() {
        String serviceName = "service\nName";
        POService s = new POService();
        s.setServiceName(serviceName);
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(TEXT, list.get(0).getServiceName());
    }

    @Test
    public void adaptServiceNames_WithCtrlChar2() {
        String serviceName = "service\r\nName";
        POService s = new POService();
        s.setServiceName(serviceName);
        List<POService> list = Arrays.asList(s);

        list = ctrl.adaptServiceNames(list);

        assertEquals(TEXT, list.get(0).getServiceName());
    }

    @Test
    public void checkServiceName() {
        String serviceName = "serviceName";
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.checkServiceName(serviceName)));
    }

    @Test
    public void checkServiceName_WithBlank() {
        String serviceName = "service Name";
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.checkServiceName(serviceName)));
    }

    @Test
    public void checkServiceName_WithTab() {
        String serviceName = "service\tName";
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.checkServiceName(serviceName)));
    }

    @Test
    public void checkServiceName_WithCtrlChar() {
        String serviceName = "service\nName";
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(ctrl.checkServiceName(serviceName)));
    }

    @Test
    public void checkServiceName_WithCtrlChar2() {
        String serviceName = "service\r\nName";
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(ctrl.checkServiceName(serviceName)));
    }

    @Test
    public void marketplaceChanged_noSelection() throws Exception {
        // given
        model.setSelectedMarketplace(null);
        given(landingpageConfigurationService().getMarketplaceSelections())
                .willReturn(new ArrayList<POMarketplace>());
        // when
        String result = ctrl.marketplaceChanged();

        // then
        assertEquals("", result);
        verify(ctrl, times(1)).resetModel();
    }

    @Test
    public void marketplaceChanged_concurrentChanged() throws Exception {
        // given
        model.setSelectedMarketplace("mp");
        LandingpageConfigurationService configureLandingpage = landingpageConfigurationService();
        doThrow(new ObjectNotFoundException()).when(configureLandingpage)
                .loadLandingpageType(anyString());

        // when
        String result = ctrl.marketplaceChanged();

        // then
        assertEquals(CONCURRENT_MODIFICATION_ERROR, result);
        verify(ctrl, times(1)).resetModel();

    }
}
