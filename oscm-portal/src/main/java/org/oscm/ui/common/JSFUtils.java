/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Utility class which provides helper methods around the JSF classes.
 * 
 */
public class JSFUtils {

    /**
     * Append a FacesMessage to the current faces context. The FacesMessage is
     * added to the set of messages associated with the specified client
     * identifier, if clientId is not null. If clientId is null, this
     * FacesMessage is assumed to not be associated with any specific component
     * instance.
     * 
     * @param clientId
     *            the id of the client component to which the message is
     *            associated. If null the message is not associated with any
     *            specific component.
     * 
     * @param severity
     *            the message severity.
     * 
     * @param key
     *            the key of the message in the resource bundle.
     * 
     * @param params
     *            option parameters of the message.
     */
    public static void addMessage(String clientId,
            FacesMessage.Severity severity, String key, Object[] params) {
        FacesContext fc = FacesContext.getCurrentInstance();

        // if the current user changed his locale we must update the locale
        // in the view before the info message is added
        verifyViewLocale();

        String text = JSFUtils.getText(key, params);
        if (!existMessageInList(fc, text)) {
            fc.addMessage(clientId, new FacesMessage(severity, text, null));
        }
    }

    /**
     * Checks if a message is already added to FacesContext messages list.
     * 
     * @param fc
     *            FacesContext instance
     * 
     * @param msg
     *            the message
     * @return Returns true if the msg exists, otherwise false.
     */
    public static boolean existMessageInList(FacesContext fc, String msg) {
        for (Iterator<FacesMessage> i = fc.getMessages(); i.hasNext();) {
            if (i.next().getDetail().equals(msg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean replaceMessageInListIfExisting(String oldKey,
            Object[] oldParams, String newKey, Object[] newParams) {

        FacesContext fc = FacesContext.getCurrentInstance();
        String oldText = JSFUtils.getText(oldKey, oldParams);
        String newText = JSFUtils.getText(newKey, newParams);

        for (Iterator<FacesMessage> i = fc.getMessages(); i.hasNext();) {
            FacesMessage fm = i.next();
            if (fm.getDetail().equals(oldText)) {
                fm.setDetail(newText);
                fm.setSummary(newText);
                return true;
            }
        }
        return false;
    }

    public static boolean hasErrors(FacesContext fc) {
        for (Iterator<FacesMessage> i = fc.getMessages(); i.hasNext();) {
            FacesMessage m = i.next();
            if (FacesMessage.SEVERITY_ERROR == m.getSeverity()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasWarnings(FacesContext fc) {
        for (Iterator<FacesMessage> i = fc.getMessages(); i.hasNext();) {
            FacesMessage m = i.next();
            if (FacesMessage.SEVERITY_WARN == m.getSeverity()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its
     * parents. If the params array is set, placeholders in this string will be
     * replaced by the elements of the array.
     * 
     * @param key
     *            the key for the desired string.
     * @param params
     *            an array of objects to be formatted and substituted.
     */
    public static String getText(String key, Object[] params) {
        FacesContext fc = FacesContext.getCurrentInstance();
        return getText(key, params, fc);
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its
     * parents. If the params array is set, placeholders in this string will be
     * replaced by the elements of the array.
     * 
     * @param key
     *            the key for the desired string.
     * @param params
     *            an array of objects to be formatted and substituted.
     * @param context
     *            the FacesContext that is currently used
     * 
     */
    public static String getText(String key, Object[] params,
            FacesContext context) {
        ResourceBundle bundle = context.getApplication().getResourceBundle(
                context, Constants.BUNDLE_NAME);
        String text;
        try {
            text = bundle.getString(key);
        } catch (MissingResourceException e) {
            text = "?? key " + key + " not found ??";
        }

        if (params != null) {
            SimpleDateFormat f = null;
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Date) {
                    if (f == null) {
                        f = new SimpleDateFormat(
                                bundle.getString(Constants.BUNDLE_DATE_PATTERN_KEY),
                                bundle.getLocale());
                    }
                    params[i] = f.format(params[i]);
                }
            }
            MessageFormat mf = new MessageFormat(text, bundle.getLocale());
            text = mf.format(params, new StringBuffer(), null).toString();
        }
        return text;
    }

    /**
     * Helper method for getting the generic FacesMessage.
     * 
     * @param uiComponent
     *            the UIComponent
     * @param context
     *            the FacesContext
     * @return the generated FacesMessage
     */
    public static FacesMessage getFacesMessage(UIComponent uiComponent,
            FacesContext context, String message) {
        String param[] = null;
        String label = JSFUtils.getLabel(uiComponent);
        if (label != null) {
            param = new String[] { label };
        }
        String text = JSFUtils.getText(message, param, context);
        FacesMessage facesMessage = new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null);
        return facesMessage;
    }

    /**
     * Gets the label of a component.
     * 
     * @param component
     *            the component
     * @return the label of the component.
     */
    public static String getLabel(UIComponent component) {
        String label = null;
        if (component != null) {
            Object obj = component.getAttributes().get(
                    Constants.UI_COMPONENT_ATTRIBUTE_LABEL);
            if (obj instanceof String) {
                label = (String) obj;
            }
        }
        return label;
    }

    /**
     * Verifies that the view locale is equal to the user's locale
     * 
     */
    public static void verifyViewLocale() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null)
            verifyViewLocale(fc);
    }

    private static void verifyViewLocale(FacesContext fc) {
        HttpServletRequest request = (HttpServletRequest) fc
                .getExternalContext().getRequest();
        String localeString = localeStringFrom(request);

        // if the view locale differs from the users locale change the view
        // locale
        UIViewRoot viewRoot = fc.getViewRoot();
        if (localeString != null
                && !viewRoot.getLocale().toString().equals(localeString)) {
            Iterator<Locale> it = fc.getApplication().getSupportedLocales();
            while (it.hasNext()) {
                Locale locale = it.next();
                if (locale.toString().equals(localeString)) {
                    viewRoot.setLocale(locale);
                    return;
                }
            }
            // we use the default locale if the requested locale was not
            // found
            if (!viewRoot.getLocale().equals(
                    fc.getApplication().getDefaultLocale())) {
                viewRoot.setLocale(fc.getApplication().getDefaultLocale());
            }
        }
    }

    private static String localeStringFrom(HttpServletRequest request) {
        if (request == null)
            return null;
        HttpSession session = request.getSession(false);
        String localeString = null;
        VOUserDetails voUserDetails = null;
        if (session != null) {
            voUserDetails = (VOUserDetails) session
                    .getAttribute(Constants.SESS_ATTR_USER);
        }
        if (voUserDetails == null) {
            localeString = request.getParameter(Constants.REQ_PARAM_LOCALE);
            if (session != null) {
                if (localeString == null) {
                    localeString = (String) session
                            .getAttribute(Constants.REQ_PARAM_LOCALE);
                } else {
                    session.setAttribute(Constants.REQ_PARAM_LOCALE,
                            localeString);
                }
            }
        } else {
            localeString = voUserDetails.getLocale();
        }
        return localeString;
    }

    /**
     * Gets the current HTTP session from the faces context.
     * 
     * @return the current HTTP session or null if no session exists.
     */
    public static HttpSession getSession() {
        if (FacesContext.getCurrentInstance() == null) {
            return null;
        }
        return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
    }

    /**
     * Gets the current HTTP request from the faces context.
     * 
     * @return the current HTTP request or null if no faces context exists.
     */
    public static HttpServletRequest getRequest() {
        if (FacesContext.getCurrentInstance() == null) {
            throw new SaaSSystemException("FacesContext not initialized!");
        }
        return (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
    }

    /**
     * Sets a value in the http request
     */
    public static void setRequestAttribute(String key, String value) {
        getRequest().setAttribute(key, value);
    }

    /**
     * Sets a value in the http session
     */
    public static void setSessionAttribute(String key, String value) {
        HttpSession session = getRequest().getSession(false);
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    /**
     * Retrieves the attribute with the given key.
     * 
     * @param key
     *            lookup key
     * @return the attribute value
     */
    public static Object getSessionAttribute(String key) {
        HttpSession session = getSession();
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     * Adds the given attribute with the given key to the session.
     * 
     * @param key
     *            lookup key
     * @param value
     *            attribute value to store
     */
    public static void setSessionAttribute(String key, Object value) {
        getSession().setAttribute(key, value);
    }

    /**
     * Gets the current HTTP response from the faces context.
     * 
     * @return the current HTTP response or null if no faces context exists.
     */
    public static HttpServletResponse getResponse() {
        if (FacesContext.getCurrentInstance() == null) {
            throw new SaaSSystemException("FacesContext not initialized!");
        }
        return (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext().getResponse();
    }

    /**
     * Reset the values of all UIInput children. This might be necessary after a
     * validation error to successfully process an AJAX request. See [Bug 5449]
     * and http://wiki.apache.org/myfaces/ClearInputComponents
     * 
     * @param uiComponent
     *            the root component to be processed.
     */
    public static void resetUIInputChildren(UIComponent uiComponent) {
        if (uiComponent != null) {
            List<UIComponent> children = uiComponent.getChildren();
            for (UIComponent child : children) {
                if (child instanceof UIInput) {
                    UIInput uiInput = (UIInput) child;
                    uiInput.setSubmittedValue(null);
                    uiInput.setValue(null);
                    uiInput.setLocalValueSet(false);
                } else {
                    resetUIInputChildren(child);
                }
            }
        }
    }

    /**
     * Removes the specified components, searching down the UI tree beginning
     * with the specifier root component, from the children lists of their
     * parents, so they are rebuilt and rendered again. This might be necessary
     * after changing the UI model without navigating away from the page.
     * 
     * @param rootComponent
     *            the component to begin searching down the UI tree for the
     *            specified components
     * @param componentIds
     *            the IDs of the components to be reseted
     * @see http://wiki.apache.org/myfaces/ClearInputComponents
     */
    public static void resetUIComponents(UIComponent rootComponent,
            Set<String> componentIds) {
        List<UIComponent> children = rootComponent.getChildren();
        for (UIComponent child : children) {
            if (componentIds.contains(child.getId())) {
                child.getParent().getChildren().remove(child);
            } else {
                resetUIComponents(child, componentIds);
            }
        }
    }

    /**
     * Returns the {@link Locale} currently set in the view root.
     * 
     * @return the current {@link Locale}
     */
    public static Locale getViewLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    /**
     * Returns the value stored in the cookie represented by the given param.
     * 
     * @param httpRequest
     *            the request
     * @param param
     *            the param representing the name of the cookie
     * @return the cookie value
     * @throws IllegalArgumentException
     *             in case the value contains illegal chars like \n or \r
     */
    public static String getCookieValue(HttpServletRequest httpRequest,
            String param) {
        final Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (param.equals(cookies[i].getName())) {
                    return validateHeader(cookies[i].getValue());
                }
            }
        }
        return null;
    }

    /**
     * Adds a cookie to the http response. Validates the value first. the value
     * stored in the cookie represented by the given param.
     * 
     * @param httpResponse
     *            the response
     * @param name
     *            the cookie name
     * @param value
     *            the cookie value
     * @param maxAge
     *            the life time of the cookie. Only value greater than -1 will
     *            be set. 0 causes the cookie to be deleted.
     * @throws IllegalArgumentException
     *             in case the value contains illegal chars like \n or \r
     */
    public static void setCookieValue(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, String name, String value,
            int maxAge) {
        final Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    httpResponse.addCookie(cookie);
                }
            }
        }
        final Cookie cookie = new Cookie(name, validateHeader(value));
        cookie.setHttpOnly(true);
        cookie.setPath(httpRequest.getContextPath());
        if (maxAge > -1) {
            cookie.setMaxAge(maxAge);
        }
        httpResponse.addCookie(cookie);
    }

    /**
     * Get bean by name from FacesContext. <br/>
     * Do NOT use this method directly due to testability. Please use class
     * BeanLocator.find...
     */
    @SuppressWarnings("unchecked")
    public static <T> T findBean(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context,
                "#{" + beanName + "}", Object.class);
    }

    /**
     * Check the given URL for illegal characters and send redirect response.
     * 
     * @param redirectUrl
     *            - the URL to redirect to.
     * @throws IllegalArgumentException
     *             - if URL contains illegal characters
     * 
     * @see HttpServletResponse#sendRedirect(String)
     */
    public static void sendRedirect(HttpServletResponse response,
            String redirectUrl) throws IllegalArgumentException, IOException {
        response.sendRedirect(validateHeader(redirectUrl));
    }

    /**
     * Check the given URL for illegal characters and redirect.
     * 
     * @param redirectUrl
     *            - the URL to redirect to.
     * @throws IllegalArgumentException
     *             - if URL contains illegal characters
     * 
     * @see ExternalContext#redirect(String)
     */
    public static void redirect(ExternalContext extContext, String redirectUrl)
            throws IllegalArgumentException, IOException {
        extContext.redirect(validateHeader(redirectUrl));
    }

    private static String validateHeader(String value)
            throws IllegalArgumentException {
        if (value != null
                && (value.indexOf('\r') > -1 || value.indexOf('\n') > -1)) {
            throw new IllegalArgumentException("The value '" + value
                    + "' contains illegal characters.");
        }
        return value;
    }

    /**
     * Writes the given content to the response as attachment of the given type
     * with the given filename.
     * 
     * @param content
     *            the data
     * @param filename
     *            the wanted filename
     * @param contentType
     *            the wanted content type
     * @throws IOException
     */
    public static void writeContentToResponse(byte[] content, String filename,
            String contentType) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        writeContentToResponse(content, filename, contentType, fc);
        fc.responseComplete();
    }

    /**
     * Writes the given content to the response as attachment of the given type
     * with the given filename.
     * 
     * @param content
     *            the data
     * @param filename
     *            the wanted filename
     * @param contentType
     *            the wanted content type
     * @param fc
     *            the face context
     * @throws IOException
     */
    public static void writeContentToResponse(byte[] content, String filename,
            String contentType, FacesContext fc) throws IOException {
        HttpServletResponse response = (HttpServletResponse) fc
                .getExternalContext().getResponse();

        response.setContentType(contentType);
        response.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);
        response.setHeader("Content-disposition", "attachment; filename=\""
                + filename + "\"");
        response.setContentLength(content.length);
        OutputStream out;
        out = response.getOutputStream();
        out.write(content);
        out.flush();
        out.close();
    }

}
