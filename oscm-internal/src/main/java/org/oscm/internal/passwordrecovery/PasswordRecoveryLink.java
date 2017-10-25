/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterEncoder;
import org.oscm.domobjects.PlatformUser;

/**
 * Generate and decode the confirm url for password recovery
 * 
 * @author Mao
 * 
 */
class PasswordRecoveryLink {

    private final boolean isPureCustomer;
    private final static String tokenString = "token=";
    private final static String tailString = "&et";

    private final ConfigurationServiceLocal configs;

    public PasswordRecoveryLink(boolean isPureCustomer,
            ConfigurationServiceLocal configs) {
        this.isPureCustomer = isPureCustomer;
        this.configs = configs;
    }

    /**
     * Decode the confirmation link for password recovery.
     * 
     * @param recoveryPasswordLink
     * @return parameters array which contains userId, passwordRecoveryStartDate
     *         and marketplaceId (can be null)
     */
    static String[] decodeRecoveryPasswordLink(
            String recoveryPasswordLink) {
        if (recoveryPasswordLink == null || recoveryPasswordLink.length() == 0) {
            return null;
        }
        
        try {
            recoveryPasswordLink = URLDecoder.decode(
                    recoveryPasswordLink, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        if (recoveryPasswordLink.endsWith(tailString)) {
            recoveryPasswordLink = recoveryPasswordLink.substring(0,
                    recoveryPasswordLink.indexOf(tailString));
        }
        return ParameterEncoder.decodeParameters(recoveryPasswordLink);
    }

    /**
     * remove all the "/" characters in the end of url
     * 
     * @param url
     */
    void removeTrailingSlashes(StringBuffer url) {
        while (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
            url.replace(url.length() - 1, url.length(), "");
        }
    }

    /**
     * Encode the password recovery link with a generated token, information of
     * the user, marketplace ID and timeStamp.
     * 
     * @param pUser
     * @param timeStamp
     * @param marketplaceId
     * @throws UnsupportedEncodingException
     *             - if the encoding has failed
     */
    String encodePasswordRecoveryLink(PlatformUser pUser, long timeStamp,
            String marketplaceId) throws UnsupportedEncodingException {
        StringBuffer url = new StringBuffer();
        String[] urlParam = new String[3];
        urlParam[0] = pUser.getUserId();
        urlParam[1] = Long.toString(timeStamp);
        urlParam[2] = (marketplaceId == null) ? "" : marketplaceId;

        url.append(configs.getBaseURL());
        removeTrailingSlashes(url);

        if (!isPureCustomer) {
            url.append("/public/changePassword.jsf?").append(tokenString);
        } else {
            url.append("/marketplace/changePassword.jsf?")
                    .append((marketplaceId != null) ? "mId=" + marketplaceId
                            + "&" : "").append(tokenString);
        }
        url.append(generateToken(urlParam));
        url.append(tailString);
        return url.toString();
    }

    /**
     * Generate the token for the confirmation URL.
     * 
     * @throws UnsupportedEncodingException
     *             - if the encoding has failed
     */
    StringBuffer generateToken(String[] urlParam)
            throws UnsupportedEncodingException {
        StringBuffer token = new StringBuffer();
        token.append(URLEncoder.encode(
                ParameterEncoder.encodeParameters(urlParam), "UTF-8"));
        return token;
    }
}
