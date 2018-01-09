/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.build.ant.CreateKPITask.Statistics;

public class CreateKPITaskTest {
    private static final String JUNIT_TESTFILE1 = "javares/JUnitResult1.xml";
    private static File OUTPUT_FOLDER = new File(
            System.getProperty("java.io.tmpdir") + "/output/");
    private static File KPI_FILE = new File(OUTPUT_FOLDER.getAbsolutePath()
            + "kpi.txt");

    @BeforeClass
    public static void setup() {
        OUTPUT_FOLDER.mkdirs();
        OUTPUT_FOLDER.deleteOnExit();
    }

    @After
    public void cleanTest() {
        if (KPI_FILE.exists()) {
            KPI_FILE.delete();
        }
    }

    @Test
    public void execute() {
        // given
        CreateKPITask task = new CreateKPITask();
        task.setInputFile(JUNIT_TESTFILE1);
        task.setOutputFile(KPI_FILE.getAbsolutePath());

        // when
        task.execute();

        // then
        assertTrue(KPI_FILE.exists());
    }

    @Test(expected = BuildException.class)
    public void execute_inputFileNotSet() {
        // given
        CreateKPITask task = new CreateKPITask();
        task.setInputFile(null);
        task.setOutputFile(KPI_FILE.getAbsolutePath());

        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_InputFileEmpty() {
        // given
        CreateKPITask task = new CreateKPITask();
        task.setInputFile(" ");
        task.setOutputFile(KPI_FILE.getAbsolutePath());

        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_notExistingFile() {
        // given
        CreateKPITask task = new CreateKPITask();
        task.setInputFile("notExistingFile.xml");
        task.setOutputFile(KPI_FILE.getAbsolutePath());

        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_outputFileNotSet() throws Exception {
        // given
        KPI_FILE.createNewFile();
        CreateKPITask task = new CreateKPITask();
        task.setInputFile(KPI_FILE.getAbsolutePath());
        task.setOutputFile(null);

        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_outputFileEmpty() throws Exception {
        // given
        KPI_FILE.createNewFile();
        CreateKPITask task = new CreateKPITask();
        task.setInputFile(KPI_FILE.getAbsolutePath());
        task.setOutputFile(" ");

        // when
        task.execute();
    }

    @Test
    public void createStatistics() throws Exception {
        // given
        CreateKPITask task = new CreateKPITask();
        String inputFile = JUNIT_TESTFILE1;

        // when
        Statistics statistics = task.createStatistics(inputFile);

        // then
        assertEquals(12, statistics.tests);
        assertEquals(6, statistics.bugs);
        assertEquals(0.767, statistics.duration, 0);
    }

    @Test
    public void writeStatistics() throws Exception {
        // given test,bugs,duration,currentDate
        Statistics statistics = new Statistics(1, 2, 5327.328, new Date(0));
        CreateKPITask task = new CreateKPITask();

        // when
        task.writeStatistics(statistics, KPI_FILE.getAbsolutePath());

        // then kpi file is not empty
        assertEquals(
                statistics.serializeToString(),
                new String(Files.readAllBytes(Paths.get(KPI_FILE
                        .getAbsolutePath())), "UTF-8"));
    }

    @Test(expected = IOException.class)
    public void writeStatistics_notExistingFile() throws Exception {
        // given
        CreateKPITask task = new CreateKPITask();
        Statistics statistics = new Statistics(1, 2, 5327.328, new Date(0));
        String outputFile = "notExistingFolder/NotExistingFile.xml";

        // when
        task.writeStatistics(statistics, outputFile);
    }

    @Test
    public void serializeToString() {
        // given test,bugs,duration,currentDate
        Statistics statistics = new Statistics(4, 3, 0, new Date(0));

        // when
        String value = statistics.serializeToString();

        // then
        assertEquals("tests=4 bugs=3 duration=0:00:00.000", value);
    }

    @Test
    public void formatDuration() {
        // given test,bugs,duration,currentDate
        Statistics statistics = new Statistics(0, 0, 5327.328, null);

        // when
        String duration = statistics.formatDuration();

        // then
        assertEquals("1:28:47.328", duration);
    }
}
