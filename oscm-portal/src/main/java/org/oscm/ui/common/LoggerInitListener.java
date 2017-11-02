/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-3-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Initializes the logger for portal.
 * 
 * @author Yu
 * 
 */
public class LoggerInitListener implements ServletContextListener {

    String filePath;
    String logLevel;
    String logConfigFile;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        this.filePath = null;
        this.logLevel = null;
        this.logConfigFile = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConfigurationService service = getConfigurationService();
        filePath = service.getVOConfigurationSetting(
                ConfigurationKey.LOG_FILE_PATH, Configuration.GLOBAL_CONTEXT)
                .getValue();
        logLevel = service.getVOConfigurationSetting(
                ConfigurationKey.LOG_LEVEL, Configuration.GLOBAL_CONTEXT)
                .getValue();
        logConfigFile = service.getVOConfigurationSetting(
                ConfigurationKey.LOG_CONFIG_FILE, Configuration.GLOBAL_CONTEXT)
                .getValue();
        LoggerFactory.activateRollingFileAppender(filePath, logConfigFile,
                logLevel);

    }

    ConfigurationService getConfigurationService() {
        return new ServiceLocator().findService(ConfigurationService.class);
    }

}
