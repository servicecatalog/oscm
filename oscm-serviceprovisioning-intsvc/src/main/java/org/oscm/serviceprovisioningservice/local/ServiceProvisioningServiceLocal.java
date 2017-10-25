/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ServiceNotPublishedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

@Local
public interface ServiceProvisioningServiceLocal {

    /**
     * Performs the concrete operations to activate a product, after a required
     * confirmation of a notification listener has been retrieved. If none is
     * required, it will be executed synchronously with the call to
     * {@link #activateService(VOService)}.
     * 
     * @param tp
     *            The trigger process containing the information to continue the
     *            activation process.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the technical product cannot be reached.
     * @throws ProductOperationFailed
     *             Thrown in case the current caller is not permitted to invoke
     *             the operation.
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws OrganizationAuthoritiesException
     * @throws ServiceStateException
     * @throws ServiceNotPublishedException
     *             Thrown in case the service is currently not published on any
     *             marketplace.
     * @throws ConcurrentModificationException
     *             if the service has been published to a different marketplace
     *             concurrently
     */
    public void activateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceOperationException,
            TechnicalServiceNotAliveException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceNotPublishedException, ConcurrentModificationException;

    /**
     * Performs the concrete operations to deactivate a product, after a
     * required confirmation of a notification listener has been retrieved. If
     * none is required, it will be executed synchronously with the call to
     * {@link #deactivateService(VOService)}.
     * 
     * @param tp
     *            The trigger process containing the context data.
     * @throws ObjectNotFoundException
     *             Thrown in case the referenced product cannot be found.
     * @throws ServiceStateException
     *             Thrown in case the product state cannot be changed due to its
     *             current state.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the calling organization does not have the
     *             required authority.
     * @throws OperationNotPermittedException
     *             Thrown in case an attempt is made to modify the object of
     *             another organization.
     * @throws ConcurrentModificationException
     *             if the service has been published to a different marketplace
     *             concurrently
     * @throws ProductOperationFailed
     *             Thrown in case the product could not be deactivated.
     */
    public void deactivateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, ConcurrentModificationException;

    /**
     * Creates the provided Product to a VOServiceDetails instance. The
     * non-configurable parameter definitions for the related technical product
     * will not be contained in the result.
     * 
     * @param product
     *            the Product to convert
     * @param facade
     *            the localizer facade to use for internationalization purposes
     * @return the VOServiceDetails
     */
    public VOServiceDetails getServiceDetails(Product product,
            LocalizerFacade facade);

    /**
     * Checks if upgrade paths have been defined for the {@link Product} with
     * the passed key
     * 
     * @param serviceKey
     *            the product key
     * @return <code>true</code> if at lest one service can be upgraded to the
     *         passed one or the passed one can be upgraded to another service.
     *         Otherwise <code>false</code> is returned.
     */
    public boolean isPartOfUpgradePath(long serviceKey);

    /**
     * Creates references from the <b>new</b> {@link Product} to the
     * {@link PaymentType}s that are configured as service default for the
     * passed vendor {@link Organization}.
     * 
     * @param product
     *            the <b>new</b> {@link Product}
     * @param vendor
     *            the product vendor {@link Organization}
     */
    public void copyDefaultPaymentEnablement(Product product,
            Organization vendor);

    /**
     * Verifies whether the maximum number of subscriptions allowed per user for
     * the specified product was reached.
     * 
     * @param product
     *            the product to verify the subscription limit for.
     * @return <code>true</code> if the user is logged in and the maximum number
     *         of subscriptions allowed per user was reached, <code>false</code>
     *         otherwise.
     */
    public boolean isSubscriptionLimitReached(Product product);

    /**
     * Returns the list of customer specific products of the passed seller for
     * the passed customer
     * 
     * @param cust
     *            the customer {@link Organization}
     * @param seller
     *            the calling seller {@link Organization}
     * @return the list of customer specific {@link Product}s
     * @throws OperationNotPermittedException
     *             in case the seller is not a seller for the customer
     */
    public List<Product> getCustomerSpecificProducts(Organization cust,
            Organization seller) throws OperationNotPermittedException;

    /**
     * Returns the list of customer specific products which are based on the
     * passed template product
     * 
     * @param Product
     *            the product template {@link Product}
     * @throws OperationNotPermittedException
     *             in case the caller is not a seller for the customer
     */
    public List<Product> getCustomerSpecificCopyProducts(Product template)
            throws OperationNotPermittedException;

    /**
     * Tries to delete the passed product.
     * 
     * @param supplier
     *            the calling {@link Organization}
     * @param p
     *            the {@link Product} to delete
     * @throws OperationNotPermittedException
     *             i case the passes supplier is not the owner
     * @throws ServiceOperationException
     *             in case the {@link Product} is related to a
     *             {@link Subscription} or resale permissions exist.
     * @throws ServiceStateException
     *             in case the product is in a state where it cannot be deleted
     *             or non deleteable customer specific copies exist
     */
    public void deleteProduct(Organization supplier, Product p)
            throws OperationNotPermittedException, ServiceOperationException,
            ServiceStateException;

}
