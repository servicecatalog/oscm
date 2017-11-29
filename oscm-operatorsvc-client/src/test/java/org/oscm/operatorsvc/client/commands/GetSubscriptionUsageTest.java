/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 26.04.17 13:54
 *
 ******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.After;
import org.junit.Test;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author tokoda
 */
public class GetSubscriptionUsageTest extends CommandTestBase {

    private static final String CORRECT_FILE_NAME = "test.csv";

    @Override
    protected IOperatorCommand createCommand() {
        return new GetSubscriptionUsage();
    }

    @Test
    public void testGetName() {
        assertEquals("getsubscriptionusage", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("filename"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        String fileName = CORRECT_FILE_NAME;

        args.put("filename", fileName);

        stubCallReturn = Collections.emptyList();
        assertTrue(command.run(ctx));
        assertEquals("getSubscriptionUsageReport", stubMethodName);

        File file = new File(CORRECT_FILE_NAME);
        String cannonicalPath = file.getCanonicalPath();
        assertOut("Successfully created the file: " + cannonicalPath
                + System.getProperty("line.separator"));
        assertErr("");
        assertTrue(file.exists());
    }

    @After
    public void cleanup() {
        File file = new File(CORRECT_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
