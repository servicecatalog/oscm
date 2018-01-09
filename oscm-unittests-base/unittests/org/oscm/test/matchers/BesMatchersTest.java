/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                           
 *                                                                              
 *  Creation Date: 24.07.2012                                                      
 *                                                                                                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.test.matchers;

import static org.oscm.test.matchers.BesMatchers.isPersisted;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.oscm.internal.vo.VOService;

/**
 * 
 * Test cases for Bes matchers
 * 
 * @author cheld
 * 
 */
public class BesMatchersTest {

    @Test
    public void testIsPersisted() {
        VOService voObject = new VOService();
        voObject.setKey(3);
        assertThat(voObject, isPersisted());
    }

    @Test
    public void testIsPersisted_negative() {
        VOService voObject = new VOService();
        assertThat(voObject, not(isPersisted()));
    }

}
