/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.ejb.ScheduleExpression;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMPropertyHandler.SubscriptionEndStatus;
import org.oscm.app.vmware.encryption.AESEncrypter;
import org.oscm.app.vmware.remote.bes.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides data access to the vmware controller database.
 */
public class APPDataAccessService {

    private static final Logger logger = LoggerFactory
            .getLogger(APPDataAccessService.class);
    private static final String DATASOURCE = "BSSAppDS";
    private static final String CRYTO_PREFIX = "_crypt:";

    private DataSource ds = null;

    public void setSubscriptionEndStatus(String instanceId,
            SubscriptionEndStatus status) throws Exception {

        logger.debug("instanceId: " + instanceId + " status: " + status.name());
        String query = "UPDATE instanceparameter SET parametervalue = ? WHERE parameterkey = '"
                + VMPropertyHandler.SUBSCRIPTION_END_STATUS
                + "' AND serviceinstance_tkey = (SELECT tkey from serviceinstance WHERE instanceid = ?)";

        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, status.name());
            stmt.setString(2, instanceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to set subscription end state for instanceId "
                    + instanceId, e);
            throw e;
        }
    }

    public String getBSSWebServiceURL() throws Exception {
        String bssWebserviceURL = null;
        String query = "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'BSS_WEBSERVICE_WSDL_URL' AND controllerid = 'PROXY'";

        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bssWebserviceURL = rs.getString("settingvalue");
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve BSS_WEBSERVICE_URL", e);
            throw e;
        }

        return bssWebserviceURL;
    }

    public List<String> getOrgIds() throws Exception {
        List<String> keys = new ArrayList<String>();
        List<String> orgIds = new ArrayList<String>();
        String query = "SELECT settingkey FROM configurationsetting WHERE settingkey like ? AND controllerid = '"
                + Controller.ID + "'";

        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, "USERID_%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                keys.add(rs.getString("settingkey"));
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve orgIds", e);
            throw e;
        }

        for (String key : keys) {
            orgIds.add(key.substring(7, key.length()));
        }

        return orgIds;
    }

    public ScheduleExpression getTimerSchedule(String defaultDay,
            String defaultHour, String defaultMinute) {
        logger.debug("");
        String queryMinute = "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'TIMER_SCHEDULE_MINUTE' AND controllerid = '"
                + Controller.ID + "'";
        String queryHour = "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'TIMER_SCHEDULE_HOUR' AND controllerid = '"
                + Controller.ID + "'";
        String queryDay = "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'TIMER_SCHEDULE_DAY' AND controllerid = '"
                + Controller.ID + "'";

        String day = null;
        String hour = null;
        String minute = null;

        try (Connection con = getDatasource().getConnection();) {
            try (PreparedStatement stmt = con.prepareStatement(queryMinute);) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    minute = rs.getString("settingvalue");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(queryHour);) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    hour = rs.getString("settingvalue");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement(queryDay);) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    day = rs.getString("settingvalue");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve schedule for timer", e);
        }

        if (minute == null) {
            minute = defaultMinute;
        }

        if (hour == null) {
            hour = defaultHour;
        }

        if (day == null) {
            day = defaultDay;
        }

        ScheduleExpression schedule = new ScheduleExpression();
        schedule.dayOfWeek(day).hour(hour).minute(minute);
        return schedule;
    }

    public Credentials getCredentials(String orgId) throws Exception {
        String userId = getUserId(orgId);
        long userKey = getUserKey(orgId);
        logger.debug("orgId: " + orgId + " userId: " + userId + " userKey: "
                + userKey);
        String password = getUserPwd(orgId);
        boolean isSSO = false;
        Credentials cred = new Credentials(isSSO, userId, password);
        cred.setUserKey(userKey);
        return cred;
    }

    private String getUserId(String orgId) throws Exception {
        String query = "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = '"
                + Controller.ID + "'";
        String userId = null;
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, "USERID_" + orgId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userId = rs.getString("settingvalue");
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve userId for orgId " + orgId, e);
            throw e;
        }

        if (userId == null) {
            throw new RuntimeException("Failed to retrieve userId for orgId "
                    + orgId);
        }

        return userId;
    }

    private long getUserKey(String orgId) throws Exception {
        String query = "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = '"
                + Controller.ID + "'";
        long userKey = -1;
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, "USERKEY_" + orgId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userKey = Long.parseLong(rs.getString("settingvalue"));
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve userKey for orgId " + orgId, e);
            throw e;
        }

        if (userKey == -1) {
            throw new RuntimeException("Failed to retrieve userKey for orgId "
                    + orgId);
        }

        return userKey;
    }

    private String getUserPwd(String orgId) throws Exception {
        String query = "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = '"
                + Controller.ID + "'";
        String userPwd = null;
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, "USERPWD_" + orgId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userPwd = rs.getString("settingvalue");
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve password for orgId " + orgId, e);
            throw e;
        }

        if (userPwd == null) {
            throw new RuntimeException("Failed to retrieve password for orgId "
                    + orgId);
        }

        userPwd = decryptPassword(userPwd, "USERPWD_" + orgId);
        return userPwd;
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
            decryptedPassword = AESEncrypter.decrypt(password);
        }

        return decryptedPassword;
    }

    private void encryptPasswordInDatabase(String password, String settingValue)
            throws Exception {
        String query = "UPDATE configurationsetting SET settingvalue = ? WHERE settingkey = ? AND controllerid = '"
                + Controller.ID + "'";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, AESEncrypter.encrypt(password));
            stmt.setString(2, settingValue);
            stmt.executeUpdate();
        }
    }

    public Credentials loadTechnologyProviderCredentials() throws Exception {
        boolean isSSO = false;
        Credentials credentials = new Credentials(isSSO);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_KEY' AND controllerid='"
                + Controller.ID + "') as key,");
        sql.append("(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_ID' AND controllerid='"
                + Controller.ID + "') as id,");
        sql.append("(SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey='BSS_USER_PWD' AND controllerid='"
                + Controller.ID + "') as password");
        sql.append(";");

        try (Connection connection = getDatasource().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql
                        .toString())) {

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                credentials.setUserKey(Long.valueOf(resultSet.getString("key"))
                        .longValue());
                credentials.setUserId(resultSet.getString("id"));
                credentials.setPassword(resultSet.getString("password"));
            }
        }

        credentials.setPassword(decryptPassword(credentials.getPassword(),
                "BSS_USER_PWD"));

        logger.debug("loaded technology provider credentials for user "
                + credentials.getUserId() + "(" + credentials.getUserKey());
        return credentials;
    }

    public HashMap<String, String> getControllerSettings() throws Exception {
        HashMap<String, String> settings = new HashMap<String, String>();
        String query = "SELECT settingkey,settingvalue FROM configurationsetting WHERE controllerid = '"
                + Controller.ID + "'";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                settings.put(rs.getString("settingkey"),
                        rs.getString("settingvalue"));
            }
        } catch (SQLException e) {
            logger.error(
                    "Failed to retrieve controller settings for controller "
                            + Controller.ID, e);
            throw e;
        }

        if (settings.size() == 0) {
            throw new RuntimeException(
                    "Failed to retrieve controller settings for controller "
                            + Controller.ID);
        }
        logger.debug("number of settings: " + settings.size());
        return settings;
    }

    public String getOrgId(String instanceId) throws Exception {
        logger.debug("instanceId: " + instanceId);
        String organizationid = null;
        String query = "SELECT organizationid FROM serviceinstance WHERE instanceid = ?";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, instanceId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                organizationid = rs.getString("organizationid");
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve organizationId for instanceId "
                    + instanceId, e);
            throw e;
        }

        if (organizationid == null) {
            throw new RuntimeException(
                    "Failed to retrieve organizationId for instanceId "
                            + instanceId);
        }

        logger.debug("organizationid: " + organizationid);
        return organizationid;
    }

    protected DataSource getDatasource() throws Exception {
        if (ds == null) {
            try {
                final Properties ctxProperties = new Properties();
                ctxProperties.putAll(System.getProperties());
                Context namingContext = getNamingContext(ctxProperties);
                ds = (DataSource) namingContext.lookup(DATASOURCE);
            } catch (Exception e) {
                throw new Exception("Datasource " + DATASOURCE + " not found.",
                        e);
            }
        }
        return ds;
    }

    protected Context getNamingContext(Properties ctxProperties)
            throws Exception {
        return new InitialContext(ctxProperties);
    }
}
