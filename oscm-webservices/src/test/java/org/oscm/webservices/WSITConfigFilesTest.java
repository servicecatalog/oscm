/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.webservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.test.ClassFilter;
import org.oscm.test.PackageClassReader;

public class WSITConfigFilesTest {

    private final String WEBSERVICES_PACKAGE_NAME = "org.oscm.webservices";

    @Test
    public void checkFiles() throws ClassNotFoundException {
        File wsitFilesDir = new File(IdentityServiceWS.class.getResource(
                "/wsitconfig/").getPath());
        File classesDir = wsitFilesDir.getParentFile();
        assertTrue(classesDir.exists() && classesDir.isDirectory()
                && classesDir.list().length > 0);

        assertTrue(wsitFilesDir.exists() && wsitFilesDir.isDirectory()
                && wsitFilesDir.list().length > 0);

        List<String> filesNameList = Arrays.asList(wsitFilesDir.list());
        List<String> wsitFilesNameList = new ArrayList<String>();
        for (String fileName : filesNameList) {
            if (fileName.startsWith("wsit-")) {
                wsitFilesNameList.add(fileName);
            }
        }

        assertTrue(classesDir.exists() && classesDir.isDirectory());

        List<Class<?>> webservicesClasses = null;
        webservicesClasses = PackageClassReader.findClasses(classesDir,
                WEBSERVICES_PACKAGE_NAME + ".", null, ClassFilter.CLASSES_ONLY);

        if (webservicesClasses == null || webservicesClasses.isEmpty()) {
            Assert.fail("Webservices classes can not be found!");
            return;
        }

        Set<String> classesNameSet = new HashSet<String>();
        for (Class<?> webserviceClass : webservicesClasses) {
            String className = webserviceClass.getName();
            if (!webserviceClass.getPackage().getName()
                    .equalsIgnoreCase(WEBSERVICES_PACKAGE_NAME)
                    || className.contains("PaymentRegistrationService")) {
                continue;
            }
            classesNameSet.add(className);
            assertTrue(wsitFilesNameList.contains("wsit-" + className + ".xml"));
        }

        assertEquals(classesNameSet.size(), wsitFilesNameList.size());

    }
}
