/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.oscm.paginator.*;

/**
 * @author stavreva
 * 
 */
public class SubscriptionDaoTest {

    private final SubscriptionDao dao = new SubscriptionDao(null);

    @Test
    public void getQuerySubscriptionForMyCustomers_All() {
        // given
        Pagination pagination = new Pagination();
        pagination.setFilterSet(createFilterSet());
        pagination.setSorting(new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC));

        // when
        String query = dao.getQuerySubscriptionsForMyCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("ASC"));
        assertTrue(query.contains("LIKE"));
    }

    private Set<Filter> createFilterSet() {
        Filter filter = new Filter(TableColumns.SUBSCRIPTION_ID, "ABC");
        Set<Filter> filterSet = new HashSet<Filter>();
        filterSet.add(filter);
        return filterSet;
    }

    @Test
    public void getQuerySubscriptionForMyCustomers_NoFilter() {
        // given
        Pagination pagination = new Pagination();
        pagination.setSorting(new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC));

        // when
        String query = dao.getQuerySubscriptionsForMyCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("ASC"));
        assertFalse(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyCustomers_NoSorting() {
        // given
        Pagination pagination = new Pagination();
        pagination.setFilterSet(createFilterSet());

        // when
        String query = dao.getQuerySubscriptionsForMyCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("DESC"));
        assertTrue(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyCustomers_NoSortingNoFilter() {
        // given
        Pagination pagination = new Pagination();

        // when
        String query = dao.getQuerySubscriptionsForMyCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("DESC"));
        assertFalse(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyBrokerCustomers_All() {
        // given
        Pagination pagination = new Pagination();
        pagination.setFilterSet(createFilterSet());
        pagination.setSorting(new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC));

        // when
        String query = dao
                .getQuerySubscriptionsForMyBrokerCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("ASC"));
        assertTrue(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyBrokerCustomers_NoFilter() {
        // given
        Pagination pagination = new Pagination();
        pagination.setSorting(new Sorting(TableColumns.SUBSCRIPTION_ID,
                SortOrder.ASC));

        // when
        String query = dao
                .getQuerySubscriptionsForMyBrokerCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("ASC"));
        assertFalse(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyBrokerCustomers_NoSorting() {
        // given
        Pagination pagination = new Pagination();
        pagination.setFilterSet(createFilterSet());

        // when
        String query = dao
                .getQuerySubscriptionsForMyBrokerCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("DESC"));
        assertTrue(query.contains("LIKE"));
    }

    @Test
    public void getQuerySubscriptionForMyBrokerCustomers_NoSortingNoFilter() {
        // given
        Pagination pagination = new Pagination();

        // when
        String query = dao
                .getQuerySubscriptionsForMyBrokerCustomers(pagination);

        // then
        assertTrue(query.contains("ORDER BY"));
        assertTrue(query.contains("DESC"));
        assertFalse(query.contains("LIKE"));
    }

}
