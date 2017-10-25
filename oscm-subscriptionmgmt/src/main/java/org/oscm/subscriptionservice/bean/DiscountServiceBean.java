/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.accountservice.assembler.DiscountAssembler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VODiscount;

/**
 * Session Bean implementation class of DiscountService (Remote IF)
 */
@Stateless
@Remote(DiscountService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class DiscountServiceBean implements DiscountService {

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    public VODiscount getDiscountForService(long serviceKey)
            throws ObjectNotFoundException {

        VODiscount voDiscount = null;
        // Retrieve the current logged-in user, if present.
        PlatformUser currentUser = dm.getCurrentUserIfPresent();

        // If no user is logged-in (which can happen if the user accesses the
        // public catalog without first logging-in) then return null as a
        // discount.
        if (currentUser == null) {
            return null;
        }
        Organization customer = currentUser.getOrganization();

        // Retrieve the supplier of the selected service.
        Organization supplier = getSupplierForService(serviceKey);

        // Retrieve the discount from the customer to supplier association.
        OrganizationReference customerSupplierRef = null;
        for (OrganizationReference orgRef : customer
                .getSourcesForType(OrganizationReferenceType.SUPPLIER_TO_CUSTOMER)) {
            if (orgRef.getSource().getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                customerSupplierRef = orgRef;
                break;
            }
        }
        if (customerSupplierRef == null) {
            return null;
        }
        voDiscount = DiscountAssembler.toVODiscount(customerSupplierRef
                .getDiscount());

        return voDiscount;
    }

    public VODiscount getDiscountForCustomer(String customerId)
            throws ObjectNotFoundException {

        VODiscount voDiscount = null;
        // Retrieve the current logged-in user, if present.
        PlatformUser currentUser = dm.getCurrentUserIfPresent();

        // If no user is logged-in (which can happen if the user accesses the
        // public catalog without first logging-in) then return null as a
        // discount.
        if (currentUser == null) {
            return null;
        }
        Organization supplier = currentUser.getOrganization();

        // Retrieve the customer with the given key
        Organization customer = getCustomer(customerId);

        // Retrieve the discount from the customer to supplier association.
        OrganizationReference customerSupplierRef = null;
        for (OrganizationReference orgRef : customer
                .getSourcesForType(OrganizationReferenceType.SUPPLIER_TO_CUSTOMER)) {
            if (orgRef.getSource().getOrganizationId()
                    .equals(supplier.getOrganizationId())) {
                customerSupplierRef = orgRef;
                break;
            }
        }
        if (customerSupplierRef == null) {
            return null;
        }
        voDiscount = DiscountAssembler.toVODiscount(customerSupplierRef
                .getDiscount());

        return voDiscount;
    }

    /**
     * Gets the supplier organization of a service with the given service key
     * 
     * @param serviceKey
     *            the key of the service for which to fetch the supplier
     * @return an Organization representing the supplier of the service
     * @throws ObjectNotFoundException
     *             if the supplier of the given service is not found.
     */
    private Organization getSupplierForService(long serviceKey)
            throws ObjectNotFoundException {
        Product product = dm.getReference(Product.class, serviceKey);
        return product.getVendor();
    }

    /**
     * Gets the customer organization with the given organization key
     * 
     * @param customerId
     *            the id of the customer organization
     * @return an Organization representing the customer
     * @throws ObjectNotFoundException
     *             if the customer is not found.
     */
    private Organization getCustomer(String customerId)
            throws ObjectNotFoundException {
        Organization organization = new Organization();
        organization.setOrganizationId(customerId);

        organization = (Organization) dm
                .getReferenceByBusinessKey(organization);
        return organization;
    }
}
