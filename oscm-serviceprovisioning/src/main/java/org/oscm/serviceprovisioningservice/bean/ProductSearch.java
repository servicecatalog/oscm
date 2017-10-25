/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Tag;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.Sorting;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.ListCriteria;

/**
 * Searches for services.
 * 
 * @author Enes Sejfi
 */
public class ProductSearch {

    // Input fields
    private DataService dm;
    private String marketplaceId;
    private ListCriteria listCriteria;
    private String defaultLocale;
    private String locale;

    // Temporary fields
    Marketplace marketplace;
    Organization currentUsersOrg;
    PlatformUser user;
    Set<Long> inVisibleProductKeys;
    private Tag tag;
    private boolean doObjectQuery;
    private boolean doCountQuery;
    private boolean sortingRequired;
    private String categoryId;
    private String sql;
    private String whereClause;
    private boolean parameterLocaleSet;
    private boolean parameterInvisibleSet;
    private boolean parameterDefaultLocaleSet;

    private static final String PARAM_LOCALE = "locale";
    private static final String PARAM_DEFAULT_LOCALE = "defaultLocale";
    private static final String PARAM_INVISIBLESERVICE_KEYSET = "inVisibleProductKeys";

    public ProductSearch(DataService dm, String marketplaceId,
            ListCriteria listCriteria, String defaultLocale, String locale,
            Set<Long> invisibleKeys) throws ObjectNotFoundException {
        this.dm = dm;
        this.marketplaceId = marketplaceId;
        this.listCriteria = listCriteria;
        this.defaultLocale = defaultLocale;
        this.locale = locale;
        this.inVisibleProductKeys = invisibleKeys;

        init();
    }

    /**
     * Needed initialization for the execute method.
     * 
     * @throws ObjectNotFoundException
     *             Thrown if the marketplace does not exist.
     */
    private void init() throws ObjectNotFoundException {
        marketplace = loadMarketplace(marketplaceId);

        // if no objects are required, do a count only execution
        doObjectQuery = listCriteria.getLimit() != 0;

        // some queries require a separate object count query due to paging
        doCountQuery = !doObjectQuery;

        sortingRequired = isSortingRequired(listCriteria);

        // is the current user logged in or anonymous
        user = dm.getCurrentUserIfPresent();
        if (user != null) {
            currentUsersOrg = user.getOrganization();
            // if given locale is not set, use users locale
            if (locale == null || locale.trim().length() == 0) {
                locale = user.getLocale();
            }
        }

        initParameterLocalesForProjection();
        initParameterInvisibleSet();
    }

    /**
     * Parameter locales are only used for the select list section.
     */
    private void initParameterLocalesForProjection() {
        parameterLocaleSet = false;
        parameterDefaultLocaleSet = false;
    }

    /**
     * Parameter inVisibleProductKeys are only used for not adminUser.
     */
    private void initParameterInvisibleSet() {
        if (inVisibleProductKeys == null || inVisibleProductKeys.isEmpty()) {
            parameterInvisibleSet = false;
        } else {
            parameterInvisibleSet = true;
        }
    }

