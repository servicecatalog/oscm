/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import org.oscm.internal.base.BasePO;

/**
 * Represents a role defined on a technical service.
 * 
 * @author weiser
 * 
 */
public class POServiceRole extends BasePO {

    private static final long serialVersionUID = -5486504976198811768L;

    private String id;
    private String name;

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
