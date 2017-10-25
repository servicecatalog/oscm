/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 4,2015                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.build.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author qiu
 * 
 */
public class WsdlHandleTask extends Task {

    private String dirName;
    private String fileName;
    private String version;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void execute() throws BuildException {
        if (StringUtils.isEmpty(dirName)) {
            throw new BuildException("dirname is mandatory");
        }
        if (StringUtils.isEmpty(fileName)) {
            throw new BuildException("filename is mandatory");
        }
        File file = new File(dirName + File.separator + fileName);
        if (!file.exists()) {
            throw new BuildException("wsdl file does not exist");
        }
        try {
            Document doc = parse(file);
            Element element = doc.createElement("documentation");
            element.setTextContent("v" + version);

            doc.getElementsByTagName("definitions").item(0)
                    .appendChild(element);

            save(dirName + File.separator + fileName, doc);
        } catch (ParserConfigurationException | SAXException | IOException
                | TransformerException e) {
            throw new BuildException(e);
        }
    }

    private Document parse(File file) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private void save(String fileName, Document doc)
            throws TransformerException, IOException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource();
        source.setNode(doc);
        StreamResult result = new StreamResult();
        OutputStream out = new FileOutputStream(fileName);
        result.setOutputStream(out);
        transformer.transform(source, result);
        out.close();
    }
}
