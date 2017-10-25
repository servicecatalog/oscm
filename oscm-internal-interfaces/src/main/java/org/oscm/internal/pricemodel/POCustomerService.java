/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel;

import org.oscm.internal.base.BasePO;

/**
 * @author weiser
 * 
 */
public class POCustomerService extends BasePO {

    private static final long serialVersionUID = 7365684187425516330L;

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
