/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.07.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Avoid "Unable to set request character encoding to UTF-8" messages in the
 * glassfish logfile
 * 
 * @author kulle
 */
public class IgnoreCharacterEncodingHttpRequestWrapper extends
        HttpServletRequestWrapper {

    /**
     * If the character encoding is not initialized, it will be set to UTF-8
     */
    public IgnoreCharacterEncodingHttpRequestWrapper(HttpServletRequest request)
            throws UnsupportedEncodingException {

        super(request);
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);
        }
    }

    @Override
    public void setCharacterEncoding(String enc)
            throws UnsupportedEncodingException {
    }
}
