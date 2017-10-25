/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 21.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.data;

import java.net.URL;

import org.oscm.operation.intf.OperationService;

/**
 * @author weiser
 * 
 */
public enum SupportedOperationVersions {

    /**
     * Current Version
     */
    VERSION_1_5(OperationService.class, "http://oscm.org/xsd", "v1.5",
            "/wsdl/operation/OperationService.wsdl");

    private Class<?> versionClass;
    private String namespace;
    private String version;
    private String localWsdlUrl;

    private SupportedOperationVersions(Class<?> versionClass, String namespace,
            String version, String wsdlPath) {
        this.versionClass = versionClass;
        this.namespace = namespace;
        this.version = version;
        this.localWsdlUrl = wsdlPath;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getVersion() {
        return version;
    }

    public Class<?> getServiceClass() {
        return versionClass;
    }

    public URL getLocalWSDL() {
        return this.getClass().getResource(localWsdlUrl);
    }

    public static SupportedOperationVersions getForVersionString(String version) {
        for (SupportedOperationVersions current : SupportedOperationVersions
                .values()) {
            if (current.getVersion().equals(version)) {
                return current;
            }
        }
        return null;
    }

    public static SupportedOperationVersions getForNamespaceString(
            String namespace) {
        for (SupportedOperationVersions current : SupportedOperationVersions
                .values()) {
            if (current.getNamespace().equals(namespace)) {
                return current;
            }
        }
        return null;
    }

}
