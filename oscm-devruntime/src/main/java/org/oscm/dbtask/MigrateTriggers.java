/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.11.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.beans.XMLDecoder;
import java.io.Closeable;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.stream.Streams;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Database migration to create trigger process identifiers out of serialized
 * trigger process parameters.
 */
public class MigrateTriggers extends DatabaseUpgradeTask {

    private static final String TEMP_TABLE_NAME = "triggerprocessidentifier_temp";

    private static final String QUERY_PENDING_TRIGGER_FOR_TYPE = "SELECT * FROM triggerdefinition td WHERE type = ? AND suspendprocess = true ORDER BY td.tkey ASC";
    private static final String QUERY_INSERT_TPI = "INSERT INTO "
            + TEMP_TABLE_NAME
            + " (version, triggerprocess_tkey, name, value) VALUES (0, ?, ?, ?)";
    private static final String QUERY_TRIGGER_PROCESS_DETAILS = "SELECT tp.tkey AS processkey FROM triggerprocess tp WHERE tp.triggerdefinition_tkey = ? ORDER BY tp.tkey ASC";
    private static final String QUERY_TRIGGER_PROCESS_PARAMETERS_DETAILS = "SELECT tpp.name AS name, tpp.serializedvalue AS value FROM triggerprocessparameter tpp WHERE tpp.triggerprocess_tkey = ?";
    private static final String QUERY_COPY_ENTRIES = "INSERT INTO triggerprocessidentifier(tkey, version, triggerprocess_tkey, name, value) SELECT tkey, version, triggerprocess_tkey, name, value FROM "
            + TEMP_TABLE_NAME;
    private static final String QUERY_CREATE_TEMP_TABLE = "CREATE TABLE "
            + TEMP_TABLE_NAME
            + " (tkey SERIAL, version INT NOT NULL, triggerprocess_tkey BIGINT NOT NULL, name varchar(255) NOT NULL, value TEXT NOT NULL)";
    private static final String QUERY_REMOVE_TEMP_ENTRIES = "DELETE FROM "
            + TEMP_TABLE_NAME;
    private static final String QUERY_DROP_TEMP_TABLE = "DROP TABLE "
            + TEMP_TABLE_NAME;
    private static final String QUERY_UPDATE_HIBERNATE_SEQUENCES = "UPDATE hibernate_sequences SET sequence_next_hi_value = (SELECT COALESCE((MAX(tkey)/1000),0)+10 FROM triggerprocessidentifier) where sequence_name = 'TriggerProcessIdentifier'";

    private Connection conn;

    @Override
    public void execute() throws Exception {
        conn = getConnection();
        createTempTable();
        handlePendingTriggers();
        copyToLiveTable();
        updateHibernateSequences();
        dropTempTable();
    }

    /**
     * Determines all triggers that are defined as suspending and processes
     * their process parameters.
     */
    private void handlePendingTriggers() throws Exception {
        for (TriggerType type : TriggerType.values()) {
            PreparedStatement pstmt = conn
                    .prepareStatement(QUERY_PENDING_TRIGGER_FOR_TYPE);
            pstmt.setString(1, type.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long orgKey = rs.getLong("organization_tkey");
                long triggerDefinitionKey = rs.getLong("tkey");
                switch (type) {
                case ACTIVATE_SERVICE:
                case DEACTIVATE_SERVICE:
                    handleServiceActivation(orgKey, triggerDefinitionKey);
                    break;
                case SUBSCRIBE_TO_SERVICE:
                case UNSUBSCRIBE_FROM_SERVICE:
                    handleSubscribingProcess(orgKey, triggerDefinitionKey);
                    break;
                case MODIFY_SUBSCRIPTION:
                case UPGRADE_SUBSCRIPTION:
                    handleSubscriptionChange(orgKey, triggerDefinitionKey);
                    break;
                case SAVE_PAYMENT_CONFIGURATION:
                    handleSavePayment(orgKey, triggerDefinitionKey);
                    break;
                case REGISTER_CUSTOMER_FOR_SUPPLIER:
                    handleCustomerRegistration(orgKey, triggerDefinitionKey);
                    break;
                case ADD_REVOKE_USER:
                    handleUserAssignment(orgKey, triggerDefinitionKey);
                    break;
                default:
                    ;
                }
            }
        }
    }

