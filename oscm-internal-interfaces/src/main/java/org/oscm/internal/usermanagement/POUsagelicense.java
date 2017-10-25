/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-8                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import org.oscm.internal.base.BasePO;

/**
 * @author yuyin
 * 
 */
public class POUsagelicense extends BasePO {

    private static final long serialVersionUID = -6406144845402811741L;

    private POServiceRole poServieRole;

    public POServiceRole getPoServieRole() {
        return poServieRole;
    }

    public void setPoServieRole(POServiceRole poServieRole) {
        this.poServieRole = poServieRole;
    }
}
