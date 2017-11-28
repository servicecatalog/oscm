/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 06.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api.model;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import org.oscm.saml2.api.model.metadata.MetadataFactory;

/**
 * @author roderus
 * 
 */
public class ObjectFactoryTest {

    private Object[] objectFactories;

    @Before
    public void setup() {
        objectFactories = new Object[5];
        objectFactories[0] = new org.oscm.saml2.api.model.protocol.ObjectFactory();
        objectFactories[1] = new org.oscm.saml2.api.model.assertion.ObjectFactory();
        objectFactories[2] = new org.oscm.saml2.api.model.xmldsig.ObjectFactory();
        objectFactories[3] = new org.oscm.saml2.api.model.xmlenc.ObjectFactory();
        objectFactories[4] = new MetadataFactory();
    }

    @Test
    public void callAllCreators() throws Exception {

        for (Object objectFactory : objectFactories) {
            for (Method factoryMethod : objectFactory.getClass().getMethods()) {

                if (factoryMethod.getReturnType().isAssignableFrom(
                        JAXBElement.class)) {
                    Class<?>[] parameters = factoryMethod.getParameterTypes();

                    if (!parameters[0].getName().contains("BaseIDAbstractType")) {
                        Object methodArgument = getInputArgument(parameters[0],
                                true);
                        JAXBElement<?> element = (JAXBElement<?>) factoryMethod
                                .invoke(objectFactory, methodArgument);

                        assertTrue(element.getDeclaredType().isAssignableFrom(
                                methodArgument.getClass()));
                    }
                }
            }
        }
    }

    private void testSettersAndGetters(Object typeObject) throws Exception {
        for (Method method : typeObject.getClass().getMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {

                String setMethodName = methodName;
                if (setMethodName.startsWith("is")) {
                    setMethodName = setMethodName.replace("is", "set");
                } else {
                    setMethodName = setMethodName.replace("get", "set");
                }
                try {
                    Method setMethod = typeObject.getClass().getMethod(
                            setMethodName, method.getReturnType());
                    Class<?>[] parameters = setMethod.getParameterTypes();
                    if (!parameters[0].getName().contains("BaseIDAbstractType")) {
                        Object inputArgument = getInputArgument(parameters[0],
                                false);
                        setMethod.invoke(typeObject, inputArgument);
                    }
                } catch (NoSuchMethodException e) {
                    // the getter does not have a corresponding setter
                }

                method.invoke(typeObject);
            }
        }
    }

    private Object getInputArgument(Class<?> clazz,
            boolean testSettersAndGetters) throws Exception {

        if (clazz.isAssignableFrom(String.class)) {
            return new String("Dummy Value String");
        } else if (clazz.isAssignableFrom(XMLGregorianCalendar.class)) {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar();
        } else if (clazz.isAssignableFrom(Boolean.class)) {
            return new Boolean(true);
        } else if (clazz.isAssignableFrom(Integer.class)) {
            return new Integer(5);
        } else if (clazz.isAssignableFrom(BigInteger.class)) {
            return new BigInteger("5");
        } else if (clazz.isAssignableFrom(byte[].class)) {
            return new byte[5];
        } else if (clazz.isAssignableFrom(Duration.class)) {
            return DatatypeFactory.newInstance().newDuration(100L);
        } else if (clazz.isEnum()) {
            return clazz.getEnumConstants()[0];
        } else {
            Object typeObject = createTypeFromObjectFactory(clazz);
            if (testSettersAndGetters) {
                testSettersAndGetters(typeObject);
            }
            return typeObject;
        }
    }

    private Object createTypeFromObjectFactory(Class<?> typeClass)
            throws Exception {

        for (Object objectFactory : objectFactories) {
            for (Method m : objectFactory.getClass().getMethods()) {
                if (typeClass.isAssignableFrom(m.getReturnType())) {
                    return m.invoke(objectFactory);
                }
            }
        }

        throw new NoSuchMethodException();
    }
}
