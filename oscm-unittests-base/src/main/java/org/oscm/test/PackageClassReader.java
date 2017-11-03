/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                  
 *                                                                              
 *  Creation Date: 30.09.2010                                                      
 *                                                                              
 *  Completion Time: 30.09.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author weiser
 * 
 */
public class PackageClassReader {

    private static List<String> excludeClasses = new ArrayList<String>();
    static {
        excludeClasses.add("org.oscm.integrationhelper.BssClient");
        excludeClasses
                .add("org.oscm.integrationhelper.UserCredentialsHandler");
    }

    public static List<Class<?>> getClasses(Class<?> classInDir,
            Class<?> superClass, ClassFilter classFilter)
            throws ClassNotFoundException {
        String seedName = classInDir.getSimpleName() + ".class";
        final URL basedir = classInDir.getResource(seedName);
        String dir = basedir.getFile();
        dir = dir.substring(0, dir.length() - seedName.length());

        String cn = classInDir.getName();
        String packageName = cn.substring(0, (cn.length() - classInDir
                .getSimpleName().length()));
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.addAll(findClasses(new File(dir), packageName, superClass,
                classFilter));
        if (classes.isEmpty()) {
            // this actually must not happen in the test - otherwise a wrong
            // path/package was specified
            throw new RuntimeException(String.format(
                    "No classes found in %s and package %s.", dir, packageName));
        }
        return classes;
    }

    public static List<Class<?>> findClasses(File directory,
            String packageName, Class<?> superClass, ClassFilter classFilter)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            String subPackageName = packageName;
            String currentDirName = file.getAbsolutePath().replace(
                    File.separatorChar, '.');
            if (currentDirName.contains("..")) {
                currentDirName = currentDirName.substring(file
                        .getAbsolutePath().indexOf(".."));
            }
            if (currentDirName.contains("metadata")
                    || !currentDirName.contains("oscm-")) {
                continue;
            }
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                if (currentDirName.contains(packageName)) {
                    subPackageName += file.getName() + ".";
                }
                classes.addAll(findClasses(file, subPackageName, superClass,
                        classFilter));
            } else if (file.getName().endsWith(".class")
                    && !file.getName().contains("$")
                    && currentDirName.contains(subPackageName)) {
                addClass(classes, packageName, file, classFilter, superClass);
            }
        }
        return classes;
    }

    private static void addClass(Collection<Class<?>> classes,
            String packageName, File file, ClassFilter classFilter,
            Class<?> superClass) {
        String className = packageName
                + file.getName().substring(0, file.getName().length() - 6);
        Class<?> clazz;
        try {
            if (!excludeClasses.contains(className)) {
                clazz = Class.forName(className);
            } else {
                return;
            }
        } catch (ClassNotFoundException e) {
            return;
        }
        if (classFilter.isNeglectableClass(clazz)) {
            // ignore interfaces, enumerations and abstract classes
            return;
        }
        if (classExtendsSuperClass(clazz, superClass)) {
            classes.add(clazz);
        }
    }

    /**
     * Check if it is a subclass of {@link Throwable}.
     * 
     * @param clazz
     *            the class to test
     * @return
     */
    private static boolean classExtendsSuperClass(Class<?> clazz,
            Class<?> expectedSuperClass) {
        if (expectedSuperClass == null) {
            return true;
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass == Object.class) {
            return false;
        } else if (superClass == expectedSuperClass) {
            return true;
        }
        return classExtendsSuperClass(superClass, expectedSuperClass);
    }

    public List<String> getExcludeClasses() {
        return excludeClasses;
    }

}
