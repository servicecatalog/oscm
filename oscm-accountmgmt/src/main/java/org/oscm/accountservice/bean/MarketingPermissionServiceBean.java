/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 02.12.2011                                                      
 *                                                                              
 *  Completion Time: 05.12.2011                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.accountservice.dao.TechnicalProductDao;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.MarketingPermission;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.permission.PermissionCheck;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.MarketingPermissionNotFoundException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * Bean for marketing permission related operations.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@Local(MarketingPermissionServiceLocal.class)
public class MarketingPermissionServiceBean implements
        MarketingPermissionServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketingPermissionServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService ds;

    @EJB(beanInterface = TechnicalProductDao.class)
    TechnicalProductDao technicalProductDao;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeMarketingPermission(long technicalServiceKey,
            List<String> organizationIds) throws ObjectNotFoundException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException {
        Organization technologyProvider = ds.getCurrentUser().getOrganization();
        TechnicalProduct tpRef = ds.getReference(TechnicalProduct.class,
                technicalServiceKey);
        PermissionCheck.owns(tpRef, technologyProvider, logger, null);

        Map<String, MarketingPermission> permissionForOrgId = getPermissionKeysAndSuppliers(
                organizationIds, tpRef);
        StringBuffer orgIdsThatFailed = removeMarketingPermissionsAndObsoleteOrgRefs(
                organizationIds, tpRef, permissionForOrgId);

        if (orgIdsThatFailed.length() > 0) {
            String orgIdString = orgIdsThatFailed.toString();
            MarketingPermissionNotFoundException mpnfe = new MarketingPermissionNotFoundException(
                    String.format(
                            "MarketingPermission for technical service '%s' and supplier with identifer '%s' does not exist.",
                            tpRef, orgIdString), new Object[] { orgIdString });
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_FOUND,
                    String.valueOf(tpRef.getKey()), orgIdString);
            throw mpnfe;
        }

    }

    /**
     * Removes the organization references if there is no related marketing
     * permission.
     * 
     * @param organizationIds
     *            The organization identifiers to be considered.
     * @param tpRef
     *            The reference to the technical service.
     * @param permissionForOrgId
     *            The marketing permissions in a map with the organization
     *            identifier as key.
     * @return The ids for those organizations where the processing failed.
     */
    private StringBuffer removeMarketingPermissionsAndObsoleteOrgRefs(
            List<String> organizationIds, TechnicalProduct tpRef,
            Map<String, MarketingPermission> permissionForOrgId) {
        Set<Long> affectedOrgRefs = new HashSet<Long>();
        StringBuffer orgIdsThatFailed = new StringBuffer();
        for (String orgId : organizationIds) {
            MarketingPermission permission = permissionForOrgId.remove(orgId);
            removeMarketingPermission(tpRef, permission, affectedOrgRefs,
                    orgId, orgIdsThatFailed);
        }

        // remove the obsolete organization references
        removeObsoleteOrgRefs(affectedOrgRefs);

        return orgIdsThatFailed;
    }

    private void removeObsoleteOrgRefs(Set<Long> affectedOrgRefs) {
        if (!affectedOrgRefs.isEmpty()) {
            Query query = ds
                    .createNamedQuery("OrganizationReference.getObsolete");
            query.setParameter("refKeys", affectedOrgRefs);
            List<OrganizationReference> obsoleteReferences = ParameterizedTypes
                    .list(query.getResultList(), OrganizationReference.class);
            for (OrganizationReference orgRef : obsoleteReferences) {
                ds.remove(orgRef);
            }
        }
    }

    /**
     * Removes the marketing permission if existing, or appends the organization
     * identifier to the provided string buffer in case the operation failed.
     * 
     * @param tpRef
     *            The reference to the technical service.
     * @param permission
     *            The permission to be removed, if existing.
     * @param affectedOrgRefs
     *            The collection of all so far affected organization references
     * @param orgId
     *            The id of the organization to remove the permission for.
     * @param orgIdsThatFailed
     *            The string buffer containing the ids of the organization for
     *            which the processing failed.
     */
    private void removeMarketingPermission(TechnicalProduct tpRef,
            MarketingPermission permission, Set<Long> affectedOrgRefs,
            String orgId, StringBuffer orgIdsThatFailed) {
        if (permission == null) {
            appendIdToString(orgId, orgIdsThatFailed);
        } else {
            affectedOrgRefs.add(Long.valueOf(permission
                    .getOrganizationReference().getKey()));
            updateStateOfMarketingProducts(tpRef, permission
                    .getOrganizationReference().getTarget(),
                    ServiceStatus.OBSOLETE);
            ds.remove(permission);
        }
    }

    /**
     * Appends the specified id to the string buffer.
     * 
     * @param orgId
     *            The id to append.
     * @param sb
     *            The string buffer.
     */
    private void appendIdToString(String orgId, StringBuffer sb) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(orgId);
    }

    /**
     * Reads the required marketing permissions and the suppliers from the
     * database.
     * 
     * @param organizationIds
     *            The identifiers of the organization to read the permissions
     *            for.
     * @param tpRef
     *            The technical product the permissions have to belong to.
     * @return A map containing the organization identifier as key and the
     *         marketing permission as value.
     */
    private Map<String, MarketingPermission> getPermissionKeysAndSuppliers(
            List<String> organizationIds, TechnicalProduct tpRef) {
        List<String> searchList = new ArrayList<String>(organizationIds);
        searchList.add("");
        Query query = ds
                .createNamedQuery("MarketingPermission.findForSupplierIds");
        query.setParameter("tp", tpRef);
        query.setParameter("orgIds", searchList);
        query.setParameter("refType",
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        List<Object[]> result = ParameterizedTypes.list(query.getResultList(),
                Object[].class);
        Map<String, MarketingPermission> permissionForOrgId = new HashMap<String, MarketingPermission>();
        for (Object[] entry : result) {
            MarketingPermission permission = (MarketingPermission) entry[0];
            Organization supplier = (Organization) entry[1];
            permissionForOrgId.put(supplier.getOrganizationId(), permission);
        }
        return permissionForOrgId;
    }

    /**
     * Checks if products of the supplier are based on technical products of the
     * provider and sets the products state to the given one.
     * 
     * @param provider
     *            the provider organization of the technical products
     * @param supplier
     *            the supplier organization potentially owning products based on
     *            the providers technical products
     * @param status
     *            the status to set to matching products of the supplier
     */
    private void updateStateOfMarketingProducts(
            TechnicalProduct technicalProduct, Organization supplier,
            ServiceStatus status) {
        List<Product> products = loadNonDeletedProducts(supplier);
        for (Product product : products) {
            if (isProductToBeUpdated(technicalProduct, product)) {
                product.setStatus(status);
            }
        }
    }

    /**
     * Loads all non-deleted products of a supplier. In ST-Test some
     * organizations create lots of products that are deleted afterwards. In BES
     * deleted products are not removed from the database. They are only
     * flagged. This will slow down the test execution when the list of deleted
     * products gets longer. A long list of deleted products is not very
     * realistic in production. However, I added this optimization to simplify
     * ST-Testing.
     * 
     * @param supplier
     *            Supplier that created the products
     * @return List of products
     */
    private List<Product> loadNonDeletedProducts(Organization supplier) {

        Query query = null;
        query = ds.createNamedQuery("Product.getProductsForVendor");
        query.setParameter("vendorKey", Long.valueOf(supplier.getKey()));
        query.setParameter("filterOutWithStatus",
                EnumSet.of(ServiceStatus.DELETED));
        @SuppressWarnings("unchecked")
        List<Product> result = query.getResultList();

        return result;
    }

    /**
     * Checks if the given product is based on the technical product contained
     * in the given list. Not affected are deleted products and products of
     * subscriptions.
     * 
     * @param technicalProducts
     *            the list of technical products
     * @param product
     *            the product to check
     * @return <code>true</code> in case the products technical product is
     *         contained in the list otherwise <code>false</code>
     */
    private boolean isProductToBeUpdated(TechnicalProduct technicalProduct,
            Product product) {
        if (technicalProduct.getKey() == product.getTechnicalProduct().getKey()
                && product.getStatus() != ServiceStatus.DELETED
                && product.getOwningSubscription() == null) {
            return true;
        }
        return false;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void addMarketingPermission(Organization provider,
            long technicalServiceKey, List<String> organizationIds)
            throws ObjectNotFoundException, AddMarketingPermissionException {

        // list holding invalid ids
        StringBuffer errorInvalidIds = new StringBuffer();

        // load the technical product
        TechnicalProduct technicalProduct = ds.getReference(
                TechnicalProduct.class, technicalServiceKey);

        // grant permission to the suppliers
        Organization supplier = null;
        for (String organizationId : organizationIds) {
            // get supplier organization and verify organization role
            supplier = new Organization();
            supplier.setOrganizationId(organizationId.trim());
            try {
                supplier = (Organization) ds
                        .getReferenceByBusinessKey(supplier);
            } catch (ObjectNotFoundException e) {
                appendIdToString(organizationId, errorInvalidIds);
                continue;
            }
            if (!supplier.hasRole(OrganizationRoleType.SUPPLIER)) {
                appendIdToString(organizationId, errorInvalidIds);
                continue;
            }

            OrganizationReference orgReference = createOrganizationReference(
                    provider, supplier);
            boolean newMarketingPermission = createMarketingPermission(
                    technicalProduct, orgReference);

            if (newMarketingPermission) {
                updateStateOfMarketingProducts(technicalProduct, supplier,
                        ServiceStatus.INACTIVE);
            }
        }

        // if errors occurred throw an exception
        if (errorInvalidIds.length() > 0) {
            String idString = errorInvalidIds.toString();
            String msg = String
                    .format("MarketingPermission for technical service '%s' and supplier ids '%s' could not be added.",
                            String.valueOf(technicalServiceKey), idString);
            AddMarketingPermissionException mpe = new AddMarketingPermissionException(
                    msg, new Object[] { idString });
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                    String.valueOf(technicalServiceKey), idString);
            throw mpe;
        }
    }

    private boolean createMarketingPermission(
            TechnicalProduct technicalProduct,
            OrganizationReference orgReference) {

        MarketingPermission permission = new MarketingPermission();
        permission.setOrganizationReference(orgReference);
        permission.setTechnicalProduct(technicalProduct);
        try {
            ds.persist(permission);
            return true;
        } catch (NonUniqueBusinessKeyException e) {
            // ignore because user wanted to save anyway
            return false;
        }
    }

    /**
     * Creates an organization reference if it not already exists.
     */
    private OrganizationReference createOrganizationReference(
            Organization provider, Organization supplier) {
        OrganizationReference reference = new OrganizationReference(provider,
                supplier,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        OrganizationReference orgReference = (OrganizationReference) ds
                .find(reference);
        if (orgReference == null) {
            orgReference = reference;
            try {
                ds.persist(orgReference);
            } catch (NonUniqueBusinessKeyException e) {
                // ignore because reference should be created anyway
                orgReference = (OrganizationReference) ds.find(reference);
            }
        }
        return orgReference;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Organization> getSuppliersForTechnicalService(
            long technicalServiceKey) throws ObjectNotFoundException,
            OperationNotPermittedException {

        TechnicalProduct tpRef = ds.getReference(TechnicalProduct.class,
                technicalServiceKey);
        PermissionCheck.owns(tpRef, ds.getCurrentUser().getOrganization(),
                logger, null);
        Query query = ds
                .createNamedQuery("MarketingPermission.getOrgsForUsingTechnicalService");
        query.setParameter("tpKey", Long.valueOf(technicalServiceKey));
        query.setParameter("refType",
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        List<Organization> result = ParameterizedTypes.list(
                query.getResultList(), Organization.class);

        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<TechnicalProduct> getTechnicalServicesForSupplier(
            Organization supplier) {

        return technicalProductDao.retrieveTechnicalProduct(supplier);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeMarketingPermissions(TechnicalProduct technicalProduct) {
        Query query = ds
                .createNamedQuery("MarketingPermission.findForTechnicalService");
        query.setParameter("tp", technicalProduct);
        List<MarketingPermission> result = ParameterizedTypes.list(
                query.getResultList(), MarketingPermission.class);

        // remove permissions
        Set<Long> affectedReferences = new HashSet<Long>();
        for (MarketingPermission mp : result) {
            affectedReferences.add(Long.valueOf(mp
                    .getOrganizationReferenceKey()));
            ds.remove(mp);
        }

        // remove organization reference
        removeObsoleteOrgRefs(affectedReferences);
    }
}
