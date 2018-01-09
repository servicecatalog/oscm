/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Aug 4, 2011                                                      
 *                                                                              
 *  Completion Time: Aug 4, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.xml.sax.InputSource;

/**
 * Custom ANT condition evaluating boolean XPath expressions on a given file.
 * 
 * @author Dirk Bernsau
 * 
 */
public class XPathCondition implements Condition {

    private String fileName;
    private String path;

    /**
     * @param file
     *            the file to look in
     */
    public void setFile(String file) {
        this.fileName = file;
    }

    /**
     * @param path
     *            the xPath expression to validate
     */
    public void setPath(String path) {
        this.path = path;
    }

    public boolean eval() throws BuildException {
        if (nullOrEmpty(fileName)) {
            throw new BuildException("No file set");
        }
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new BuildException(
                    "The specified file does not exist or is a directory");
        }
        if (nullOrEmpty(path)) {
            throw new BuildException("No XPath expression set");
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(fileName);
        Boolean result = Boolean.FALSE;
        try {
            result = (Boolean) xpath.evaluate(path, inputSource,
                    XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new BuildException("XPath expression fails", e);
        }
        return result.booleanValue();
    }

    private boolean nullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
