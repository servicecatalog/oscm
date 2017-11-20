/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.12.2010                                                      
 *                                                                              
 *  Completion Time: 07.12.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the evaluation methods of the BilllingConditionsEvaluatorTest.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BillingConditionsEvaluatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testCreate() {
        // only for coverage
        assertNotNull(new BillingConditionsEvaluator());
    }

}
