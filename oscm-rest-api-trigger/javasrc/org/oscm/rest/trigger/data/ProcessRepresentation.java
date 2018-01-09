/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 3, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.trigger.data;

import javax.ws.rs.WebApplicationException;

import org.oscm.rest.common.CommonParams;
import org.oscm.rest.common.Representation;
import org.oscm.rest.common.WebException;
import org.oscm.rest.trigger.config.TriggerCommonParams;

/**
 * Representation class of trigger processes.
 * 
 * @author miethaner
 */
public class ProcessRepresentation extends Representation {

    private String comment;

    public ProcessRepresentation() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void validateContent() throws WebApplicationException {

        if (comment == null) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_COMMENT)
                    .message(CommonParams.ERROR_MANDATORY_PROPERTIES).build();

        } else if (!comment.matches(CommonParams.PATTERN_STRING)) {
            throw WebException.badRequest()
                    .property(TriggerCommonParams.PROPERTY_COMMENT)
                    .message(CommonParams.ERROR_BAD_PROPERTY).build();
        }
    }
}
