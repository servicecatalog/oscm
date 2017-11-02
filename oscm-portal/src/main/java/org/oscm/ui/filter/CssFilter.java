/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 02.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.oscm.ui.common.ADMStringUtils;

/**
 * Filter which sets the content type of the response to text/css and the
 * expires header attribute so that the response can be cached by the browser.
 * 
 */
public class CssFilter implements Filter {

    private final static String HEADER_CACHE_CONTROL = "Cache-Control";
    private final static String HEADER_EXPIRES = "Expires";
    private final static String HEADER_LAST_MODIFIED = "Last-Modified";
    private final static char[] HTML_END_TAG = "</html>".toCharArray();

    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service.
     * 
     * @param filterConfig
     *            the filter configuration
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     */
    @Override
    public void destroy() {
    }

    /**
     * Set the content type and the expires header attribute of the response.
     * 
     * The doFilter method of the Filter is called by the container each time a
     * request/response pair is passed through the chain due to a client request
     * for a resource at the end of the chain.
     * 
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!httpRequest.getServletPath().endsWith("css.jsf")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (!ADMStringUtils.isBlank(httpRequest.getHeader("If-Modified-Since"))) {
            httpResponse.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
            return;
        }

        httpResponse.setContentType("text/css");

        httpResponse.setHeader(HEADER_CACHE_CONTROL, "max-age=1800");
        final SimpleDateFormat df = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss z");
        final Calendar cal = Calendar.getInstance();
        httpResponse.setHeader(HEADER_LAST_MODIFIED, df.format(cal.getTime()));
        cal.add(Calendar.MINUTE, 30);
        httpResponse.setHeader(HEADER_EXPIRES, df.format(cal.getTime()));

        final HttpServletResponse wrappedHttpResponse = new HttpServletResponseWrapper(
                httpResponse) {
            PrintWriter printWriter;

            /**
             * Avoid that the content type is overwritten
             */
            @Override
            public void setContentType(String ignore) {
            }

            /**
             * Avoid that the already set header values are overwritten
             */
            @Override
            public void setHeader(String key, String value) {
                if (!HEADER_CACHE_CONTROL.equalsIgnoreCase(key)
                        && !HEADER_EXPIRES.equalsIgnoreCase(key)
                        && !HEADER_LAST_MODIFIED.equalsIgnoreCase(key)) {
                    ((HttpServletResponse) super.getResponse()).setHeader(key,
                            value);
                }
            }

            /**
             * Skip the trailing </html> which is added by JSF
             */
            @Override
            public PrintWriter getWriter() throws IOException {

                // this to the client
                if (printWriter != null) {
                    return printWriter;
                }

                printWriter = new PrintWriter(new FilterWriter(
                        httpResponse.getWriter()) {

                    private int idx = 0;

                    @Override
                    public void write(int c) throws IOException {
                        if (c == HTML_END_TAG[idx]) {
                            idx++;
                            if (idx == HTML_END_TAG.length) {
                                idx = 0;
                            }
                        } else {
                            if (idx > 0) {
                                for (int i = 0; i < idx; i++) {
                                    out.write(HTML_END_TAG[i]);
                                }
                                idx = 0;
                            }
                            out.write(c);
                        }
                    }

                    @Override
                    public void write(char[] cbuf, int off, int len)
                            throws IOException {
                        for (int i = off; i < off + len; i++) {
                            write(cbuf[i]);
                        }
                    }

                    @Override
                    public void write(String str, int off, int len)
                            throws IOException {
                        for (int i = off; i < off + len; i++) {
                            write(str.charAt(i));
                        }
                    }

                });

                return printWriter;

            }

        };

        final HttpServletRequestWrapper wrappedHttpRequest = new HttpServletRequestWrapper(
                httpRequest) {
            /*
             * Since we misappropriate JSF to generate CSS files we have to
             * overcome the restriction that the default ResponseWriter can not
             * actually deliver responses of type 'text/css'. In the case the
             * browser accepts only the specific type (e.g. IE9 in strict mode
             * does so), we need to fake the accept header setting a little (add
             * a wild card part) to get JSF delivering the content correctly.
             */

            @Override
            public String getHeader(String name) {

                if ("accept".equalsIgnoreCase(name)) {
                    String accept = super.getHeader(name);
                    if (accept != null && accept.trim().length() > 0
                            && accept.indexOf("*/*") < 0) {
                        return accept + ", */*";
                    }
                    return "*/*";
                }
                return super.getHeader(name);
            }
        };

        chain.doFilter(wrappedHttpRequest, wrappedHttpResponse);
    }
}
