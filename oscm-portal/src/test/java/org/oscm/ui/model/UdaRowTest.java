/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 25.10.2010                                                      
 *                                                                              
 *  Completion Time: 25.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.oscm.test.data.Udas;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaRowTest {

    @Test
    public void testUdaRow() {
        VOUdaDefinition def = Udas.createVOUdaDefinition("CUSTOMER", "udaId",
                "defaultValue", UdaConfigurationType.SUPPLIER);
        def.setName("name");
        VOUda uda = Udas.createVOUda(def, null, 12345);
        UdaRow row = new UdaRow(def, uda);
        assertEquals(def.getUdaId(), row.getUdaId());
        assertEquals(uda.getUdaValue(), row.getUdaValue());
        assertEquals(def.getDefaultValue(), row.getUdaValue());
    }

    @Test
    public void testGetUdaRows_BothNull() {
        List<UdaRow> udaRows = UdaRow.getUdaRows(null, null);
        assertNotNull(udaRows);
        assertTrue(udaRows.isEmpty());
    }

    @Test
    public void testGetUdaRows_DefinitionsNull() {
        VOUdaDefinition def = Udas.createVOUdaDefinition("CUSTOMER", "udaId",
                "defaultValue", UdaConfigurationType.SUPPLIER);
        VOUda uda = Udas.createVOUda(def, "value", 12345);
        List<UdaRow> udaRows = UdaRow.getUdaRows(null,
                Collections.singletonList(uda));
        assertNotNull(udaRows);
        assertTrue(udaRows.isEmpty());
    }

    @Test
    public void testGetUdaRows_UdasNull() {
        VOUdaDefinition def = Udas.createVOUdaDefinition("CUSTOMER", "udaId",
                "defaultValue", UdaConfigurationType.SUPPLIER);
        List<UdaRow> udaRows = UdaRow.getUdaRows(
                Collections.singletonList(def), null);
        assertNotNull(udaRows);
        assertEquals(1, udaRows.size());
        UdaRow row = udaRows.get(0);
        assertEquals(def, row.getUdaDefinition());
        assertNotNull(row.getUda());
        assertEquals(0, row.getUda().getKey());
        assertEquals(def.getUdaId(), row.getUdaId());
        assertEquals(def.getDefaultValue(), row.getUdaValue());
    }

    @Test
    public void testGetUdaRows() {
        VOUdaDefinition def = Udas.createVOUdaDefinition("CUSTOMER", "udaId",
                "defaultValue", UdaConfigurationType.SUPPLIER);
        VOUda uda = Udas.createVOUda(def, "value", 12345);
        List<UdaRow> udaRows = UdaRow.getUdaRows(
                Collections.singletonList(def), Collections.singletonList(uda));
        assertNotNull(udaRows);
        assertEquals(1, udaRows.size());
        UdaRow row = udaRows.get(0);
        assertEquals(def, row.getUdaDefinition());
        assertEquals(uda, row.getUda());
    }

    // tbd: refactor
    @Test
    public void isInputRendered_ForSUPPLIER() {
        // given a Uda with SUPPLIER Configuration Type
        UdaRow row = prepareUdaRow(UdaConfigurationType.SUPPLIER);
        // when
        boolean result = row.isInputRendered();
        // then
        assertTrue(result);
    }

    @Test
    public void isInputRendered_ForCUSTOMER() {
        // given a Uda with CUSTOMER Configuration Type
        UdaRow row = prepareUdaRow(UdaConfigurationType.USER_OPTION_MANDATORY);
        // when
        boolean result = row.isInputRendered();
        // then
        assertFalse(result);
    }

    @Test
    public void getUdaNameToShowTest() {
        //given
        UdaRow row = prepareUdaRow(UdaConfigurationType.USER_OPTION_OPTIONAL);

        //when
        String udaNameToShow = row.getUdaNameToShow();

        //then
        assertEquals(udaNameToShow, "name");
    }

    private UdaRow prepareUdaRow(UdaConfigurationType type) {
        VOUdaDefinition def = Udas.createVOUdaDefinition("SUPPLIER", "udaId",
                "defaultValue", type);
        VOUda uda = Udas.createVOUda(def, "value", 12345);
        def.setName("name");
        UdaRow row = new UdaRow(def, uda);
        return row;
    }

}
