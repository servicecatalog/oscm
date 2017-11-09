/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.07.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.saml2.api.SAMLResponseExtractor;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.internal.types.exception.UserIdNotFoundException;

/**
 * @author stavreva
 * 
 */
public class SAMLCredentials {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SAMLCredentials.class);

    private static final SAMLResponseExtractor samlResponse = new SAMLResponseExtractor();

    private HttpServletRequest httpRequest;

    public SAMLCredentials(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Get user id from the SAMLResponse parameter of the request.
     * 
     * @param httpRequest
     * @return
     */
    protected String getUserId() {

        String response = httpRequest.getParameter("SAMLResponse");
        if (ADMStringUtils.isBlank(response)) {
            return null;
        }

        String userId = null;
        try {
            userId = getSAMLResponse().getUserId(response);
        } catch (UserIdNotFoundException e) {
            try {
                getLogger()
                        .logError(
                                LogMessageIdentifier.ERROR_GET_USER_FROM_SAML_RESPONSE_FAILED,
                                new String(getSAMLResponse().decode(response)));
            } catch (UnsupportedEncodingException e1) {
                getLogger().logError(
                        LogMessageIdentifier.ERROR_DECODE_SAML_RESPONSE_FAILED,
                        response);
            }
        }
        return userId;
    }

    /**
     * Generate password which consist of the request id and the SAML response.
     * It will be used in the custom realm implementation for authenticating the
     * user in case OSCM acts as a service provider.
     * 
     * @return
     */
    protected String generatePassword() {

        return generatePassword(getTenantId());
    }

    /**
     * Generate password which consist of the request id and the SAML response.
     * It will be used in the custom realm implementation for authenticating the
     * user in case OSCM acts as a service provider.
     *
     * @param tenantId generates password using thenant
     * @return
     */
    protected String generatePassword(String tenantId) {

        String response = httpRequest.getParameter("SAMLResponse");
        String requestId = getRequestId();
        if (ADMStringUtils.isBlank(response)
                || ADMStringUtils.isBlank(requestId)) {
            return null;
        }

        String password = "";
        try {
            String decodedSamlResponse = new String(getSAMLResponse().decode(response));
            password = "UI" + requestId + tenantId + decodedSamlResponse;
        } catch (UnsupportedEncodingException e) {
            getLogger().logError(
                    LogMessageIdentifier.ERROR_DECODE_SAML_RESPONSE_FAILED,
                    response);
        }
        return password;
    }

    protected String getRequestId() {
        return (String) httpRequest.getSession().getAttribute(
                Constants.SESS_ATTR_IDP_REQUEST_ID);
    }

    protected String getTenantId() {
        return (String) httpRequest.getSession().getAttribute(
                Constants.REQ_PARAM_TENANT_ID);
    }

    protected Log4jLogger getLogger() {
        return logger;
    }

    protected SAMLResponseExtractor getSAMLResponse() {
        return samlResponse;
    }

}
