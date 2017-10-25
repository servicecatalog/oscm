/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 31.05.2016
 *
 *******************************************************************************/

package org.oscm.paginator;

/**
 * Class which transports full text filter value.
 */
public class PaginationFullTextFilter extends Pagination {
    public PaginationFullTextFilter() {
    }

    public PaginationFullTextFilter(int offset, int limit) {
        super(offset, limit);
    }

    private String fullTextFilterValue;

    public String getFullTextFilterValue() {
        return fullTextFilterValue;
    }

    public void setFullTextFilterValue(String fullTextFilterValue) {
        this.fullTextFilterValue = fullTextFilterValue;
    }
}
