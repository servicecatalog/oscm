/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * Internal support for handling of file structures. Used to create packages.
 */
public class Files {

    /**
     * Description of a logical file entry. This file may not necessarily exist,
     * the content might be created on demand.
     */
    public interface IFile {

        public static final int UNDEF_PERMISSIONS = -1;

        String getLocalPath();

        long getLastModified();

        int getPermissions();

        void writeTo(OutputStream out) throws IOException;
    }

    /**
     * Description of a set of files.
     */
    public interface IFileSet {
        Collection<IFile> getFiles();
    }

    /**
     * Container for files and file sets.
     */
    public interface IContainer {
        void add(String localpath, IFile file);

        void add(String localpath, IFileSet set);
    }

    /**
     * Filter for file properties or content.
     */
    public interface IFileFilter {
        IFile filter(IFile file);
    }

    /**
     * Apply a file filter to all files of a given file set.
     * 
     * @param file
     *            original file set
     * @param filter
     *            file filter
     * @return filtered version of the file set
     */
    public static IFileSet apply(final IFileSet fileset,
            final IFileFilter filter) {
        return new IFileSet() {
            public Collection<IFile> getFiles() {
                final Collection<IFile> files = fileset.getFiles();
                final Collection<IFile> result = new ArrayList<IFile>(
                        files.size());
                for (final IFile f : files) {
                    result.add(filter.filter(f));
                }
                return result;
            }
        };
    }

    /**
     * Combines multiple filters into a single filter. The first filter given
     * will be applied first. If no filter is given the returned filter will not
     * modify any file (identity).
     * 
     * @param filters
     * @return
     */
    public static IFileFilter combine(final IFileFilter... filters) {
        return new IFileFilter() {
            public IFile filter(IFile file) {
                for (final IFileFilter f : filters) {
                    file = f.filter(file);
                }
                return file;
            }
        };
    }

    /**
     * File implementation based on an existing local file. Depending on the
     * constructor the local path is just the name of the local file or any
     * other given path.
     */
    public static class PhysicalFile implements IFile {
        private final String localpath;
        private final File f;

        public PhysicalFile(File f) {
            this(f.getName(), f);
        }

        public PhysicalFile(String localpath, File f) {
            this.localpath = localpath;
            this.f = f;
        }

        public String getLocalPath() {
            return localpath;
        }

        public long getLastModified() {
            return f.lastModified();
        }

        public int getPermissions() {
            return UNDEF_PERMISSIONS;
        }

