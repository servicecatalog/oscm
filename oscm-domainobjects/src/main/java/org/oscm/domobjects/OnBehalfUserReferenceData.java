/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: 26.05.2011                                                      
 *                                                                              
 *  Completion Time: 26.05.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container for the domain object <code>OnBehalfUserReference</code>.
 * 
 * @author tokoda
 * 
 */
@Embeddable
public class OnBehalfUserReferenceData extends DomainDataContainer {

    private static final long serialVersionUID = 1245262248642707380L;

    /**
     * The last time that the slave user was last logged in.
     */
    @Column(nullable = false)
    private long lastAccessTime;

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

}
