/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-03-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Test for the logger initialization.
 * 
 * @author Goebel
 */
public class LoggerInitListenerTest {

    private static LoggerInitListener listener;
    private static ConfigurationService configServiceMock;
    private static Log4jLogger logger;
    private static File logsFolder;

    @BeforeClass
    public static void setup() throws Exception {
        emptyLogsFolder();

        configServiceMock = setupConfigurationMockForLogging();
        listener = new LoggerInitListener() {
            @Override
            ConfigurationService getConfigurationService() {
                return configServiceMock;
            }
        };

        logger = LoggerFactory.getLogger(LoggerInitListenerTest.class);
    }

    @Test
    public void contextDestroyed() {
        // given
        ServletContextEvent event = mock(ServletContextEvent.class);

        // when
        listener.contextDestroyed(event);

        // then
        assertEquals(null, listener.filePath);
        assertEquals(null, listener.logConfigFile);
        assertEquals(null, listener.logLevel);
    }

    @Test
    public void contextInitialized() throws Exception {
        // given
        ServletContextEvent event = mock(ServletContextEvent.class);

        // when contextInitialized and access logged
        listener.contextInitialized(event);

        logger.logInfo(Log4jLogger.ACCESS_LOG,
                LogMessageIdentifier.INFO_USER_LOGIN_SUCCESS, "test-user",
                "10.140.19.9");

        // then
        final String logEntryRegEx = getInfoEntryStartRegEx()
                + ".*test-user.*logged\\sin.*\\(.*10\\.140\\.19\\.9\\).*";

        assertWrittenToLogFile(logEntryRegEx);

    }

    private static ConfigurationService setupConfigurationMockForLogging()
            throws Exception {
        HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);
        ConfigurationService csMock = mock(ConfigurationService.class);
        when(
                csMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.LOG_FILE_PATH), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.LOG_FILE_PATH,
                                logsFolder.getAbsolutePath()));

        when(
                csMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.LOG_LEVEL), anyString()))
                .thenReturn(
                        createSetting(ConfigurationKey.LOG_LEVEL,
                                LoggerFactory.INFO_LOG_LEVEL));

        when(
                csMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.LOG_CONFIG_FILE), anyString()))
                .thenReturn(createSetting(ConfigurationKey.LOG_CONFIG_FILE, ""));

        ServiceAccess sa = mock(ServiceAccess.class);
        HttpSession httpSessionMock = mock(HttpSession.class);
        doReturn(httpSessionMock).when(servletRequestMock).getSession();
        doReturn(sa).when(httpSessionMock).getAttribute(anyString());
        doReturn(csMock).when(sa).getService(ConfigurationService.class);
        return csMock;
    }

    private File getAccessLogFile() {
        return new File(logsFolder, "access.log");
    }

    private static VOConfigurationSetting createSetting(ConfigurationKey key,
            String value) {
        return new VOConfigurationSetting(key, "global", value);
    }

    private String getInfoEntryStartRegEx() {
        return ".*INFO.*" + LoggerInitListenerTest.class.getSimpleName();
    }

    private void assertWrittenToLogFile(String regex) throws IOException {
        assertEquals(Boolean.TRUE, Boolean.valueOf(getAccessLogFile().exists()));
        assertEquals("No log entry matching '" + regex + "' found in logfile.",
        Boolean.TRUE, Boolean.valueOf(scanFile(regex)));
    }

    private static void emptyLogsFolder() throws IOException {
        logsFolder = new File("javares/logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        } else {
            for (File file : logsFolder.listFiles()) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }

    private boolean scanFile(String regex) throws IOException {
        FileReader fReader = null;
        BufferedReader bReader = null;
       
        try {
            fReader = new FileReader(getAccessLogFile());
            bReader = new BufferedReader(fReader);
            String line = bReader.readLine();

            while (line != null) {
        	System.out.println(line ); 
                if (line.matches(regex))
                    return true;
                line = bReader.readLine();
            }
        } finally {
            if (fReader != null) {
                fReader.close();
            }
            if (bReader != null) {
                bReader.close();
            }
        }
        return false;
    }

}
