/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Dec 2, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.v2_0.exceptions.APPlatformException;

public class KeystoneClientTest {
    private final MockURLStreamHandler streamHandler = new MockURLStreamHandler();

    @Before
    public void setUp() throws Exception {
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
    }

    @Test
    public void authenticate() throws APPlatformException,
            OpenStackConnectionException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticate("user", "password", "domainName", "tenantId");
    }

    @Test
    public void authenticateWithNoDomain() throws APPlatformException,
            OpenStackConnectionException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticate("user", "password", "", "tenantId");
    }

    @Test
    public void authenticate_http_666() throws APPlatformException,
            OpenStackConnectionException {
        // given
        int status = 666;
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(status,
                ""));

        try {
            // when
            new KeystoneClient(
                    new OpenStackConnection("https://xyz.de/v3/auth"))
                    .authenticate("user", "password", "domainName", "tenantId");
            assertTrue("Test must fail at this point", false);
        } catch (RuntimeException ex) {
            // then
            assertTrue("Wrong exception message!",
                    ex.getMessage().indexOf("" + status) > -1);
        }
    }

    @Test(expected = OpenStackConnectionException.class)
    public void authenticate_malFormedURL() throws APPlatformException,
            OpenStackConnectionException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("xyz")).authenticate("user",
                "password", "domainName", "tenantId");
    }

    @Test(expected = RuntimeException.class)
    public void authenticate_noJSONResponse() throws APPlatformException,
            OpenStackConnectionException {
        // given
        streamHandler.put("/v3/auth/tokens",
                new MockHttpsURLConnection(201, ""));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticate("user", "password", "domainName", "tenantId");

    }

    @Test(expected = APPlatformException.class)
    public void authenticate_noHeatEndpoint() throws APPlatformException,
            OpenStackConnectionException {
        // given
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(201,
                MockURLStreamHandler.respTokens(false, true, true)));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticate("user", "password", "domainName", "tenantId");
    }

    @Test(expected = APPlatformException.class)
    public void authenticate_noNovaEndpoint() throws APPlatformException,
            OpenStackConnectionException {
        // given
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(201,
                MockURLStreamHandler.respTokens(true, false, true)));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticate("user", "password", "domainName", "tenantId");
    }

}
