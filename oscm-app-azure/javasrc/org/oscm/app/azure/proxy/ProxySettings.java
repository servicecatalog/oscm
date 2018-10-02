/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.proxy;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxySettings {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ProxySettings.class);

    /**
     * System property https.proxyHost
     */
    public static final String HTTPS_PROXY_HOST = "https.proxyHost";

    /**
     * System property https.proxyPort
     */
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";

    /**
     * System property https.proxyUser
     */
    public static final String HTTPS_PROXY_USER = "https.proxyUser";

    /**
     * System property https.proxyPassword
     */
    public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    /**
     * System property https.nonProxyHosts
     */
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    /**
     * Azure
     */
    public static boolean useProxy(String url) {
        if (useProxyHost() && !useProxyByPass(url)) {
            logger.debug("ProxySettings.useProxy: true");
            return true;
        }
        logger.debug("ProxySettings.useProxy: false");
        return false;
    }

    /**
     * Azure
     */
    private static boolean useProxyHost() {
        String proxyHost = System.getProperty(HTTPS_PROXY_HOST);
        String proxyPort = System.getProperty(HTTPS_PROXY_PORT);

        if (StringUtils.isNotBlank(proxyHost)
                && NumberUtils.isDigits(proxyPort)) {
            return true;
        }
        return false;
    }

    /**
     * Azure
     */
    private static boolean useProxyByPass(String url) {
        String host;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURLException url=" + url, e);
        }

        String nonProxy = System.getProperty(HTTP_NON_PROXY_HOSTS);
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
                    if (wcStart && wcEnd && host.contains(np)) {
                        return true;
                    }
                    if (wcStart && host.endsWith(np)) {
                        return true;
                    }
                    if (wcEnd && host.startsWith(np)) {
                        return true;
                    }
                    if (host.equalsIgnoreCase(np)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Azure
     */
    public static Proxy getProxy(String url) {
        Proxy proxy = Proxy.NO_PROXY;
        if (useProxy(url)) {
            String proxyHost = System.getProperty(HTTPS_PROXY_HOST);
            String proxyPort = System.getProperty(HTTPS_PROXY_PORT);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost,
                    Integer.parseInt(proxyPort)));
        }
        return proxy;
    }
}