    /**
     * Loads a marketplace.
     * 
     * @param marketplaceId
     *            Given marketplace id
     * @return Marketplace instance
     * @throws ObjectNotFoundException
     *             Thrown if the marketplace does not exist
     */
    private Marketplace loadMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace marketplace = new Marketplace(marketplaceId);
        return (Marketplace) dm.getReferenceByBusinessKey(marketplace);
    }

    /**
     * Checks if sorting is required. if true, a sorting statement is added at
     * the end of the SQL statement in method prepareSortOrder
     * 
     * @param listCriteria
     *            List Criteria
     * @return True if the ListCriteria instance is not a
     *         KeyRestrictedListCriteria otherwise false.
     */
    private boolean isSortingRequired(ListCriteria listCriteria) {
        boolean sortingRequried = true; // default: sort by name
        if (listCriteria instanceof KeyRestrictedListCriteria
                && listCriteria.getSorting() == null) {
            sortingRequried = false;
        }
        return sortingRequried;
    }

    /**
     * Executes the product search.
     * 
     * @return Result instance containing the found services and the number of
     *         services in the database
     * @throws ObjectNotFoundException
     *             Thrown if the marketplace or category was not found
     */
    public ProductSearchResult execute() throws ObjectNotFoundException {
        List<Product> foundServices = new ArrayList<Product>();
        int resultSize = 0;

        // is needed for object and count query
        prepareSql();

        // search products on current page
        if (doObjectQuery) {
            foundServices = executeObjectQuery();
            resultSize = foundServices.size();
        }

        // count all products of all pages
        if (doCountQuery) {
            resultSize = executeCountQuery();
        }

        // result
        ProductSearchResult result = new ProductSearchResult();
        result.setResultSize(resultSize);
        result.setServices(foundServices);
        return result;
    }

    /**
     * Searches for products
     * 
     * @return found VO services
     */
    @SuppressWarnings("unchecked")
    private List<Product> executeObjectQuery() {
        Query query = dm.createNativeQuery(sql, Product.class);
        setParameters(query);

        if (listCriteria.getOffset() > 0) {
            query.setFirstResult(listCriteria.getOffset());
            doCountQuery = true;
        }

        if (listCriteria.getLimit() >= 0) {
            query.setMaxResults(listCriteria.getLimit());
            doCountQuery = true;
        }

        List<Product> result = query.getResultList();
        return result;
    }

    /**
     * Counts for products. A product count is done if a count was required
     * (listCriteria.limit = 0) or the services should be paged
     * 
     * @return number of services in database
     */
    private int executeCountQuery() {
        initParameterLocalesForProjection();

        StringBuffer queryBuffer = new StringBuffer(
                "SELECT COUNT(p) from product AS p");
        queryBuffer.append(whereClause.toString());

        Query query = dm.createNativeQuery(queryBuffer.toString());
        setParameters(query);

        @SuppressWarnings("unchecked")
        List<BigInteger> countResultList = query.getResultList();
        return countResultList.get(0).intValue();
    }

    /**
     * Set the parameters for the Jpa Query instance
     * 
     * @param query
     *            Jpa Query instance
     */
    private void setParameters(Query query) {
        if (currentUsersOrg != null) {
            query.setParameter("customerKey",
                    Long.valueOf(currentUsersOrg.getKey()));
        }
        query.setParameter("marketplaceId", marketplaceId);
        if (tag != null) {
            query.setParameter("tagKey", Long.valueOf(tag.getKey()));
        }
        if (categoryId != null) {
            addParametersForCategory(marketplace, categoryId, query);
        }
        if (parameterLocaleSet) {
            query.setParameter(PARAM_LOCALE, locale);
        }
        if (parameterDefaultLocaleSet) {
            query.setParameter(PARAM_DEFAULT_LOCALE, defaultLocale);
        }
        if (parameterInvisibleSet) {
            query.setParameter(PARAM_INVISIBLESERVICE_KEYSET,
                    inVisibleProductKeys);
        }
    }

    /**
     * Generate a SQL statement needed for the database search.
     * 
     * @throws ObjectNotFoundException
     *             Thrown if the category or tag was not found
     */
    private void prepareSql() throws ObjectNotFoundException {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append(generateProjectionStmt());
        sqlStmt.append(generateFromStmt());

        whereClause = generateWhereClauseStmt();
        sqlStmt.append(whereClause);

        sqlStmt.append(generateSortOrderStmt());
        sql = sqlStmt.toString();
    }

    /**
     * Creates a select list statement.
     * 
     * @return comma separated select list
     */
    private String generateProjectionStmt() {
        StringBuffer buffer = new StringBuffer("SELECT ");

        // First column 'p' : All product data
        buffer.append("p.*");

        // Second column 'localName' : localized product name resp. product id
        // if no localization exists
        boolean use2ndLocale = !defaultLocale.equals(locale);

        buffer.append(", COALESCE(");
        if (use2ndLocale) {
            buffer.append(prepareGetLocalizedProductId(false));
            buffer.append(",");
        }
        buffer.append(prepareGetLocalizedProductId(true));
        buffer.append(",");

        buffer.append("p.productid"); // final fall back: productId
        buffer.append(") AS localName");

        // Third column 'rating' : If rating should be sorted
        if (Sorting.RATING_ASCENDING.equals(listCriteria.getSorting())
                || Sorting.RATING_DESCENDING.equals(listCriteria.getSorting())) {
            buffer.append(prepareAverageRatingForSorting());
        }

        return buffer.toString();
    }

    /**
     * Generates a rating column in the select list
     * 
     * @return rating statement
     */
    private String prepareAverageRatingForSorting() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(", COALESCE(");
        buffer.append("   (SELECT averagerating");
        buffer.append("      FROM productfeedback AS pf");
        buffer.append("     WHERE pf.product_tkey = p.tkey)");
        buffer.append("   ,");
        buffer.append("   (SELECT averagerating");
        buffer.append("      FROM productfeedback AS pf, product AS pt");
        buffer.append("     WHERE p.template_tkey = pt.tkey");
        buffer.append("       AND pf.product_tkey = pt.tkey)");
        buffer.append("  ) AS rating");
        return buffer.toString();
    }

    private String generateFromStmt() {
        return " FROM product AS p ";
    }

    /**
     * Generates the where clause for the SQL statement
     * 
     * @return where statement
     * @throws ObjectNotFoundException
     *             Thrown if the tag or category was not in found in database
     */
    private String generateWhereClauseStmt() throws ObjectNotFoundException {
        StringBuffer buffer = new StringBuffer(" WHERE ");

        buffer.append(prepareProductStatusRestriction());

        boolean currentUserAnonymous = false;
        if (currentUsersOrg != null) {
            buffer.append(prepareCustomerRestriction());
        } else {
            buffer.append(" AND (p.type='");
            buffer.append(ServiceType.TEMPLATE + "' OR p.type='");
            buffer.append(ServiceType.PARTNER_TEMPLATE + "') ");
            buffer.append("");
            currentUserAnonymous = true;
        }
        buffer.append(prepareServiceVisibilityRestriction(currentUserAnonymous));

        buffer.append(prepareTagRestriction());
        buffer.append(prepareCategoryRestriction());
        buffer.append(prepareIdListRestriction());
        return buffer.toString();
    }

    /**
     * Generates a SQL statement to restrict the service visibility in the SQL
     * statement
     * 
     * @param currentUserAnonymous
     * @return Service visibility SQL statement
     */
    private String prepareServiceVisibilityRestriction(
            boolean currentUserAnonymous) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" AND EXISTS");
        buffer.append("     (SELECT m");
        buffer.append("       FROM marketplace AS m");
        buffer.append("            LEFT JOIN catalogentry AS ce ON m.tkey = ce.marketplace_tkey");
        buffer.append("       WHERE m.marketplaceid = :marketplaceId");
        buffer.append("         AND ce.visibleincatalog = true");
        buffer.append("         AND (ce.product_tkey = p.tkey OR (p.type = 'CUSTOMER_TEMPLATE' AND ce.product_tkey = p.template_tkey))");
        if (currentUserAnonymous) {
            buffer.append("     AND ce.anonymousvisible = true");
        }
        buffer.append("     )");
        if (parameterInvisibleSet) {
            buffer.append(" AND p.tkey NOT IN (:"
                    + PARAM_INVISIBLESERVICE_KEYSET + ") ");
        }
        return buffer.toString();
    }

    /**
     * Generates sort order statement
     * 
     * @return
     */
    private String generateSortOrderStmt() {
        StringBuffer buffer = new StringBuffer();
        if (doObjectQuery) {
            if (sortingRequired) {
                buffer.append(prepareSortOrder(listCriteria));
            }
        }
        return buffer.toString();
    }

    /**
     * 
     * @return
     */
    private String prepareIdListRestriction() {
        StringBuffer buffer = new StringBuffer();

        if (listCriteria instanceof KeyRestrictedListCriteria) {
            // add further restriction
            KeyRestrictedListCriteria kc = (KeyRestrictedListCriteria) listCriteria;
            if (kc.isRestricted()) {
                buffer.append(" AND p.tkey IN (");
                buffer.append(kc.getRestrictionString());
                buffer.append(")");
            }
        }
        return buffer.toString();
    }

    /**
     * 
     * @param listCriteria
     * @return
     */
    private String prepareSortOrder(ListCriteria listCriteria) {
        StringBuffer buffer = new StringBuffer(" ORDER BY ");

        Sorting sorting = listCriteria.getSorting();
        if (Sorting.NAME_ASCENDING.equals(sorting)) {
            buffer.append("localName ASC");

        } else if (Sorting.NAME_DESCENDING.equals(sorting)) {
            buffer.append("localName DESC");

        } else if (Sorting.ACTIVATION_ASCENDING.equals(sorting)) {
            buffer.append("p.provisioningdate ASC, localName ASC");

        } else if (Sorting.RATING_ASCENDING.equals(sorting)
                || Sorting.RATING_DESCENDING.equals(sorting)) {
            buffer.append("rating ");
            buffer.append((Sorting.RATING_ASCENDING.equals(sorting) ? "ASC NULLS FIRST"
                    : "DESC NULLS LAST"));
            buffer.append(", localName ASC");

        } else {
            buffer.append("p.provisioningdate DESC, localName ASC");
        }
        return buffer.toString();
    }

    /**
     * Returns a SQL stmt to filter services for categories
     * 
     * @param categoryId
     *            Category id
     * @return SQL containing category restriction
     */
    String prepareCategoryRestriction() throws ObjectNotFoundException {
        StringBuffer buffer = new StringBuffer();
        categoryId = listCriteria.getCategoryId();
        if (categoryId != null) {
            checkIfCategoryExists(categoryId);
            buffer.append(" AND EXISTS (");
            buffer.append(" SELECT p.tkey");
            buffer.append("   FROM category c, categorytocatalogentry ctce, catalogentry ce");
            buffer.append("  WHERE c.categoryid = :categoryId");
            buffer.append("    AND c.marketplacekey = :marketplaceKey");
            buffer.append("    AND c.tkey = ctce.category_tkey");
            buffer.append("    AND ctce.catalogentry_tkey = ce.tkey");
            buffer.append("    AND (ce.product_tkey = p.tkey OR (ce.product_tkey = p.template_tkey AND p.type = 'CUSTOMER_TEMPLATE'))");
            buffer.append(" )");
        }
        return buffer.toString();
    }

    /**
     * Verify if the category exist in database
     * 
     * @param categoryId
     *            Given category id
     * @throws ObjectNotFoundException
     *             Thrown if the category does not exist
     */
    private void checkIfCategoryExists(String categoryId)
            throws ObjectNotFoundException {
        categoryId = categoryId.trim();
        Category category = new Category();
        category.setMarketplace(marketplace);
        category.setCategoryId(categoryId);
        dm.getReferenceByBusinessKey(category);
    }

    /**
     * Creates sql statement for where clause, to filter for a specific tag.
     * 
     * @param filter
     *            Tag Filter
     * @param locale
     *            locale of the current user
     * @return Tag SQL statement
     * @throws ObjectNotFoundException
     *             Thrown if the tag does not exist
     */
    private String prepareTagRestriction() throws ObjectNotFoundException {
        String tagFilter = "";
        String filter = listCriteria.getFilter();

        // generate filter query part if necessary
        if (filter != null && filter.trim().length() > 0) {
            filter = filter.trim();
            String[] parts = filter.split(",", 2);
            String tagLocale = locale;
            if (parts.length == 2) {
                tagLocale = parts[0];
                filter = parts[1];
            } else {
                filter = parts[0];
            }

            tag = (Tag) dm
                    .getReferenceByBusinessKey(new Tag(tagLocale, filter));

            StringBuffer b = new StringBuffer();
            b.append(" AND EXISTS");
            b.append(" (");
            b.append("   SELECT tpt ");
            b.append("     FROM technicalproducttag AS tpt");
            b.append("    WHERE tpt.technicalproduct_tkey = p.technicalproduct_tkey");
            b.append("      AND tpt.tag_tkey = :tagKey");
            b.append(" )");

            tagFilter = b.toString();
        }
        return tagFilter;
    }

    private String prepareGetLocalizedProductId(boolean defaultLocale) {

        String parameterName;
        if (defaultLocale) {
            parameterDefaultLocaleSet = true;
            parameterName = PARAM_DEFAULT_LOCALE;
        } else {
            parameterLocaleSet = true;
            parameterName = PARAM_LOCALE;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("(SELECT value ");
        buffer.append("   FROM localizedresource ");
        buffer.append("  WHERE locale = :" + parameterName);
        buffer.append("    AND objecttype = 'PRODUCT_MARKETING_NAME'");
        buffer.append("    AND");
        buffer.append("    (");
        buffer.append("        (p.template_tkey IS NULL AND objectkey = p.tkey)");
        buffer.append("     OR (p.template_tkey IS NOT NULL AND objectkey = p.template_tkey))");
        buffer.append("    )");
        return buffer.toString();
    }

    String prepareProductStatusRestriction() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" (p.status = '");
        if (user != null) {
            if (marketplace.getOrganization() == currentUsersOrg
                    && user.hasRole(UserRoleType.MARKETPLACE_OWNER)) {
                buffer.append(ServiceStatus.SUSPENDED.name());
                buffer.append("' OR p.status = '");
            }
        }
        buffer.append(ServiceStatus.ACTIVE.name());
        buffer.append("')");
        return buffer.toString();
    }

    private String prepareCustomerRestriction() {
        StringBuffer buffer = new StringBuffer();
        // customer specific and subscription products have the
        // customer_tkey set - so the one to read must have it set but no
        // subscription must exist for it
        buffer.append(" AND ((p.targetcustomer_tkey = :customerKey");
        buffer.append(" AND p.type='");
        buffer.append(ServiceType.CUSTOMER_TEMPLATE + "')");
        // or we have a template but no customer specific must exist for the
        // customer...
        buffer.append(" OR (p.template_tkey IS NULL");
        buffer.append(" AND NOT EXISTS (SELECT cup FROM product AS cup WHERE cup.template_tkey = p.tkey AND cup.targetcustomer_tkey = :customerKey");
        // ...that is no subscription product
        buffer.append(" AND cup.type='");
        buffer.append(ServiceType.CUSTOMER_TEMPLATE + "')");
        buffer.append(") OR p.type='");
        buffer.append(ServiceType.PARTNER_TEMPLATE + "')");
        return buffer.toString();
    }

    /**
     * Adds parameters for the category search
     * 
     * @param marketplace
     *            Marketplace
     * @param categoryId
     *            Category Id
     * @param jpaQuery
     *            Jpa Query
     */
    private void addParametersForCategory(Marketplace marketplace,
            String categoryId, Query jpaQuery) {
        jpaQuery.setParameter("categoryId", categoryId);
        jpaQuery.setParameter("marketplaceKey",
                Long.valueOf(marketplace.getKey()));
    }
}
