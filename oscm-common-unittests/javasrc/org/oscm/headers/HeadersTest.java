/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 2015-10-01
 *
 * <p>*****************************************************************************
 */
package org.oscm.headers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Test;

/** @author lixiangjun */
public class HeadersTest {

    private static boolean failed = false;
    private static ArrayList<String> failedFilesCollection = new ArrayList<String>();
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "oscm-integrationtests-setup" + java.io.File.separator + "resources"
                    + java.io.File.separator + "work",
            "oscm-integrationtests-setup" + java.io.File.separator + "resources"
                    + java.io.File.separator + "workapp",
            "oscm-portal-webtests" + java.io.File.separator + "results",
            "oscm-build" + java.io.File.separator + "result",
            java.io.File.separator + "bin",
            "oscm-app-sample" + java.io.File.separator + "resources",
            "oscm-app-extsvc-2-0" + java.io.File.separator + "javares",
            "oscm-extsvc" + java.io.File.separator + "javares",
            "oscm-extsvc-operation" + java.io.File.separator + "javares",
            "oscm-extsvc-notification" + java.io.File.separator + "javares",
            "oscm-extsvc-provisioning" + java.io.File.separator + "javares",
            "oscm-psp-extsvc" + java.io.File.separator + "javares",
            "oscm-billing-external-interfaces" + java.io.File.separator
                    + "javares",
            "oscm-extsvc-internal" + java.io.File.separator + "javares");

    @Test
    public void testIsContainCopyrightHeader() {

        ArrayList<String> projectCollection = new ArrayList<>();
        File dir = new File(System.getProperty("user.dir") + "/..");
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()
                    && (files[i].getName().startsWith("oscm-"))) {
                projectCollection.add(files[i].getAbsolutePath());
            }
        }

        for (int i = 0; i < projectCollection.size(); i++) {
            checkFiles(projectCollection.get(i));
        }
        assertEquals("", Boolean.FALSE, Boolean.valueOf(failed));
    }

    private static boolean containExcludePath(String filePath) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (filePath.endsWith(excludePath)) {
                return true;
            }
        }
        return false;
    }

    @After
    public void after() {
        if (failed) {
            System.out.println(
                    "Test failed due to the following files don't contain the specified copyright headers:");
            for (int i = 0; i < failedFilesCollection.size(); i++) {
                System.out.println(failedFilesCollection.get(i));
            }
        }
    }

    private static void checkFiles(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (!containExcludePath(files[i].getAbsolutePath())) {
                    checkFiles(files[i].getAbsolutePath());
                }
            } else {
                String fileName = files[i].getAbsolutePath();
                if (fileName.toLowerCase().endsWith(".java")
                        || fileName.toLowerCase().endsWith(".js")
                        || fileName.toLowerCase().endsWith(".css")) {
                    if (fileName.contains("book.css")
                            || fileName.contains("import_en.css")) {
                        continue;
                    }
                    checkFile(fileName, "Copyright FUJITSU LIMITED");
                } else if (fileName.toLowerCase().endsWith(".xml")
                        || fileName.toLowerCase().endsWith(".xhtml")) {
                    if (fileName
                            .contains("TechnicalServiceImportEmptyFile.xml")) {
                        continue;
                    }
                    checkFile(fileName, "Copyright FUJITSU LIMITED");
                } else if (fileName.toLowerCase().endsWith(".properties")) {
                    if (fileName.toLowerCase()
                            .contains("oscm-common-unittests"
                                    + java.io.File.separator + "junit")
                            || fileName.contains("wt.testInWork.properties")) {
                        continue;
                    }
                    checkFile(fileName, "Copyright FUJITSU LIMITED");
                }
            }
        }
    }

    private static void checkFile(String filePath, String header) {
        try {
            byte[] fileContent;
            RandomAccessFile randomFile = new RandomAccessFile(filePath, "rw");
            fileContent = new byte[(int) randomFile.length()];
            randomFile.readFully(fileContent);
            randomFile.close();
            String text = new String(fileContent);
            if (text.indexOf("Copyright IBM Corp") != -1) {
                return;
            }
            if (text.indexOf(header) == -1) {
                failed = true;
                failedFilesCollection.add(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
