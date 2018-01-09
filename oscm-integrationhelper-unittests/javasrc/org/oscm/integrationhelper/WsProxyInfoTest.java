/*
 *  Copyright FUJITSU LIMITED 2018
 */

package org.oscm.integrationhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class WsProxyInfoTest {

    private static final String TOKENHANDLER_PROPERTY_FILE = "tokenhandler.properties";
    private static final String CURRENT_VERSION = "1.9";

    @Test
    public void WsProxyInfo_fileNameNull() {
        // given
        String fileName = null;
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        verifyAllAttributesAreNull(result);
    }

    @Test
    public void WsProxyInfo_ServiceNameNull() {
        // given
        String fileName = "test_samlSp.properties";
        String serviceName = null;

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        verifyAllAttributesAreNull(result);
    }

    @Test
    public void WsProxyInfo_sts() {
        // given
        String fileName = "test_sts.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        verifyAllAttributesAreSetForSamlSp(result);
    }

    @Test
    public void WsProxyInfo_basic() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        verifyAllAttributesAreSetForInternal(result);
    }

    @Test
    public void WsProxyInfo_clientcert() {
        // given
        String fileName = "test_clientcert.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        verifyAllAttributesAreSetForInternal(result);
    }

    @Test
    public void WsProxyInfo_basicBssWsUrl() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        assertEquals("https://xy.com:8081/oscm/" + CURRENT_VERSION
                + "/SessionService/BASIC?wsdl", result.getWsInfo()
                .getRemoteBssWsUrl());
    }

    @Test
    public void WsProxyInfo_clientcertBssWsUrl() {
        // given
        String fileName = "test_clientcert.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        assertEquals("https://xy.com:8081/oscm/" + CURRENT_VERSION
                + "/SessionService/CLIENTCERT?wsdl", result.getWsInfo()
                .getRemoteBssWsUrl());
    }

    @Test
    public void WsProxyInfo_stsBssWsUrl() {
        // given
        String fileName = "test_sts.properties";
        String serviceName = "SessionService";

        // when
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                TOKENHANDLER_PROPERTY_FILE);

        // then
        assertEquals("https://xy.com:8081/oscm/" + CURRENT_VERSION
                + "/SessionService/STS?wsdl", result.getWsInfo()
                .getRemoteBssWsUrl());
    }

    private void verifyAllAttributesAreNull(WsProxyInfo result) {
        assertNull(result.getServicePort());
        assertNull(result.getForward());
        assertNull(result.getWsInfo());
        assertNull(result.getUserCredentials());
    }

    private void verifyAllAttributesAreSetForSamlSp(WsProxyInfo result) {
        assertNotNull(result.getServicePort());
        assertNotNull(result.getForward());
        assertNotNull(result.getWsInfo());
        assertNotNull(result.getUserCredentials());
    }

    private void verifyAllAttributesAreSetForInternal(WsProxyInfo result) {
        assertNotNull(result.getServicePort());
        assertNotNull(result.getForward());
        assertNotNull(result.getWsInfo());
        assertNotNull(result.getUserCredentials());
    }

    @Test
    public void getAndLogTokenHandlerPropertyTest() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                "tokenhandlerNull.properties");
        assertNotNull(result.getServicePort());
        assertNull(result.getForward());
        assertNotNull(result.getWsInfo());
        assertNotNull(result.getUserCredentials());
    }

    @Test
    public void getAndLogTokenHandlerPropertyNullInputTest() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                "tokenhandlerNull.properties");
        assertNull(result.getAndLogTokenHandlerProperty(null));
    }

    @Test
    public void getAndLogTokenHandlerPropertyNoSuchPropertyTest() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                "tokenhandler.properties");
        assertNull(result.getAndLogTokenHandlerProperty("unknown"));
    }

    @Test
    public void getAndLogTokenHandlerPropertyValueTest() {
        // given
        String fileName = "test_basic.properties";
        String serviceName = "SessionService";
        WsProxyInfo result = new WsProxyInfo(fileName, serviceName,
                "tokenhandler.properties");
        assertEquals("/Welcome.jsp",
                result.getAndLogTokenHandlerProperty("FORWARD"));
    }

}
