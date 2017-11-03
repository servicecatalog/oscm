/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.Arrays;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;

/**
 * @author weiser
 * 
 */
public class OrganizationReferences {

    /**
     * Creates a reference of the passed type between the passed organizations
     * and adds it to their corresponding reference lists.
     * 
     * @param source
     *            the source {@link Organization}
     * @param target
     *            the target {@link Organization}
     * @param type
     *            the {@link OrganizationReferenceType}
     * @return the created {@link OrganizationReference}
     */
    public static OrganizationReference addReference(Organization source,
            Organization target, OrganizationReferenceType type) {
        OrganizationReference ref = new OrganizationReference(source, target,
                type);
        source.getTargets().add(ref);
        target.getSources().add(ref);
        return ref;
    }

    /**
     * Creates the SUPPLIER_TO_CUSTOMER reference between the passed
     * organizations and adds it to their corresponding reference lists.
     * 
     * @param supplier
     *            the supplier {@link Organization}
     * @param customer
     *            the customer {@link Organization}
     * @return the created {@link OrganizationReference}
     */
    public static OrganizationReference addSupplierReference(
            Organization supplier, Organization customer) {
        return addReference(supplier, customer,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
    }

    public static OrganizationRefToPaymentType enablePayment(
            OrganizationReference ref, PaymentType pt) {
        OrganizationRefToPaymentType refToPt = new OrganizationRefToPaymentType();
        refToPt.setOrganizationReference(ref);
        refToPt.setPaymentType(pt);
        refToPt.setUsedAsDefault(true);
        refToPt.setUsedAsServiceDefault(true);
        ref.setPaymentTypes(Arrays.asList(refToPt));
        return refToPt;
    }
}
