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
public class POCustomer extends BasePO {

    private static final long serialVersionUID = -1433596636475826557L;

    String id;
    String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
