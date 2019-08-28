package org.oscm.identityservice.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RestUtils {

    public static String getResponse(InputStream input) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            content.append(inputLine);
        }
        br.close();
        return content.toString();
    }

    public static String getIdentityServiceBaseUrl(String tenantId) {
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
        return url.toString();
    }
}
