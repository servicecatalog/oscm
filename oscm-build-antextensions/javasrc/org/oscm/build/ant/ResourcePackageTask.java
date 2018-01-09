/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import org.oscm.build.ant.Files.BaseContainer;
import org.oscm.build.ant.Files.IContainer;
import org.oscm.build.ant.Files.IFile;
import org.oscm.build.ant.Files.IFileFilter;
import org.oscm.build.ant.Files.IFileSet;
import org.oscm.build.ant.Files.PhysicalFile;
import org.oscm.build.ant.Files.PhysicalFileSet;

public class ResourcePackageTask extends Task {

    private File packagefile;
    private File outputdir;

    private final IFileFilter replacefilter = new IFileFilter() {
        @Override
        public IFile filter(final IFile file) {
            return new IFile() {
                @Override
                public long getLastModified() {
                    return file.getLastModified();
                }

                @Override
                public String getLocalPath() {
                    return file.getLocalPath();
                }

                @Override
                public int getPermissions() {
                    return file.getPermissions();
                }

                @Override
                public void writeTo(OutputStream out) throws IOException {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    file.writeTo(buffer);
                    String s = new String(buffer.toByteArray(), "ISO-8859-1");
                    out.write(replaceProperties(s).getBytes("ISO-8859-1"));
                }
            };
        }
    };

    private final IFileFilter renamewsitfilefilter = new IFileFilter() {
        private static final String namePattern = ".*webservices\\.(.*)WS\\.xml";

        @Override
        public IFile filter(final IFile file) {
            return new IFile() {
                @Override
                public long getLastModified() {
                    return file.getLastModified();
                }

                @Override
                public String getLocalPath() {

                    Pattern pattern = Pattern.compile(namePattern,
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(file.getLocalPath());
                    String fileName = "";
                    if (matcher.matches()) {
                        fileName = matcher.group(1);

                    }
                    return fileName + ".wsdl";
                }

                @Override
                public int getPermissions() {
                    return file.getPermissions();
                }

                @Override
                public void writeTo(OutputStream out) throws IOException {
                    file.writeTo(out);
                }
            };
        }
    };

    public void setPackagefile(File file) {
        packagefile = file;
    }

    public void setOutputdir(File dir) {
        outputdir = dir;
    }

    @Override
    public void execute() throws BuildException {
        if (packagefile == null)
            throw new BuildException("No package file set.");
        if (outputdir == null)
            throw new BuildException("No output location set.");

        try {
            Files.writeFiles(createFileSet(), outputdir);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private IFileSet createFileSet() throws IOException {
        final BaseContainer rootcontainer = new BaseContainer();
        final IContainer container = Files.autozip(rootcontainer);
        // We just use the Properties.load() method as a parser. As we
        // will allow duplicate keys we have to hook into the put() method.
        final Properties parser = new Properties() {
            private static final long serialVersionUID = 1L;

            @Override
            public synchronized Object put(Object key, Object value) {
                String outputpath = replaceProperties((String) key);
                String sourcepath = replaceProperties((String) value);
                addFiles(container, outputpath, sourcepath);
                return null;
            }
        };
        final InputStream in = new FileInputStream(packagefile);
        try {
            parser.load(in);
        } finally {
            FileUtils.close(in);
        }
        return rootcontainer;
    }

    private String replaceProperties(String s) {
        s = getProject().replaceProperties(s);
        final Matcher m = Pattern.compile("\\$\\{.*\\}").matcher(s);
        if (m.find()) {
            throw new BuildException("Unknown property " + m.group(0) + " in "
                    + "packaging specification. Check dependency declarations.");
        }
        return s;
    }

    private IFileFilter getFilter(String name) {
        if (name.equals("replace")) {
            return replacefilter;
        }
        if (name.equals("renamewsitfile")) {
            return renamewsitfilefilter;
        }
        if (name.startsWith("chmod")) {
            final String[] args = name.split("\\s+");
            if (args.length != 2) {
                throw new BuildException("Invalid parameter for chmod filter.");
            }
            final String mode = args[1];
            try {
                return Files.permissionsFilter(Integer.parseInt(mode, 8));
            } catch (NumberFormatException e) {
                throw new BuildException("Invalid mode for chmod filter: "
                        + mode);
            }
        }
        throw new BuildException("Unknown filter: " + name);
    }

    private void addFiles(IContainer container, String outputpath,
            String sourcespec) {
        final String[] tokens = sourcespec.split("\\s*\\|\\s*");

        final IFileFilter filter;
        {
            final IFileFilter[] filters = new IFileFilter[tokens.length - 1];
            for (int i = 0; i < filters.length; i++) {
                filters[i] = getFilter(tokens[i + 1].trim());
            }
            filter = Files.combine(filters);
        }

        String sourcepath = tokens[0];
        String pattern = null;

        int idxl = sourcepath.indexOf('[');
        int idxr = sourcepath.indexOf(']');
        if (idxl != -1 && idxr == sourcepath.length() - 1 && idxl < idxr) {
            pattern = sourcepath.substring(idxl + 1, idxr);
            sourcepath = sourcepath.substring(0, idxl);
        }

        addFiles(container, outputpath, sourcepath, pattern, filter);
    }

    private void addFiles(IContainer container, String outputpath,
            String sourcepath, String pattern, IFileFilter filter) {
        if (pattern != null) {
            final File basedir = new File(sourcepath);
            final IFileSet set = new PhysicalFileSet(basedir, pattern);
            container.add(outputpath, Files.apply(set, filter));
        } else {
            final IFile file = new PhysicalFile(new File(sourcepath));
            container.add(outputpath, filter.filter(file));
        }
    }

}
