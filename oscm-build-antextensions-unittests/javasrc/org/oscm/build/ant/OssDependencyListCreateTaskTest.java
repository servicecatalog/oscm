/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Aug 2, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 2, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author tokoda
 *
 */
public class OssDependencyListCreateTaskTest {

    private static final File TESTWORKSPACE = new File("..");
    private static final File DIR_LICENSES = new File(
            "../oscm-build/result/licenses");
    protected static final File DIR_OUTPUT = new File(
            "resources/result/osslist/");
    protected static final String FILE_OUTPUT_NAME = "osslist.txt";

    private OssDependencyListCreateTask task;

    @Before
    public void setUp() {
        task = new OssDependencyListCreateTask();
        task.setWorkspace(TESTWORKSPACE);
        task.setLicensesDir(DIR_LICENSES);
        task.setOutputDir(DIR_OUTPUT);
        task.setOutputFileName(FILE_OUTPUT_NAME);
    }

    @Test
    public void testExecute() {
        task.execute();
        File outputFile = new File(DIR_OUTPUT, FILE_OUTPUT_NAME);
        outputFile.delete();
        DIR_OUTPUT.delete();
    }
}
