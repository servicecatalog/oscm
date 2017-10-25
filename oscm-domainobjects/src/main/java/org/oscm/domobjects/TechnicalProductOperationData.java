/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author weiser
 * 
 */
@Embeddable
public class TechnicalProductOperationData extends DomainDataContainer {

    private static final long serialVersionUID = -4046480195177952568L;

    @Column(nullable = false, updatable = false)
    private String operationId;

    @Column(nullable = false)
    private String actionUrl;

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
