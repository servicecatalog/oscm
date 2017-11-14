/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年3月10日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.wsit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author gaowenxin
 * 
 */
public class WSITContentTest {

    private List<Class<?>> wsClasses;

    @Before
    public void setup() throws Exception {
        wsClasses = getClasses("org.oscm.webservices");
    }

    @Test
    public void checkWSITFilesExists() throws Exception {
        for (Class<?> clazz : wsClasses) {
            URL url = clazz.getResource("/wsitconfig/wsit-" + clazz.getName()
                    + ".xml");
            assertNotNull(url);
        }
    }

    @Test
    public void checkWSITContent() throws Exception {
        for (Class<?> clazz : wsClasses) {
            InputStream in = clazz.getResourceAsStream("/wsitconfig/wsit-"
                    + clazz.getName() + ".xml");
            checkMethodDefinitionInXML(in, clazz.getName());
            in.close();
        }
    }

    @Test
    public void checkAllMethodsDefinedInWSIT() throws Exception {
        for (Class<?> clazz : wsClasses) {
            List<String> methodsInXML = getMethodFromXML(clazz);
            for (String methodName : getMethodForClass(clazz)) {
                if (!methodsInXML.contains(methodName)) {
                    fail("Method " + clazz.getName() + "#" + methodName
                            + " does not defined in file wsit-"
                            + clazz.getName() + ".xml");
                }
            }
        }
    }

    private List<String> getMethodForClass(Class<?> clazz) {
        List<String> methodNames = new ArrayList<String>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.contains("jacocoInit")) {
                continue;
            }
            methodNames.add(methodName);
        }
        return methodNames;
    }

    private List<String> getMethodFromXML(Class<?> clazz) throws Exception {
        String className = clazz.getName();
        InputStream in = clazz.getResourceAsStream("/wsitconfig/wsit-"
                + className + ".xml");
        List<String> methods = readMethodNameFromXML(in);
        in.close();
        return methods;
    }

    private List<Class<?>> getClasses(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".class")
                    && !file.getName().contains("Test")
                    && !file.getName().contains("PaymentRegistrationService")) {
                classes.add(Class.forName(packageName
                        + '.'
                        + file.getName().substring(0,
                                file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private List<String> readMethodNameFromXML(InputStream in) throws Exception {
        List<String> methodNames = new ArrayList<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(in);
        NodeList operationNodes = doc.getElementsByTagName("operation");
        for (int i = 0; i < operationNodes.getLength(); i++) {
            Node operation = operationNodes.item(i);
            if (operation != null
                    && operation.getNodeType() == Node.ELEMENT_NODE
                    && operation.getParentNode().getNodeName()
                            .equalsIgnoreCase("portType")) {
                methodNames.add(operation.getAttributes().getNamedItem("name")
                        .getNodeValue());
            }
        }
        return methodNames;
    }

    private void checkMethodDefinitionInXML(InputStream in, String className)
            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(in);
        NodeList operationNodes = doc.getElementsByTagName("operation");
        List<String> portTypeNodeList = new ArrayList<String>();
        for (int i = 0; i < operationNodes.getLength(); i++) {
            Node operation = operationNodes.item(i);
            if (operation != null
                    && operation.getNodeType() == Node.ELEMENT_NODE
                    && operation.getParentNode().getNodeName()
                            .equalsIgnoreCase("portType")) {
                portTypeNodeList.add(operation.getAttributes()
                        .getNamedItem("name").getNodeValue());
            }
        }
        List<String> policyNodeList = new ArrayList<String>();
        for (int i = 0; i < operationNodes.getLength(); i++) {
            Node operation = operationNodes.item(i);
            if (operation != null
                    && operation.getNodeType() == Node.ELEMENT_NODE
                    && operation.getParentNode().getNodeName()
                            .equalsIgnoreCase("binding")) {
                policyNodeList.add(operation.getAttributes()
                        .getNamedItem("name").getNodeValue());
            }
        }

        List<String> messageNodeList = new ArrayList<String>();
        NodeList messageNodes = doc.getElementsByTagName("message");
        for (int i = 0; i < messageNodes.getLength(); i++) {
            Node message = messageNodes.item(i);
            if (message != null && message.getNodeType() == Node.ELEMENT_NODE) {
                messageNodeList.add(message.getAttributes()
                        .getNamedItem("name").getNodeValue());
            }
        }

        for (String methodName : portTypeNodeList) {
            assertTrue("Method " + className + "#" + methodName
                    + " does not defined with policy.",
                    policyNodeList.contains(methodName));
            assertTrue("Method " + className + "#" + methodName
                    + " does not defined with message.",
                    messageNodeList.contains(methodName));
            assertTrue("Method " + className + "#" + methodName
                    + " does not defined with message response.",
                    messageNodeList.contains(methodName + "Response"));

        }
    }
}
