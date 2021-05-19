/*x********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2021
 *
 * Creation Date: 07.05.2021
 *
 *******************************************************************************x*/

package org.oscm.build.ant;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/** @author goebel */
public class IdeaModuleConfigurationTaskTest {

  IdeaModuleConfigurationTask task;
  private File metaFileDir;

  @Before
  public void setup() throws Exception {
    final String root = System.getProperty("user.dir");
    metaFileDir = new File(root + "/oscm-build-antextensions-unittests/javares");
    File ws = new File(root);
    if (!metaFileDir.exists()) {
      metaFileDir = new File(root + "/javares");
      ws = new File(root).getParentFile();
    }
    task = new IdeaModuleConfigurationTask();
    task.setDirName(metaFileDir.getPath());
    task.setFileName("oscm.iml");
    task.setWorkspace(ws);
  }

  @Test
  public void testExecute() throws IOException {
    // when
    task.execute();

    // then
    assertSrcReferences(new File(metaFileDir, "oscm.iml"));
  }

  void assertSrcReferences(File out) throws IOException {
    assertTrue(out.exists());
    String result = FileUtils.readFileToString(out);

    // then expect source references of this project
    assertTrue(result.contains("/oscm-build-antextensions-unittests/javares"));
    assertTrue(result.contains("/oscm-build-antextensions-unittests/javasrc"));
  }
}
