/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class DependencyPropertiesTask extends Task {

    private static final String PROP_PROJECTNAME = "project.name";
    private static final String PROP_PROJECT_PREFIX = "project.";
    private static final String PROP_WORK_PREFIX = "result.work.";
    private static final String PROP_PACKAGE_PREFIX = "result.package.";
    private static final String PROP_DIR_SUFFIX = ".dir";

    private static final String REF_COMPILEPATH = "dependencies.compile.path";
    private static final String REF_RUNTIMEPATH = "dependencies.runtime.path";

    private static final String ESS_PREFIX = "ess-";
    private static final String OSCM_PREFIX = "oscm-";
    private static final String SEPARATOR = File.separator;
    private static final String OSCM_BUILD = "oscm-build";
    private static final String ESS_BUILD = "ess-oscm-build";

    private File workspacedir;
    private File workdir;
    private File packagedir;
    private File projectdir;

    public void setWorkspacedir(File sourcedir) {
        this.workspacedir = sourcedir;
    }

    public void setResultdir(File resultdir) {
        this.workdir = new File(resultdir, "work");
        this.packagedir = new File(resultdir, "package");
    }

    public void setProjectdir(File projectdir) {
        this.projectdir = projectdir;
    }

    @Override
    public void execute() throws BuildException {
        if (workspacedir == null) {
            throw new BuildException("No workspace location set.");
        }
        if (projectdir == null) {
            throw new BuildException("No project location set.");
        }
        if (workdir == null) {
            throw new BuildException("No result location set.");
        }

        EclipseProjectReader reader = new EclipseProjectReader(workspacedir);
        IEclipseProject project = reader.getDefinition(projectdir);

        PathBuilder builder = new PathBuilder(getProject(), project, workdir);
        getProject().addReference(REF_COMPILEPATH, builder.getCompilePath());
        getProject().addReference(REF_RUNTIMEPATH, builder.getRuntimePath());

        createProperties(project);
    }

    private void createProperties(IEclipseProject project) {
        Project p = getProject();

        p.setProperty(PROP_PROJECTNAME, project.getName());

        // Variables for all dependent projects directories
        for (IEclipseProject d : project.getDependencies()) {
            String name = d.getName();
            p.setProperty(PROP_PROJECT_PREFIX + name + PROP_DIR_SUFFIX, d
                    .getLocation().toString());
            p.setProperty(PROP_WORK_PREFIX + name + PROP_DIR_SUFFIX,
                    getWorkDir(name).toString());
            p.setProperty(PROP_PACKAGE_PREFIX + name + PROP_DIR_SUFFIX,
                    getPackageDir(name).toString());

            name = ESS_PREFIX + name;

            p.setProperty(
                    PROP_PROJECT_PREFIX + name + PROP_DIR_SUFFIX,
                    d.getLocation()
                            .toString()
                            .replace(SEPARATOR + OSCM_PREFIX,
                                    SEPARATOR + ESS_PREFIX + OSCM_PREFIX)
                            .replace(ESS_BUILD, OSCM_BUILD));
            p.setProperty(
                    PROP_WORK_PREFIX + name + PROP_DIR_SUFFIX,
                    getWorkDir(name)
                            .toString()
                            .replace(SEPARATOR + OSCM_PREFIX,
                                    SEPARATOR + ESS_PREFIX + OSCM_PREFIX)
                            .replace(ESS_BUILD, OSCM_BUILD));
            p.setProperty(
                    PROP_PACKAGE_PREFIX + name + PROP_DIR_SUFFIX,
                    getPackageDir(name)
                            .toString()
                            .replace(SEPARATOR + OSCM_PREFIX,
                                    SEPARATOR + ESS_PREFIX + OSCM_PREFIX)
                            .replace(ESS_BUILD, OSCM_BUILD));
        }
    }

    private File getWorkDir(String projectname) {
        return new File(workdir, projectname);
    }

    private File getPackageDir(String projectname) {
        return new File(packagedir, projectname);
    }

}
