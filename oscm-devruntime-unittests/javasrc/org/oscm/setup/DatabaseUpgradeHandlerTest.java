/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 26.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Contains tests for the DatabaseUpgradeHandler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class DatabaseUpgradeHandlerTest {

    private DatabaseUpgradeHandler handler;

    @Before
    public void setUp() {
        handler = new DatabaseUpgradeHandler();
    }

    /**
     * Reads the script files from a directory that does not contain any. The
     * expectation is to receive an empty list as result.
     */
    @Test
    public void getScriptFilesFromDirectory_noResult() throws Exception {
        // when
        List<File> files = handler.getScriptFilesFromDirectory(".", "derby");

        // then
        assertNotNull("Returned value must not be null", files);
        assertEquals("Wrong size of returned list of elements", 0, files.size());
    }

    /**
     * Reads the script files from a directory that contains only one matching
     * file. The expectation is to receive a list with only that entry as
     * result.
     */
    @Test
    public void getScriptFilesFromDirectory_oneResult() throws Exception {
        // given
        File tempFile = new File("./upd_derby_00_09_01.sql");
        tempFile.createNewFile();

        // when
        List<File> files = handler.getScriptFilesFromDirectory(".", "derby");

        // then
        assertNotNull("Returned value must not be null", files);
        assertEquals("Wrong size of returned list of elements", 1, files.size());

        tempFile.delete();
    }

    /**
     * Reads the script files from a directory that contains one matching and
     * two non-matching sql files. Excpectation is to receive the two files.
     */
    @Test
    public void getScriptFilesFromDirectory_twoResults() throws Exception {
        // given
        File tempFile1 = new File("./upd_derby_00_09_01.sql");
        tempFile1.createNewFile();
        File tempFile2 = new File("./upd_derby_00_09_02.sql");
        tempFile2.createNewFile();
        File tempFile3 = new File("./upd_derby_00_09_023.sql");
        tempFile3.createNewFile();

        // when
        List<File> files = handler.getScriptFilesFromDirectory(".", "derby");

        // then
        assertNotNull("Returned value must not be null", files);
        assertEquals("Wrong size of returned list of elements", 2, files.size());

        tempFile1.delete();
        tempFile2.delete();
        tempFile3.delete();
    }

    /**
     * Reads the script files from a directory that contains one matching and
     * two non-matching sql files. Excpectation is to receive the two files.
     */
    @Test
    public void getScriptFilesFromDirectory_mixedDBSTwoResults()
            throws Exception {
        // given
        File tempFile1 = new File("./upd_derby_00_09_01.sql");
        tempFile1.createNewFile();
        File tempFile2 = new File("./upd_derby_00_09_02.sql");
        tempFile2.createNewFile();
        File tempFile3 = new File("./upd_derby_00_09_023.sql");
        tempFile3.createNewFile();
        File tempFile4 = new File("./upd_postgresql_00_09_05.sql");
        tempFile4.createNewFile();
        File tempFile5 = new File("./upd_postgresql_01_00_01.sql");
        tempFile5.createNewFile();

        // when
        List<File> files = handler.getScriptFilesFromDirectory(".",
                "postgresql");

        // then
        assertNotNull("Returned value must not be null", files);
        assertEquals("Wrong size of returned list of elements", 2, files.size());

        tempFile1.delete();
        tempFile2.delete();
        tempFile3.delete();
        tempFile4.delete();
        tempFile5.delete();
    }

    /**
     * Tries to retrieve a list of script files from a file but not a directory.
     * Test is expected to return null, as no valid directory is specified.
     */
    @Test(expected = IOException.class)
    public void getScriptFilesFromDirectory_fromFile() throws Exception {
        // given
        File tempFile = File.createTempFile("test", ".sql", new File("."));

        // when
        handler.getScriptFilesFromDirectory(tempFile.getAbsolutePath(), "derby");

        tempFile.delete();
    }

    /**
     * Tries to retrieve a list of files for a non-existing directory. Method
     * under test is expected to return null.
     */
    @Test(expected = IOException.class)
    public void getScriptFilesFromDirectory_nonExisting() throws Exception {
        handler.getScriptFilesFromDirectory("./bla", "derby");
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * product major version is higher than the current one in the database (so
     * file must be returned).
     */
    @Test
    public void getFileExecutionOrder_oneEntryHigherMajorVersion()
            throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(0, 9, 1);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 1,
                fileExecutionOrder.size());
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * product major version is less than the current one in the database (so
     * file must not be returned).
     */
    @Test
    public void getFileExecutionOrder_OneEntryLowerMajorVersion()
            throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(2, 9, 1);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 0,
                fileExecutionOrder.size());
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * product minor version is higher (while major version is the same) than
     * the current one in the database (so file must be returned).
     */
    @Test
    public void getFileExecutionOrder_OneEntryGreaterMinorVersion()
            throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_10_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 9, 1);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 1,
                fileExecutionOrder.size());
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * product minor version is less than the current one in the database (so
     * file must not be returned).
     */
    @Test
    public void getFileExecutionOrder_OneEntryLessMinorVersion()
            throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 9, 1);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 0,
                fileExecutionOrder.size());
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * schema version is greater than the current one in the database (so file
     * must be returned). The major and minor version are the same.
     */
    @Test
    public void getFileExecutionOrder_oneEntryGreaterSchemaVersion()
            throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_02.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 1, 1);

        // hen
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 1,
                fileExecutionOrder.size());
    }

    /**
     * Retrieves the execution order for one upgrade script file, where its
     * version is less than the current one in the database (so file must not be
     * returned).
     */
    @Test
    public void getFileExecutionOrder_lesserSchemaVersion() throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_02.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 1, 3);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 0,
                fileExecutionOrder.size());
    }

    /**
     * Calls the method which must retrieve three entries. The verification is
     * to ensure the correct order of files.
     * 
     * @throws Exception
     */
    @Test
    public void getFileExecutionOrder_verifyOrder() throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_03_02_02.sql"));
        files.add(new File("upd_derby_04_01_01.sql"));
        files.add(new File("upd_derby_03_02_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(2, 1, 3);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 3,
                fileExecutionOrder.size());
        assertEquals("Wrong file order", "upd_derby_03_02_01.sql",
                fileExecutionOrder.get(0).getName());
        assertEquals("Wrong file order", "upd_derby_03_02_02.sql",
                fileExecutionOrder.get(1).getName());
        assertEquals("Wrong file order", "upd_derby_04_01_01.sql",
                fileExecutionOrder.get(2).getName());
    }

    /**
     * Retrieves the execution list for file names for a more complex scenario
     * with initially wrong order and also entries to be excluded.
     */
    @Test
    public void getFileExecutionOrder_complexList() throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_03_02_02.sql"));
        files.add(new File("upd_derby_04_01_01.sql"));
        files.add(new File("upd_derby_03_02_01.sql"));
        files.add(new File("upd_derby_01_01_01.sql"));
        files.add(new File("upd_derby_01_01_02.sql"));
        files.add(new File("upd_derby_02_01_01.sql"));
        files.add(new File("upd_derby_02_01_02.sql"));
        files.add(new File("upd_derby_02_01_03.sql"));
        files.add(new File("upd_derby_02_02_01.sql"));
        files.add(new File("upd_derby_02_01_04.sql"));
        files.add(new File("upd_derby_03_01_02.sql"));
        files.add(new File("upd_derby_03_01_01.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(2, 1, 3);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 7,
                fileExecutionOrder.size());
        assertEquals("Wrong file order", "upd_derby_02_01_04.sql",
                fileExecutionOrder.get(0).getName());
        assertEquals("Wrong file order", "upd_derby_02_02_01.sql",
                fileExecutionOrder.get(1).getName());
        assertEquals("Wrong file order", "upd_derby_03_01_01.sql",
                fileExecutionOrder.get(2).getName());
        assertEquals("Wrong file order", "upd_derby_03_01_02.sql",
                fileExecutionOrder.get(3).getName());
        assertEquals("Wrong file order", "upd_derby_03_02_01.sql",
                fileExecutionOrder.get(4).getName());
        assertEquals("Wrong file order", "upd_derby_03_02_02.sql",
                fileExecutionOrder.get(5).getName());
        assertEquals("Wrong file order", "upd_derby_04_01_01.sql",
                fileExecutionOrder.get(6).getName());
    }

    /**
     * Verifies, that the upper version limit is respected.
     */
    @Test
    public void getFileExecutionOrder_withToVersion() throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_00_00.sql"));
        files.add(new File("upd_derby_01_01_00.sql"));
        files.add(new File("upd_derby_01_02_00.sql"));
        files.add(new File("upd_derby_02_00_00.sql"));
        files.add(new File("upd_derby_02_01_00.sql"));
        files.add(new File("upd_derby_02_02_00.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 1, 0);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, new DatabaseVersionInfo(2, 1, 0));

        // then
        assertEquals("Wrong number of elements returned", 3,
                fileExecutionOrder.size());
        assertEquals("Wrong file order", "upd_derby_01_02_00.sql",
                fileExecutionOrder.get(0).getName());
        assertEquals("Wrong file order", "upd_derby_02_00_00.sql",
                fileExecutionOrder.get(1).getName());
        assertEquals("Wrong file order", "upd_derby_02_01_00.sql",
                fileExecutionOrder.get(2).getName());
    }

    /**
     * Verifies behaviour based on a scenario that caused problems in a local
     * environment.
     */
    @Test
    public void getFileExecutionOrder_upgradeSample() throws Exception {
        // given
        List<File> files = new ArrayList<File>();
        files.add(new File("upd_derby_01_01_06.sql"));
        files.add(new File("upd_derby_01_01_07.sql"));
        files.add(new File("upd_derby_01_01_08.sql"));
        files.add(new File("upd_derby_01_01_09.sql"));
        files.add(new File("upd_derby_01_01_10.sql"));
        files.add(new File("upd_derby_01_01_11.sql"));
        DatabaseVersionInfo controlVersion = new DatabaseVersionInfo(1, 1, 10);

        // when
        List<File> fileExecutionOrder = handler.getFileExecutionOrder(files,
                controlVersion, DatabaseVersionInfo.MAX);

        // then
        assertEquals("Wrong number of elements returned", 1,
                fileExecutionOrder.size());
        assertEquals("Wrong file order", "upd_derby_01_01_11.sql",
                fileExecutionOrder.get(0).getName());
    }

    @Test(expected = FileNotFoundException.class)
    public void getSQLCommandsFromFile_noFile() throws Exception {
        handler.getSQLCommandsFromFile(new File("bla"));
    }

    @Test
    public void getSQLCommandsFromFile_emptyFile() throws Exception {
        // given
        File file = getFile("emptyFile.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getSQLCommandsFromFile_onlyComments() throws Exception {
        // given
        File file = getFile("onlyComments.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getSQLCommandsFromFile_oneStatement() throws Exception {
        // given
        File file = getFile("oneStatement.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("statement1", result.get(0));
    }

    @Test
    public void getSQLCommandsFromFile_twoStatements() throws Exception {
        // given
        File file = getFile("twoStatements.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("statement1", result.get(0));
        assertEquals("statement2", result.get(1));
    }

    @Test
    public void getSQLCommandsFromFile_twoStatementsWithComments()
            throws Exception {
        // given
        File file = getFile("twoStatementsWithComments.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("statement1", result.get(0));
        assertEquals("statement2", result.get(1));
    }

    @Test
    public void getSQLCommandsFromFile_multipleStatements() throws Exception {
        // given
        File file = getFile("multipleStatements.sql");

        // when
        List<String> result = handler.getSQLCommandsFromFile(file);

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("statement1", result.get(0));
        assertEquals("another\r\nmultiline\r\nstatement", result.get(1));
        assertEquals("statement2", result.get(2));
    }

    private File getFile(String name) {
        URL resource = this.getClass().getResource("/" + name);
        assertNotNull(resource);
        return new File(resource.getFile());
    }

    @Test
    public void getSQLCommandsFromFile_closeStream() throws Exception {
        // given
        handler = spy(new DatabaseUpgradeHandler());
        File file = getFile("emptyFile.sql");

        // when
        handler.getSQLCommandsFromFile(file);

        // then
        verify(handler, times(3)).closeStream(any(Closeable.class));
    }

    @Test
    public void getSQLCommandsFromFile_closeStream_ioException()
            throws Exception {
        // given
        handler = spy(new DatabaseUpgradeHandler());

        // when
        try {
            handler.getSQLCommandsFromFile(new File("unknownpath"));
            fail();
        } catch (FileNotFoundException e) {

            // then
            assertNotNull(e);
            verify(handler, times(3)).closeStream(any(Closeable.class));
        }
    }

}
