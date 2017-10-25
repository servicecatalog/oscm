/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.data;

import java.net.URL;

/**
 * Enumeration to represent the currently supported namespaces for the
 * notification service.
 * 
 * @author weiser
 * 
 */
public enum SupportedVersions {

    /**
     * current version in system
     */
    CURRENT_VERSION(
            org.oscm.notification.intf.NotificationService.class,
            "http://oscm.org/xsd", "v1.9",
            "/wsdl/notification/NotificationService.wsdl");

    private Class<?> versionClass;
    private String version;
    private String localWsdlUrl;
    private String namespace;

    private SupportedVersions(Class<?> versionClass, String namespace,
            String version, String wsdlPath) {
        this.versionClass = versionClass;
        this.namespace = namespace;
        this.version = version;
        this.localWsdlUrl = wsdlPath;

    }

    public static SupportedVersions getForClass(Class<?> serviceClass) {
        for (SupportedVersions current : SupportedVersions.values()) {
            if (current.versionClass.equals(serviceClass)) {
                return current;
            }
        }
        return null;
    }

    public static SupportedVersions getForVersionString(String version) {
        for (SupportedVersions current : SupportedVersions.values()) {
            if (current.version.equals(version)) {
                return current;
            }
        }
        return null;
    }

    public static SupportedVersions getForNamespaceString(String namespace) {
        for (SupportedVersions current : SupportedVersions.values()) {
            if (current.namespace.equals(namespace)) {
                return current;
            }
        }
        return null;
    }

    public String getVersion() {
        return version;
    }

    public Class<?> getServiceClass() {
        return versionClass;
    }

    public String getNamespace() {
        return namespace;
    }

    public URL getLocalWSDL() {
        return this.getClass().getResource(localWsdlUrl);
    }

}
