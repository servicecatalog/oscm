/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.05.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.common.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.i18n.Messages;

/**
 * Unit test of property handler
 */
public class MessagesTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMissingResource() throws Exception {
        String message = Messages.get("de", "key");
        assertEquals("!key!", message);
    }

    @Test
    public void testLocalizedResources() throws Exception {
        String message1 = Messages.get("de", "ui.config.column.value");
        String message2 = Messages.get("en", "ui.config.column.value");
        String message3 = Messages.get("fr", "ui.config.column.value");
        assertFalse(message1.equals(message2));
        assertTrue(message2.equals(message3));
    }

    @Test
    public void testGetAll() throws Exception {
        List<LocalizedText> all = Messages.getAll("error_overall_undefined",
                "123456789");
        assertNotNull(all);
        assertTrue(all.size() > 0);
        assertTrue(all.get(0).getText().contains("123456789"));
    }

    @Test
    public void testGetAll2() throws Exception {
        List<LocalizedText> all = Messages.getAll("error_overall_undefined");
        assertNotNull(all);
        assertTrue(all.size() > 0);
        assertTrue(all.get(0).getText().contains("{0}"));
    }
}
