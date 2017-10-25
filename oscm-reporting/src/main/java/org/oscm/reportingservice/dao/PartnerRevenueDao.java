/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * @author kulle
 * 
 */
public class PartnerRevenueDao {

    public class ReportData {
        private String resultXml;
        private String organizationId;
        private String name;
        private String address;
        private String countryIsoCode;
        private String resulttype;
        private long periodStart;
        private long periodEnd;

        public String getResultXml() {
            return resultXml;
        }

        public void setResultXml(String resultXml) {
            this.resultXml = resultXml;
        }

        public String getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(String organizationId) {
            this.organizationId = organizationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCountryIsoCode() {
            return countryIsoCode;
        }

        public void setCountryIsoCode(String countryIsoCode) {
            this.countryIsoCode = countryIsoCode;
        }

        public long getPeriodStart() {
            return periodStart;
        }

        public void setPeriodStart(long periodStart) {
            this.periodStart = periodStart;
        }

        public long getPeriodEnd() {
            return periodEnd;
        }

        public void setPeriodEnd(long periodEnd) {
            this.periodEnd = periodEnd;
        }

        public String getResulttype() {
            return resulttype;
        }

        public void setResulttype(String resulttype) {
            this.resulttype = resulttype;
        }
    }

    private final DataService ds;
    private Locale defaultLocale = Locale.ENGLISH;
    private final List<ReportData> reportData = new ArrayList<ReportData>();
    static final String QUERY_SINGLE_PARTNER = "SELECT o.organizationid, o.name, o.address, bsr.resultxml, bsr.periodstarttime, bsr.periodendtime, bsr.resulttype, sc.countryisocode FROM organization o, billingsharesresult bsr, supportedcountry sc WHERE bsr.organizationtkey=? AND o.tkey=bsr.organizationtkey AND o.domicilecountry_tkey=sc.tkey AND bsr.resulttype=? AND bsr.periodstarttime=? AND bsr.periodendtime=?";
    static final String QUERY_PARTNER = "SELECT o.organizationid, o.name, o.address, bsr.resultxml, bsr.periodstarttime, bsr.periodendtime, bsr.resulttype , sc.countryisocode FROM organization o, billingsharesresult bsr, supportedcountry sc WHERE o.tkey=bsr.organizationtkey AND o.domicilecountry_tkey=sc.tkey AND (bsr.resulttype='BROKER' OR bsr.resulttype='RESELLER') AND bsr.periodstarttime=? AND bsr.periodendtime=?";
    static final String QUERY_SUPPLIERS = "SELECT o.organizationid, o.name, o.address, bsr.resultxml, bsr.periodstarttime, bsr.periodendtime, bsr.resulttype , sc.countryisocode FROM organization o, billingsharesresult bsr, supportedcountry sc WHERE o.tkey=bsr.organizationtkey AND (bsr.resulttype='SUPPLIER') AND bsr.periodstarttime=? AND bsr.periodendtime=? AND sc.tkey=o.domicilecountry_tkey ";
    static final String QUERY_TEMPLATE_SERVICE_IDS = "SELECT DISTINCT s.objkey, s.productid FROM ProductHistory s, (SELECT ph.objkey, MAX(ph.objversion) AS objversion FROM ProductHistory ph, Organization o WHERE moddate < ? AND ph.status <> ? AND ph.type = ? AND o.tkey = ph.vendorobjkey GROUP BY ph.objkey) AS flt WHERE s.objkey = flt.objkey AND s.objversion = flt.objversion";
    static final String QUERY_TEMPLATE_SERVICE_IDS_FOR_SUPPLIER = "SELECT DISTINCT s.objkey, s.productid FROM ProductHistory s, (SELECT ph.objkey, MAX(ph.objversion) AS objversion FROM ProductHistory ph, Organization o WHERE moddate < ? AND ph.status <> ? AND ph.type = ? AND o.organizationid = ? AND o.tkey = ph.vendorobjkey GROUP BY ph.objkey) AS flt WHERE s.objkey = flt.objkey AND s.objversion = flt.objversion";
    static final String QUERY_MARKETPLACE_NAMES = "SELECT mph.marketplaceid, lr.locale, lr.value FROM localizedresource lr, marketplacehistory mph WHERE mph.moddate < ? AND mph.objkey = lr.objectkey AND lr.objecttype='MARKETPLACE_NAME' GROUP BY mph.marketplaceid, lr.locale, lr.value";
    static final String QUERY_SERVICE_NAMES = "SELECT ph.productid, lr.locale, lr.value FROM localizedresource lr, producthistory ph WHERE (ph.type = 'TEMPLATE' OR ph.type = 'PARTNER_TEMPLATE') AND ph.moddate < ? AND ph.objkey = lr.objectkey AND lr.objecttype='PRODUCT_MARKETING_NAME' GROUP BY ph.productid, lr.locale, lr.value";

