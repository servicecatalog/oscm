/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.ejb.EJBException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.exceptions.ConnectException;
import org.oscm.types.exceptions.InvalidUserSession;
import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Exception helper class.
 * 
 */
public class ExceptionHandler {

    /**
     * Get the causing EJBException for the given Throwable or null if the
     * Throwable was not caused by an EJBException.
     * 
     * @param t
     *            the Throwable to be analyzed
     * @return the causing EJBException or null
     */
    public static EJBException getEJBException(Throwable t) {
        while (t != null && t != t.getCause() && !(t instanceof EJBException)) {
            t = t.getCause();
        }
        if (t instanceof EJBException) {
            return (EJBException) t;
        }
        return null;
    }

    /**
     * Get the causing SaasApplicationException for the given Throwable or null
     * if the Throwable was not caused by an SaasApplicationException.
     * 
     * @param t
     *            the Throwable to be analyzed
     * @return the causing SaasApplicationException or null
     */
    public static SaaSApplicationException getSaasApplicationException(
            Throwable t) {
        while (t != null && t != t.getCause()
                && !(t instanceof SaaSApplicationException)
                && !(t instanceof SaaSSystemException)) {
            if (t instanceof EJBException && t.getCause() instanceof Exception
                    && (((EJBException) t).getCausedByException() != null)) {
                t = ((EJBException) t).getCausedByException();
            } else {
                t = t.getCause();
            }
        }
        if (t instanceof SaaSApplicationException) {
            return (SaaSApplicationException) t;
        }
        return null;
    }

    /**
     * Try to find the root cause for the Throwable
     * 
     * @param t
     *            the Throwable to be analyzed
     * @return the potential root cause of the Throwable
     */
    public static Throwable unwrapException(Throwable t) {
        if (t instanceof EJBException) {
            EJBException ejbEx = (EJBException) t;
            if (ejbEx.getCause() instanceof Exception) {
                if (ejbEx != ejbEx.getCausedByException()
                        && ejbEx.getCausedByException() != null) {
                    t = unwrapException(ejbEx.getCausedByException());
                } else if (ejbEx != ejbEx.getCause()) {
                    t = unwrapException(ejbEx.getCause());
                }
            }
        }
        if (t instanceof RemoteException) {
            RemoteException remoteEx = (RemoteException) t;
            if (remoteEx != remoteEx.detail
                    && remoteEx.detail instanceof Exception) {
                t = unwrapException(remoteEx.detail);
            }
        }
        return t;
    }

    /**
     * Convert a EJBException into FacesMessage which is presented to the user.
     * 
     * @param ex
     *            the EJBException to be analyzed
     */
    public static void execute(EJBException ex) {
        if (ex != null && ex.getCause() instanceof Exception
                && ex.getCausedByException() instanceof AccessException) {
            handleOrganizationAuthoritiesException();
        } else if (ex != null && isInvalidUserSession(ex)) {
            HttpServletRequest request = JSFUtils.getRequest();
            request.getSession().removeAttribute(Constants.SESS_ATTR_USER);
            request.getSession().invalidate();
            handleInvalidSession();
        } else if (ex != null && isConnectionException(ex)) {
            handleMissingConnect(BaseBean.ERROR_DATABASE_NOT_AVAILABLE);
        } else {
            throw new FacesException(ex);
        }
    }

    /**
     * Return <code>true</code> if cause for the given {@link Throwable} is
     * InvalidUserSession
     * 
     * @param t
     *            the {@link Throwable} to be analyzed
     * @return <code>true</code> if and only if the root cause for the given
     *         {@link Throwable} is InvalidUserSession
     */
    public static boolean isInvalidUserSession(Throwable t) {
        if (unwrapException(getEJBException(t)) instanceof InvalidUserSession) {
            return true;
        }
        return false;
    }

    /**
     * Convert a SaaSApplicationException into FacesMessage which is presented
     * to the user.
     * 
     * @param ex
     *            the SaaSApplicationException to be analyzed
     */
    public static void execute(SaaSApplicationException ex) {
        execute(ex, false);
    }

