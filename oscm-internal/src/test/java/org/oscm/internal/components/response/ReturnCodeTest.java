/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components.response;

import static org.oscm.test.matchers.JavaMatchers.hasToString;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test cases for return codes.
 * 
 * @author cheld
 */
public class ReturnCodeTest {

    /**
     * toString should be readable for debugging purpose
     */
    @Test
    public void toString_Info() {
        ReturnCode returnCode = new ReturnCode(ReturnType.INFO, "KEY");
        assertThat(returnCode, hasToString());
    }

    /**
     * toString should be readable for debugging purpose
     */
    @Test
    public void toString_Warning() {
        ReturnCode returnCode = new ReturnCode(ReturnType.WARNING, "KEY",
                "param1");
        assertThat(returnCode, hasToString());
    }

}
