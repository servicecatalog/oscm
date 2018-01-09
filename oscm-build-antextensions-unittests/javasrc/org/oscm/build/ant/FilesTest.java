/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import org.oscm.build.ant.Files.BaseContainer;
import org.oscm.build.ant.Files.IContainer;
import org.oscm.build.ant.Files.IFile;
import org.oscm.build.ant.Files.IFileFilter;
import org.oscm.build.ant.Files.IFileSet;
import org.oscm.build.ant.Files.PhysicalFile;
import org.oscm.build.ant.Files.PhysicalFileSet;
import org.oscm.build.ant.Files.ZipFile;

public class FilesTest {

    private static class FileStub implements IFile {
        private final String localpath;

        FileStub(String localpath) {
            this.localpath = localpath;
        }

        public String getLocalPath() {
            return localpath;
        }

        public long getLastModified() {
            return 0;
        }

        public int getPermissions() {
            return UNDEF_PERMISSIONS;
        }

        public void writeTo(OutputStream out) {
        }
    }

    private static IFileSet stubFileSet(String... localpaths) {
        final Collection<IFile> files = new ArrayList<IFile>();
        for (final String p : localpaths) {
            files.add(new FileStub(p));
        }
        return new IFileSet() {
            public Collection<IFile> getFiles() {
                return files;
            }
        };
    }

    private static List<String> getLocalPaths(IFileSet set) {
        final List<String> names = new ArrayList<String>();
        for (final IFile f : set.getFiles()) {
            names.add(f.getLocalPath());
        }
        return names;
    }

    @Test
    public void testApplyFilterToFileSet() {
        final IFileSet set = stubFileSet("a.txt", "b.txt");
        final IFileFilter filter = new IFileFilter() {
            public IFile filter(IFile file) {
                return new FileStub(file.getLocalPath() + ".bak");
            }
        };
        final IFileSet fileset = Files.apply(set, filter);
        assertEquals(Arrays.asList("a.txt.bak", "b.txt.bak"),
                getLocalPaths(fileset));
    }

    @Test
    public void testCombine() {
        final IFileFilter f1 = new IFileFilter() {
            public IFile filter(IFile file) {
                return new FileStub(file.getLocalPath() + ".tar");
            }
        };
        final IFileFilter f2 = new IFileFilter() {
            public IFile filter(IFile file) {
                return new FileStub(file.getLocalPath() + ".gz");
            }
        };
        final IFileFilter filter = Files.combine(f1, f2);
        final IFile filtered = filter.filter(new FileStub("sample.txt"));
        assertEquals("sample.txt.tar.gz", filtered.getLocalPath());
    }

    @Test
    public void testPhysicalFileLocalPath1() {
        final File source = new File("resources/files/a.txt");
        IFile file = new PhysicalFile(source);
        assertEquals("a.txt", file.getLocalPath());
        assertEquals(IFile.UNDEF_PERMISSIONS, file.getPermissions());
        assertEquals(source.lastModified(), file.getLastModified());
    }

    @Test
    public void testPhysicalFileLocalPath2() {
        IFile file = new PhysicalFile("localdir/b.xml", new File(
                "resources/files/a.txt"));
        assertEquals("localdir/b.xml", file.getLocalPath());
    }

