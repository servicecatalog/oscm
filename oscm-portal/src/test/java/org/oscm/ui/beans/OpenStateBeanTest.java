/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.01.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;

/**
 * Unit tests fo the CollapseStateBean class.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OpenStateBeanTest {

    private OpenStateBean testBean;
    private Map<String, String> states;

    @Before
    public void setUp() {
        testBean = new OpenStateBean();
        states = testBean.getStates();
        new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void testGetStates_ForNewIdentifier() {
        assertTrue(Boolean.parseBoolean(states.get("newId")));
    }

    @Test
    public void testGetStates_ForExistingIdentifier() {
        states.put("newId", Boolean.FALSE.toString());
        assertFalse(Boolean.parseBoolean(states.get("newId")));
    }

    @Test
    public void testGetStates_ForNullPutCall() {
        states.put("newId", Boolean.FALSE.toString());
        states.put("newId", null);
        assertTrue(Boolean.parseBoolean(states.get("newId")));
    }

}
