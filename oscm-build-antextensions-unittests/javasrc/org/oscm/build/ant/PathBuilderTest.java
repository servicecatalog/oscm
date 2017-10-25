/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PathBuilder}.
 * 
 * @author hoffmann
 */
public class PathBuilderTest {

    private static final File WORKDIR = new File("resources/result/work");

    private static final String LIB = "resources/testworkspace/project-lib/javalib/samplelib.jar";

    private Project antProject;

    @Before
    public void setup() {
        antProject = new Project();
    }

    @Test
    public void testGetCompilePathNoDependencies() {
        final ProjectStub projectA = new ProjectStub("project-a");
        Path path = createBuilder(projectA).getCompilePath();
        assertEquals(Collections.emptyList(), Arrays.asList(path.list()));
    }

    @Test
    public void testGetCompilePathWithInternalLibrary() {
        final ProjectStub projectA = new ProjectStub("project-a");
        projectA.addLibraries(LIB);
        Path path = createBuilder(projectA).getCompilePath();
        assertEquals(Collections.singletonList(absospath(LIB)),
                Arrays.asList(path.list()));
    }

    @Test(expected = BuildException.class)
    public void testGetCompilePathNonExistingLibrary() {
        final ProjectStub projectA = new ProjectStub("project-a");
        projectA.addLibraries("doesnotexists.jar");
        createBuilder(projectA).getCompilePath();
    }

    @Test
    public void testGetCompileClassDependencies() {
        final ProjectStub projectB = new ProjectStub("project-b");
        final ProjectStub projectA = new ProjectStub("project-a");
        projectB.addJavaPathDependencies(projectA);
        Path path = createBuilder(projectB).getCompilePath();
        List<String> expected = Collections
                .singletonList(absospath("resources/result/work/project-a/classes"));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testGetCompileClassDependenciesWithoutResAndSrc() {
        final ProjectStub projectA = new ProjectStub("project-a");
        final ProjectStub projectB = new ProjectStub("project-b");
        projectA.addJavaPathDependencies(projectB);
        Path path = createBuilder(projectA).getCompilePath();
        assertEquals(Collections.emptyList(), Arrays.asList(path.list()));
    }

    @Test
    public void testGetCompileClassDependenciesWithoutExportedLibrary() {
        final ProjectStub projectA = new ProjectStub("project-a");
        final ProjectStub projectB = new ProjectStub("project-b");
        projectB.addLibraries(LIB);
        projectA.addJavaPathDependencies(projectB);
        Path path = createBuilder(projectA).getCompilePath();
        assertEquals(Collections.emptyList(), Arrays.asList(path.list()));
    }

    @Test
    public void testGetCompileClassDependenciesWithExportedLibrary() {
        final ProjectStub projectA = new ProjectStub("project-a");
        final ProjectStub projectB = new ProjectStub("project-b");
        projectB.addExportedLibraries(LIB);
        projectA.addJavaPathDependencies(projectB);
        Path path = createBuilder(projectA).getCompilePath();
        List<String> expected = Collections.singletonList(absospath(LIB));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testGetCompileClassDependenciesNoDuplicates() {
        final ProjectStub projectA = new ProjectStub("project-a");
        projectA.addExportedLibraries(LIB);
        final ProjectStub projectB = new ProjectStub("project-b");
        projectB.addExportedLibraries(LIB);
        projectA.addJavaPathDependencies(projectB);
        Path path = createBuilder(projectA).getCompilePath();
        List<String> expected = Collections.singletonList(absospath(LIB));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testGetRuntimePathNoDependencies() {
        final ProjectStub projectA = new ProjectStub("project-a");
        projectA.addLibraries(LIB);
        Path path = createBuilder(projectA).getRuntimePath();
        List<String> expected = Arrays.asList(
                absospath("resources/result/work/project-a/classes"),
                absospath(LIB));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testGetRuntimePathWithDependencies() {
        final ProjectStub projectA = new ProjectStub("project-a");
        final ProjectStub projectC = new ProjectStub("project-c");
        projectC.addLibraries(LIB);
        projectA.addJavaPathDependencies(projectC);
        Path path = createBuilder(projectA).getRuntimePath();
        List<String> expected = Arrays.asList(
                absospath("resources/result/work/project-a/classes"),
                absospath(LIB));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    @Test
    public void testGetRuntimePathNoDuplicates() {
        final ProjectStub projectA = new ProjectStub("project-a");
        projectA.addLibraries(LIB);
        final ProjectStub projectB = new ProjectStub("project-b");
        projectB.addLibraries(LIB);
        projectA.addJavaPathDependencies(projectB);
        Path path = createBuilder(projectA).getRuntimePath();
        List<String> expected = Arrays.asList(
                absospath("resources/result/work/project-a/classes"),
                absospath(LIB));
        assertEquals(expected, Arrays.asList(path.list()));
    }

    private static class ProjectStub implements IEclipseProject {

        private final String name;

        private final List<IEclipseProject> javaPathDependencies = new ArrayList<IEclipseProject>();

        private final List<File> libraries = new ArrayList<File>();

        private final List<File> exportedlibraries = new ArrayList<File>();

        ProjectStub(String name) {
            this.name = name;
        }

        void addJavaPathDependencies(IEclipseProject... projects) {
            javaPathDependencies.addAll(Arrays.asList(projects));
        }

        void addLibraries(String... files) {
            for (String f : files) {
                libraries.add(new File(f));
            }
        }

        void addExportedLibraries(String... files) {
            for (String f : files) {
                libraries.add(new File(f));
                exportedlibraries.add(new File(f));
            }
        }

        // === IEclipseProject ===

        public String getName() {
            return name;
        }

        public File getLocation() {
            File workspace = new File("resources/testworkspace/");
            return new File(workspace, name);
        }

        public List<IEclipseProject> getJavaPathDependencies() {
            return javaPathDependencies;
        }

        public List<File> getLibraries() {
            return libraries;
        }

        public List<File> getExportedLibraries() {
            return exportedlibraries;
        }

        public List<File> getJavaSourceFolders() {
            throw new NotImplementedException("");
        }

        public List<IEclipseProject> getDependencies() {
            throw new NotImplementedException("");
        }

    }

    private PathBuilder createBuilder(IEclipseProject p) {
        return new PathBuilder(antProject, p, WORKDIR);
    }

    private String absospath(String path) {
        return new File(path).getAbsolutePath();
    }

}