    /**
     * Convert a SaaSApplicationException into FacesMessage which is presented
     * to the user.
     * 
     * @param ex
     *            the SaaSApplicationException to be analyzed
     * @param clean
     *            indicate whether to clean page status or not
     */
    public static void execute(SaaSApplicationException ex, boolean clean) {

        if (ex instanceof OrganizationAuthoritiesException) {
            handleOrganizationAuthoritiesException();
        } else {
            String[] params = ex.getMessageParams();
            localizeParameters(params);
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ex.getMessageKey(), params);
            if (ex instanceof ConcurrentModificationException || clean) {
                resetDirty();
            }
        }
    }

    public static void resetDirty() {
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        request.setAttribute(Constants.REQ_ATTR_DIRTY, Boolean.FALSE.toString());
    }

    public static void localizeParameters(String[] params) {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String key = getKeyForParam(params[i]);
                if (key == null) {
                    continue;
                }
                String desc = JSFUtils.getText(key, null);
                if (desc != null) {
                    params[i] = desc;
                }
            }
        }
    }

    /**
     * 
     */
    private static void handleOrganizationAuthoritiesException() {
        FacesContext fc = FacesContext.getCurrentInstance();
        // distinguish old/new portal by means of relative marketplace url
        HttpServletRequest request = JSFUtils.getRequest();
        if (request.getServletPath().startsWith(Marketplace.MARKETPLACE_ROOT)) {
            fc.getApplication()
                    .getNavigationHandler()
                    .handleNavigation(fc, "bean.actionName",
                            "InsufficientOrganizationAuthoritiesMPL");
        } else {
            fc.getApplication()
                    .getNavigationHandler()
                    .handleNavigation(fc, "bean.actionName",
                            "InsufficientOrganizationAuthorities");
        }
        fc.responseComplete();
    }

    /**
     * Determines the key to be used for resource bundle lookups that is related
     * to the given parameter. Returns <code>null</code> in case the value does
     * not start with prefix 'enum.'.
     * 
     * @param param
     *            The parameter value.
     * 
     * @return The key to be used for resource bundle checks.
     */
    public static String getKeyForParam(String param) {
        String result = null;
        if (param != null
                && param.startsWith(SaaSApplicationException.ENUM_PREFIX)) {
            result = param.substring(SaaSApplicationException.ENUM_PREFIX
                    .length());
        }
        return result;
    }

    /**
     * Read the message resource bundle with the locale of the current user
     * 
     * @param request
     *            the current HTTP request
     * @return the message resource bundle with the locale of the current user
     */
    private static ResourceBundle getBundle(HttpServletRequest request) {
        Locale locale = Locale.ENGLISH;
        VOUserDetails voUserDetails = (VOUserDetails) request.getSession()
                .getAttribute(Constants.SESS_ATTR_USER);
        if (voUserDetails != null) {
            String[] a = voUserDetails.getLocale().split("_");
            if (a.length > 2) {
                locale = new Locale(a[0], a[1], a[2]);
            } else if (a.length > 1) {
                locale = new Locale(a[0], a[1]);
            } else if (a.length > 0) {
                locale = new Locale(a[0]);
            }

        }
        return ResourceBundle.getBundle(
                "org.oscm.ui.resources.Messages", locale);
    }

    public static String getErrorTitle(HttpServletRequest request) {
        ResourceBundle bundle = getBundle(request);
        String key = "error.title";
        if (bundle == null) {
            return key;
        }
        return bundle.getString(key);
    }

    public static String getErrorText(HttpServletRequest request) {
        ResourceBundle bundle = getBundle(request);
        if (bundle == null) {
            return Constants.BUNDLE_ERR_KEY;
        }
        return bundle.getString(Constants.BUNDLE_ERR_KEY);
    }

    /**
     * 
     */
    public static void handleInvalidSession() {
        FacesContext fc = FacesContext.getCurrentInstance();
        // distinguish old/new portal by means of relative marketplace url
        HttpServletRequest request = JSFUtils.getRequest();
        if (request.getServletPath().startsWith(Marketplace.MARKETPLACE_ROOT)) {
            fc.getApplication()
                    .getNavigationHandler()
                    .handleNavigation(fc, "bean.actionName",
                            "marketplace/login");
        } else {
            fc.getApplication().getNavigationHandler()
                    .handleNavigation(fc, "bean.actionName", "login");
        }
        fc.responseComplete();
    }

    /**
     * @throws IOException
     * @throws IllegalArgumentException
     * 
     */
    public static void handleMissingConnect(String errMsg) {
        HttpServletRequest request = JSFUtils.getRequest();
        HttpServletResponse response = JSFUtils.getResponse();
        String relativePath = "";
        if (request.getServletPath().startsWith(Marketplace.MARKETPLACE_ROOT)) {
            relativePath = BaseBean.MARKETPLACE_ERROR_PAGE;
        } else {
            relativePath = BaseBean.ERROR_PAGE;
        }
        if (errMsg != null) {
            StringBuffer sb = new StringBuffer(relativePath);
            if (relativePath.indexOf('?') > 0)
                sb.append('&');
            else
                sb.append('?');
            sb.append(Constants.REQ_ATTR_ERROR_KEY);
            sb.append("=");
            sb.append(errMsg);
            relativePath = sb.toString();
        }
        try {
            JSFUtils.sendRedirect(response, request.getContextPath()
                    + relativePath);
        } catch (Exception e) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    Constants.BUNDLE_ERR_KEY, null);
        }
    }

    public static boolean isConnectionException(Throwable th) {
        th = unwrapException(th);
        if (th instanceof java.net.ConnectException
                || th instanceof ConnectException) {
            return true;
        }
        return false;
    }

    public static boolean execute(FacesException exc) {
        boolean handled = false;
        SaaSApplicationException saasE = getSaasApplicationException(exc);
        EJBException ejbException = getEJBException(exc);
        if (saasE != null) {
            execute(saasE);
            handled = true;
        } else if (ejbException != null) {
            execute(ejbException);
            handled = true;
        }
        return handled;
    }
}
