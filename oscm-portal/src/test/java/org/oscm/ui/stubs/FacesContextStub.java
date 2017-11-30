/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.util.Iterator;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;

public class FacesContextStub extends FacesContext {

    private Locale usedLocale;

    private UIViewRoot uiViewRoot;

    private Application applicationStub;
    private ExternalContextStub externalContextStub;

    public FacesContextStub(Locale locale) {
        usedLocale = locale;
        FacesContext.setCurrentInstance(this);
    }

    public void setCurrentInstance(FacesContextStub instance) {
        FacesContext.setCurrentInstance(instance);
    }

    public void setLocale(Locale locale) {
        usedLocale = locale;
    }

    private final Iterator<FacesMessage> EMPTY_ITERATOR = new Iterator<FacesMessage>() {

        @Override
        public void remove() {
        }

        @Override
        public FacesMessage next() {
            return null;
        }

        @Override
        public boolean hasNext() {
            return false;
        }
    };

    @Override
    public Application getApplication() {
        if (applicationStub == null) {
            applicationStub = new ApplicationStub();
        }
        return applicationStub;
    }

    @Override
    public ExternalContext getExternalContext() {
        if (externalContextStub == null) {
            externalContextStub = new ExternalContextStub(usedLocale);
        }
        return externalContextStub;
    }

    @Override
    public UIViewRoot getViewRoot() {
        return uiViewRoot;
    }

    @Override
    public void addMessage(String arg0, FacesMessage arg1) {
    }

    @Override
    public Iterator<String> getClientIdsWithMessages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Severity getMaximumSeverity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<FacesMessage> getMessages() {
        return EMPTY_ITERATOR;
    }

    @Override
    public Iterator<FacesMessage> getMessages(String arg0) {
        return EMPTY_ITERATOR;
    }

    @Override
    public RenderKit getRenderKit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getRenderResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getResponseComplete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseStream getResponseStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseWriter getResponseWriter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renderResponse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void responseComplete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResponseStream(ResponseStream arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResponseWriter(ResponseWriter arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setViewRoot(UIViewRoot arg0) {
        uiViewRoot = arg0;
    }

}
