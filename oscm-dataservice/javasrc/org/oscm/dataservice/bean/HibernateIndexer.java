/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: Dirk Bernsau
 *
 *  Creation Date: July 26, 2011
 *
 *  Completion Time: July 26, 2011
 *
 *******************************************************************************/

package org.oscm.dataservice.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.ejb.*;
import javax.persistence.Query;
import javax.xml.crypto.Data;

import org.apache.lucene.index.IndexReader;
import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Message driven bean to handle the index request objects sent by the business
 * logic.
 */
@Stateless
public class HibernateIndexer {

    private static final int BATCH_SIZE = 1000;

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(HibernateIndexer.class);
    @EJB(beanInterface = DataService.class)
    DataService dm;

    /**
     * It must be stupid simply indexing the passed domain objects without
     * additional BL and additional queries.<br>
     *
     * Returns the list of products to be indexed. In case if the template
     * product is updated, beside of the index fields for the this product also
     * these for broker, reseller and customer specific copies must be updated
     * as well.
     */
    private List<Product> getProductAndCopiesForIndexUpdate(Product product,
            Session session) {
        List<Product> productsToUpdate = new ArrayList<>();
        if (!product.isCopy()) {

            org.hibernate.Query query = session
                    .getNamedQuery("Product.getProductsForTemplateIndexUpdate");
            query.setParameter("template", product);
            query.setParameterList("state",
                    EnumSet.of(ServiceStatus.ACTIVE, ServiceStatus.INACTIVE,
                            ServiceStatus.SUSPENDED, ServiceStatus.OBSOLETE));
            query.setParameterList("type",
                    EnumSet.of(ServiceType.PARTNER_TEMPLATE,
                            ServiceType.CUSTOMER_TEMPLATE));
            productsToUpdate.addAll(
                    ParameterizedTypes.list(query.list(), Product.class));
        }
        return productsToUpdate;
    }

    @Asynchronous
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public void handleIndexing(DomainObject<?> object,
            ModificationType modType) {
        Session session = getSession();
        object = session.get(object.getClass(), object.getKey());

        if (object instanceof Product) {
            Product product = (Product) object;
            // Bug 9670: In case if a template of a partner or customer product
            // is modified we must also write the copies to the index
            if (modType == ModificationType.MODIFY) {
                List<Product> productsToUpdate = getProductAndCopiesForIndexUpdate(
                        product, session);
                handleListIndexing(ParameterizedTypes.list(productsToUpdate,
                        Product.class), session);
            }
            handleObjectIndexing(object, session);
        }
        if (object instanceof PriceModel) {
            handleObjectIndexing(((PriceModel) object).getProduct(), session);
        }
        if (object instanceof CatalogEntry) {
            handleObjectIndexing(((CatalogEntry) object).getProduct(), session);
        }
        if (object instanceof TechnicalProductTag) {
            TechnicalProduct tp = ((TechnicalProductTag) object)
                    .getTechnicalProduct();
            handleListIndexing(tp.getProducts(), session);
            return;
        }
        if (object instanceof TechnicalProduct) {
            handleListIndexing(((TechnicalProduct) object).getProducts(),
                    session);
        }
        if (object instanceof Category) {
            // This only happens when categories are "renamed". It will NOT be
            // invoked when categories are deleted.
            final org.hibernate.Query servicesQuery = session
                    .getNamedQuery("Category.findServices");
            servicesQuery.setParameter("categoryKey", object.getKey());
            handleListIndexing(ParameterizedTypes.list(servicesQuery.list(),
                    Product.class), session);
        }
        if (object instanceof Subscription) {
            Subscription subscription = (Subscription) object;
            if (isSubscriptionNotDeactivatedOrInvalid(subscription)) {
                handleObjectIndexing(object, session);
            }
        }
        if (object instanceof Parameter) {
            Parameter parameter = (Parameter) object;
            if (parameter.getParameterDefinition()
                    .getValueType() == ParameterValueType.STRING) {
                Product product = parameter.getParameterSet().getProduct();
                if (product != null) {
                    Subscription subscription = product.getOwningSubscription();
                    if (subscription != null
                            && isSubscriptionNotDeactivatedOrInvalid(
                                    subscription)) {
                        handleObjectIndexing(subscription, session);
                    }
                }
            }
        }
        if (object instanceof Uda) {
            Uda uda = (Uda) object;
            UdaDefinition udaDef = uda.getUdaDefinition();

            if (udaDef.getTargetType() == UdaTargetType.CUSTOMER_SUBSCRIPTION
                    && udaDef
                            .getConfigurationType() != UdaConfigurationType.SUPPLIER) {
                Subscription sub = session.get(Subscription.class,
                        uda.getTargetObjectKey());
                if (sub == null) {
                    logger.logDebug("uda target didn't match any subscription",
                            Log4jLogger.SYSTEM_LOG);
                } else {
                    handleObjectIndexing(sub, session);
                }
            }
        }
        if (object instanceof UdaDefinition) {
            UdaDefinition udaDef = (UdaDefinition) object;

            List<Product> prodList = udaDef.getOrganization().getProducts();
            List<Subscription> subList = new ArrayList<>();
            for (Product prod : prodList) {
                subList.add(prod.getOwningSubscription());
            }
            handleListIndexing(subList, session);
        }
    }

