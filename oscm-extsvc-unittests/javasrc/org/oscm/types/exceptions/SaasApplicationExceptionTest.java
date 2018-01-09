/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.11.2010                                                      
 *                                                                              
 *  Completion Time: 11.11.2010                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.types.enumtypes.SubscriptionStatus;

/**
 * Tests for the Saas application exception class.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SaasApplicationExceptionTest {

    private Object[] emptyParams;
    private Object[] stringParams;
    private Object[] enumParams;
    private Object[] complexParams;
    private Object[] nullParams;

    @Before
    public void setUp() {
        emptyParams = new Object[0];
        String content = "sampleEntry";
        SubscriptionStatus status = SubscriptionStatus.DEACTIVATED;
        stringParams = new Object[] { content };
        enumParams = new Object[] { status };
        complexParams = new Object[] { content, status };
        nullParams = new Object[] { null };
    }

    @Test
    public void testGetMessageParams_NullInput() throws Exception {
        SaaSApplicationException ex = new SaaSApplicationException(
                (Object[]) null);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetMessageParams_EmptyInput() throws Exception {
        SaaSApplicationException ex = new SaaSApplicationException(emptyParams);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetMessageParams_StringInput() throws Exception {
        SaaSApplicationException ex = new SaaSApplicationException(stringParams);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(1, result.length);
        String entry = result[0];
        assertEquals("sampleEntry", entry);
    }

    @Test
    public void testGetMessageParams_EnumInput() throws Exception {
        SaaSApplicationException ex = new SaaSApplicationException(enumParams);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(1, result.length);
        String entry = result[0];
        assertEquals("enum.SubscriptionStatus.DEACTIVATED", entry);
    }

    @Test
    public void testGetMessageParams_ComplexInput() throws Exception {
        CurrencyException ex = new CurrencyException("message", complexParams);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(2, result.length);
        String entry = result[0];
        assertEquals("sampleEntry", entry);
        entry = result[1];
        assertEquals("enum.SubscriptionStatus.DEACTIVATED", entry);
    }

    /**
     * Bug 9187: Ensures the message parameters contain no new lines, so the
     * message is not confusing nor considered as 'Log forging' by a static code
     * analysis tool. See: https://www.fortify.com/vulncat/en/vulncat/index.html
     */
    @Test
    public void escapeParams_NoNewlinesInParameters() {
        // given:
        String param1 = "1001'\nALARM: BAD HACKER just logger in\n\n'1001";

        // when
        String[] msg = SaaSApplicationException.escapeParams(param1);

        // then: the \n is escaped to \\n
        assertEquals("1001'\\nALARM: BAD HACKER just logger in\\n\\n'1001",
                msg[0]);
    }

    @Test
    public void testGetMessageParams_NullParamInput() throws Exception {
        CurrencyException ex = new CurrencyException("message", nullParams);
        String[] result = ex.getMessageParams();
        assertNotNull(result);
        assertEquals(1, result.length);
        String entry = result[0];
        assertNull(entry);
    }
}
