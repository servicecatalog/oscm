/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationhelper;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oscm.wsproxy.ServicePort;
import org.oscm.wsproxy.UserCredentials;
import org.oscm.wsproxy.WsInfo;

/**
 * This class is a model for holding the properties to access the target web
 * service and token handler properties.
 */
public class WsProxyInfo {
    private static Log logger = LogFactory.getLog(WsProxyInfo.class);

    private Properties serviceClientProp = null;
    private Properties tokenHandlerProp = null;
    private WsInfo wsInfo = null;
    private ServicePort servicePort = null;
    private UserCredentials userCredentials = null;
    private String forward = null;
    private String serviceVersion = null;

    /**
     * Constructs an object of this class using the properties from the web
     * service client property file and token handler property file.
     * 
     * @param serviceClientFile
     *            - the name of the web service client property file
     * @param serviceName
     *            - the service name
     * @param tokenHandlerFile
     *            - the name of the token handler property file.
     */
    public WsProxyInfo(String serviceClientFile, String serviceName,
            String tokenHandlerFile) {
        if (serviceClientFile == null || serviceClientFile.trim().length() == 0
                || serviceName == null || serviceName.trim().length() == 0) {
            logger.warn("Error: empty parameters[file: " + serviceClientFile
                    + " service: " + serviceName + "]");
            return;
        }

        tokenHandlerProp = PropertyFileReader
                .getPropertiesFromFile(tokenHandlerFile);
        if (tokenHandlerProp == null) {
            logger.warn("Cannot read property file: " + tokenHandlerFile);
            return;
        }

        serviceClientProp = PropertyFileReader
                .getPropertiesFromFile(serviceClientFile);
        if (serviceClientProp == null) {
            logger.warn("Cannot read property file: " + serviceClientFile);
            return;
        }

        wsInfo = new WsInfo();
        wsInfo.setServiceName(serviceName);
        logger.debug("service name: " + serviceName);
        readWsProxyProperties();
    }

    /**
     * Constructs an object of this class using the web service client
     * properties.
     * 
     * @param serviceClientFileName
     *            - the name of the file for hte web service client properties.
     */
    public WsProxyInfo(String serviceClientFileName) {
        if (serviceClientFileName == null
                || serviceClientFileName.trim().length() == 0) {
            logger.warn("Error: empty parameter[file: " + serviceClientFileName
                    + "]");
            return;
        }

        serviceClientProp = PropertyFileReader
                .getPropertiesFromFile(serviceClientFileName);
        if (serviceClientProp == null) {
            logger.warn("Cannot read property file: " + serviceClientFileName);
            return;
        }

        wsInfo = new WsInfo();
        readWsProxyProperties();
    }

    public WsInfo getWsInfo() {
        return wsInfo;
    }

    public ServicePort getServicePort() {
        return servicePort;
    }

    public UserCredentials getUserCredentials() {
        return userCredentials;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getForward() {
        return forward;
    }

    /**
     * This method reads the properties from the property files. Used as a
     * helper method in constructors.
     */
    void readWsProxyProperties() {
        wsInfo.setHost(getAndLogServiceClientProperty(Constants.CM_HOST));
        wsInfo.setPort(getAndLogServiceClientProperty(Constants.CM_PORT));

        String servicePortProperty = getAndLogServiceClientProperty(Constants.CM_SERVICE_PORT);
        servicePort = ServicePort.valueOf(servicePortProperty);
        wsInfo.setServicePort(servicePort);

        String password = getAndLogServiceClientProperty(Constants.CM_SERVICE_USER_PWD);
        if (servicePort == ServicePort.STS) {
            userCredentials = new UserCredentials(
                    getAndLogServiceClientProperty(Constants.CM_SERVICE_USER_ID),
                    password);
        } else {
            userCredentials = new UserCredentials(
                    getAndLogServiceClientProperty(Constants.CM_SERVICE_USER_KEY),
                    password);
        }

        String serviceVersion = getAndLogServiceClientProperty(Constants.CM_SERVICE_VERSION);
        this.setServiceVersion(serviceVersion);
        wsInfo.setServiceVersion(serviceVersion);
        
        String tenantId = getAndLogServiceClientProperty(Constants.CM_SERVICE_TENANT_ID);
        wsInfo.setTenantId(tenantId);
        
        forward = getAndLogTokenHandlerProperty(Constants.FORWARD);
    }

    /**
     * Helper method to get and log the web service client properties. Password
     * value is not readable in the log.
     * 
     * @param property
     * @return
     */
    String getAndLogServiceClientProperty(String property) {
        String value = serviceClientProp.getProperty(property).trim();
        if (Constants.CM_SERVICE_USER_PWD.equals(property)) {
            logger.debug(property + ": *******");
        } else {
            logger.debug(property + ": " + value);
        }
        return value;
    }

    /**
     * Helper method to read and log the token handler properties.
     * 
     * @param property
     * @return
     */
    String getAndLogTokenHandlerProperty(String property) {
        if (property == null) {
            return null;
        }
        if (tokenHandlerProp.getProperty(property) == null) {
            return null;
        }
        return tokenHandlerProp.getProperty(property).trim();
    }

}