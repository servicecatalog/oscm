/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.tables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.paginator.Filter;
import org.oscm.paginator.Sorting;

/**
 * Contains the pagination parameters.
 * @deprecated Use Pagination class from common module. {@link org.oscm.paginator.Pagination}
 */
@Deprecated
public class Pagination implements Serializable {

    private static final long serialVersionUID = 5658910751349761L;
    private int offset;
    private int limit;
    private Sorting sorting;
    private Set<Filter> filterSet;
    private String dateFormat;
    private Map<SubscriptionStatus, String> localizedStatusesMap;

    public Pagination(int offset, int limit) {
        this();
        this.offset = offset;
        this.limit = limit;
    }

    public Pagination() {
        localizedStatusesMap = new HashMap<>();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    public Set<Filter> getFilterSet() {
        return filterSet;
    }

    public void setFilterSet(Set<Filter> filterSet) {
        this.filterSet = filterSet;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Map<SubscriptionStatus, String> getLocalizedStatusesMap() {
        return localizedStatusesMap;
    }
}
