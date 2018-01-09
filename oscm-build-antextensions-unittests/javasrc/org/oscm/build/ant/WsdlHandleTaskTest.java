/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年2月9日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author qiu
 * 
 */
public class WsdlHandleTaskTest {
    private static final String WSDL_FILE = "javares/ProvisioningService.wsdl";
    private static final String TEST_FILE = "javares/test.wsdl";
    private WsdlHandleTask task;

    @BeforeClass
    public static void copyWsdl() throws IOException {
        copyFile(new File(WSDL_FILE), new File(TEST_FILE));
    }

    @AfterClass
    public static void clean() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    private static void copyFile(File sourceFile, File targetFile)
            throws IOException {
        BufferedInputStream inBuf = null;
        BufferedOutputStream outBuf = null;
        try {
            inBuf = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuf = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024];
            int len;
            while ((len = inBuf.read(b)) != -1) {
                outBuf.write(b, 0, len);
            }
            outBuf.flush();
        } finally {
            if (inBuf != null)
                inBuf.close();
            if (outBuf != null)
                outBuf.close();
        }
    }

    @Before
    public void setUp() {
        task = new WsdlHandleTask();
        task.setDirName("javares");
        task.setFileName("test.wsdl");
        task.setVersion("1.9");
    }

    @Test
    public void execute() throws Exception {
        // when
        task.execute();
        // then
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(TEST_FILE));
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath path = xpfactory.newXPath();
        Element node = (Element) path.evaluate("/definitions/documentation",
                doc, XPathConstants.NODE);
        assertEquals("v1.7", node.getTextContent());
    }

    @Test(expected = BuildException.class)
    public void execute_dirEmpty() {
        // given
        task.setDirName("");
        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_fileEmpty() {
        // given
        task.setFileName("");
        // when
        task.execute();
    }

    @Test(expected = BuildException.class)
    public void execute_fileNotExist() {
        // given
        task.setFileName("dummy.wsdl");
        // when
        task.execute();
    }
}
