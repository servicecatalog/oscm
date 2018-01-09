/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 28, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.proxy;

import java.net.URL;

/**
 * @author farmaki
 * 
 */
public class ProxySettings {

    public static final String HTTPS_PROXY_HOST = "https.proxyHost";
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    public static final String HTTPS_PROXY_USER = "https.proxyUser";
    public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    public static boolean useProxyByPass(URL url) {

        String nonProxy = System.getProperty(HTTP_NON_PROXY_HOSTS);
        String hostName = url.getHost();
        if (nonProxy != null) {
            String[] split = nonProxy.split("\\|");
            for (int i = 0; i < split.length; i++) {
                String np = split[i].trim();
                if (np.length() > 0) {
                    boolean wcStart = np.startsWith("*");
                    boolean wcEnd = np.endsWith("*");
                    if (wcStart) {
                        np = np.substring(1);
                    }
                    if (wcEnd) {
                        np = np.substring(0, np.length() - 1);
                    }
                    if (wcStart && wcEnd && hostName.contains(np)) {
                        return true;
                    }
                    if (wcStart && hostName.endsWith(np)) {
                        return true;
                    }
                    if (wcEnd && hostName.startsWith(np)) {
                        return true;
                    }
                    if (np.equals(hostName)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
