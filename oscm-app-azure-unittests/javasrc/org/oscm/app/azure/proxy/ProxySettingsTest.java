/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.proxy;


import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by PLGrubskiM on 2017-03-24.
 */
public class ProxySettingsTest {

    private static final String URL = "http://someUrl.proxy.host";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
    private static final String HOST = "proxy.host";
    private static final String NOPROXY_HOSTS = "*proxy.host*|*other.host*";
    private static final String PORT = "80";

    @Before
    public void setUp() {

    }

    @Test
    public void getProxyTest() {
        // given
        // when
        ProxySettings.getProxy(URL);
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest() {
        // given
        // when
        ProxySettings.useProxy(URL);
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest_malformed() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        // when
        try {
            ProxySettings.useProxy("thisIsNotURL");
        } catch (RuntimeException e) {
            // then
            Assert.assertTrue(e.getCause() instanceof MalformedURLException);
        }

    }

    @Test
    public void useProxyTest_onlyHost() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, "not digits");
        // when
        ProxySettings.useProxy(URL);
        // then
        // no exceptions

    }

    @Test
    public void useProxyTest_setProxy() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        System.setProperty(HTTP_NON_PROXY_HOSTS, HOST);
        // when
        ProxySettings.useProxy(URL);
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest_withNoProxy_1() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        System.setProperty(HTTP_NON_PROXY_HOSTS, NOPROXY_HOSTS);
        // when
        ProxySettings.useProxy(URL);
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest_withNoProxy_2() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        System.setProperty(HTTP_NON_PROXY_HOSTS, "*proxy.host|*other.host*");
        // when
        ProxySettings.useProxy("http://proxy.host");
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest_withNoProxy_3() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        System.setProperty(HTTP_NON_PROXY_HOSTS, "proxy.host*|*other.host*");
        // when
        ProxySettings.useProxy("http://proxy.host");
        // then
        // no exceptions
    }

    @Test
    public void useProxyTest_withNoProxy_4() {
        // given
        System.setProperty(HTTPS_PROXY_HOST, HOST);
        System.setProperty(HTTPS_PROXY_PORT, PORT);
        System.setProperty(HTTP_NON_PROXY_HOSTS, "proxy.host|*other.host*");
        // when
        ProxySettings.useProxy("http://proxy.host");
        // then
        // no exceptions
    }
}
