/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

public class Discounts {
    public static Discount createDiscount(DataService mgr,
            OrganizationReference orgReference, BigDecimal value)
            throws NonUniqueBusinessKeyException {

        Discount discount = new Discount();
        discount.setOrganizationReference(orgReference);
        discount.setValue(value);

        mgr.persist(discount);
        mgr.flush();
        mgr.refresh(discount);
        return discount;
    }
}
