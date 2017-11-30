/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: June 8, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;
import org.oscm.ui.stubs.UIViewRootStub;

public class UiDelegateTest {

    UiDelegate uiDelegate;
    VOUserDetails voUserInSession;
    ExternalContextStub externalContextStub;

    @Before
    public void setup() {
        uiDelegate = spy(new UiDelegate());
        voUserInSession = prepareSessionVOUserDetail();
    }

    @Test
    public void isNameSequenceReversed_True() {
        // given
        doReturn("ja").when(uiDelegate).getUserLanguage();
        // when
        boolean result = uiDelegate.isNameSequenceReversed();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isNameSequenceReversed_False() {
        // given
        doReturn("en").when(uiDelegate).getUserLanguage();
        // when
        boolean result = uiDelegate.isNameSequenceReversed();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void getUserLanguage_FacesContextNull() {
        // when
        when(uiDelegate.getFacesContext()).thenReturn(null);
        String result = uiDelegate.getUserLanguage();
        // then
        assertEquals(result, "en");
    }

    @Test
    public void getUserLanguage_UserNotNull() {
        // given
        prepareFacesContextforNotNullUser();
        // when
        String result = uiDelegate.getUserLanguage();
        // then
        assertEquals(result, "ja");
    }

    @Test
    public void getUserLanguage_UserNull() {
        // given
        prepareFacesContextforNullUser();
        // when
        String result = uiDelegate.getUserLanguage();
        // then
        assertEquals(result, "en");
    }

    private void prepareFacesContextforNullUser() {
        final HttpSession session = new HttpSessionStub(Locale.ENGLISH) {
            @Override
            public Object getAttribute(String arg0) {
                return null;
            }
        };
        new HttpServletRequestStub(Locale.ENGLISH) {
            @Override
            public HttpSession getSession() {
                return session;
            }
        };

        FacesContext facesContext = new FacesContextStub(Locale.ENGLISH);
        UIViewRootStub vrStub = new UIViewRootStub() {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            };
        };
        facesContext.setViewRoot(vrStub);
    }

    private void prepareFacesContextforNotNullUser() {
        final HttpSession session = new HttpSessionStub(Locale.JAPANESE);
        session.setAttribute(Constants.SESS_ATTR_USER, voUserInSession);
        new HttpServletRequestStub(Locale.JAPANESE) {
            @Override
            public HttpSession getSession() {
                return session;
            }
        };
        new FacesContextStub(Locale.JAPANESE);
    }

    public VOUserDetails prepareSessionVOUserDetail() {
        VOUserDetails voCurrentUser = new VOUserDetails();
        voCurrentUser.setLocale("ja");
        voCurrentUser.setKey(10000);
        return voCurrentUser;
    }

}
