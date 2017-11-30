/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 04.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.stubs;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;

/**
 * @author Mike J&auml;ger
 * 
 */
@SuppressWarnings("deprecation")
public class UIInputStub extends UIInput {

    private Object value;
    private String clientId = "newPassword2";

    public UIInputStub() {
    }

    public UIInputStub(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void addValidator(Validator validator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean compareValues(Object previous, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decode(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object getConvertedValue(FacesContext arg0, Object arg1)
            throws ConverterException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConverterMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFamily() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequiredMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSubmittedValue() {
        return value;
    }

    @Override
    public MethodBinding getValidator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getValidatorMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Validator[] getValidators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodBinding getValueChangeListener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueChangeListener[] getValueChangeListeners() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isImmediate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLocalValueSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequired() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processDecodes(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processUpdates(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processValidators(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeValidator(Validator validator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreState(FacesContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object saveState(FacesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConverterMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImmediate(boolean immediate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocalValueSet(boolean localValueSet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRequired(boolean required) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRequiredMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubmittedValue(Object submittedValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValid(boolean valid) {

    }

    @Override
    public void setValidator(MethodBinding arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValidatorMessage(String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValueChangeListener(MethodBinding arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateModel(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(FacesContext arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void validateValue(FacesContext arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Converter getConverter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getLocalValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setConverter(Converter converter) {
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
        return clientId;
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
    public void queueEvent(FacesEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeFacesListener(FacesListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException();
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

    @Override
    public void setValue(Object value) {
        this.value = value;
    }
}
