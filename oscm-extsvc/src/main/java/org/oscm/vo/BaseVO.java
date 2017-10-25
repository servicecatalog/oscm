/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

/**
 * Base class for all value object classes which defines the mandatory
 * attributes of the value objects. The mandatory attributes include the numeric
 * key and the version number. The latter is required to ensure correct locking
 * behavior.
 * 
 */
public abstract class BaseVO implements Serializable {

    private static final long serialVersionUID = 2678038734981795074L;

    private long key;
    private int version;

    /**
     * Default constructor.
     */
    protected BaseVO() {
        this.key = 0;
        this.version = 0;
    }

    /**
     * Constructs a value object with the given key and version.
     * 
     * @param key
     *            the numeric key
     * @param version
     *            the version
     */
    protected BaseVO(long key, int version) {
        this.key = key;
        this.version = version;
    }

    /**
     * Retrieves the numeric key of the value object.
     * 
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * Retrieves the version of the value object.
     * 
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the numeric key for the value object.
     * 
     * @param key
     *            the key
     */

    public void setKey(long key) {
        this.key = key;
    }

    /**
     * Sets the version of the value object.
     * 
     * @param version
     *            the version
     */
    public void setVersion(int version) {
        this.version = version;
    }

}
