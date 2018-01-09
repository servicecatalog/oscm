/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import org.oscm.build.ant.Files.BaseContainer;
import org.oscm.build.ant.Files.IContainer;
import org.oscm.build.ant.Files.IFile;
import org.oscm.build.ant.Files.IFileSet;
import org.oscm.build.ant.Files.PhysicalFileSet;

public class LicensesPackageTask extends Task {

    private File packagefile;
    private File licensesdir;
    private File outputdir;

    public void setPackagefile(File file) {
        packagefile = file;
    }

    public void setLicensesdir(File dir) {
        licensesdir = dir;
    }

    public void setOutputdir(File dir) {
        outputdir = dir;
    }

    @Override
    public void execute() throws BuildException {
        if (packagefile == null) {
            throw new BuildException("No package file set.");
        }
        if (packagefile == null) {
            throw new BuildException("No licenses location set.");
        }
        if (outputdir == null) {
            throw new BuildException("No output location set.");
        }
        Set<String> references = findProjectReferences();

        IFileSet fileset = createFileSet(references);
        writeFiles(fileset);
    }

    private void writeFiles(IFileSet set) throws BuildException {
        final Set<String> filenames = new HashSet<String>();
        for (final IFile f : set.getFiles()) {
            if (filenames.add(f.getLocalPath())) {
                try {
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(new File(outputdir,
                                f.getLocalPath()));
                        f.writeTo(out);
                    } finally {
                        if (out != null)
                            FileUtils.close(out);
                    }
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }
        }
    }

    private IFileSet createFileSet(Set<String> references) {
        final BaseContainer container = new BaseContainer();
        for (final String project : references) {
            addFiles(container, project);
        }
        return container;
    }

    private void addFiles(IContainer container, String project) {
        final File basedir = new File(licensesdir, project);
        IFileSet set = new PhysicalFileSet(basedir, "*");
        container.add("", set);
    }

    private Set<String> findProjectReferences() {
        final Set<String> references = new HashSet<String>();
        final Pattern p = Pattern
                .compile("\\$\\{(project|result\\.package)\\.([^\\.}]*)\\.");

        Properties parser = new Properties() {
            private static final long serialVersionUID = 1L;

            @Override
            public synchronized Object put(Object key, Object value) {
                String sourcepath = getProject().replaceProperties(
                        (String) value);
                checkUnresolvedProperties(sourcepath);

                Matcher m = p.matcher((String) value);
                if (m.find()) {
                    references.add(m.group(2));
                }
                return null;
            }
        };

        InputStream in = null;
        try {
            in = new FileInputStream(packagefile);
            parser.load(in);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            if (in != null) {
                FileUtils.close(in);
            }
        }
        return references;
    }

    private void checkUnresolvedProperties(String s) {
        Matcher m = Pattern.compile("\\$\\{.*\\}").matcher(s);
        if (m.find()) {
            throw new BuildException("Unknown property " + m.group(0) + " in "
                    + "packaging specification. Check dependency declarations.");
        }
    }

}
