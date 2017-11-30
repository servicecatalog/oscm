/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 12.03.2013
 *
 *******************************************************************************/
package org.oscm.ui.dialog.mp.usesubscriptions;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.context.ExternalContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.paginator.PaginationInt;
import org.richfaces.component.SortOrder;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.tables.Pagination;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOSubscription;

public class MySubscriptionsLazyDataModelTest {

    private static final String BASE_URL_HTTP = "http://localhost:8080/oscm-portal";
    private static final String BASE_URL_HTTPS = "https://localhost:8080/oscm-portal";

    MySubscriptionsLazyDataModel model;
    ApplicationBean appBean = mock(ApplicationBean.class);

    private SubscriptionService subSvc;

    private SubscriptionsService subscriptionsService = mock(SubscriptionsService.class);

    @Before
    public void setup() {
        MySubscriptionsLazyDataModel realObject = new MySubscriptionsLazyDataModel(){
            @Override
            protected void applyFilters(List<FilterField> filterFields, Pagination pagination) {

            }

            @Override
            protected void decorateWithLocalizedStatuses(Pagination pagination) {
            }

            @Override
            protected void applyFilters(List<FilterField> filterFields, PaginationInt pagination) {

            }

            @Override
            protected void decorateWithLocalizedStatuses(org.oscm.paginator.Pagination pagination) {
            }
        };
        model = spy(realObject);
        model.setApplicationBean(appBean);

        ExternalContext extContext = mock(ExternalContext.class);
        when(extContext.getRequestContextPath()).thenReturn(
                "/oscm-portal");

        model.setSubscriptionsService(subscriptionsService);
        ApplicationBean applicationBean = mock(ApplicationBean.class);
        model.setApplicationBean(applicationBean);
        when(applicationBean.getServerBaseUrlHttps()).thenReturn(
                BASE_URL_HTTPS);
        when(applicationBean.getServerBaseUrl()).thenReturn(BASE_URL_HTTP);

        subSvc = mock(SubscriptionService.class);
    }

    @Test
    public void bug11725() {
        //given
        //when
        model.init();
        //then
        assertEquals(SortOrder.descending, model.getSortOrders().get(model.getACTIVATION()));
    }


    @Test
    public void testGetAccessUrl_DIRECT() {
        POSubscription subscription = new POSubscription(new VOSubscription());
        subscription.setServiceAccessType(ServiceAccessType.DIRECT);
        subscription.setServiceAccessInfo("Go planet!");

        String accessUrl = model.getAccessUrl(subscription);
        assertTrue(accessUrl.isEmpty());
    }

    @Test
    public void testGetAccessUrl_USER() {
        POSubscription subscription = new POSubscription(new VOSubscription());
        subscription.setServiceAccessType(ServiceAccessType.USER);
        subscription.setServiceAccessInfo("Go planet!");

        String accessUrl = model.getAccessUrl(subscription);
        assertTrue(accessUrl.isEmpty());
    }

    /**
     * For FCIP we have decided to use the ServiceBaseUrl property for storing
     * the service access link. Thus if the baseUrl is set in the
     * TechnicalService we can atomatically forward to the service if
     * subscription is available.
     *
     *
     * The Implementation of getAccessUrl method was sligtly changed. The method
     * return EMPTY_STRING or the baseUrl (without any formattings) if available
     * for USER or DIRECT access type
     */
    @Test
    public void testGetAccessUrl_USER_ContainsBaseUrl() {
        // given
        POSubscription subscription = new POSubscription(new VOSubscription());
        subscription.setServiceAccessType(ServiceAccessType.USER);
        subscription.setServiceAccessInfo("Go planet!");
        subscription.setServiceBaseURL("base_url");

        // when
        String accessUrl = model.getAccessUrl(subscription);

        // expected
        assertEquals("base_url", accessUrl);
    }

    /**
     * For FCIP we have decided to use the ServiceBaseUrl property for storing
     * the service access link. Thus if the baseUrl is set in the
     * TechnicalService we can atomatically forward to the service if
     * subscription is available.
     *
     *
     * The Implementation of getAccessUrl method was sligtly changed. The method
     * return EMPTY_STRING or the baseUrl (without any formattings) if available
     * for USER or DIRECT access type
     */
    @Test
    public void testGetAccessUrl_DIRECT_ContainsBaseUrl() {
        // given
        POSubscription subscription = new POSubscription(new VOSubscription());
        subscription.setServiceAccessType(ServiceAccessType.DIRECT);
        subscription.setServiceAccessInfo("Go planet!");
        subscription.setServiceBaseURL("base_url");

        // when
        String accessUrl = model.getAccessUrl(subscription);

        // expected
        assertEquals("base_url", accessUrl);
    }

    @Test
    public void testIsInternalURL_Empty() {
        // expected
        assertFalse(model.isInternalURL(""));
    }

    @Test
    public void testIsInternalURL_Null() {
        // expected
        assertFalse(model.isInternalURL(null));
    }

    @Test
    public void testIsInternalURL_HTTP() {
        // given
        String serviceAccessURL = "http://localhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = model.isInternalURL(serviceAccessURL);

        // expected
        assertTrue(result);
    }

    @Test
    public void testIsInternalURL_HTTPS() {
        // given
        String serviceAccessURL = "https://localhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = model.isInternalURL(serviceAccessURL);

        // expected
        assertTrue(result);
    }

    @Test
    public void testIsInternalURL_External() {
        // given
        String serviceAccessURL = "http://somehost:8080/someapp/admin/subscriptions.jsp";

        // when
        boolean result = model.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }

