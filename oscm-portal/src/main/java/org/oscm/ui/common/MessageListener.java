/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletRequest;

import org.richfaces.component.UICalendar;
import org.richfaces.component.UITogglePanel;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.OpenStateBean;

/**
 * PhaseListener which sets the style attribute of all UI input components which
 * are associated with a message.
 * 
 */
public class MessageListener implements PhaseListener {

    private static final long serialVersionUID = -6974097776108013908L;
    private static final String ATTRIBUTE_FIRST_ERROR_ELEMENT = "firstErrorElement";
    private static final String ATTRIBUTE_ADDITIONAL_ERROR_ELEMENTS = "additionalErrorElements";
    private static final String ATTRIBUTE_STYLE = "style";
    private static final String STYLE_ERROR = "border:2px solid #A00000;";
    private static final String STYLE_CHECKBOX_ERROR = "outline:2px solid #A00000;";
    private static final String FILEUPLOAD_EXCEPTION = "org.apache.myfaces.custom.fileupload.exception";
    private static final String FILEUPLOAD_MAX_SIZE = "org.apache.myfaces.custom.fileupload.maxSize";

    private static final Set<String> IDS_TO_IGNORE = new HashSet<String>(
            Collections.singleton(BaseBean.PROGRESS_PANEL));

    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    public void beforePhase(PhaseEvent event) {
        FacesContext fc = event.getFacesContext();
        ServletRequest request = (ServletRequest) fc.getExternalContext()
                .getRequest();
        UIViewRoot root = fc.getViewRoot();
        resetColor(root.getChildren());
        List<String> additionalErrorElements = new ArrayList<String>();
        Set<String> sectionsToExpand = new HashSet<String>();
        Iterator<String> clientIds = fc.getClientIdsWithMessages();
        while (clientIds.hasNext()) {
            String clientId = clientIds.next();
            if (canIgnoreClientId(clientId)) {
                // skip, since it is not associated with a specific field
                continue;
            }
            if (request.getAttribute(ATTRIBUTE_FIRST_ERROR_ELEMENT) == null) {
                // set focus to first component only
                request.setAttribute(ATTRIBUTE_FIRST_ERROR_ELEMENT, clientId);
            }
            UIComponent uiComponent = root.findComponent(clientId);
            String sectionId = null;
            if (uiComponent != null) {
                sectionId = getSectionId(uiComponent);
                uiComponent.getAttributes().put(ATTRIBUTE_STYLE, STYLE_ERROR);
                Object object = uiComponent.getAttributes().get("connectedTo");
                if (object != null) {
                    UIComponent connected = root.findComponent(object
                            .toString());
                    if (connected != null) {
                        connected.getAttributes().put(ATTRIBUTE_STYLE,
                                STYLE_ERROR);
                    }
                }
                if (uiComponent instanceof UISelectBoolean) {
                    uiComponent.getAttributes().put(ATTRIBUTE_STYLE,
                            STYLE_CHECKBOX_ERROR);
                } else if (uiComponent instanceof UICalendar) {
                    additionalErrorElements.add(clientId + "InputDate");
                } else {
                    uiComponent.getAttributes().put(ATTRIBUTE_STYLE,
                            STYLE_ERROR);
                }
            } else {
                sectionId = getTableSectionId(clientId, root);
                // if the element is part of a data table we must change the
                // style from javascript
                additionalErrorElements.add(clientId);
            }
            if (sectionId != null) {
                sectionsToExpand.add(sectionId);
            }
        }
        request.setAttribute(ATTRIBUTE_ADDITIONAL_ERROR_ELEMENTS,
                additionalErrorElements);
        if (!sectionsToExpand.isEmpty()) {
            request.setAttribute(OpenStateBean.SECTIONS_TO_EXPAND,
                    sectionsToExpand);
        }
        if (request.getAttribute(ATTRIBUTE_FIRST_ERROR_ELEMENT) != null) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_TEXT_FIELDS, null);
        }

        if (request.getAttribute(FILEUPLOAD_EXCEPTION) != null) {
            // The upload of one or a bunch of files failed because either one
            // of the files exceeds the value of "uploadMaxFileSize" or the size
            // of all files exceeds the value of "uploadMaxSize". If
            // "uploadMaxSize" if not set => uploadMaxSize = uploadMaxFileSize

            Integer maxSize = (Integer) request
                    .getAttribute(FILEUPLOAD_MAX_SIZE);

            if (maxSize == null) {
                // The attribute indicating the maximum allowed size was not set
                JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                        BaseBean.ERROR_UPLOAD_SIZE_LIMIT_EXCEEDED, null);
            } else {
                // Calculate a more handy megabyte value and bring it to a nice
                // format
                Locale viewLocale = JSFUtils.getViewLocale();
                DecimalFormat df = (DecimalFormat) DecimalFormat
                        .getInstance(viewLocale);
                df.applyPattern("#0.00");

                double maxValueMb = maxSize.intValue() / (1024.0 * 1024.0);

                JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                        BaseBean.ERROR_UPLOAD_SIZE_LIMIT_EXCEEDED_KNOWNMAX,
                        new Object[] { df.format(maxValueMb) });
            }
        }
    }

    /**
     * Determines if a client id can be ignored.
     * 
     * @param clientId
     *            The client id to check.
     * @return <code>true</code> in case the client id related message can be
     *         ignored, <code>false</code> otherwise.
     */
    private boolean canIgnoreClientId(String clientId) {
        return clientId == null || IDS_TO_IGNORE.contains(clientId);
    }

    /**
     * Ids of fields inside tables look like <code>tableId:rowNr:fieldId</code>.
     * So we try to cut the fieldId and the rowNr parts to get the table id and
     * then check if it is inside a section and return the section id.
     * 
     * @param clientId
     *            the id of the field inside of a table
     * @param root
     *            the {@link UIViewRoot}
     * @return the section id or <code>null</code>
     */
    private String getTableSectionId(String clientId, UIViewRoot root) {
        if (!clientId.contains(":")) {
            return null;
        }
        // cut the field id
        clientId = clientId.substring(0, clientId.lastIndexOf(':'));
        if (!clientId.contains(":")) {
            return null;
        }
        // cut the row number - we should have the table id
        clientId = clientId.substring(0, clientId.lastIndexOf(':'));
        UIComponent comp = root.findComponent(clientId);
        if (comp != null) {
            return getSectionId(comp);
        }
        return null;
    }

    /**
     * Tries to get the id of a parent section if existing.
     * 
     * @param uiComponent
     *            the component to check the parents for
     * @return the id of the section if found or <code>null</code>
     */
    private String getSectionId(UIComponent uiComponent) {
        UIComponent parent = uiComponent.getParent();
        if (parent instanceof UIViewRoot) {
            return null;
        }
        if (parent instanceof UITogglePanel) {
            return parent.getId();
        }
        return getSectionId(parent);
    }

    /**
     * Remove the error style from all given UI input components
     * 
     * @param children
     *            list of UI components
     * 
     */
    private void resetColor(List<UIComponent> children) {
        for (UIComponent uiComponent : children) {
            if (isErrorStyle(uiComponent)) {
                uiComponent.getAttributes().put(ATTRIBUTE_STYLE, "");
            }
            // recursive call
            resetColor(uiComponent.getChildren());
        }
    }

    /**
     * Check if the component is an input component and if the style is the
     * error style.
     * 
     * @param uiComponent
     *            UI component
     * @return true if the component is an input component and if the style is
     *         the error style
     */
    private boolean isErrorStyle(UIComponent uiComponent) {
        if (!(uiComponent instanceof UIInput)) {
            return false;
        }
        if (uiComponent instanceof UISelectBoolean) {
            return (STYLE_CHECKBOX_ERROR).equals(uiComponent.getAttributes()
                    .get(ATTRIBUTE_STYLE));
        }
        return (STYLE_ERROR).equals(uiComponent.getAttributes().get(
                ATTRIBUTE_STYLE));
    }

    public void afterPhase(PhaseEvent e) {
    }

}
