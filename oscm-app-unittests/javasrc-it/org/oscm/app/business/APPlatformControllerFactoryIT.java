/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.business;

import javax.naming.InitialContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.app.adapter.APPlatformControllerAdapter;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.intf.APPlatformController;

/**
 * Unit tests for {@link APPlatformControllerFactory};
 * 
 * @author hoffmann
 */
public class APPlatformControllerFactoryIT extends EJBTestBase {

    private InitialContext context;

    @Override
    protected void setup(TestContainer container) throws Exception {
        context = new InitialContext();
    }

    @Test(expected = ControllerLookupException.class)
    public void testWrongInterface() throws Exception {

        context.bind(APPlatformController.JNDI_PREFIX + "test.controller",
                new String());
        APPlatformControllerFactory.getInstance("test.controller");
    }

    @Test
    public void testOK() throws Exception {

        new APPlatformControllerFactory(); // coverage
        context.bind(APPlatformController.JNDI_PREFIX + "test.controller",
                Mockito.mock(APPlatformController.class));
        APPlatformController instance = APPlatformControllerFactory
                .getInstance("test.controller");
        Assert.assertTrue(instance instanceof APPlatformControllerAdapter);
    }

    @Test(expected = ControllerLookupException.class)
    public void testNotBound() throws Exception {
        APPlatformControllerFactory.getInstance("not-existing.controller");
    }

}
