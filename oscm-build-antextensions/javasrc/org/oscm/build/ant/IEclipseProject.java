/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.util.List;

/**
 * Description of the Meta data provided by Eclipse project files
 * <code>.project</code> and <code>.classpath</code>.
 * 
 * @author hoffmann
 */
public interface IEclipseProject {

    /**
     * Returns the name of the project.
     * 
     * @return project name
     */
    public String getName();

    /**
     * Returns the project's folder location.
     * 
     * @return project location
     */
    public File getLocation();

    /**
     * Returns the descriptions of all dependent projects, including
     * dependencies coming from class path references.
     * 
     * @return dependent projects
     */
    public List<IEclipseProject> getDependencies();

    /**
     * Returns the descriptions of the dependent projects which are declared to
     * be part of the Java class path of this project.
     * 
     * @return dependent projects
     */
    public List<IEclipseProject> getJavaPathDependencies();

    /**
     * Returns the list of all Java source folders defined by the project.
     * 
     * @return Java source folders
     */
    public List<File> getJavaSourceFolders();

    /**
     * Returns the list of all libraries defined by the classpath of this
     * project.
     * 
     * @return all libraries of this project
     */
    public List<File> getLibraries();

    /**
     * Returns the list of libraries exported to other projects as defined by
     * the class path of this project.
     * 
     * @return exported libraries of this project
     */
    public List<File> getExportedLibraries();

}
