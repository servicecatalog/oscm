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
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/** @author goebel */
public class IdeaModuleConfigurationTaskTest {

  IdeaModuleConfigurationTask task;
  private File metaFileDir;

  @Before
  public void setup() throws Exception {
    final String root = System.getProperty("user.dir");
    metaFileDir = new File(root + "/oscm-build-antextensions-unittests/javares");
    task = new IdeaModuleConfigurationTask();
    task.setDirName(metaFileDir.getPath());
    task.setFileName("oscm.iml");
    task.setWorkspace(new File(root));
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

    // then expect source references for a maven project
    assertTrue(result.contains("/oscm-ui-tests/src/main"));
    assertTrue(result.contains("/oscm-ui-tests/src/test"));
  }
}
