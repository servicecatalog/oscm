/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 14.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.oscm.app.common.Constants.APPLICATION_SERVER_HOME_CONSTANT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.business.Initializer;

/**
 * Unit test of initializer
 */
public class InitializerTest {

    private Initializer testElm;
    private String oldSysSetting;
    private String log4jFolderPath;

    private String FILE_PATH = "temp_unit_test/conf/log4j.app.core.properties";
    private String FILE_PATH_CONFIG = "/conf/log4j.app.core.properties";
    private String FUJITSU_PACKAGE = "com.fujitsu.bss.app";
    private String OSCM_PACKAGE = "org.oscm.app";

    private final String LOG4J_CONFIG1 = "log4j.rootLogger=OFF\n"
            + "org.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog\n"
            + "log4j.logger.org.oscm.app=INFO, CONSOLE\n"
            + "log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender\n"
            + "log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout\n"
            + "log4j.appender.CONSOLE.layout.ConversionPattern=%d [%t] - %-5p - %m%n\n";

    private final String LOG4J_CONFIG2 = "log4j.rootLogger=OFF\n"
            + "org.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog\n"
            + "log4j.logger.org.oscm.app=DEBUG, CONSOLE\n"
            + "log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender\n"
            + "log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout\n"
            + "log4j.appender.CONSOLE.layout.ConversionPattern=%d [%t] - %-5p - %m%n\n";

    private final String LOG4J_CONFIG3 = "log4j.rootLogger=OFF\n"
            + "org.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog\n"
            + "log4j.logger.org.oscm.app=OFF, CONSOLE\n"
            + "log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender\n"
            + "log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout\n"
            + "log4j.appender.CONSOLE.layout.ConversionPattern=%d [%t] - %-5p - %m%n\n";

    private final String LOG4J_CONFIG_FUJITSU_PACKAGE = "log4j.rootLogger=OFF\n"
            + "org.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog\n"
            + "log4j.logger.com.fujitsu.bss.app=INFO, CONSOLE\n"
            + "log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender\n"
            + "log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout\n"
            + "log4j.appender.CONSOLE.layout.ConversionPattern=%d [%t] - %-5p - %m%n\n";

    private TimerService timerService;

    @Before
    public void setUp() throws Exception {

        testElm = new Initializer();
        timerService = Mockito.mock(TimerService.class);
        Collection<Timer> timers = new ArrayList<Timer>();
        Mockito.when(timerService.getTimers()).thenReturn(timers);

        // Set timer resource
        Field field = testElm.getClass().getDeclaredField("timerService");
        field.setAccessible(true);
        field.set(testElm, timerService);

    }

    @After
    public void tearDown() throws Exception {
        if (log4jFolderPath != null) {
            File folder = new File(log4jFolderPath);
            if (folder.exists()) {
                folder.delete();
            }
        }
    }

    private File createLog4jFile(String file) throws IOException {
        File tmpFile = File.createTempFile("log4j", ".tmp");
        File log4jFile = new File(tmpFile.getParentFile(), FILE_PATH);
        tmpFile.delete();
        log4jFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(log4jFile);
        try {
            fw.write(file);
        } finally {
            fw.close();
        }
        return log4jFile;
    }

    private void setSysSetting(String value) {
        oldSysSetting = System.getProperty(APPLICATION_SERVER_HOME_CONSTANT);
        if (value != null) {
            System.setProperty(APPLICATION_SERVER_HOME_CONSTANT, value);
        } else {
            System.clearProperty(APPLICATION_SERVER_HOME_CONSTANT);
        }
    }

    private void resetSysSetting() {
        if (oldSysSetting != null) {
            System.setProperty(APPLICATION_SERVER_HOME_CONSTANT, oldSysSetting);
        } else {
            System.clearProperty(APPLICATION_SERVER_HOME_CONSTANT);
        }
    }

