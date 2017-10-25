/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.converters;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oscm.apiversioning.enums.ApiVersion;
import org.oscm.apiversioning.enums.ConverterType;

/**
 * @author qiu
 * 
 */
public class ConverterContext {

    private ApiVersion version;

    private ConverterType converterType;

    private String serviceName;

    private String methodName;

    private SOAPMessageContext soapContext;

    public ApiVersion getVersion() {
        return version;
    }

    public void setVersion(ApiVersion version) {
        this.version = version;
    }

    public ConverterType getConverterType() {
        return converterType;
    }

    public void setConverterType(ConverterType converterType) {
        this.converterType = converterType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public SOAPMessageContext getSoapContext() {
        return soapContext;
    }

    public void setSoapContext(SOAPMessageContext soapContext) {
        this.soapContext = soapContext;
    }

}
