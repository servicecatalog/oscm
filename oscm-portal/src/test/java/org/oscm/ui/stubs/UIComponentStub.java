/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

@SuppressWarnings({ "deprecation" })
public class UIComponentStub extends UIComponent {

    private final Map<String, Object> attributes;

    public UIComponentStub(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    protected void addFacesListener(FacesListener arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void broadcast(FacesEvent arg0) throws AbortProcessingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decode(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encodeBegin(FacesContext arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encodeChildren(FacesContext arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encodeEnd(FacesContext arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent findComponent(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UIComponent> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClientId(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected FacesContext getFacesContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected FacesListener[] getFacesListeners(Class arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent getFacet(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, UIComponent> getFacets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFamily() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Renderer getRenderer(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRendererType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getRendersChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueBinding getValueBinding(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRendered() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processDecodes(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processRestoreState(FacesContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object processSaveState(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processUpdates(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processValidators(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void queueEvent(FacesEvent arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeFacesListener(FacesListener arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(UIComponent arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRendered(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRendererType(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValueBinding(String arg0, ValueBinding arg1) {
        throw new UnsupportedOperationException();
    }

    public boolean isTransient() {
        throw new UnsupportedOperationException();
    }

    public void restoreState(FacesContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    public Object saveState(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    public void setTransient(boolean arg0) {
        throw new UnsupportedOperationException();
    }

}
