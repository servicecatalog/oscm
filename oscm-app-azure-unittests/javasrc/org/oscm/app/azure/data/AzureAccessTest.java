/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by PLGrubskiM on 2017-03-27.
 */
public class AzureAccessTest {

    AzureAccess azureAccess;

    @Before
    public void setUp() {
        azureAccess = new AzureAccess();
    }

    @Test
    public void constructorTest() {
        // given
        String privateIp = "somePrvIp";
        String publcIp = "somePubIp";
        String state = "someState";
        String vmName = "someVmName";
        // when
        final List<String> privateIpAddress = azureAccess.getPrivateIpAddress();
        final List<String> publicIpAddress = azureAccess.getPublicIpAddress();
        final List<String> states = azureAccess.getState();
        final List<String> vmNames = azureAccess.getVmName();
        // then
        Assert.assertTrue(privateIpAddress.isEmpty());
        Assert.assertTrue(publicIpAddress.isEmpty());
        Assert.assertTrue(states.isEmpty());
        Assert.assertTrue(vmNames.isEmpty());
        // when
        List<String> prvIpList = new ArrayList<>();
        prvIpList.add(privateIp);
        List<String> pubIpList = new ArrayList<>();
        pubIpList.add(publcIp);
        List<String> stateList = new ArrayList<>();
        stateList.add(state);
        List<String> vmNameList = new ArrayList<>();
        vmNameList.add(vmName);

        azureAccess.setPrivateIpAddress(prvIpList);
        azureAccess.setPublicIpAddress(pubIpList);
        azureAccess.setState(stateList);
        azureAccess.setVmName(vmNameList);

        // then
        Assert.assertTrue(azureAccess.getPrivateIpAddress().contains(privateIp));
        Assert.assertTrue(azureAccess.getPublicIpAddress().contains(publcIp));
        Assert.assertTrue(azureAccess.getState().contains(state));
        Assert.assertTrue(azureAccess.getVmName().contains(vmName));
    }
}
