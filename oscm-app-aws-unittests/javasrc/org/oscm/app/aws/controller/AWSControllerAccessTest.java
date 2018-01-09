/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 03.06.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.common.intf.ControllerAccessTest;

public class AWSControllerAccessTest extends ControllerAccessTest {

    @Test
    public void testGetControllerId() throws Exception {
        assertEquals(AWSController.ID,
                new AWSControllerAccess().getControllerId());
    }

    @Test
    public void testGetMessage() throws Exception {
        assertNotNull(new AWSControllerAccess().getMessage(
                Messages.DEFAULT_LOCALE, "key", "args0"));
    }

    @Test
    public void testGetConfigKeys() throws Exception {
        List<String> controllerParameterKeys = new AWSControllerAccess()
                .getControllerParameterKeys();
        assertNotNull(controllerParameterKeys);
        assertEquals(2, controllerParameterKeys.size());
    }

    @Test
    public void testProperties() throws Exception {
        checkMessageProperties(Messages.DEFAULT_LOCALE,
                new AWSControllerAccess());
    }
}
