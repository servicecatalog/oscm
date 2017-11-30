/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

@SuppressWarnings({ "deprecation" })
public class ApplicationStub extends Application {

    private ResourceBundleStub resorceBundleStub;

    private Collection<Locale> locales;

    private Locale defaultLocale;

    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
        if (resorceBundleStub != null) {
            return resorceBundleStub;
        }
        return new ResourceBundleStub();
    }

    public void setResourceBundleStub(ResourceBundleStub stub) {
        resorceBundleStub = stub;
    }

    @Override
    public void addComponent(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConverter(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConverter(@SuppressWarnings("rawtypes") Class arg0,
            String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addValidator(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent createComponent(String arg0) throws FacesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent createComponent(ValueBinding arg0, FacesContext arg1,
            String arg2) throws FacesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Converter createConverter(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Converter createConverter(@SuppressWarnings("rawtypes") Class arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodBinding createMethodBinding(String arg0,
            @SuppressWarnings("rawtypes") Class[] arg1)
            throws ReferenceSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Validator createValidator(String arg0) throws FacesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueBinding createValueBinding(String arg0)
            throws ReferenceSyntaxException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionListener getActionListener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getComponentTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> getConverterIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Class<?>> getConverterTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getDefaultLocale() {
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return Locale.ENGLISH;
    }

    @Override
    public String getDefaultRenderKitId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessageBundle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigationHandler getNavigationHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StateManager getStateManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Locale> getSupportedLocales() {
        if (null == locales) {
            return new ArrayList<Locale>().iterator();
        }
        return locales.iterator();
    }

    @Override
    public Iterator<String> getValidatorIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VariableResolver getVariableResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ViewHandler getViewHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setActionListener(ActionListener arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultLocale(Locale arg0) {
        defaultLocale = arg0;
    }

    @Override
    public void setDefaultRenderKitId(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMessageBundle(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNavigationHandler(NavigationHandler arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPropertyResolver(PropertyResolver arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStateManager(StateManager arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSupportedLocales(Collection<Locale> locales) {
        this.locales = locales;
    }

    @Override
    public void setVariableResolver(VariableResolver arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setViewHandler(ViewHandler arg0) {
        throw new UnsupportedOperationException();
    }
}