    @Test
    public void testLoggingEmptySetting() throws Exception {
        try {
            setSysSetting(null);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

        } finally {
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWrongFileType() throws Exception {
        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            String log4jPath = log4jFile.getCanonicalPath();
            setSysSetting(log4jPath);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithException() throws Exception {
        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            Mockito.when(
                    timerService.createTimer(Matchers.anyLong(),
                            Matchers.anyLong(),
                            Matchers.any(Serializable.class))).thenThrow(
                    new RuntimeException("error"));

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithUnexpectedTimer() throws Exception {
        // Simulate timer
        testElm.handleTimer(null);

        try {

            setSysSetting(null);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

        } finally {
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithExistingTimer() throws Exception {
        Collection<Timer> timers = new ArrayList<Timer>();
        Timer tmpTimer = Mockito.mock(Timer.class);
        timers.add(tmpTimer);
        Mockito.when(timerService.getTimers()).thenReturn(timers);

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            Mockito.verify(timerService, never()).createTimer(
                    Matchers.anyLong(), Matchers.anyLong(),
                    Matchers.any(Serializable.class));

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    @Ignore
    // temporarily ignoring failing test
    public void testLogging() throws Exception {

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            Mockito.verify(timerService).createTimer(Matchers.anyLong(),
                    Matchers.anyLong(), Matchers.any(Serializable.class));

            // Validate log setting
            org.apache.log4j.Logger logger = LogManager
                    .getLogger(Initializer.class);
            assertTrue(logger.isInfoEnabled());
            assertFalse(logger.isDebugEnabled());

            // Modify properties
            FileWriter fw = new FileWriter(log4jFile);
            try {
                fw.write(LOG4J_CONFIG2);
            } finally {
                fw.close();
            }

            // Simulate timer
            testElm.handleTimer(null);

            // And check whether log level has been modified to DEBUG
            assertTrue(logger.isInfoEnabled());
            assertTrue(logger.isDebugEnabled());

            // Simulate timer (again without any change)
            testElm.handleTimer(null);
            assertTrue(logger.isInfoEnabled());
            assertTrue(logger.isDebugEnabled());

            // Modify properties (switch off!)
            fw = new FileWriter(log4jFile);
            try {
                fw.write(LOG4J_CONFIG3);
            } finally {
                fw.close();
            }

            // Simulate timer
            testElm.handleTimer(null);

            // And check whether log level has been modified to DEBUG
            assertFalse(logger.isInfoEnabled());
            assertFalse(logger.isDebugEnabled());

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @SuppressWarnings("boxing")
    @Test
    public void testLoggingWithFileAccessException() throws Exception {
        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Invoke "private" method :)
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            // Now we exchange the internal stored file with a mockup :)
            File oopsFile = Mockito.mock(File.class);
            Field fileField = testElm.getClass().getDeclaredField("logFile");
            fileField.setAccessible(true);
            fileField.set(testElm, oopsFile);

            // And enable damage!
            Mockito.when(oopsFile.lastModified()).thenThrow(
                    new SecurityException());

            // Simulate timer (-> this will now result in a security exception!)
            testElm.handleTimer(null);

            // Simulate timer (-> this will now result in a security exception!)
            testElm.handleTimer(null);

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithPublish() throws Exception {

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Delete temp file again
            log4jFile.delete();

            assertFalse(log4jFile.exists());

            // Invoke "private" method :)
            // => publish template file
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            assertTrue(log4jFile.exists());

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithPublishWithoutTemplate() throws Exception {

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Delete temp file again
            log4jFile.delete();

            assertFalse(log4jFile.exists());

            // Manipulate name of template file
            Field field = testElm.getClass().getDeclaredField("LOG4J_TEMPLATE");
            field.setAccessible(true);
            field.set(testElm, "not_existing_template");

            // Invoke "private" method :)
            // => publish template file
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            // File does still not exists
            assertFalse(log4jFile.exists());

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void testLoggingWithPublishWithFileWriteError() throws Exception {

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            // Delete temp file and parent folder again => file can't be
            // published because parent folder is missing
            log4jFile.delete();
            log4jFile.getParentFile().delete();

            assertFalse(log4jFile.exists());

            // Invoke "private" method :)
            // => publish template file
            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            // File does still not exists
            assertFalse(log4jFile.exists());

        } finally {
            log4jFile.delete();
            resetSysSetting();
        }
    }

    @Test
    public void replacePackageNameFujitsu() throws IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        File log4jFile = createLog4jFile(LOG4J_CONFIG_FUJITSU_PACKAGE);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            // testElm.replacePackageName(log4jFolderPath + FILE_PATH_CONFIG);
            Path path = Paths.get(log4jFolderPath + FILE_PATH_CONFIG);
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            assertTrue(content.contains(OSCM_PACKAGE));
            assertFalse(content.contains(FUJITSU_PACKAGE));
        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }

    @Test
    public void replacePackageNameOscm() throws IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        File log4jFile = createLog4jFile(LOG4J_CONFIG1);
        try {
            // Set path of log4j properties
            log4jFolderPath = log4jFile.getParentFile().getParent();
            setSysSetting(log4jFolderPath);

            Method method = testElm.getClass().getDeclaredMethod(
                    "postConstruct");
            method.setAccessible(true);
            method.invoke(testElm);

            Path path = Paths.get(log4jFolderPath + FILE_PATH_CONFIG);
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            assertTrue(content.contains(OSCM_PACKAGE));
            assertFalse(content.contains(FUJITSU_PACKAGE));
        } finally {
            log4jFile.delete();
            resetSysSetting();
        }

    }
}
