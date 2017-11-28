/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.jmx.internal.common;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JavaFileScanner {
    public static List<Class<?>> scanForAnnotatedClassesInFolder(
            String relPath, Class<? extends Annotation> annotation) {
        List<Class<?>> resultList = new LinkedList<Class<?>>();

        String packageName = relPath.replace("/", ".");
        URL packageURL = Thread.currentThread().getContextClassLoader()
                .getResource(relPath);

        File folder = new File(packageURL.getFile());
        for (File file : folder.listFiles()) {
            if (file.getAbsolutePath().endsWith(".class")) {
                String className = file.getName().substring(0,
                        file.getName().lastIndexOf('.'));
                String fullClassName = packageName + "." + className;
                verifyAndAppendToList(fullClassName, annotation, resultList);
            }
        }
        return resultList;
    }

    private static void verifyAndAppendToList(String className,
            Class<? extends Annotation> annotation, List<Class<?>> resultList) {
        try {
            Class<?> aClass = Class.forName(className);
            if (aClass.isAnnotationPresent(annotation)) {
                resultList.add(aClass);
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }
}