    @Test
    public void testPhysicalFileWriteTo() throws IOException {
        IFile file = new PhysicalFile(new File("resources/files/a.txt"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        file.writeTo(buffer);
        assertEquals("abcde", new String(buffer.toByteArray()));
    }

    @Test
    public void testPhysicalFileSet() {
        final IFileSet set = new PhysicalFileSet(new File("resources/files"),
                "**/*.txt");
        final Set<String> actual = new HashSet<String>();
        for (final IFile f : set.getFiles()) {
            actual.add(f.getLocalPath());
        }
        final Set<String> expected = new HashSet<String>(Arrays
                .asList(new String[] { "a.txt", "dir/c.txt" }));
        assertEquals(expected, actual);
    }

    @Test
    public void testBaseContainerEmpty() {
        final BaseContainer c = new BaseContainer();
        assertEquals(Collections.emptyList(), c.getFiles());
    }

    @Test
    public void testBaseContainerAddFile() {
        final BaseContainer c = new BaseContainer();
        c.add("new/path/file.xml", new FileStub("original/path/file.txt"));
        final Collection<IFile> files = c.getFiles();
        assertEquals(1, files.size());
        assertEquals("new/path/file.xml", files.iterator().next()
                .getLocalPath());
    }

    @Test
    public void testBaseContainerAddFileSet1() {
        final BaseContainer c = new BaseContainer();
        final IFileSet set = stubFileSet("original/path/file.txt");
        c.add("", set);
        final Collection<IFile> files = c.getFiles();
        assertEquals(1, files.size());
        assertEquals("original/path/file.txt", files.iterator().next()
                .getLocalPath());
    }

    @Test
    public void testBaseContainerAddFileSet2() {
        final BaseContainer c = new BaseContainer();
        final IFileSet set = stubFileSet("original/path/file.txt");
        c.add("new/path", set);
        assertEquals(Collections
                .singletonList("new/path/original/path/file.txt"),
                getLocalPaths(c));
    }

    @Test
    public void testBaseContainerAddFileFileAndSet() {
        final BaseContainer c = new BaseContainer();
        c.add("file1.txt", new FileStub("file1.txt"));
        final IFileSet set = stubFileSet("file2.txt", "file3.txt");
        c.add("setpath/", set);
        assertEquals(Arrays.asList("file1.txt", "setpath/file2.txt",
                "setpath/file3.txt"), getLocalPaths(c));
    }

    @Test
    public void testPermissionsFilter() {
        final IFileFilter filter = Files.permissionsFilter(0755);
        final IFile file = filter.filter(new FileStub("doit.sh"));
        assertEquals(0755, file.getPermissions());
    }

    @Test
    public void testZipFile() throws IOException {
        ZipFile zip = new ZipFile("archive.zip");
        zip.add("file1.txt", new FileStub("file1.txt"));
        zip.add("folder", stubFileSet("file2.txt", "subfolder/file3.txt"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        zip.writeTo(buffer);
        Set<String> expectedentries = new HashSet<String>(Arrays
                .asList(new String[] { "file1.txt", "folder/file2.txt",
                        "folder/subfolder/file3.txt" }));
        assertEquals(expectedentries, getZipEntries(buffer.toByteArray())
                .keySet());
        assertEquals("archive.zip", zip.getLocalPath());
    }

    @Test(expected = IOException.class)
    public void testZipFileAbsolutePath() throws IOException {
        ZipFile zip = new ZipFile("archive.zip");
        zip.add("/file1.txt", new FileStub("file1.txt"));
        zip.writeTo(new ByteArrayOutputStream());
    }

    @Test(expected = IOException.class)
    public void testZipFileDuplicates() throws IOException {
        ZipFile zip = new ZipFile("archive.zip");
        zip.add("file1.txt", new FileStub("file1.txt"));
        zip.add("file1.txt", new FileStub("file1.txt"));
        zip.writeTo(new ByteArrayOutputStream());
    }

    @Test
    public void testAutoZip() throws IOException {
        BaseContainer base = new BaseContainer();
        IContainer autozip = Files.autozip(base);
        autozip.add("archive.zip@file2.txt", new FileStub("file2.txt"));
        autozip.add("archive.zip@folder/file3.txt", new FileStub("file3.txt"));
        autozip.add("archive.zip@archive.zip@", stubFileSet("file4.txt",
                "folder/file5.txt"));
        autozip.add("file1.txt", new FileStub("file1.txt"));
        Collection<IFile> files = base.getFiles();
        assertEquals(Arrays.asList("archive.zip", "file1.txt"),
                getLocalPaths(base));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        files.iterator().next().writeTo(buffer);
        Map<String, byte[]> zipentries1 = getZipEntries(buffer.toByteArray());
        Set<String> expectedentries = new HashSet<String>(Arrays.asList(
                "file2.txt", "folder/file3.txt", "archive.zip"));
        assertEquals(expectedentries, zipentries1.keySet());
        Map<String, byte[]> zipentries2 = getZipEntries(zipentries1
                .get("archive.zip"));
        expectedentries = new HashSet<String>(Arrays.asList(new String[] {
                "file4.txt", "folder/file5.txt" }));
        assertEquals(expectedentries, zipentries2.keySet());
    }

    protected Map<String, byte[]> getZipEntries(byte[] content)
            throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        ZipInputStream in = new ZipInputStream(
                new ByteArrayInputStream(content));
        ZipEntry entry;
        while ((entry = in.getNextEntry()) != null) {
            String path = entry.getName();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1)
                buffer.write(b);
            assertNull("duplicate entry " + path, result.put(path, buffer
                    .toByteArray()));
        }
        return result;
    }

}
