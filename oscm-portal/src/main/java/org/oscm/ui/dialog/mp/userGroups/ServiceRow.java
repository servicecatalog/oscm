/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-2-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import org.oscm.internal.usergroupmgmt.POService;

/**
 * Row of Service table
 * 
 * @author Mao
 * 
 */
public class ServiceRow {

    private POService service;

    private boolean selected = true;

    public ServiceRow(POService service, boolean selected) {
        this.service = service;
        this.selected = selected;
    }

    public POService getService() {
        return service;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setService(POService service) {
        this.service = service;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
