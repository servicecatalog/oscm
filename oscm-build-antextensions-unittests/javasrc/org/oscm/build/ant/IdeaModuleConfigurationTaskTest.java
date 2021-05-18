/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2021
 *
 * <p>Creation Date: 07.05.2021
 *
 * <p>*****************************************************************************
 */
package org.oscm.build.ant;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/** @author goebel */
public class IdeaModuleConfigurationTaskTest {

  IdeaModuleConfigurationTask task;

  @Before
  public void setup() {
    final String root = System.getProperty("user.dir");
    task = new IdeaModuleConfigurationTask();
    task.setDirName("javares");
    task.setFileName("oscm.iml");
    task.setWorkspace(new File(root).getAbsoluteFile().getParentFile());
  }

  @Test
  public void testExecute() {
    task.execute();
  }
}
