/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.j2ep;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import net.sf.j2ep.model.Server;
import net.sf.j2ep.model.ServerContainer;

import org.junit.Test;

import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpServletResponseStub;

/**
 * Base server test class
 * 
 * @author pock
 */
public abstract class ServerTestBase {

    protected Server server;
    protected ServerContainer serverContainer;

    protected static final String domainName = "domainName";
    protected static final String path = "path";

    @Test
    public void testGetter() {
        Assert.assertEquals(domainName, server.getDomainName());
        Assert.assertEquals(path, server.getPath());
    }

    @Test
    public void testPreExecute() {
        HttpServletRequest request = new HttpServletRequestStub();
        Assert.assertEquals(request, server.preExecute(request));
    }

    @Test
    public void testPostExecute() {
        HttpServletResponse response = new HttpServletResponseStub();
        Assert.assertEquals(response, server.postExecute(response));
    }

    @Test
    public void testGetServerFromAttribute() {
        HttpServletRequest request;
        request = new HttpServletRequestStub() {
            public Object getAttribute(String name) {
                return server;
            }
        };
        Assert.assertEquals(server, serverContainer.getServer(request));
    }

    @Test
    public void testGetServerMapped() {
        Assert.assertEquals(null, serverContainer.getServerMapped(null));
    }

}
