/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.10.15 10:08
 *
 ******************************************************************************/

package org.oscm.app.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;

public class APPlatformControllerAdapterTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void constructorTest() throws APPlatformException {
        APPlatformController controllerInterface = mock(APPlatformController.class);
        APPlatformControllerAdapter adapter = new APPlatformControllerAdapter(
                controllerInterface);
        assertNotNull(adapter.getDelegate());
    }

    @Test
    public void testGetConverterData() throws Exception {

    }

    @Test
    public void testGetConverterException() throws Exception {

    }

    @Test
    public void testGetControllerInterface() throws Exception {

    }

    @Test
    public void testSetControllerInterface() throws Exception {

    }

    @Test
    public void testCreateInstance() throws Exception {

    }

    @Test
    public void testModifyInstance() throws Exception {

    }

    @Test
    public void testDeleteInstance() throws Exception {

    }

    @Test
    public void testGetInstanceStatus() throws Exception {

    }

    @Test
    public void testNotifyInstance() throws Exception {

    }

    @Test
    public void testActivateInstance() throws Exception {

    }

    @Test
    public void testDeactivateInstance() throws Exception {

    }

    @Test
    public void testCreateUsers() throws Exception {

    }

    @Test
    public void testDeleteUsers() throws Exception {

    }

    @Test
    public void testUpdateUsers() throws Exception {

    }

    @Test
    public void testGetControllerStatus() throws Exception {

    }

    @Test
    public void testGetOperationParameters() throws Exception {

    }

    @Test
    public void testExecuteServiceOperation() throws Exception {

    }

    @Test
    public void testSetControllerSettings() throws Exception {

    }
}