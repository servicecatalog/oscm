/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-07-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.pagination;

/**
 * Generic interface for limiting the number of elements retrieved from the
 * database in a list.
 */
public class Pagination {

    /**
     * The starting point in the complete list of elements.
     */
    private int offset;

    /**
     * The number of elements to be retrieved.
     */
    private int limit;

    /**
     * Default constructor.
     */
    public Pagination() {
    }

    /**
     * Constructs a pagination object with the given offset and number of
     * elements.
     * 
     * @param offset
     *            the first element to be retrieved, counting from the beginning
     *            of the complete list of elements
     * @param limit
     *            the number of elements to be retrieved
     */
    public Pagination(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    /**
     * Returns the first element to be retrieved, counting from the beginning of
     * the complete list of elements.
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the first element to be retrieved, counting from the beginning of
     * the complete list of elements.
     * 
     * @param offset
     *            the offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the number of elements to be retrieved from the database.
     * 
     * @return the number of elements
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the number of elements to be retrieved from the database.
     * 
     * @param limit
     *            the number of elements
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }
}
