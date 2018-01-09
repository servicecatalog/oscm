/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.i18n;

import org.junit.Assert;

import org.junit.Test;

public class MessagesTest {

    @Test
    public void testMissingLocale() throws Exception {
        String fallback = Messages.get("missing", "error_configuration");
        String def = Messages.get(Messages.DEFAULT_LOCALE,
                "error_configuration");
        Assert.assertEquals(def, fallback);
    }

    @Test
    public void testMissingKey() throws Exception {
        String message = Messages.get(Messages.DEFAULT_LOCALE, "missing");
        Assert.assertEquals("!missing!", message);
    }

    @Test
    public void testUIMissingLocale() throws Exception {
        String fallback = org.oscm.app.ui.i18n.Messages.get("missing",
                "error_configuration");
        String def = org.oscm.app.ui.i18n.Messages.get(
                org.oscm.app.ui.i18n.Messages.DEFAULT_LOCALE,
                "error_configuration");
        Assert.assertEquals(def, fallback);
    }

    @Test
    public void testUIMissingKey() throws Exception {
        String message = org.oscm.app.ui.i18n.Messages.get(
                org.oscm.app.ui.i18n.Messages.DEFAULT_LOCALE, "missing");
        Assert.assertEquals("!missing!", message);
    }
}
