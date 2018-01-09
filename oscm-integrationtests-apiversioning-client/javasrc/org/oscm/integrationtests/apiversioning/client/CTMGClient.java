/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月23日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.client;

import java.util.List;
import java.util.Properties;

import org.oscm.apiversioning.handler.PropertyFileReader;
import org.oscm.integrationtests.apiversioning.ws.AccountServiceClient;
import org.oscm.integrationtests.apiversioning.ws.IdentityServiceClient;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Main entrance of DEMO client
 */
public class CTMGClient {
    private static final String PROPERTY_FILE_NAME = "configuration.properties";
    private static final String KEYSTORE_PATH = "keystore.path";
    private static final String KEYSTORE_PASSWORD = "keystore.password";
    private static final String TRUSTSTORE_PATH = "truststore.path";
    private static final String TRUSTSTORE_PASSWORD = "truststore.password";

    private final IdentityServiceClient identityServiceClient;
    private final AccountServiceClient accountServiceClient;

    public static void main(String[] args) {
        CTMGClient client = new CTMGClient();
        client.getAdminUserInfo();
        VOUser voUser = client.createUser();
        client.grantRoleForUser(voUser);
        client.revokeRoleForUser(voUser);
        voUser = client.lockUser(voUser);
        voUser = client.unlockUser(voUser);
        client.deleteUser(voUser);
    }

    public CTMGClient() {
        loadConfiguration();
        String defaultAdminKey = "1000";
        String defaultAdminPassword = "admin123";
        // Connect to services and logged in as default administrator
        // user
        identityServiceClient = new IdentityServiceClient(defaultAdminKey,
                defaultAdminPassword);
        accountServiceClient = new AccountServiceClient(defaultAdminKey,
                defaultAdminPassword);
    }

    private static void loadConfiguration() {
        Properties props = PropertyFileReader
                .getPropertiesFromFile(PROPERTY_FILE_NAME);
        System.setProperty("javax.net.ssl.keyStore",
                props.getProperty(KEYSTORE_PATH));
        System.setProperty("javax.net.ssl.keyStorePassword",
                props.getProperty(KEYSTORE_PASSWORD));
        System.setProperty("javax.net.ssl.trustStore",
                props.getProperty(TRUSTSTORE_PATH));
        System.setProperty("javax.net.ssl.trustStorePassword",
                props.getProperty(TRUSTSTORE_PASSWORD));
    }

    /**
     * Get information of default administrator user
     */
    private void getAdminUserInfo() {
        VOUserDetails user = identityServiceClient.getUserDetails();
        System.out.println("Default administrator user ID is: \""
                + user.getUserId() + "\"");
    }

    /**
     * Create new user
     */
    private VOUser createUser() {
        String organizationId = accountServiceClient.getOrganization()
                .getOrganizationId();
        VOUserDetails userDetails = identityServiceClient
                .createUser(organizationId);
        VOUser voUser = identityServiceClient
                .getVOUser(userDetails.getUserId());
        System.out
                .println("Created user ID is: \"" + voUser.getUserId() + "\"");
        printAllUsersForOrganization();
        return voUser;
    }

    /**
     * grant role for user
     */
    private void grantRoleForUser(VOUser user) {
        identityServiceClient.grantUserRoles(user.getUserId(),
                UserRoleType.PLATFORM_OPERATOR);
        VOUser voUser = identityServiceClient.getVOUser(user.getUserId());
        System.out.print("After grant user role, user contains role: ");
        for (UserRoleType role : voUser.getUserRoles()) {
            System.out.print("\"" + role + "\" ");
        }
        System.out.println("");
    }

    /**
     * revoke role from user
     */
    private void revokeRoleForUser(VOUser user) {
        identityServiceClient.revokeUserRoles(user.getKey(),
                UserRoleType.PLATFORM_OPERATOR);
        user = identityServiceClient.refreshUserValue(user);
        System.out.print("After revoke user role, user contains role: ");
        for (UserRoleType role : user.getUserRoles()) {
            System.out.print("\"" + role + "\" ");
        }
        System.out.println("");
    }

    /**
     * lock user account
     */
    private VOUser lockUser(VOUser voUser) {
        identityServiceClient.lockUserAccount(voUser);
        voUser = identityServiceClient.refreshUserValue(voUser);
        System.out.println("After lock user, user's status is \""
                + voUser.getStatus().toString() + "\"");
        return voUser;
    }

    /**
     * Unlock user account
     */
    private VOUser unlockUser(VOUser voUser) {
        identityServiceClient.unlockUserAccount(voUser);
        voUser = identityServiceClient.refreshUserValue(voUser);
        System.out.println("After unlock user, user's status is \""
                + voUser.getStatus().toString() + "\"");
        return voUser;
    }

    /**
     * Unlock user account
     */
    private void deleteUser(VOUser voUser) {
        identityServiceClient.deleteUser(voUser);
        printAllUsersForOrganization();
    }

    private void printAllUsersForOrganization() {
        List<VOUserDetails> userList = identityServiceClient
                .getUsersForOrganization();
        System.out.print("Organization contains " + userList.size()
                + " users: ");
        for (VOUserDetails userInOrg : userList) {
            System.out.print("\"" + userInOrg.getUserId() + "\" ");
        }
        System.out.println("");
    }
}
