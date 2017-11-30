/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-5-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.authorization;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.model.User;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Unit test for PageAuthorizationBuilder
 * 
 * @author gaowenxin
 * 
 */
public class PageAuthorizationBuilderTest {

    private PageAuthorizationBuilder builder;
    private ServiceAccess serviceAccess;
    private ConfigurationService configurationService;
    private TriggerService triggerService;
    private VOConfigurationSetting voConfigurationSetting;
    private Set<UserRoleType> userRoles;
    private User user;

    @Before
    public void setup() {
        configurationService = mock(ConfigurationService.class);
        triggerService = mock(TriggerService.class);
        serviceAccess = mock(ServiceAccess.class);
        doReturn(configurationService).when(serviceAccess).getService(
                eq(ConfigurationService.class));
        doReturn(triggerService).when(serviceAccess).getService(
                eq(TriggerService.class));
        voConfigurationSetting = new VOConfigurationSetting();
        doReturn(voConfigurationSetting).when(configurationService)
                .getVOConfigurationSetting(
                        eq(ConfigurationKey.HIDDEN_UI_ELEMENTS),
                        eq(Configuration.GLOBAL_CONTEXT));
        VOConfigurationSetting reportEngineUrl = new VOConfigurationSetting();
        reportEngineUrl.setValue("value");
        doReturn(reportEngineUrl).when(configurationService)
                .getVOConfigurationSetting(
                        eq(ConfigurationKey.REPORT_ENGINEURL),
                        eq(Configuration.GLOBAL_CONTEXT));
        VOConfigurationSetting authMode = new VOConfigurationSetting();
        authMode.setValue("INTERNAL");
        doReturn(reportEngineUrl).when(configurationService)
                .getVOConfigurationSetting(eq(ConfigurationKey.AUTH_MODE),
                        eq(Configuration.GLOBAL_CONTEXT));
        VOUserDetails voUserDetails = new VOUserDetails();
        userRoles = new HashSet<UserRoleType>();
        voUserDetails.setUserRoles(userRoles);
        user = new User(voUserDetails);

        builder = new PageAuthorizationBuilder(serviceAccess);
    }

    @Test
    public void getHiddenUIElements() {
        // given
        voConfigurationSetting.setValue("ID1,ID2");
        // when
        Map<String, Boolean> result = builder.getHiddenUIElements();
        // then
        assertEquals(2, result.size());
        assertEquals(Boolean.FALSE, result.get("ID1"));
        assertEquals(Boolean.FALSE, result.get("ID2"));
    }

    @Test
    public void getHiddenUIElements_null() {
        // given
        voConfigurationSetting = null;
        // when
        Map<String, Boolean> result = builder.getHiddenUIElements();
        // then
        assertEquals(0, result.size());
    }

    @Test
    public void buildPageAuthorizationList_BROKER() {
        // given
        userRoles.add(UserRoleType.BROKER_MANAGER);
        // when
        List<PageAuthorization> result = builder
                .buildPageAuthorizationList(user);
        // then
        assertEquals(68, result.size());
        assertEquals(MenuBean.LINK_GOTO_MARKETPLACE, result.get(0)
                .getCurrentPageLink());
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.get(0).isAuthorized()));
        assertEquals(MenuBean.LINK_ORGANIZATION_MANAGE_SUPPLIERS, result
                .get(10).getCurrentPageLink());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.get(10).isAuthorized()));
    }

    @Test
    public void buildPageAuthorizationList_10991() {
        // given
        userRoles.add(UserRoleType.SERVICE_MANAGER);
        // when
        List<PageAuthorization> result = builder
                .buildPageAuthorizationList(user);
        // then
        assertEquals(68, result.size());
        assertEquals(MenuBean.LINK_GOTO_MARKETPLACE, result.get(0)
                .getCurrentPageLink());
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(result.get(0).isAuthorized()));
        assertEquals(MenuBean.LINK_OPERATOR_MANAGE_LANGUAGES, result.get(27)
                .getCurrentPageLink());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(result.get(27).isAuthorized()));
    }
}
