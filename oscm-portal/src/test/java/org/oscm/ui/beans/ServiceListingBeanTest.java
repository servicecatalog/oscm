/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito.BDDMyOngoingStubbing;

import org.oscm.ui.beans.marketplace.CategorySelectionBean;
import org.oscm.ui.beans.marketplace.ServicePagingBean;
import org.oscm.ui.beans.marketplace.TagCloudBean;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.intf.SearchServiceInternal;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationService;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceListResult;

/**
 * Test backing bean ServiceListingBean.
 * 
 * @author Enes Sejfi
 */
public class ServiceListingBeanTest {
    private ServiceListingBean serviceListingBean;
    private CategorySelectionBean categorySelectionBean;
    private TagCloudBean tagCloudBean;
    private ServicePagingBean servicePagingBean;

    @Before
    public void setup() throws Exception {
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        given(viewRoot.getLocale()).willReturn(Locale.ENGLISH);
        new FacesContextStub(Locale.ENGLISH).setViewRoot(viewRoot);

        initBeans();
    }

    private void initBeans() {
        // init ServiceListingBean
        serviceListingBean = new ServiceListingBean();
        servicePagingBean = spy(new ServicePagingBean());
        serviceListingBean.setServicePagingBean(servicePagingBean);

        // init CategorySelectionBean
        categorySelectionBean = mock(CategorySelectionBean.class);
        serviceListingBean.setCategorySelectionBean(categorySelectionBean);

        // init TagCloudBean
        tagCloudBean = mock(TagCloudBean.class);
        serviceListingBean.setTagCloudBean(tagCloudBean);

        serviceListingBean.ui = new UiDelegateStub();
        serviceListingBean.ui.setMarketplaceId("someId");
        ((UiDelegateStub) serviceListingBean.ui).setViewLocale(Locale.ENGLISH);
    }

    /**
     * Categories are cached. ResetCategoriesForMarketplace must be called, if
     * the SearchService is called with a concurrently deleted category.
     */
    @Test
    public void searchWithCriteria_refreshCategories() throws Exception {

        // given non existing category
        givenSearchService().willThrow(categoryNotFoundException());

        // when
        serviceListingBean.searchWithCriteria();

        // then
        verify(categorySelectionBean, times(1)).resetCategoriesForMarketplace();
    }

    /**
     * Categories are cached. ResetCategoriesForMarketplace must NOT be called,
     * if the SearchService is called successfully without problem.
     */
    @Test
    public void searchWithCriteria_categoriesNotRefreshed() throws Exception {

        // given an existing category
        givenSearchService().willReturn(anyResult());

        // when
        serviceListingBean.searchWithCriteria();

        // then
        verify(categorySelectionBean, never()).resetCategoriesForMarketplace();
    }

    /**
     * Category selection should be removed, if full text search is done.
     */
    @Test
    public void searchWithPhrase_CategorySelectionRemoved() {

        // given a selected category
        givenCategorySelectionBean()
                .setSelectedCategoryId("selectedCategoryId");

        // when
        serviceListingBean.searchWithPhrase();

        // then
        assertThat(categorySelectionBean.getSelectedCategoryId(), isEmpty());
    }

    private static Matcher<Object> isEmpty() {
        return is((nullValue()));
    }

    private CategorySelectionBean givenCategorySelectionBean() {
        serviceListingBean.setServicePagingBean(mock(ServicePagingBean.class));
        CategorySelectionBean categorySelectionBean = new CategorySelectionBean();
        serviceListingBean.setCategorySelectionBean(categorySelectionBean);
        return categorySelectionBean;
    }

    /**
     * Tags are cached. ResetTagForMarketplace must be called, if the
     * SearchService is called with a concurrently removed tag.
     */
    @Test
    public void searchWithCriteria_refreshTags() throws Exception {

        // given non existing tag
        givenSearchService().willThrow(tagNotFoundException());

        // when
        serviceListingBean.searchWithCriteria();

        // then
        verify(tagCloudBean, times(1)).resetTagsForMarketplace();
    }

    /**
     * Tags are cached. ResetTagForMarketplace must be NOT called, if the
     * SearchService is called successfully with an existing tag.
     */
    @Test
    public void searchWithCriteria_tagsNotRefreshed() throws Exception {

        // given existing tag
        givenSearchService().willReturn(anyResult());

        // when
        serviceListingBean.searchWithCriteria();

        // then
        verify(tagCloudBean, never()).resetTagsForMarketplace();
    }

    private VOServiceListResult anyResult() {
        return new VOServiceListResult();
    }

