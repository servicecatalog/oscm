/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.data;

import java.net.URL;

import org.oscm.psp.intf.PaymentServiceProvider;

/**
 * Enumeration to represent the currently supported name spaces for the payment
 * service provider interface.
 * 
 * @author weiser
 * 
 */
public enum SupportedVersions {

    /**
     * Current Version
     */
    CURRENT_VERSION(PaymentServiceProvider.class, "http://oscm.org/xsd",
            "/wsdl/psp/PaymentServiceProvider.wsdl", "v1.9");

    private Class<?> versionClass;
    private String namespace;
    private String version;
    private String localWsdlUrl;

    private SupportedVersions(Class<?> versionClass, String namespace,
            String wsdlPath, String version) {
        this.versionClass = versionClass;
        this.namespace = namespace;
        this.localWsdlUrl = wsdlPath;
        this.version = version;
    }

    public static SupportedVersions getForClass(Class<?> serviceClass) {
        for (SupportedVersions current : SupportedVersions.values()) {
            if (current.versionClass.equals(serviceClass)) {
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

    public static SupportedVersions getForVersion(String version) {
        for (SupportedVersions current : SupportedVersions.values()) {
            if (current.version.equals(version)) {
                return current;
            }
        }
        return null;
    }

    public String getNamespace() {
        return namespace;
    }

    public Class<?> getServiceClass() {
        return versionClass;
    }

    public URL getLocalWSDL() {
        return this.getClass().getResource(localWsdlUrl);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
