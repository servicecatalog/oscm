/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 28.04.15 08:08
 *
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.oscm.paginator.Pagination;
import org.richfaces.model.ArrangeableState;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.subscriptions.SubscriptionsService;

public class BPLazyDataModelTest {

    private BPLazyDataModel beanUnderTheTest = spy(new BPLazyDataModel());

    private SubscriptionsService subscriptionsService = mock(SubscriptionsService.class);


    @Test
    public void testInit() throws Exception {
        //when
        beanUnderTheTest.init();
        //then
        assertEquals(beanUnderTheTest.getColumnNamesMapping().size(), 5);
        assertEquals(beanUnderTheTest.getSortOrders().size(), 5);
    }

    @Test
    public void testGetDataList() throws Exception {
        //given
        int firstRow = 0;
        int numRows = 10;
        int totalCount = 1;
        List<POSubscriptionAndCustomer> expectedList = prepareList();
        Response resp = new Response(expectedList);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagers(any(Pagination.class))).thenReturn(resp);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagersSize(any(Pagination.class))).thenReturn(totalCount);
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
        when(beanUnderTheTest.getArrangeable()).thenReturn(arrangeable);
        doNothing().when(beanUnderTheTest).applyFilters(anyList(), any(Pagination.class));
        doNothing().when(beanUnderTheTest).decorateWithLocalizedStatuses(any(Pagination.class));
        beanUnderTheTest.setSubscriptionsService(subscriptionsService);

        //when
        List<POSubscriptionAndCustomer> result = beanUnderTheTest.getDataList(firstRow, numRows,
                Collections.<FilterField>emptyList(), Collections.<SortField>emptyList(), new Object());
        //then
        assertArrayEquals(expectedList.toArray(), result.toArray());
        assertEquals(beanUnderTheTest.getTotalCount(), totalCount);
    }

    @Test
    public void testGetTotalCount() throws Exception {
        //given
        beanUnderTheTest.setTotalCount(-1);
        when(subscriptionsService.getSubscriptionsAndCustomersForManagersSize(any(Pagination.class))).thenReturn(2);
        beanUnderTheTest.setSubscriptionsService(subscriptionsService);
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
        when(beanUnderTheTest.getArrangeable()).thenReturn(arrangeable);
        doNothing().when(beanUnderTheTest).applyFilters(anyList(), any(Pagination.class));
        //when
        int totalCount = beanUnderTheTest.getTotalCount();
        //then
        assertEquals(2, totalCount);
        verify(subscriptionsService, atLeastOnce()).getSubscriptionsAndCustomersForManagersSize(any(Pagination.class));
    }

    @Test
    public void testGetSubscriptionsListSize() throws Exception {
        //given
        when(beanUnderTheTest.getCachedList()).thenReturn(prepareList());
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
        when(beanUnderTheTest.getArrangeable()).thenReturn(arrangeable);
        doNothing().when(beanUnderTheTest).applyFilters(anyList(), any(Pagination.class));
        when(subscriptionsService.getSubscriptionsAndCustomersForManagersSize(any(Pagination.class))).thenReturn(1);
        beanUnderTheTest.setSubscriptionsService(subscriptionsService);
        //when
        int totalCount = beanUnderTheTest.getTotalCount();
        //then
        assertEquals(1,totalCount);
    }

    @Test
    public void testGetCurrentRowIndex_null() throws Exception {
        //given
        beanUnderTheTest.setSelectedSubscriptionAndCustomer(null);

        //when
        int currentRowIndex = beanUnderTheTest.getCurrentRowIndex();

        //then
        assertEquals(-1, currentRowIndex);
    }

    @Test
    public void testGetCurrentRowIndex() throws Exception {
        //given
        POSubscriptionAndCustomer selectedSubscriptionAndCustomer = new POSubscriptionAndCustomer();
        selectedSubscriptionAndCustomer.setSubscriptionId("subId");
        selectedSubscriptionAndCustomer.setCustomerId("subId");
        beanUnderTheTest.setSelectedSubscriptionAndCustomer(selectedSubscriptionAndCustomer);
        when(beanUnderTheTest.getCachedList()).thenReturn(prepareList());

        //when
        int currentRowIndex = beanUnderTheTest.getCurrentRowIndex();

        //then
        assertEquals(0, currentRowIndex);
    }

    private List<POSubscriptionAndCustomer> prepareList() {
        List<POSubscriptionAndCustomer> cachedList = new ArrayList<>();
        POSubscriptionAndCustomer subAndCust = new POSubscriptionAndCustomer();
        subAndCust.setCustomerId("subId");
        subAndCust.setSubscriptionId("subId");
        cachedList.add(subAndCust);
        return cachedList;
    }
}
