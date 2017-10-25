/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-02-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VODiscount;

/**
 * Remote interface of the discount service.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface DiscountService {

    /**
     * Retrieves the discount that the supplier of the given service has granted
     * to the organization of the calling user.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service for which to retrieve the
     *            discount
     * @return the discount, or <code>null</code> if the calling user is not
     *         logged in, if the user's organization cannot be associated with
     *         the supplier of the service, or if no discount has been granted
     *         to the user's organization
     * @throws ObjectNotFoundException
     *             if the supplier of the service is not found
     */
    @WebMethod
    public VODiscount getDiscountForService(
            @WebParam(name = "serviceKey") long serviceKey)
            throws ObjectNotFoundException;

    /**
     * Retrieves the discount that has been granted to the specified customer by
     * the supplier organization the calling user is a member of.
     * <p>
     * Required role: any user role in a supplier organization
     * 
     * @param customerId
     *            the ID of the customer organization for which to retrieve the
     *            discount
     * @return the discount, or <code>null</code> if the calling user's supplier
     *         organization cannot be associated with the given customer or if
     *         no discount has been granted to the customer
     * @throws ObjectNotFoundException
     *             if the customer organization is not found
     */
    @WebMethod
    public VODiscount getDiscountForCustomer(
            @WebParam(name = "customerId") String customerId)
            throws ObjectNotFoundException;
}
