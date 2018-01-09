/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reader for Eclipse project definitions in a given workspace. The
 * implementation assumes that all projects reside in the same workspace and
 * caches the description objects. The name of each project must correspond to
 * its folder name.
 * 
 * @author hoffmann
 */
public class EclipseProjectReader {

    private static final String FILE_PROJECT = ".project";
    private static final String FILE_LOCATION = ".location";
    private static final String FILE_CLASSPATH = ".classpath";

    private final File workspace;
    private final Map<File, IEclipseProject> cache;

    private String wsAvailable;

    /**
     * key: projectName, value: path to project
     */
    Map<String, String> projectLocations = null;

    /**
     * Constructs a new reader for the given workspace directory.
     * 
     * @param workspace
     *            workspace directory
     */
    public EclipseProjectReader(File workspace) {
        this.workspace = workspace;
        this.cache = new HashMap<File, IEclipseProject>();
    }

    /**
     * Scans the workspace for projects, i.e. looks for folders containing a
     * <code>.project</code> file.
     * 
     * @return names of all projects in the workspace
     */
    public String[] getProjectNames() {
        try {
            if (projectLocations == null) {
                projectLocations = new HashMap<String, String>();
            }
            List<String> result = new ArrayList<>();
            Collections.addAll(result, scanForLocalProjects());
            Collections.addAll(result, scanForReferencedProjects());
            Collections.addAll(result, scanForProjectsInGitEnvironment());
            String[] arrayResult = new String[result.size()];
            for (int i = 0; i < result.size(); i++) {
                arrayResult[i] = result.get(i);
            }
            Arrays.sort(arrayResult);
            return arrayResult;
        } catch (IOException e) {
            throw new BuildException(".location file could not be read", e);
        }
    }