    private boolean isSubscriptionNotDeactivatedOrInvalid(
            Subscription subscription) {
        return subscription.getStatus() != SubscriptionStatus.DEACTIVATED
                && subscription.getStatus() != SubscriptionStatus.INVALID;
    }

    private void handleListIndexing(Collection<? extends DomainObject<?>> list,
            Session session) {
        if (list == null || session == null) {
            return;
        }

        FullTextSession fts = Search.getFullTextSession(session);

        for (DomainObject<?> obj : list) {
            if (obj != null) {
                fts.index(obj);
            }
        }
    }

    private void handleObjectIndexing(Object parameter, Session session) {

        if (parameter == null || session == null) {
            return;
        }

        FullTextSession fts = Search.getFullTextSession(session);
        fts.index(parameter);
    }

    @Asynchronous
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public void initIndexForFulltextSearch(final boolean force) {
        FullTextSession fullTextSession = Search
                .getFullTextSession(getSession());

        // check if index is already present (via query)
        boolean isIndexEmpty;
        SearchFactory searchFactory = fullTextSession.getSearchFactory();
        IndexReader reader = searchFactory.getIndexReaderAccessor()
                .open(Product.class, Subscription.class);

        try {
            isIndexEmpty = reader.numDocs() == 0;
        } finally {
            searchFactory.getIndexReaderAccessor().close(reader);
        }

        if (!isIndexEmpty) {
            if (!force) {
                // if so and force is NOT set, return without
                // indexing
                return;
            } else {
                // otherwise delete previous index
                fullTextSession.purgeAll(Product.class);
                fullTextSession.purgeAll(Subscription.class);
            }
        }

        // index all entities relevant for full text search
        // for it: get all products from all global marketplaces
        // (full text search only available for global marketplaces
        // by definition)

        Query query = dm.createNamedQuery("Marketplace.getAll");
        List<Marketplace> globalMps = ParameterizedTypes
                .list(query.getResultList(), Marketplace.class);

        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        for (Marketplace mp : globalMps) {
            // call find to ensure entity manager is registered
            dm.find(mp);

            StringBuffer nativeQueryString = new StringBuffer();
            nativeQueryString.append(
                    "SELECT p FROM Product p WHERE EXISTS (SELECT c FROM CatalogEntry c WHERE c.marketplace.key = ")
                    .append(mp.getKey())
                    .append(" AND c.dataContainer.visibleInCatalog=TRUE AND c.product.key = p.key AND p.dataContainer.status = :status)");

            org.hibernate.Query productsOnMpQuery = fullTextSession
                    .createQuery(nativeQueryString.toString());
            productsOnMpQuery.setParameter("status", ServiceStatus.ACTIVE);
            ScrollableResults results = productsOnMpQuery
                    .scroll(ScrollMode.FORWARD_ONLY);

            int index = 0;
            while (results.next()) {
                index++;
                fullTextSession.index(results.get(0));
                if (index % BATCH_SIZE == 0) {
                    fullTextSession.flushToIndexes();
                    fullTextSession.clear();
                }
            }
            results.close();

        }

        // index all active subscriptions
        org.hibernate.Query objectQuery = fullTextSession.createQuery(
                "SELECT s FROM Subscription s WHERE s.dataContainer.status NOT IN (:statuses)");
        objectQuery.setParameterList("statuses", new Object[] {
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.INVALID });
        ScrollableResults results = objectQuery.scroll(ScrollMode.FORWARD_ONLY);

        int index = 0;
        while (results.next()) {
            index++;
            fullTextSession.index(results.get(0));
            if (index % BATCH_SIZE == 0) {
                fullTextSession.flushToIndexes();
                fullTextSession.clear();
            }
        }

        results.close();
    }

    private Session getSession() {
        return dm.getSession();
    }

    public void setDataService(DataService dm) {
        this.dm = dm;
    }
}
