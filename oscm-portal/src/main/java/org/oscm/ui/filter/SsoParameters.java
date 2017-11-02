/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 28.04.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.filter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Class which holds the parameters for a post request to the single sign on
 * bridge.
 * 
 */
public class SsoParameters {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SsoParameters.class);

    private String contextPath = "";

    private String instanceId = "LCM";

    private String language = Locale.ENGLISH.toString();

    private String subscriptionKey = "";

    private String bssId = "";

    private String usertoken = "";

    public int getContentLength() {
        return getQueryString().length();
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public String getBssId() {
        return bssId;
    }

    public void setBssId(String bssId) {
        this.bssId = bssId;
    }

    public String getUsertoken() {
        return usertoken;
    }

    public void setUsertoken(String usertoken) {
        this.usertoken = usertoken;
    }

    public String getQueryString() {
        try {
            String content = "usertoken="
                    + URLEncoder.encode(usertoken,
                            Constants.CHARACTER_ENCODING_UTF8)
                    + "&subKey="
                    // The internal representation of the subscription key
                    // is hexadecimal. We pass the decimal representation for
                    // the external APIs.
                    + URLEncoder.encode(String.valueOf(ADMStringUtils
                            .parseUnsignedLong(subscriptionKey)),
                            Constants.CHARACTER_ENCODING_UTF8)
                    + "&bssId="
                    + URLEncoder.encode(bssId,
                            Constants.CHARACTER_ENCODING_UTF8)
                    + "&saasId="
                    + URLEncoder.encode(subscriptionKey + "_" + bssId,
                            Constants.CHARACTER_ENCODING_UTF8)
                    + "&instanceId="
                    + URLEncoder.encode(instanceId,
                            Constants.CHARACTER_ENCODING_UTF8)
                    + "&language="
                    + URLEncoder.encode(language,
                            Constants.CHARACTER_ENCODING_UTF8);

            if (contextPath != null && contextPath.length() > 0) {
                content += "&contextPath="
                        + URLEncoder.encode(contextPath,
                                Constants.CHARACTER_ENCODING_UTF8);
            }

            if (logger.isDebugLoggingEnabled()) {
                logger.logDebug("SsoParameters() queryString:" + content);
            }

            return content;
        } catch (UnsupportedEncodingException e) {
            throw new SaaSSystemException("Encoding of SSO parameters failed!",
                    e);
        }
    }

    public InputStream getInputStream() {
        try {
            return new ByteArrayInputStream(getQueryString().getBytes(
                    Constants.CHARACTER_ENCODING_UTF8));
        } catch (UnsupportedEncodingException e) {
            throw new SaaSSystemException("Encoding of SSO parameters failed!",
                    e);
        }
    }

}
