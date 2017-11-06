/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-2-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.provisioning.adapter.v1_0;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.oscm.applicationservice.provisioning.adapter.ProvisioningServiceAdapterV1_0;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;

/**
 * @author Yuyin
 * 
 */
public class ProvisioningServiceAdapterV1_0Test {

    private static final String USER1 = "user1";
    private static final String SYNC = "sync";
    private static final String ASYNC = "async";
    private static final String PING = "ping";
    private static final String SUBSCRIPTIONID = "subscriptionId";
    private static final String REFERENCEID = "referenceId";
    private static final String INSTANCEID = "instance";
    private static final String ORGANIZATIONID = "organization";
    private final String resourceUrl = "/wsdl/provisioning/ProvisioningService.wsdl";

    private ProvisioningServiceAdapterV1_0 adapter;
    private ProvisioningService service;
    private InstanceResult instance;
    private BaseResult baseResult;

    @Mock
    List<ServiceParameter> params;

    @Mock
    List<ServiceAttribute> attr;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        instance = new InstanceResult();
        instance.setDesc(SYNC);
        baseResult = new BaseResult();
        baseResult.setDesc(ASYNC);
        MockitoAnnotations.initMocks(this);
        adapter = new ProvisioningServiceAdapterV1_0();
        service = mock(ProvisioningService.class);
        adapter.setProvisioningService(service);
    }

    @Test
    public void getLocalWSDL() {
        // when
        URL result = adapter.getLocalWSDL();
        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.getPath().contains(resourceUrl)));

    }

    @Test
    public void createInstance() {
        // given
        InstanceRequest request = new InstanceRequest();
        when(service.createInstance(any(InstanceRequest.class),
                any(User.class))).thenReturn(instance);
        // when
        InstanceResult result = adapter.createInstance(request, null);
        // then
        assertEquals(SYNC, result.getDesc());
        verify(service).createInstance(same(request), any(User.class));
    }

    @Test
    public void asyncCreateInstance() {
        // given
        InstanceRequest request = new InstanceRequest();
        when(service.asyncCreateInstance(any(InstanceRequest.class),
                any(User.class))).thenReturn(baseResult);
        // when
        BaseResult result = adapter.asyncCreateInstance(request, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).asyncCreateInstance(same(request), any(User.class));
    }

    @Test
    public void createUsers() {
        // given
        List<User> users = new ArrayList<>();
        users.add(new User());
        UserResult userResult = new UserResult();
        userResult.setUsers(users);
        when(service.createUsers(anyString(), anyListOf(User.class),
                any(User.class))).thenReturn(userResult);
        // when
        UserResult result = adapter.createUsers(USER1, users, null);
        // then
        assertEquals(1, result.getUsers().size());
        verify(service).createUsers(same(USER1), same(users), any(User.class));
    }

    @Test
    public void deleteInstance() {
        // given
        when(service.deleteInstance(anyString(), anyString(), anyString(),
                any(User.class))).thenReturn(baseResult);
        // when
        BaseResult result = adapter.deleteInstance(INSTANCEID, ORGANIZATIONID,
                SUBSCRIPTIONID, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).deleteInstance(same(INSTANCEID), same(ORGANIZATIONID),
                same(SUBSCRIPTIONID), any(User.class));
    }

    @Test
    public void deleteUsers() {
        // given
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(service.deleteUsers(anyString(), anyListOf(User.class),
                any(User.class))).thenReturn(baseResult);
        // when
        BaseResult result = adapter.deleteUsers(USER1, users, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).deleteUsers(same(USER1), same(users), any(User.class));
    }

    @Test
    public void sendPing() {
        // given
        when(service.sendPing(anyString())).thenReturn(PING);
        // when
        String result = adapter.sendPing(PING);
        // then
        assertEquals(PING, result);
        verify(service).sendPing(same(PING));
    }

    @Test
    public void modifySubscription() {
        // given
        when(service.modifySubscription(anyString(), anyString(), anyString(),
                anyListOf(ServiceParameter.class),
                anyListOf(ServiceAttribute.class), any(User.class)))
                        .thenReturn(baseResult);
        // when
        BaseResult result = adapter.modifySubscription(ASYNC, SUBSCRIPTIONID,
                REFERENCEID, params, attr, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).modifySubscription(same(ASYNC), same(SUBSCRIPTIONID),
                same(REFERENCEID), same(params), same(attr), any(User.class));
        verifyZeroInteractions(params);
    }

    @Test
    public void updateUsers() {
        // given
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(service.updateUsers(anyString(), anyListOf(User.class),
                any(User.class))).thenReturn(baseResult);
        // when
        BaseResult result = adapter.updateUsers(ASYNC, users, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).updateUsers(same(ASYNC), same(users), any(User.class));
    }

    @Test
    public void activateInstance() {
        // given
        when(service.activateInstance(anyString(), any(User.class)))
                .thenReturn(baseResult);
        // when
        BaseResult result = adapter.activateInstance(INSTANCEID, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).activateInstance(same(INSTANCEID), any(User.class));
    }

    @Test
    public void deactivateInstance() {
        // given
        when(service.deactivateInstance(anyString(), any(User.class)))
                .thenReturn(baseResult);
        // when
        BaseResult result = adapter.deactivateInstance(INSTANCEID, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).deactivateInstance(same(INSTANCEID), any(User.class));
    }

    @Test
    public void asyncModifySubscription() {
        // given
        when(service.asyncModifySubscription(anyString(), anyString(),
                anyString(), anyListOf(ServiceParameter.class),
                anyListOf(ServiceAttribute.class), any(User.class)))
                        .thenReturn(baseResult);
        // when
        BaseResult result = adapter.asyncModifySubscription(ASYNC,
                SUBSCRIPTIONID, REFERENCEID, params, attr, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).asyncModifySubscription(same(ASYNC),
                same(SUBSCRIPTIONID), same(REFERENCEID), same(params),
                same(attr), any(User.class));
        verifyZeroInteractions(params);
    }

    @Test
    public void asyncUpgradeSubscription() {
        // given
        when(service.asyncUpgradeSubscription(anyString(), anyString(),
                anyString(), anyListOf(ServiceParameter.class),
                anyListOf(ServiceAttribute.class), any(User.class)))
                        .thenReturn(baseResult);
        // when
        BaseResult result = adapter.asyncUpgradeSubscription(ASYNC,
                SUBSCRIPTIONID, REFERENCEID, params, attr, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).asyncUpgradeSubscription(same(ASYNC),
                same(SUBSCRIPTIONID), same(REFERENCEID), same(params),
                same(attr), any(User.class));
        verifyZeroInteractions(params);
    }

    @Test
    public void upgradeSubscription() {
        // given
        when(service.upgradeSubscription(anyString(), anyString(), anyString(),
                anyListOf(ServiceParameter.class),
                anyListOf(ServiceAttribute.class), any(User.class)))
                        .thenReturn(baseResult);
        // when
        BaseResult result = adapter.upgradeSubscription(ASYNC, SUBSCRIPTIONID,
                REFERENCEID, params, attr, null);
        // then
        assertEquals(ASYNC, result.getDesc());
        verify(service).upgradeSubscription(same(ASYNC), same(SUBSCRIPTIONID),
                same(REFERENCEID), same(params), same(attr), any(User.class));
        verifyZeroInteractions(params);
    }
}
