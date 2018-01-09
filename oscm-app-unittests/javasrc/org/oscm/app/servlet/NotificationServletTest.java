/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015-3-26                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.app.servlet;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;

public class NotificationServletTest {
    private NotificationServlet servlet;
    private APPTimerServiceBean timerService;
    private APPConfigurationServiceBean configService;

    @Before
    public void setup() throws Exception {
        servlet = new NotificationServlet();
        timerService = mock(APPTimerServiceBean.class);
        configService = mock(APPConfigurationServiceBean.class);
        servlet.timerService = timerService;
        servlet.configService = configService;

        doReturn(Boolean.TRUE).when(servlet.timerService).restart(anyBoolean());
        doNothing().when(servlet.timerService).initTimers();
    }

    @Test
    public void init_APPSuspendTrue() throws Throwable {
        // given
        doReturn(Boolean.TRUE).when(servlet.configService).isAPPSuspend();
        // when
        servlet.init();
        // then
        verify(servlet.configService, times(1)).isAPPSuspend();
        verify(servlet.timerService, times(1)).restart(anyBoolean());
        verify(servlet.timerService, times(1)).initTimers();
    }

    @Test
    public void init_APPSuspendFalse() throws Throwable {
        // given
        doReturn(Boolean.FALSE).when(servlet.configService).isAPPSuspend();
        // when
        servlet.init();
        // then
        verify(servlet.configService, times(1)).isAPPSuspend();
        verify(servlet.timerService, times(0)).restart(anyBoolean());
        verify(servlet.timerService, times(1)).initTimers();
    }

}
