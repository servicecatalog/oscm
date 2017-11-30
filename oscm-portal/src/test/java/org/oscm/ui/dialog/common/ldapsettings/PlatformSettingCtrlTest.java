/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.dialog.common.ldapsettings.LdapSetting;
import org.oscm.ui.dialog.common.ldapsettings.LdapSettingConverter;
import org.oscm.ui.dialog.common.ldapsettings.PlatformSettingCtrl;
import org.oscm.ui.dialog.common.ldapsettings.PlatformSettingModel;
import org.oscm.internal.usermanagement.POLdapSetting;
import org.oscm.internal.usermanagement.UserManagementService;

public class PlatformSettingCtrlTest {

    private PlatformSettingCtrl ctrl;
    private LdapSettingConverter converter;
    private UserManagementService ums;
    private PlatformSettingModel model;
    private Set<POLdapSetting> serverSettings = new HashSet<POLdapSetting>();

    @Captor
    ArgumentCaptor<byte[]> writtenContent;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        ctrl = spy(new PlatformSettingCtrl());
        converter = spy(new LdapSettingConverter());
        ums = mock(UserManagementService.class);
        model = new PlatformSettingModel();
        ctrl.setModel(model);

        doReturn(converter).when(ctrl).getSettingConverter();
        doReturn(ums).when(ctrl).getUserManagementService();
        doReturn(serverSettings).when(ums).getPlatformSettings();
        doNothing().when(ctrl).writeSettings(writtenContent.capture());
    }

    @Test
    public void initModelData_Delegation() {
        ctrl.initModelData();
        verify(ums, times(1)).getPlatformSettings();
        verifyNoMoreInteractions(ums);
        verify(converter, times(1)).addToModel(
                same(model.getPlatformSettings()), same(serverSettings));
    }

    @Test
    public void exportSettings_Delegation() throws IOException {
        String result = ctrl.exportSettings();
        verify(converter, times(1)).toProperties(
                same(model.getPlatformSettings()), eq(false));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void exportSettings_ContentValidation() throws IOException {
        model.getPlatformSettings().add(new LdapSetting("key", "value", false));
        ctrl.exportSettings();
        ByteArrayInputStream bais = new ByteArrayInputStream(
                writtenContent.getValue());
        Properties props = new Properties();
        props.load(bais);
        assertEquals(1, props.keySet().size());
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void isPlatformSettingsDefined_ContentValidation_Empty() {
        assertFalse(ctrl.isPlatformSettingsDefined());
    }

    @Test
    public void isPlatformSettingsDefined_ContentValidation_SettingsDefined() {
        model.getPlatformSettings().add(new LdapSetting("key", "value", false));
        assertTrue(ctrl.isPlatformSettingsDefined());
    }

}
