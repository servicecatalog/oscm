/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.06.2018                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.usage;

import static org.oscm.app.openstack.controller.PropertyHandler.LAST_USAGE_FETCH;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.oscm.app.openstack.controller.OpenStackController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the APP database.
 */
public class AppDb {

    private static final Logger LOG = LoggerFactory.getLogger(AppDb.class);
    private static final String DATASOURCE = "BSSAppDS";
    private static final String CRYTO_PREFIX = "_crypt:";

    private DataSource ds = null;

    private DataSource getDatasource() throws Exception {
        if (ds != null) {
            return ds;
        }

        try {
            final Properties ctxProperties = new Properties();
            ctxProperties.putAll(System.getProperties());
            Context namingContext = new InitialContext(ctxProperties);
            return ds = (DataSource) namingContext.lookup(DATASOURCE);
        } catch (Exception e) {
            throw new Exception("Datasource " + DATASOURCE + " not found.", e);
        }
    }

    public Credentials loadTechnologyProviderCredentials() throws Exception {
        boolean isSSO = false;
        Credentials credentials = new Credentials(isSSO);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(
                "(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_KEY' AND controllerid='"
                        + OpenStackController.ID + "') as key,");
        sql.append(
                "(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_ID' AND controllerid='"
                        + OpenStackController.ID + "') as id,");
        sql.append(
                "(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_PWD' AND controllerid='"
                        + OpenStackController.ID + "') as password");
        sql.append(";");

        try (Connection connection = getDatasource().getConnection();
                PreparedStatement stmt = connection
                        .prepareStatement(sql.toString())) {

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                credentials.setUserKey(
                        Long.valueOf(resultSet.getString("key")).longValue());
                credentials.setUserId(resultSet.getString("id"));
                credentials.setPassword(resultSet.getString("password"));
            }
        }

        credentials.setPassword(
                decryptPassword(credentials.getPassword(), "BSS_USER_PWD"));

        LOG.debug("loaded technology provider credentials for user "
                + credentials.getUserId() + "(" + credentials.getUserKey()
                + "), " + "****");
        return credentials;
    }

    /**
     * Decrypts a password. If the password is not encrypted it will we then
     * encrypted in the database.
     */
    private String decryptPassword(String password, String settingValue)
            throws Exception {
        String decryptedPassword = null;

        if (password.startsWith(CRYTO_PREFIX)) {
            decryptedPassword = password.substring(password.indexOf(":") + 1,
                    password.length());
            encryptPasswordInDatabase(decryptedPassword, settingValue);
        } else {
            decryptedPassword = AesEncrypter.decrypt(password);
        }

        return decryptedPassword;
    }

    private void encryptPasswordInDatabase(String password, String settingValue)
            throws Exception {
        String query = "UPDATE configurationsetting SET settingvalue = ? WHERE settingkey = ? AND controllerid = '"
                + OpenStackController.ID + "'";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, AesEncrypter.encrypt(password));
            stmt.setString(2, settingValue);
            stmt.executeUpdate();
        }
    }

    public HashMap<String, String> getControllerSettings() throws Exception {
        HashMap<String, String> settings = new HashMap<String, String>();
        String query = "SELECT settingkey,settingvalue FROM configurationsetting WHERE controllerid = '"
                + OpenStackController.ID + "'";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                settings.put(rs.getString("settingkey"),
                        rs.getString("settingvalue"));
            }
        } catch (SQLException e) {
            LOG.error("Failed to retrieve controller settings for controller "
                    + OpenStackController.ID, e);
            throw e;
        }

        if (settings.size() == 0) {
            throw new RuntimeException(
                    "Failed to retrieve controller settings for controller "
                            + OpenStackController.ID);
        }
        return settings;
    }

    public long loadRequestTime(String instanceId) throws Exception {
        String sql = "SELECT requesttime FROM bssappuser.serviceinstance WHERE instanceid = '%s'";
        sql = String.format(sql, instanceId);
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong("requesttime");
        }
    }

    public void updateLastUsageFetch(String instanceId, String endTime)
            throws SQLException, Exception {

        String sql = "UPDATE bssappuser.instanceparameter SET parametervalue='%s' WHERE parameterkey='%s' AND serviceinstance_tkey=(SELECT tkey FROM serviceinstance WHERE instanceid='%s')";
        sql = String.format(sql, endTime, LAST_USAGE_FETCH, instanceId);
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(sql);) {
            stmt.executeUpdate();
        }
    }

}
