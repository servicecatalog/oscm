/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenueDetails;
import org.oscm.reportingservice.dao.PlatformRevenueDao;
import org.oscm.reportingservice.dao.PlatformRevenueDao.RowData;

/**
 * The class <code>PlatformRevenueBuilder</code> prepares the data for the
 * parses the platform revenue report. The SQL data is parsed and then
 * aggregated and finally sorted.
 * 
 * @author cheld
 * 
 */
public class PlatformRevenueBuilder {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PlatformRevenueBuilder.class);

    private final PlatformRevenueDao sqlResult;

    private final PriceConverter priceConverter;

    private final RDOPlatformRevenue result = new RDOPlatformRevenue();

    public PlatformRevenueBuilder(PlatformRevenueDao sqlResult,
            Locale userLocale) {
        this.sqlResult = sqlResult;
        this.priceConverter = new PriceConverter(userLocale);
    }

    private interface KeyBuilder {
        String getKey(RowData row);
    }

    /**
     * Prepares the data required for the platform revenue report.
     * 
     * @param locale
     * 
     * @return RDOPlatformRevenue
     */
    public RDOPlatformRevenue build() {
        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug("building platform revenue with sql query result of "
                    + sqlResult.getRowData().size() + " entries");
        }
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        result.setCreationTime(DateConverter.convertLongToDateTimeFormat(
                (new Date()).getTime(), TimeZone.getDefault(),
                DateConverter.DTP_WITHOUT_MILLIS));
        result.setEntryNr(0);
        result.setNoMarketplaces(getNoGlobalMarketplaces());
        result.setNoSuppliers(getNoSuppliers());
        result.setSummaryByMarketplace(getSummaryByMarketplace());
        result.setSummaryBySupplier(getSummaryBySupplier());
        result.setSummaryByCountry(getSummaryByCountry());
        result.setSupplierDetails(getSupplierDetails());
        result.setTotalByCurrency(getTotalByCurrency());
        return result;
    }

    private String getNoGlobalMarketplaces() {
        final Set<String> marketplaces = new HashSet<String>();
        for (RowData row : sqlResult.getRowData()) {
            marketplaces.add(row.getMarketplace());
        }
        return "" + marketplaces.size();
    }

    private String getNoSuppliers() {
        final Set<String> supplierIDs = new HashSet<String>();
        for (RowData row : sqlResult.getRowData()) {
            supplierIDs.add(row.getSupplierID());
        }
        return "" + supplierIDs.size();
    }

    private List<RDOPlatformRevenueDetails> getSummaryByMarketplace() {
        final List<RDOPlatformRevenueDetails> data = sumUp(new KeyBuilder() {
            @Override
            public String getKey(RowData row) {
                return (row.getMarketplace() == null
                        || row.getMarketplace().trim().length() == 0 ? row.getSupplierName()
                        + row.getSupplierID()
                        : row.getMarketplace())
                        + row.getCurrency();
            }
        }, "summary by marketplace");
        return data;
    }

    private List<RDOPlatformRevenueDetails> getSummaryBySupplier() {
        final List<RDOPlatformRevenueDetails> data = sumUp(new KeyBuilder() {
            @Override
            public String getKey(RowData row) {
                return row.getSupplierName() + row.getSupplierID()
                        + row.getCurrency();
            }
        }, "summary by supplier");
        return data;
    }

    private List<RDOPlatformRevenueDetails> getSummaryByCountry() {
        final List<RDOPlatformRevenueDetails> data = sumUp(new KeyBuilder() {
            @Override
            public String getKey(RowData row) {
                return row.getSupplierCountry() + row.getCurrency();
            }
        }, "total by country");
        return data;
    }

    private List<RDOPlatformRevenueDetails> getSupplierDetails() {
        final List<RDOPlatformRevenueDetails> data = sumUp(new KeyBuilder() {
            @Override
            public String getKey(RowData row) {
                return row.getSupplierName() + row.getSupplierID()
                        + row.getBillingKey() + row.getMarketplace()
                        + row.getCurrency();
            }
        }, "supplier details");
        return data;
    }

    private List<RDOPlatformRevenueDetails> getTotalByCurrency() {
        final List<RDOPlatformRevenueDetails> data = sumUp(new KeyBuilder() {
            @Override
            public String getKey(RowData row) {
                return row.getCurrency();
            }
        }, "total by currency");
        return data;
    }

    private List<RDOPlatformRevenueDetails> sumUp(KeyBuilder keyBuilder,
            String logMsg) {
        final Map<String, RDOPlatformRevenueDetails> data = new HashMap<String, RDOPlatformRevenueDetails>();
        RDOPlatformRevenueDetails details;
        int i = 0;
        for (RowData row : sqlResult.getRowData()) {
            final String key = keyBuilder.getKey(row);
            if (data.containsKey(key)) {
                details = data.get(key);
                details.setAmount(new BigDecimal(details.getAmount()).add(
                        row.getAmount()).toString());
            } else {
                details = new RDOPlatformRevenueDetails();
                details.setAmount(row.getAmount().toString());
                details.setCurrency(row.getCurrency());
                details.setEntryNr(i++);
                details.setMarketplace(row.getMarketplace());
                details.setName(row.getSupplierName() + " ("
                        + row.getSupplierID() + ')');
                details.setBillingKey(row.getBillingKey());
                details.setCountry(row.getSupplierCountry());
                details.setParentEntryNr(0);
                data.put(key, details);
            }
        }
        final Iterator<String> it = data.keySet().iterator();
        RDOPlatformRevenueDetails row;
        while (it.hasNext()) {
            row = data.get(it.next());
            row.setAmount(priceConverter.getValueToDisplay(
                    new BigDecimal(row.getAmount()), true));
        }
        if (logger.isDebugLoggingEnabled()) {
            logger.logDebug(logMsg + " has " + data.size() + " entries");
        }
        return sort(data);
    }

    private List<RDOPlatformRevenueDetails> sort(
            Map<String, RDOPlatformRevenueDetails> data) {
        final Object[] keys = data.keySet().toArray();
        Arrays.sort(keys);
        final List<RDOPlatformRevenueDetails> list = new ArrayList<RDOPlatformRevenueDetails>();
        for (int i = 0; i < keys.length; i++) {
            list.add(data.get(keys[i]));
        }
        return list;
    }
}
