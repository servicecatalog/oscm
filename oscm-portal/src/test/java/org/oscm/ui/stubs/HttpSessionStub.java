/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.internal.vo.VOUserDetails;

@SuppressWarnings({ "deprecation" })
public class HttpSessionStub implements HttpSession {

    Map<String, Object> attributes = new HashMap<String, Object>();

    private Locale userLocale;

    private String id = ADMStringUtils.getRandomString(32);

    public HttpSessionStub(Locale locale) {
        userLocale = locale;
    }

    public Object getAttribute(String arg0) {
        Object obj = attributes.get(arg0);
        if (obj != null) {
            return obj;
        }
        if (Constants.SESS_ATTR_USER.equals(arg0)) {
            VOUserDetails voUser = new VOUserDetails();
            voUser.setLocale(userLocale.getLanguage());
            return voUser;
        }
        return null;
    }

    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }

    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }

    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    public Object getValue(String arg0) {
        throw new UnsupportedOperationException();
    }

    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }

    public void invalidate() {
    }

    public boolean isNew() {
        throw new UnsupportedOperationException();
    }

    public void putValue(String arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String arg0) {
        throw new UnsupportedOperationException();
    }

    public void removeValue(String arg0) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String arg0, Object arg1) {
        attributes.put(arg0, arg1);
    }

    public void setMaxInactiveInterval(int arg0) {
        throw new UnsupportedOperationException();
    }
}
