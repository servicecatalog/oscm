/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author weiser
 * 
 */
public class AuthorizationRequestDataTest {

    private AuthorizationRequestData ard;

    @Before
    public void setup() throws Exception {
        ard = new AuthorizationRequestData();
        ard.setPassword("password");
    }

    @Test
    public void isPasswordSet() throws Exception {
        assertTrue(ard.isPasswordSet());
    }

    @Test
    public void isPasswordSet_Null() throws Exception {
        ard.setPassword(null);
        assertFalse(ard.isPasswordSet());
    }

    @Test
    public void isPasswordSet_Empty() throws Exception {
        ard.setPassword("");
        assertFalse(ard.isPasswordSet());
    }

    @Test
    public void isPasswordSet_Whitespaces() throws Exception {
        ard.setPassword("      ");
        assertTrue(ard.isPasswordSet());
    }
}
