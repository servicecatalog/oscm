/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

public class DependencyResolverTask extends Task {

    private static final String REF_RESOLVEDPROJECTSPATH = "resolved.projects.path";

    private File workspacedir = null;
    private String[] projects = new String[0];

    public void setWorkspacedir(File workspacedir) {
        this.workspacedir = workspacedir;
    }

    public void setProjects(String projects) {
        projects = projects.trim();
        if (projects == null || projects.length() == 0) {
            this.projects = new String[0];
        } else {
            this.projects = projects.split("[, ]+");
        }
    }

    public void execute() throws BuildException {
        if (workspacedir == null)
            throw new BuildException("No workspace location set.");

        EclipseProjectReader reader = new EclipseProjectReader(workspacedir);

        if (projects.length == 0) {
            projects = reader.getProjectNames();
        }

        List<IEclipseProject> resolved = new ArrayList<IEclipseProject>();
        for (int i = 0; i < projects.length; i++) {
            IEclipseProject project = reader.getDefinition(projects[i]);
            List<IEclipseProject> empty = Collections.emptyList();
            resolveDependencies(project, resolved, empty);
        }
        Path path = new Path(getProject());
        for (Iterator<IEclipseProject> i = resolved.iterator(); i.hasNext();) {
            IEclipseProject project = i.next();
            path.add(new Path(getProject(), project.getLocation().toString()));
        }
        getProject().addReference(REF_RESOLVEDPROJECTSPATH, path);
    }

    private void resolveDependencies(IEclipseProject project,
            List<IEclipseProject> resolved, List<IEclipseProject> stack) {
        stack = new ArrayList<IEclipseProject>(stack);
        stack.add(project);
        if (!resolved.contains(project)) {
            for (IEclipseProject ref : project.getDependencies()) {
                if (stack.contains(ref)) {
                    // reduce the stack to the actual cycle:
                    stack = stack.subList(stack.indexOf(ref), stack.size());
                    throw new BuildException("Dependency Cycle: " + stack);
                }
                resolveDependencies(ref, resolved, stack);
            }
            resolved.add(project);
        }
    }

}
