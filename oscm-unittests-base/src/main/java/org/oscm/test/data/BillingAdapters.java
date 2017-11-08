/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.09.2012                                                                                                                                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Utility class for the creation of BillingAdapter
 * 
 * @author stavreva
 */
public class BillingAdapters {

    public static BillingAdapter createBillingAdapter(DataService ds,
            String billingIdentifier, boolean isDefaultAdapter)
                    throws NonUniqueBusinessKeyException {
        BillingAdapter adapter = new BillingAdapter();
        adapter.setBillingIdentifier(billingIdentifier);
        adapter.setName("Adapter Name");
        adapter.setDefaultAdapter(isDefaultAdapter);
        ds.persist(adapter);
        return adapter;
    }

}
