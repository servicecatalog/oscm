/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 11, 2011                                                      
 *                                                                              
 *  Completion Time: July 11, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.bridge;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.hibernate.search.analyzer.Discriminator;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Custom class bridge implementation for indexing product domain objects
 * together with related information.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ProductClassBridge implements FieldBridge, Discriminator {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(ProductClassBridge.class);

    public static final String DEFINED_LOCALES_SUFFIX = "locales";
    public static final String MP_ID = "mp_id";
    public static final String PRICEMODEL_DESCRIPTION = "prmDesc_";
    public static final String TAGS = "tags_";
    public static final String SERVICE_SHORT_DESC = "svcShort_";
    public static final String SERVICE_NAME = "svcName_";
    public static final String SERVICE_DESCRIPTION = "svcDesc_";
    public static final String CATEGORY_NAME = "catName_";

    public void set(String name, Object value, Document document,
            LuceneOptions luceneOptions) {

        if (value instanceof Product) {
            try {
                Product product = (Product) value;
                boolean mp_added = false;
                if (product.getOwningSubscription() == null) {
                    try {
                        // Write marketplace IDs of catalog entries belonging to
                        // the given product in the index. Only for customer
                        // specific copies we use the entries of the template
                        // (Bug 9784).
                        Product catalogEntryContainer = product;
                        if (product.getTargetCustomer() != null) {
                            catalogEntryContainer = product.getTemplate();
                        }
                        for (CatalogEntry entry : catalogEntryContainer
                                .getCatalogEntries()) {
                            if (entry.isVisibleInCatalog()
                                    && entry.getMarketplace() != null) {
                                FieldType fieldType = new FieldType();
                                fieldType.setIndexOptions(IndexOptions.DOCS);
                                fieldType.setStored(false);
                                fieldType.setOmitNorms(true);
                                Field f_mid = new Field(MP_ID, new String(entry
                                        .getMarketplace().getMarketplaceId()
                                        .toLowerCase().getBytes()), fieldType);
                                document.add(f_mid);
                                mp_added = true;
                            }
                        }
                    } catch (NullPointerException e) {
                        // FIXME happens sometimes on lazy test objects
                        logger.logError(
                                Log4jLogger.SYSTEM_LOG,
                                e,
                                LogMessageIdentifier.ERROR_SET_PRODUCT_TO_BRIDGE_OF_DOMAIN_OBJECT_FAILED);
                    }
                }
                if (!mp_added) {
                    // a non-public service does not need to be analyzed
                    // => by not adding any fields the index will not return
                    // these products
                    return;
                }
                EntityManager em = BridgeDataManager.getEntityManager();
                if (em != null) {
                    createProductLocaleFields(document, em, product);
                    PriceModel priceModel = product.getPriceModel();
                    if (priceModel != null) {
                        createLocaleFields(document, em, priceModel.getKey(),
                                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                                PRICEMODEL_DESCRIPTION, 2F);
                    }
                }

                HashMap<String, String> tagMapping = new HashMap<String, String>();
                for (TechnicalProductTag tag : product.getTechnicalProduct()
                        .getTags()) {
                    String locale = tag.getTag().getLocale();
                    String tagValue = tag.getTag().getValue();
                    if (tagMapping.get(locale) == null) {
                        tagMapping.put(locale, tagValue);
                    } else {
                        tagMapping.put(locale, tagMapping.get(locale) + " "
                                + tagValue);
                    }
                }
                for (String locale : tagMapping.keySet()) {
                    FieldType fieldType = new FieldType();
                    fieldType.setStored(false);
                    fieldType.setIndexOptions(IndexOptions.DOCS);
                    fieldType.setOmitNorms(false);
                    Field field = new Field(TAGS + locale,
                            tagMapping.get(locale), fieldType);
                    field.setBoost(1807F);
                    document.add(field);
                }
            } catch (Exception e) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_SET_PRODUCT_TO_BRIDGE_OF_DOMAIN_OBJECT_FAILED);
            }
        }
    }

    private void createProductLocaleFields(Document document, EntityManager em,
            Product product) {
        final long key = product.getTemplateOrSelf().getKey();
        createLocaleFields(document, em, key,
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME, SERVICE_NAME,
                3263443F);
        createLocaleFields(document, em, key,
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                SERVICE_DESCRIPTION, 3F);
        createLocaleFields(document, em, key,
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                SERVICE_SHORT_DESC, 7F);

        // search for categories assigned to this product
        List<Number> resultList = ParameterizedTypes
                .list(em.createNativeQuery(
                        "select cce.category_tkey from categorytocatalogentry cce join catalogentry ce on cce.catalogentry_tkey=ce.tkey where ce.product_tkey="
                                + product.getKey()).getResultList(),
                        Number.class);
        if (resultList != null && !resultList.isEmpty()) {
            for (Number categoryKey : resultList) {
                createLocaleFields(document, em, categoryKey.longValue(),
                        LocalizedObjectTypes.CATEGORY_NAME, CATEGORY_NAME, 43F);
            }
        }
    }

    private void createLocaleFields(Document document, EntityManager em,
            long key, Object type, String fieldPrefix, float boost) {
        // needed to keep track of non-empty fields
        StringBuffer definedLocales = new StringBuffer();

        Query query = em
                .createNamedQuery("LocalizedResource.getAllTextsWithLocale");
        query.setParameter("objectKey", Long.valueOf(key));
        query.setParameter("objectType", type);
        for (LocalizedResource resource : ParameterizedTypes.iterable(
                query.getResultList(), LocalizedResource.class)) {

            FieldType fieldType = new FieldType();
            fieldType.setStored(false);
            fieldType.setOmitNorms(false);
            fieldType.setIndexOptions(IndexOptions.DOCS);

            Field field = new Field(fieldPrefix + resource.getLocale(),
                    resource.getValue(), fieldType);
            field.setBoost(boost);
            document.add(field);
            // add to defined locales (needed for default locale handling)
            if ((resource.getValue().trim().length() > 0)) {
                if (definedLocales.length() > 0) {
                    definedLocales.append(", ");
                }
                definedLocales.append(resource.getLocale());
            }
        }

        // index defined locales
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
        fieldType.setStored(false);
        fieldType.setOmitNorms(false);
        Field field = new Field(fieldPrefix + DEFINED_LOCALES_SUFFIX,
                definedLocales.toString(), fieldType);
        field.setBoost(boost);
        document.add(field);
    }

    @Override
    public String getAnalyzerDefinitionName(Object value, Object entity,
            String field) {
        // specific analyzer discriminator allowing to choose the
        // filter/analyzer based on the fields locale
        if (field != null) {
            String[] name = field.split("_", 2);
            if (name.length == 2) {
                if ("de".equals(name[1])) {
                    return "de";
                }
                if ("en".equals(name[1])) {
                    return "en";
                }
            }
        }
        return null;
    }

}
