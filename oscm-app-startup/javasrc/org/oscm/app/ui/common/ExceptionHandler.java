/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-11-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.common;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.oscm.app.ui.SessionConstants;

/**
 * Exception helper class.
 * 
 */
public class ExceptionHandler {

    public static final String BUNDLE_ERR_TITLE_KEY = "error.title";
    public static final String BUNDLE_ERR_TEXT_KEY = "error.text.default";

    /**
     * Read the message resource bundle with the locale of faces view
     * 
     * @param request
     *            the current HTTP request
     * @return the message resource bundle with the locale of the current user
     */
    private static ResourceBundle getBundle(HttpServletRequest request) {
        Locale locale = request.getLocale();
        String currentUserLocale = ""
                + request.getSession().getAttribute(
                        SessionConstants.SESSION_USER_LOCALE);
        if (currentUserLocale.trim().length() != 0) {
            String[] a = currentUserLocale.split("_");
            if (a.length > 2) {
                locale = new Locale(a[0], a[1], a[2]);
            } else if (a.length > 1) {
                locale = new Locale(a[0], a[1]);
            } else if (a.length > 0) {
                locale = new Locale(a[0]);
            }

        }
        return ResourceBundle.getBundle("org.oscm.app.ui.i18n.messages",
                locale);
    }

    public static String getErrorTitle(HttpServletRequest request) {
        ResourceBundle bundle = getBundle(request);
        if (bundle == null) {
            return BUNDLE_ERR_TITLE_KEY;
        }
        return bundle.getString(BUNDLE_ERR_TITLE_KEY);
    }

    public static String getErrorText(HttpServletRequest request) {
        ResourceBundle bundle = getBundle(request);
        if (bundle == null) {
            return BUNDLE_ERR_TEXT_KEY;
        }
        return bundle.getString(BUNDLE_ERR_TEXT_KEY);
    }

}
