/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.10.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.xml;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Utility class for working with XML transformers.
 * 
 * @author kulle
 * 
 */
public class Transformers {

    /**
     * Creates a XML transformer with the following properties:<br />
     * <br />
     * character encoding: UTF-8<br />
     * output method: xml<br />
     * output version: 1.0
     */
    public static Transformer newTransformer()
            throws TransformerConfigurationException {

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        return transformer;
    }

    /**
     * Creates a XML transformer with the following properties:<br />
     * <br />
     * character encoding: UTF-8<br />
     * output method: xml<br />
     * output version: 1.0
     */
    public static Transformer newTransformer(Source source)
            throws TransformerConfigurationException {

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(source);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        return transformer;
    }

    /**
     * Creates a XML transformer with the following properties:<br />
     * <br />
     * character encoding: UTF-8<br />
     * output method: xml<br />
     * output version: 1.0<br />
     * indention size: 2
     */
    public static Transformer newFormatingTransformer()
            throws TransformerConfigurationException {

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xalan}indent-amount", "2");
        return transformer;
    }

}
