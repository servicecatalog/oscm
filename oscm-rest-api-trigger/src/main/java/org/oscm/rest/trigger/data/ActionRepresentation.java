/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.data;

import javax.ws.rs.WebApplicationException;

import org.oscm.rest.common.Representation;

/**
 * Representation class for trigger actions
 * 
 * @author miethaner
 */
public class ActionRepresentation extends Representation {

    public enum Action {
        SUBSCRIBE_TO_SERVICE, UNSUBSCRIBE_FROM_SERVICE, MODIFY_SUBSCRIPTION
    }

    private Action description;

    public ActionRepresentation(Long id, Action description) {
        this.setId(id);
        this.description = description;
    }
}
