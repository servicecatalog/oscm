/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;

/**
 * Request wrapper to perform a post request to the single sign on bridge.
 * 
 */
public class SsoHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SsoHttpServletRequestWrapper.class);

    private static final String HEADER_CONTENT_TYPE = "Content-type";
    private static final String HEADER_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String HEADER_CONTENT_LENGTH = "Content-length";

    private ServletInputStream servletInputStream;

    private Map<String, String> parameters;

    private SsoParameters ssoParameters = null;

    private String method = null;

    private String requestURI = null;

    public SsoHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            servletInputStream = request.getInputStream();
            if (servletInputStream != null
                    && !servletInputStream.markSupported()) {
                servletInputStream = new BufferedServletInputStream(
                        servletInputStream);
                byte[] buf = new byte[1024];
                int len = buf.length;
                int offset = 0;
                servletInputStream.mark(buf.length);
                do {
                    int inputLen = servletInputStream.read(buf, offset, len
                            - offset);
                    if (inputLen <= 0) {
                        break;
                    }
                    offset += inputLen;
                } while ((len - offset) > 0);
                servletInputStream.reset();

                parameters = new HashMap<String, String>();
                String str = new String(buf, 0, offset,
                        Constants.CHARACTER_ENCODING_UTF8);
                String[] pairs = str.split("&");
                for (int i = 0; i < pairs.length; i++) {
                    int idx = pairs[i].indexOf('=');
                    if (idx >= 0) {
                        parameters.put(pairs[i].substring(0, idx), URLDecoder
                                .decode(pairs[i].substring(idx + 1),
                                        Constants.CHARACTER_ENCODING_UTF8));
                    }
                }
            }
        } catch (IOException e) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_ACCESS_INPUT_STREAM_OF_REQUEST_FAILED);
        }
    }

    /**
     * 
     */
    public SsoHttpServletRequestWrapper(HttpServletRequest request,
            String subKey, String loginUrl, String instanceId,
            String userToken, String contextPath) {
        this(request);

        if (loginUrl == null) {
            loginUrl = "";
        }

        setRequestURI(request.getContextPath() + Constants.SERVICE_BASE_URI
                + "/" + subKey + loginUrl);
        setMethod(Constants.REQ_METHOD_POST);

        SsoParameters ssoParameters = new SsoParameters();
        if (contextPath == null) {
            contextPath = getParameter(Constants.REQ_PARAM_CONTEXT_PATH);
        }
        if (contextPath != null) {
            ssoParameters.setContextPath(contextPath);
        }
        ssoParameters.setInstanceId(instanceId);
        ssoParameters.setSubscriptionKey(subKey);
        ssoParameters.setBssId(request.getSession().getId());
        ssoParameters.setUsertoken(userToken);

        setSsoParameters(ssoParameters);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new BufferedServletInputStream(ssoParameters.getInputStream());
    }

    public void setInputStream(ServletInputStream servletInputStream) {
        this.servletInputStream = servletInputStream;
    }

    public SsoParameters getSsoParameters() {
        return ssoParameters;
    }

    public void setSsoParameters(SsoParameters ssoParameters) {
        this.ssoParameters = ssoParameters;
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String getMethod() {
        if (method == null) {
            return super.getMethod();
        }
        return method;
    }

    @Override
    public String getContentType() {
        if (method == null) {
            return super.getContentType();
        }
        return HEADER_CONTENT_TYPE_FORM;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getRequestURI() {
        if (requestURI == null) {
            return super.getRequestURI();
        }
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public int getContentLength() {
        if (ssoParameters == null) {
            return super.getContentLength();
        }
        return ssoParameters.getContentLength();
    }

    public String getHeaderInt(String name) {
        if (name.equalsIgnoreCase(HEADER_CONTENT_LENGTH)
                && ssoParameters != null) {
            return String.valueOf(ssoParameters.getContentLength());
        } else if (name.equalsIgnoreCase(HEADER_CONTENT_TYPE) && method != null
                && Constants.REQ_METHOD_POST.equalsIgnoreCase(method))
            return HEADER_CONTENT_TYPE_FORM;
        return null;
    }

    @Override
    public String getHeader(String name) {
        String value = getHeaderInt(name);
        if (value != null) {
            return value;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = getHeaderInt(name);
        if (value != null) {
            Vector<String> v = new Vector<String>();
            v.add(value);
            return v.elements();
        }
        return super.getHeaders(name);
    }

}
