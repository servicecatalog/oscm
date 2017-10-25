/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 25.06.15 12:09
 *
 *******************************************************************************/

package org.oscm.converter.utils;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class JaxbConverter {

    private static final String JAXB_FORMATTED_OUT = "jaxb.formatted.output";

    private static final String CODING = "UTF-8";

    public static String toXML(Object objToConvert) {
        try {
            final JAXBContext context = JAXBContext.newInstance(objToConvert.getClass());
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(JAXB_FORMATTED_OUT, Boolean.FALSE);
            marshaller.marshal(objToConvert, out);

            return new String(out.toByteArray(), CODING);
        } catch (JAXBException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T fromXML(String xmlString, Class<T> targetClass) {
        try {
            final JAXBContext context = JAXBContext.newInstance(targetClass);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(new StringReader(xmlString));

            return unmarshaller.unmarshal(source, targetClass).getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
