/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 18.11.15 16:44
 *
 *******************************************************************************/
package org.oscm.paginator;

import java.util.Set;

public interface PaginationInt {
    public int getOffset();

    public void setOffset(int offset);

    public int getLimit();

    public void setLimit(int limit);

    public Sorting getSorting();

    public void setSorting(Sorting sorting);

    public Set<Filter> getFilterSet();

    public void setFilterSet(Set<Filter> filterSet);

    public String getDateFormat();

    public void setDateFormat(String dateFormat);
}
