/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 10.11.2010                                                     
 *                                                                              
 *******************************************************************************/

/**
 * Utility for generating source code of different version value objects converter.
 * 
 * @author Aleh Khomich. 
 */
package org.oscm.generator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VOConverterGenerator {

    private static final String VO_LIST_FILE = "javares/volist.properties";
    private static final String VO_PACKAGE_NAME = "org.oscm.internal.vo";
    private static final String VO_PACKAGE_NAME_OLD = "org.oscm.vo.v1_5";
    private static final String VERSION_UP = "Up";
    private static final String VERSION_OLD = "V1_5";
    final String GENERATOR_FILE_NAME = "javares/VOConverter.java";

    private static final String OLD_SUFFIX = "v1_5";
    private static final Set<String> COLLECTION_TYPES = new HashSet<String>(
            Arrays.asList("java.util.List", "java.util.Set"));

    /** List of collections of VO object. */
    private final HashSet<String> setOfVOLists = new HashSet<String>();

    public static void main(String args[]) throws Exception {
        VOConverterGenerator generator = new VOConverterGenerator();
        generator.run();
    }

    /**
     * Generate source code of converter for different version value objects.
     */
    public void run() {
        // prepare file name for properties
        File currentDir = new File(".");
        String canonicalPath = null;
        try {
            canonicalPath = currentDir.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileName = canonicalPath + "/" + GENERATOR_FILE_NAME;
        createFile(fileName);
    }

    /**
     * Getting list of all VO.
     * 
     * @param listNamesFile
     *            Name of file with list of VO to generate convertor for them.
     * 
     * @return List of VOs.
     */
    public List<String> getVOList(String listNamesFile) {
        File file = new File(listNamesFile);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        InputStreamReader isr = null;
        BufferedReader d = null;
        ArrayList<String> voList = new ArrayList<String>();

        try {
            fis = new FileInputStream(file);

            bis = new BufferedInputStream(fis);

            isr = new InputStreamReader(bis);

            d = new BufferedReader(isr);

            String str = d.readLine();
            while (str != null) {
                voList.add(str);
                str = d.readLine();
            }
            fis.close();
            bis.close();
            isr.close();
            d.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                // ignore, wanted to close anyway
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ex) {
                // ignore, wanted to close anyway
            }
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException ex) {
                // ignore, wanted to close anyway
            }
            try {
                if (d != null) {
                    d.close();
                }
            } catch (IOException ex) {
                // ignore, wanted to close anyway
            }
        }
        return voList;
    }

    /**
     * Getting name of file with list of VO objects.
     * 
     * @return File name.
     */
    public String getVOListFileName() {
        File currentDir = new File(".");
        String canonicalPath = null;
        try {
            canonicalPath = currentDir.getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String voListFileName = canonicalPath + "/" + VO_LIST_FILE;

        return voListFileName;
    }

    /**
     * Getting information about VOs.
     * 
     * @return Information about all VOs.
     */
    public VOInfo getPropertiesInfo() {
        VOInfo result = new VOInfo();
        HashMap<String, List<VOPropertyDescription>> voPropertiesInfo = new HashMap<String, List<VOPropertyDescription>>();

        List<String> voList = getVOList(getVOListFileName());
        for (String vo : voList) {
            List<VOPropertyDescription> oneVOProperties = getVOProperties(vo);
            // fill map of VO and properties
            voPropertiesInfo.put(vo, oneVOProperties);
        }
        result.setVoPropertiesInfo(voPropertiesInfo);

        HashSet<String> setOfVOLists = new HashSet<String>();
        for (Map.Entry<String, List<VOPropertyDescription>> entry : voPropertiesInfo
                .entrySet()) {
            // add list operations for all VOs in the list
            setOfVOLists.add(entry.getKey());

            // now check which are only lists on the specified VOs
            List<VOPropertyDescription> list = entry.getValue();
            for (VOPropertyDescription prop : list) {
                // fill set for properties as list of VOs
                if (COLLECTION_TYPES.contains(prop.getType())
                        && !prop.getTypeParameterWithoutPackage().equals(
                                "String")) {
                    if (!setOfVOLists.contains(prop
                            .getTypeParameterWithoutPackage())
                            && !prop.isEnumType()) {
                        setOfVOLists.add(prop.getTypeParameterWithoutPackage());
                    }
                }
            }
        }
        result.setSetOfVOLists(setOfVOLists);

        return result;

    }

    /**
     * Getting list of VO methods.
     * 
     * @return File name.
     */
    public List<String> getVOMethods(String vOName) {
        String fullClassName = VO_PACKAGE_NAME + "." + vOName;

        ArrayList<String> voMethodsList = new ArrayList<String>();

        try {
            Class<?> cl = Class.forName(fullClassName);

            Method mtd[] = cl.getMethods();

            for (int i = 0; i < mtd.length; i++) {
                String methodName = mtd[i].getName();
                voMethodsList.add(methodName);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return voMethodsList;
    }

    /**
     * Getting list of VO properties.
     * 
     * @param vOName
     *            Name of VO.
     * @return List of VO properties.
     */
    public List<VOPropertyDescription> getVOProperties(String vOName) {
        String fullClassName = VO_PACKAGE_NAME + "." + vOName;

        ArrayList<VOPropertyDescription> voList = new ArrayList<VOPropertyDescription>();

        try {
            Class<?> cl = Class.forName(fullClassName);

            Field fld[] = cl.getDeclaredFields();

            // System.out.println("Superclass : " + superClassName);
            if (isSuperClassOfTypeBaseVO(cl)) {

                // add key and version from BaseVO
                VOPropertyDescription descrBaseKey = new VOPropertyDescription();
                descrBaseKey.setName("key");
                descrBaseKey.setGenericType("long");
                descrBaseKey.setType("long");
                descrBaseKey.setTypeParameter("");
                descrBaseKey.setTypeParameterWithoutPackage("");
                voList.add(descrBaseKey);

                VOPropertyDescription descrBaseVersion = new VOPropertyDescription();
                descrBaseVersion.setName("version");
                descrBaseVersion.setGenericType("int");
                descrBaseVersion.setType("int");
                descrBaseVersion.setTypeParameter("");
                descrBaseVersion.setTypeParameterWithoutPackage("");
                voList.add(descrBaseVersion);
            }
            // --

            for (int i = 0; i < fld.length; i++) {
                VOPropertyDescription descr = new VOPropertyDescription();

                int modifiers = fld[i].getModifiers();
                boolean constProp = false;
                if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                    constProp = true;
                }

                String genericType = fld[i].getGenericType().toString();
                String[] inputs = genericType.split("\\<|\\>| ");

                String propertyName = fld[i].getName();

                if (!propertyName.equals("serialVersionUID") && !constProp) {
                    descr.setName(propertyName);
                    descr.setGenericType(genericType);
                    descr.setType(inputs[0]);
                    descr.setEnumType(fld[i].getType().isEnum());
                    if (inputs.length > 1) {
                        descr.setTypeParameter(inputs[1]);
                        String[] parts = inputs[1].split("\\.");
                        descr.setTypeParameterWithoutPackage(parts[parts.length - 1]);
                    } else {
                        descr.setTypeParameter("");
                        descr.setTypeParameterWithoutPackage("");
                    }

                    voList.add(descr);

                    // System.out.println(descr.toString());

                    if (descr.getType().equals("java.util.List")) {
                        if (!setOfVOLists.contains(descr
                                .getTypeParameterWithoutPackage())) {
                            setOfVOLists.add(descr
                                    .getTypeParameterWithoutPackage());
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return voList;
    }

    private boolean isSuperClassOfTypeBaseVO(Class<?> clazz) {
        boolean result = false;
        Class<?> superClass = clazz.getSuperclass();
        while (!result && superClass != null) {
            result = superClass.toString().contains("BaseVO");
            superClass = superClass.getSuperclass();
        }
        return result;
    }

    /**
     * Create a file with all needed methods.
     * 
     * @param stringLowerCase
     */
    public String upperFirstLetter(String stringLowerCase) {

        String stringUpperCase = stringLowerCase.toUpperCase();
        StringBuffer newBufferLowerCase = new StringBuffer(stringLowerCase);
        StringBuffer newBufferUpperCase = new StringBuffer(stringUpperCase);

        newBufferLowerCase.setCharAt(0, newBufferUpperCase.charAt(0));

        return newBufferLowerCase.toString();
    }

    /**
     * Create a file with all needed methods.
     * 
     * @param fileName
     */
    public void createFile(String fileName) {
        FileWriter fstream;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter(fileName);
            out = new BufferedWriter(fstream);

            VOInfo info = getPropertiesInfo();

            HashMap<String, List<VOPropertyDescription>> propertiesInfo = info
                    .getVoPropertiesInfo();

            Set<String> setOfVO = propertiesInfo.keySet();

            out.write("package org.oscm.converter;\n");
            out.write("import java.util.ArrayList;");
            out.write("import java.util.List;");
            out.write("public class VOConverter {\n");
            for (String voName : setOfVO) {

                // generate convert method from old to actual version
                generateConverterMethodCode(out, voName, VERSION_UP,
                        propertiesInfo, VO_PACKAGE_NAME, VO_PACKAGE_NAME_OLD);

                // generate convert method from actual to old version
                generateConverterMethodCode(out, voName, VERSION_OLD,
                        propertiesInfo, VO_PACKAGE_NAME_OLD, VO_PACKAGE_NAME);

            }

            // create methods for converting Lists of VOs
            HashSet<String> setOfVOList = info.getSetOfVOLists();

            for (String voName : setOfVOList) {
                // create methods for converting Lists of VOs to actual
                generateConverterMethodCodeForListOfVO(out, voName, VERSION_UP,
                        VO_PACKAGE_NAME, VO_PACKAGE_NAME_OLD);

                // create methods for converting Lists of VOs to old version
                generateConverterMethodCodeForListOfVO(out, voName,
                        VERSION_OLD, VO_PACKAGE_NAME_OLD, VO_PACKAGE_NAME);

            }
            out.write("}\n");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(out);
        }
    }

    private void close(Writer out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generate code for converter method for VOs. Use this method for
     * converting in both direction.
     * 
     * @param out
     * @param voName
     * @param version
     * @param propertiesInfo
     * @param packageNameTarget
     * @param packageNameSource
     * @throws IOException
     */
    private void generateConverterMethodCode(BufferedWriter out, String voName,
            String version,
            HashMap<String, List<VOPropertyDescription>> propertiesInfo,
            String packageNameTarget, String packageNameSource)
            throws IOException {
        // generate convert method from 1.1 to actual version
        out.write("/**\n");
        out.write(" * Convert source version VO to target version VO.\n");
        out.write(" * \n");
        out.write("* @param oldVO VO to convert.\n");
        out.write("* @return VO of target version.\n");
        out.write("*/\n");

        out.write("public static " + packageNameTarget + "." + voName
                + " convertTo" + version + "(" + packageNameSource + "."
                + voName + " oldVO) {\n");
        out.write("if (oldVO == null) {return null;}\n");
        out.write(packageNameTarget + "." + voName + " newVO = new "
                + packageNameTarget + "." + voName + "();\n");

        List<VOPropertyDescription> voPropertiesList = propertiesInfo
                .get(voName);
        for (VOPropertyDescription element : voPropertiesList) {
            String prop = element.getName();
            String propUpper = upperFirstLetter(prop);
            String typeParameterWithoutPackage = element
                    .getTypeParameterWithoutPackage();

            if (element.getType().equals("java.util.List")
                    && !typeParameterWithoutPackage.equals("String")) {
                String propTypeParam = element.getTypeParameterWithoutPackage();
                out.write("newVO.set" + propUpper + "(convertTo" + version
                        + propTypeParam + "(oldVO.get" + propUpper + "()));\n");
            } else if (element.isEnumType()) {

                out.write("newVO.set" + propUpper
                        + "(EnumConverter.convert(oldVO.get" + propUpper
                        + "(), ");
                String type = element.getTypeParameter();
                if (packageNameTarget.endsWith(OLD_SUFFIX)) {
                    out.write(type.replace(typeParameterWithoutPackage,
                            OLD_SUFFIX + '.' + typeParameterWithoutPackage));
                } else {
                    out.write(type);
                }
                out.write(".class));\n");
            } else {
                if (typeParameterWithoutPackage.contains("VO")) {
                    out.write("newVO.set" + propUpper + "(convertTo" + version
                            + "(oldVO.get" + propUpper + "()));\n");

                } else if (element.getType().equals("boolean")) {
                    out.write("newVO.set" + propUpper + "(oldVO.is" + propUpper
                            + "());\n");
                } else {
                    out.write("newVO.set" + propUpper + "(oldVO.get"
                            + propUpper + "());\n");
                }
            }
        }

        out.write("return newVO;\n");
        out.write("}\n");
    }

    /**
     * Generate code for converter method for VO list. Use this method for
     * converting in both direction.
     * 
     * @param out
     * @param voName
     * @param version
     * @param packageNameTarget
     * @param packageNameSource
     * @throws IOException
     */
    private void generateConverterMethodCodeForListOfVO(BufferedWriter out,
            String voName, String version, String packageNameTarget,
            String packageNameSource) throws IOException {
        String voNameUpper = upperFirstLetter(voName);
        out.write("/**\n");
        out.write("* Convert list of " + voName + ".\n");
        out.write("* @param oldVO List of VO to convert.\n");
        out.write("* @return Converted list of VO.\n");
        out.write("*/\n");

        out.write("public static List<" + packageNameTarget + "." + voName
                + "> convertTo" + version + voNameUpper + "(List<"
                + packageNameSource + "." + voName + "> oldVO) {\n");
        out.write("if (oldVO == null) {return null;}\n");
        out.write("List<" + packageNameTarget + "." + voName
                + "> newVO = new ArrayList<" + packageNameTarget + "." + voName
                + ">();\n");

        out.write("for (" + packageNameSource + "." + voName
                + " tmp : oldVO) { newVO.add(convertTo" + version + "(tmp)); }");

        out.write("return newVO;\n");
        out.write("}\n");
    }

}
