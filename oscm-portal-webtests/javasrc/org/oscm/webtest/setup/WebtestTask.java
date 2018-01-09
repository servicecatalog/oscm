/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: May 23, 2011                                                      
 *                                                                              
 *  Completion Time: June 6, 2011
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Common super class for custom ANT tasks for WS-API calls.
 * 
 * @author Dirk Bernsau
 * 
 */
public abstract class WebtestTask extends Task {

    protected static final String TEST_ORGANIZATION_ADDRESS = "test.organization.address";
    protected static final String COMMON_ORG_NAME = "common.orgName";
    protected static final String COMMON_EMAIL = "common.email";
    private String runAsUser;

    public abstract void executeInternal() throws Exception;

    public void setRunAs(String value) {
        runAsUser = value;
    }

    protected <T> T getServiceInterface(Class<T> clazz) {
        try {
            try {
                return ServiceFactory.get(getProject()).getServiceInterface(
                        clazz, runAsUser);
            } catch (MissingSettingsException e) {
                if (runAsUser != null) {
                    ServiceFactory.create(getProject(), runAsUser, null);
                    return ServiceFactory.get(getProject())
                            .getServiceInterface(clazz, runAsUser);
                }
                throw e;
            }
        } catch (Exception e) {
            throwBuildException(null, e);
        }
        return null;
    }

    @Override
    public final void execute() throws BuildException {
        ClassLoader defaultCL = ServiceFactory.replaceClassloader();
        try {
            executeInternal();
        } catch (Throwable e) {
            if (e instanceof BuildException) {
                log("Build exception " + e.getMessage(), 0);
                throw (BuildException) e;
            } else {
                if (e instanceof WebtestTaskException) {
                    // just a message carrier
                    throwBuildException(e.getMessage());
                } else {
                    throwBuildException(null, e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(defaultCL);
            log(ServiceFactory.getHeapInfo());
        }
    }

    protected void throwBuildException(String message) {
        throwBuildException(message, null);
    }

    protected void throwBuildException(String message, Throwable e) {
        if (message == null && e != null) {
            message = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        log(message, 0);
        if (e == null) {
            throw new BuildException(message);
        }
        throw new BuildException(message, e);
    }

    protected void throwBuildExceptionForIds(String start,
            Collection<String> ids) {
        throwBuildException(getStringForIds(start, ids));
    }

    protected void logForIds(String start, Collection<String> ids) {
        log(getStringForIds(start, ids));
    }

    protected String getStringForIds(String start, Collection<String> ids) {
        StringBuffer buf = new StringBuffer(start);
        int i = 0;
        for (String id : ids) {
            if (i < 5) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(id);
            } else {
                buf.append(" ...");
                break;
            }
            i++;
        }
        return buf.toString();
    }

    protected static final boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    @Override
    protected void handleErrorOutput(String output) {
        String loc = getFailedTestLocation();
        if (loc != null) {
            super.handleErrorOutput(loc);
        }
        super.handleErrorOutput(output);
    }

    @Override
    public void log(String msg, int msgLevel) {
        if (msgLevel == 0) {
            String loc = getFailedTestLocation();
            if (loc != null) {
                super.log(loc, msgLevel);
            }
        }
        super.log(msg, msgLevel);
    }

    private String getFailedTestLocation() {
        String testFile = getOwningTarget().getLocation().getFileName() + ":"
                + getOwningTarget().getLocation().getLineNumber() + " ["
                + getOwningTarget().getName() + "]";

        testFile = testFile.substring(getProject().getBaseDir().getPath()
                .length());

        return "WEBTEST-TASK FAILED" + "\n - Java class: "
                + this.getClass().getSimpleName() + "\n - Called by: "
                + testFile;
    }

}
