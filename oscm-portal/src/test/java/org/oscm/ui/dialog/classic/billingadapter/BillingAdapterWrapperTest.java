/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 17.08.16 13:26
 *
 ******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.oscm.internal.billingadapter.ConnectionPropertyItem;
import org.oscm.internal.billingadapter.POBillingAdapter;
import org.oscm.internal.types.exception.SaaSApplicationException;

import junit.framework.Assert;
import org.oscm.validation.Invariants;

/**
 * Authored by dawidch
 */

public class BillingAdapterWrapperTest {
    @Test
    public void addBillingAdapterTest() throws SaaSApplicationException {
        // given
        POBillingAdapter poObject = new POBillingAdapter();
        Set<ConnectionPropertyItem> cp = new HashSet<>();
        cp.add(new ConnectionPropertyItem("1", "2"));
        poObject.setConnectionProperties(cp);
        BillingAdapterWrapper baw = new BillingAdapterWrapper(poObject);
        // when
        baw.addItem();
        // then
        Assert.assertEquals(baw.getConnectionProperties().size(), 2);
        baw.removeItem(0);
        Assert.assertEquals(baw.getConnectionProperties().size(), 1);
        baw.setConnectionProperties(null);
        Invariants.assertNull(baw.getConnectionProperties());
    }
}
