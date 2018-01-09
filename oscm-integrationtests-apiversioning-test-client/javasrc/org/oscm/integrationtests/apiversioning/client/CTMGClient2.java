/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015å¹´1æœˆ23æ—¥                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.client;

import java.util.Properties;

import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Assert;

import org.junit.Test;

import org.oscm.apiversioning.handler.PropertyFileReader;
import org.oscm.integrationtests.apiversioning.ws.AccountServiceClient;
import org.oscm.integrationtests.apiversioning.ws.IdentityServiceClient2;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Main entrance of DEMO client
 */
public class CTMGClient2 {
    private static final String PROPERTY_FILE_NAME = "configuration.properties";
    private static final String KEYSTORE_PATH = "keystore.path";
    private static final String KEYSTORE_PASSWORD = "keystore.password";
    private static final String TRUSTSTORE_PATH = "truststore.path";
    private static final String TRUSTSTORE_PASSWORD = "truststore.password";

    private final IdentityServiceClient2 identityServiceClient;
    private final AccountServiceClient accountServiceClient;

    /**
     * if the CTMG change the method name in an api upgrade:
     * 
     * rename getCurrentUserDetails to getCurrentUserDetailsNew in
     * IdentityService.java
     */
    @Test
    public void renameMethod() {
        // when
        CTMGClient2 client = new CTMGClient2();
        VOUserDetails user = client.getAdminUserInfo();

        // then
        Assert.assertEquals(1000L, user.getKey());
        Assert.assertEquals("PLATFORM_OPERATOR", user.getOrganizationId());
    }

    /**
     * if the CTMG rename web parameter in an api upgrade:
     * 
     * rename createUser(@WebParam(name = "user")) to createUser(@WebParam(name
     * = "usernew")) in IdentityService.java.
     */
    @Test
    public void renameWebParameter() {
        // when
        CTMGClient2 client = new CTMGClient2();
        VOUserDetails voUser = client.createUserAndReturnDetails();

        // then
        Assert.assertEquals("PLATFORM_OPERATOR", voUser.getOrganizationId());
        Assert.assertEquals("firstName", voUser.getFirstName());
        Assert.assertEquals("gaowenxin@test.fnst.cn.fujitsu.com",
                voUser.getEMail());
        // Assert.assertEquals(Locale.ENGLISH.getLanguage(),
        // voUser.getLocaleNew());

    }

    /**
     * if the CTMG rename web parameter field in an api upgrade:
     * 
     * rename VOUserDetails.locale to VOUserDetails.localeNew in
     * org.oscm.vo.VOUserDetails.java
     */
    @Test
    public void renameWebParameterField() {
        // when
        CTMGClient2 client = new CTMGClient2();
        client.getAdminUserInfo();
        VOUserDetails voUser = client.createUserAndReturnDetails();

        // then
        // Assert.assertEquals(Locale.ENGLISH.getLanguage(),
        // voUser.getLocaleNew());
    }

    /**
     * if the CTMG add exception in return value in an api upgrade:
     * 
     * add exception ObjectNotFoundException in
     * IdentityService.lockUserAccount()
     */
    @Test
    public void addReturnException() {
        try {
            // when
            VOUser user = new VOUser();
            user.setKey(15667733456547558L);
            lockUser(user);
        } catch (SOAPFaultException e) {
            // then
            Assert.assertTrue(e.getMessage().contains(
                    "Could not find object of type 'USER' with business key"));
        }
    }

    /**
     * if the CTMG rename field in return value in an api upgrade:
     * 
     * 
     * rename VOUserDetails.locale to VOUserDetails.localeNew in
     * org.oscm.vo.VOUserDetails.java. the return value of method
     * IdentityService.createUser() is VOUserDetails.
     */
    @Test
    public void renameFieldInReturnValue() {
        // when
        CTMGClient2 client = new CTMGClient2();
        client.getAdminUserInfo();
        VOUserDetails voUser = client.createUserAndReturnDetails();

        // then
        // Assert.assertEquals(Locale.ENGLISH.getLanguage(),
        // voUser.getLocaleNew());
    }

    public CTMGClient2() {
        loadConfiguration();
        String defaultAdminKey = "1000";
        String defaultAdminPassword = "admin123";
        // Connect to services and logged in as default administrator
        // user
        identityServiceClient = new IdentityServiceClient2(defaultAdminKey,
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
    private VOUserDetails getAdminUserInfo() {
        VOUserDetails user = identityServiceClient.getUserDetails();
        System.out.println("Default administrator user ID is: \""
                + user.getUserId() + "\"");

        return user;
    }

    private VOUserDetails createUserAndReturnDetails() {
        String organizationId = accountServiceClient.getOrganization()
                .getOrganizationId();
        VOUserDetails userDetails = identityServiceClient
                .createUser(organizationId);
        return userDetails;
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
}