    public PartnerRevenueDao(DataService ds) {
        this.ds = ds;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void executeSinglePartnerQuery(long periodStart, long periodEnd,
            long vendorKey, String vendorType) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SINGLE_PARTNER);
        sqlQuery.setLong(1, vendorKey);
        sqlQuery.setString(2, vendorType);
        sqlQuery.setLong(3, periodStart);
        sqlQuery.setLong(4, periodEnd);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        convertSinglePartnerQueryResult(dataSet);
    }

    private void convertSinglePartnerQueryResult(DataSet rs) {
        if (rs.next()) {
            ReportData rd = new ReportData();
            rd = new ReportData();
            rd.setAddress(rs.getString("address"));
            rd.setName(rs.getString("name"));
            rd.setOrganizationId(rs.getString("organizationid"));
            rd.setPeriodEnd(rs.getLong("periodendtime"));
            rd.setPeriodStart(rs.getLong("periodstarttime"));
            rd.setResultXml(rs.getString("resultxml"));
            rd.setResulttype(rs.getString("resulttype").toLowerCase());
            rd.setCountryIsoCode(rs.getString("countryisocode"));
            reportData.add(rd);
        }
    }

    public List<ReportData> getReportData() {
        return reportData;
    }

    public void executePartnerQuery(long periodStart, long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_PARTNER);
        sqlQuery.setLong(1, periodStart);
        sqlQuery.setLong(2, periodEnd);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        convertPartnerQueryResult(dataSet);
    }

    public void executeSupplierQuery(long periodStart, long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SUPPLIERS);
        sqlQuery.setLong(1, periodStart);
        sqlQuery.setLong(2, periodEnd);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        convertPartnerQueryResult(dataSet);
    }

    private void convertPartnerQueryResult(DataSet rs) {
        while (rs.next()) {
            ReportData rd = new ReportData();
            rd = new ReportData();
            rd.setAddress(rs.getString("address"));
            rd.setName(rs.getString("name"));
            rd.setOrganizationId(rs.getString("organizationid"));
            rd.setPeriodEnd(rs.getLong("periodendtime"));
            rd.setPeriodStart(rs.getLong("periodstarttime"));
            rd.setResultXml(rs.getString("resultxml"));
            rd.setResulttype(rs.getString("resulttype").toLowerCase());
            rd.setCountryIsoCode(rs.getString("countryisocode"));
            reportData.add(rd);
        }
    }

    public Map<String, String> readTemplateServiceIds(long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_TEMPLATE_SERVICE_IDS);
        sqlQuery.setDate(1, new Date(periodEnd));
        sqlQuery.setString(2, ServiceStatus.DELETED.name());
        sqlQuery.setString(3, ServiceType.TEMPLATE.name());
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        HashMap<String, String> serviceIdMap = new HashMap<String, String>();
        while (dataSet.next()) {
            serviceIdMap.put(String.valueOf(dataSet.getLong("objkey")),
                    dataSet.getString("productid"));
        }
        return serviceIdMap;
    }

    public Map<String, String> readTemplateServiceIdsForSupplier(
            String organizationId, long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(
                QUERY_TEMPLATE_SERVICE_IDS_FOR_SUPPLIER);
        sqlQuery.setDate(1, new Date(periodEnd));
        sqlQuery.setString(2, ServiceStatus.DELETED.name());
        sqlQuery.setString(3, ServiceType.TEMPLATE.name());
        sqlQuery.setString(4, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        HashMap<String, String> serviceIdMap = new HashMap<String, String>();
        while (dataSet.next()) {
            serviceIdMap.put(String.valueOf(dataSet.getLong("objkey")),
                    dataSet.getString("productid"));
        }
        return serviceIdMap;
    }

    public Map<String, String> readMarketplaceNames(Locale locale,
            long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_MARKETPLACE_NAMES);
        sqlQuery.setDate(1, new Date(periodEnd));
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        HashMap<String, String> marketplaceIdMap = new HashMap<String, String>();
        while (dataSet.next()) {
            Locale loc = new Locale(dataSet.getString("locale"));
            String mpId = dataSet.getString("marketplaceid");
            String mpName = dataSet.getString("value");
            if (null != locale && locale.equals(loc)) {
                marketplaceIdMap.put(mpId, mpName);
            } else if (defaultLocale.equals(loc)
                    && null == marketplaceIdMap.get(mpId)) {
                marketplaceIdMap.put(mpId, mpName);
            }
        }
        return marketplaceIdMap;
    }

    public Map<String, String> readServiceNames(Locale locale, long periodEnd) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SERVICE_NAMES);
        sqlQuery.setDate(1, new Date(periodEnd));
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        HashMap<String, String> marketplaceIdMap = new HashMap<String, String>();
        while (dataSet.next()) {
            Locale loc = new Locale(dataSet.getString("locale"));
            String servId = dataSet.getString("productid");
            String servName = dataSet.getString("value");
            if (null != locale && locale.equals(loc)) {
                marketplaceIdMap.put(servId, servName);
            } else if (defaultLocale.equals(loc)
                    && null == marketplaceIdMap.get(servId)) {
                marketplaceIdMap.put(servId, servName);
            }
        }
        return marketplaceIdMap;
    }

}
