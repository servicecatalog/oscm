/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: May 23, 2011                                                      
 *                                                                              
 *  Completion Time: June 17, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tools.ant.Project;

import org.oscm.ct.login.LoginHandlerFactory;
import org.oscm.webtest.base.PropertiesReader;

/**
 * Factory providing service interfaces to ANT tasks. One factory instance is
 * kept per ANT project.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ServiceFactory {

    private static WeakHashMap<Project, ServiceFactory> projectMap = new WeakHashMap<Project, ServiceFactory>();

    private static HashMap<String, String> credentials = new HashMap<String, String>();

    private String userkey;
    private String password;
    private Properties props;

    public static ServiceFactory create(Project project, String userkey,
            String password) {
        ServiceFactory pm = new ServiceFactory(userkey, password);
        projectMap.clear();
        projectMap.put(project, pm);
        return pm;
    }

    public static ServiceFactory get(Project project) {
        ServiceFactory serviceFactory = projectMap.get(project);
        if (serviceFactory == null) {
            throw new MissingSettingsException(
                    "Please specify login data using <store.settings ...>");
        }
        return serviceFactory;
    }

    public static ClassLoader replaceClassloader() {
        ClassLoader defaultCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                ServiceFactory.class.getClassLoader());
        return defaultCL;
    }

    private ServiceFactory(String userkey, String password) {
        this.userkey = userkey;
        this.password = password;

        setSystemPropertiesForKeyAndTruststore();

        if (password != null) {
            credentials.put(userkey, password);
        }
    }

    public <T> T getServiceInterface(Class<T> clazz, String runAsUser)
            throws Exception {

        String runWithPassword = "secret";
        if (runAsUser == null && userkey != null) {
            runAsUser = userkey;
            runWithPassword = password;
        } else {
            if (credentials.containsKey(runAsUser)
                    && credentials.get(runAsUser) != null) {
                runWithPassword = credentials.get(runAsUser);
            } else {
                if ("1000".equals(runAsUser)) {
                    runWithPassword = "admin123";
                }
            }
        }
        try {
            return getEJBServiceInterface(clazz, runAsUser, runWithPassword);
        } catch (NamingException e) {
            // Try/Catch is necessary due to incomprehensible NamingException
            // during lookup for internal.intf.MarketplaceService.
            // The first lookup works fine in UITest target part1. In target
            // part2, only the second lookup finds the desired object.
            return getEJBServiceInterface(clazz, runAsUser, runWithPassword);
        }
    }

    private <T> T getEJBServiceInterface(Class<T> remoteInterface,
            String userName, String password) throws Exception {
        InitialContext initialContext = new InitialContext(props);
        LoginHandlerFactory.getInstance().login(userName, password);

        @SuppressWarnings("unchecked")
        T service = (T) initialContext.lookup(remoteInterface.getName());
        return service;
    }

    public static String getHeapInfo() {
        return "HEAP uses "
                + getByteString(Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()) + " of "
                + getByteString(Runtime.getRuntime().totalMemory())
                + " (max is " + getByteString(Runtime.getRuntime().maxMemory())
                + ")";
    }

    private static String getByteString(long val) {
        int i = 0;
        String[] units = new String[] { "", "k", "m", "g", "t" };
        while (val > 1024 && (i + 1) < units.length) {
            val = val / 1024;
            i++;
        }
        return val + units[i];
    }

    private void setSystemPropertiesForKeyAndTruststore() {
        try {
            PropertiesReader reader = new PropertiesReader();
            props = reader.load();
            String glassfishBesDomain = props
                    .getProperty("glassfish.bes.domain");
            if (glassfishBesDomain == null || glassfishBesDomain.isEmpty()) {
                throw new IllegalStateException(
                        "Please check your test.properties file. No property 'glassfish.bes.domain' present!");
            }
            System.setProperty("javax.net.ssl.keyStore", glassfishBesDomain
                    + "/config/keystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
            System.setProperty("javax.net.ssl.trustStore", glassfishBesDomain
                    + "/config/cacerts.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        } catch (IOException ioe) {
            throw new IllegalStateException(
                    "Cannot read test.properties in order to set keystore and truststore!");
        }
    }
}
