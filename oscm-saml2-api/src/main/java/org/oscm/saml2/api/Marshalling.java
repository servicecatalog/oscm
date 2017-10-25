/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author roderus
 * 
 */
public class Marshalling<T> {
    public Document marshallElement(JAXBElement<T> element) throws Exception {
        Document finalDocument = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

        Marshaller marshaller = JAXBContext.newInstance(
                element.getDeclaredType()).createMarshaller();

        marshaller.marshal(element, finalDocument);

        return finalDocument;
    }
}
