/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                    
 *                                                                              
 *  Creation Date: Oct 5, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 5, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author tokoda
 */
public class GetUserOperationLogCommandTest extends CommandTestBase {

    private static final String CORRECT_FILE_NAME = "resources/test.csv";
    private static final String STUB_RETURN = "09/21/2011_11:59:00:000 FSP_INTS-BSS: INFO: 30001:,log,SUBSCR,op,MODIFY,user,mdehn,subscription,Docs2Go Gold - dept. PM,customer,BMW,customer id,90349ce5,service,Docs2Go Gold,activation,09/21/2011_12:03:24.231,status, DEACTIVATED,deactivation,09/21/2011_15:46:20.859,maketplace,FUJITSU,payment type,Invoice,billing contact, my home address,reference,4711\n";

    @Override
    protected IOperatorCommand createCommand() {
        return new GetUserOperationLogCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("getuseroperationlog", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("filename", "entitytype", "from", "to"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertTrue(command.run(ctx));
        assertEquals("getUserOperationLog", stubMethodName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long fromDateLong = Long.valueOf(sdf.parse(fromDate).getTime());
        Long toDateLong = Long.valueOf(sdf.parse(toDate).getTime());
        assertEquals(fromDateLong, stubCallArgs[1]);
        assertEquals(toDateLong, stubCallArgs[2]);

        File file = new File(CORRECT_FILE_NAME);
        String cannonicalPath = file.getCanonicalPath();
        assertOut("Successfully created the file: " + cannonicalPath
                + System.getProperty("line.separator"));
        assertErr("");
        assertTrue(file.exists());
    }

    @Test
    public void testSuccess_WithoutEntityType() throws Exception {
        String fileName = CORRECT_FILE_NAME;
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertTrue(command.run(ctx));
        assertEquals("getUserOperationLog", stubMethodName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long fromDateLong = Long.valueOf(sdf.parse(fromDate).getTime());
        Long toDateLong = Long.valueOf(sdf.parse(toDate).getTime());
        assertEquals(fromDateLong, stubCallArgs[1]);
        assertEquals(toDateLong, stubCallArgs[2]);

        File file = new File(CORRECT_FILE_NAME);
        String cannonicalPath = file.getCanonicalPath();
        assertOut("Successfully created the file: " + cannonicalPath
                + System.getProperty("line.separator"));
        assertErr("");
        assertTrue(file.exists());
    }

    @Test
    public void testFailedEmptyFileName() throws Exception {
        String fileName = "";
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertFalse(command.run(ctx));

        File file = new File(CORRECT_FILE_NAME);
        assertOut("");
        assertErr("File name can not be empty.\n");
        assertFalse(file.exists());
    }

    @Test
    public void testFailedWrongFileName() throws Exception {
        String fileName = "wrongDir/test.csv";
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertFalse(command.run(ctx));

        File file = new File(CORRECT_FILE_NAME);
        assertOut("");
        assertErr("The file can not be created.\n");
        assertFalse(file.exists());
    }

    @Test
    public void testFailedFileExisting() throws Exception {
        File file = new File(CORRECT_FILE_NAME);
        if (!file.createNewFile()) {
            fail();
        }

        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertFalse(command.run(ctx));
        assertOut("");
        assertErr("Specified file already exists.\n");
    }

    @Test(expected = java.text.ParseException.class)
    public void testFailedWrongFromdate() throws Exception {
        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "wrong";
        String toDate = "2011-11-30";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertFalse(command.run(ctx));
        fail();
    }

    @Test(expected = java.text.ParseException.class)
    public void testFailedWrongTodate() throws Exception {
        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "wrong";

        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);

        stubCallReturn = STUB_RETURN.getBytes();
        assertFalse(command.run(ctx));
        fail();
    }

    @Test
    public void testWriterIsClosed_GoodCase() throws Exception {
        PrintWriter pw = mock(PrintWriter.class);
        GetUserOperationLogCommand comm = spy(new GetUserOperationLogCommand());
        doReturn(pw).when(comm).createPrintWriter(any(File.class));

        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";
        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);
        stubCallReturn = STUB_RETURN.getBytes();

        assertTrue(comm.run(ctx));
        verify(pw, times(1)).close();
    }

    @Test
    public void testWriterIsClosed_BadCase() throws Exception {
        PrintWriter pw = mock(PrintWriter.class);
        doThrow(
                new IllegalStateException(
                        "Thrown to test if PrintWriter is closed on exception"))
                .when(pw).print(anyString());

        GetUserOperationLogCommand comm = spy(new GetUserOperationLogCommand());
        doReturn(pw).when(comm).createPrintWriter(any(File.class));

        String fileName = CORRECT_FILE_NAME;
        String entityType = "SUBSCR";
        String fromDate = "2011-10-01";
        String toDate = "2011-11-30";
        args.put("filename", fileName);
        args.put("entitytype", entityType);
        args.put("from", fromDate);
        args.put("to", toDate);
        stubCallReturn = STUB_RETURN.getBytes();

        assertFalse(comm.run(ctx));
        verify(pw, times(1)).close();
    }

    @After
    public void cleanup() {
        File file = new File(CORRECT_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
