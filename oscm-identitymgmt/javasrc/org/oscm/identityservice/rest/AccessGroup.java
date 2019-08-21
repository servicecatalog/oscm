/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: Aug 9, 2019
 *
 *******************************************************************************/
package org.oscm.identityservice.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.oscm.identityservice.model.AccesGroupModel;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.google.gson.Gson;

public class AccessGroup {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AccessGroup.class);

    public void createGroup(String tenantId, String token) throws Exception {
        String response = "";
        try {
            URL url = new URL(createUrl(tenantId));
            HttpURLConnection conn = createConnection(url, token);
            AccesGroupModel group = getAccessGroupModel(tenantId, tenantId);
            String json = PojoToJsonString(group);
            setOutputStream(conn, json);
            conn.connect();

            if (conn.getResponseCode() != 200) {
                logger.logInfo(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED,
                        "response code from identity service was "
                                + conn.getResponseCode());
                throw new RuntimeException(
                        "response code from identity service was "
                                + conn.getResponseCode());
            }
            response = getResponse(conn.getInputStream());
            conn.disconnect();
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
            throw e;
        }
    }

    public void addMemberToGroup(String userId, String tenantId, String token) {

    }

    protected HttpURLConnection createConnection(URL url, String tokenId)
            throws IOException, ProtocolException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + tokenId);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        logger.logDebug("Connection to identity service successfull");
        return conn;
    }

    protected String createUrl(String tenantId) {
        StringBuilder url = new StringBuilder();
        url.append("http://oscm-identity");
        url.append(":");
        url.append("9090");
        url.append("/");
        url.append("oscm-identity");
        url.append("/");
        url.append("tenants/");
        if (tenantId == null || tenantId.isEmpty()) {
            url.append("default");
        } else {
            url.append(tenantId);
        }
        url.append("/groups");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }

    protected String createUrlToAddUser(String tenantId, String groupId) {
        StringBuilder url = new StringBuilder();
        url.append(createUrl(tenantId));
        url.append("/");
        url.append(groupId);
        url.append("/");
        url.append("members");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }

    private String getResponse(InputStream input) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            content.append(inputLine);
        }
        br.close();
        return content.toString();
    }

    private AccesGroupModel getAccessGroupModel(String tenantId,
            String description) {
        AccesGroupModel group = new AccesGroupModel();
        if (tenantId == null || tenantId.isEmpty()) {
            group.setName("default");
        } else {
            group.setName(tenantId);
        }
        group.setDescription(description);
        return group;
    }

    private String PojoToJsonString(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }

    private void setOutputStream(HttpURLConnection con, String jsonInputString)
            throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

}
