/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Test;

public class DependencyResolverTaskTest {

    protected static final File TESTWORKSPACE = new File(
            "resources/testworkspace");

    private Project project;

    private DependencyResolverTask task;

    @Before
    public void setUp() {
        project = new Project();
        task = new DependencyResolverTask();
        task.setProject(project);
    }

    @Test(expected = BuildException.class)
    public void testWorkspaceNotSet() {
        task.setProjects("a,b");
        task.execute();
    }

    @Test
    public void testProjectsNotSet() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjects("");
        task.execute();
        Path path = (Path) project.getReference("resolved.projects.path");
        String[] entries = path.list();
        assertEquals(4, entries.length);
        assertEquals(new File("resources/testworkspace/project-lib")
                .getAbsolutePath(), entries[0]);
        assertEquals(new File("resources/testworkspace/project-a")
                .getAbsolutePath(), entries[1]);
        assertEquals(new File("resources/testworkspace/project-b")
                .getAbsolutePath(), entries[2]);
        assertEquals(new File("resources/testworkspace/project-c")
                .getAbsolutePath(), entries[3]);
    }

    @Test
    public void testProjectLib() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjects("project-lib");
        task.execute();
        Path path = (Path) project.getReference("resolved.projects.path");
        String[] entries = path.list();
        assertEquals(1, entries.length);
        assertEquals(new File("resources/testworkspace/project-lib")
                .getAbsolutePath(), entries[0]);
    }

    @Test
    public void testProjectA() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjects("project-a");
        task.execute();
        Path path = (Path) project.getReference("resolved.projects.path");
        String[] entries = path.list();
        assertEquals(2, entries.length);
        assertEquals(new File("resources/testworkspace/project-lib")
                .getAbsolutePath(), entries[0]);
        assertEquals(new File("resources/testworkspace/project-a")
                .getAbsolutePath(), entries[1]);
    }

    @Test
    public void testProjectAandLib() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjects("project-a, project-lib");
        task.execute();
        Path path = (Path) project.getReference("resolved.projects.path");
        String[] entries = path.list();
        assertEquals(2, entries.length);
        assertEquals(new File("resources/testworkspace/project-lib")
                .getAbsolutePath(), entries[0]);
        assertEquals(new File("resources/testworkspace/project-a")
                .getAbsolutePath(), entries[1]);
    }

    @Test
    public void testProjectBandA() {
        task.setWorkspacedir(TESTWORKSPACE);
        task.setProjects("project-b, project-a");
        task.execute();
        Path path = (Path) project.getReference("resolved.projects.path");
        String[] entries = path.list();
        assertEquals(3, entries.length);
        assertEquals(new File("resources/testworkspace/project-lib")
                .getAbsolutePath(), entries[0]);
        assertEquals(new File("resources/testworkspace/project-b")
                .getAbsolutePath(), entries[1]);
        assertEquals(new File("resources/testworkspace/project-a")
                .getAbsolutePath(), entries[2]);
    }

}
