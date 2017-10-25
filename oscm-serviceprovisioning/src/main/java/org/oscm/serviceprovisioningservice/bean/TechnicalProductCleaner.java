/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.interceptor.DateFactory;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;

/**
 * POJO class to delete all references (Product, Parameter, Event, Role,
 * Subscription ...) from a technical product.
 * 
 */
public class TechnicalProductCleaner {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TechnicalProductCleaner.class);

    private final DataService dm;

    private final TenantProvisioningServiceBean tenantProvisioning;

    /**
     * Constructor
     * 
     * @param dm
     *            the data manager bean
     * @param tenantProvisioning
     *            the local tenant provisioning service
     */
    public TechnicalProductCleaner(DataService dm,
            TenantProvisioningServiceBean tenantProvisioning) {
        this.dm = dm;
        this.tenantProvisioning = tenantProvisioning;
    }

    /**
     * Delete all references (Product, Parameter, Event, Role, Subscription ...)
     * from a technical product.
     * 
     * @param technicalProduct
     *            the technical product to be processed.
     */
    public void cleanupTechnicalProduct(TechnicalProduct technicalProduct)
            throws DeletionConstraintException {

        final List<Product> templateProducts = technicalProduct.getProducts();
        final List<Product> specificTemplateProducts = moveProductsOfType(
                templateProducts, ServiceType.CUSTOMER_TEMPLATE,
                ServiceType.PARTNER_TEMPLATE);
        final List<Product> subscriptionProducts = moveProductsOfType(
                templateProducts, ServiceType.SUBSCRIPTION,
                ServiceType.PARTNER_SUBSCRIPTION,
                ServiceType.CUSTOMER_SUBSCRIPTION);
        deleteProducts(technicalProduct, subscriptionProducts);
        for (Product specificTemplateProduct : specificTemplateProducts) {
            deleteFromLandingPage(specificTemplateProduct);
        }
        deleteProducts(technicalProduct, specificTemplateProducts);
        deleteProducts(technicalProduct, templateProducts);
    }

    /**
     * clean up the landingpageProduct of Product
     */
    void deleteFromLandingPage(Product product) {
            dm.createNamedQuery(
                    "LandingpageProduct.deleteLandingpageProductForProduct")
                    .setParameter("productKey", Long.valueOf(product.getKey()))
                    .executeUpdate();
    }

    /**
     * Moves products of a certain type from the given list to a separate list
     * 
     * @param products
     *            may contain products of different types
     * @param types
     *            the types of products which should only be included in the
     *            result list
     * @return list containing products of the desired type
     */
    List<Product> moveProductsOfType(final List<Product> products,
            ServiceType... types) {

        if (types == null) {
            return new ArrayList<Product>();
        }

        final List<Product> result = new ArrayList<Product>();
        HashSet<ServiceType> srvTypes = new HashSet<ServiceType>(
                Arrays.asList(types));
        for (Product prd : products) {
            if (srvTypes.contains(prd.getType())) {
                result.add(prd);
            }
        }
        products.removeAll(result);
        return result;
    }

    private void deleteProducts(TechnicalProduct technicalProduct,
            final List<Product> productArray)
            throws DeletionConstraintException {

        if ((productArray != null) && (productArray.size() > 0)) {
            for (Product product : productArray) {
                if (product != null) {
                    if (!product.isDeleted()) {
                        // there is a product which can not be deleted
                        // stop deleting process for technical product
                        DeletionConstraintException e = new DeletionConstraintException(
                                DomainObjectException.ClassEnum.TECHNICAL_SERVICE,
                                technicalProduct.getTechnicalProductId(),
                                DomainObjectException.ClassEnum.SERVICE);
                        logger.logError(
                                Log4jLogger.SYSTEM_LOG,
                                e,
                                LogMessageIdentifier.ERROR_DELETE_TECHNICAL_PRODUCT_FAILED,
                                technicalProduct.getTechnicalProductId());
                        throw e;
                    }

                    final Subscription subscription = product
                            .getOwningSubscription();
                    if (subscription != null) {
                        if (!subscription.isDeletable()) {
                            // there is a subscription which can not be deleted
                            // stop deleting process
                            DeletionConstraintException e = new DeletionConstraintException(
                                    DomainObjectException.ClassEnum.TECHNICAL_SERVICE,
                                    technicalProduct.getTechnicalProductId(),
                                    DomainObjectException.ClassEnum.SUBSCRIPTION);
                            logger.logError(
                                    Log4jLogger.SYSTEM_LOG,
                                    e,
                                    LogMessageIdentifier.ERROR_DELETE_TECHNICAL_PRODUCT_FAILED,
                                    technicalProduct.getTechnicalProductId());
                            throw e;
                        }
                        try {
                            tenantProvisioning
                                    .deleteProductInstance(subscription);
                        } catch (TechnicalServiceOperationException
                                | TechnicalServiceNotAliveException e) {
                            logger.logWarn(
                                    Log4jLogger.SYSTEM_LOG,
                                    e,
                                    LogMessageIdentifier.WARN_TECH_SERV_DELETE_INSTANCE_FAILED,
                                    subscription.getProductInstanceId(),
                                    subscription.getOrganization()
                                            .getOrganizationId(),
                                    technicalProduct.getTechnicalProductId(), e
                                            .getMessage());
                        }
                        deleteSubscriptionAndUdas(subscription);
                    }
                    // delete product, all other referenced record from tables
                    // will be deleted by JPA
                    dm.remove(product);
                }
            }
        }

    }

    /**
     * Deletes the subscription and all udas saved for it.
     * 
     * @param subscription
     *            the {@link Subscription} to remove
     */
    private void deleteSubscriptionAndUdas(Subscription subscription) {
        Long historyModificationTime = Long.valueOf(DateFactory.getInstance()
                .getTransactionTime());
        subscription.setHistoryModificationTime(historyModificationTime);

        // read udas
        Query query = dm.createNamedQuery("Uda.getByTargetTypeAndKey");
        query.setParameter("targetKey", Long.valueOf(subscription.getKey()));
        query.setParameter("targetType", UdaTargetType.CUSTOMER_SUBSCRIPTION);

        for (Uda uda : ParameterizedTypes.iterable(query.getResultList(),
                Uda.class)) {
            uda.setHistoryModificationTime(historyModificationTime);
            dm.remove(uda);
        }

        // delete subscription, all other referenced record from
        // tables will be deleted by JPA
        dm.remove(subscription);
    }

}
