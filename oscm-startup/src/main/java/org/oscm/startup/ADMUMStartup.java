/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 27.03.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.startup;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Checks for existence of the mandatory configuration settings of the ADM UM
 * product at startup time. The result will be logged.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ADMUMStartup extends HttpServlet {

    private static final long serialVersionUID = -3504533241988904286L;

    private static Log4jLogger logger = LoggerFactory
            .getLogger(ADMUMStartup.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cs;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = SessionServiceLocal.class)
    protected SessionServiceLocal prodSessionMgmt;

    @EJB
    protected TimerServiceBean timerMgmt;

    @EJB(beanInterface = SearchServiceLocal.class)
    protected SearchServiceLocal searchService;

    @Override
    public void init() throws ServletException {
        super.init();
        checkSettings();
        initializeLogger();
        checkResourceBundleExistence();
        cleanUpProductSessions();
        initTimers();
        initIndexing();
    }

    /**
     * Delegates the call to the search component to create the initial index
     * for full text search.
     */
    private void initIndexing() {
        searchService.initIndexForFulltextSearch(true);
    }

    /**
     * Delegates the call to the product timer management component to create
     * the timers on the node.
     */
    private void initTimers() {
        try {
            timerMgmt.initTimers();
        } catch (ValidationException e) {
            // Timer interval and offset value are too large and leads to next
            // expiration
            // date negative. Exception info has been logged into system log.No
            // handler is needed here.
        }
    }

    /**
     * Delegates the call to the product session management component to clean
     * up the corresponding database table.
     */
    private void cleanUpProductSessions() {
        prodSessionMgmt.deleteAllSessions();
    }

    /**
     * Invokes the services relying on resource bundles to check for their
     * existence.
     */
    private void checkResourceBundleExistence() {
        // currently only the localizer relies on resource bundles, so issue
        // this component to verify the existence of at least the existence of
        // the default locale bundles
        localizer.checkExistenceOfBundleFiles();
    }

    /**
     * Initializes the logger for the application.
     */
    private void initializeLogger() {
        String filePath = cs
                .getConfigurationSetting(ConfigurationKey.LOG_FILE_PATH,
                        Configuration.GLOBAL_CONTEXT)
                .getValue();
        String logLevel = cs.getConfigurationSetting(ConfigurationKey.LOG_LEVEL,
                Configuration.GLOBAL_CONTEXT).getValue();
        String logConfigFile = cs.getConfigurationSetting(
                ConfigurationKey.LOG_CONFIG_FILE, cs.getNodeName()).getValue();
        LoggerFactory.activateRollingFileAppender(filePath, logConfigFile,
                logLevel);
    }

    /**
     * Checks the currently existing configuration settings. If a mandatory
     * setting is not present, an exception will be logged. It also checks for
     * the setting of the node name.
     */
    private void checkSettings() {
        ConfigurationKey[] keys = ConfigurationKey.values();
        for (ConfigurationKey key : keys) {
            if (key.isMandatory()) {
                try {
                    cs.getConfigurationSetting(key,
                            Configuration.GLOBAL_CONTEXT);
                } catch (EJBException e) {
                    // will always log to the application server log file
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_MANDATORY_PROPERTY_NOT_SET,
                            key.getKeyName());
                }
            }
        }

        // check if the node name is configured
        String nodeName = cs.getNodeName();
        if (nodeName == null) {
            logger.logError(
                    LogMessageIdentifier.ERROR_MANDATORY_SETTING_OF_NODE_NOT_SET);
        }
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // not allowed because of security risk
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

}
