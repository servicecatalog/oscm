/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 21.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author weiser
 * 
 */
public class OperationServiceAdapterFactoryTest {

    private TechnicalProductOperation tpo;

    @Before
    public void setup() {
        tpo = new TechnicalProductOperation();
    }

    @Test(expected = SaaSSystemException.class)
    public void getNotificationServiceAdapter_TargetNull() throws Exception {
        tpo.setActionUrl(null);

        OperationServiceAdapterFactory.getOperationServiceAdapter(tpo, null,
                null, null);
    }

    @Test(expected = SaaSSystemException.class)
    public void getNotificationServiceAdapter_TargetEmpty() throws Exception {
        tpo.setActionUrl("  ");

        OperationServiceAdapterFactory.getOperationServiceAdapter(tpo, null,
                null, null);
    }
}