    @Test
    public void testIsInternalURL_SameAppOtherHost() {
        // given
        String serviceAccessURL = "https://otherhost:8080/oscm-portal/admin/subscriptions.jsp";

        // when
        boolean result = model.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }

    @Test
    public void testIsInternalURL_SameHostOtherApp() {
        // given
        String serviceAccessURL = "https://localhost:8080/otherapp/subscriptions.jsp";

        // when
        boolean result = model.isInternalURL(serviceAccessURL);

        // expected
        assertFalse(result);
    }


    @Test
    public void testGetAccessUrl_LOGIN() {
        testGetAccessUrl_BuiltURL(ServiceAccessType.LOGIN);
    }

    private void testGetAccessUrl_BuiltURL(ServiceAccessType accessType) {
        VOSubscription vo = new VOSubscription();
        vo.setStatus(SubscriptionStatus.ACTIVE);
        POSubscription subscription = new POSubscription(vo);
        subscription.setServiceAccessType(accessType);
        subscription.setServiceAccessInfo("Go planet!");
        subscription.setServiceBaseURL("marketplace");
        String accessUrl = model.getAccessUrl(subscription);
        String expected = BASE_URL_HTTP + Constants.SERVICE_BASE_URI + "/"
                + subscription.getHexKey() + "/";
        Assert.assertEquals(expected, accessUrl);
    }



    @Test
    public void testGetDataList() throws Exception {
        //given
        int firstRow = 0;
        int numRows = 10;
        int totalCount = 1;
        List<POSubscription> expectedList = prepareList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);
        model.setSelectedSubscription(new POSubscription(new VOSubscription()));

        //when
        List<POSubscription> result = model.getDataList(firstRow, numRows,
                Collections.<FilterField>emptyList(), Collections.<SortField>emptyList(), new Object());
        //then
        assertArrayEquals(expectedList.toArray(), result.toArray());
        assertEquals(model.getTotalCount(), totalCount);
    }



    @Test
    public void testGetDataListFilteredOut() throws Exception {
        //given
        int firstRow = 0;
        int numRows = 10;
        int totalCount = 1;
        List<POSubscription> expectedList = prepareList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setKey(1001L);
        model.setSelectedSubscription(new POSubscription(voSubscription));

        //when
        List<POSubscription> result = model.getDataList(firstRow, numRows,
                Collections.<FilterField>emptyList(), Collections.<SortField>emptyList(), new Object());
        //then
        assertArrayEquals(expectedList.toArray(), result.toArray());
        assertEquals(model.getTotalCount(), totalCount);
    }

    @Test
    public void testGetDataListReturnsDetails() throws Exception {
        //given
        int firstRow = 0;
        int numRows = 10;
        int totalCount = 1;
        List<POSubscription> expectedList = prepareList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        doReturn(expectedList.get(0)).when(subscriptionsService).getMySubscriptionDetails(expectedList.get(0).getKey());
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);
        model.setSelectedSubscription(new POSubscription(new VOSubscription()));

        //when
        List<POSubscription> result = model.getDataList(firstRow, numRows,
                Collections.<FilterField>emptyList(), Collections.<SortField>emptyList(), new Object());
        //then
        assertArrayEquals(expectedList.toArray(), result.toArray());
        assertEquals(model.getTotalCount(), totalCount);
    }

    @Test
    public void testGetRowTotalCount() throws Exception {
        //given
        int totalCount = 1;
        List<POSubscription> expectedList = prepareList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);
        model.setSelectedSubscription(new POSubscription(new VOSubscription()));

        //when
        int result = model.getTotalCount();
        //then
        assertEquals(result, totalCount);
    }

    @Test
    public void testGetRowTotalCountEmptyResult() throws Exception {
        //given
        int totalCount = 0;
        List<POSubscription> expectedList = Collections.emptyList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);
        model.setSelectedSubscription(new POSubscription(new VOSubscription()));

        //when
        int result = model.getTotalCount();
        //then
        assertEquals(result, totalCount);
    }

    @Test
    public void testGetDataListNullSelected() throws Exception {
        //given
        int firstRow = 0;
        int numRows = 10;
        int totalCount = 1;
        List<POSubscription> expectedList = prepareList();
        Response resp = new Response(expectedList);
        POSubscription selectedSubscription = mock(POSubscription.class);
        when(selectedSubscription.getKey()).thenReturn(-1L);
        when(subscriptionsService.getMySubscriptionsWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(resp);
        when(subscriptionsService.getMySubscriptionsSizeWithFiltering(any(PaginationFullTextFilter.class))).thenReturn(totalCount);
        when(subscriptionsService.getMySubscriptionDetails(-1L)).thenReturn(null);
        ArrangeableState arrangeable = new ArrangeableState() {
            @Override
            public List<FilterField> getFilterFields() {
                return Collections.emptyList();
            }

            @Override
            public List<SortField> getSortFields() {
                return Collections.emptyList();
            }

            @Override
            public Locale getLocale() {
                return Locale.JAPAN;
            }
        };
        when(model.getArrangeable()).thenReturn(arrangeable);

        //when
        List<POSubscription> result = model.getDataList(firstRow, numRows,
                Collections.<FilterField>emptyList(), Collections.<SortField>emptyList(), new Object());
        //then
        assertArrayEquals(expectedList.toArray(), result.toArray());
        assertEquals(model.getTotalCount(), totalCount);
        assertNull(model.getSelectedSubscription());
        assertNull(model.getSelectedSubscriptionId());
    }

    private List<POSubscription> prepareList() {
        List<POSubscription> cachedList = new ArrayList<>();
        POSubscription subAndCust = new POSubscription(new VOSubscription());
        subAndCust.setSubscriptionId("subId");
        cachedList.add(subAndCust);
        return cachedList;
    }

}
