/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: stavreva                                                
 *                                                                              
 *  Creation Date: 12.06.2013                                                      
 *                                                                              
 *  Completion Time: 12.06.2013                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * SQL query to read the authentication settings from "configurationsetting"
 * table.
 * 
 * @author stavreva
 * 
 */
class AuthenticationModeQuery extends AbstractQuery {

    private String authMode;
    private String recipient;
    private String recipientHttps;
    private String idpTruststore;
    private String idpTruststorePasswd;

    AuthenticationModeQuery(DataSource ds) {
        super(ds);
        authMode = "";
        recipient = "";
        recipientHttps = "";
        idpTruststore = "";
        idpTruststorePasswd = "";
    }

    @Override
    protected String getStatement() {
        String authMode = "c.information_id='AUTH_MODE'";
        String baseUrl = "c.information_id='BASE_URL'";
        String baseUrlHttps = "c.information_id='BASE_URL_HTTPS'";
        String idpTruststore = "c.information_id='SSO_IDP_TRUSTSTORE'";
        String idpTruststorePassw = "c.information_id='SSO_IDP_TRUSTSTORE_PASSWORD'";
        return "SELECT c.information_id, c.env_value FROM ConfigurationSetting c WHERE "
                + authMode //
                + " OR " //
                + baseUrl //
                + " OR "//
                + baseUrlHttps //
                + " OR "//
                + idpTruststore //
                + " OR "//
                + idpTruststorePassw + ";";
    }

    @Override
    protected void mapResult(ResultSet rs) throws SQLException {
        do {
            if ("AUTH_MODE".equals(rs.getString(1))) {
                authMode = rs.getString(2);
            } else if ("SSO_IDP_TRUSTSTORE".equals(rs.getString(1))) {
                idpTruststore = replaceNullValue(rs.getString(2));
            } else if ("SSO_IDP_TRUSTSTORE_PASSWORD".equals(rs.getString(1))) {
                idpTruststorePasswd = replaceNullValue(rs.getString(2));
            } else if ("BASE_URL".equals(rs.getString(1))) {
                recipient = replaceNullValue(rs.getString(2));
                recipient += recipient.endsWith("/") ? "" : "/";
            } else if ("BASE_URL_HTTPS".equals(rs.getString(1))) {
                recipientHttps = rs.getString(2);
                recipientHttps += recipientHttps.endsWith("/") ? "" : "/";
            }
        } while (rs.next());
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
    }

    public String getAuthenticationMode() {
        return authMode;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRecipientHttps() {
        return recipientHttps;
    }

    public String getIDPTruststore() {
        return idpTruststore;
    }

    public String getIDPTruststorePassword() {
        return idpTruststorePasswd;
    }

    private String replaceNullValue(String value) {
        return value == null ? "" : value;
    }
}
