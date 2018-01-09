/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/11/16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common.instanceDetail;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.InstanceAccess;
import org.oscm.app.v2_0.intf.ServerInformation;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * @author tateiwamext
 * 
 */
public class ExtensionInterfaceBeanTest extends EJBTestBase {

    private InstanceAccess instanceAccess;
    private List<? extends ServerInformation> serverInfo;
    private String instanceID = "inst1";
    private String subscriptionID = "subscription id";
    private String organizationID = "12345f";

    private ExtensionInterfaceBean getTestBean(String instanceId,
            String subscriptionId, String organizationId) throws Exception {
        instanceAccess = Mockito.mock(InstanceAccess.class);

        serverInfo = getServerInfoMock(3);

        String encodedInstId = Base64.encodeBase64URLSafeString(
                instanceId.getBytes(StandardCharsets.UTF_8));
        String encodedSubId = Base64.encodeBase64URLSafeString(
                subscriptionId.getBytes(StandardCharsets.UTF_8));
        String encodedOrgId = Base64.encodeBase64URLSafeString(
                organizationId.getBytes(StandardCharsets.UTF_8));

        Mockito.when(instanceAccess.getAccessInfo(instanceID, subscriptionID,
                organizationID)).thenReturn("Access info from IaaS");
        Mockito.<List<? extends ServerInformation>> when(instanceAccess
                .getServerDetails(instanceID, subscriptionID, organizationID))
                .thenReturn(serverInfo);

        ExtensionInterfaceBean bean = new ExtensionInterfaceBean();
        bean.setInstanceId(encodedInstId);
        bean.setSubscriptionId(encodedSubId);
        bean.setOrganizationId(encodedOrgId);
        bean.setInstanceAccess(instanceAccess);

        return bean;
    }

    /**
     * @return
     */
    private List<ServerInformation> getServerInfoMock(int numberOfServer) {
        List<ServerInformation> result = new ArrayList<>();
        List<String> status = Arrays.asList("ACTIVE", "STOPPED", "ERROR");
        List<String> type = Arrays.asList("S-1", "P-1", "S-2");
        for (int i = 0; i < numberOfServer; i++) {
            ServerInformation server = Mockito.mock(ServerInformation.class);
            Mockito.when(server.getId()).thenReturn(String.valueOf(i + 1));
            Mockito.when(server.getName())
                    .thenReturn("instance" + String.valueOf(i + 1));
            Mockito.when(server.getPrivateIP())
                    .thenReturn(Arrays.asList("192.168.0." + i,
                            "192.168.0." + String.valueOf(i + 1),
                            "192.168.0." + String.valueOf(i + 2)));
            Mockito.when(server.getPublicIP()).thenReturn(Arrays
                    .asList("10.1.0." + i, "10.1.0." + String.valueOf(i + 1)));
            Mockito.when(server.getStatus())
                    .thenReturn(status.get(3 % (i + 1)));
            Mockito.when(server.getType()).thenReturn(type.get(3 % (i + 1)));
            Mockito.when(server.getPrivateIPasString())
                    .thenReturn("192.168.0." + i + "\n192.168.0."
                            + String.valueOf(i + 1) + "\n192.168.0."
                            + String.valueOf(i + 2));
            Mockito.when(server.getPublicIPasString()).thenReturn(
                    "10.1.0." + i + "\n10.1.0." + String.valueOf(i + 1));
            result.add(server);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.oscm.test.EJBTestBase#setup(org.oscm.test.ejb.TestContainer)
     */
    @Override
    protected void setup(TestContainer container) throws Exception {
        // TODO Auto-generated method stub

    }

    @Test
    public void getInstanceDetails() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                organizationID);
        // when
        List<? extends ServerInformation> sv = bean.getInstanceDetails();
        // then
        assertEquals(3, sv.size());
        assertEquals("1", sv.get(0).getId());
        assertEquals("instance1", sv.get(0).getName());
        assertEquals("192.168.0.0\n192.168.0.1\n192.168.0.2",
                sv.get(0).getPrivateIPasString());
        assertEquals("10.1.0.0\n10.1.0.1", sv.get(0).getPublicIPasString());
        assertEquals("ACTIVE", sv.get(0).getStatus());
        assertEquals("S-1", sv.get(0).getType());

        // call second time
        List<? extends ServerInformation> sv2 = bean.getInstanceDetails();
        // then
        Mockito.verify(instanceAccess, Mockito.times(2))
                .getServerDetails(instanceID, subscriptionID, organizationID);
        assertEquals(3, sv2.size());
        assertEquals("1", sv2.get(0).getId());
        assertEquals("instance1", sv2.get(0).getName());
        assertEquals("192.168.0.0\n192.168.0.1\n192.168.0.2",
                sv.get(0).getPrivateIPasString());
        assertEquals("10.1.0.0\n10.1.0.1", sv2.get(0).getPublicIPasString());
        assertEquals("ACTIVE", sv2.get(0).getStatus());
        assertEquals("S-1", sv2.get(0).getType());

    }

