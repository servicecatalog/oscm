/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 20.08.2012                                                      
 *                                                                              
 *  Completion Time:                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.util.Arrays;
import java.util.Locale;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author cheld
 */
public class UiDelegateStub extends UiDelegate {

    private boolean errors = false;
    private String messageKey;
    private Object messageParams[];

    // inject stubs or mocks if needed:
    private Locale viewLocale;
    private HttpSession session;
    private HttpServletRequest request;
    private ExternalContext externalContext;
    private boolean resetDirtyCalled;
    private String marketplaceId;

    @Override
    public <T> T findBean(String beanName) {
        return null;
    }

    @Override
    public void handle(Response response, String successMessageKey,
            Object... params) {
        ReturnCode returnCode = response.getMostSevereReturnCode();
        if (returnCode != null) {
            messageKey = returnCode.getMessageKey();
            messageParams = returnCode.getMessageParam();
        } else {
            messageKey = successMessageKey;
            if (params != null && params.length > 0 && params[0] == null) {
                messageParams = null;
            } else {
                messageParams = params;
            }
        }
        errors = false;
    }

    @Override
    public void handle(String successMessageKey, Object... params) {
        messageKey = successMessageKey;
        if (params != null && params.length > 0 && params[0] == null) {
            messageParams = null;
        } else {
            messageParams = params;
        }
        errors = false;
    }

    boolean successMessageEqualsTo(String successMessageKey, Object... params) {
        return (messageKey.equals(successMessageKey) && Arrays.equals(
                messageParams, params));
    }

    public static Matcher<Object> hasSuccessMessage(
            final String successMessageKey, final Object... params) {
        return new BaseMatcher<Object>() {
            public boolean matches(Object instance) {
                return ((UiDelegateStub) instance).successMessageEqualsTo(
                        successMessageKey, params);
            }

            public void describeTo(Description description) {
                description.appendText("messageKey: " + successMessageKey
                        + ", params: " + params);
            }
        };
    }

    @Override
    public void handleException(SaaSApplicationException ex) {
        errors = true;
    }

    @Override
    public void handleException(SaaSApplicationException ex, boolean clean) {
        errors = true;
    }

    @Override
    public boolean hasErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ContainerDelegateStub [errors=" + errors + ", messageKey="
                + messageKey + ", messageParams="
                + Arrays.toString(messageParams) + "]";
    }

    @Override
    public String getText(String key, Object... params) {
        StringBuffer text = new StringBuffer().append(key);
        if (params != null && params.length > 0) {
            text.append(Arrays.toString(params));
        }
        return text.toString();
    }

    @Override
    public Locale getViewLocale() {
        return viewLocale;
    }

    public void setViewLocale(Locale locale) {
        this.viewLocale = locale;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ExternalContext getExternalContext() {
        return externalContext;
    }

    public void setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
    }

    @Override
    public void resetDirty() {
        setResetDirtyCalled(true);
    }

    public boolean isResetDirtyCalled() {
        return resetDirtyCalled;
    }

    public void setResetDirtyCalled(boolean resetDirtyCalled) {
        this.resetDirtyCalled = resetDirtyCalled;
    }

    @Override
    public String getMarketplaceId() {
        return marketplaceId;
    }

    @Override
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

}
