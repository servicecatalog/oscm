/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.ui;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.v1_0.data.PasswordAuthentication;
import org.oscm.app.v1_0.exceptions.APPlatformException;
import org.oscm.app.v1_0.intf.APPlatformService;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by PLGrubskiM on 2017-03-22.
 */
public class ConfigurationBeanTest {

    ConfigurationBean configurationBean;
    APPlatformService mockAppService;

    @Before
    public void setUp() {
        mockAppService = mock(APPlatformService.class);
        configurationBean = new ConfigurationBean(mockAppService);

    }

    @Test
    public void getItemsTest_empty() throws APPlatformException {
        // given
        // when
        final HashMap<String, String> items = configurationBean.getItems();
        // then
        assertTrue(items.isEmpty());
        verify(mockAppService, times(1))
                .getControllerSettings(any(String.class), any(PasswordAuthentication.class));
    }

    @Test
    public void getItemsTest_exception() throws APPlatformException {
        // given
        doThrow(new APPlatformException("exception"))
                .when(mockAppService).getControllerSettings(any(String.class), any(PasswordAuthentication.class));
        // when
        final HashMap<String, String> items = configurationBean.getItems();
        // then
        assertTrue(items.size() == 4);
    }

    @Test
    public void getItemKeysTest() throws APPlatformException {
        // given
        // when
        final List<String> itemKeys = configurationBean.getItemKeys();
        // then
        assertTrue(itemKeys.isEmpty());
        verify(mockAppService, times(1))
                .getControllerSettings(any(String.class), any(PasswordAuthentication.class));
    }

    @Test
    public void saveTest() throws APPlatformException {
        // given
        // when
        configurationBean.save();
        // then
        assertTrue(configurationBean.getStatus() != null);
        verify(mockAppService, times(1))
                .storeControllerSettings(any(String.class), any(HashMap.class), any(PasswordAuthentication.class));
    }

}