    @Test
    public void getInstanceDetails_withException() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                organizationID);
        Mockito.<List<? extends ServerInformation>> when(instanceAccess
                .getServerDetails(instanceID, subscriptionID, organizationID))
                .thenThrow(new APPlatformException("Error!!"));
        // when
        List<? extends ServerInformation> sv = bean.getInstanceDetails();
        // then
        assertEquals(0, sv.size());
    }

    @Test
    public void getInstanceDetails_withNoInstanceID() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean("", subscriptionID,
                organizationID);
        Mockito.<List<? extends ServerInformation>> when(instanceAccess
                .getServerDetails(null, subscriptionID, organizationID))
                .thenReturn(getServerInfoMock(0));
        // when
        List<? extends ServerInformation> sv = bean.getInstanceDetails();
        // then
        assertEquals(0, sv.size());
    }

    @Test
    public void getInstanceDetails_withNoSubscriptionID() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, "",
                organizationID);
        Mockito.<List<? extends ServerInformation>> when(instanceAccess
                .getServerDetails(instanceID, null, organizationID))
                .thenReturn(getServerInfoMock(0));
        // when
        List<? extends ServerInformation> sv = bean.getInstanceDetails();
        // then
        assertEquals(0, sv.size());
    }

    @Test
    public void getInstanceDetails_withNoOrganizationID() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                "");
        Mockito.<List<? extends ServerInformation>> when(instanceAccess
                .getServerDetails(instanceID, subscriptionID, null))
                .thenReturn(getServerInfoMock(0));
        // when
        List<? extends ServerInformation> sv = bean.getInstanceDetails();
        // then
        assertEquals(0, sv.size());
    }

    @Test
    public void getSubscriptionName() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                organizationID);

        // when
        String result = bean.getSubscriptionId();

        // then
        assertEquals("subscription id", result);

    }

    @Test
    public void getSubscriptionName_nothing() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, "",
                organizationID);

        // when
        String result = bean.getSubscriptionId();

        // then
        assertEquals("", result);

    }

    @Test
    public void getSubscriptionName_withJapanese() throws Exception {
        // given
        String japaneseSubId = "abcd あいう 123";
        ExtensionInterfaceBean bean = getTestBean(instanceID, japaneseSubId,
                organizationID);

        // when
        String result = bean.getSubscriptionId();

        // then
        assertEquals(japaneseSubId, result);

    }

    @Test
    public void getAccessInfo() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                organizationID);

        // when
        String result = bean.getAccessInfo();

        // then
        assertEquals("Access info from IaaS", result);

        // call second time
        String result2 = bean.getAccessInfo();

        // then
        Mockito.verify(instanceAccess, Mockito.times(2))
                .getAccessInfo(instanceID, subscriptionID, organizationID);
        assertEquals("Access info from IaaS", result2);
    }

    @Test
    public void getAccessInfo_withError() throws Exception {
        // given
        ExtensionInterfaceBean bean = getTestBean(instanceID, subscriptionID,
                organizationID);
        Mockito.when(instanceAccess.getAccessInfo(instanceID, subscriptionID,
                organizationID)).thenThrow(new APPlatformException("Error!!!"));

        // when
        String result = bean.getAccessInfo();

        // then
        assertEquals("", result);

    }
}
