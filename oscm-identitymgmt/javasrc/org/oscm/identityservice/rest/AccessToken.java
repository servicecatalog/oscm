package org.oscm.identityservice.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.oscm.identityservice.model.AccessTokenModel;
import org.oscm.identityservice.model.UserinfoModel;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import com.google.gson.Gson;

public class AccessToken {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AccessToken.class);

    public String getOidcToken(String tenantId) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(createUrlForAccessToken(tenantId));
            conn = createConnection(url);
            conn.connect();

            if (!RestUtils.isResponseSuccessful(conn.getResponseCode())) {
                throwRegistrationException(conn);
            }
            String response = RestUtils.getResponse(conn.getInputStream());
            String token = getAccessTokenFromModel(response);
            conn.disconnect();

            return token;
        } catch (Exception e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED);
        } finally {
            conn.disconnect();
        }
        return "";
    }

    protected String createUrlForAccessToken(String tenantId) {
        StringBuilder url = new StringBuilder();
        url.append(RestUtils.getIdentityServiceBaseUrl(tenantId));
        url.append("/token");
        logger.logDebug(
                "Connection Url for identity service call = " + url.toString());
        return url.toString();
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        logger.logDebug("Connection to identity service successful");
        return conn;
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

    protected String getAccessTokenFromModel(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, AccessTokenModel.class).getAccessToken();
    }
}
