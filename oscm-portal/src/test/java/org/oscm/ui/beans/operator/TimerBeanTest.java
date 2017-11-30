/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 08.02.2011                                                      
 *                                                                              
 *  Completion Time: 08.02.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;

import org.mockito.stubbing.Answer;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.types.enumtypes.TimerType;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOTimerInfo;

/**
 * @author weiser
 * 
 */
public class TimerBeanTest {

    private TimerBean bean;
    private TimerBean timerBean;
    private OperatorService operatorService;

    protected String messageKey;
    protected boolean serviceCalled;
    protected List<VOTimerInfo> expirationInfo;

    private final List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    @Before
    public void setup() {
        timerBean = spy(new TimerBean());
        operatorService = mock(OperatorService.class);
        doReturn(operatorService).when(timerBean).getOperatorService();

        new FacesContextStub(Locale.ENGLISH) {
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };

        final OperatorService stub = mock(OperatorService.class);
        Answer<List<VOTimerInfo>> answer = invocationOnMock -> {
            serviceCalled = true;
            return expirationInfo;
        };
        try {
            doAnswer(answer).when(stub).getTimerExpirationInformation();
            doAnswer(answer).when(stub).reInitTimers();
        } catch (OrganizationAuthoritiesException | ValidationException e) {
            e.printStackTrace();
        }
        bean = new TimerBean() {

            private static final long serialVersionUID = 5713210290878291988L;

            protected OperatorService getOperatorService() {
                return stub;
            }

            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
                messageKey = key;
            }

        };
        expirationInfo = new ArrayList<VOTimerInfo>();
        VOTimerInfo o = new VOTimerInfo();
        o.setExpirationDate(new Date());
        o.setTimerType(TimerType.BILLING_INVOCATION.name());
        expirationInfo.add(o);
    }

    @Test
    public void testGetExpirationInfo() throws Exception {
        List<VOTimerInfo> list = bean.getExpirationInfo();
        assertEquals(expirationInfo, list);
        assertTrue(serviceCalled);
        // the second call must do no service call because the data is cached
        // but return the same data
        serviceCalled = false;
        list = bean.getExpirationInfo();
        assertEquals(expirationInfo, list);
        assertFalse(serviceCalled);
    }

    @Test
    public void testReInitTimers() throws Exception {
        String result = bean.reInitTimers();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(BaseOperatorBean.INFO_TASK_SUCCESSFUL, messageKey);
        assertTrue(serviceCalled);
        // reinit timers also returns the timer info - so the field must be not
        // null and thus no service must be called to get the timer information
        serviceCalled = false;
        List<VOTimerInfo> list = bean.getExpirationInfo();
        assertEquals(expirationInfo, list);
        assertFalse(serviceCalled);
    }

    @Test
    public void testReInitTimers_timerIntervalInvalid() throws Exception {
        // given
        doThrow(new ValidationException()).when(operatorService).reInitTimers();

        // when
        String result = timerBean.reInitTimers();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
    }
}
