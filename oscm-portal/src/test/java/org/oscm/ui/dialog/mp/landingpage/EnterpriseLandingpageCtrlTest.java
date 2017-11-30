/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.landingpage;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.context.ExternalContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.landingpage.EnterpriseLandingpageData;
import org.oscm.internal.landingpage.EnterpriseLandingpageService;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.vo.VOCategory;

/**
 * @author zankov
 * 
 */
public class EnterpriseLandingpageCtrlTest {

    /**
     * 
     */
    private static final String QUERY_SERVICE_ID = "serviceId=123";

    /**
     * 
     */
    private static final String QUERY_MP_ID = "mpId=123";

    private static final String BASE_URL_HTTP = "http://localhost:8080/oscm-portal";

    private static final String BASE_URL_HTTPS = "https://localhost:8080/oscm-portal";

    private EnterpriseLandingpageCtrl ctrl;

    private UiDelegate ui;

    private ApplicationBean applicationBean;

    private ExternalContext extContext;

    private static String MARKETPLACE_ID = "marketplace_id";

    private static Locale LOCALE = Locale.ENGLISH;

    @Before
    public void setUp() {
        ctrl = new EnterpriseLandingpageCtrl();

        extContext = mock(ExternalContext.class);
        when(extContext.getRequestContextPath()).thenReturn(
                "/oscm-portal");

        ui = mock(UiDelegate.class);
        when(ui.getViewLocale()).thenReturn(LOCALE);
        when(ui.getMarketplaceId()).thenReturn(MARKETPLACE_ID);
        when(ui.getMarketplaceIdQueryPart()).thenReturn(QUERY_MP_ID);
        when(ui.getSelectedServiceKeyQueryPart(anyString())).thenReturn(
                QUERY_SERVICE_ID);
        when(ui.getExternalContext()).thenReturn(extContext);
        ctrl.ui = ui;

        applicationBean = mock(ApplicationBean.class);
        when(applicationBean.getServerBaseUrl()).thenReturn(BASE_URL_HTTP);
        when(applicationBean.getServerBaseUrlHttps())
                .thenReturn(BASE_URL_HTTPS);
        ctrl.applicationBean = applicationBean;
    }

    @After
    public void tearDown() {
        ctrl = null;
    }

    private void givenModel(List<VOCategory> categories,
            List<POLandingpageEntry> entries) {
        EnterpriseLandingpageModel result = new EnterpriseLandingpageModel();
        EnterpriseLandingpageData data = givenLandingpageData(categories,
                entries);

        result.addLandingpageData(data);

        addEntriesToModel(result, data);

        ctrl.model = result;
    }

    /**
     * @param categories
     * @param entries
     * @return
     */
    EnterpriseLandingpageData givenLandingpageData(List<VOCategory> categories,
            List<POLandingpageEntry> entries) {
        EnterpriseLandingpageData data = new EnterpriseLandingpageData();

        appendCategories(categories, data);

        appendEntries(entries, data);
        return data;
    }

    /**
     * @param result
     * @param data
     */
    private void addEntriesToModel(EnterpriseLandingpageModel result,
            EnterpriseLandingpageData data) {
        if (data.numberOfColumns() >= 1) {
            result.addEntries(0, newLandingpageModelEntries(data.getEntries(0)));
        }

        if (data.numberOfColumns() >= 2) {
            result.addEntries(1, newLandingpageModelEntries(data.getEntries(1)));
        }

        if (data.numberOfColumns() == 3) {
            result.addEntries(2, newLandingpageModelEntries(data.getEntries(2)));
        }
    }

    private List<LandingpageEntryModel> newLandingpageModelEntries(
            List<POLandingpageEntry> entries) {
        ArrayList<LandingpageEntryModel> result = new ArrayList<LandingpageEntryModel>();
        for (POLandingpageEntry entry : entries) {
            LandingpageEntryModel entryModel = new LandingpageEntryModel(entry);
            entryModel.setAccessLink("http://access_url.com");
            result.add(entryModel);
        }
        return result;
    }

