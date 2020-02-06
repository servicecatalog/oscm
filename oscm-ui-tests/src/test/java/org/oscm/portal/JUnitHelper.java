/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 16 07, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.portal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class JUnitHelper extends TestWatcher {
    public static final Logger logger = LogManager.getLogger(JUnitHelper.class.getName());

    @Override
    protected void starting(Description description) {
        super.starting(description);
        System.out.println("\n");
        logger.info(
                new StringBuilder()
                        .append("TEST STARTED :")
                        .append(description.getClassName())
                        .append(" - ")
                        .append(description.getMethodName()).toString());
    }

    @Override
    protected void succeeded(Description description) {
        super.succeeded(description);
        logger.info(
                new StringBuilder()
                        .append("TEST SUCCESSFUL :")
                        .append(description.getClassName())
                        .append(" - ")
                        .append(description.getMethodName()).toString());
    }

    @Override
    protected void failed(Throwable e, Description description) {
        super.failed(e, description);
        logger.error(
                new StringBuilder()
                        .append("TEST FAILURE :")
                        .append(description.getClassName())
                        .append(" - ")
                        .append(description.getMethodName()).toString());
    }
}
