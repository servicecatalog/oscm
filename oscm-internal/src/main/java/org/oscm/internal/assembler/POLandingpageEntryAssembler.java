/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.landingpage.POLandingpageEntry;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;

/**
 * Assembler that merges service and subscription into PO object
 * 
 * @author zankov
 * 
 */
public class POLandingpageEntryAssembler extends BasePOAssembler {

    public static List<POLandingpageEntry> toPOLandingpageEntries(
            List<Product> produts, List<Subscription> subscriptions,
            LocalizerFacade facade) {
        prefetchForPoducts(produts, facade);
        prefetchForSubscription(subscriptions, facade);

        return assembleLandingpageEntries(produts, subscriptions, facade);
    }

    static List<POLandingpageEntry> assembleLandingpageEntries(
            List<Product> products, List<Subscription> subscriptions,
            LocalizerFacade facade) {
        ArrayList<POLandingpageEntry> result = new ArrayList<POLandingpageEntry>();

        addNewEntryForEachProduct(products, facade, result);

        updateSubscriptionData(subscriptions, result);

        return result;
    }

    static void updateSubscriptionData(List<Subscription> subscriptions,
            ArrayList<POLandingpageEntry> result) {
        for (Subscription subscription : subscriptions) {
            POLandingpageEntry entry = findEntry(result,
                    getServiceId(subscription));

            if (entry != null) {
                if (!entry.isSubscribed()) {
                    // Update data for the first subscription
                    updateEntryForSubscription(subscription, entry);
                } else {
                    // if more than one subscription than tha base URL is
                    // removed
                    entry.setServiceAccessURL(null);
                }
            }
        }
    }

    static void addNewEntryForEachProduct(List<Product> products,
            LocalizerFacade facade, ArrayList<POLandingpageEntry> result) {
        for (Product product : products) {
            POLandingpageEntry entry = new POLandingpageEntry();
            fillProductFields(product, entry, facade);
            result.add(entry);
        }
    }

    private static POLandingpageEntry findEntry(
            ArrayList<POLandingpageEntry> result, String serviceId) {
        for (POLandingpageEntry entry : result) {
            if (entry.getServiceId().equals(serviceId)) {
                return entry;
            }
        }

        return null;
    }

    private static String getServiceId(Subscription subscription) {
        Product product = subscription.getProduct();
        if (product.getTemplate() == null) {
            return product.getProductId();
        } else {
            return ProductAssembler.getProductId(product);
        }
    }

    protected static void fillProductFields(Product product,
            POLandingpageEntry entry, LocalizerFacade facade) {
        entry.setServiceId(ProductAssembler.getProductId(product));
        entry.setServiceStatus(product.getStatus());
        entry.setServiceKey(product.getKey());
        entry.setServiceVersion(product.getVersion());

        long key = getKeyForLocalizedResource(product);
        String name = ProductAssembler.getServiceName(product, facade);
        entry.setName(name);

        String shortDescription = facade.getText(key,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        entry.setShortDescription(shortDescription);

        final Organization supplier = getSupplier(product);
        if (supplier != null) {
            entry.setSellerName(supplier.getName() != null
                    && supplier.getName().trim().length() > 0 ? supplier
                    .getName() : supplier.getOrganizationId());
        }
    }

    protected static void updateEntryForSubscription(Subscription subscription,
            POLandingpageEntry entry) {
        entry.setSubscriptionId(subscription.getSubscriptionId());
        entry.setSubscriptionStatus(subscription.getStatus());
        entry.setSubscriptionKey(subscription.getKey());
        entry.setSubscriptionVersion(subscription.getVersion());

        Product product = subscription.getProduct();
        TechnicalProduct techProd = product.getTechnicalProduct();

        entry.setServiceAccessType(techProd.getAccessType());

        setServiceAccessUrl(subscription, entry, techProd);

        entry.setSubscribed(true);
    }

    static void setServiceAccessUrl(Subscription subscription,
            POLandingpageEntry entry, TechnicalProduct techProd) {
        if (subscription.getBaseURL() == null) {
            entry.setServiceAccessURL(techProd.getBaseURL());
        } else {
            entry.setServiceAccessURL(subscription.getBaseURL());
        }
    }

    private static Organization getSupplier(final Product product) {
        final Organization supplier;
        if (ServiceType.isSubscription(product.getType())) {
            supplier = product.getTemplate().getVendor();
        } else {
            supplier = product.getVendor();
        }
        return supplier;
    }

    private static long getKeyForLocalizedResource(Product product) {
        long key = product.getKey();
        Organization supplier = getSupplier(product);
        if (supplier != null) {
            Set<OrganizationRoleType> roles = supplier.getGrantedRoleTypes();
            if (roles.contains(OrganizationRoleType.BROKER)
                    || roles.contains(OrganizationRoleType.RESELLER)) {
                key = product.getTemplate().getKey();
            }
        }
        return key;
    }

    private static void prefetchForPoducts(List<Product> products,
            LocalizerFacade facade) {
        List<Long> objectKeys = new ArrayList<Long>();
        for (Product product : products) {
            objectKeys.add(Long.valueOf(product.getTemplateOrSelf().getKey()));
        }
        facade.prefetch(
                objectKeys,
                Arrays.asList(new LocalizedObjectTypes[] {
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION }));
    }

    private static void prefetchForSubscription(
            List<Subscription> subscriptions, LocalizerFacade facade) {
        List<Long> objectKeys = new ArrayList<Long>();
        for (Subscription sub : subscriptions) {
            objectKeys.add(Long.valueOf(sub.getKey()));
            TechnicalProduct techProd = sub.getProduct().getTechnicalProduct();
            objectKeys.add(Long.valueOf(techProd.getKey()));
            for (TechnicalProductOperation opperation : techProd
                    .getTechnicalProductOperations()) {
                objectKeys.add(Long.valueOf(opperation.getKey()));
            }
        }
        facade.prefetch(objectKeys, Arrays.asList(new LocalizedObjectTypes[] {
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME,
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION,
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS,
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC }));
    }

}
