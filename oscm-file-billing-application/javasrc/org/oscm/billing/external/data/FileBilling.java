/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The file billing application logic
 *
 */
public class FileBilling {

    private final String DEFAULT_LOCALE = "en";
    private final String SUCCESS_MSG = "Billing Application";

    public String getSuccessfulConnectionMsg() {
        return SUCCESS_MSG;
    }

    /**
     * Get a specific price model file
     * 
     * @param fileName
     *            the file name
     * @return the price model file
     */
    public File getPriceModelFile(String fileName) {

        if (fileName != null && !fileName.isEmpty()) {
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            URL resource = classLoader.getResource("pm/" + fileName);
            if (resource != null) {
                File pmFile = new File(resource.getFile());
                if (pmFile.exists()) {
                    return pmFile;
                }
            }
        }

        return null;
    }

    /**
     * Get a price model specified by the given context
     * 
     * @param contextKeys
     *            the keys of the context parameters
     * @param contextValues
     *            the values of the context parameters
     * @param locales
     *            a list of locales
     * @return the price model data
     */
    public List<String> getPriceModel(List<String> contextKeys,
            List<String> contextValues, List<String> locales) {

        PriceModelContext pmContext = PriceModelContext.create(contextKeys,
                contextValues);
        if (pmContext.isServicePriceModelContext()) {

            ServicePriceModelTable pmTable = new ServicePriceModelTable();
            List<ServicePriceModelTableEntry> tableEntries = pmTable
                    .filter(new ServicePriceModelTableFilter(pmContext));

            List<ServicePriceModelTableEntry> entriesForLocales = new ArrayList<>();
            getEntriesForLocales(tableEntries, locales, entriesForLocales);

            return generateServicePriceModelData(entriesForLocales);
        } else if (pmContext.isSubscriptionPriceModelContext()) {

            SubscriptionPriceModelTable pmTable = new SubscriptionPriceModelTable();
            List<SubscriptionPriceModelTableEntry> tableEntries = pmTable
                    .filter(new SubscriptionPriceModelTableFilter(pmContext));

            List<SubscriptionPriceModelTableEntry> entriesForLocales = new ArrayList<>();
            getEntriesForLocales(tableEntries, locales, entriesForLocales);

            return generateSubscriptionPriceModelData(entriesForLocales);
        } else {
            return null;
        }
    }

    /**
     * Search for all table entries that match the given locales. If no entry is
     * found, search an entry for the default locale.
     * 
     * @param tableEntries
     *            a list of billing table entries
     * @param locales
     *            a list of locales
     * @param resultList
     *            the result list with the found entries
     */
    private void getEntriesForLocales(
            List<? extends BillingTableEntry> tableEntries,
            List<String> locales, List<? extends BillingTableEntry> resultList) {

        if (locales != null) {
            for (String locale : locales) {
                addEntryForLocale(tableEntries, locale, resultList);
            }
        }

        if (resultList.size() == 0) {
            addEntryForLocale(tableEntries, DEFAULT_LOCALE, resultList);
        }
    }

    /**
     * Search a price model table entry for the specified locale and add the
     * entry to the destination list
     * 
     * @param tableEntries
     *            a list of billing table entries
     * @param locale
     *            a locale
     * @param resultList
     *            the result list
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addEntryForLocale(
            List<? extends BillingTableEntry> tableEntries, String locale,
            List resultList) {

        if (locale != null) {
            for (BillingTableEntry entry : tableEntries) {
                if (entry.localeEquals(locale)) {
                    resultList.add(entry);
                    break;
                }
            }
        }
    }

    /**
     * Generate a list of string's, which contains the price model data from the
     * service price model table
     * 
     * @param tableEntries
     *            a list of service price model table entries for different
     *            locales
     * @return the service price model data
     */
    private List<String> generateServicePriceModelData(
            List<ServicePriceModelTableEntry> tableEntries) {

        List<String> priceModelData = new ArrayList<String>();
        if (tableEntries.size() == 0) {
            return priceModelData;
        }

        String pmUuid = "";
        for (ServicePriceModelTableEntry entry : tableEntries) {
            String uuid = entry.getPriceModelUUID();
            if (pmUuid.isEmpty() && !uuid.isEmpty()) {
                pmUuid = uuid;
                priceModelData.add(pmUuid);
            }

            String locale = entry.getLocale();
            String fileType = entry.getFileType();
            String fileName = entry.getFileName();
            String tag = entry.getTag();

            if (!uuid.isEmpty() && uuid.equals(pmUuid) && !locale.isEmpty()
                    && !fileType.isEmpty() && !fileName.isEmpty()
                    && !tag.isEmpty()) {
                priceModelData.add(locale);
                priceModelData.add(fileType);
                priceModelData.add(fileName);
                priceModelData.add(tag);
            }
        }

        return priceModelData;
    }

    /**
     * Generate a list of string's, which contains the price model data from the
     * subscription price model table
     * 
     * @param tableEntries
     *            a list of service price model table entries for different
     *            locales
     * @return the service price model data
     */
    private List<String> generateSubscriptionPriceModelData(
            List<SubscriptionPriceModelTableEntry> tableEntries) {

        List<String> priceModelData = new ArrayList<String>();
        if (tableEntries.size() == 0) {
            return priceModelData;
        }

        String pmUuid = "";
        for (SubscriptionPriceModelTableEntry entry : tableEntries) {
            String uuid = entry.getPriceModelUUID();
            if (pmUuid.isEmpty() && !uuid.isEmpty()) {
                pmUuid = uuid;
                priceModelData.add(pmUuid);
            }

            String locale = entry.getLocale();
            String fileType = entry.getFileType();
            String fileName = entry.getFileName();

            if (!uuid.isEmpty() && uuid.equals(pmUuid) && !locale.isEmpty()
                    && !fileType.isEmpty() && !fileName.isEmpty()) {
                priceModelData.add(locale);
                priceModelData.add(fileType);
                priceModelData.add(fileName);
                priceModelData.add("");
            }
        }

        return priceModelData;
    }

}
