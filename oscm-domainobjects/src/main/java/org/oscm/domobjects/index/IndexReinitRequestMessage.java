/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects.index;

import java.io.Serializable;

/**
 * Indicator to the search index managing master node that the search index has
 * to be recreated based on the current database content.
 */
public class IndexReinitRequestMessage implements Serializable {

    private static final long serialVersionUID = 5847352160372311025L;

    /**
     * Indicates whether the index should be created in any case (
     * <code>true</code>) or only in case no index exists at all (
     * <code>false</code>).
     */
    private boolean forceIndexCreation = false;

    public IndexReinitRequestMessage(boolean forceIndexCreation) {
        this.forceIndexCreation = forceIndexCreation;
    }

    public void setForceIndexCreation(boolean forceIndexCreation) {
        this.forceIndexCreation = forceIndexCreation;
    }

    public boolean isForceIndexCreation() {
        return forceIndexCreation;
    }

}
