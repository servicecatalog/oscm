/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.base;

import java.io.Serializable;

public abstract class BasePO implements Serializable {

    private static final long serialVersionUID = -8229831915605055495L;

    protected long key;
    protected int version;

    /**
     * Default constructor.
     */
    public BasePO() {
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
    public BasePO(long key, int version) {
        this.key = key;
        this.version = version;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (key ^ (key >>> 32));
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BasePO other = (BasePO) obj;
        if (key != other.key)
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
