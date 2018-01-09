/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 05.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.data;

import org.oscm.provisioning.intf.ProvisioningService;

/**
 * Enumeration to represent the currently supported namespaces for the
 * provisioning service.
 * 
 * @author Mike J&auml;ger
 * 
 */
public enum SupportedProvisioningVersions {

    /**
     * Current Version
     */
    VERSION_1_7(ProvisioningService.class, "http://oscm.org/xsd", "v1.7"), //
    VERSION_1_8(ProvisioningService.class, "http://oscm.org/xsd", "v1.8");

    private Class<?> versionClass;
    private String version;
    private String namespace;

    private SupportedProvisioningVersions(Class<?> versionClass,
            String namespace, String version) {
        this.versionClass = versionClass;
        this.namespace = namespace;
        this.version = version;
    }

    public static SupportedProvisioningVersions getForClass(
            Class<?> serviceClass) {
        for (SupportedProvisioningVersions current : SupportedProvisioningVersions
                .values()) {
            if (current.versionClass.equals(serviceClass)) {
                return current;
            }
        }
        return null;
    }

    public static SupportedProvisioningVersions getForVersionString(
            String version) {
        for (SupportedProvisioningVersions current : SupportedProvisioningVersions
                .values()) {
            if (current.version.equals(version)) {
                return current;
            }
        }
        return null;
    }

    public static SupportedProvisioningVersions getForNamespaceString(
            String namespace) {
        for (SupportedProvisioningVersions current : SupportedProvisioningVersions
                .values()) {
            if (current.namespace.equals(namespace)) {
                return current;
            }
        }
        return null;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class<?> getServiceClass() {
        return versionClass;
    }

}
