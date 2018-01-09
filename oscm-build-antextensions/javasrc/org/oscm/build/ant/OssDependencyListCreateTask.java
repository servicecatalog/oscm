/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Jul 29, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 1, 2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.oscm.build.ant.Files.IFile;
import org.oscm.build.ant.Files.PhysicalFileSet;

/**
 * Task to create the list of OSS dependencies. This class is used as a tool,
 * and the result can show the relationship between OSS and the project using
 * these OSS.
 * 
 * @author tokoda
 * 
 */
public class OssDependencyListCreateTask extends Task {

    private static final String INDENT1 = "    ";
    private static final String INDENT2 = "        ";
    private static final String FILE_PACKAGE_PROPERTIES = "package.properties";

    private boolean remainTemporaryLicenseFiles = false;
    private File workspace = null;
    private File licensesDir = null;
    private File outputDir = null;
    private String outputFileName = null;

    private PrintWriter pw = null;
    private EclipseProjectReader projectReader = null;

    public void setRemainTemporaryLicenseFiles(
            boolean remainTemporaryLicenseFiles) {
        this.remainTemporaryLicenseFiles = remainTemporaryLicenseFiles;
    }

    public void setWorkspace(File file) {
        workspace = file;
    }

    public void setLicensesDir(File file) {
        licensesDir = file;
    }

    public void setOutputDir(File file) {
        outputDir = file;
    }

    public void setOutputFileName(String fileName) {
        outputFileName = fileName;
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (workspace == null)
                throw new BuildException("No workspace location set.");
            if (licensesDir == null)
                throw new BuildException("No licences location set.");
            if (outputDir == null)
                throw new BuildException("No output location set.");
            if (outputFileName == null)
                throw new BuildException("No output file name set.");

            // setup
            projectReader = new EclipseProjectReader(workspace);
            prepareOutputDir();
            preparePrintWriter();

            // process
            extractLicenseFiles();
            processLicenses();

        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            if (pw != null) {
                pw.close();
                pw = null;
            }
        }
    }

    private void prepareOutputDir() throws BuildException, IOException {
        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) {
                throw new BuildException("Create directory failed.");
            }
        } else if (!outputDir.isDirectory()) {
            throw new BuildException(
                    "The output directory couldn't be created because there is a file which has same name.");
        } else {
            Collection<Files.IFile> iFiles = new Files.PhysicalFileSet(
                    outputDir, "*").getFiles();
            for (Files.IFile iFile : iFiles) {
                if (!deleteFile(outputDir.getCanonicalPath(),
                        iFile.getLocalPath())) {
                    String message = String
                            .format("Initialize the output directory failed because the file '%s' could't be deleted.",
                                    iFile.getLocalPath());
                    throw new BuildException(message);
                }
            }

            if (!outputDir.delete()) {
                throw new BuildException(
                        "The output directory couldn't be created.");
            }
            outputDir.mkdir();
        }
    }

    private boolean deleteFile(String directory, String fileLocalName) {
        boolean result = false;
        File file = new File(directory, fileLocalName);
        result = file.delete();
        file = null;
        return result;
    }

    private void preparePrintWriter() throws IOException {
        File outputFile = new File(outputDir, outputFileName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    }

    private void extractLicenseFiles() throws FileNotFoundException,
            IOException {

        File[] licenseDirForPackages = licensesDir.listFiles();
        for (File licenseDirForPackage : licenseDirForPackages) {
            Collection<IFile> fileSet = new PhysicalFileSet(
                    licenseDirForPackage, "*.txt").getFiles();

            for (IFile file : fileSet) {
                final File outputFile = new File(outputDir.getCanonicalPath(),
                        file.getLocalPath());
                OutputStream out = null;
                try {
                    out = new FileOutputStream(outputFile);
                    file.writeTo(out);
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        }
    }

    private void processLicenses() throws IOException {

        Collection<IFile> licenses = new PhysicalFileSet(outputDir, "*.txt")
                .getFiles();
        for (IFile license : licenses) {
            if (!license.getLocalPath().equals(outputFileName)) {
                processLicenseFile(license);

                if (!remainTemporaryLicenseFiles) {
                    File licenseActualFile = new File(outputDir,
                            license.getLocalPath());
                    licenseActualFile.delete();
                }
            }
        }
    }

    private void processLicenseFile(IFile licenseFile) throws IOException {
        String actualOssName = extractActualOssName(licenseFile.getLocalPath());
        pw.println(licenseFile.getLocalPath() + "  [" + actualOssName + "]");

        pw.println(INDENT1 + "classpath:");
        List<String> dependencyList = createDependenciesList(actualOssName);
        for (String dependency : dependencyList) {
            pw.println(INDENT2 + dependency);
        }

        pw.println(INDENT1 + "package.properties:");
        List<String> packagedInfoList = createPackagedInfoList(actualOssName);
        for (String packagedInfo : packagedInfoList) {
            pw.println(INDENT2 + packagedInfo);
        }
        pw.println("");
    }

    private String extractActualOssName(String licenceFileName) {
        String filteredName = licenceFileName;
        filteredName = filteredName.replaceFirst("-license.txt", "");
        if (filteredName.indexOf("-LICENSE.txt") > 0) {
            filteredName = filteredName.replaceFirst("-LICENSE.txt", "");
            filteredName = filteredName.substring(filteredName.indexOf("-lib-")
                    + "-lib-".length());
        }

        return filteredName;
    }

    private List<String> createDependenciesList(String filterWord) {
        List<String> dependenciesList = new ArrayList<String>();
        String[] projectNames = projectReader.getProjectNames();
        for (String projectName : projectNames) {
            IEclipseProject project = projectReader.getDefinition(projectName);

            if (containDependencyProject(project.getJavaPathDependencies(),
                    filterWord)) {
                dependenciesList.add(projectName + " (src)");
            } else if (containDependencySource(project.getJavaSourceFolders(),
                    filterWord)) {
                dependenciesList.add(projectName + " (src-local)");
            } else if (containDependencySource(project.getLibraries(),
                    filterWord)) {
                dependenciesList.add(projectName + " (lib)");
            }

        }
        return dependenciesList;
    }

    private boolean containDependencyProject(
            List<IEclipseProject> dependencies, String projectName) {
        for (IEclipseProject dependency : dependencies) {
            if (dependency.getName().indexOf(projectName) >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean containDependencySource(List<File> projects,
            String projectName) {
        for (File project : projects) {
            if (project.getName().indexOf(projectName) >= 0) {
                return true;
            }
        }
        return false;
    }

    private List<String> createPackagedInfoList(String filterWord)
            throws IOException {
        List<String> packagedInfoList = new ArrayList<String>();
        String[] projectNames = projectReader.getProjectNames();
        for (String projectName : projectNames) {
            IEclipseProject project = projectReader.getDefinition(projectName);

            String packagedInfo = readPackageProperties(project, filterWord);
            if (packagedInfo != null) {
                packagedInfoList.add(packagedInfo);
            }
        }
        return packagedInfoList;
    }

    private String readPackageProperties(IEclipseProject project,
            String filterWord) throws IOException {

        BufferedReader reader = null;
        try {
            File propertiesFile = new File(project.getLocation(),
                    FILE_PACKAGE_PROPERTIES);
            if (!propertiesFile.exists())
                return null;

            reader = new BufferedReader(new FileReader(propertiesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(filterWord) >= 0) {
                    return project.getName() + " (" + line + ")";
                }
            }
            return null;
        } finally {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }
    }

}