    void appendEntries(List<POLandingpageEntry> entries,
            EnterpriseLandingpageData result) {
        if (result.category0 != null) {
            if (entries.isEmpty()) {
                result.entriesOfCateogry0 = Collections.emptyList();
            } else {
                result.entriesOfCateogry0 = Arrays.asList(entries.get(0));
            }
        }

        if (result.category1 != null) {
            if (entries.isEmpty() || entries.size() < 2) {
                result.entriesOfCateogry1 = Collections.emptyList();
            } else {
                result.entriesOfCateogry1 = Arrays.asList(entries.get(1));
            }
        }

        if (result.category2 != null) {
            if (entries.isEmpty() || entries.size() < 3) {
                result.entriesOfCateogry2 = Collections.emptyList();
            } else {
                result.entriesOfCateogry2 = Arrays.asList(entries.get(2));
            }
        }
    }

    void appendCategories(List<VOCategory> categories,
            EnterpriseLandingpageData result) {
        if (categories.size() > 0) {
            result.category0 = categories.get(0);
        }
        if (categories.size() > 1) {
            result.category1 = categories.get(1);
        }
        if (categories.size() > 2) {
            result.category2 = categories.get(2);
        }
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void getNameFirstCategoryShow_NameNotNull() {
        // given
        givenModel(newCategories(false, "red"), noEntries());
        // when
        String result = ctrl.getNameFirstCategoryShow();
        // then
        assertEquals("redname", result);
    }

    @Test
    public void getNameFirstCategoryShow_NameNull() {
        // given
        givenModel(newCategories(true, "red"), noEntries());
        // when
        String result = ctrl.getNameFirstCategoryShow();
        // then
        assertEquals("red", result);
    }

    @Test
    public void getNameSecondCategoryShow_NameNotNull() {
        // given
        givenModel(newCategories(false, "red", "blue"), noEntries());
        // when
        String result = ctrl.getNameSecondCategoryShow();
        // then
        assertEquals("bluename", result);
    }

    @Test
    public void getNameSecondCategoryShow_NameNull() {
        // given
        givenModel(newCategories(true, "red", "blue"), noEntries());
        // when
        String result = ctrl.getNameSecondCategoryShow();
        // then
        assertEquals("blue", result);
    }

    @Test
    public void getNameThirdCategoryShow_NameNotNull() {
        // given
        givenModel(newCategories(false, "red", "blue", "green"), noEntries());
        // when
        String result = ctrl.getNameThirdCategoryShow();
        // then
        assertEquals("greenname", result);
    }

    @Test
    public void getNameThirdCategoryShow_NameNull() {
        // given
        givenModel(newCategories(true, "red", "blue", "green"), noEntries());
        // when
        String result = ctrl.getNameThirdCategoryShow();
        // then
        assertEquals("green", result);
    }

    @Test
    public void isNameFirstCategoryShowNull_NameNull() {
        // given
        givenModel(newCategories(true, "red"), noEntries());
        // when
        boolean result = ctrl.isNameFirstCategoryShowNull();
        // then
        assertTrue(result);
    }

    @Test
    public void isNameFirstCategoryShowNull_NameNotNull() {
        // given
        givenModel(newCategories(false, "red"), noEntries());
        // when
        boolean result = ctrl.isNameFirstCategoryShowNull();
        // then
        assertFalse(result);
    }

    @Test
    public void isNameSecondCategoryShowNull_NameNull() {
        // given
        givenModel(newCategories(true, "red", "blue"), noEntries());
        // when
        boolean result = ctrl.isNameSecondCategoryShowNull();
        // then
        assertTrue(result);
    }

    @Test
    public void isNameSecondCategoryShowNull_NameNotNull() {
        // given
        givenModel(newCategories(false, "red", "blue"), noEntries());
        // when
        boolean result = ctrl.isNameSecondCategoryShowNull();
        // then
        assertFalse(result);
    }

    @Test
    public void isNameThirdCategoryShowNull_NameNull() {
        // given
        givenModel(newCategories(true, "red", "blue", "green"), noEntries());
        // when
        boolean result = ctrl.isNameThirdCategoryShowNull();
        // then
        assertTrue(result);
    }

    @Test
    public void isNameThirdCategoryShowNull_NameNotNull() {
        // given
        givenModel(newCategories(false, "red", "blue", "green"), noEntries());
        // when
        boolean result = ctrl.isNameThirdCategoryShowNull();
        // then
        assertFalse(result);
    }

    @Test
    public void isShowFirstCategory() {
        // given
        givenModel(newCategories(false, "red"), newEntries("s1"));

        // when
        boolean result = ctrl.isShowFirstCategory();

        // then
        assertTrue(result);
    }

    private List<POLandingpageEntry> newEntries(String... serviceIds) {
        List<POLandingpageEntry> entries = new ArrayList<POLandingpageEntry>();
        for (String serviceId : serviceIds) {
            entries.add(newEntry(serviceId));
        }
        return entries;
    }

    POLandingpageEntry newEntry(String serviceId) {
        POLandingpageEntry entry = new POLandingpageEntry();
        entry.setServiceId(serviceId);
        entry.setName(serviceId);
        entry.setServiceKey(Long.valueOf(serviceId.length()).longValue());
        entry.setSubscriptionKey(Long.valueOf(serviceId.length()).longValue());
        return entry;
    }

    private List<VOCategory> newCategories(boolean isEmptyName,
            String... categoryIds) {
        List<VOCategory> categories = new ArrayList<VOCategory>();
        for (String categoryId : categoryIds) {
            VOCategory category = new VOCategory();
            category.setCategoryId(categoryId);
            category.setName(isEmptyName ? "" : categoryId + "name");
            categories.add(category);
        }
        return categories;
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */

    @Test
    public void isShowFirstCategory_EmptyCategory() {
        // given
        givenModel(newCategories(false, "red"), noEntries());

        // when
        boolean result = ctrl.isShowFirstCategory();

        // then
        assertTrue(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowFirstCategory_NoCategory() {
        // given
        givenModel(noCategories(), noEntries());

        // when
        boolean result = ctrl.isShowFirstCategory();

        // then
        assertFalse(result);
    }

    private List<VOCategory> noCategories() {
        return Collections.emptyList();
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowSecondCategory() {
        // given
        givenModel(newCategories(false, "red", "blue"), newEntries("s1", "s2"));

        // when
        boolean result = ctrl.isShowSecondCategory();

        // then
        assertTrue(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowSecondCategory_EmptyCategory() {
        // given
        givenModel(newCategories(false, "red", "blue"), newEntries("s1"));

        // when
        boolean result = ctrl.isShowSecondCategory();

        // then
        assertTrue(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowSecondCategory_NoCategory() {
        // given
        givenModel(newCategories(false, "red"), newEntries("s1"));

        // when
        boolean result = ctrl.isShowSecondCategory();

        // then
        assertFalse(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowThirdCategory() {
        // given
        givenModel(newCategories(false, "red", "blue", "green"),
                newEntries("s1", "s2", "s3"));

        // when
        boolean result = ctrl.isShowThirdCategory();

        // then
        assertTrue(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowThirdCategory_EmptyCategory() {
        // given
        givenModel(newCategories(false, "red", "blue", "green"),
                newEntries("s1", "s2"));

        // when
        boolean result = ctrl.isShowThirdCategory();

        // then
        assertTrue(result);
    }

    /**
     * The first three categories should always be visible even if there are no
     * service added
     */
    @Test
    public void isShowThirdCategory_NoCategory() {
        // given
        givenModel(newCategories(false, "red", "blue"), newEntries("s1", "s2"));

        // when
        boolean result = ctrl.isShowThirdCategory();

        // then
        assertFalse(result);
    }

    /**
     * Check that the UI Service is called for loading of the landingpage
     * entries
     */
    @Test
    public void loadData() {
        // given
        ctrl.model = new EnterpriseLandingpageModel();
        EnterpriseLandingpageService landingpageService = givenLandingpageService();

        // when
        ctrl.loadData();

        // than
        verify(landingpageService, times(1)).loadLandingpageEntries(
                MARKETPLACE_ID, LOCALE.getLanguage());
    }

    private EnterpriseLandingpageService givenLandingpageService() {
        EnterpriseLandingpageService service = mock(EnterpriseLandingpageService.class);
        Response response = new Response();
        response.getResults().add(new EnterpriseLandingpageData());
        when(
                service.loadLandingpageEntries(MARKETPLACE_ID,
                        LOCALE.getLanguage())).thenReturn(response);
        ctrl.landingpageService = service;

        return service;
    }

    private List<POLandingpageEntry> noEntries() {
        return Collections.emptyList();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getEntry_NoEntries() {
        ctrl.getEntry(noLandingpageModelEntries(), "some_key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getEntry_NotExistingEntryId() {
        // given
        ArrayList<LandingpageEntryModel> entries = givenLandingpageModelEntries("e1");

        // when
        ctrl.getEntry(entries, "unknown_key");
    }

    @Test
    public void getEntry() {
        // given
        ArrayList<LandingpageEntryModel> entries = givenLandingpageModelEntries(
                "e1", "e12");

        // when
        LandingpageEntryModel result = ctrl.getEntry(entries,
                String.valueOf(Long.valueOf("e12".length()).longValue()));

        // than
        assertNotNull(result);
        assertEquals("e12", result.getName());
    }

    private ArrayList<LandingpageEntryModel> givenLandingpageModelEntries(
            String... keys) {
        ArrayList<LandingpageEntryModel> result = new ArrayList<LandingpageEntryModel>();
        for (String serviceId : keys) {
            LandingpageEntryModel entry = new LandingpageEntryModel(
                    newEntry(serviceId));
            result.add(entry);
        }

        return result;

    }

    List<LandingpageEntryModel> noLandingpageModelEntries() {
        return Collections.emptyList();
    }

    @Test
    public void mergeAllEntries_EmptyModel() {
        // given empty model
        givenModel(noCategories(), noEntries());

        // when
        ArrayList<LandingpageEntryModel> result = ctrl.mergeAllEntries();

        // than
        assertThat(result, hasNoItems());
    }

    @Test
    public void mergeAllEntries() {
        // given empty model
        givenModel(newCategories(false, "cat1", "cat2"), newEntries("s1", "s2"));

        // when
        ArrayList<LandingpageEntryModel> result = ctrl.mergeAllEntries();

        // than
        assertThat(result, hasItems(2));
    }

    @Test
    public void addModelEntries() {
        // given
        givenModel(noCategories(), noEntries());
        EnterpriseLandingpageData data = givenLandingpageData(
                newCategories(false, "cat1", "cat2"), newEntries("e1", "e2"));

        // when
        ctrl.addModelEntries(data);

        // than
        assertThat(ctrl.getModel().entriesOfCateogry0, hasOneItem());
        assertThat(ctrl.getModel().entriesOfCateogry1, hasOneItem());
        assertNull(ctrl.getModel().entriesOfCateogry2);
    }

    @Test
    public void getAccessUrl_DIRECT() {
        // given
        POLandingpageEntry entry = newEntry("s1");
        entry.setServiceAccessType(ServiceAccessType.DIRECT);

        // when
        String accessUrl = ctrl.getAccessUrl(entry);

        // than
        assertNull(accessUrl);
    }

    @Test
    public void getAccessUrl_USER() {
        // given
        POLandingpageEntry entry = newEntry("s1");
        entry.setServiceAccessType(ServiceAccessType.DIRECT);

        // when
        String accessUrl = ctrl.getAccessUrl(entry);

        // than
        assertNull(accessUrl);
    }

    @Test
    public void getAccessUrl_LOGIN() {
        // given
        POLandingpageEntry entry = newEntry("s1");
        entry.setServiceAccessType(ServiceAccessType.LOGIN);
        entry.setServiceAccessURL("base_url");

        // when
        String result = ctrl.getAccessUrl(entry);

        // than
        assertEquals(
                BASE_URL_HTTP + Constants.SERVICE_BASE_URI + "/"
                        + Long.toHexString(entry.getSubscriptionKey()) + "/",
                result);
    }

    /**
     * If there is one subscription for the given entry (Product) the UI should
     * redirect to the service. The normal case for fcip ist that there is only
     * one or none subscription on given product.
     * 
     * For now the redirect url is stored as accessUrl in the entry object.
     */
    @Test
    public void composeRedirectUrl_RedirectToService() {
        // given
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(true);
        entry.setServiceAccessURL("base_url");
        entry.setAccessLink("access_url");

        // when
        String result = ctrl.composeRedirectUrl(entry);

        // than
        assertEquals("access_url", result);
    }

    /**
     * If there are no subscriptions for given entry the UI should redirect to
     * the service details page.
     */
    @Test
    public void composeRedirectUrl_RedirectToServiceDetails() {
        // given
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(false);
        entry.setAccessLink("access_url");
        when(extContext.encodeActionURL(anyString())).thenReturn(
                "service_details_url");

        // when
        String result = ctrl.composeRedirectUrl(entry);

        // than
        assertEquals("service_details_url", result);
    }

    /**
     * If there are more than one subscription for given entry than the UI
     * should redirect to the "My Subscriptions" page.
     */
    @Test
    public void composeRedirectUrl_RedirectToListSubscriptions() {

        // given subscribed and no base url
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(true);
        entry.setServiceAccessURL(null);
        when(extContext.encodeActionURL(anyString())).thenReturn(
                "mysubscriptions_url");

        // when
        String result = ctrl.composeRedirectUrl(entry);

        // than
        assertEquals("mysubscriptions_url", result);
    }

    /**
     * when redirecting to the service a new tab should be opened in the browser
     */
    @Test
    public void isOpenNewTab_newTab() {
        // given subscribed and base url available
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(true);
        entry.setServiceAccessURL(BASE_URL_HTTP);

        // then
        assertFalse(ctrl.isOpenNewTab(entry));
    }

    /**
     * when redirecting to "Service Details" or "My Subscriptions" the new page
     * should be opened in the same tab
     */
    @Test
    public void isOpenNewTab_SameTabNotSubscribed() {
        // given not subscribed
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(false);

        // then
        assertFalse(ctrl.isOpenNewTab(entry));
    }

    /**
     * when redirecting to "Service Details" or "My Subscriptions" the new page
     * should be opened in the same tab
     */
    @Test
    public void isOpenNewTab_SameTabSubscribed() {
        // given not subscribed no base url
        LandingpageEntryModel entry = new LandingpageEntryModel(newEntry("s1"));
        entry.setSubscribed(true);
        entry.setServiceAccessURL(null);

        // then
        assertFalse(ctrl.isOpenNewTab(entry));
    }

    @Test
    public void testIsInternalURL_Empty() {
        // expected
        assertFalse(ctrl.isInternalURL(""));
    }

    @Test
    public void testIsInternalURL_Null() {
        // expected
        assertFalse(ctrl.isInternalURL(null));
    }

    @Test
    public void testIsInternalURL_HTTP() {
        // given
        String serviceAccessURL = "http://localhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = ctrl.isInternalURL(serviceAccessURL);

        // expected
        assertTrue(result);
    }

    @Test
    public void testIsInternalURL_HTTPS() {
        // given
        String serviceAccessURL = "https://localhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = ctrl.isInternalURL(serviceAccessURL);

        // expected
        assertTrue(result);
    }

    @Test
    public void testIsInternalURL_SameAppOtherHost() {
        // given
        String serviceAccessURL = "https://otherhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = ctrl.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }

    @Test
    public void testIsInternalURL_SameHostOtherApp() {
        // given
        String serviceAccessURL = "https://localhost:8080/otherapp/subscriptions.jsp";

        // when
        boolean result = ctrl.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }

    @Test
    public void testIsInternalURL_External() {
        // given
        String serviceAccessURL = "http://somehost:8080/someapp/admin/subscriptions.jsp";

        // when
        boolean result = ctrl.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }
}
