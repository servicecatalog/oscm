/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 26, 2014       
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * unit test for TimeStampUtil
 * 
 * @author cmin
 * 
 */
public class TimeStampUtilTest {
    private final String PRODUCT_ID = "PROD_ID";

    @Test
    public void removeTimestampFromId_withTimeStamp() {
        // given
        String productId = PRODUCT_ID + "#"
                + String.valueOf(System.currentTimeMillis());

        // when
        String result = TimeStampUtil.removeTimestampFromId(productId);

        // then
        assertEquals(PRODUCT_ID, result);
    }

    @Test
    public void removeTimestampFromId() {
        // given
        String productId = PRODUCT_ID;

        // when
        String result = TimeStampUtil.removeTimestampFromId(productId);

        // then
        assertEquals(PRODUCT_ID, result);
    }

    @Test
    public void removeTimestampFromId_null() {
        // given
        String productId = null;

        // when
        String result = TimeStampUtil.removeTimestampFromId(productId);

        // then
        assertNull(result);
    }

}
