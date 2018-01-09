/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.ant;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.junit.Before;
import org.junit.Test;

public class OperatorClientTaskTest {

    private OperatorClientTask task;

    @Before
    public void setup() {
        task = new OperatorClientTask();
    }

    @Test(expected = BuildException.class)
    public void testMissingUsername() {
        task.setPassword("secret");
        task.setCommand("createorganization");
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void testMissingPassword() {
        task.setUsername("me");
        task.setCommand("createorganization");
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void testInvalidCommand() {
        task.setUsername("me");
        task.setPassword("secret");
        task.setCommand("doesnotexist");
        task.execute();
    }

    @Test
    public void testCreateContextProperties() {
        task.setContextFactory("testFactory");
        task.setContextProviderUrl("testUrl");
        task.setRealm("testRealm");

        final Properties expected = new Properties();
        expected.put("java.naming.factory.initial", "testFactory");
        expected.put("java.naming.provider.url", "testUrl");
        expected.put("openejb.authentication.realmName", "testRealm");

        assertEquals(expected, task.createContextProperties());
    }

    @Test
    public void testCreateArguments() {
        final Parameter parameter = new Parameter();
        parameter.setName("key");
        parameter.setValue("value");
        task.addParameter(parameter);

        final Map<String, String> expected = Collections.singletonMap("key",
                "value");
        assertEquals(expected, task.createArguments());
    }

}
