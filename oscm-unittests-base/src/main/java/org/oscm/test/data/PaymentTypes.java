/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 13.10.2011                                                      
 *                                                                              
 *  Completion Time: 13.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.util.ArrayList;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author weiser
 * 
 */
public class PaymentTypes {

    /**
     * Enables the payment types with the given ids for the supplier. The
     * reference between operator and supplier will be created if not existing.
     * 
     * @param operator
     *            the platform operator {@link Organization}
     * @param supplier
     *            the supplier {@link Organization}
     * @param ds
     *            the {@link DataService}
     * @param customerDefault
     *            if customer default should be set
     * @param serviceDefault
     *            if service default should be set
     * @param types
     *            the payment type ids to enable
     * @throws Exception
     */
    public static final void enableForSupplier(Organization operator,
            Organization supplier, DataService ds, boolean customerDefault,
            boolean serviceDefault, String... types) throws Exception {
        assert (operator.hasRole(OrganizationRoleType.PLATFORM_OPERATOR));
        assert (supplier.hasRole(OrganizationRoleType.SUPPLIER));
        OrganizationRole suppRole = null;
        for (OrganizationToRole ref : supplier.getGrantedRoles()) {
            OrganizationRole role = ref.getOrganizationRole();
            if (role.getRoleName() == OrganizationRoleType.SUPPLIER) {
                suppRole = role;
                break;
            }
        }
        assert (suppRole != null);
        // get or persist the required organization reference
        OrganizationReference orgRef = new OrganizationReference(operator,
                supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        try {
            orgRef = (OrganizationReference) ds
                    .getReferenceByBusinessKey(orgRef);
        } catch (ObjectNotFoundException e) {
            ds.persist(orgRef);
        }
        // create the payment type references
        for (String type : types) {
            PaymentType pt = new PaymentType();
            pt.setPaymentTypeId(type);
            pt = (PaymentType) ds.getReferenceByBusinessKey(pt);

            OrganizationRefToPaymentType ref = new OrganizationRefToPaymentType();
            ref.setOrganizationReference(orgRef);
            ref.setOrganizationRole(suppRole);
            ref.setPaymentType(pt);
            ref.setUsedAsDefault(customerDefault);
            ref.setUsedAsServiceDefault(serviceDefault);
            ds.persist(ref);
        }
    }

    /**
     * Enables the payment types with the given ids for the product.
     * 
     * @param product
     *            the {@link Product}
     * @param ds
     *            the {@link DataService}
     * @param types
     *            the payment type ids to enable
     * @throws ObjectNotFoundException
     */
    public static void enableForProduct(Product product, DataService ds,
            String... types) throws ObjectNotFoundException {
        assert (product != null);
        List<ProductToPaymentType> paymentTypes = new ArrayList<ProductToPaymentType>();
        for (String type : types) {
            PaymentType pt = new PaymentType();
            pt.setPaymentTypeId(type);
            pt = (PaymentType) ds.getReferenceByBusinessKey(pt);

            ProductToPaymentType ptpt = new ProductToPaymentType(product, pt);
            try {
                ds.persist(ptpt);
            } catch (NonUniqueBusinessKeyException e) {
                // already there - ignore
            }
            paymentTypes.add(ptpt);
        }
        product.setPaymentTypes(paymentTypes);
    }
}
