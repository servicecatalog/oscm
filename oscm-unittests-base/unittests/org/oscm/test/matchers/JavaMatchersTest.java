/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld //TODO                                                      
 *                                                                              
 *  Creation Date: 18.06.2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.matchers;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test class for java matchers. See hamcrest.
 * 
 * @author cheld
 * 
 */
public class JavaMatchersTest {

    static class TestWithoutToString {
    }

    static class TestWithToString {
        @Override
        public String toString() {
            return "test";
        }
    }

    @Test
    public void hasToString_negative() {
        assertThat(new TestWithoutToString(), not(JavaMatchers.hasToString()));
    }

    @Test
    public void hasToString() {
        assertThat(new TestWithToString(), JavaMatchers.hasToString());
    }
    
    @Test
    public void hasItemInArray(){
        assertThat(new String[] { "a", "b" }, JavaMatchers.hasItemInArray());
    }

    @Test
    public void hasItemInArray_negative() {
        assertThat(new String[] {}, not(JavaMatchers.hasItemInArray()));
    }
}
