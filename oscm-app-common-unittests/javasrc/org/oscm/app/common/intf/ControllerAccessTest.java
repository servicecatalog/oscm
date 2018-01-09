/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 23.06.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common.intf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Ignore;

import org.oscm.app.common.ui.ConfigurationBean;
import org.oscm.app.v2_0.intf.ControllerAccess;

/**
 * Abstract base class for testing common functionality within each controller.
 */
@Ignore
public abstract class ControllerAccessTest {

    protected void checkMessageProperties(String locale, ControllerAccess ca)
            throws Exception {
        assertNotNull(ca);
        assertNotNull(locale);
        HashSet<String> keys = new HashSet<String>(
                ca.getControllerParameterKeys());
        assertNotNull(keys);
        assertFalse(keys.isEmpty());
        keys.addAll(Arrays.asList(ConfigurationBean.ACCESS_PARAMETERS));
        HashSet<String> keys2 = new HashSet<String>();

        for (String key : keys) {
            keys2.add(ConfigurationBean.MSG_DISPLAYNAME_PREFIX + key);
            keys2.add(ConfigurationBean.MSG_TOOLTIP_PREFIX + key);
        }
        keys2.add(ConfigurationBean.MSG_CONFIG_TITLE);
        for (String key : keys2) {
            String message = ca.getMessage(locale, key, new Object[0]);
            String error = "Missing message for key " + key + " in locale "
                    + locale;
            assertNotNull(error, message);
            assertFalse(error, message.startsWith("!"));
        }
    }
}
