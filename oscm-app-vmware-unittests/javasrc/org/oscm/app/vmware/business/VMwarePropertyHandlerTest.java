/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.persistence.DataAccessService;

/**
 * @author Dirk Bernsau
 * 
 */
public class VMwarePropertyHandlerTest {

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private VMPropertyHandler propertyHandler = new VMPropertyHandler(settings);
    private DataAccessService das;

    @Before
    public void before() {
        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings,
                Messages.DEFAULT_LOCALE);
        propertyHandler = spy(new VMPropertyHandler(settings));

        das = mock(DataAccessService.class);
        doReturn(das).when(propertyHandler).getDataAccessService();
    }

    @Test
    public void testNetworkParameter() {
        settings.getParameters()
                .put(VMPropertyHandler.TS_NIC1_NETWORK_SETTINGS,
                        new Setting(VMPropertyHandler.TS_NIC1_NETWORK_SETTINGS,
                                "DHCP"));
        settings.getParameters()
                .put(VMPropertyHandler.TS_NIC2_NETWORK_SETTINGS,
                        new Setting(VMPropertyHandler.TS_NIC2_NETWORK_SETTINGS,
                                "DHCP"));
        settings.getParameters()
                .put(VMPropertyHandler.TS_NIC3_NETWORK_SETTINGS,
                        new Setting(VMPropertyHandler.TS_NIC3_NETWORK_SETTINGS,
                                "DHCP"));
        settings.getParameters()
                .put(VMPropertyHandler.TS_NIC4_NETWORK_SETTINGS,
                        new Setting(VMPropertyHandler.TS_NIC4_NETWORK_SETTINGS,
                                "DHCP"));

        settings.getParameters().put(VMPropertyHandler.TS_NIC1_GATEWAY,
                new Setting(VMPropertyHandler.TS_NIC1_GATEWAY, "127.0.0.1"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC2_GATEWAY,
                new Setting(VMPropertyHandler.TS_NIC2_GATEWAY, "127.0.0.2"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC3_GATEWAY,
                new Setting(VMPropertyHandler.TS_NIC3_GATEWAY, "127.0.0.3"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC4_GATEWAY,
                new Setting(VMPropertyHandler.TS_NIC4_GATEWAY, "127.0.0.4"));

        settings.getParameters().put(VMPropertyHandler.TS_NIC1_IP_ADDRESS,
                new Setting(VMPropertyHandler.TS_NIC1_IP_ADDRESS, "127.1.0.1"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC2_IP_ADDRESS,
                new Setting(VMPropertyHandler.TS_NIC2_IP_ADDRESS, "127.1.0.2"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC3_IP_ADDRESS,
                new Setting(VMPropertyHandler.TS_NIC3_IP_ADDRESS, "127.1.0.3"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC4_IP_ADDRESS,
                new Setting(VMPropertyHandler.TS_NIC4_IP_ADDRESS, "127.1.0.4"));

        settings.getParameters().put(VMPropertyHandler.TS_NIC1_DNS_SERVER,
                new Setting(VMPropertyHandler.TS_NIC1_DNS_SERVER, "127.2.0.1"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC2_DNS_SERVER,
                new Setting(VMPropertyHandler.TS_NIC2_DNS_SERVER, "127.2.0.2"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC3_DNS_SERVER,
                new Setting(VMPropertyHandler.TS_NIC3_DNS_SERVER, "127.2.0.3"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC4_DNS_SERVER,
                new Setting(VMPropertyHandler.TS_NIC4_DNS_SERVER, "127.2.0.4"));

        settings.getParameters().put(VMPropertyHandler.TS_NIC1_DNS_SUFFIX,
                new Setting(VMPropertyHandler.TS_NIC1_DNS_SUFFIX, "suffix1"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC2_DNS_SUFFIX,
                new Setting(VMPropertyHandler.TS_NIC2_DNS_SUFFIX, "suffix2"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC3_DNS_SUFFIX,
                new Setting(VMPropertyHandler.TS_NIC3_DNS_SUFFIX, "suffix3"));
        settings.getParameters().put(VMPropertyHandler.TS_NIC4_DNS_SUFFIX,
                new Setting(VMPropertyHandler.TS_NIC4_DNS_SUFFIX, "suffix4"));

        settings.getParameters().put(
                VMPropertyHandler.TS_NIC1_SUBNET_MASK,
                new Setting(VMPropertyHandler.TS_NIC1_SUBNET_MASK,
                        "255.255.1.0"));
        settings.getParameters().put(
                VMPropertyHandler.TS_NIC2_SUBNET_MASK,
                new Setting(VMPropertyHandler.TS_NIC2_SUBNET_MASK,
                        "255.255.2.0"));
        settings.getParameters().put(
                VMPropertyHandler.TS_NIC3_SUBNET_MASK,
                new Setting(VMPropertyHandler.TS_NIC3_SUBNET_MASK,
                        "255.255.3.0"));
        settings.getParameters().put(
                VMPropertyHandler.TS_NIC4_SUBNET_MASK,
                new Setting(VMPropertyHandler.TS_NIC4_SUBNET_MASK,
                        "255.255.4.0"));

        for (int i = 1; i < 5; i++) {
            propertyHandler.isAdapterConfiguredByDhcp(i);
            propertyHandler.getGateway(i);
            propertyHandler.getIpAddress(i);
            propertyHandler.getDNSServer(i);
            propertyHandler.getDNSSuffix(i);
            propertyHandler.getSubnetMask(i);
        }

        try {
            propertyHandler.isAdapterConfiguredByDhcp(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }

        try {
            propertyHandler.getGateway(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }

        try {
            propertyHandler.getIpAddress(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }

        try {
            propertyHandler.getDNSServer(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }

        try {
            propertyHandler.getDNSSuffix(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }

        try {
            propertyHandler.getSubnetMask(5);
        } catch (IllegalArgumentException e) {
            assertTrue("NIC identifier 5 is out of range. Valid range is [1-4]."
                    .equals(e.getMessage()));
        }
    }

    @Test
    public void getConfigDiskSpaceMB_sizeParameter() throws Exception {
        // given
        settings.getParameters().put(VMPropertyHandler.TS_DISK_SIZE,
                new Setting(VMPropertyHandler.TS_DISK_SIZE, "17"));

        // when
        double diskSize = propertyHandler.getConfigDiskSpaceMB();

        // then
        assertTrue(diskSize == (17.0 * 1024));
    }

    @Test
    public void getConfigDiskSpaceMB_parameterMissing() throws Exception {
        // given

        // when
        double diskSize = propertyHandler.getConfigDiskSpaceMB();

        // then
        assertTrue(diskSize == .0);
    }

    @Test(expected = APPlatformException.class)
    public void getConfigDiskSpaceMB_parameterInvalid() throws Exception {
        // given
        settings.getParameters().put(VMPropertyHandler.TS_DISK_SIZE,
                new Setting(VMPropertyHandler.TS_DISK_SIZE, "12abc"));

        // when
        propertyHandler.getConfigDiskSpaceMB();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNetworkAdapter_0() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        propertyHandler.getNetworkAdapter(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNetworkAdapter_greater_4() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        propertyHandler.getNetworkAdapter(5);
    }

    @Test
    public void getNetworkAdapter_1() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        String adapter = propertyHandler.getNetworkAdapter(1);

        // then
        assertEquals("adapter 1", adapter);
    }

    @Test
    public void getNetworkAdapter_2() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        String adapter = propertyHandler.getNetworkAdapter(2);

        // then
        assertEquals("adapter 2", adapter);
    }

    @Test
    public void getNetworkAdapter_3() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        String adapter = propertyHandler.getNetworkAdapter(3);

        // then
        assertEquals("adapter 3", adapter);
    }

    @Test
    public void getNetworkAdapter_4() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC1_NETWORK_ADAPTER, "adapter 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC2_NETWORK_ADAPTER, "adapter 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC3_NETWORK_ADAPTER, "adapter 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, new Setting(
                VMPropertyHandler.TS_NIC4_NETWORK_ADAPTER, "adapter 4"));

        // when
        String adapter = propertyHandler.getNetworkAdapter(4);

        // then
        assertEquals("adapter 4", adapter);
    }

    @Test
    public void releaseManuallyDefinedIPAddresses() throws Exception {
        // given
        parameters.put(VMPropertyHandler.TS_NUMBER_OF_NICS, new Setting(
                VMPropertyHandler.TS_NUMBER_OF_NICS, "1"));
        doReturn(Boolean.TRUE).when(propertyHandler)
                .isAdapterConfiguredByDatabase(1);
        doReturn("ipaddress").when(propertyHandler).getIpAddress(anyInt());
        doReturn("site").when(propertyHandler).getTargetVCenterServer();
        doReturn("datacenter").when(propertyHandler).getTargetDatacenter();
        doReturn("cluster").when(propertyHandler).getTargetCluster();
        doReturn("vlan").when(propertyHandler).getVLAN(anyInt());

        // when
        propertyHandler.releaseManuallyDefinedIPAddresses();

        // then
        verify(das, times(1)).releaseIPAddress(eq("site"), eq("datacenter"),
                eq("cluster"), eq("vlan"), eq("ipaddress"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIpAddress_NIC0() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        propertyHandler.getIpAddress(0);
    }

    @Test
    public void getIpAddress_NIC1() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        String ipAddress = propertyHandler.getIpAddress(1);

        // then
        assertEquals("ip address 1", ipAddress);
    }

    @Test
    public void getIpAddress_NIC1_undefined() {
        // given

        // when
        String ipAddress = propertyHandler.getIpAddress(1);

        // then
        assertNull(ipAddress);
    }

    @Test
    public void getIpAddress_NIC2() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        String ipAddress = propertyHandler.getIpAddress(2);

        // then
        assertEquals("ip address 2", ipAddress);
    }

    @Test
    public void getIpAddress_NIC3() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        String ipAddress = propertyHandler.getIpAddress(3);

        // then
        assertEquals("ip address 3", ipAddress);
    }

    @Test
    public void getIpAddress_NIC4() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        String ipAddress = propertyHandler.getIpAddress(4);

        // then
        assertEquals("ip address 4", ipAddress);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIpAddress_greater_4() {
        // given
        parameters.put(VMPropertyHandler.TS_NIC1_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC1_IP_ADDRESS, "ip address 1"));
        parameters.put(VMPropertyHandler.TS_NIC2_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC2_IP_ADDRESS, "ip address 2"));
        parameters.put(VMPropertyHandler.TS_NIC3_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC3_IP_ADDRESS, "ip address 3"));
        parameters.put(VMPropertyHandler.TS_NIC4_IP_ADDRESS, new Setting(
                VMPropertyHandler.TS_NIC4_IP_ADDRESS, "ip address 4"));

        // when
        propertyHandler.getIpAddress(5);
    }
}
