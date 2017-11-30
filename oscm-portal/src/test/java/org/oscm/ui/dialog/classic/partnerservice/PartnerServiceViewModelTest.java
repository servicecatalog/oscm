/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar //TODO                                                      
 *                                                                              
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.partnerservice;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.internal.partnerservice.POPartnerServiceDetails;

/**
 * @author afschar
 * 
 */
public class PartnerServiceViewModelTest {

    @Test
    public void getPartnerServiceDetails() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();

        // when
        POPartnerServiceDetails s = m.getPartnerServiceDetails();

        // then
        Assert.assertNull(s);
    }

    @Test
    public void setPartnerServiceDetails() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();
        POPartnerServiceDetails s = new POPartnerServiceDetails();

        // when
        m.setPartnerServiceDetails(s);

        // then
        Assert.assertTrue(s == m.getPartnerServiceDetails());
    }

    @Test
    public void getSelectedServiceKey() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();

        // when
        long l = m.getSelectedServiceKey();

        // then
        Assert.assertEquals(l, 0);
    }

    @Test
    public void setSelectedServiceKey() {
        // given
        PartnerServiceViewModel m = new PartnerServiceViewModel();

        // when
        m.setSelectedServiceKey(5);

        // then
        Assert.assertEquals(5, m.getSelectedServiceKey());
    }

}
