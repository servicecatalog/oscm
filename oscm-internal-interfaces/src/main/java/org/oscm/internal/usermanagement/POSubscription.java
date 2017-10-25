/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.base.BasePO;

/**
 * Represents an available subscription for assignment with the available
 * service roles and the one selected for assignment.
 * 
 * @author weiser
 * 
 */
public class POSubscription extends BasePO {

    private static final long serialVersionUID = -2803085724169806762L;

    private String id;
    private boolean assigned;
    private POUsagelicense usageLicense;

    private List<POServiceRole> roles = new ArrayList<POServiceRole>();

    /**
     * Default constructor.
     */
    public POSubscription() {
        super();
        this.usageLicense = new POUsagelicense();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<POServiceRole> getRoles() {
        return roles;
    }

    public void setRoles(List<POServiceRole> roles) {
        this.roles = roles;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public POUsagelicense getUsageLicense() {
        return usageLicense;
    }

    public void setUsageLicense(POUsagelicense usageLicense) {
        this.usageLicense = usageLicense;
    }
}
