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
    logger.info(
        "TEST STARTED :" + description.getClassName() + " - " + description.getMethodName());
  }

  @Override
  protected void succeeded(Description description) {
    super.succeeded(description);
    logger.info(
        "TEST SUCCESSFUL :" + description.getClassName() + " - " + description.getMethodName() + "\n");
  }

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);
    logger.error(
        "TEST FAILURE :" + description.getClassName() + " - " + description.getMethodName());
  }
}
