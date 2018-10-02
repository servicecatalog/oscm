/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure.proxy;

import java.net.PasswordAuthentication;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by PLGrubskiM on 2017-03-24.
 */
public class ProxyAuthenticatorTest {

    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";

    @Test
    public void getPasswordAuthenticationTest() {
        // given
        ProxyAuthenticator pa = new ProxyAuthenticator(USERNAME, PASSWORD);
        // when
        final PasswordAuthentication passwordAuthentication = pa.getPasswordAuthentication();
        // then
        Assert.assertTrue(passwordAuthentication.getUserName().equals(USERNAME));
        Assert.assertTrue(String.valueOf(passwordAuthentication.getPassword()).equals(PASSWORD));
    }
}
