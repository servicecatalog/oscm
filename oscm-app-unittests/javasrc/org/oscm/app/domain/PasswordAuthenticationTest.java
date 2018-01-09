/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oscm.app.v2_0.data.PasswordAuthentication;

public class PasswordAuthenticationTest {

    @Test
    public void passwordAuth_null() {
        // given
        PasswordAuthentication obj = new PasswordAuthentication("user",
                "passwd");

        // then
        assertFalse(obj == null);
    }

    @Test
    public void passwordAuth_equal1() {
        // given
        PasswordAuthentication obj1 = new PasswordAuthentication("user",
                "passwd");
        PasswordAuthentication obj2 = new PasswordAuthentication("user",
                "passwd");

        // then
        assertTrue(obj1.equals(obj2));
    }

    @Test
    public void passwordAuth_equal2() {
        // given
        PasswordAuthentication obj = new PasswordAuthentication("user",
                "passwd");

        // then
        assertTrue(obj.equals(obj));
    }

    @Test
    public void passwordAuth_notEqual_user() {
        // given
        PasswordAuthentication obj1 = new PasswordAuthentication("user1",
                "passwd");
        PasswordAuthentication obj2 = new PasswordAuthentication("user2",
                "passwd");

        // then
        assertFalse(obj1.equals(obj2));
    }

    @Test
    public void passwordAuth_notEqual_password() {
        // given
        PasswordAuthentication obj1 = new PasswordAuthentication("user",
                "passwd1");
        PasswordAuthentication obj2 = new PasswordAuthentication("user",
                "passwd2");

        // then
        assertFalse(obj1.equals(obj2));
    }
}
