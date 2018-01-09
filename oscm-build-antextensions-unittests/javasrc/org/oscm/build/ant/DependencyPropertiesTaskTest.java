/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Test;

public class DependencyPropertiesTaskTest {

    protected static final File TESTWORKSPACE = new File(
            "resources/testworkspace");
    protected static final File RESULTDIR = new File("resources/result");
    protected static final File PROJECT_A_DIR = new File(
            "resources/testworkspace/project-a");
    protected static final File PROJECT_C_DIR = new File(
            "resources/testworkspace/project-c");

    private Project project;
    private DependencyPropertiesTask task;

    @Before
    public void setUp() {
        project = new Project();
        task = new DependencyPropertiesTask();
        task.setProject(project);
    }

    @Test(expected = BuildException.class)
    public void testWorkspaceNotSet() {
        task.setProjectdir(PROJECT_A_DIR);
        task.setResultdir(RESULTDIR);
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void testProjectDirNotSet() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setResultdir(RESULTDIR);
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void testResultDirNotSet() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjectdir(PROJECT_A_DIR);
        task.execute();
    }

    @Test
    public void testProjectProperties() {
        // given
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjectdir(PROJECT_A_DIR);
        task.setResultdir(RESULTDIR);

        // when
        task.execute();

        // then
        assertEquals("project-a", project.getProperty("project.name"));
        assertEquals(
                platformfile("resources/testworkspace/project-lib")
                        .getAbsoluteFile().toString(),
                project.getProperty("project.project-lib.dir"));
        assertEquals(ospath("resources/result/work/project-lib"),
                project.getProperty("result.work.project-lib.dir"));
        assertEquals(ospath("resources/result/package/project-lib"),
                project.getProperty("result.package.project-lib.dir"));
    }

    @Test
    public void testCompilePathProjectA() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjectdir(PROJECT_A_DIR);
        task.setResultdir(RESULTDIR);
        task.execute();
        Path path = (Path) project.getReference("dependencies.compile.path");
        List<String> expected = Arrays.asList(absospath(
                "resources/testworkspace/project-lib/javalib/samplelib.jar"));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testCompilePathProjectC() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjectdir(PROJECT_C_DIR);
        task.setResultdir(RESULTDIR);
        task.execute();
        Path path = (Path) project.getReference("dependencies.compile.path");
        List<String> expected = Collections.singletonList(
                absospath("resources/result/work/project-a/classes"));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testRuntimePathProjectC() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjectdir(PROJECT_C_DIR);
        task.setResultdir(RESULTDIR);
        task.execute();
        Path path = (Path) project.getReference("dependencies.runtime.path");
        List<String> expected = Arrays.asList(
                absospath("resources/result/work/project-a/classes"), absospath(
                        "resources/testworkspace/project-lib/javalib/samplelib.jar"));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    private String ospath(String path) {
        return path.replace('/', File.separatorChar);
    }

    private String absospath(String path) {
        return new File(path).getAbsolutePath();
    }

    protected File platformfile(String path) {
        path = path.replace('/', File.separatorChar);
        return new File(path);
    }

}
