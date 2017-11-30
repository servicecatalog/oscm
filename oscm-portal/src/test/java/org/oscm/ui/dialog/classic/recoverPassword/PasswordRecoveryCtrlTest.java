/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2013-2-5
 *
 *******************************************************************************/
package org.oscm.ui.dialog.classic.recoverPassword;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.passwordrecovery.PasswordRecoveryService;

/**
 * @author Mao
 *
 */
public class PasswordRecoveryCtrlTest {

    private final static String MARKETPLACE_ID = "MPL_ID";
    private final static String USER_ID = "USER_ID";
    private Map<String, Object> sessionMock;
    private PasswordRecoveryCtrl ctrl;
    private PasswordRecoveryModel model;
    private PasswordRecoveryService passwordRecoveryServiceMock;
    private List<String> messages = new ArrayList<String>();
    boolean isMp = false;

    @Before
    public void setup() throws Exception {
        passwordRecoveryServiceMock = mock(PasswordRecoveryService.class);
        sessionMock = new HashMap<String, Object>();
        model = new PasswordRecoveryModel();
        model.setUserId(USER_ID);
        messages.clear();
        ctrl = new PasswordRecoveryCtrl() {
            // override some methods of BaseBean
            @Override
            protected Object getSessionAttribute(String key) {
                return sessionMock.get(key);
            }

            @Override
            protected void setSessionAttribute(String key, Object value) {
                sessionMock.put(key, value);
            }

            @Override
            protected String getMarketplaceId() {
                if (isMp) {
                    return MARKETPLACE_ID;
                } else {
                    return null;
                }
            }

            @Override
            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key,
                    final Object[] params) {
                messages.add(key);
            }

            @Override
            protected boolean isMarketplaceSet(HttpServletRequest httpRequest) {
                return isMp;
            }

            @Override
            protected HttpServletRequest getRequest() {
                return null;
            }

            @Override
            protected PasswordRecoveryService getPasswordRecoveryService() {
                return passwordRecoveryServiceMock;
            }
        };
        ctrl.setModel(model);
    }

    @Test
    public void getInitializeForMarketplace() {
        // when
        isMp = true;
        ctrl.initialize();
        // then
        assertEquals(USER_ID, model.getUserId());
        assertEquals(MARKETPLACE_ID, model.getMarketpalceId());
    }

    @Test
    public void getInitializeForClassic() {
        // when
        isMp = false;
        ctrl.initialize();
        // then
        assertEquals(USER_ID, model.getUserId());
        assertEquals(null, model.getMarketpalceId());
    }

    @Test
    public void getInitializeWithNullModel() {
        // given
        ctrl.setModel(null);
        // when
        ctrl.initialize();
        // then
        assertNotNull(ctrl.getModel());
    }

    @Test
    public void startPasswordRecovery_Classic() {
        // given
        isMp = false;
        ctrl.initialize();
        // when
        String result = ctrl.startPasswordRecovery();
        // then
        verify(passwordRecoveryServiceMock, times(1)).startPasswordRecovery(
                model.getUserId(), null);
        assertEquals(1, messages.size());
        assertEquals(BaseBean.INFO_RECOVERPASSWORD_START, messages.get(0));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void startPasswordRecovery_Marketplace() {
        // given
        isMp = true;
        ctrl.initialize();
        // when
        String result = ctrl.startPasswordRecovery();
        // then
        verify(passwordRecoveryServiceMock, times(1)).startPasswordRecovery(
                model.getUserId(), MARKETPLACE_ID);
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_CONFIRMSTARTPWDRECOVERY,
                result);
    }
}
