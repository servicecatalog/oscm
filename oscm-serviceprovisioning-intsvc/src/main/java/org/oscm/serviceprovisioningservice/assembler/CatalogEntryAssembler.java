/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                     
 *                                                                              
 *  Creation Date: Jan 27, 2011                                                      
 *                                                                              
 *  Completion Time: Feb 1, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.marketplace.assembler.CategoryAssembler;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.CatalogEntryRemovedException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;

/**
 * Assembler for catalog entries.
 * 
 * @author groch
 * 
 */
public class CatalogEntryAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(CatalogEntryAssembler.class);

    /**
     * Converts the catalog entry domain object to the corresponding catalog
     * entry value object.
     * 
     * @param catalogEntry
     *            The domain object to be converted
     * @param facade
     *            The localizer facade
     * @return The catalog entry value object.
     */
    public static VOCatalogEntry toVOCatalogEntry(CatalogEntry catalogEntry,
            LocalizerFacade facade) {
        return toVOCatalogEntry(catalogEntry, facade,
                PerformanceHint.ALL_FIELDS);
    }

    public static VOCatalogEntry toVOCatalogEntry(CatalogEntry catalogEntry,
            LocalizerFacade facade, PerformanceHint scope) {
        if (catalogEntry == null) {
            return null;
        }
        VOCatalogEntry entry = new VOCatalogEntry();
        updateVoCatalogEntry(catalogEntry, facade, entry, scope);
        updateValueObject(entry, catalogEntry);
        entry.setCategories(mapToVOCategories(catalogEntry, facade));
        return entry;

    }

    private static List<VOCategory> mapToVOCategories(
            CatalogEntry catalogEntry, LocalizerFacade facade) {
        List<VOCategory> resultList = new ArrayList<VOCategory>();
        if (catalogEntry.getCategoryToCatalogEntry() != null) {
            for (CategoryToCatalogEntry cce : catalogEntry
                    .getCategoryToCatalogEntry()) {
                VOCategory cat = CategoryAssembler.toVOCategory(
                        cce.getCategory(), facade);
                resultList.add(cat);
            }
        }
        return resultList;
    }

    static void updateVoCatalogEntry(CatalogEntry catalogEntry,
            LocalizerFacade facade, VOCatalogEntry entry, PerformanceHint scope) {

        entry.setAnonymousVisible(catalogEntry.isAnonymousVisible());
        entry.setVisibleInCatalog(catalogEntry.isVisibleInCatalog());
        entry.setMarketplace(MarketplaceAssembler.toVOMarketplace(
                catalogEntry.getMarketplace(), facade));

        switch (scope) {
        case ONLY_IDENTIFYING_FIELDS:
        case ONLY_FIELDS_FOR_LISTINGS:
            entry.setService(ProductAssembler.toVOProduct(
                    catalogEntry.getProduct(), facade,
                    PerformanceHint.ONLY_IDENTIFYING_FIELDS));
            break;
        default:
            entry.setService(ProductAssembler.toVOProduct(
                    catalogEntry.getProduct(), facade,
                    PerformanceHint.ALL_FIELDS));
            break;
        }
    }

    /**
     * Updates an already existing catalog entry domain object with the settings
     * of a given value object.
     * 
     * @param domEntry
     *            The domain object to be updated.
     * @param voCatalogEntry
     *            The value object to get the recent data from.
     * @return The updated catalog entry domain object.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions do not match.
     */
    public static CatalogEntry updateCatalogEntry(CatalogEntry domEntry,
            VOCatalogEntry voCatalogEntry)
            throws ConcurrentModificationException {
        if (voCatalogEntry == null || domEntry == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Parameters must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }
        verifyVersionAndKey(domEntry, voCatalogEntry);
        copyAttributes(domEntry, voCatalogEntry);
        return domEntry;
    }

    /**
     * Synchronizes a list of given value objects with corresponding domain
     * representations. For it, every given value object is mapped to the
     * corresponding existing domain object (if any) whose attributes are then
     * updated accordingly. For the latter,
     * {@link #updateCatalogEntry(CatalogEntry, VOCatalogEntry)} is used.
     * 
     * @param catalogEntries
     *            The set of value objects to get the updated data from.
     * @param supplierCatalogEntries
     *            The list of all potential domain objects applicable for
     *            update.
     * @return The complete list of now up-to-date catalog entry domain objects.
     * @see #updateCatalogEntry(CatalogEntry, VOCatalogEntry)
     * @throws ValidationException
     *             Thrown if after having completed the update of all domain
     *             objects, two catalog entries have the same position within
     *             the catalog.
     * @throws CatalogEntryRemovedException
     *             Thrown if for any of the given value objects, no
     *             corresponding domain object can be found.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions do not match.
     */
    public static List<CatalogEntry> consolidateCatalogEntries(
            Collection<VOCatalogEntry> catalogEntries,
            List<CatalogEntry> supplierCatalogEntries)
            throws ValidationException, ConcurrentModificationException,
            CatalogEntryRemovedException {
        if (catalogEntries == null) {
            return supplierCatalogEntries;
        }
        HashMap<Long, CatalogEntry> entryMap = new HashMap<Long, CatalogEntry>();
        for (CatalogEntry entry : supplierCatalogEntries) {
            entryMap.put(Long.valueOf(entry.getKey()), entry);
        }
        Iterator<VOCatalogEntry> iterator = catalogEntries.iterator();
        while (iterator.hasNext()) {
            VOCatalogEntry voEntry = iterator.next();
            CatalogEntry removed = entryMap.remove(Long.valueOf(voEntry
                    .getKey()));
            if (removed == null) {
                CatalogEntryRemovedException e = new CatalogEntryRemovedException(
                        "Given VO catalog entry does not correspond to any existing domain catalog entry of this supplier or incoming set contains at least two items with identical key '"
                                + voEntry.getKey() + "'.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_CATALOG_ENTRY_REMOVED,
                        String.valueOf(voEntry.getKey()));
                throw e;
            }
            updateCatalogEntry(removed, voEntry);
        }

        return supplierCatalogEntries;
    }

    /**
     * Helper method setting certain attributes in a catalog entry domain object
     * corresponding to the attributes of a given VO object.
     * 
     * @param catalogEntry
     *            The domain object to update. Must not be <code>null</code>.
     * @param template
     *            The given VO object which partly serves as a pattern. Must not
     *            be <code>null</code>.
     */
    private static void copyAttributes(CatalogEntry catalogEntry,
            final VOCatalogEntry template) {
        catalogEntry.setAnonymousVisible(template.isAnonymousVisible());
        catalogEntry.setVisibleInCatalog(template.isVisibleInCatalog());
    }

    /**
     * Creates a detached catalog entry domain object holding the information of
     * the given VO.
     * 
     * @param voCatalogEntry
     *            The given value object to get the data form. Must not be
     *            <code>null</code>.
     * @return The new domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    public static CatalogEntry toCatalogEntry(VOCatalogEntry voCatalogEntry)
            throws ValidationException {
        if (voCatalogEntry == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Parameters must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }

        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setKey(voCatalogEntry.getKey());
        copyAttributes(catalogEntry, voCatalogEntry);

        if (voCatalogEntry.getMarketplace() != null) {
            catalogEntry.setMarketplace(MarketplaceAssembler
                    .toMarketplace(voCatalogEntry.getMarketplace()));
        }

        return catalogEntry;
    }

}
