/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.oscm.apiversioning.enums.ModificationType;

/**
 * @author zhaoh.fnst
 * 
 */
public class AdapterFactoryTest {

    @SuppressWarnings("unused")
    private AdapterFactory adapter;

    @Before
    public void setUp() throws Exception {
        adapter = new AdapterFactory();
    }

    @Test
    public void getAdapter_Add() throws Exception {
        // given
        ModificationType type = ModificationType.ADD;

        // when
        IAdapter result = AdapterFactory.getAdapter(type);

        // then
        assertTrue(result instanceof AddAdapter);
    }

    @Test
    public void getAdapter_Remove() throws Exception {

        // given
        ModificationType type = ModificationType.ADD;

        // when
        IAdapter result = AdapterFactory.getAdapter(type);

        // then
        assertTrue(result instanceof AddAdapter);
    }

    @Test
    public void getAdapter_AddException() throws Exception {

        // given
        ModificationType type = ModificationType.ADDEXCEPTION;

        // when
        IAdapter result = AdapterFactory.getAdapter(type);

        // then
        assertTrue(result instanceof ExceptionAdapter);
    }

    @Test
    public void getAdapter_Update() throws Exception {

        // given
        ModificationType type = ModificationType.UPDATE;

        // when
        IAdapter result = AdapterFactory.getAdapter(type);

        // then
        assertTrue(result instanceof UpdateAdapter);
    }

    @Test
    public void getAdapter_UpdateField() throws Exception {

        // given
        ModificationType type = ModificationType.UPDATEFIELD;

        // when
        IAdapter result = AdapterFactory.getAdapter(type);

        // then
        assertTrue(result instanceof UpdateFieldAdapter);
    }

    @Test
    public void getAdapter_Exception() throws Exception {

        // given
        ModificationType type = ModificationType.REMOVEFIELD;

        // when
        try {
            AdapterFactory.getAdapter(type);

        } catch (RuntimeException ex) {
            // then
            assertEquals("No adapter is found", ex.getMessage());
        }
    }
}
