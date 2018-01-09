/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.wsproxy;

import java.net.URL;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a model of the web service proxy and contains all necessary
 * properties for consuming a web service.
 * 
 */
public class WsInfo {
    private static Log logger = LogFactory.getLog(WsInfo.class);

    private String host;
    private String port;
    private String serviceName;
    private String serviceVersion;
    private ServicePort servicePort;
    private String tenantId;

    public WsInfo(String host, String port, String serviceName,
            ServicePort servicePort, String serviceVersion) {
        super();
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.servicePort = servicePort;
        this.setServiceVersion(serviceVersion);
    }

    public WsInfo() {
        super();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServicePort getServicePort() {
        return servicePort;
    }

    public void setServicePort(ServicePort servicePort) {
        this.servicePort = servicePort;
    }
    
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    /**
     * Returns a URL of the local wsdl file from the resource directory for a
     * service.
     * 
     * @return URL of the local wsdl.
     */
    public URL getLocalBssWsUrl() {
        URL wsdlURL = Thread.currentThread().getContextClassLoader()
                .getResource("/wsdl/" + serviceName + ".wsdl");
        logger.debug(wsdlURL);
        verifyAttributes(wsdlURL.toString());
        return wsdlURL;
    }

    /**
     * Returns a URL of the remote wsdl file.
     * 
     * @return URL as a string in format:
     *         https://<host>:<port>/<service_name>/<serviceport>?wsdl
     */
    public String getRemoteBssWsUrl() {
        String servletAddress = "https://" + host + ":" + port
                + "/oscm-webservices/";
        if (serviceVersion != null) {
            servletAddress += serviceVersion + "/";
        }
        servletAddress += serviceName + "/" + servicePort.name();
        
        String url = servletAddress + "?wsdl";
        
        if(ServicePort.STS.equals(servicePort)){
            String tenantParam = "&tenantID=" + tenantId;
            url+=tenantParam;
        }
      
        logger.debug(url);
        verifyAttributes(url);
        return url;
    }

    /**
     * Return an endpoint address for the web service.
     * 
     * @return URL in format: https://<host>:<port>/<service_name>/<serviceport>
     */
    public String getEndpointAddress() {
        String endpointAddress = "https://" + host + ":" + port + "/oscm-webservices/"
                + serviceName + "/" + servicePort.name();
        return endpointAddress;
    }

    void verifyAttributes(String url) {
        if (host == null || port == null || serviceName == null
                || servicePort == null) {

            String text = "Error: WS URL not correct  " + url;
            logger.error(text);
            throw new WebServiceException(text);
        }
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

}
