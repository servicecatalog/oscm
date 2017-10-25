/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

import org.oscm.types.enumtypes.Sorting;

/**
 * Wrapper object for search and list parameters including filtering, paging,
 * and sorting criteria.
 * 
 */
public class ListCriteria implements Serializable {

    private static final long serialVersionUID = -2247570459651301358L;

    private int offset;
    private int limit;
    private String filter; // texts and tags
    private Sorting sorting;

    private String categoryId;

    /**
     * Returns the offset of the first result to include in the output.
     * 
     * @return the offset of the first result
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the first result to include in the output. A negative
     * offset is handled like an offset of zero.
     * 
     * @param offset
     *            the offset of the first result
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the maximum number of results that are to be returned beginning
     * with the one obtained by {@link #getOffset()}.
     * 
     * @return the maximum number of results
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the maximum number of results that are to be returned beginning with
     * the one obtained by {@link #getOffset()}. A negative limit returns all
     * results.
     * 
     * @param limit
     *            the maximum number of results
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Returns the filter criteria; for retrieving services, this is the tag. If
     * the value is empty (set to <code>null</code>), the results are not
     * filtered.
     * 
     * @return the filter criteria
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter criteria; for retrieving services, this is the tag. If
     * set to <code>null</code>), the results are not filtered.
     * 
     * @param filter
     *            the filter criteria
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Returns the sorting criteria for the results.
     * 
     * @return the sorting criteria
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * Sets the sorting criteria for the results.
     * 
     * @param sorting
     *            the sorting criteria
     */
    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    /**
     * Returns the category which must be assigned to an item to be included in
     * the results.
     * 
     * @return the category identifier
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category which must be assigned to an item to be included in the
     * results.
     * 
     * @param categoryId
     *            the category identifier
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
