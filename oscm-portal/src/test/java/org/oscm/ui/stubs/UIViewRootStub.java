/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseListener;
import javax.faces.render.Renderer;

/**
 * @author Mike J&auml;ger
 * 
 */
@SuppressWarnings("deprecation")
public class UIViewRootStub extends UIViewRoot {

    private Map<String, Object> components = new HashMap<String, Object>();

    private String id;

    @Override
    public void addPhaseListener(PhaseListener newPhaseListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createUniqueId() {
        return "viewRootID-" + new Date().getTime();
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodExpression getAfterPhaseListener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodExpression getBeforePhaseListener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFamily() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRenderKitId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getViewId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processApplication(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processDecodes(FacesContext arg0) {
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
    public void removePhaseListener(PhaseListener toRemove) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object saveState(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAfterPhaseListener(MethodExpression newAfterPhase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBeforePhaseListener(MethodExpression newBeforePhase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRenderKitId(String renderKitId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setViewId(String viewId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void addFacesListener(FacesListener listener) {
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
    public void encodeChildren(FacesContext arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent findComponent(String key) {
        return (UIComponent) components.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
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

    @SuppressWarnings("rawtypes")
    @Override
    protected FacesListener[] getFacesListeners(Class arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UIComponent getFacet(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFacetCount() {
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
    public String getId() {
        return this.id;
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
    public ValueBinding getValueBinding(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId,
            ContextCallback callback) throws FacesException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRendered() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransient() {
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
    protected void removeFacesListener(FacesListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setParent(UIComponent parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRendered(boolean rendered) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRendererType(String rendererType) {
    }

    @Override
    public void setTransient(boolean transientFlag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValueBinding(String arg0, ValueBinding arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encodeAll(FacesContext arg0) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContainerClientId(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueExpression getValueExpression(String arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValueExpression(String arg0, ValueExpression arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    public void addComponent(String name, Object value) {
        this.components.put(name, value);
    }

    public void resetComponents() {
        this.components.clear();
    }
}
