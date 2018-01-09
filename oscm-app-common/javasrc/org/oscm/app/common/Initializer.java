/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-05-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common;

import static org.oscm.app.common.Constants.APPLICATION_SERVER_HOME_CONSTANT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class Initializer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Initializer.class);

    // Default name of log4j template
    private String LOG4J_TEMPLATE = "log4j.properties.template";

    // Timer based log file monitoring
    private long TIMER_DELAY_VALUE = 60000;

    @Resource
    private TimerService timerService;
    private File logFile;
    private long logFileLastModified = 0;
    private boolean logFileWarning = false;

    @Inject
    private ControllerAccess controllerAccess;

    public void setControllerAccess(final ControllerAccess controllerAccess) {
        this.controllerAccess = controllerAccess;
    }

    @PostConstruct
    private void postConstruct() {
        try {
            String instanceRoot = System
                    .getProperty(APPLICATION_SERVER_HOME_CONSTANT);
            String controllerId = controllerAccess.getControllerId();
            if (instanceRoot != null) {
                File root = new File(instanceRoot);
                if (root.isDirectory()) {
                    String filePath = "/conf/log4j." + controllerId
                            + ".properties";
                    // Determine log file
                    logFile = new File(root, filePath);

                    // If the target file does not exist we will provide it once
                    // from the template
                    if (!logFile.exists()) {
                        publishTemplateFile();
                    } else {
                        replacePackageName(instanceRoot + filePath);
                    }

                    // Read configuration
                    handleOnChange(logFile);

                    // And init timer based service
                    LOGGER.debug(
                            "Enable timer service for monitoring modification of "
                                    + logFile.getPath());
                    initTimer();

                } else {
                    LOGGER.error(
                            "Failed to initialize log file: invalid instanceRoot "
                                    + instanceRoot);
                    logFile = null;
                }
            } else {
                LOGGER.error(
                        "Failed to initialize log file: missing system property 'catalina.home'");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to initialize log file", e);
            logFile = null;
        }
    }

    /**
     * Initialize timer service.
     */
    private void initTimer() {
        Collection<?> timers = timerService.getTimers();
        if (timers.isEmpty()) {
            timerService.createTimer(0, TIMER_DELAY_VALUE, null);
        }
    }

    /**
     * Copy template file to default destination
     */
    private void publishTemplateFile() throws Exception {
        InputStream is = null;
        try {
            // Search resource in controller package
            is = controllerAccess.getClass().getClassLoader()
                    .getResourceAsStream(LOG4J_TEMPLATE);
            if (is == null) {
                LOGGER.warn("Template file not found: " + LOG4J_TEMPLATE);
            } else if (logFile.getParentFile().exists()) {
                FileUtils.writeByteArrayToFile(logFile,
                        IOUtils.toByteArray(is));
            }
        } catch (Exception e) {
            // ignore
            LOGGER.error("Failed to publish template file from "
                    + LOG4J_TEMPLATE + " to " + logFile.getAbsolutePath(), e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Replace the package names from "com.fujitsu.bss.app" to "org.oscm.app" in
     * the existing log files.
     */
    private void replacePackageName(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("com.fujitsu.bss.app", "org.oscm.app");
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Handles the timer event.
     */
    @Timeout
    public void handleTimer(@SuppressWarnings("unused") Timer timer) {
        if (logFile != null) {
            handleOnChange(logFile);
        }
    }

    /**
     * On change event
     */
    void handleOnChange(File logFile) {
        try {
            long lastModif = logFile.lastModified();
            if (lastModif > logFileLastModified) {
                logFileLastModified = lastModif;
                LOGGER.debug("Reload log4j configuration from "
                        + logFile.getAbsolutePath());
                new PropertyConfigurator().doConfigure(
                        logFile.getAbsolutePath(),
                        LogManager.getLoggerRepository());
                logFileWarning = false;
            }
        } catch (Exception e) {
            if (!logFileWarning) {
                logFileWarning = true;
                LOGGER.error(logFile.getAbsolutePath(), e);
            }
        }
    }
}
