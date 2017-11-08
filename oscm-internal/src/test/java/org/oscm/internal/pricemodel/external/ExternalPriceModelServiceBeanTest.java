/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel.external;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.billing.application.bean.LocalizedBillingResourceDAO;
import org.oscm.billing.application.bean.PriceModelPluginBean;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;

/**
 * @author stavreva
 * 
 */
public class ExternalPriceModelServiceBeanTest {

    private static final String BILLING_ID = "bill id";
    private static final UUID PRICE_MODEL_UUID = UUID
            .fromString("392b159e-e60a-41c4-8495-4fd4b6252ed9");
    private ExternalPriceModelServiceBean externalPriceModelBean;
    private final PriceModelPluginBean priceModelPluginBeanMock = mock(PriceModelPluginBean.class);
    private final LocalizedBillingResourceDAO billingResourceDAOMock = mock(LocalizedBillingResourceDAO.class);
    private final OperatorServiceLocalBean operatorServiceMock = mock(OperatorServiceLocalBean.class);
    private List<SupportedLanguage> languageList = getSupportedLanguages("en",
            "de", "ja");
    private DataService dm = mock(DataService.class);

    @Before
    public void setup() throws Exception {
        externalPriceModelBean = spy(new ExternalPriceModelServiceBean());
        externalPriceModelBean.billingResourceDAO = billingResourceDAOMock;
        externalPriceModelBean.operatorService = operatorServiceMock;
        externalPriceModelBean.priceModelPluginBean = priceModelPluginBeanMock;
        externalPriceModelBean.setDm(dm);
        doReturn(languageList).when(operatorServiceMock).getLanguages(false);
    }

    @Test
    public void getPriceModelPresentationFromCache() {
        // given
        LocalizedBillingResource resource = new LocalizedBillingResource();
        doReturn(resource).when(billingResourceDAOMock).get(
                any(LocalizedBillingResource.class));

        // when
        LocalizedBillingResource result = externalPriceModelBean
                .getPriceModelContentFromCache(Locale.ENGLISH, PRICE_MODEL_UUID);

        // then
        assertEquals(resource, result);
    }

    // @Test(expected = ExternalPriceModelException.class)
    public void updateCacheWithException() throws Exception {
        // given
        PriceModel priceModel = new PriceModel(PRICE_MODEL_UUID);
        doThrow(new BillingApplicationException()).when(externalPriceModelBean)
                .convertToLocalizedBillingResource(priceModel, null);
        // when
        externalPriceModelBean.updateCache(priceModel);
    }

    // @SuppressWarnings("unchecked")
    // @Test
    public void updateCache() throws BillingApplicationException,
            ExternalPriceModelException {
        // given
        List<LocalizedBillingResource> resources = getLocalizedResources();
        PriceModel priceModel = new PriceModel(PRICE_MODEL_UUID);
        doReturn(resources).when(billingResourceDAOMock)
                .update(any(List.class));
        doReturn(getLocalizedPriceModelContent())
                .when(priceModelPluginBeanMock).getPriceModel(anyString(),
                        any(Set.class), any(Map.class));
        doReturn(resources).when(externalPriceModelBean)
                .convertToLocalizedBillingResource(any(PriceModel.class),
                        any(LocalizedBillingResourceType.class));

        doReturn(dm).when(externalPriceModelBean).getDm();
        PlatformUser user = new PlatformUser();
        user.setLocale("de");
        doReturn(user).when(dm).getCurrentUser();
        // when
        // externalPriceModelBean.updateCache(priceModel);

        // then
        verify(billingResourceDAOMock).update(resources);
    }

    private List<SupportedLanguage> getSupportedLanguages(String... languages) {
        List<SupportedLanguage> languageList = new ArrayList<SupportedLanguage>();
        for (String language : languages) {
            SupportedLanguage supportedLanguage = new SupportedLanguage();
            supportedLanguage.setLanguageISOCode(language);
            languageList.add(supportedLanguage);
        }
        return languageList;
    }

    private PriceModel getLocalizedPriceModelContent() {
        PriceModel priceModel = new PriceModel(PRICE_MODEL_UUID);
        PriceModelContent descriptionEn = new PriceModelContent(
                MediaType.APPLICATION_JSON,
                "{\"desc_en\":\"value en\"}".getBytes());
        PriceModelContent descriptionDe = new PriceModelContent(
                MediaType.APPLICATION_JSON,
                "{\"desc_de\":\"value de\"}".getBytes());
        priceModel.put(Locale.ENGLISH, descriptionEn);
        priceModel.put(Locale.GERMAN, descriptionDe);
        return priceModel;
    }

    private List<LocalizedBillingResource> getLocalizedResources() {
        List<LocalizedBillingResource> resources = new ArrayList<LocalizedBillingResource>();
        LocalizedBillingResource resourceEn = new LocalizedBillingResource();
        resourceEn
                .setResourceType(LocalizedBillingResourceType.PRICEMODEL_SERVICE);
        resourceEn.setLocale("en");
        resources.add(resourceEn);
        LocalizedBillingResource resourceDe = new LocalizedBillingResource();
        resourceDe
                .setResourceType(LocalizedBillingResourceType.PRICEMODEL_SERVICE);
        resourceDe.setLocale("de");
        resources.add(resourceEn);
        return resources;
    }

