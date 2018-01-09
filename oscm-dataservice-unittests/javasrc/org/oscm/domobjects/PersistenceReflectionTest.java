/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 20, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author Dirk Bernsau
 * 
 */
public class PersistenceReflectionTest {

    @Test
    public void testGetGetterName() throws Exception {
        String name = PersistenceReflection.getGetterName("field");
        assertEquals("Wrong name - ", "getField", name);
    }

    @Test
    public void testGetFieldName() throws Exception {
        String name = PersistenceReflection.getFieldName("getField");
        assertEquals("Wrong name - ", "field", name);
    }

    @Test
    public void testGetFieldNameNull() throws Exception {
        String name = PersistenceReflection.getFieldName("setField");
        assertNull("No result expected", name);
    }

    @Test
    public void testValue() throws Exception {
        Product p = new Product();
        String id = "myId";
        p.setProductId(id);
        Object value = PersistenceReflection.getValue(p, "productId");
        assertEquals(id, value);
    }

    @Test(expected = SaaSSystemException.class)
    public void testValueEx() throws Exception {
        Product p = new Product();
        String id = "myId";
        p.setProductId(id);
        PersistenceReflection.getValue(p, "nofield");
    }

}
