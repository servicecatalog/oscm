/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.propertyimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.oscm.converter.PropertiesLoader;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.MandatoryAttributesInSamlSP;

public class V2_9_14__PropertyImport implements JdbcMigration {

    private static final String TABLE_NAME = "ConfigurationSetting";
    private static final String FIELD_TKEY = "tkey";
    private static final String FIELD_KEY = "information_id";
    private static final String FIELD_VALUE = "env_value";
    private static final String FIELD_CONTEXT = "context_id";
    private static final String FIELD_VERSION = "version";

    private int count = 0;

    private String driverURL;
    private String userName;
    private String userPwd;
    private String propertyFile;
    private boolean overwriteFlag;
    private String contextId = "global";


    private void verifyValueValid(ConfigurationKey key, String value,
            boolean isSamlSP) {
        if (isNullValue(value)
                && (key.isMandatory() || isMandatoryAttributeInSamlSPMode(
                        isSamlSP, key.getKeyName()))) {
            if (key == ConfigurationKey.AUDIT_LOG_ENABLED
                    || key == ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED
                    || key == ConfigurationKey.MAX_NUMBER_ALLOWED_USERS
                    || key == ConfigurationKey.TIMER_INTERVAL_USER_COUNT) {
                value = key.getFallBackValue();
            } else {
                throw new RuntimeException("Mandatory attribute "
                        + key.getKeyName() + " can not be set a null value");
            }
        }
    }

    private boolean isMandatoryAttributeInSamlSPMode(boolean isSamlSP,
            String keyName) {
        return isSamlSP && verifyMandatoryInSamlSP(keyName);
    }

    private boolean isNullValue(String value) {
        return (value == null || value.isEmpty());
    }

