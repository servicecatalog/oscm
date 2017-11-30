/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oscm.internal.vo.VOPriceModel;

public class PriceModelTest {

    @Test
    public void getTimezone() throws Exception {
        // given a price model in the local timezone
        PriceModel priceModel = new PriceModel(new VOPriceModel());

        // when
        String timezone = priceModel.getTimezone();

        // then check the UTC timezone
        assertNotNull(timezone);
        assertTrue(timezone.startsWith("UTC"));
    }

}
