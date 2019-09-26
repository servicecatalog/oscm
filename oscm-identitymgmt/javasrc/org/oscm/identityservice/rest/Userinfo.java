/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: Aug 9, 2019
 *
 *******************************************************************************/
package org.oscm.identityservice.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.oscm.identityservice.model.UserinfoModel;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.google.gson.Gson;

public class Userinfo {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(Userinfo.class);

    public VOUserDetails getUserinfoFromIdentityService(String userId,
            String tenantId, String token) throws Exception {
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(createUrlForUserinfo(userId, tenantId));
            conn = createConnection(url, token);

            if (!RestUtils.isResponseSuccessful(conn.getResponseCode())) {
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED,
                        "Response code from identity service was "
                                + conn.getResponseCode());
                throw new RegistrationException(
                        "Response code from identity service was "
                                + conn.getResponseCode());
            }
            response = RestUtils.getResponse(conn.getInputStream());
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
            throw e;
        } finally {
            conn.disconnect();
        }
        return createUserDetails(response, userId);
    }
    
    
    public List<VOUserDetails> getAllUserDetailsForGroup(String groupId, String tenantId, String token){
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(createUrlForAllUsersInGroup(groupId, tenantId));
            conn = createConnection(url, token);

            if (!RestUtils.isResponseSuccessful(conn.getResponseCode())) {
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED,
                        "Response code from identity service was "
                                + conn.getResponseCode());
                throw new RegistrationException(
                        "Response code from identity service was "
                                + conn.getResponseCode());
            }
            response = RestUtils.getResponse(conn.getInputStream());
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
          //  throw e;
        } finally {
            conn.disconnect();
        }
        return null;
    }

    private String createUrlForAllUsersInGroup(String groupId, String tenantId) {
        StringBuilder url = new StringBuilder();
        url.append(RestUtils.getIdentityServiceBaseUrl(tenantId));
        url.append("/groups/");
        url.append(groupId);
        url.append("/members");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }


    protected VOUserDetails createUserDetails(String response, String userId) {
        Gson gson = new Gson();
        UserinfoModel userInfoModel = gson.fromJson(response,
                UserinfoModel.class);
        return mapUserInfoToUserDetails(userInfoModel, userId);
    }

    protected VOUserDetails mapUserInfoToUserDetails(
            UserinfoModel userInfoModel, String userId) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId(userId);
        userDetails.setFirstName(userInfoModel.getFirstName());
        userDetails.setLastName(userInfoModel.getLastName());
        if (userInfoModel.getEmail() != null
                && !userInfoModel.getEmail().isEmpty()
                && !userInfoModel.getEmail().equalsIgnoreCase("null")) {
            userDetails.setEMail(userInfoModel.getEmail());
        } else {
            userDetails.setEMail(userId);
        }
        userDetails.setPhone(userInfoModel.getPhone());
        userDetails.setLocale("en"); // use en as default language here
        userDetails.setSalutation(
                mapGenderToSalutation(userInfoModel.getGender()));
        userDetails.setAddress(userInfoModel.getAddress());
        return userDetails;
    }

    protected UserinfoModel mapUserDetailsToUserInfo(
            VOUserDetails userDetails) {
        UserinfoModel userInfo = new UserinfoModel();
        userInfo.setUserId(userDetails.getUserId());
        userInfo.setFirstName(userDetails.getFirstName());
        userInfo.setLastName(userDetails.getLastName());

        userInfo.setEmail(userDetails.getEMail());
        userInfo.setPhone(userDetails.getPhone());
        userInfo.setLocale("en"); // use en as default language here
        userInfo.setAddress(userDetails.getAddress());
        return userInfo;
    }

    protected Salutation mapGenderToSalutation(String gender) {
        if (gender == null) {
            return Salutation.MS;
        }
        switch (gender) {
        case "male":
            return Salutation.MR;
        case "female":
            return Salutation.MS;
        case "?":
            return Salutation.MS;
        default:
            logger.logDebug("No gender defined or the gender " + gender
                    + " is unknown");
            return Salutation.MS;
        }
    }

    protected HttpURLConnection createConnection(URL url, String tokenId)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + tokenId);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.connect();
        logger.logDebug("Connection to identity service successful");
        return conn;
    }

    protected String createUrlForUserinfo(String userId, String tenantId) {
        StringBuilder url = new StringBuilder();
        url.append(RestUtils.getIdentityServiceBaseUrl(tenantId));
        url.append("/users/");
        url.append(userId);
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }
}
