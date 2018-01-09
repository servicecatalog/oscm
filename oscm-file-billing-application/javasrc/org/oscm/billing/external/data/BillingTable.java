/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * A billing table
 *
 */
public abstract class BillingTable<T extends BillingTableEntry> {

    private File tableFile;

    public BillingTable(String fileName) {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        URL resource = classLoader.getResource(fileName);
        tableFile = new File(resource.getFile());
    }

    /**
     * Open the price model table file and read the entries, that are accepted
     * by the given filter
     * 
     * @param filter
     *            a price model table filter
     * @return a list of price model table entries accepted by the filter
     */
    public List<T> filter(BillingTableFilter<T> filter) {

        List<T> tableEntries = new ArrayList<>();
        LineIterator fileIter = null;

        try {
            fileIter = FileUtils.lineIterator(tableFile,
                    StandardCharsets.UTF_8.toString());
            if (fileIter.hasNext()) {
                // Skip headline of table
                fileIter.nextLine();
            }

            while (fileIter.hasNext()) {
                T entry = filter.accept(fileIter.nextLine());
                if (entry != null) {
                    tableEntries.add(entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            tableEntries.clear();
        } finally {
            if (fileIter != null) {
                fileIter.close();
            }
        }

        return tableEntries;
    }

}
