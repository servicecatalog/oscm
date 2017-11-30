/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: May 23, 2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.vo.VOPaymentType;

/**
 * @author afschar
 * 
 */
public class SelectedPaymentTypeTest {
    private SelectedPaymentType pt;

    @Before
    public void setup() {
        pt = new SelectedPaymentType(new VOPaymentType());
    }

    @Test
    public void getPaymentIdWithoutBklanks() {
        // given
        pt.getPaymentType().setPaymentTypeId(" XäY  Z ");

        // when
        String result = pt.getPaymentTypeIdWithoutBlanks();

        // then
        assertEquals("_X_Y__Z_", result);
    }

}