    private String[] scanForProjectsInGitEnvironment() throws IOException {
        String[] resultArray = new String[0];
        if ("true".equals(wsAvailable)) {
            List<String> result = new ArrayList<>();
            File[] subfolders = workspace.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() && f.exists();
                }
            });
            for (int i = 0; i < subfolders.length; i++) {
                Collections.addAll(result,
                        scanForSubFolder(subfolders[i], FILE_PROJECT));
            }

            resultArray = new String[result.size()];
            for (int i = 0; i < result.size(); i++) {
                resultArray[i] = result.get(i);
            }
        }
        return resultArray;
    }

    private String[] scanForReferencedProjects() throws IOException {
        String[] result = new String[0];
        String wspath = workspace.getAbsolutePath();
        Path path = Paths.get(wspath);
        Path projects = path.resolve(".metadata").resolve(".plugins")
                .resolve("org.eclipse.core.resources").resolve(".projects");
        if (Files.exists(projects)) {
            result = scanForSubFolder(projects.toFile(), FILE_LOCATION);
        }
        return result;
    }

    private String[] scanForLocalProjects() throws IOException {
        String[] names = scanForSubFolder(workspace, FILE_PROJECT);
        return names;
    }

    private String[] scanForSubFolder(File folder, final String condition)
            throws IOException {
        File[] folders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && new File(f, condition).exists();
            }
        });
        String[] names = new String[folders.length];
        for (int i = 0; i < folders.length; i++) {
            storeProjectLocation(folders[i], condition);
            names[i] = folders[i].getName();
        }
        return names;
    }

    private void storeProjectLocation(final File project,
            final String condition) throws IOException {
        switch (condition) {
        case FILE_PROJECT:
            projectLocations.put(project.getName(), project.getAbsolutePath());
            break;
        case FILE_LOCATION:
            String location = readFromLocationFile(project);
            projectLocations.put(project.getName(), location);
            break;
        }
    }

    /**
     * Reads the path to the project from the Eclipse' .location meta data file.
     * This file can be found in
     * 'workspace\.metadata\.plugins\org.eclipse.core.resources\.projects\
     * PROJECTNAME\.lo c a t i o n ' .<br />
     * The .location files is written with a special Outpustream and read with
     * an special InputStream implementation. You can find them in the eclipse
     * project org.eclipse.core.resource. The class names are<br />
     * org.eclipse.core.internal.localstore.SafeChunkyInputStream and <br />
     * org.eclipse.core.internal.localstore.SafeChunkyOutputStream.
     * <p>
     * The eclipse implementation which reads the .location file can be found in
     * the same project, the class name is
     * org.eclipse.core.internal.resources.LocalMetaArea, refer to method
     * readPrivateDescription.
     * <p>
     * Basically the first 16 bytes of the .location file are for consistency
     * reason there, the next bytes are the path to the project source. Those
     * bytes must be read via DataInputStream.readUTF.
     */
    private String readFromLocationFile(File project) throws IOException {
        String result = "";
        Path location = Paths.get(project.getAbsolutePath())
                .resolve(".location");
        InputStream inputStream = Files.newInputStream(location);
        try (DataInputStream dataStream = new DataInputStream(inputStream);) {
            byte[] begin_chunk = new byte[16];
            dataStream.read(begin_chunk, 0, 16);
            result = dataStream.readUTF();

            String uriPrefix = "URI//file:";
            if (System.getProperty("os.name").startsWith("Windows")) {
                uriPrefix = uriPrefix.concat("/");
            }

            if (result.startsWith(uriPrefix)) {
                result = result.substring(uriPrefix.length());
            }
        }
        return result;
    }

    /**
     * Get the definition of the project with the given name in this workspace.
     * 
     * @param project
     *            name name of the project
     * @return project definition
     */
    public IEclipseProject getDefinition(String projectname) {
        if (projectLocations == null) {
            projectLocations = new HashMap<String, String>();
            getProjectNames();
        }

        String projectLocation = projectLocations.get(projectname);
        if (projectLocation == null) {
            throw new BuildException(
                    "Couldn't find folder of project '" + projectname + "'.");
        }
        File location = Paths.get(projectLocation).toFile();
        return getDefinition(location);
    }

    /**
     * Get the definition of the project at the given location.
     * 
     * @param location
     *            location of the project
     * @return project definition
     */
    public IEclipseProject getDefinition(File location) {
        if (projectLocations == null) {
            projectLocations = new HashMap<String, String>();
            getProjectNames();
        }
        IEclipseProject definition = cache.get(location);
        if (definition == null) {
            definition = readDefinition(location);
            cache.put(location, definition);
        }
        return definition;
    }

    private IEclipseProject readDefinition(File location) {
        ProjectImpl definition = new ProjectImpl(location);
        readProjectFile(definition);
        readClasspathFile(definition);
        return definition;
    }

    private void readProjectFile(ProjectImpl definition) {
        Document doc = parse(new File(definition.getLocation(), FILE_PROJECT));
        Node namenode = doc.getElementsByTagName("name").item(0);
        definition.name = namenode.getFirstChild().getNodeValue();
        NodeList projectnodes = doc.getElementsByTagName("project");
        for (int i = 0; i < projectnodes.getLength(); i++) {
            Node name = projectnodes.item(i).getFirstChild();
            definition.dependencies.add(name.getNodeValue());
        }
    }

    private void readClasspathFile(ProjectImpl definition) {
        File classpathfile = new File(definition.getLocation(), FILE_CLASSPATH);
        // May not exist for non Java projects
        if (!classpathfile.exists()) {
            return;
        }
        Document doc = parse(classpathfile);
        NodeList entries = doc.getElementsByTagName("classpathentry");
        for (int i = 0; i < entries.getLength(); i++) {
            NamedNodeMap attrs = entries.item(i).getAttributes();
            String path = attrs.getNamedItem("path").getNodeValue();
            String kind = attrs.getNamedItem("kind").getNodeValue();
            File location = getLocation(definition.getLocation(), path, kind);
            if (kind.equals("lib")) {
                definition.libraries.add(location);
                Node exattr = attrs.getNamedItem("exported");
                if (exattr != null && "true".equals(exattr.getNodeValue())) {
                    definition.exportedlibraries.add(location);
                }
            }
            if (kind.equals("src")) {
                if (path.startsWith("/")) {
                    // Java project reference
                    String ref = path.substring(1);
                    definition.dependencies.add(ref);
                    definition.javapathdependencies.add(ref);
                } else {
                    // local source folder
                    definition.javasourcefolders.add(location);
                }
            }
        }
    }

    /**
     * Calculates the absolute location for a path specified within the given
     * project. If the given path starts with / the location is relative to the
     * workspace, of not it is relative to the project directory.
     * 
     * @param projectname
     *            name of the project
     * @param path
     *            local classpath path
     * @return absolute location
     */
    private File getLocation(File projectpath, String path, String kind) {
        if (path.startsWith("/")) {
            if ("src".equals(kind)) {
                return new File(projectLocations.get(path.substring(1)));
            }
            int i = path.indexOf("/", 1);
            String location = projectLocations.get(path.substring(1, i))
                    + path.substring(i);
            return new File(location);
        } else {
            return new File(projectpath, path);
        }
    }

    private Document parse(File file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException ex) {
            throw new BuildException(ex);
        } catch (SAXException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException("Error while reading project definition, "
                    + "additional checkout might be required (" + file + ").",
                    ex);
        }
    }

    private class ProjectImpl implements IEclipseProject {

        private final File location;
        private String name;
        final List<String> dependencies = new ArrayList<String>();
        final List<String> javapathdependencies = new ArrayList<String>();
        final List<File> javasourcefolders = new ArrayList<File>();
        final List<File> libraries = new ArrayList<File>();
        final List<File> exportedlibraries = new ArrayList<File>();

        ProjectImpl(File location) {
            this.location = location;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public File getLocation() {
            return location;
        }

        private List<IEclipseProject> getProjects(List<String> names,
                IEclipseProject referent) {

            List<IEclipseProject> projects = new ArrayList<IEclipseProject>(
                    names.size());
            try {
                for (String name : names) {
                    projects.add(getDefinition(name));
                }
            } catch (Exception be) {
                String msg = String.format(
                        "Error while reading dependencies of project %s: %s",
                        referent.getName(), be.getMessage());
                throw new BuildException(msg, be);
            }
            return projects;
        }

        @Override
        public List<IEclipseProject> getDependencies() {
            return getProjects(dependencies, this);
        }

        @Override
        public List<IEclipseProject> getJavaPathDependencies() {
            return getProjects(javapathdependencies, this);
        }

        @Override
        public List<File> getLibraries() {
            return Collections.unmodifiableList(libraries);
        }

        @Override
        public List<File> getExportedLibraries() {
            return Collections.unmodifiableList(exportedlibraries);
        }

        @Override
        public List<File> getJavaSourceFolders() {
            return Collections.unmodifiableList(javasourcefolders);
        }

        @Override
        public String toString() {
            return "Project[" + name + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IEclipseProject) {
                return name.equals(((IEclipseProject) obj).getName());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

    }

    public void setWsAvailability(String wsAvailable) {
        this.wsAvailable = wsAvailable;
    }
}
