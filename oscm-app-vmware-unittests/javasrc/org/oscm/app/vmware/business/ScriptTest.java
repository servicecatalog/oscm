/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.oscm.app.vmware.business.Script.OS;

/**
 * Unit tests for Script.
 * 
 */
public class ScriptTest {

    @Test
    public void testHidePasswordsLinux() throws Exception {
        // given
        String script = "#!/bin/bash" + OS.LINUX.getLineEnding()
                + "LINUX_ROOT_PWD='sunny'" + OS.LINUX.getLineEnding()
                + "DOMAIN_NAME='sunnyside.up.com'" + OS.LINUX.getLineEnding()
                + "INSTANCENAME='sunny'" + OS.LINUX.getLineEnding()
                + "REQUESTING_USER='Julia@DarkSide.com'"
                + OS.LINUX.getLineEnding()
                + "SCRIPT_URL='http://localhost:28880/test_script.sh'"
                + OS.LINUX.getLineEnding() + "SCRIPT_USERID='root'"
                + OS.LINUX.getLineEnding() + "SCRIPT_PWD='sunnyscript'"
                + OS.LINUX.getLineEnding() + "NIC1_DNS_SERVER=''"
                + OS.LINUX.getLineEnding() + "NIC1_DNS_SUFFIX=''"
                + OS.LINUX.getLineEnding() + "NIC1_GATEWAY=''"
                + OS.LINUX.getLineEnding() + "NIC1_IP_ADDRESS=''"
                + OS.LINUX.getLineEnding() + OS.LINUX.getLineEnding()
                + "shutdown -h now";

        List<String> passwords = new ArrayList<String>();
        passwords.add("sunny");
        passwords.add("sunnyscript");

        // when
        String changedScript = Script
                .hidePasswords(script, passwords, OS.LINUX);

        // then
        assertTrue(changedScript.contains("LINUX_ROOT_PWD='"
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("SCRIPT_PWD='" + Script.HIDDEN_PWD));
        System.out.println(changedScript);
    }

    @Test
    public void testHidePasswordsLinux_EmptyPwd() throws Exception {
        // given
        String script = "#!/bin/bash" + OS.LINUX.getLineEnding()
                + "LINUX_ROOT_PWD=''" + OS.LINUX.getLineEnding()
                + "DOMAIN_NAME='sunnyside.up.com'" + OS.LINUX.getLineEnding()
                + "INSTANCENAME='sunny'" + OS.LINUX.getLineEnding()
                + "REQUESTING_USER='Julia@DarkSide.com'"
                + OS.LINUX.getLineEnding()
                + "SCRIPT_URL='http://localhost:28880/test_script.sh'"
                + OS.LINUX.getLineEnding() + "SCRIPT_USERID='root'"
                + OS.LINUX.getLineEnding() + "SCRIPT_PWD='sunnyscript'"
                + OS.LINUX.getLineEnding() + "NIC1_DNS_SERVER=''"
                + OS.LINUX.getLineEnding() + "NIC1_DNS_SUFFIX=''"
                + OS.LINUX.getLineEnding() + "NIC1_GATEWAY=''"
                + OS.LINUX.getLineEnding() + "NIC1_IP_ADDRESS=''"
                + OS.LINUX.getLineEnding() + OS.LINUX.getLineEnding()
                + "shutdown -h now";

        List<String> passwords = new ArrayList<String>();
        passwords.add("");
        passwords.add("sunnyscript");

        // when
        String changedScript = Script
                .hidePasswords(script, passwords, OS.LINUX);

        // then
        System.out.println(changedScript);
        assertTrue(changedScript.contains("LINUX_ROOT_PWD='"
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("SCRIPT_PWD='" + Script.HIDDEN_PWD));
    }

    @Test
    public void testHidePasswordsWindows() throws Exception {
        // given
        String script = "set WINDOWS_LOCAL_ADMIN_PWD=sunny"
                + OS.WINDOWS.getLineEnding()
                + "set WINDOWS_DOMAIN_ADMIN_PWD=admin123"
                + OS.WINDOWS.getLineEnding()
                + "set DOMAIN_NAME=sunnyside.up.com"
                + OS.WINDOWS.getLineEnding() + "set INSTANCENAME=sunny"
                + OS.WINDOWS.getLineEnding()
                + "set REQUESTING_USER=Julia@DarkSide.com"
                + OS.WINDOWS.getLineEnding()
                + "set SCRIPT_URL=http://localhost:28880/test_script.sh"
                + OS.WINDOWS.getLineEnding()
                + "set SCRIPT_USERID=Administrator"
                + OS.WINDOWS.getLineEnding() + "set SCRIPT_PWD=sunnyscript"
                + OS.WINDOWS.getLineEnding() + "set NIC1_DNS_SERVER="
                + OS.WINDOWS.getLineEnding() + "set NIC1_DNS_SUFFIX="
                + OS.WINDOWS.getLineEnding() + "set NIC1_GATEWAY="
                + OS.WINDOWS.getLineEnding() + "set NIC1_IP_ADDRESS="
                + OS.WINDOWS.getLineEnding();

        List<String> passwords = new ArrayList<String>();
        passwords.add("sunny");
        passwords.add("sunnyscript");
        passwords.add("admin123");
        // when
        String changedScript = Script.hidePasswords(script, passwords,
                OS.WINDOWS);

        // then
        assertTrue(changedScript.contains("WINDOWS_LOCAL_ADMIN_PWD="
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("WINDOWS_DOMAIN_ADMIN_PWD="
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("SCRIPT_PWD=" + Script.HIDDEN_PWD));
        System.out.println(changedScript);
    }

    @Test
    public void testHidePasswordsWindows_EmptyPwd() throws Exception {
        // given
        String script = "set WINDOWS_LOCAL_ADMIN_PWD="
                + OS.WINDOWS.getLineEnding()
                + "set WINDOWS_DOMAIN_ADMIN_PWD=admin123"
                + OS.WINDOWS.getLineEnding()
                + "set DOMAIN_NAME=sunnyside.up.com"
                + OS.WINDOWS.getLineEnding() + "set INSTANCENAME=sunny"
                + OS.WINDOWS.getLineEnding()
                + "set REQUESTING_USER=Julia@DarkSide.com"
                + OS.WINDOWS.getLineEnding()
                + "set SCRIPT_URL=http://localhost:28880/test_script.sh"
                + OS.WINDOWS.getLineEnding()
                + "set SCRIPT_USERID=Administrator"
                + OS.WINDOWS.getLineEnding() + "set SCRIPT_PWD=sunnyscript"
                + OS.WINDOWS.getLineEnding() + "set NIC1_DNS_SERVER="
                + OS.WINDOWS.getLineEnding() + "set NIC1_DNS_SUFFIX="
                + OS.WINDOWS.getLineEnding() + "set NIC1_GATEWAY="
                + OS.WINDOWS.getLineEnding() + "set NIC1_IP_ADDRESS="
                + OS.WINDOWS.getLineEnding();

        List<String> passwords = new ArrayList<String>();
        passwords.add("");
        passwords.add("sunnyscript");
        passwords.add("admin123");
        // when
        String changedScript = Script.hidePasswords(script, passwords,
                OS.WINDOWS);

        // then
        assertTrue(changedScript.contains("WINDOWS_LOCAL_ADMIN_PWD="
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("WINDOWS_DOMAIN_ADMIN_PWD="
                + Script.HIDDEN_PWD));
        assertTrue(changedScript.contains("SCRIPT_PWD=" + Script.HIDDEN_PWD));
        System.out.println(changedScript);
    }

}
