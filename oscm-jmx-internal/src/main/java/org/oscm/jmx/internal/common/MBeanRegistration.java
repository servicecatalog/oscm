/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.jmx.internal.common;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.oscm.jmx.internal.bean.EJBClientFacade;

public class MBeanRegistration {
    private MBeanServer mbeanServer;
    private String mbeanRegistrationName;
    private EJBClientFacade clientFacade;
    private static final String MBEAN_REGISTRATION_PATTERN = "%s:type=%s";

    public MBeanRegistration(String mbeanRegistrationName, EJBClientFacade clientFacade) {
        this.mbeanRegistrationName = mbeanRegistrationName;
        this.clientFacade = clientFacade;
        mbeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    public void registerFromFolder(String relPath) {
        for (Class<?> mbean : JavaFileScanner.scanForAnnotatedClassesInFolder(
                relPath, MBean.class)) {
            registerMBean(mbean, mbeanRegistrationName);
        }
    }

    private void registerMBean(Class<?> mbean, String registrationName) {
        try {
            Object mbeanInstance = mbean.getConstructor(EJBClientFacade.class)
                    .newInstance(clientFacade);
            String mbeanRegistrationName = String.format(
                    MBEAN_REGISTRATION_PATTERN, registrationName,
                    mbean.getSimpleName());
            mbeanServer.registerMBean(mbeanInstance, new ObjectName(
                    mbeanRegistrationName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        for (ObjectName mBean : queryRegisteredMBeans(mbeanRegistrationName)) {
            unregisterMBean(mBean);
        }
    }

    Set<ObjectName> queryRegisteredMBeans(String registrationName) {
        try {
            ObjectName query = new ObjectName(registrationName + ":*");
            return mbeanServer.queryNames(query, null);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void unregisterMBean(ObjectName mBean) {
        try {
            mbeanServer.unregisterMBean(mBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
