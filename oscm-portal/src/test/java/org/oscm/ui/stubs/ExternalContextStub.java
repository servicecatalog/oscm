/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;

public class ExternalContextStub extends ExternalContext {

    private HttpServletRequestStub httpServletRequestStub;
    private HttpServletResponseStub httpServletResponseStub;

    public ExternalContextStub(Locale usedLocale) {
        httpServletRequestStub = new HttpServletRequestStub(usedLocale);
        httpServletResponseStub = new HttpServletResponseStub();
    }

    @Override
    public Object getRequest() {
        return httpServletRequestStub;
    }

    @Override
    public Object getResponse() {
        return httpServletResponseStub;
    }

    @Override
    public void dispatch(String arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeActionURL(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeNamespace(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeResourceURL(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getApplicationMap() {
        return null;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getInitParameter(String arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getInitParameterMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestContextPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getRequestCookieMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getRequestHeaderValuesMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getRequestLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Locale> getRequestLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getRequestMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getRequestParameterMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getRequestParameterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String[]> getRequestParameterValuesMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestServletPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getResource(String arg0) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getResourcePaths(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSession(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getSessionMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String arg0, Throwable arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void redirect(String arg0) throws IOException {
        throw new UnsupportedOperationException();
    }
}
