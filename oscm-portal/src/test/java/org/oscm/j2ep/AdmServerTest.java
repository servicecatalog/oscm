/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.j2ep;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.logging.LoggerFactory;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;
import org.oscm.internal.vo.VOSubscription;

/**
 * Test class
 * 
 * @author pock
 */
public class AdmServerTest extends ServerTestBase {

    @Before
    public void setup() {

        AdmServer server = new AdmServer();
        server.setDomainName(domainName);
        server.setPath(path);
        server.setConnectionExceptionRecieved(new NullPointerException());

        this.server = server;
        this.serverContainer = server;

        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    @Test
    public void testGetServer() {
        HttpServletRequest request;
        request = new HttpServletRequestStub() {

            public String getRequestURI() {
                return "/opt/1000/img/logo.gif";
            }

            public String getContextPath() {
                return "";
            }

            public HttpSession getSession() {
                return new HttpSessionStub(Locale.ENGLISH) {

                    public Object getAttribute(String name) {
                        Map<String, VOSubscription> map = new HashMap<String, VOSubscription>();
                        VOSubscription sub = new VOSubscription();
                        sub.setServiceBaseURL("http://localhost:1080/example");
                        map.put("1000", sub);
                        return map;
                    }
                };
            }
        };

        AdmServer server = (AdmServer) serverContainer.getServer(request);

        Assert.assertEquals("localhost:1080", server.getDomainName());
        Assert.assertEquals(AdmRule.class, server.getRule().getClass());
    }

}