    private BDDMyOngoingStubbing<VOServiceListResult> givenSearchService()
            throws ObjectNotFoundException {
        SearchServiceInternal searchServiceInternal = mock(SearchServiceInternal.class);
        BDDMyOngoingStubbing<VOServiceListResult> mock = given(searchServiceInternal
                .getServicesByCriteria(anyString(), anyString(),
                        any(ListCriteria.class), any(PerformanceHint.class)));
        serviceListingBean.searchServiceInternal = searchServiceInternal;
        return mock;
    }

    private ObjectNotFoundException tagNotFoundException() {
        return new ObjectNotFoundException(ClassEnum.TAG, "unknownTag");
    }

    private ObjectNotFoundException categoryNotFoundException() {
        return new ObjectNotFoundException(ClassEnum.CATEGORY,
                "unknownCategoryId");
    }

    @Test
    public void showServiceListSearch_nullPhrase() {
        // given
        doReturn(null).when(servicePagingBean).getSearchPhrase();
        // when
        String result = serviceListingBean.showServiceListSearch();
        // then
        assertEquals("showServiceList", result);
    }

    @Test
    public void showServiceListSearch_emptyPhrase() {
        // given
        doReturn("").when(servicePagingBean).getSearchPhrase();
        // when
        String result = serviceListingBean.showServiceListSearch();
        // then
        assertEquals("showServiceList", result);

    }

    @Test
    public void updateServiceListContainsChargeableResellerService_noService() {
        serviceListingBean
                .updateServiceListContainsChargeableResellerService(new ArrayList<VOService>());

        assertFalse(serviceListingBean
                .isServiceListContainsChargeableResellerService());
    }

    @Test
    public void updateServiceListContainsChargeableResellerService_onlyNonResellerServices() {
        List<VOService> svcList = new ArrayList<VOService>();
        VOService svc1 = new VOService();
        svc1.setOfferingType(OfferingType.DIRECT);
        svcList.add(svc1);
        VOService svc2 = new VOService();
        svc2.setOfferingType(OfferingType.BROKER);
        svcList.add(svc2);

        serviceListingBean
                .updateServiceListContainsChargeableResellerService(svcList);

        assertFalse(serviceListingBean
                .isServiceListContainsChargeableResellerService());
    }

    @Test
    public void updateServiceListContainsChargeableResellerService_nonChargeableResellerService() {
        List<VOService> svcList = new ArrayList<VOService>();

        VOService svc1 = new VOService();
        svc1.setOfferingType(OfferingType.DIRECT);
        svcList.add(svc1);

        VOPriceModel pmFree = new VOPriceModel();
        pmFree.setType(PriceModelType.FREE_OF_CHARGE);
        VOService svc2 = spy(new VOService());
        svc2.setOfferingType(OfferingType.RESELLER);
        svc2.setPriceModel(pmFree);
        svcList.add(svc2);

        serviceListingBean
                .updateServiceListContainsChargeableResellerService(svcList);

        assertFalse(serviceListingBean
                .isServiceListContainsChargeableResellerService());
    }

    @Test
    public void updateServiceListContainsChargeableResellerService_chargeableResellerService() {
        List<VOService> svcList = new ArrayList<VOService>();

        VOService svc1 = new VOService();
        svc1.setOfferingType(OfferingType.DIRECT);
        svcList.add(svc1);

        VOPriceModel pmChargeable = new VOPriceModel();
        pmChargeable.setType(PriceModelType.PRO_RATA);
        VOService svc2 = spy(new VOService());
        svc2.setOfferingType(OfferingType.RESELLER);
        svc2.setPriceModel(pmChargeable);
        svcList.add(svc2);

        serviceListingBean
                .updateServiceListContainsChargeableResellerService(svcList);

        assertTrue(serviceListingBean
                .isServiceListContainsChargeableResellerService());
    }

    /**
     * If the the marketplace is set to public should recieve a positive result
     */
    @Test
    public void isPublicLandingPage_PublicLandingpageType() throws Exception {
        // given
        given(landingpageService().loadLandingpageType(anyString()))
                .willReturn(LandingpageType.PUBLIC);

        // when
        boolean result = serviceListingBean.isPublicLandingpage();

        // expected
        assertTrue(result);
    }

    LandingpageConfigurationService landingpageService() {
        LandingpageConfigurationService landingpageService = mock(LandingpageConfigurationService.class);
        serviceListingBean.landingpageService = landingpageService;
        return landingpageService;
    }

    /**
     * If the the marketplace is set to enterprise should recieve a negative
     * result
     */
    @Test
    public void isPublicLandingPage_EnterpriseLandingpageType()
            throws Exception {
        // given
        given(landingpageService().loadLandingpageType("any_mp_id"))
                .willReturn(LandingpageType.ENTERPRISE);

        // when
        boolean result = serviceListingBean.isPublicLandingpage();

        // expected
        assertFalse(result);
    }

}
