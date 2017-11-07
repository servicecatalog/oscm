/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link CharConverter}.
 * 
 * @author zhoum.fnst
 * 
 */
public class CharConverterTest {
    private static final String DBC_STRING = "Ｔｅｓｔ　Ｃａｓｅ　１";
    private static final String MIXED_STRING = "Tｅst　Ｃaｓｅ　１";
    private static final String SBC_STRING = "Test Case 1";

    @Test
    public void converterToSBC() throws Exception {
        // when
        String result = CharConverter.convertToSBC(DBC_STRING);

        // then
        assertEquals(SBC_STRING, result);
    }

    @Test
    public void converterToSBC_MixedString() throws Exception {
        // when
        String result = CharConverter.convertToSBC(MIXED_STRING);

        // then
        assertEquals(SBC_STRING, result);
    }
}
