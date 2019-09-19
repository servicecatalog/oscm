/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: Aug 9, 2019
 *
 *******************************************************************************/
package org.oscm.identityservice.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.oscm.identityservice.model.AccessGroupModel;
import org.oscm.identityservice.model.UserinfoModel;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.google.gson.Gson;

public class AccessGroup {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AccessGroup.class);

    public String createGroup(String tenantId, String token, String groupName) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(createUrlForGroups(tenantId));
            conn = createConnection(url, token);
            AccessGroupModel group = getAccessGroupModel(groupName, tenantId);
            String json = pojoToJsonString(group);
            setOutputStream(conn, json);
            conn.connect();

            if (!RestUtils.isResponseSuccessful(conn.getResponseCode())) {
                throwRegistrationException(conn);
            }
            String response = RestUtils.getResponse(conn.getInputStream());
            conn.disconnect();

            group = createAccessGroupModelFromJson(response);
            return group.getId();
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
            throw e;
        } finally {
            conn.disconnect();
        }
    }

    public void addMemberToGroup(String groupId, String tenantId, String token,
            VOUserDetails userDetails) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(createUrlToAddUser(tenantId, groupId));
            conn = createConnection(url, token);

            Userinfo userinfo = new Userinfo();
            UserinfoModel userInfo = userinfo
                    .mapUserDetailsToUserInfo(userDetails);
            String json = pojoToJsonString(userInfo);
            setOutputStream(conn, json);
            conn.connect();

            if (!RestUtils.isResponseSuccessful(conn.getResponseCode())) {
                throwRegistrationException(conn);
            }
            conn.disconnect();
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
            throw e;
        } finally {
            conn.disconnect();
        }
    }
    
    private void throwRegistrationException(HttpURLConnection conn)
            throws IOException, RegistrationException {
        logger.logInfo(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED,
                "response code from identity service was "
                        + conn.getResponseCode());
        throw new RegistrationException(
                "response code from identity service was "
                        + conn.getResponseCode());
    }

    protected HttpURLConnection createConnection(URL url, String tokenId)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + tokenId);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        logger.logDebug("Connection to identity service successful");
        return conn;
    }

    protected String createUrlForGroups(String tenantId) {
        StringBuilder url = new StringBuilder();
        url.append(RestUtils.getIdentityServiceBaseUrl(tenantId));
        url.append("/groups");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }

    protected String createUrlToAddUser(String tenantId, String groupId) {
        StringBuilder url = new StringBuilder();
        url.append(RestUtils.getIdentityServiceBaseUrl(tenantId));
        url.append("/groups");
        url.append("/");
        url.append(groupId);
        url.append("/");
        url.append("members");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }

    protected AccessGroupModel getAccessGroupModel(String groupName,
            String description) {
        AccessGroupModel group = new AccessGroupModel();
        if (groupName == null || groupName.isEmpty()) {
            group.setName("OSCM_default");
        } else {
            group.setName("OSCM_" + groupName);
        }
        group.setDescription("Used tenant is:" + description);
        return group;
    }

    protected String pojoToJsonString(Object object) {
        return new Gson().toJson(object);
    }

    private void setOutputStream(HttpURLConnection con, String jsonInputString)
            throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_IO_VALIDITY_EXTERNAL_JSON);
            throw e;
        }
    }

    protected AccessGroupModel createAccessGroupModelFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, AccessGroupModel.class);
    }

}
