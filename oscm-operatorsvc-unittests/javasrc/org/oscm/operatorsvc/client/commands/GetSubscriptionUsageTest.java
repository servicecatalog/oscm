/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 26.04.17 13:54
 *
 ******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Test;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author tokoda
 */
public class GetSubscriptionUsageTest extends CommandTestBase {

    private static final String CORRECT_FILE_NAME = "resources/test.csv";

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
