/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sept 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;

/**
 * @author kulle
 */
public class BillingDao {

    public class ReportBillingData {
        private long date;
        private String supplierName;
        private String supplierAddress;
        private String billingResult;
        private Long userGroup;

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getSupplierAddress() {
            return supplierAddress;
        }

        public void setSupplierAddress(String supplierAddress) {
            this.supplierAddress = supplierAddress;
        }

        public String getBillingResult() {
            return billingResult;
        }

        public void setBillingResult(String billingResult) {
            this.billingResult = billingResult;
        }

        public Long getUserGroup() {
            return userGroup;
        }

        public void setUserGroup(Long userGroup) {
            this.userGroup = userGroup;
        }
    }

    public class ReportData {
        private String name;
        private String address;
        private String productId;

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

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

    }

    private final DataService ds;
    private ReportData reportData;

    static final String QUERY_ORGANIZATION_DETAILS = "SELECT org.name, org.address FROM Organization org WHERE org.tkey = ?";
    static final String QUERY_SERVICE_NAME = "SELECT subq.productid, lr.value FROM (SELECT pr.productid, pr.templateobjkey FROM producthistory pr, pricemodelhistory pm WHERE pm.objkey=? AND pr.objkey=pm.productobjkey AND pm.objversion=(SELECT max(ipm.objversion) FROM pricemodelhistory ipm WHERE ipm.objkey=pm.objkey) AND pr.objversion=(SELECT max(ipr.objversion) FROM producthistory ipr WHERE ipr.objkey=pr.objkey)) as subq LEFT JOIN localizedresource lr ON subq.templateobjkey=lr.objectkey AND lr.objecttype='PRODUCT_MARKETING_NAME'";
    static final String QUERY_SUPPLIER_BILLING = "SELECT br.creationtime as billingdate, br.periodstarttime, br.periodendtime, cust.organizationid, cust.name, cust.email, br.resultxml, br.tkey as billingkey FROM organization sup, organization cust, billingresult br WHERE br.chargingorgkey = sup.tkey AND cust.tkey = br.organizationtkey AND sup.organizationid=? ORDER BY cust.organizationid";
    static final String QUERY_SUPPLIER_BILLING_PRIVACY = "SELECT br.creationtime as billingdate, br.periodstarttime, br.periodendtime, cust.organizationid, br.resultxml, br.tkey as billingkey FROM organization sup, organization cust, billingresult br WHERE br.chargingorgkey = sup.tkey AND cust.tkey = br.organizationtkey AND sup.organizationid=? ORDER BY cust.organizationid";
    static final String QUERY_BILLING_DETAILS_SUB = "SELECT DISTINCT br.creationtime, br.periodstarttime, br.periodendtime, br.resultxml, sup.name, sup.address, br.usergroup_tkey FROM billingresult br, organization sup, subscription sub WHERE br.TKEY = ? AND br.chargingorgkey = sup.tkey AND br.subscriptionkey = sub.tkey AND (br.organizationtkey = ? OR br.chargingorgkey = ?)";
    static final String QUERY_BILLING_DETAILS = "SELECT DISTINCT br.creationtime, br.periodstarttime, br.periodendtime, br.resultxml, sup.name, sup.address, br.usergroup_tkey FROM billingresult br, organization sup, subscriptionhistory subhist WHERE br.TKEY = ? AND br.chargingorgkey = sup.tkey AND br.subscriptionkey = subhist.objkey AND (br.organizationtkey = ? OR br.chargingorgkey = ?)";
    static final String QUERY_BILLING_DETAILS_PRIVACY = "SELECT br.creationtime, br.resultxml, sup.name, sup.address  FROM billingresult br, organization sup WHERE br.tkey = ? AND br.chargingorgkey = sup.tkey";

    public BillingDao(DataService ds) {
        this.ds = ds;
    }