    /**
     * Updates the hibernate sequences table according to the
     * 
     * @throws SQLException
     */
    private void updateHibernateSequences() throws SQLException {
        executeStatement(QUERY_UPDATE_HIBERNATE_SEQUENCES);
    }

    /**
     * Copies the migrated data from the temporary table to the live table.
     * 
     * @throws SQLException
     */
    private void copyToLiveTable() throws SQLException {
        executeStatement(QUERY_COPY_ENTRIES);
    }

    /**
     * Drops the temporary table.
     * 
     * @throws SQLException
     */
    private void dropTempTable() throws SQLException {
        executeStatement(QUERY_DROP_TEMP_TABLE);
    }

    /**
     * Executes the provided query and closes the used resources.
     * 
     * @param query
     *            The query to execute.
     * @throws SQLException
     */
    private void executeStatement(String query) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to saving of a payment configuration.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleSavePayment(long orgKey, long triggerDefinitionKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            writeOrgKeyIdentifierForProcess(orgKey, processes);
        }
        holder.close();
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to customer registration.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleCustomerRegistration(long orgKey,
            long triggerDefinitionKey) throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            long triggerProcessKey = writeOrgKeyIdentifierForProcess(orgKey,
                    processes);
            writeRegistrationIdentifiers(triggerProcessKey);
        }
        holder.close();
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to user assignments.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleUserAssignment(long orgKey, long triggerDefinitionKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            long triggerProcessKey = writeOrgKeyIdentifierForProcess(orgKey,
                    processes);
            writeAssignmentIdentifiers(triggerProcessKey);
        }
        holder.close();
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to the subscribing or unsubscribing
     * process.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleSubscribingProcess(long orgKey, long triggerDefinitionKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            long triggerProcessKey = writeOrgKeyIdentifierForProcess(orgKey,
                    processes);
            writeSubscriptionIdentifier(triggerProcessKey);
        }
        holder.close();
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to subscription modification or
     * upgrade.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleSubscriptionChange(long orgKey, long triggerDefinitionKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            long triggerProcessKey = writeOrgKeyIdentifierForProcess(orgKey,
                    processes);
            writeSubscriptionKey(triggerProcessKey);
        }
        holder.close();
    }

    /**
     * Evaluates the trigger process data and creates the database entries
     * required for the processes related to service activation.
     * 
     * @param orgKey
     *            The organization key.
     * @param triggerDefinitionKey
     *            The key of the trigger definition the trigger processes have
     *            to be checked for.
     * @throws Exception
     */
    private void handleServiceActivation(long orgKey, long triggerDefinitionKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcesses(triggerDefinitionKey);
        ResultSet processes = holder.getRs();
        while (processes.next()) {
            long triggerProcessKey = writeOrgKeyIdentifierForProcess(orgKey,
                    processes);
            writeServiceKeyIdentifier(triggerProcessKey);
        }
        holder.close();
    }

    /**
     * Determines the service key corresponding to the current trigger process
     * and inserts an according entry in the database.
     * 
     * @param triggerProcessKey
     *            The key of the current trigger process.
     * @throws Exception
     *             Thrown in case no product parameter is found for the trigger
     *             process.
     */
    private void writeServiceKeyIdentifier(long triggerProcessKey)
            throws Exception {
        long serviceKey = determineServiceKey(triggerProcessKey);
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.SERVICE_KEY,
                String.valueOf(serviceKey));
    }

    /**
     * Determines the subscription identifier corresponding to the current
     * trigger process and inserts an according entry in the database.
     * 
     * @param triggerProcessKey
     *            The key of the current trigger process.
     * @throws Exception
     *             Thrown in case no product parameter is found for the trigger
     *             process.
     */
    private void writeSubscriptionIdentifier(long triggerProcessKey)
            throws Exception {
        String subId = determineSubscriptionId(triggerProcessKey);
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, subId);
    }

    /**
     * Determines the registration identifiers corresponding to the current
     * trigger process and inserts an according entry in the database.
     * 
     * @param triggerProcessKey
     *            The key of the current trigger process.
     * @throws Exception
     *             Thrown in case no product parameter is found for the trigger
     *             process.
     */
    private void writeRegistrationIdentifiers(long triggerProcessKey)
            throws Exception {
        RegistrationIdentifiers regIds = determineRegistrationIds(triggerProcessKey);
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.USER_ID, regIds.getUserId());
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.USER_EMAIL, regIds.getUserMail());
    }

    /**
     * Determines the user assignment identifiers corresponding to the current
     * trigger process and inserts an according entry in the database.
     * 
     * @param triggerProcessKey
     *            The key of the current trigger process.
     * @throws Exception
     *             Thrown in case no product parameter is found for the trigger
     *             process.
     */
    private void writeAssignmentIdentifiers(long triggerProcessKey)
            throws Exception {
        UserAssignmentIdentifiers userAssignmentIds = determineUserAssignmentIds(triggerProcessKey);
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                userAssignmentIds.getSubscriptionId());
        for (String userIdToAdd : userAssignmentIds.getUsersToAdd()) {
            createTriggerProcessIdentifier(triggerProcessKey,
                    TriggerProcessIdentifierName.USER_TO_ADD, userIdToAdd);
        }
        for (String userIdToRevoke : userAssignmentIds.getUsersToRevoke()) {
            createTriggerProcessIdentifier(triggerProcessKey,
                    TriggerProcessIdentifierName.USER_TO_REVOKE, userIdToRevoke);
        }
    }

    /**
     * Determines the subscription key corresponding to the current trigger
     * process and inserts an according entry in the database.
     * 
     * @param triggerProcessKey
     *            The key of the current trigger process.
     * @throws Exception
     *             Thrown in case no product parameter is found for the trigger
     *             process.
     */
    private void writeSubscriptionKey(long triggerProcessKey) throws Exception {
        String subKey = determineSubscriptionKey(triggerProcessKey);
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, subKey);
    }

    /**
     * Determines the key of the current trigger process and inserts a
     * corresponding trigger process identifier in the database containing the
     * organization's key.
     * 
     * @param orgKey
     *            The key of the organization to set.
     * @param processes
     *            The result set containing the trigger processes.
     * @return The key of the current trigger process the result set is
     *         positioned on.
     * @throws SQLException
     */
    private long writeOrgKeyIdentifierForProcess(long orgKey,
            ResultSet processes) throws SQLException {
        long triggerProcessKey = processes.getLong("processkey");
        triggerProcessKey = processes.getLong("processkey");
        createTriggerProcessIdentifier(triggerProcessKey,
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(orgKey));
        return triggerProcessKey;
    }

    /**
     * (determineRegistrationIds) Determines the subscription identifier for the
     * trigger process parameters of the current trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger process.
     * @return The subscription identifier.
     * @throws Exception
     */
    private String determineSubscriptionId(long triggerProcessKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcessParameters(triggerProcessKey);
        ResultSet triggerProcessParameters = holder.getRs();
        while (triggerProcessParameters.next()) {
            String parameterName = triggerProcessParameters.getString("name");
            if (TriggerProcessParameterName.SUBSCRIPTION.name().equals(
                    parameterName)) {
                Object entry = getObjectFromXML(triggerProcessParameters,
                        Object.class);
                // entry can be either of type VOSubscription or String
                holder.close();
                if (entry instanceof String) {
                    return (String) entry;
                } else if (entry instanceof VOSubscription) {
                    return ((VOSubscription) entry).getSubscriptionId();
                }
            }
        }
        throw new Exception(
                "Migration failed, no product entry found for trigger process!");
    }

    /**
     * Determines subscription key for the trigger process parameters of the
     * current trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger process.
     * @return The subscription key.
     * @throws Exception
     */
    private String determineSubscriptionKey(long triggerProcessKey)
            throws Exception {
        ResourceHolder holder = getTriggerProcessParameters(triggerProcessKey);
        ResultSet triggerProcessParameters = holder.getRs();
        while (triggerProcessParameters.next()) {
            String parameterName = triggerProcessParameters.getString("name");
            if (TriggerProcessParameterName.SUBSCRIPTION.name().equals(
                    parameterName)) {
                VOSubscription entry = getObjectFromXML(
                        triggerProcessParameters, VOSubscription.class);
                holder.close();
                return String.valueOf(entry.getKey());
            }
        }
        throw new Exception(
                "Migration failed, no product entry found for trigger process!");
    }

    /**
     * Determines the registration identifiers for the trigger process
     * parameters of the current trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger process.
     * @return The subscription key.
     * @throws Exception
     */
    private RegistrationIdentifiers determineRegistrationIds(
            long triggerProcessKey) throws Exception {
        ResourceHolder holder = getTriggerProcessParameters(triggerProcessKey);
        ResultSet triggerProcessParameters = holder.getRs();
        while (triggerProcessParameters.next()) {
            String parameterName = triggerProcessParameters.getString("name");
            if (TriggerProcessParameterName.USER.name().equals(parameterName)) {
                VOUserDetails entry = getObjectFromXML(
                        triggerProcessParameters, VOUserDetails.class);
                holder.close();
                return new RegistrationIdentifiers(entry.getUserId(),
                        entry.getEMail());
            }
        }
        throw new Exception(
                "Migration failed, no product entry found for trigger process!");
    }

    /**
     * Determines the user assignment identifiers for the trigger process
     * parameters of the current trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger process.
     * @return The subscription key.
     * @throws Exception
     */
    private UserAssignmentIdentifiers determineUserAssignmentIds(
            long triggerProcessKey) throws Exception {
        ResourceHolder holder = getTriggerProcessParameters(triggerProcessKey);
        ResultSet triggerProcessParameters = holder.getRs();
        UserAssignmentIdentifiers result = new UserAssignmentIdentifiers();
        while (triggerProcessParameters.next()) {
            String parameterName = triggerProcessParameters.getString("name");
            if (TriggerProcessParameterName.SUBSCRIPTION.name().equals(
                    parameterName)) {
                String subId = getObjectFromXML(triggerProcessParameters,
                        String.class);
                result.setSubscriptionId(subId);
            }
            if (TriggerProcessParameterName.USERS_TO_ADD.name().equals(
                    parameterName)) {
                List<VOUsageLicense> usersToAdd = ParameterizedTypes.list(
                        getObjectFromXML(triggerProcessParameters,
                                ArrayList.class), VOUsageLicense.class);
                for (VOUsageLicense license : usersToAdd) {
                    VOUser user = license.getUser();
                    if (user != null) {
                        result.addUserToAdd(user.getUserId());
                    }
                }
            }
            if (TriggerProcessParameterName.USERS_TO_REVOKE.name().equals(
                    parameterName)) {
                List<VOUserDetails> usersToRevoke = ParameterizedTypes.list(
                        getObjectFromXML(triggerProcessParameters,
                                ArrayList.class), VOUserDetails.class);
                for (VOUserDetails user : usersToRevoke) {
                    result.addUserToRevoke(user.getUserId());
                }
            }
        }
        holder.close();
        if (result.isInitialized()) {
            return result;
        }
        throw new Exception(
                "Migration failed, no product entry found for trigger process!");
    }

    /**
     * Determines service key for the trigger process parameters of the current
     * trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger process.
     * @return The trigger process parameter data.
     * @throws Exception
     */
    private long determineServiceKey(long triggerProcessKey) throws Exception {
        ResourceHolder holder = getTriggerProcessParameters(triggerProcessKey);
        ResultSet triggerProcessParameters = holder.getRs();
        while (triggerProcessParameters.next()) {
            String parameterName = triggerProcessParameters.getString("name");
            if (TriggerProcessParameterName.PRODUCT.name()
                    .equals(parameterName)) {
                VOService service = getObjectFromXML(triggerProcessParameters,
                        VOService.class);
                holder.close();
                return service.getKey();
            }
        }
        throw new Exception(
                "Migration failed, no product entry found for trigger process!");
    }

    /**
     * Returns the trigger processes for the trigger definition.
     * 
     * @param triggerDefinitionKey
     *            The technical key of the trigger definition.
     * @return A result set containing the trigger processes for the trigger
     *         definition.
     * @throws SQLException
     */
    private ResourceHolder getTriggerProcesses(long triggerDefinitionKey)
            throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(QUERY_TRIGGER_PROCESS_DETAILS);
        pstmt.setLong(1, triggerDefinitionKey);
        ResultSet processes = pstmt.executeQuery();
        ResourceHolder result = new ResourceHolder();
        result.setRs(processes);
        result.setStmt(pstmt);
        return result;
    }

    /**
     * Returns the trigger process parameters for the trigger process.
     * 
     * @param triggerProcessKey
     *            The technical key of the trigger definition.
     * @return A result set containing the trigger process parameters for the
     *         trigger process.
     * @throws SQLException
     */
    private ResourceHolder getTriggerProcessParameters(long triggerProcessKey)
            throws SQLException {
        PreparedStatement pstmt = conn
                .prepareStatement(QUERY_TRIGGER_PROCESS_PARAMETERS_DETAILS);
        pstmt.setLong(1, triggerProcessKey);
        ResultSet processes = pstmt.executeQuery();
        ResourceHolder result = new ResourceHolder();
        result.setRs(processes);
        result.setStmt(pstmt);
        return result;
    }

    /**
     * Retrieves the object from the serialized format.
     * 
     * @param <T>
     *            The object type.
     * @param is
     *            The result set, containing the serialized format of the
     *            object.
     * @param expectedClass
     *            The expected class of the object.
     * @return The object.
     */
    <T> T getObjectFromXML(ResultSet resultSet, Class<T> expectedClass)
            throws SQLException {
        InputStream is = null;
        try {
            is = resultSet.getBinaryStream("value");
            XMLDecoder decoder = newXmlDecoder(is);
            return expectedClass.cast(decoder.readObject());
        } finally {
            closeStream(is);
        }
    }

    XMLDecoder newXmlDecoder(InputStream is) {
        return new XMLDecoder(is);
    }

    void closeStream(Closeable stream) {
        Streams.close(stream);
    }

    /**
     * Creates a database entry for the temporary table for trigger process
     * identifiers.
     * 
     * @param triggerProcessKey
     *            The key of the trigger process the entry belongs to.
     * @param identifierName
     *            The name of the identifier to be stored.
     * @param value
     *            The value to be stored.
     * @throws SQLException
     */
    private void createTriggerProcessIdentifier(long triggerProcessKey,
            TriggerProcessIdentifierName identifierName, String value)
            throws SQLException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(QUERY_INSERT_TPI);
            pstmt.setLong(1, triggerProcessKey);
            pstmt.setString(2, identifierName.name());
            pstmt.setString(3, value);
            pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    /**
     * Creates a temporary table using serial data type to assign correct
     * primary keys.
     * 
     * @throws SQLException
     */
    private void createTempTable() throws SQLException {
        executeStatement(QUERY_CREATE_TEMP_TABLE);
        executeStatement(QUERY_REMOVE_TEMP_ENTRIES);
    }

    /**
     * Wrapper class for registration identifiers.
     */
    private static final class RegistrationIdentifiers {

        private final String userId;
        private final String userMail;

        RegistrationIdentifiers(String userId, String userMail) {
            this.userId = userId;
            this.userMail = userMail;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserMail() {
            return userMail;
        }
    }

    private static final class UserAssignmentIdentifiers {

        private String subscriptionId;
        private final List<String> usersToAdd = new ArrayList<String>();
        private final List<String> usersToRevoke = new ArrayList<String>();

        public void setSubscriptionId(String subId) {
            subscriptionId = subId;
        }

        public void addUserToAdd(String userId) {
            usersToAdd.add(userId);
        }

        public void addUserToRevoke(String userId) {
            usersToRevoke.add(userId);
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public List<String> getUsersToAdd() {
            Collections.sort(usersToAdd);
            return usersToAdd;
        }

        public List<String> getUsersToRevoke() {
            Collections.sort(usersToRevoke);
            return usersToRevoke;
        }

        public boolean isInitialized() {
            return subscriptionId != null;
        }
    }

    private static final class ResourceHolder {
        private ResultSet rs;
        private Statement stmt;

        public ResultSet getRs() {
            return rs;
        }

        public void setRs(ResultSet rs) {
            this.rs = rs;
        }

        public void setStmt(Statement stmt) {
            this.stmt = stmt;
        }

        /**
         * Closes currently used resources.
         * 
         * @throws SQLException
         */
        public void close() throws SQLException {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }
}
