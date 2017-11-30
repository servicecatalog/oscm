/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.oscm.ui.beans.SelectOrganizationIncludeBean;
import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.vo.VOOperatorOrganization;

/**
 * @author ZhouMin
 * 
 */
public class OperatorSelectOrgBeanTest {
    private VOOperatorOrganization org;
    private OperatorSelectOrgBean bean;
    private final ApplicationBean appBean = mock(ApplicationBean.class);
    private final OperatorService operatorService = mock(OperatorService.class);

    @Before
    public void setup() throws Exception {
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

}
