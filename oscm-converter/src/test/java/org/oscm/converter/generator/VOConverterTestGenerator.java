/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 25, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.generator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * <p>
 * Generates test class for VOConverters. VOConverters are responsible for
 * copying same properties from lower versions to main version and vice versa.
 * So the generated tests will test the properties existing in both VO versions.
 * </p>
 * <p>
 * It generates test methods checking the up and down conversion functionality
 * including null checks.
 * </p>
 * <p>
 * Due to the test methods are generated for all classes within the relevant
 * package, it is possible the created class has compile errors for abstract
 * classes, package-info classes and classes not present anymore in main (up)
 * version. This methods has to be removed by hand ;)
 * </p>
 * <ol>
 * Usage instructions:
 * <li>Define the API_VERSION the test class is to be created for.</li>
 * <li>Run the application (main method).</li>
 * </ol>
 * 
 * @author muenz
 * 
 */
public class VOConverterTestGenerator {

    /**
     * Defines the API version. Please use syntax like: v1_3
     */
    private static final String API_VERSION = "v1_3";

    /**
     * =====================================================================
     * ========== DO NOT ADJUST BELOW THIS LINE (for generation) ===========
     * =====================================================================
     */

    /**
     * Package name the main VO objects are placed.
     */
    private static final String VO_BASE_PACKAGE = "org.oscm.vo.";

    /**
     * Package name the version VO objects are placed.
     */
    private static final String VO_VERSION_PACKAGE = VO_BASE_PACKAGE
            + API_VERSION;

    /**
     * Package the generated test class is to be placed in the project.
     */
    private static final String TEST_CLASS_PACKAGE = "org.oscm.converter."
            + API_VERSION;

    /**
     * File name of the generated test class.
     */
    private static final String TEST_CLASS_NAME = "VOConverterTest";

    @SuppressWarnings("rawtypes")
    private Class[] voClasses;

