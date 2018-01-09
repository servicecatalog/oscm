/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Creates class paths for a given Eclipse project.
 * 
 * @author hoffmann
 */
public class PathBuilder {

    private final Project antProject;

    private final IEclipseProject eclipseProject;

    private final File workdir;

    /**
     * Creates a new builder for the given project.
     * 
     * @param eclipseProject
     */
    public PathBuilder(Project antProject, IEclipseProject eclipseProject,
            File workdir) {
        this.antProject = antProject;
        this.eclipseProject = eclipseProject;
        this.workdir = workdir;
    }

    /**
     * Creates the compile path for this project which is the libraries of this
     * project, all compiled class files of dependent projects and the exported
     * libraries of the dependent projects.
     * 
     * @return compile path for this project
     */
    public Path getCompilePath() {
        Path path = new Path(antProject);
        addLibraries(path, eclipseProject.getLibraries());
        for (IEclipseProject d : eclipseProject.getJavaPathDependencies()) {
            addClasses(path, d);
            addLibraries(path, d.getExportedLibraries());
        }
        return path;
    }

    /**
     * Creates the runtime path of this project which is the class files and all
     * libraries of this project as well as all recursively dependet projects.
     * 
     * @return runtime path of this project
     */
    public Path getRuntimePath() {
        Path path = new Path(antProject);
        addRuntimePath(path, eclipseProject);
        return path;
    }

    private void addRuntimePath(Path path, IEclipseProject p) {
        addClasses(path, p);
        addLibraries(path, p.getLibraries());
        for (IEclipseProject d : p.getJavaPathDependencies()) {
            addRuntimePath(path, d);
        }
    }

    private void addClasses(Path path, IEclipseProject project) {
        File javasrc = new File(project.getLocation(), "javasrc");
        if (javasrc.exists()) {
            // Add class files only if this project has Java sources
            File projectWorkDir = new File(workdir, project.getName());
            path.setLocation(new File(projectWorkDir, "classes"));
        }
    }

    private void addLibraries(Path path, List<File> libraries) {
        for (File l : libraries) {
            addLibrary(path, l);
        }
    }

    private void addLibrary(Path path, File library) {
        if (!library.exists()) {
            throw new BuildException("Class path entry does not exist: "
                    + library);
        }
        path.setLocation(library);
    }
}
