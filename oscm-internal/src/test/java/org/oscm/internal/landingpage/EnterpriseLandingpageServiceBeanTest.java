/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.landingpage;

import static org.oscm.test.Numbers.L_TIMESTAMP;
import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito.BDDMyOngoingStubbing;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOCategory;

/**
 * @author zankov
 * 
 */
public class EnterpriseLandingpageServiceBeanTest {

    private EnterpriseLandingpageServiceBean bean;
    private SearchServiceLocal searchService;
    private DataService dataManager;
    private SubscriptionServiceLocal subscriptionService;
    private LocalizerServiceStub localizer;
    private PlatformUser someUser;

    @Before
    public void setup() {
        bean = new EnterpriseLandingpageServiceBean();

        localizer = new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return objectType.name();
            }
        };
        bean.localizer = localizer;

        searchService = mock(SearchServiceLocal.class);
        bean.searchService = searchService;

        dataManager = mock(DataService.class);
        bean.dataManager = dataManager;
        someUser = new PlatformUser();
        when(dataManager.getCurrentUser()).thenReturn(someUser);

        subscriptionService = mock(SubscriptionServiceLocal.class);
        bean.subscriptionService = subscriptionService;
    }

    /**
     * All categories from server must be part of result
     */
    @Test
    public void loadCategoriesForEnterpriseLandingpage() throws Exception {

        // given
        givenCategorizationService().willReturn(newCategories("red", "blue"));

        // when
        List<VOCategory> result = bean.loadCategories("mp_id", "en");

        // then
        assertThat(result, hasItems(2));
    }

    /**
     * Only the first three categories are used
     */
    @Test
    public void loadCategories_resultCutToThree() throws Exception {

        // given 4 categories
        givenCategorizationService().willReturn(
                newCategories("red", "blue", "yellow", "black"));

        // when
        List<VOCategory> result = bean.loadCategories("mp_id", "en");

        // then only the first three are returned
        assertThat(result, hasItems(3));
    }

    /**
     * No exceptions must be thrown if server returns empty list of categories.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void loadCategoriesForEnterpriseLandingpage_empty() throws Exception {

        // given empty list
        givenCategorizationService().willReturn(Collections.EMPTY_LIST);

        // when
        List<VOCategory> result = bean.loadCategories("mp_id", "en");

        // then no exception is thrown
        assertThat(result, hasNoItems());
    }

    private BDDMyOngoingStubbing<List<VOCategory>> givenCategorizationService() {
        CategorizationService categorizationService = mock(CategorizationService.class);
        BDDMyOngoingStubbing<List<VOCategory>> mock = given(categorizationService
                .getCategories(anyString(), anyString()));
        bean.categorizationService = categorizationService;
        return mock;
    }

    private List<VOCategory> newCategories(String... categoryIds) {
        List<VOCategory> categories = new ArrayList<VOCategory>();
        for (String categoryId : categoryIds) {
            VOCategory category = new VOCategory();
            category.setCategoryId(categoryId);
            category.setName(categoryId);
            categories.add(category);
        }
        return categories;
    }

    @Test
    public void loadCategories_NoCategories() {
        // given
        noCategoriesCreated("mp_id", Locale.ENGLISH.getLanguage());

        // when
        List<VOCategory> result = bean.loadCategories("mp_id",
                Locale.ENGLISH.getLanguage());

        // than
        assertThat(result, hasNoItems());
    }

    private void noCategoriesCreated(String marketplaceId, String locale) {
        CategorizationService categorizationBean = mock(CategorizationService.class);
        bean.categorizationService = categorizationBean;
        when(categorizationBean.getCategories(marketplaceId, locale))
                .thenReturn(new ArrayList<VOCategory>());

    }

    @Test
    public void loadLandingpageEntries() throws Exception {
        // given
        givenCategorizationService().willReturn(newCategories("red"));
        when(searchService.getServicesByCategory("mp_id", "red")).thenReturn(
                oneServiceFound());
        when(subscriptionService.getSubscriptionsForUserInt(someUser))
                .thenReturn(oneSubscription());

        // when
        Response result = bean.loadLandingpageEntries("mp_id", "en");

        // then
        assertNotNull(result.getResult(EnterpriseLandingpageData.class));
        EnterpriseLandingpageData entries = result
                .getResult(EnterpriseLandingpageData.class);
        assertNotNull(entries.category0);
        assertThat(entries.getEntries(0), hasOneItem());
        verify(searchService, times(1)).getServicesByCategory("mp_id", "red");
        verify(subscriptionService, times(1)).getSubscriptionsForUserInt(
                someUser);
    }

    @Test
    public void loadLandingpageEntries_NoServicesFound() throws Exception {

        // given
        givenCategorizationService().willReturn(newCategories("red"));
        when(searchService.getServicesByCategory("mp_id", "red")).thenReturn(
                noServiceFound());
        when(subscriptionService.getSubscriptionsForUserInt(someUser))
                .thenReturn(oneSubscription());

        // when
        Response result = bean.loadLandingpageEntries("mp_id", "en");

        // then
        EnterpriseLandingpageData entries = result
                .getResult(EnterpriseLandingpageData.class);
        assertNotNull(entries.category0);
        assertThat(entries.getEntries(0), hasNoItems());
    }

    private ProductSearchResult noServiceFound() {
        ProductSearchResult result = new ProductSearchResult();
        List<Product> products = Collections.emptyList();
        result.setResultSize(0);
        result.setServices(products);
        return result;
    }

    private List<Subscription> oneSubscription() {
        List<Subscription> result = new ArrayList<Subscription>();

        Subscription subscription = new Subscription();
        subscription.bindToProduct(givenProduct());
        subscription.setSubscriptionId("SUB_ID");
        subscription.setCreationDate(L_TIMESTAMP);
        subscription.setActivationDate(L_TIMESTAMP);
        subscription.setDeactivationDate(L_TIMESTAMP);
        subscription.setTimeoutMailSent(false);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAccessInfo("accessInfo");
        subscription.setBaseURL("baseURL");
        subscription.setLoginPath("loginPath");
        subscription.setProductInstanceId("productInstanceId");
        subscription.setPurchaseOrderNumber("purchaseOrderNumber");
        PlatformUser owner = new PlatformUser();
        owner.setUserId("OWNER_USER_ID");
        subscription.setOwner(owner);

        result.add(subscription);

        return result;
    }

    private Product givenProduct() {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setVendor(givenSupplier());
        product.setTechnicalProduct(givenTechnicalProduct());
        product.setKey(4321);
        product.setProductId("productId");
        product.setStatus(ServiceStatus.ACTIVE);

        return product;
    }

    private Organization givenSupplier() {
        Organization supplier = new Organization();
        supplier.setOrganizationId("organizationId");
        supplier.setName("name");

        return supplier;
    }

    private TechnicalProduct givenTechnicalProduct() {
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setAccessType(ServiceAccessType.DIRECT);
        technicalProduct.setBaseURL("baseURL");
        technicalProduct.setKey(1234);
        technicalProduct.setLoginPath("loginPath");
        technicalProduct.setTechnicalProductId("technicalProductId");

        return technicalProduct;
    }

    private ProductSearchResult oneServiceFound() {
        ProductSearchResult result = new ProductSearchResult();
        result.setServices(Arrays.asList(givenProduct()));
        result.setResultSize(1);
        return result;
    }

    @Test
    public void loadLandingpageEntries_NotExistingCategoryOrMarketplace()
            throws Exception {
        // given the specified marketplace not found
        givenCategorizationService().willReturn(newCategories("red"));
        when(searchService.getServicesByCategory("not_existing_mp", "red"))
                .thenThrow(new ObjectNotFoundException());

        // when
        Response response = bean
                .loadLandingpageEntries("not_existing_mp", "en");
        EnterpriseLandingpageData result = response
                .getResult(EnterpriseLandingpageData.class);

        // then
        assertThat(result.getEntries(0), hasNoItems());
    }

    @After
    public void teardown() {
        bean = null;
    }

}
