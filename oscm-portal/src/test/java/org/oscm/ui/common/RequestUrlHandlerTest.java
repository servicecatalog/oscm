/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mao                                                    
 *                                                                              
 *  Creation Date: 29.08.2013                                                      
 *                                                                              
 *  Completion Time: 29.08.2013                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mao
 * 
 */

public class RequestUrlHandlerTest {

    private static final String WHITE_LABEL_PATH = "http://localhost:8180/oscm-portal";

    private static final String INVALID_URL = "http://thisisaunittest.com";

    @Test(expected = IOException.class)
    public void isUrlAvailable_IOException() throws IOException {
        // when
        boolean result = RequestUrlHandler.isUrlAccessible(INVALID_URL);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isUrlAvailable_NullOrEmpty() throws IOException {
        // when
        boolean result1 = RequestUrlHandler.isUrlAccessible(null);
        boolean result2 = RequestUrlHandler.isUrlAccessible(null);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result1));
        assertEquals(Boolean.FALSE, Boolean.valueOf(result2));
    }

    @Ignore
    // FIXME : No!! This attempts a real connection! Furthermore the URL is
    // hard-coded!
    // Mock the connection - for real connection test create an integration test
    // it the IT project!
    @Test
    public void isUrlAvailable_Succeed() throws IOException {
        // when
        boolean result = RequestUrlHandler.isUrlAccessible(WHITE_LABEL_PATH);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

}
