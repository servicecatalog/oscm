package org.oscm.identityservice.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.identityservice.model.UserinfoModel;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.google.gson.Gson;

public class Userinfo {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(Userinfo.class);

    public VOUserDetails getUserinfoFromIdentityService(String userId,
            String token) throws Exception {

        String response = "";
        String tenantId = "";
        try {
            URL url = new URL(createUrl(userId, tenantId, token));
            HttpURLConnection conn = createConnection(url);

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
        VOUserDetails details = createUserDetails(response);
        return details;
    }

    protected VOUserDetails createUserDetails(String response) {
        Gson gson = new Gson();
        UserinfoModel userInfoModel = gson.fromJson(response,
                UserinfoModel.class);
        VOUserDetails details = mapUserInfoToUserDetails(userInfoModel);
        return details;
    }

    protected VOUserDetails mapUserInfoToUserDetails(
            UserinfoModel userInfoModel) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName(userInfoModel.getFirstName());
        userDetails.setLastName(userInfoModel.getLastName());
        userDetails.setEMail(userInfoModel.getEmail());
        userDetails.setPhone(userInfoModel.getPhone());
        userDetails.setLocale("en"); // use en as default language here
        userDetails.setSalutation(
                mapGenderToSalutation(userInfoModel.getGender()));
        userDetails.setAddress(userInfoModel.getAddress());
        return userDetails;
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
                    + " is unkonwn");
            return Salutation.MS;
        }
    }

    protected HttpURLConnection createConnection(URL url)
            throws IOException, ProtocolException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();
        logger.logDebug("Connection to identity service successfull");
        return conn;
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

    protected String createUrl(String userId, String tenantId, String token) {
        StringBuilder url = new StringBuilder();
        url.append("http://oscm-identity");
        url.append(":");
        url.append("9090");
        url.append("/");
        url.append("oscm-identity");
        url.append("/");
        url.append("users/");
        url.append(userId);
        url.append("?tenantId=");
        if (tenantId == null || tenantId.isEmpty()) {
            url.append("default");
        } else {
            url.append(tenantId);
        }
        url.append("&token=");
        url.append(token);
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();

    }
}
