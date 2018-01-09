/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;

public class ResourcePackageTaskTest {

    protected static final File OUTPUTDIR = new File("result");
    protected static final File PACKAGEFILE = new File("package.properties");

    Project project;
    ResourcePackageTask task;

    @Before
    public void setUp() {
        project = new Project();
        task = new ResourcePackageTask();
        task.setProject(project);
    }

    @Test(expected = BuildException.class)
    public void testPackageFileNotSet() {
        task.setOutputdir(OUTPUTDIR);
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void testOutputDirNotSet() {
        task.setPackagefile(PACKAGEFILE);
        task.execute();
    }

}