        public void writeTo(OutputStream out) throws IOException {
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                byte[] buffer = new byte[0x1000];
                int len;
                while ((len = in.read(buffer)) != -1)
                    out.write(buffer, 0, len);
            } finally {
                if (in != null)
                    FileUtils.close(in);
            }
        }
    }

    /**
     * File set that is created from a local directory based on a given pattern.
     */
    public static class PhysicalFileSet implements IFileSet {
        private final File basedir;
        private final String pattern;

        public PhysicalFileSet(File basedir, String pattern) {
            this.basedir = basedir;
            this.pattern = pattern;
        }

        public Collection<IFile> getFiles() {
            final DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(basedir);
            ds.setIncludes(new String[] { pattern });
            ds.addDefaultExcludes();
            ds.scan();
            final String[] filenames = ds.getIncludedFiles();
            final Collection<IFile> files = new ArrayList<IFile>(
                    filenames.length);
            for (final String name : filenames) {
                final String normName = name.replace(File.separatorChar, '/');
                files.add(new PhysicalFile(normName, new File(basedir, name)));
            }
            return files;
        }
    }

    /**
     * Base implementation of IFileSet that records the files and file sets
     * inserted to an IContainer.
     */
    public static class BaseContainer implements IContainer, IFileSet {

        private final List<IFile> files = new ArrayList<IFile>();
        private final List<IFileSet> sets = new ArrayList<IFileSet>();

        public Collection<IFile> getFiles() {
            final List<IFile> result = new ArrayList<IFile>(files);
            for (final IFileSet s : sets) {
                result.addAll(s.getFiles());
            }
            return result;
        }

        public void add(String localpath, IFile file) {
            files.add(rename(localpath, file));
        }

        public void add(String localpath, IFileSet set) {
            sets.add(prefix(localpath, set));
        }

    }

    /**
     * A container that can deliver its content as a zip file.
     */
    public static class ZipFile implements IContainer, IFile {

        private final BaseContainer container;
        private final String localpath;

        public ZipFile(String localpath) {
            this.container = new BaseContainer();
            this.localpath = localpath;
        }

        public String getLocalPath() {
            return localpath;
        }

        public long getLastModified() {
            return System.currentTimeMillis();
        }

        public int getPermissions() {
            return UNDEF_PERMISSIONS;
        }

        public void writeTo(OutputStream out) throws IOException {
            final Set<String> filenames = new HashSet<String>();
            final ZipOutputStream zipout = new ZipOutputStream(out);
            for (IFile f : container.getFiles()) {
                assertNoAbsolutePath(f);
                assertNoDuplicates(filenames, f);
                ZipEntry entry = new ZipEntry(f.getLocalPath());
                entry.setTime(f.getLastModified());
                if (f.getPermissions() != IFile.UNDEF_PERMISSIONS) {
                    entry.setUnixMode(f.getPermissions());
                }
                zipout.putNextEntry(entry);
                f.writeTo(zipout);
                zipout.closeEntry();
            }
            zipout.finish();
        }

        public void add(String localpath, IFile file) {
            container.add(localpath, file);
        }

        public void add(String localpath, IFileSet set) {
            container.add(localpath, set);
        }

    }

    /**
     * Automatically creates zip files in the given container for local path
     * names containing a '@' character. E.g. archive.zip@readme.txt will create
     * a Zip file called archive.zip containing a file readme.txt.
     * 
     * @param delegate
     *            the container that should be populated
     * @return new container that will create zips automatically
     */
    public static IContainer autozip(final IContainer delegate) {
        return new IContainer() {
            private final Map<String, IContainer> zipfiles = new HashMap<String, IContainer>();

            private IContainer getZipFile(String zipname) {
                IContainer zipcontainer = zipfiles.get(zipname);
                if (zipcontainer == null) {
                    ZipFile zip = new ZipFile(zipname);
                    delegate.add(zipname, zip);
                    zipcontainer = autozip(zip);
                    zipfiles.put(zipname, zipcontainer);
                }
                return zipcontainer;
            }

            public void add(String localpath, IFile file) {
                IContainer container = delegate;
                int atidx = localpath.indexOf('@');
                if (atidx != -1) {
                    String zipname = localpath.substring(0, atidx);
                    container = getZipFile(zipname);
                    localpath = localpath.substring(atidx + 1);
                }
                container.add(localpath, file);
            }

            public void add(String localpath, IFileSet set) {
                IContainer container = delegate;
                int atidx = localpath.indexOf('@');
                if (atidx != -1) {
                    String zipname = localpath.substring(0, atidx);
                    container = getZipFile(zipname);
                    localpath = localpath.substring(atidx + 1);
                }
                container.add(localpath, set);
            }
        };
    }

    /**
     * A filter that sets the file permissions to the given value.
     * 
     * @param permissions
     *            unix mode in octal notation (e.g. 0755)
     * @return filter for permissions
     */
    public static IFileFilter permissionsFilter(final int permissions) {
        return new IFileFilter() {
            public IFile filter(final IFile file) {
                return new IFile() {
                    public long getLastModified() {
                        return file.getLastModified();
                    }

                    public String getLocalPath() {
                        return file.getLocalPath();
                    }

                    public int getPermissions() {
                        return permissions;
                    }

                    public void writeTo(OutputStream out) throws IOException {
                        file.writeTo(out);
                    }
                };
            }
        };
    }

    /**
     * Writes the given set of files into the output directory.
     * 
     * @param set
     * @param outputdir
     */
    public static void writeFiles(final IFileSet set, final File outputdir)
            throws IOException {
        final Set<String> filenames = new HashSet<String>();
        for (final IFile f : set.getFiles()) {
            assertNoAbsolutePath(f);
            assertNoDuplicates(filenames, f);
            final File file = new File(outputdir, f.getLocalPath());
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                f.writeTo(out);
            } finally {
                if (out != null)
                    FileUtils.close(out);
            }
        }
    }

    private static IFile rename(final String newpath, final IFile file) {
        return new IFile() {
            public String getLocalPath() {
                return newpath;
            }

            public long getLastModified() {
                return file.getLastModified();
            }

            public int getPermissions() {
                return file.getPermissions();
            }

            public void writeTo(OutputStream out) throws IOException {
                file.writeTo(out);
            }
        };
    }

    private static IFile prefix(String prefix, final IFile file) {
        while (prefix.endsWith("/"))
            prefix = prefix.substring(0, prefix.length() - 1);
        final String p = prefix;
        return new IFile() {
            public String getLocalPath() {
                if (p.length() > 0) {
                    return p + '/' + file.getLocalPath();
                } else {
                    return file.getLocalPath();
                }
            }

            public long getLastModified() {
                return file.getLastModified();
            }

            public int getPermissions() {
                return file.getPermissions();
            }

            public void writeTo(OutputStream out) throws IOException {
                file.writeTo(out);
            }
        };
    }

    private static IFileSet prefix(final String prefixpath, final IFileSet set) {
        return new IFileSet() {
            public Collection<IFile> getFiles() {
                final Collection<IFile> files = set.getFiles();
                final Collection<IFile> result = new ArrayList<IFile>(
                        files.size());
                for (final IFile f : files) {
                    result.add(prefix(prefixpath, f));
                }
                return result;
            }
        };
    }

    private static void assertNoAbsolutePath(final IFile f) throws IOException {
        final String path = f.getLocalPath();
        if (path.startsWith("/")) {
            throw new IOException("No absolute paths allowed: " + path);
        }
    }

    private static void assertNoDuplicates(final Set<String> filenames,
            final IFile f) throws IOException {
        final String path = f.getLocalPath();
        if (!filenames.add(path)) {
            throw new IOException("Duplicate definition of file " + path);
        }
    }

}