    @Test
    public void getPriceModelTagWithCache() {
        // given
        LocalizedBillingResource resource = new LocalizedBillingResource();
        doReturn(resource).when(billingResourceDAOMock).get(
                any(LocalizedBillingResource.class));

        // when
        LocalizedBillingResource result = externalPriceModelBean
                .getPriceModelTagFromCache(Locale.ENGLISH, PRICE_MODEL_UUID);

        // then
        assertEquals(resource, result);
    }

    @Test(expected = ExternalPriceModelException.class)
    public void getPriceModelTagWithNullPriceModelId()
            throws ExternalPriceModelException {
        // given
        LocalizedBillingResource resource = new LocalizedBillingResource();
        doReturn(resource).when(billingResourceDAOMock).get(
                any(LocalizedBillingResource.class));

        // when
        externalPriceModelBean.getCachedPriceModelTag(Locale.ENGLISH, null);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void getPriceModelTagNoCache() throws BillingApplicationException,
            ExternalPriceModelException {
        // given
        List<LocalizedBillingResource> resources = getLocalizedResources();

        doReturn(null).when(externalPriceModelBean).getPriceModelTagFromCache(
                any(Locale.class), any(UUID.class));
        doReturn(resources).when(billingResourceDAOMock)
                .update(any(List.class));

        // when
        String priceModelTag = externalPriceModelBean.getCachedPriceModelTag(
                Locale.ENGLISH, PRICE_MODEL_UUID);

        // then
        assertEquals("", priceModelTag);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getPriceModelTagNoCacheWithException()
            throws BillingApplicationException, ExternalPriceModelException {
        // given
        String expected = "";

        doReturn(null).when(externalPriceModelBean).getPriceModelTagFromCache(
                any(Locale.class), any(UUID.class));
        doThrow(new BillingApplicationException()).when(billingResourceDAOMock)
                .update(any(List.class));

        // when
        String priceModelTag = externalPriceModelBean.getCachedPriceModelTag(
                Locale.ENGLISH, PRICE_MODEL_UUID);

        // then
        assertEquals(expected, priceModelTag);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getExternalPriceModelForService()
            throws ExternalPriceModelException, BillingApplicationException {
        // given
        PriceModel priceModel = new PriceModel(PRICE_MODEL_UUID);
        VOServiceDetails serviceDetails = createVOServiceDetails();
        doReturn(priceModel).when(priceModelPluginBeanMock).getPriceModel(
                Mockito.eq(BILLING_ID), any(Set.class), any(Map.class));

        // when
        PriceModel result = externalPriceModelBean
                .getExternalPriceModelForService(serviceDetails);

        // then
        assertEquals(PRICE_MODEL_UUID, result.getId());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getExternalPriceModelBillingApplicationException()
            throws BillingApplicationException {
        // given
        VOServiceDetails serviceDetails = createVOServiceDetails();
        BillingApplicationException billingApplicationException = new BillingApplicationException();

        doThrow(billingApplicationException).when(priceModelPluginBeanMock)
                .getPriceModel(Mockito.eq(BILLING_ID), any(Set.class),
                        any(Map.class));

        // when
        try {
            externalPriceModelBean
                    .getExternalPriceModelForService(serviceDetails);
        }
        // then
        catch (ExternalPriceModelException e) {
            assertEquals(billingApplicationException, e.getCause());
        }
    }

    @Test
    public void convertToLocalizedBillingResource()
            throws BillingApplicationException {
        // given
        PriceModel externalPriceModel = createExternalPriceModel();

        // when
        List<LocalizedBillingResource> resultList = externalPriceModelBean
                .convertToLocalizedBillingResource(externalPriceModel, null);

        // then
        assertEquals(4, resultList.size());
    }

    private PriceModel createExternalPriceModel() {
        PriceModel externalPriceModel = new PriceModel(PRICE_MODEL_UUID);
        String content1 = "{\"key1\":\"value1\"}";
        String tag1 = "tag1";
        String content2 = "{\"key2\":\"value2\"}";
        String tag2 = "tag2";

        PriceModelContent priceModelContent1 = new PriceModelContent(
                MediaType.APPLICATION_JSON, content1.getBytes(), tag1);
        PriceModelContent priceModelContent2 = new PriceModelContent(
                MediaType.APPLICATION_JSON, content2.getBytes(), tag2);
        externalPriceModel.put(new Locale("en"), priceModelContent1);
        externalPriceModel.put(new Locale("de"), priceModelContent2);

        Map<ContextKey, ContextValue<?>> contextMap = new HashMap<>();
        externalPriceModel.setContext(contextMap);
        return externalPriceModel;
    }

    private VOServiceDetails createVOServiceDetails() {
        VOServiceDetails selectedService = new VOServiceDetails();
        selectedService.setBillingIdentifier(BILLING_ID);
        VOTechnicalService technicalService = new VOTechnicalService();
        technicalService.setExternalBilling(true);
        technicalService.setBillingIdentifier(BILLING_ID);
        selectedService.setTechnicalService(technicalService);
        return selectedService;

    }
}
