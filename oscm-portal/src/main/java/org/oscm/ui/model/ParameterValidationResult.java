/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *  Completion Time: 18.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.io.Serializable;

/**
 * Wrapper Class for VOService which holds additional view attributes.
 * 
 */
public class ParameterValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean validationError = false;
    private String configRequest = null;

    public ParameterValidationResult(boolean error, String request) {
        this.validationError = error;
        this.configRequest = request;
    }

    public boolean getValidationError() {
        return validationError;
    }

    public void setValidationError(boolean validationError) {
        this.validationError = validationError;
    }

    public String getConfigRequest() {
        return configRequest;
    }

    public void setConfigRequest(String configRequest) {
        this.configRequest = configRequest;
    }

}