    private boolean isSamlSPMode(Properties p) {
        String authMode = (String) p
                .get(ConfigurationKey.AUTH_MODE.getKeyName());

        if (!isNullValue(authMode)
                && authMode.equals(AuthenticationMode.SAML_SP.name())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean verifyMandatoryInSamlSP(String keyName) {
        boolean isMandatory = false;
        MandatoryAttributesInSamlSP[] attrs = MandatoryAttributesInSamlSP
                .values();
        for (MandatoryAttributesInSamlSP attr : attrs) {
            if (attr.name().equals(keyName)) {
                isMandatory = true;
                break;
            }
        }
        return isMandatory;
    }

    private void verifyAuthMode(String keyName, String value) {
        boolean isContained = false;
        if (ConfigurationKey.AUTH_MODE.name().equals(keyName)) {
            AuthenticationMode[] modes = AuthenticationMode.values();
            for (AuthenticationMode mode : modes) {
                if (mode.name().equals(value)) {
                    isContained = true;
                    break;
                }
            }
            if (!isContained) {
                throw new RuntimeException(
                        "Authentication mode has an invalid value - Allowed values are [INTERNAL, SAML_SP, SAML_IDP, OPENID_RP]");
            }
        }
    }

    private void verifyConfigurationValue(ConfigurationKey key, String value) {
        ConfigurationSettingsValidator.validate(key, value);
    }

    private void initStartCount(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT MAX(TKEY) FROM " + TABLE_NAME);
            while (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Could not determine max id value in use.");
        }

    }

    private Map<String, String> loadConfigurationSettings(Connection conn) {

        Map<String, String> settings = new HashMap<>();
        try {
            String query = "SELECT " + FIELD_KEY + ", " + FIELD_VALUE + " FROM "
                    + TABLE_NAME + " WHERE " + FIELD_CONTEXT + " = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, contextId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                settings.put(rs.getString(1), rs.getString(2));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load configuration settings",
                    e);
        }
        return settings;
    }

    private void createEntries(Connection conn, Map<String, String> toCreate) {

        try {
            String query = "INSERT INTO " + TABLE_NAME + " (" + FIELD_VALUE
                    + ", " + FIELD_KEY + ", " + FIELD_CONTEXT + ", "
                    + FIELD_TKEY + ", " + FIELD_VERSION
                    + ") VALUES(?, ?, ?, ?, 0)";
            PreparedStatement stmt = conn.prepareStatement(query);

            for (String key : toCreate.keySet()) {
                count++;

                String value = toCreate.get(key);
                stmt.setString(1, value);
                stmt.setString(2, key);
                stmt.setString(3, contextId);
                stmt.setLong(4, count);

                stmt.executeUpdate();
                System.out.println("Create Configuration " + key
                        + " with value '" + value + "'");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Unable to insert new configuration key");
        }
    }

    private void updateEntries(Connection conn, Map<String, String> toUpdate) {

        try {
            String query = "UPDATE " + TABLE_NAME + " SET " + FIELD_VALUE
                    + " = ? WHERE " + FIELD_KEY + " = ? AND " + FIELD_CONTEXT
                    + " = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            for (String key : toUpdate.keySet()) {
                count++;

                String value = toUpdate.get(key);
                stmt.setString(1, value);
                stmt.setString(2, key);
                stmt.setString(3, contextId);

                stmt.executeUpdate();
                System.out.println("Update Configuration " + key
                        + " with value '" + value + "'");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Unable to update new configuration key");
        }
    }

    private void deleteEntries(Connection conn, Map<String, String> settings) {
        try {
            String query = "DELETE FROM " + TABLE_NAME + " WHERE " + FIELD_KEY
                    + " = ? AND " + FIELD_CONTEXT + " = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            for (String key : settings.keySet()) {

                stmt.setString(1, key);
                stmt.setString(2, contextId);

                stmt.executeUpdate();
                System.out.println("Delete Configuration " + key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Unable to delete new configuration key");
        }
    }

    // protected to enable mocking in unit tests
    protected Connection getConnetion() throws SQLException {
        return DriverManager.getConnection(driverURL, userName, userPwd);
    }

    @Override
    public void migrate(Connection connection) throws Exception {

        InetAddress addr;
        addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        InputStream in = this.getClass().getResourceAsStream("/" + hostname + "/conf.properties");



        if (in == null) {
            // Check if maven was build with configsettings.path parameter set (external configsettings.properties file)
            in = this.getClass().getResourceAsStream("/paths.properties");
            Properties paths = PropertiesLoader.loadProperties(in);
            String configSettingsPath = paths.getProperty("configsettings.path");
            if (new File(configSettingsPath).exists()) {
                in = new FileInputStream(configSettingsPath);
            } else {
                in = this.getClass().getResourceAsStream("/travis/conf.properties");
            }
        }

        final Properties p = PropertiesLoader.loadProperties(in);

        try {
            Map<String, String> settings = loadConfigurationSettings(connection);
            Map<String, String> toCreate = new HashMap<>();
            Map<String, String> toUpdate = new HashMap<>();

            initStartCount(connection);
            ConfigurationKey[] allKeys = ConfigurationKey.values();
            boolean isSamlSP = isSamlSPMode(p);

            for (ConfigurationKey key : allKeys) {
                String keyName = key.getKeyName();
                String value = (String) p.get(keyName);

                verifyValueValid(key, value, isSamlSP);

                if (value == null || value.isEmpty()) {
                    if (key.getFallBackValue() != null) {
                        value = key.getFallBackValue();
                    } else {
                        value = "";
                    }
                }

                if (key.getType() == ConfigurationKey.TYPE_BOOLEAN
                    || key.getType() == ConfigurationKey.TYPE_LONG
                    || key.getType() == ConfigurationKey.TYPE_URL
                    || key.getType() == ConfigurationKey.TYPE_STRING
                    || key.getType() == ConfigurationKey.TYPE_PASSWORD) {

                    value = value.trim();
                }

                verifyAuthMode(keyName, value);
                verifyConfigurationValue(key, value);

                if (settings.containsKey(keyName)) {
                    toUpdate.put(keyName, value);
                } else {
                    toCreate.put(keyName, value);
                }
                settings.remove(keyName);
            }

            createEntries(connection, toCreate);
            if (overwriteFlag) {
                updateEntries(connection, toUpdate);
            } else {
                for (String key : toUpdate.keySet()) {
                    System.out.println(
                        "Existing Configuration " + key + " skipped");
                }
            }
            deleteEntries(connection, settings);
        } finally {

        }
    }
}
