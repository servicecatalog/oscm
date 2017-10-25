/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: goebel                                                      
 *                                                                              
 *  Creation Date: 27.05.2011                                                      
 *                                                                              
 *  Completion Time: 27.05.2011                                            
 *                                                                              
 *******************************************************************************/
package org.oscm.marketplace.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.categorizationService.local.CategorizationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.marketplace.assembler.CategoryAssembler;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOService;

@Stateless
@Remote(CategorizationService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class CategorizationServiceBean implements CategorizationService,
        CategorizationServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(CategorizationServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    private LocalizerServiceLocal localizer;

    @EJB(beanInterface = TaskQueueServiceLocal.class)
    public TaskQueueServiceLocal tqs;

    @Resource
    protected SessionContext sessionCtx;

    @Override
    public List<VOCategory> getCategories(String marketplaceId, String locale) {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        ArgumentValidator.notEmptyString("locale", locale);
        final Query queryCategories = dm
                .createNamedQuery("Category.findByMarketplaceId");
        queryCategories.setParameter("marketplaceId", marketplaceId);
        final List<Category> categories = ParameterizedTypes.list(
                queryCategories.getResultList(), Category.class);
        final ArrayList<VOCategory> voCategories = new ArrayList<VOCategory>();
        final LocalizerFacade facade = new LocalizerFacade(localizer, locale);
        for (Category category : categories) {
            voCategories.add(CategoryAssembler.toVOCategory(category, facade));
        }
        logger.logDebug("getCategories(String, String) exited");
        return voCategories;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void saveCategories(List<VOCategory> toBeSaved,
            List<VOCategory> toBeDeleted, String locale)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException {

        if (toBeDeleted != null && !toBeDeleted.isEmpty()) {
            deleteCategories(toBeDeleted);
        }

        if (toBeSaved != null && !toBeSaved.isEmpty()) {
            BLValidator.isNotBlank("locale", locale);
            saveCategories(toBeSaved, locale);
        }
        logger.logDebug("saveCategories(List<VOCategory>, List<VOCategory>, String) exited");
    }

    /**
     * @param toBeDeleted
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     */
    private void deleteCategories(List<VOCategory> toBeDeleted)
            throws OperationNotPermittedException, ObjectNotFoundException {
        final Organization currentOrg = dm.getCurrentUser().getOrganization();
        final Query marketplaceQuery = dm
                .createNamedQuery("Marketplace.findByBusinessKey");
        for (VOCategory voCategory : toBeDeleted) {
            final Category category = dm.getReference(Category.class,
                    voCategory.getKey());
            try {
                marketplaceQuery.setParameter("marketplaceId", category
                        .getMarketplace().getMarketplaceId());
            } catch (EntityNotFoundException e) {
                throw new ObjectNotFoundException(ClassEnum.MARKETPLACE,
                        voCategory.getMarketplaceId());
            }
            final Marketplace marketplace = (Marketplace) marketplaceQuery
                    .getSingleResult();
            PermissionCheck.owns(marketplace, currentOrg, logger, null);
            List<PlatformUser> usersToBeNotified = collectUsersToBeNotified(category);
            prefetchRequiredObject(category);
            dm.remove(category);
            sendNotification(category, usersToBeNotified);
        }
    }

    /**
     * load products before removal because we need them in IndexRequestMessage
     * 
     * @param category
     */
    private void prefetchRequiredObject(Category category) {
        final List<CategoryToCatalogEntry> categoryToCatalogEntries = category
                .getCategoryToCatalogEntry();
        for (CategoryToCatalogEntry categoryToCatalogEntry : categoryToCatalogEntries) {
            categoryToCatalogEntry.getCatalogEntry().getProduct();
        }
    }

    /**
     * @param toBeSaved
     * @param locale
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     * @throws ValidationException
     * @throws ConcurrentModificationException
     * @throws OperationNotPermittedException
     */
    private void saveCategories(List<VOCategory> toBeSaved, String locale)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, ConcurrentModificationException,
            OperationNotPermittedException {

        final Organization currentOrg = dm.getCurrentUser().getOrganization();

        List<VOCategory> updateCategories = new ArrayList<VOCategory>();
        List<VOCategory> createCategories = new ArrayList<VOCategory>();
        for (VOCategory category : toBeSaved) {
            if (category.getKey() == 0) {
                createCategories.add(category);
            } else {
                updateCategories.add(category);
            }
        }

        // first update categories and in a second step create new categories
        persistCategories(locale, currentOrg, updateCategories);
        persistCategories(locale, currentOrg, createCategories);
    }

    private void persistCategories(String locale,
            final Organization currentOrg, final List<VOCategory> catList)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, ConcurrentModificationException {

        final Query marketplaceQuery = dm
                .createNamedQuery("Marketplace.findByBusinessKey");
        Category lCat = new Category();

        validateNames(catList);
        checkNameUniqueness(catList, locale);

        for (VOCategory voCategory : catList) {
            marketplaceQuery.setParameter("marketplaceId",
                    voCategory.getMarketplaceId());
            final Marketplace marketplace = (Marketplace) marketplaceQuery
                    .getSingleResult();
            PermissionCheck.owns(marketplace, currentOrg, logger, null);
            lCat.setCategoryId(voCategory.getCategoryId());
            lCat.setMarketplace(marketplace);
            lCat.setKey(voCategory.getKey());
            dm.validateBusinessKeyUniqueness(lCat);
            final Category category;
            if (voCategory.getKey() == 0) {
                category = new Category();
            } else {
                category = dm.getReference(Category.class, voCategory.getKey());
            }
            CategoryAssembler.updateCategory(category, voCategory);
            category.setMarketplace(marketplace);
            try {
                dm.persist(category);
            } catch (NonUniqueBusinessKeyException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
            localizer.storeLocalizedResource(locale, category.getKey(),
                    LocalizedObjectTypes.CATEGORY_NAME, voCategory.getName());
        }
    }

    private void validateNames(List<VOCategory> toBeSaved)
            throws ValidationException {
        for (VOCategory voCategory : toBeSaved) {
            BLValidator.isName("name", voCategory.getName(), true);
        }
    }

    /**
     * We need to check new categories also, to see if some of them have the
     * same name. Since they do not already exist in the database, remember them
     * here in this set.
     * 
     * @param toBeSaved
     * @param locale
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    private void checkNameUniqueness(List<VOCategory> toBeSaved, String locale)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        final List<String> marketplaceIdList = createExistingMarketplaceList(toBeSaved);
        for (String marketplaceId : marketplaceIdList) {
            List<VOCategory> toBeSavedForMarketplace = createCategoriesListForMarketplace(
                    marketplaceId, toBeSaved);
            checkNameUniquenessForMarketplace(marketplaceId,
                    toBeSavedForMarketplace, locale);
        }
    }

    private List<String> createExistingMarketplaceList(
            List<VOCategory> categories) {
        List<String> marketplaces = new ArrayList<String>();
        for (VOCategory category : categories) {
            if (!marketplaces.contains(category.getMarketplaceId())) {
                marketplaces.add(category.getMarketplaceId());
            }
        }
        return marketplaces;
    }

    private List<VOCategory> createCategoriesListForMarketplace(
            String marketplaceId, List<VOCategory> categories) {
        List<VOCategory> categoriesForMarketplace = new ArrayList<VOCategory>();
        for (VOCategory category : categories) {
            if (marketplaceId.equals(category.getMarketplaceId())) {
                categoriesForMarketplace.add(category);
            }
        }
        return categoriesForMarketplace;
    }

    private void checkNameUniquenessForMarketplace(String marketplaceId,
            List<VOCategory> toBeSavedForMarketplace, String locale)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        final Map<String, String> resultCategories = retrieveCategoriesFromDB(
                marketplaceId, toBeSavedForMarketplace, locale);

        // update virtually
        for (VOCategory voCategory : toBeSavedForMarketplace) {
            resultCategories.put(voCategory.getCategoryId(),
                    voCategory.getName());
        }

        Collection<String> existingNames = resultCategories.values();
        for (VOCategory voCategory : toBeSavedForMarketplace) {
            if (containsNameMore(existingNames, voCategory.getName())) {
                NonUniqueBusinessKeyException e = new NonUniqueBusinessKeyException(
                        "Category '" + voCategory.getName()
                                + "' already exisits in marketplace "
                                + marketplaceId);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_NON_UNIQUE_CATEGORY_NAME,
                        voCategory.getCategoryId());
                throw e;
            }
        }
    }

    private Map<String, String> retrieveCategoriesFromDB(String marketplaceId,
            List<VOCategory> toBeSavedForMarketplace, String locale)
            throws ObjectNotFoundException {
        final Map<String, String> resultCategories = new HashMap<String, String>();
        final Query uniqueName = dm
                .createNamedQuery("Category.findByLocalizedName");
        for (VOCategory voCategory : toBeSavedForMarketplace) {
            uniqueName.setParameter("locale", locale);
            uniqueName.setParameter("objectType",
                    LocalizedObjectTypes.CATEGORY_NAME);
            uniqueName.setParameter("value", voCategory.getName());
            uniqueName.setParameter("marketplaceKey",
                    getMarketplaceKeyByMarketplaceId(marketplaceId));
            uniqueName.setParameter("key", Long.valueOf(voCategory.getKey()));
            List<Category> dbDuplicates = ParameterizedTypes.list(
                    uniqueName.getResultList(), Category.class);
            if (dbDuplicates.size() > 0) {
                Category category = dbDuplicates.get(0);
                resultCategories.put(category.getCategoryId(),
                        voCategory.getName());
            }

        }
        return resultCategories;
    }

    private Long getMarketplaceKeyByMarketplaceId(String marketplaceId)
            throws ObjectNotFoundException {
        final Query marketplaceQuery = dm
                .createNamedQuery("Marketplace.findByBusinessKey");
        marketplaceQuery.setParameter("marketplaceId", marketplaceId);
        final Marketplace marketplace;
        try {
            marketplace = (Marketplace) marketplaceQuery.getSingleResult();
        } catch (NoResultException e) {
            throw new ObjectNotFoundException(ClassEnum.MARKETPLACE,
                    marketplaceId);
        }

        return Long.valueOf(marketplace.getKey());
    }

    private boolean containsNameMore(Collection<String> names, String targetName) {
        int count = 0;
        for (String name : names) {
            if (targetName.equals(name)) {
                count++;
                if (count > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    void sendNotification(Category removedCategory,
            List<PlatformUser> usersToBeNotified) {
        LocalizerFacadeBundle bundle = new LocalizerFacadeBundle();

        Long marketplaceKey = null;
        if (removedCategory.getMarketplace() != null) {
            marketplaceKey = Long.valueOf(removedCategory.getMarketplace()
                    .getKey());
        }

        SendMailPayload payload = new SendMailPayload();
        for (PlatformUser user : usersToBeNotified) {
            String localizedCategoryName = loadLocalizedName(removedCategory,
                    bundle, user.getLocale());

            payload.addMailObjectForUser(user.getKey(),
                    EmailType.CATEGORY_REMOVED,
                    new Object[] { localizedCategoryName }, marketplaceKey);
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        tqs.sendAllMessages(Arrays.asList(message));

    }

    private List<PlatformUser> collectUsersToBeNotified(Category removedCategory) {
        Query query = dm.createNamedQuery("Category.findAdminsOfServices");
        query.setParameter("categoryKey",
                Long.valueOf(removedCategory.getKey()));
        List<PlatformUser> admins = ParameterizedTypes.list(
                query.getResultList(), PlatformUser.class);
        return admins;
    }

    private String loadLocalizedName(Category category,
            LocalizerFacadeBundle bundle, String locale) {
        String localizedCategoryName = bundle.getText(category.getKey(),
                LocalizedObjectTypes.CATEGORY_NAME, locale);
        if (localizedCategoryName.equals("")) {
            localizedCategoryName = category.getCategoryId();
        }
        return localizedCategoryName;
    }

    class LocalizerFacadeBundle {

        Map<String, LocalizerFacade> bundle = new HashMap<String, LocalizerFacade>();

        String getText(long objectKey, LocalizedObjectTypes objectType,
                String locale) {
            return localizer(locale).getText(objectKey, objectType);
        }

        LocalizerFacade localizer(String locale) {
            LocalizerFacade localizerFacade = bundle.get(locale);
            if (localizerFacade == null) {
                localizerFacade = new LocalizerFacade(localizer, locale);
                bundle.put(locale, localizerFacade);
            }
            return localizerFacade;
        }
    }

    @Override
    public List<VOService> getServicesForCategory(long categoryKey) {
        final Query servicesQuery = dm
                .createNamedQuery("Category.findServices");
        servicesQuery.setParameter("categoryKey", Long.valueOf(categoryKey));
        final List<Product> list = ParameterizedTypes.list(
                servicesQuery.getResultList(), Product.class);
        final List<VOService> listToReturn = new ArrayList<VOService>();
        final LocalizerFacade facade = new LocalizerFacade(localizer, "2");
        for (Product p : list) {
            listToReturn.add(ProductAssembler.toVOProduct(p, facade));
        }
        return listToReturn;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean updateAssignedCategories(CatalogEntry catalogEntry,
            List<VOCategory> newcategories) throws ObjectNotFoundException {

        if (catalogEntry == null)
            return false;

        checkIfConsistentMarketplace(catalogEntry, newcategories);

        // get old attached categories
        List<CategoryToCatalogEntry> oldCategories = catalogEntry
                .getCategoryToCatalogEntry();
        boolean isCatagoriesChanged = false;
        if (oldCategories != null && !oldCategories.isEmpty()) {
            // remove those categories that are no longer in new assignment list
            isCatagoriesChanged = deleteDeassignedCategories(catalogEntry,
                    oldCategories, newcategories);
        }

        // attach new categories
        if (newcategories != null && !newcategories.isEmpty()) {
            for (VOCategory category : newcategories) {
                // if categoryId is not in oldCategories: its newly assigned
                if (!isAssigned(category, oldCategories)) {
                    isCatagoriesChanged = true;
                    CategoryToCatalogEntry cce = new CategoryToCatalogEntry();
                    cce.setCatalogEntry(catalogEntry);
                    Category categoryObj = dm.getReference(Category.class,
                            category.getKey());
                    cce.setCategory(categoryObj);
                    if (catalogEntry.getCategoryToCatalogEntry() == null) {
                        catalogEntry
                                .setCategoryToCatalogEntry(new ArrayList<CategoryToCatalogEntry>());
                    }
                    catalogEntry.getCategoryToCatalogEntry().add(cce);
                }
            }
        }
        dm.flush();
        return isCatagoriesChanged;
    }

    private boolean deleteDeassignedCategories(CatalogEntry ce,
            List<CategoryToCatalogEntry> oldCategories,
            List<VOCategory> assignedCategories) {
        List<CategoryToCatalogEntry> categoriesToDelete = new ArrayList<CategoryToCatalogEntry>();
        List<String> newCategoryIds = new ArrayList<String>();
        if (assignedCategories != null) {
            for (VOCategory newCat : assignedCategories) {
                newCategoryIds.add(newCat.getCategoryId());
            }
        }
        for (CategoryToCatalogEntry oldCat : oldCategories) {
            if (!newCategoryIds.contains(oldCat.getCategory().getCategoryId())) {
                categoriesToDelete.add(oldCat);
            }
        }
        for (CategoryToCatalogEntry delCat : categoriesToDelete) {
            dm.remove(delCat);
        }
        ce.setCategoryToCatalogEntry(new ArrayList<CategoryToCatalogEntry>());
        dm.flush();

        if (categoriesToDelete.size() > 0) {
            return true;
        }
        return false;
    }

    private void checkIfConsistentMarketplace(CatalogEntry catalogEntry,
            List<VOCategory> assignedCategories) {
        if (assignedCategories == null || assignedCategories.size() < 1)
            return;
        // check if all marketplaceId's are the same
        Set<String> marketplaceIds = new HashSet<String>();
        for (VOCategory cat : assignedCategories) {
            marketplaceIds.add(cat.getMarketplaceId());
        }
        if (marketplaceIds.size() > 1) {
            throw new IllegalArgumentException(
                    "Categories from different marketplaces are not allowed.");
        }
        // checks if catalogEntry is on the same marketplace as ALL
        // assignedCategories
        if (catalogEntry.getMarketplace() == null) {
            if (assignedCategories.size() > 0) {
                throw new IllegalArgumentException(
                        "Categories must not be assigned when marketplaceId is null.");
            } else
                ;
        } else {
            String marketplaceId = catalogEntry.getMarketplace()
                    .getMarketplaceId();

            if (!assignedCategories.get(0).getMarketplaceId()
                    .equals(marketplaceId)) {
                throw new IllegalArgumentException(
                        "Categories marketplace does not match catalogEntry's marketplace.");
            }
        }
    }

    private boolean isAssigned(VOCategory voCategory,
            List<CategoryToCatalogEntry> categories) {
        if (categories == null || categories.size() < 1)
            return false;
        for (CategoryToCatalogEntry cat : categories) {
            if (cat.getCategory().getKey() == voCategory.getKey()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deassignAllCategories(CatalogEntry catalogEntry) {
        if (catalogEntry.getCategoryToCatalogEntry() != null
                && catalogEntry.getCategoryToCatalogEntry().size() > 0) {
            for (CategoryToCatalogEntry cce : catalogEntry
                    .getCategoryToCatalogEntry()) {
                dm.remove(cce);
            }
        }
        catalogEntry.setCategoryToCatalogEntry(null);
    }

    @Override
    public void verifyCategoriesUpdated(List<VOCategory> categories)
            throws ConcurrentModificationException, ObjectNotFoundException {
        String locale = dm.getCurrentUser().getLocale();
        final LocalizerFacade facade = new LocalizerFacade(localizer, locale);
        for (VOCategory voCategory : categories) {
            Category category = dm.getReference(Category.class,
                    voCategory.getKey());
            CategoryAssembler.verifyCategoryUpdated(category, voCategory,
                    facade);
        }
    }

}
