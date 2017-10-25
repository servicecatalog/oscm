/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.local;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

@Local
public interface ServiceProvisioningPartnerServiceLocal {

    /**
     * Retrieves the partner revenue shares of a product. They are fetched from
     * the catalog entry of the product. If not found in the catalog entry,
     * these are retrieved from the partner revenue shares stored in the
     * marketplace.
     * 
     * @param serviceKey
     *            the key of the product to retrieve the partner revenue shares
     * @param isStatusCheckNeeded
     *            if the status of product should be checked
     * @return
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws OperationNotPermittedException
     *             if the caller organization has the role SUPPLIER,BROKER,
     *             RESELLER and is not the owner of the given product
     * @throws ServiceOperationException
     *             if the service is a subscription-specific copy, or if the
     *             service is a template which is not assigned to any
     *             marketplace and the catalog entry has no price models
     * @throws ServiceStateException
     *             if the service has the state of DELETED, SUSPENDED, OBSOLETE
     */
    public Map<RevenueShareModelType, RevenueShareModel> getRevenueShareModelsForProduct(
            long serviceKey, boolean isStatusCheckNeeded)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException;

    /**
     * Retrieves the operator revenue share for a product. It is fetched from
     * the catalog entry of the product.
     * 
     * @param productKey
     *            the key of the product to retrieve the operator revenue share
     *            for
     * @return the operator revenue share
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws OperationNotPermittedException
     *             if the caller organization has no PLATFORM_OPERATOR, no
     *             MARKETPLACE_OWNER role and is not the owner of the given
     *             product.
     */
    public RevenueShareModel getOperatorRevenueShare(long productKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the default operator revenue share for a product. It is fetched
     * from the vendor organization of the product.
     * 
     * @param productKey
     *            the key of the product to retrieve the default operator
     *            revenue share for
     * @return the default operator revenue share
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws OperationNotPermittedException
     *             if the caller organization has no PLATFORM_OPERATOR, no
     *             MARKETPLACE_OWNER role and is not the owner of the given
     *             product.
     */
    public RevenueShareModel getDefaultOperatorRevenueShare(long productKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Saves the operator revenue share of a product.
     * 
     * @param productKey
     *            the key of the product to save the operator revenue for.
     * @param newRevenueShare
     *            a transient RevenueShareModel domain object instance
     *            containing the operator revenue share to be saved.
     * @param newRevenueShareVersion
     *            the version of the operator revenue share at the loading time,
     *            needed for concurrency validation.
     * @throws ValidationException
     *             if the specified revenue share is not a valid percent value.
     * @throws ConcurrentModificationException
     *             if the same revenue share was modified by another user
     *             between the loading and saving time.
     * @throws ObjectNotFoundException
     *             if no product could be found with the specified key.
     * @throws ServiceOperationException
     *             if the specified product does is not a TEMPLATE product.
     */
    public void saveOperatorRevenueShare(long productKey,
            RevenueShareModel newRevenueShare, int newRevenueShareVersion)
            throws ValidationException, ConcurrentModificationException,
            ObjectNotFoundException, ServiceOperationException;

    /**
     * Saves the partner revenue shares of a product.
     * 
     * @param serviceKey
     *            the key of the product to retrieve the partner revenue shares
     * @return
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws ServiceOperationException
     */
    public Map<RevenueShareModelType, RevenueShareModel> saveRevenueShareModelsForProduct(
            long serviceKey, RevenueShareModel brokerRevenueShareNew,
            RevenueShareModel resellerRevenueShareNew,
            int brokerRevenueShareNewVersion, int resellerRevenueShareNewVersion)
            throws ObjectNotFoundException, ServiceOperationException,
            NonUniqueBusinessKeyException, ValidationException,
            ConcurrentModificationException;

    /**
     * Retrieves the catalog entry domain object of the Product for the
     * specified product key.
     * 
     * @param serviceKey
     *            the key of the service to be retrieved
     * @return the catalog entry domain object, or return null when the product
     *         has no catalog entry
     * @throws ObjectNotFoundException
     *             if the product is not found by its key
     * @throws ServiceOperationException
     *             if the service is not a template or a partner template
     */
    public CatalogEntry getCatalogEntryForProduct(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException;

    /**
     * Retrieves all products offered by supplier with ACTIVE or INACTIVE
     * status.
     * 
     * @return all products offered by supplier
     */
    public List<Product> getTemplateProducts();

    /**
     * Retrieves all products offered by a partner (broker or reseller) with
     * ACTIVE, INACTIVE or SUSPENDED status.
     * 
     * @return all products offered by the partner
     */
    public List<Product> getProductsForVendor();

    /**
     * Grants a resale permission. The product template is copied and a new
     * catalog entry is created, which is linked to the resale product copy. The
     * corresponding partner price model is copied from the template's catalog
     * entry or from the marketplace and this copy is linked to the new catalog
     * entry.
     * 
     * @param templateId
     *            the ID of the product template to copy
     * @param grantorId
     *            the ID of the organization, that grants the resale permission
     * @param granteeId
     *            the ID of the organization, that receives the resale
     *            permission
     * @param resaleType
     *            the resale permission type (needed because the same
     *            organization maybe broker and reseller in the future)
     * @return the created product copy
     * @throws ValidationException
     *             if the given resaleType is invalid
     * @throws ObjectNotFoundException
     *             if the product template or one of the organizations is not
     *             found
     * @throws OperationNotPermittedException
     *             if the caller organization is not a SUPPLIER or if the
     *             product template doesn't belong to the grantor
     * @throws NonUniqueBusinessKeyException
     *             if a product copy or a new catalog entry cannot be created,
     *             because an entity with the same key already exists in the
     *             database
     * @throws ServiceOperationException
     *             if the product template is no template or if the product
     *             template has no valid price model defined for itself or if
     *             the product template is not assigned to a marketplace
     * @throws ConcurrentModificationException
     *             if the same product is copied for the same vendor by another
     *             user in the time between reading it and creating the copy
     * @throws OrganizationAuthorityException
     *             if the grantee organization has neither the BROKER nor the
     *             RESELLER role
     * @throws ServiceStateException
     *             if the product template is not in status INACTIVE or ACTIVE
     */
    public Product grantResalePermission(String templateId, String grantorId,
            String granteeId, OfferingType resaleType)
            throws ValidationException, ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException,
            ServiceOperationException, ConcurrentModificationException,
            OrganizationAuthorityException, ServiceStateException;

    /**
     * Revokes a resale permission. The status of the resale product copy is set
     * to deleted.
     * 
     * @param templateId
     *            the ID of the corresponding product template
     * @param grantorId
     *            the ID of the organization, that has granted the resale
     *            permission
     * @param granteeId
     *            the ID of the organization, that received the resale
     *            permission
     * @return the resale product copy, which was set to deleted
     * @throws ObjectNotFoundException
     *             if the product template or one of the organizations is not
     *             found
     * @throws ServiceOperationException
     *             if the product template is no template
     * @throws OrganizationAuthorityException
     *             if the grantee organization has neither the BROKER nor the
     *             RESELLER role
     * @throws OperationNotPermittedException
     *             if the caller organization is not a SUPPLIER
     */
    public Product revokeResalePermission(String templateId, String grantorId,
            String granteeId) throws ObjectNotFoundException,
            ServiceOperationException, OrganizationAuthorityException,
            OperationNotPermittedException;

    /**
     * Retrieves all copied products for resale with ACTIVE or INACTIVE status
     * which has the specified service as a template.
     * 
     * @param serviceKey
     *            the key of the template product
     * @return the copied products for resale
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws ServiceOperationException
     *             if the service is not template
     */
    public List<Product> getPartnerProductsForTemplate(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException;

    /**
     * @return list of template products for the calling users organization
     */
    public List<Product> loadSuppliedTemplateServices();
}
