/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.data;

import static org.oscm.test.Numbers.TIMESTAMP;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.id.IdGenerator;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOSubscription;

public class Subscriptions {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static int subscriptionId;

    public static VOSubscription createVOSubscription(String id) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(id);
        subscription.setPurchaseOrderNumber(id);
        return subscription;
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, Organization supplier, int cutOffDay)
            throws NonUniqueBusinessKeyException {

        return createSubscription(mgr, customerId, productId, subscriptionId,
                marketplaceId, TIMESTAMP, TIMESTAMP, supplier, cutOffDay);

    }

    /**
     * Creates a new subscription for the given product and customer.
     */
    public static Subscription createSubscription(DataService mgr,
            String customerId, Product product)
            throws NonUniqueBusinessKeyException {
        Organization supplier = product.getVendor();
        return createSubscription(mgr, customerId, product.getProductId(),
                "mySub" + subscriptionId++, supplier);
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, Product product, Marketplace mp, int cutOffDay)
            throws NonUniqueBusinessKeyException {
        Organization supplier = product.getVendor();
        return createSubscription(mgr, customerId, product.getProductId(),
                "mySub" + subscriptionId++, mp.getMarketplaceId(), TIMESTAMP,
                TIMESTAMP, supplier, cutOffDay);
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            Organization supplier) throws NonUniqueBusinessKeyException {

        return createSubscription(mgr, customerId, productId, subscriptionId,
                TIMESTAMP, TIMESTAMP, supplier, 1);

    }

    public static Subscription createPartnerSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            Organization supplier) throws NonUniqueBusinessKeyException {

        return createPartnerSubscription(mgr, customerId, productId,
                subscriptionId, TIMESTAMP, TIMESTAMP, supplier);

    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            Organization supplier, PaymentInfo paymentInfo, int cutOffDay)
            throws NonUniqueBusinessKeyException {

        return createSubscription(mgr, customerId, productId, subscriptionId,
                null, TIMESTAMP, TIMESTAMP, supplier, paymentInfo, cutOffDay);

    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            long creationDate, long activationDate, Organization supplier,
            int cutOffDay) throws NonUniqueBusinessKeyException {
        return createSubscription(mgr, customerId, productId, subscriptionId,
                null, creationDate, activationDate, supplier, cutOffDay);
    }

    public static Subscription createPartnerSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            long creationDate, long activationDate, Organization supplier)
            throws NonUniqueBusinessKeyException {
        return createPartnerSubscription(mgr, customerId, productId,
                subscriptionId, null, creationDate, activationDate, supplier);
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            Organization supplier, int cutOffDay)
            throws NonUniqueBusinessKeyException {
        return createSubscription(mgr, customerId, productId, subscriptionId,
                marketplaceId, creationDate, activationDate, supplier, null,
                cutOffDay);
    }

    public static Subscription createPartnerSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            Organization supplier) throws NonUniqueBusinessKeyException {
        return createPartnerSubscription(mgr, customerId, productId,
                subscriptionId, marketplaceId, creationDate, activationDate,
                supplier, null);
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            Organization supplier, PaymentInfo paymentInfo, int cutOffDay)
            throws NonUniqueBusinessKeyException {
        try {
            Product template = Products.findProduct(mgr,
                    mgr.getReference(Organization.class, supplier.getKey()),
                    productId);
            Product product = template.copyForSubscription(
                    template.getTargetCustomer(), new Subscription());
            product.setOwningSubscription(null);
            product.setType(ServiceType.SUBSCRIPTION);
            setProductType(template, product);
            setHistoryCreationTime(creationDate, product);
            mgr.persist(product);
            mgr.flush();
            return createSubscription(mgr, customerId, product, subscriptionId,
                    marketplaceId, creationDate, activationDate, paymentInfo,
                    cutOffDay);
        } catch (ObjectNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Subscription createSubscriptionWithOwner(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            Organization supplier, PaymentInfo paymentInfo, int cutOffDay,
            PlatformUser owner) throws NonUniqueBusinessKeyException {
        try {
            Product template = Products.findProduct(mgr,
                    mgr.getReference(Organization.class, supplier.getKey()),
                    productId);
            Product product = template.copyForSubscription(
                    template.getTargetCustomer(), new Subscription());
            product.setOwningSubscription(null);
            product.setType(ServiceType.SUBSCRIPTION);
            setProductType(template, product);
            setHistoryCreationTime(creationDate, product);
            mgr.persist(product);
            mgr.flush();

            assertNotNull("No customerId given for the subscription",
                    customerId);
            Organization customer = Organizations.findOrganization(mgr,
                    customerId);

            BillingContact bc = PaymentInfos.createBillingContact(mgr,
                    customer, "bc_id" + IdGenerator.generateRandomUUID());

            assertNotNull("No customerId given for the subscription", customer);

            Subscription newSub = new Subscription();
            newSub.setCreationDate(Long.valueOf(creationDate));
            newSub.setStatus(SubscriptionStatus.ACTIVE);
            newSub.setActivationDate(Long.valueOf(activationDate));
            newSub.setOrganization(customer);
            newSub.setSubscriptionId(subscriptionId);
            newSub.setOwner(owner);

            newSub.setPurchaseOrderNumber("RN0043787");
            newSub.setAccessInfo("Buffalo access");
            newSub.setBaseURL("http://www.fujitsu.com");
            newSub.setLoginPath("/main");
            newSub.setTimeoutMailSent(false);

            newSub.setBillingContact(bc);
            newSub.setCutOffDay(cutOffDay);
            newSub.setUuid(UUID.randomUUID());

            if (paymentInfo != null) {
                mgr.persist(paymentInfo);
                mgr.flush();
                newSub.setPaymentInfo(paymentInfo);
            }

            newSub.setMarketplace(Marketplaces.findMarketplace(mgr,
                    marketplaceId));
            newSub.bindToProduct(product);
            newSub.setProductInstanceId(product.getProductId());
            newSub.setHistoryModificationTime(Long.valueOf(creationDate));
            mgr.persist(newSub);
            product.setOwningSubscription(newSub);
            mgr.flush();

            return newSub;
        } catch (ObjectNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void setProductType(Product productTemplate,
            Product theProduct) {
        if (productTemplate.getType() == ServiceType.TEMPLATE) {
            theProduct.setType(ServiceType.SUBSCRIPTION);
        } else if (productTemplate.getType() == ServiceType.CUSTOMER_TEMPLATE) {
            theProduct.setType(ServiceType.CUSTOMER_SUBSCRIPTION);
        } else if (productTemplate.getType() == ServiceType.PARTNER_TEMPLATE) {
            theProduct.setType(ServiceType.PARTNER_SUBSCRIPTION);
        }
    }

    public static Subscription createPartnerSubscription(DataService mgr,
            String customerId, String productId, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            Organization supplier, PaymentInfo paymentInfo)
            throws NonUniqueBusinessKeyException {
        try {
            Product template = Products.findProduct(mgr,
                    mgr.getReference(Organization.class, supplier.getKey()),
                    productId);
            Product product = template.copyForSubscription(
                    template.getTargetCustomer(), new Subscription());
            product.setTemplate(template);
            product.setOwningSubscription(null);
            product.setType(ServiceType.PARTNER_SUBSCRIPTION);
            setHistoryCreationTime(creationDate, product);
            mgr.persist(product);
            mgr.flush();
            return createSubscription(mgr, customerId, product, subscriptionId,
                    marketplaceId, creationDate, activationDate, paymentInfo, 1);
        } catch (ObjectNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Subscription createSubscription(DataService mgr,
            String customerId, Product product, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            PaymentInfo paymentInfo, int cutOffDay)
            throws NonUniqueBusinessKeyException {

        assertNotNull("No customerId given for the subscription", customerId);
        Organization customer = Organizations.findOrganization(mgr, customerId);

        BillingContact bc = PaymentInfos.createBillingContact(mgr, customer,
                "bc_id" + IdGenerator.generateRandomUUID());

        return createSubscription(mgr, customer, product, subscriptionId,
                marketplaceId, creationDate, activationDate, paymentInfo, bc,
                cutOffDay);
    }

    public static Subscription createSubscription(DataService mgr,
            Organization customer, Product product, String subscriptionId,
            String marketplaceId, long creationDate, long activationDate,
            PaymentInfo paymentInfo, BillingContact billingContact,
            int cutOffDay) throws NonUniqueBusinessKeyException {
        assertNotNull("No customerId given for the subscription", customer);

        Subscription newSub = new Subscription();
        
        PriceModel priceModel = product.getPriceModel();
        
        if(priceModel != null){
            newSub.setExternal(priceModel.isExternal());
        }
        newSub.setCreationDate(Long.valueOf(creationDate));
        newSub.setStatus(SubscriptionStatus.ACTIVE);
        newSub.setActivationDate(Long.valueOf(activationDate));
        newSub.setOrganization(customer);
        newSub.setSubscriptionId(subscriptionId);

        newSub.setPurchaseOrderNumber("RN0043787");
        newSub.setAccessInfo("Buffalo access");
        newSub.setBaseURL("http://www.fujitsu.com");
        newSub.setLoginPath("/main");
        newSub.setTimeoutMailSent(false);

        newSub.setBillingContact(billingContact);
        newSub.setCutOffDay(cutOffDay);

        newSub.setUuid(UUID.randomUUID());

        if (paymentInfo != null) {
            mgr.persist(paymentInfo);
            mgr.flush();
            newSub.setPaymentInfo(paymentInfo);
        }

        newSub.setMarketplace(Marketplaces.findMarketplace(mgr, marketplaceId));
        newSub.bindToProduct(product);
        newSub.setProductInstanceId(product.getProductId());
        newSub.setHistoryModificationTime(Long.valueOf(creationDate));
        mgr.persist(newSub);
        product.setOwningSubscription(newSub);
        mgr.flush();

        return newSub;
    }

    /**
     * Sets the history modification time for the product and depending objects.
     * 
     * @param creationDate
     *            The creation date to be set.
     * @param product
     *            The product to set the history modification date for.
     */
    public static void setHistoryCreationTime(long creationDate, Product product) {
        product.setHistoryModificationTime(Long.valueOf(creationDate));
        PriceModel priceModel = product.getPriceModel();
        if (priceModel != null) {
            priceModel.setHistoryModificationTime(Long.valueOf(creationDate));
            for (PricedEvent evt : priceModel.getConsideredEvents()) {
                evt.setHistoryModificationTime(Long.valueOf(creationDate));
            }
        }
    }

    public static Subscription findSubscription(DataService mgr,
            String subscriptionId, long orgKey) {
        Subscription sub = new Subscription();
        sub.setSubscriptionId(subscriptionId);
        sub.setOrganizationKey(orgKey);
        return (Subscription) mgr.find(sub);
    }

    public static UsageLicense createUsageLicense(DataService mgr,
            PlatformUser user, Subscription subscription)
            throws NonUniqueBusinessKeyException {
        return createUsageLicense(mgr, user, subscription, null);
    }

    public static UsageLicense createUsageLicense(DataService mgr,
            PlatformUser user, Subscription subscription, RoleDefinition role)
            throws NonUniqueBusinessKeyException {
        UsageLicense license = new UsageLicense();
        license.setUser(user);
        license.setSubscription(subscription);
        license.setRoleDefinition(role);
        mgr.persist(license);
        return license;
    }

    /**
     * Create a usage license for each of the given user and the given
     * subscription
     */
    public static List<UsageLicense> createUsageLicenses(DataService mgr,
            List<PlatformUser> users, Subscription subscription)
            throws NonUniqueBusinessKeyException {
        List<UsageLicense> licenses = new ArrayList<UsageLicense>();
        for (PlatformUser user : users) {
            licenses.add(createUsageLicense(mgr, user, subscription));
        }
        return licenses;
    }

    public static List<Subscription> createTestData(DataService mgr,
            Organization org, int numberOfSubscriptions) throws Exception {
        List<Subscription> result = new ArrayList<Subscription>();
        org = (Organization) mgr.find(org);
        List<Product> products = Products.createTestData(mgr, org,
                numberOfSubscriptions);
        for (int i = 0; i < numberOfSubscriptions; i++) {
            Product product = products.get(0);
            Subscription subscription = Subscriptions.createSubscription(mgr,
                    org.getOrganizationId(), product.getProductId(), "subId"
                            + i, org);
            for (PlatformUser user : org.getPlatformUsers()) {
                Subscriptions.createUsageLicense(mgr, user, subscription);
            }
            result.add(subscription);
        }
        return result;
    }

    public static void createSubscriptionHistory(final DataService ds,
            final long subscriptionObjKey, final long customerOrganizationKey,
            final String modificationDate, final int version,
            final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus,
            final long productobjkey, final long marketplaceObjKey)
            throws Exception {
        SubscriptionHistory subHist = new SubscriptionHistory();

        subHist.setInvocationDate(new Date());
        subHist.setObjKey(subscriptionObjKey);
        subHist.setObjVersion(version);
        subHist.setModdate(new SimpleDateFormat(DATE_PATTERN)
                .parse(modificationDate));
        subHist.setModtype(modificationType);
        subHist.setModuser("moduser");

        subHist.getDataContainer().setCreationDate(
                Long.valueOf(System.currentTimeMillis()));
        subHist.getDataContainer().setActivationDate(
                Long.valueOf(System.currentTimeMillis()));
        subHist.getDataContainer().setStatus(subscriptionStatus);
        subHist.getDataContainer().setSubscriptionId(
                "subscriptionid" + subscriptionObjKey);
        subHist.getDataContainer().setTimeoutMailSent(false);
        subHist.setOrganizationObjKey(customerOrganizationKey);
        subHist.setProductObjKey(productobjkey);
        subHist.setMarketplaceObjKey(Long.valueOf(marketplaceObjKey));
        subHist.setCutOffDay(1);
        ds.persist(subHist);
    }

    public static SubscriptionHistory createSubscriptionHistory(DataService ds,
            long objKey, Date modDate, Long activationDate, int version,
            long orgKey) throws Exception {
        return createSubscriptionHistory(ds, objKey, modDate, activationDate,
                version, orgKey, 1L);
    }

    public static SubscriptionHistory createSubscriptionHistory(DataService ds,
            long objKey, Date modDate, Long activationDate, int version,
            long orgKey, long prdObjKey) throws Exception {
        SubscriptionHistory subHist = new SubscriptionHistory();
        subHist.setObjKey(objKey);
        subHist.setModdate(modDate);
        subHist.setOrganizationObjKey(orgKey);
        subHist.setObjVersion(version);
        subHist.getDataContainer().setActivationDate(activationDate);
        subHist.getDataContainer().setCreationDate(new Long(1));
        subHist.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
        subHist.getDataContainer().setSubscriptionId("subid");
        subHist.setInvocationDate(new Date());
        subHist.setModtype(ModificationType.ADD);
        subHist.setModuser("moduser");
        subHist.setProductObjKey(prdObjKey);
        subHist.setCutOffDay(1);
        ds.persist(subHist);

        return subHist;
    }

    public static void teminateSubscription(DataService ds,
            Subscription subscription) throws Exception {
        Subscription sub = ds.getReference(Subscription.class,
                subscription.getKey());
        sub.setDeactivationDate(Long.valueOf(System.currentTimeMillis()));
        sub.setStatus(SubscriptionStatus.DEACTIVATED);
        sub.setSubscriptionId(String.valueOf(System.currentTimeMillis()));
        ds.persist(sub);
    }

    public static Subscription assignToUnit(DataService ds,
            Subscription subscription, UserGroup unit)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        Subscription sub = ds.getReference(Subscription.class,
                subscription.getKey());
        sub.setUserGroup(unit);
        ds.persist(sub);
        return sub;
    }

}
