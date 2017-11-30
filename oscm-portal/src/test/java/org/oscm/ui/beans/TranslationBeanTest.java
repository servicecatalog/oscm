/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 09.06.2011                                                      
 *                                                                              
 *  Completion Time: 10.06.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.faces.application.FacesMessage.Severity;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.test.BaseAdmUmTest;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.ui.stubs.UIComponentStub;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.operatorservice.LocalizedDataService;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;

/**
 * @author weiser
 * 
 */
public class TranslationBeanTest {

    private static final String JA = "ja";
    private static final String DE = "de";
    private static final String EN = "en";
    private static final String KEY_IMPRINT_DESCRIPTION = "public.imprint.url.description";

    private TranslationBean tb;
    private FacesContextStub context;
    private UIComponentStub component;

    protected List<VOLocalizedText> localizations;

    private BrandService bsMock;
    private MarketplaceService msMock;
    private LocalizedDataService lsMock;

    @Before
    public void setup() throws Exception {
        localizations = new ArrayList<VOLocalizedText>();
        localizations.add(new VOLocalizedText(EN, "stage_en"));
        localizations.add(new VOLocalizedText(DE, "stage_de"));

        tb = spy(new TranslationBean());

        bsMock = mock(BrandService.class);
        lsMock = mock(LocalizedDataService.class);
        doReturn(localizations).when(bsMock).getMarketplaceStageLocalization(
                anyString());

        msMock = mock(MarketplaceService.class);

        doAnswer(new Answer<List<VOMarketplace>>() {

            public List<VOMarketplace> answer(InvocationOnMock invocation)
                    throws Throwable {
                VOMarketplace mp = new VOMarketplace();
                mp.setMarketplaceId(BaseAdmUmTest.GLOBAL_MARKETPLACE_NAME);
                return Arrays.asList(new VOMarketplace[] { mp });
            }

        }).when(msMock).getMarketplacesOwned();

        doReturn(bsMock).when(tb).getBrandManagementService();
        doReturn(msMock).when(tb).getMarketplaceService();
        doReturn(lsMock).when(tb).getLocalizedDataService();
        doNothing().when(tb).addMessage(anyString(), any(Severity.class),
                anyString());

        tb.setLocale(EN);

        MarketplaceBean mb = mock(MarketplaceBean.class);
        doReturn("FUJITSU").when(mb).getMarketplaceId();
        tb.setMarketplaceBean(mb);

        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);

        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
        ((ApplicationStub) context.getApplication())
                .setResourceBundleStub(resourceBundleStub);
        resourceBundleStub.addResource(KEY_IMPRINT_DESCRIPTION, "test!");
    }

    @Test
    public void testGetStage_Init() throws Exception {
        // this calls the private initialization method that reads the
        // marketplace id and the localization
        String stage = tb.getStage();
        assertEquals(localizations, tb.stages);
        assertEquals(localizations.get(0).getText(), stage);
    }

    @Test
    public void testGetStage_NoLocalizationForLocale() throws Exception {
        tb.setLocale(JA);
        String stage = tb.getStage();
        assertEquals(localizations, tb.stages);
        assertEquals(null, stage);
    }

    @Test
    public void testGetStage_Caching() throws Exception {
        tb.getStage();
        verify(bsMock, times(1)).getMarketplaceStageLocalization(anyString());
        tb.getStage();
        verifyNoMoreInteractions(bsMock);
        verifyZeroInteractions(msMock);

        tb.changeLocale();
        tb.getStage();
        verifyNoMoreInteractions(bsMock);
        verifyZeroInteractions(msMock);

        // after saving the localization list has to be reinitialized
        tb.saveStage();
        tb.getStage();
        verify(bsMock, times(2)).getMarketplaceStageLocalization(anyString());
        verifyNoMoreInteractions(msMock);
    }

    @Test
    public void testGetStage_ChangeLocale_B9679() throws Exception {
        tb.changeLocale();
        verify(tb).resetUIInputChildren();
    }

    @Test
    public void testGetStage_reset_B9679() throws Exception {
        doNothing().when(tb).resetBundles();
        tb.deleteTranslations();
        verify(tb).resetUIInputChildren();
    }

    @Test
    public void testPreview() throws Exception {
        tb.getStage();

        tb.preview();
        assertTrue(tb.isShowConfirm());
    }

    @Test
    public void testPreview_Reset() throws Exception {
        tb.getStage();

        tb.preview();
        assertTrue(tb.isShowConfirm());
        tb.changeLocale();
        assertFalse(tb.isShowConfirm());

        tb.preview();
        assertTrue(tb.isShowConfirm());
        tb.saveStage();
        assertFalse(tb.isShowConfirm());
    }

    @Test
    public void testGetStagePreview_EN() throws Exception {
        String stage = tb.getStage();
        String stagePreview = tb.getStagePreview();
        assertEquals(stage, stagePreview);
    }

    @Test
    public void testGetStagePreview_DE() throws Exception {
        tb.setLocale(DE);
        String stage = tb.getStage();
        String stagePreview = tb.getStagePreview();
        assertEquals(stage, stagePreview);
    }

    @Test
    public void testGetStagePreview_NoStageUsingDefaultLocale()
            throws Exception {
        tb.setLocale(JA);
        tb.getStage();
        String stagePreview = tb.getStagePreview();
        // en will be used as default
        assertEquals(localizations.get(0).getText(), stagePreview);
    }

    @Test
    public void testGetStagePreview_NoStageNoDefaultUsingImage()
            throws Exception {
        doReturn("").when(tb).getRequestContextPath();
        localizations.get(0).setText("");
        tb.setLocale(JA);
        tb.getStage();
        String stagePreview = tb.getStagePreview();
        // the image will be used
        assertTrue(stagePreview
                .startsWith("<img id=\"marketplaceStageDefault\""));
    }

    @Test
    public void testGetStagePreview_NoDefaultStageUsingImage() throws Exception {
        doReturn("").when(tb).getRequestContextPath();
        localizations.get(0).setText("");
        tb.getStage();
        String stagePreview = tb.getStagePreview();
        // the image will be used
        assertTrue(stagePreview
                .startsWith("<img id=\"marketplaceStageDefault\""));
    }

    @Test
    public void testSaveStage() throws Exception {
        tb.getStage();
        String stage = "the one to save";
        tb.setStage(stage);
        tb.setDirtyStage(true);
        String stagePreview = tb.getStagePreview();
        assertEquals(stage, stagePreview);
        tb.saveStage();

        verifySavedStage(EN, stage, BaseAdmUmTest.GLOBAL_MARKETPLACE_NAME);
        assertFalse(tb.isDirtyStage());
    }

    @Test
    public void testSaveStage_Empty() throws Exception {
        doReturn("").when(tb).getRequestContextPath();
        tb.getStage();
        String stage = "";
        tb.setStage(stage);
        tb.setDirtyStage(true);
        String stagePreview = tb.getStagePreview();
        assertTrue(stagePreview
                .startsWith("<img id=\"marketplaceStageDefault\""));
        tb.saveStage();
        verifySavedStage(EN, stage, BaseAdmUmTest.GLOBAL_MARKETPLACE_NAME);
        assertFalse(tb.isDirtyStage());
    }

    @Test
    public void testSaveStage_EmptyWithStageInDefaultLocale() throws Exception {
        tb.setLocale(DE);
        tb.getStage();
        String stage = "";
        tb.setStage(stage);
        String stagePreview = tb.getStagePreview();
        assertEquals(localizations.get(0).getText(), stagePreview);
        tb.saveStage();
        verifySavedStage(DE, stage, BaseAdmUmTest.GLOBAL_MARKETPLACE_NAME);
    }

    @Test
    public void testCancelPreview() throws Exception {
        tb.setDirtyStage(true);
        tb.setShowConfirm(true);
        tb.cancelPreview();
        assertFalse(tb.isShowConfirm());
        assertTrue(tb.isDirtyStage());
    }

    @Test
    public void validateImprint_http() throws ValidatorException {
        tb.validateImprint(context, component, "http://www.gmx.de");
    }

    @Test
    public void validateImprint_ftp() throws ValidatorException {
        tb.validateImprint(context, component, "ftp://123.45.67.80:1234");
    }

    @Test
    public void validateImprint_Null() throws ValidatorException {
        tb.validateImprint(context, component, null);
    }

    @Test
    public void validateImprint_html() throws ValidatorException {
        // see [5157]
        tb.validateImprint(context, component,
                "http://estbesrh4:8090/smartdocs/crm.html");
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_Protocol1() throws ValidatorException {
        tb.validateImprint(context, component, "www.gmx.de");
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_MissingProtocol2() throws ValidatorException {
        tb.validateImprint(context, component, "gmx.de");
    }

    @Test
    public void validateImprint_EmptyString() throws ValidatorException {
        tb.validateImprint(context, component, "");
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_Whitespaces() throws ValidatorException {
        tb.validateImprint(context, component, "   ");
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_Object() throws ValidatorException {
        tb.validateImprint(context, component, new Long(34));
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_MissingPort() throws ValidatorException {
        tb.validateImprint(context, component, "https://localhost:");
    }

    @Test(expected = ValidatorException.class)
    public void validateImprint_UnknownProtocol() throws ValidatorException {
        tb.validateImprint(context, component, "xyz://www.gmx.de");
    }

    @Test
    public void validateImprint_defaultText() throws ValidatorException {
        tb.validateImprint(context, component,
                JSFUtils.getText(KEY_IMPRINT_DESCRIPTION, null));
    }

    @Test
    public void getProperty_Bug10585_FoundInLocalizedDataService() {
        // given
        doReturn(prepareProperty("key", "ls_value")).when(lsMock)
                .loadMessageProperties(anyString());
        doReturn(prepareProperty("key1", "bs_value")).when(bsMock)
                .loadMessageProperties(anyString(), anyString());
        // when
        String property = tb.getProperty("key", "en");
        // then
        assertEquals(property, "ls_value");
    }

    @Test
    public void getProperty_Bug10585_FoundInBrandService() {
        // given
        doReturn(prepareProperty("key", "ls_value")).when(lsMock)
                .loadMessageProperties(anyString());
        doReturn(prepareProperty("key", "bs_value")).when(bsMock)
                .loadMessageProperties(anyString(), anyString());
        // when
        String property = tb.getProperty("key", "en");
        // then
        assertEquals(property, "bs_value");
    }

    private Properties prepareProperty(String key, String value) {
        Properties props = new Properties();
        props.put(key, value);
        return props;
    }

    private void verifySavedStage(String locale, String stage, String mId)
            throws Exception {
        ArgumentCaptor<String> acLocale = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> acStage = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> acMId = ArgumentCaptor.forClass(String.class);
        verify(bsMock, times(1)).setMarketplaceStage(acStage.capture(),
                acMId.capture(), acLocale.capture());

        assertEquals(locale, acLocale.getValue());
        assertEquals(stage, acStage.getValue());
        assertEquals(mId, acMId.getValue());
    }

}
