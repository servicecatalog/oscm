/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

/**
 * Help class to guess the possible height of a rich extended data table.
 * 
 */
public class TableHeightMap implements Map<Long, Long> {

    public static final long MIN_TABLE_HEIGHT = 120;

    private long contentHeight;

    private boolean ie;

    public TableHeightMap(int contentHeight, boolean ie) {
        this.contentHeight = contentHeight;
        this.ie = ie;
    }

    public void clear() {
    }

    public boolean containsKey(Object arg0) {
        // we calculate a height for every key
        return true;
    }

    public boolean containsValue(Object arg0) {
        return true;
    }

    public Set<java.util.Map.Entry<Long, Long>> entrySet() {
        return null;
    }

    public Long get(Object key) {
        long height = MIN_TABLE_HEIGHT;
        if (key instanceof Number) {
            height = contentHeight - ((Number) key).longValue();
            if (height < MIN_TABLE_HEIGHT) {
                height = MIN_TABLE_HEIGHT;
            }
        }
        if (FacesContext.getCurrentInstance().getMaximumSeverity() != null) {
            // a message is displayed, we have less space for the table
            height -= 26;
        }

        if (ie) {
            // the internet explorer needs more place to render a page
            height -= 22;
        }

        return Long.valueOf(height);
    }

    public boolean isEmpty() {
        return false;
    }

    public Set<Long> keySet() {
        return null;
    }

    public Long put(Long arg0, Long arg1) {
        return get(arg0);
    }

    public void putAll(Map<? extends Long, ? extends Long> arg0) {
    }

    public Long remove(Object arg0) {
        return null;
    }

    public int size() {
        return 0;
    }

    public Collection<Long> values() {
        return null;
    }

}
