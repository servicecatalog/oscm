/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.ui.beans.SelectOrganizationIncludeBean;
import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.vo.VOOperatorOrganization;

import javax.faces.model.SelectItem;

/**
 * @author ZhouMin
 * 
 */
public class OperatorSelectOrgBeanTest {
    private VOOperatorOrganization org;
    private OperatorSelectOrgBean bean;
    private VOOrganization voOrg;
    private PlatformUser platformUser;
    private final ApplicationBean appBean = mock(ApplicationBean.class);
    private final OperatorService operatorService = mock(OperatorService.class);

    @Before
    public void setup() throws Exception {
        platformUser = new PlatformUser();
        voOrg = new VOOrganization();
        voOrg.setOrganizationId(
                platformUser.getOrganization().getOrganizationId());
        voOrg.setEmail("admin@organization.com");
        voOrg.setPhone("123456");
        voOrg.setUrl("http://www.example.com");
        voOrg.setName("example");
        voOrg.setAddress("an address");
        voOrg.setLocale(platformUser.getLocale());
        voOrg.setVersion(platformUser.getOrganization().getVersion());
        voOrg.setKey(platformUser.getOrganization().getKey());
        voOrg.setDomicileCountry("DE");
        voOrg.setNameSpace("http://oscm.org/xsd/2.0");

        bean = new OperatorSelectOrgBean() {

            private static final long serialVersionUID = -9126265695343363133L;

            @Override
            protected OperatorService getOperatorService() {
                return operatorService;
            }
        };
        bean = spy(bean);

        org = new VOOperatorOrganization();
        org.setOrganizationId("organizationId");
        when(operatorService.getOrganization(anyString())).thenReturn(org);
        bean.ui = mock(UiDelegate.class);
        when(bean.ui.findBean(eq(OperatorSelectOrgBean.APPLICATION_BEAN)))
                .thenReturn(appBean);
        when(bean.getApplicationBean()).thenReturn(appBean);

        bean.setSelectOrganizationIncludeBean(new SelectOrganizationIncludeBean());
    }

    @Test
    public void getOrganization_invalidLocale() throws Exception {
        // given
        org.setLocale("en");
        List<String> localesStr = new ArrayList<String>();
        localesStr.add("en");
        localesStr.add("de");
        doReturn(localesStr).when(appBean).getActiveLocales();
        bean.setOrganizationId("orgId");

        // when
        bean.getOrganization();

        // then
        verify(appBean, times(1)).checkLocaleValidation("en");
    }

    @Test
    public void getAvailableOrganizations_NotNull() throws OrganizationAuthoritiesException {
        List<SelectItem> list = bean.getAvailableOrganizations();
        assertNotNull(list);
        verify(operatorService, times(1)).getOrganizations("",
                new ArrayList<OrganizationRoleType>());
    }

    @Test
    public void getAvailableOrganizations_Same() throws OrganizationAuthoritiesException {
        List<SelectItem> list1 = bean.getAvailableOrganizations();
        verify(operatorService, times(1)).getOrganizations("",
                new ArrayList<OrganizationRoleType>());
        List<SelectItem> list2 = bean.getAvailableOrganizations();
        verifyNoMoreInteractions(operatorService);
        assertSame(list1, list2);
    }

    @Test
    public void getAvailableOrganizations() throws OrganizationAuthoritiesException {
        List<SelectItem> list = bean.getAvailableOrganizations();
        verify(operatorService, times(1)).getOrganizations("",
                new ArrayList<OrganizationRoleType>());

        assertEquals(2, list.size());
        SelectItem item = list.get(0);
        assertEquals(voOrg.getName() + " (" + voOrg.getOrganizationId() + ")",
                item.getLabel());
        assertEquals(operatorService.getOrganizations("",
                new ArrayList<OrganizationRoleType>()), item.getValue());
    }

}
