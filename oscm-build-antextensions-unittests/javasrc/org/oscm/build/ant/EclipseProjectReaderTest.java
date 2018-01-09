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
import org.junit.Before;
import org.junit.Test;

public class EclipseProjectReaderTest {

    protected static final File TESTWORKSPACE = new File(
            "resources/testworkspace");

    protected EclipseProjectReader reader;

    @Before
    public void setUp() {
        reader = new EclipseProjectReader(TESTWORKSPACE);
    }

    protected File platformfile(String path) {
        path = path.replace('/', File.separatorChar);
        return new File(path);
    }

    @Test
    public void testGetProjectNames() {
        String[] actual = reader.getProjectNames();
        String[] expected = new String[] { "project-a", "project-b",
                "project-c", "project-lib" };
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }

    @Test
    public void readProjectLib() {
        // given

        // when
        IEclipseProject p = reader.getDefinition("project-lib");

        // then
        assertEquals("project-lib", p.getName());
        assertEquals("Project[project-lib]", p.toString());

        assertEquals(platformfile("resources/testworkspace/project-lib")
                .getAbsoluteFile(), p.getLocation());
        assertEquals(Collections.emptyList(), p.getDependencies());

        List<File> expectedLibraries = Collections.singletonList(platformfile(
                "resources/testworkspace/project-lib/javalib/samplelib.jar")
                        .getAbsoluteFile());
        assertEquals(expectedLibraries, p.getLibraries());
        assertEquals(expectedLibraries, p.getExportedLibraries());

        assertEquals(Collections.emptyList(), p.getJavaSourceFolders());
    }

    @Test
    public void readProjectA() {
        // given

        // when
        IEclipseProject p = reader.getDefinition("project-a");

        // then
        assertEquals("project-a", p.getName());
        assertEquals("Project[project-a]", p.toString());
        assertEquals(platformfile("resources/testworkspace/project-a")
                .getAbsolutePath(), p.getLocation().toString());
        List<IEclipseProject> dependencies = p.getDependencies();
        assertEquals(1, dependencies.size());
        assertEquals("project-lib", dependencies.get(0).getName());
        dependencies = p.getJavaPathDependencies();
        assertEquals(1, dependencies.size());
        assertEquals("project-lib", dependencies.get(0).getName());
        assertEquals(Collections.emptyList(), p.getLibraries());
        assertEquals(Collections.emptyList(), p.getExportedLibraries());
        assertEquals(platformfile("resources/testworkspace/project-a/javasrc")
                .getAbsoluteFile(), p.getJavaSourceFolders().get(0));
    }

    @Test
    public void readProjectB() {
        // given

        // when
        IEclipseProject p = reader.getDefinition("project-b");

        // then
        assertEquals("project-b", p.getName());
        assertEquals("Project[project-b]", p.toString());
        assertEquals(platformfile("resources/testworkspace/project-b")
                .getAbsolutePath(), p.getLocation().toString());

        List<IEclipseProject> dependencies = p.getDependencies();
        assertEquals(1, dependencies.size());
        assertEquals("project-lib", dependencies.get(0).getName());

        assertEquals(Collections.emptyList(), p.getJavaPathDependencies());
        assertEquals(Collections.emptyList(), p.getLibraries());
        assertEquals(Collections.emptyList(), p.getExportedLibraries());
        assertEquals(Collections.emptyList(), p.getJavaSourceFolders());
    }

    @Test(expected = BuildException.class)
    public void testReadNoExistProject() {
        reader.getDefinition("doesnotexists");
    }

}
