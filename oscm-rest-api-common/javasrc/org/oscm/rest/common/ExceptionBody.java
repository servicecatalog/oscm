/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: August 26, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.oscm.internal.types.exception.SaaSApplicationException;

public class ExceptionBody extends Representation {

    public static ExceptionBody fromSaasApplicationException(
            SaaSApplicationException e, Status s) {
        ExceptionBody eb = new ExceptionBody();
        eb.setCode(s.getStatusCode());
        eb.setMessage(e.getMessage());
        // TODO: more info and property
        return eb;
    }

    private int code;
    private Integer error;
    private String property;
    private String message;
    private String moreInfo;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    @Override
    public void validateContent() throws WebApplicationException {
    }
}