    /**
     * @param voClassesInPackage
     */
    public VOConverterTestGenerator(
            @SuppressWarnings("rawtypes") Class[] voClassesInPackage) {
        this.voClasses = voClassesInPackage;
    }

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception {
        Class[] voClassesInPackage = getClasses(VO_VERSION_PACKAGE);
        VOConverterTestGenerator generator = new VOConverterTestGenerator(
                voClassesInPackage);
        FileWriter fw = null;
        try {
            fw = generator.createOutputFile();
            generator.writeCode(fw);
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    /**
     * @param fw
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    private void writeCode(FileWriter fw) throws IOException {
        fw.write("package " + TEST_CLASS_PACKAGE + ";");
        fw.write("\n\n");
        fw.write("\n\n");
        fw.write("import static org.junit.Assert.assertEquals;\n");
        fw.write("import static org.junit.Assert.assertFalse;\n");
        fw.write("import static org.junit.Assert.assertNull;\n");
        fw.write("import static org.junit.Assert.assertTrue;\n");
        fw.write("import java.util.ArrayList;\n");
        fw.write("import java.util.Arrays;\n");
        fw.write("import java.util.HashSet;\n");
        fw.write("import java.util.List;\n");
        fw.write("import java.util.Set;\n");
        fw.write("import org.junit.Test;\n");
        fw.write("import org.oscm.converter.VOConverterTestBase;\n");
        fw.write("import org.oscm.converter.VOInitializer;\n");
        fw.write("\n\n");
        fw.write("public class " + TEST_CLASS_NAME
                + " extends VOConverterTestBase {\n\n");

        for (int i = 0; i < voClasses.length; i++) {
            Class classToConvert = voClasses[i];
            createTestMethod_ToUpNullCheck(classToConvert, fw);
            createTestMethod_ToUp(classToConvert, fw);
            createTestMethod_ToLowerVersionNullCheck(classToConvert, fw);
            createTestMethod_ToLowerVersion(classToConvert, fw);
        }

        fw.write("}");
        fw.close();
    }

    private FileWriter createOutputFile() throws IOException {
        String filePath = "javasrc/" + TEST_CLASS_PACKAGE.replace(".", "/")
                + "/" + TEST_CLASS_NAME + ".java";
        System.out.println(filePath);
        File outputFile = new File(filePath);
        if (outputFile.exists()) {
            throw new IllegalStateException("The output file already exists!");
        }
        outputFile.createNewFile();
        return new FileWriter(outputFile);
    }

    private void createTestMethod_ToLowerVersionNullCheck(
            Class<?> lowerVoClass, FileWriter fw) throws IOException {
        String simpleName = lowerVoClass.getSimpleName();
        fw.write("\t/** This method is auto-generated by " + this.getClass()
                + "*/\n");
        fw.write("\t@Test\n");
        fw.write("\tpublic void " + simpleName + "_convertTo"
                + API_VERSION.toUpperCase()
                + "_NullCheck() throws Exception {\n");
        fw.write("\t\tassertNull(" + TEST_CLASS_PACKAGE
                + ".VOConverter.convertTo" + API_VERSION.toUpperCase() + "(("
                + VO_BASE_PACKAGE + simpleName + ") null));\n");
        fw.write("\t}\n\n");
    }

    private void createTestMethod_ToUpNullCheck(Class<?> lowerVoClass,
            FileWriter fw) throws IOException {
        String simpleName = lowerVoClass.getSimpleName();
        String qualifiedClazzName = lowerVoClass.getName();
        fw.write("\t/** This method is auto-generated by " + this.getClass()
                + "*/\n");
        fw.write("\t@Test\n");
        fw.write("\tpublic void " + simpleName
                + "_convertToUp_NullCheck() throws Exception {\n");
        fw.write("\t\tassertNull(" + TEST_CLASS_PACKAGE
                + ".VOConverter.convertToUp((" + qualifiedClazzName
                + ") null));\n");
        fw.write("\t}\n\n");
    }

    private void createTestMethod_ToUp(Class<?> lowerVoClass, FileWriter fw)
            throws IOException {
        String simpleName = lowerVoClass.getSimpleName();
        String qualifiedClazzName = lowerVoClass.getName();
        fw.write("\t/** This method is auto-generated by " + this.getClass()
                + "*/\n");
        fw.write("\t@Test\n");
        fw.write("\tpublic void " + simpleName
                + "_convertToUp() throws Exception {\n");
        fw.write("\t\t" + qualifiedClazzName + " oldVO = new "
                + qualifiedClazzName + "();\n");
        fw.write("\t\tVOInitializer.initialize(oldVO);\n");
        fw.write("\t\t" + VO_BASE_PACKAGE + simpleName + " newVO = "
                + TEST_CLASS_PACKAGE + ".VOConverter.convertToUp(oldVO);\n");
        fw.write("\t\tassertValueObjects(oldVO, newVO);\n");
        fw.write("\t}\n\n");
    }

    private void createTestMethod_ToLowerVersion(Class<?> lowerVoClass,
            FileWriter fw) throws IOException {
        String simpleName = lowerVoClass.getSimpleName();
        String qualifiedClazzName = lowerVoClass.getName();
        fw.write("\t/** This method is auto-generated by " + this.getClass()
                + "*/\n");
        fw.write("\t@Test\n");
        fw.write("\tpublic void " + simpleName + "_convertTo"
                + API_VERSION.toUpperCase() + "() throws Exception {\n");
        fw.write("\t\t" + VO_BASE_PACKAGE + simpleName + " oldVO = new "
                + VO_BASE_PACKAGE + simpleName + "();\n");
        fw.write("\t\tVOInitializer.initialize(oldVO);\n");
        fw.write("\t\t" + qualifiedClazzName + " newVO = " + TEST_CLASS_PACKAGE
                + ".VOConverter.convertTo" + API_VERSION.toUpperCase()
                + "(oldVO);\n");
        fw.write("\t\tassertValueObjects(oldVO, newVO);\n");
        fw.write("\t}\n\n");
    }

    @SuppressWarnings("rawtypes")
    public static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("Unable to find classloader!");
        }

        // collect all files in package
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        // collect java classes
        List<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            FileFilter classFilter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".class")) {
                        return true;
                    }
                    return false;
                }
            };
            File[] listFiles = directory.listFiles(classFilter);
            for (int i = 0; i < listFiles.length; i++) {
                classes.add(Class.forName(packageName
                        + '.'
                        + listFiles[i].getName().substring(0,
                                listFiles[i].getName().length() - 6)));
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }
}
