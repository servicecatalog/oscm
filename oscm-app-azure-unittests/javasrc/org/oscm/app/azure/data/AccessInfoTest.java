/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.i18n.Messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by PLGrubskiM on 2017-03-22.
 */
public class AccessInfoTest {

    private static final String LOCALE_EN = "en";
    private static final String VM_NAME = "VmName1";
    private static final String STATE_NAME = "someState";
    private static final String PRIVATE_IP = "prvIP";
    private static final String PUBLIC_IP = "pubIP";

    List<AzureAccess> azureAccesses;

    AccessInfo accessInfo;

    @Before
    public void setUp() {
        accessInfo = new AccessInfo();
        azureAccesses = new ArrayList<>();
    }

    @Test
    public void getOutputTest_emptyAzureAccesses() {
        // given
        // when
        final String output = accessInfo.getOutput(LOCALE_EN);
        // then
        assertTrue(Messages.get(LOCALE_EN, "accessInfo_NOT_AVAILABLE").equals(output));
    }

    @Test
    public void getOutputTest_unsupportedLocale() {
        // given
        // when
        final String output = accessInfo.getOutput("pl");
        // then
        assertTrue(Messages.get("pl", "accessInfo_NOT_AVAILABLE").equals(output));
    }

    @Test
    public void getOutputTest_bothIPsSet() {
        // given
        setMockIPs(true, true);
        accessInfo.setAzureAccesses(azureAccesses);
        // when
        final String output = accessInfo.getOutput(LOCALE_EN);
        // then
        assertTrue(output.contains(VM_NAME));
        assertTrue(output.contains(STATE_NAME));
        assertTrue(output.contains(PRIVATE_IP));
        assertTrue(output.contains(PUBLIC_IP));

    }

    @Test
    public void getOutputTest_pubIPOnly() {
        // given
        setMockIPs(true, false);
        accessInfo.setAzureAccesses(azureAccesses);
        // when
        final String output = accessInfo.getOutput(LOCALE_EN);
        // then
        assertTrue(output.contains(VM_NAME));
        assertTrue(output.contains(STATE_NAME));
        assertTrue(output.contains(PUBLIC_IP));
        assertFalse(output.contains(PRIVATE_IP));

    }

    @Test
    public void getOutputTest_prvIPOnly() {
        // given
        setMockIPs(false, true);
        accessInfo.setAzureAccesses(azureAccesses);
        // when
        final String output = accessInfo.getOutput(LOCALE_EN);
        // then
        assertTrue(output.contains(VM_NAME));
        assertTrue(output.contains(STATE_NAME));
        assertTrue(output.contains(PRIVATE_IP));
        assertFalse(output.contains(PUBLIC_IP));

    }
    @Test
    public void getOutputTest_noIPsSet() {
        // given
        setMockIPs(false, false);
        accessInfo.setAzureAccesses(azureAccesses);
        // when
        final String output = accessInfo.getOutput(LOCALE_EN);
        // then
        assertTrue(output.contains(VM_NAME));
        assertTrue(output.contains(STATE_NAME));
        assertFalse(output.contains(PRIVATE_IP));
        assertFalse(output.contains(PUBLIC_IP));
        assertFalse(accessInfo.getAzureAccesses().isEmpty());

    }

    private void setMockIPs(boolean inclidePub, boolean includePrv) {
        AzureAccess mockAzureAccess = mock(AzureAccess.class);
        List<String> mockIPlist = new ArrayList<>();
        if (includePrv) {
            mockIPlist.add(PRIVATE_IP);
            when(mockAzureAccess.getPrivateIpAddress()).thenReturn(mockIPlist);
        }
        if (inclidePub) {
            mockIPlist.add(PUBLIC_IP);
            when(mockAzureAccess.getPublicIpAddress()).thenReturn(mockIPlist);
        }
        List<String> nameList = new ArrayList<>();
        nameList.add(VM_NAME);
        when(mockAzureAccess.getVmName()).thenReturn(nameList);
        List<String> stateList = new ArrayList<>();
        stateList.add(STATE_NAME);
        when(mockAzureAccess.getState()).thenReturn(stateList);
        azureAccesses.add(mockAzureAccess);
    }
}
