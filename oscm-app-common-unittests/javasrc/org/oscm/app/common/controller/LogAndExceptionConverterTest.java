/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 26.05.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.app.v2_0.data.Context;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.LogAndExceptionConverter;

/**
 * Unit test of configuration bean
 */
public class LogAndExceptionConverterTest {
    @Test
    public void testGetLogTextSimple() throws Exception {
        String text = LogAndExceptionConverter.getLogText("instance123", null);
        assertTrue(text.contains("instance123"));
        assertFalse(text.contains("OrganizationID"));
        assertFalse(text.contains("SubscriptionID"));
        assertFalse(text.contains("RequestingUser"));
    }

    @Test
    public void testGetLogTextMissingId() throws Exception {
        String text = LogAndExceptionConverter.getLogText(null, null);
        assertNotNull(text);
        assertEquals(0, text.length());
    }

    @Test
    public void testGetLogTextEmpty() throws Exception {
        ProvisioningSettings settings = new ProvisioningSettings(null, null,
                "en");
        String text = LogAndExceptionConverter.getLogText("instance123",
                settings);
        assertTrue(text.contains("instance123"));
        assertTrue(text.contains("OrganizationID"));
        assertTrue(text.contains("SubscriptionID"));
        assertFalse(text.contains("RequestingUser"));
    }

    @Test
    public void testGetLogText() throws Exception {
        ProvisioningSettings settings = new ProvisioningSettings(null, null,
                "en");
        settings.setOrganizationId("myOrg");
        String text = LogAndExceptionConverter.getLogText("instance123",
                settings);
        assertTrue(text.contains("instance123"));
        assertTrue(text.contains("OrganizationID"));
        assertTrue(text.contains("SubscriptionID"));
        assertTrue(text.contains("myOrg"));
        assertFalse(text.contains("RequestingUser"));
    }

    @Test
    public void testGetLogTextWithUser() throws Exception {
        ProvisioningSettings settings = new ProvisioningSettings(null, null,
                "en");
        settings.setOrganizationId("myOrg");
        ServiceUser user = new ServiceUser();
        user.setUserId("myUserId");
        settings.setRequestingUser(user);
        String text = LogAndExceptionConverter.getLogText("instance123",
                settings);
        assertTrue(text.contains("instance123"));
        assertTrue(text.contains("OrganizationID"));
        assertTrue(text.contains("SubscriptionID"));
        assertTrue(text.contains("myOrg"));
        assertTrue(text.contains("RequestingUser"));
        assertTrue(text.contains("myUserId"));
    }

    @Test
    public void testExceptionDefault() throws Exception {
        Throwable t = new Exception("problem1");
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.ACTIVATION);
        assertTrue(ex.getMessage().contains("problem1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testExceptionNoContext() throws Exception {
        Throwable t = new Exception("problem1");
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, null);
        assertTrue(ex.getMessage().contains("problem1"));
        assertTrue(ex.getMessage().contains("processing"));
        assertNull(ex.getCause());
    }

    @Test
    public void testNullPointerException() throws Exception {
        Throwable t = new NullPointerException();
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.CREATION);
        assertTrue(ex.getMessage().contains("NullPointerException"));
        assertNull(ex.getCause());
    }

    @Test
    public void testEJBException1() throws Exception {
        Throwable t = new EJBException("problem1");
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.DEACTIVATION);
        assertTrue(ex.getMessage().contains("problem1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testEJBException2() throws Exception {
        Exception cause = new Exception("cause1");
        Throwable t = new EJBException("problem1", cause);
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.DELETION);
        assertTrue(ex.getMessage().contains("cause1"));
        assertFalse(ex.getMessage().contains("problem1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testAPPException() throws Exception {
        Throwable t = new APPlatformException("problem1");
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.MODIFICATION);
        assertTrue(ex.getMessage().contains("problem1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testOViSSException1() throws Exception {
        Throwable t = new TestOViSSException("problem1");
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.OPERATION);
        assertTrue(ex.getMessage().contains("problem1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testOViSSException2() throws Exception {
        Exception cause = new Exception("cause1");
        Throwable t = new TestOViSSException("problem1", cause);
        APPlatformException ex = LogAndExceptionConverter
                .createAndLogPlatformException(t, Context.STATUS);
        assertTrue(ex.getMessage().contains("cause"));
        assertNull(ex.getCause());
    }

    @Test
    public void testContextEnum() throws Exception {
        // Coverage optimization
        new LogAndExceptionConverter();
        Context[] enumVals = Context.values();
        assertNotNull(enumVals);
        assertTrue(enumVals.length > 5);
        assertEquals(Context.ACTIVATION, Context.valueOf("ACTIVATION"));
    }

    /**
     * Internal helper class to test OViSS special handling
     */
    private class TestOViSSException extends Exception {
        private static final long serialVersionUID = -6153387271744164999L;

        public TestOViSSException(String message) {
            super(message);
        }

        public TestOViSSException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