    public ReportData getReportData() {
        return reportData;
    }

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }

    public List<ReportBillingData> retrieveBillingDetails(
            long billingResultTkey, long organizationKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_BILLING_DETAILS);
        sqlQuery.setLong(1, billingResultTkey);
        sqlQuery.setLong(2, organizationKey);
        sqlQuery.setLong(3, organizationKey);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        return convertToReportBillingData(rs);
    }

    public List<ReportBillingData> retrieveBillingDetails(
            long billingResultTkey, long organizationKey, List<Long> unitKeys) {

        if (unitKeys == null || unitKeys.isEmpty()) {
            return new ArrayList<BillingDao.ReportBillingData>();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unitKeys.size(); i++) {
            builder.append("?,");
        }

        String units = builder.deleteCharAt(builder.length() - 1).toString();

        String query = QUERY_BILLING_DETAILS_SUB
                + " AND ((sub.usergroup_tkey IN (" + units
                + ")) OR (br.usergroup_tkey IN (" + units + ")))";
        SqlQuery sqlQuery = new SqlQuery(query);
        sqlQuery.setLong(1, billingResultTkey);
        sqlQuery.setLong(2, organizationKey);
        sqlQuery.setLong(3, organizationKey);
        int index = 4;
        for (Long key : unitKeys) {
            sqlQuery.setLong(index++, key.longValue());
        }
        for (Long key : unitKeys) {
            sqlQuery.setLong(index++, key.longValue());
        }
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        return convertToReportBillingData(rs);
    }

    public List<ReportBillingData> retrieveBillingDetailsByKey(
            long billingResultKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_BILLING_DETAILS_PRIVACY);
        sqlQuery.setLong(1, billingResultKey);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        return convertToReportBillingDataOfASupplier(rs);
    }

    private List<ReportBillingData> convertToReportBillingDataOfASupplier(
            DataSet rs) {
        List<ReportBillingData> result = new ArrayList<>();
        while (rs.next()) {
            ReportBillingData data = new ReportBillingData();
            data.setDate(rs.getLong(1));
            data.setBillingResult(rs.getString(2));
            data.setSupplierName(rs.getString(3));
            data.setSupplierAddress(rs.getString(4));
            result.add(data);
        }
        return result;
    }

    private List<ReportBillingData> convertToReportBillingData(DataSet rs) {
        List<ReportBillingData> result = new ArrayList<>();
        while (rs.next()) {
            ReportBillingData data = new ReportBillingData();
            data.setDate(rs.getLong(1));
            data.setSupplierName(rs.getString(5));
            data.setSupplierAddress(rs.getString(6));
            data.setBillingResult(rs.getString(4));
            Object userGroup = rs.getObject(7);
            if (userGroup instanceof Long) {
                data.setUserGroup((Long) userGroup);
            }
            result.add(data);
        }
        return result;
    }

    public List<ReportResultData> retrieveSupplierBillingData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SUPPLIER_BILLING);
        sqlQuery.setString(1, organizationId);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(rs);
    }

    public List<ReportResultData> retrieveSupplierBillingBySupplierId(
            String supplierId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SUPPLIER_BILLING_PRIVACY);
        sqlQuery.setString(1, supplierId);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(rs);
    }

    private List<ReportResultData> convertToReportResultData(DataSet rs) {
        List<ReportResultData> result = new ArrayList<>();
        while (rs.next()) {
            ReportResultData rrd = new ReportResultData();
            rrd.setColumnCount(rs.getMetaData().getColumnCount());
            for (int column = 1; column <= rrd.getColumnCount(); column++) {
                rrd.getColumnName().add(rs.getMetaData().getColumnName(column));
                rrd.getColumnType()
                        .add(Integer.valueOf(rs.getMetaData().getColumnType(
                                column)));
                rrd.getColumnValue().add(rs.getObject(column));
            }
            result.add(rrd);
        }
        return result;
    }

    public void retrieveOrganizationDetails(long chargingOrgKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_ORGANIZATION_DETAILS);
        sqlQuery.setLong(1, chargingOrgKey);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        convertOrganizationDetailsToReportData(rs);
    }

    private void convertOrganizationDetailsToReportData(DataSet rs) {
        if (rs.next()) {
            reportData = new ReportData();
            reportData.setName(rs.getString(1));
            reportData.setAddress(rs.getString(2));
        }
    }

    public void executeServiceNameQuery(long priceModelKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SERVICE_NAME);
        sqlQuery.setLong(1, priceModelKey);
        DataSet rs = ds.executeQueryForRawData(sqlQuery);
        convertServiceNameToReportData(rs);
    }

    private void convertServiceNameToReportData(DataSet rs) {
        if (rs.next()) {
            reportData = new ReportData();
            reportData.setProductId(rs.getString(1));
            reportData.setName(rs.getString(2));
        }
    }

}
