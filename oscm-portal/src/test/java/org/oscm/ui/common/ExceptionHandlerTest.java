/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.11.2010                                                      
 *                                                                              
 *  Completion Time: 11.11.2010                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Tests for the exception handler internal methods.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ExceptionHandlerTest {

    @Test
    public void testGetKeyForParam_NullInput() {
        String keyForParam = ExceptionHandler.getKeyForParam(null);
        assertNull(keyForParam);
    }

    @Test
    public void testGetKeyForParam_EmptyInput() {
        String keyForParam = ExceptionHandler.getKeyForParam("");
        assertNull(keyForParam);
    }

    @Test
    public void testGetKeyForParam_NonEnumInput() {
        String keyForParam = ExceptionHandler.getKeyForParam("someEntry");
        assertNull(keyForParam);
    }

    @Test
    public void testGetKeyForParam_EnumInput() {
        String keyForParam = ExceptionHandler
                .getKeyForParam("enum.EnumClass.someEntry");
        assertNotNull(keyForParam);
        assertEquals("EnumClass.someEntry", keyForParam);
    }

    @Test
    public void getSaasException() {
        Exception givenEx = new EJBException(
                new ConcurrentModificationException());
        Exception mappedEx = ExceptionHandler.getSaasApplicationException(givenEx);
        assertTrue(mappedEx instanceof SaaSApplicationException);
    }

    @Test
    public void getSaasException_nested() {
        Exception givenEx = new EJBException(new EJBException(
                new ConcurrentModificationException()));
        Exception mappedEx = ExceptionHandler.getSaasApplicationException(givenEx);
        assertTrue(mappedEx instanceof SaaSApplicationException);
    }

    @Test
    public void getSaasException_noSaasException() {
        Exception givenEx = new EJBException();
        assertNull(ExceptionHandler.getSaasApplicationException(givenEx));
    }

}
