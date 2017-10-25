/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author kulle
 * 
 */
public class SubscriptionDao {

    static final Log4jLogger logger = LoggerFactory
            .getLogger(SubscriptionDao.class);
    private final DataService ds;

    static final String QUERY_SUBSCRIPTION_REPORT = "SELECT DISTINCT product.productid, platformuser.userid, platformuser.lastname,  platformuser.firstname, usagelicense.assignmentdate, subscription.subscriptionid FROM product JOIN subscription ON product.tkey=subscription.product_tkey JOIN organization ON subscription.organizationkey=organization.tkey LEFT JOIN usagelicense ON usagelicense.subscription_tkey=subscription.tkey LEFT JOIN platformuser ON usagelicense.user_tkey=platformuser.tkey WHERE organization.organizationid=? AND subscription.status IN ('ACTIVE', 'PENDING', 'PENDING_UPD', 'SUSPENDED', 'SUSPENDED_UPD')";
    static final String QUERY_CUSTOMER_SUBSCRIPTIONS = "SELECT s.subscriptionid, s.status, cust.registrationdate, cust.organizationid as organizationid, cust.name, cust.email, p.productid FROM subscription s, product p, organization sup, organization cust, organizationreference ref WHERE p.tkey = s.product_tkey AND sup.organizationid=? AND s.organizationkey=cust.tkey AND p.vendorkey = sup.tkey AND ref.sourcekey = sup.tkey AND ref.targetkey = cust.tkey AND ref.referencetype = 'SUPPLIER_TO_CUSTOMER' UNION SELECT s.subscriptionid, s.status, cust.registrationdate, cust.organizationid as organizationid, cust.name, cust.email, p.productid from subscription s, product p, organization sup, organization cust, organizationreference ref, organization brok, product pt, product t WHERE p.TKEY = s.product_tkey AND sup.organizationid=? AND s.organizationkey=cust.tkey AND p.type = 'PARTNER_SUBSCRIPTION' AND p.template_tkey = pt.tkey AND pt.type = 'PARTNER_TEMPLATE' AND pt.template_tkey = t.tkey AND pt.vendorkey = brok.tkey AND t.vendorkey = sup.tkey AND ref.sourcekey = brok.tkey AND ref.targetkey = cust.tkey AND ref.referencetype = 'BROKER_TO_CUSTOMER' ORDER BY organizationid";
    static final String QUERY_CUSTOMER_SUBSCRIPTIONS_PRIVACY = "SELECT s.subscriptionid, s.status, cust.organizationid as organizationid, p.productid FROM subscription s, product p, organization sup, organization cust, organizationreference ref WHERE p.tkey = s.product_tkey AND sup.organizationid=? AND s.organizationkey=cust.tkey AND p.vendorkey = sup.tkey AND ref.sourcekey = sup.tkey AND ref.targetkey = cust.tkey AND ref.referencetype = 'SUPPLIER_TO_CUSTOMER' UNION SELECT s.subscriptionid, s.status, cust.organizationid as organizationid, p.productid from subscription s, product p, organization sup, organization cust, organizationreference ref, organization brok, product pt, product t WHERE p.TKEY = s.product_tkey AND sup.organizationid=? AND s.organizationkey=cust.tkey AND p.type = 'PARTNER_SUBSCRIPTION' AND p.template_tkey = pt.tkey AND pt.type = 'PARTNER_TEMPLATE' AND pt.template_tkey = t.tkey AND pt.vendorkey = brok.tkey AND t.vendorkey = sup.tkey AND ref.sourcekey = brok.tkey AND ref.targetkey = cust.tkey AND ref.referencetype = 'BROKER_TO_CUSTOMER' ORDER BY organizationid";
    static final String QUERY_LAST_VALID_SUBID_MAP = "SELECT DISTINCT s.subscriptionid as sid, sh.subscriptionid FROM subscriptionhistory s, "
            + " (SELECT sh.subscriptionid, sh.objkey FROM subscriptionhistory sh, "
            + "(SELECT s.objkey,  MAX(s.moddate) AS moddate FROM subscriptionhistory s WHERE s.status <> :status  GROUP BY s.objkey ) AS s "
            + " WHERE sh.objkey = s.objkey AND sh.moddate = s.moddate) AS sh WHERE s.objkey=sh.objkey";
    static final String QUERY_SUPPLIER_PRODUCT_REPORT = "SELECT subscription.subscriptionid, subscription.activationdate, subscription.deactivationdate, product.productid as productid from subscription, product, organization WHERE subscription.product_tkey=product.tkey AND product.vendorkey=organization.tkey AND organization.organizationid=? UNION SELECT subscription.subscriptionid, subscription.activationdate, subscription.deactivationdate, product.productid as productid from subscription, product, Organization supplier, Organization broker, OrganizationToRole otr, OrganizationRole orgRole, product pt, product t WHERE subscription.product_tkey=product.tkey AND supplier.organizationid=? AND product.vendorkey=broker.tkey AND product.TYPE = 'PARTNER_SUBSCRIPTION' AND product.template_tkey = pt.tkey AND pt.type = 'PARTNER_TEMPLATE' AND pt.template_tkey = t.tkey AND pt.vendorkey = broker.tkey AND t.vendorkey = supplier.tkey AND otr.organization_tkey = broker.tkey AND otr.organizationrole_tkey= orgRole.tkey AND orgRole.rolename= 'BROKER' ORDER BY productid";

    public SubscriptionDao(DataService ds) {
        this.ds = ds;
    }

    public List<ReportResultData> retrieveSubscriptionReportData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SUBSCRIPTION_REPORT);
        sqlQuery.setString(1, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

    public List<ReportResultData> retrieveSubscriptionReportData(
            String organizationId, List<Long> unitKeys) {

        if (unitKeys == null || unitKeys.isEmpty()) {
            return new ArrayList<ReportResultData>();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unitKeys.size(); i++) {
            builder.append("?,");
        }

        String query = QUERY_SUBSCRIPTION_REPORT
                + " AND subscription.usergroup_tkey IN ("
                + builder.deleteCharAt(builder.length() - 1).toString() + ")";

        SqlQuery sqlQuery = new SqlQuery(query);
        sqlQuery.setString(1, organizationId);
        int index = 2;
        for (Long key : unitKeys) {
            sqlQuery.setLong(index++, key.longValue());
        }
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

    private List<ReportResultData> convertToReportResultData(DataSet rs) {
        List<ReportResultData> result = new ArrayList<ReportResultData>();
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

    public List<ReportResultData> retrieveSupplierCustomerReportData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_CUSTOMER_SUBSCRIPTIONS);
        sqlQuery.setString(1, organizationId);
        sqlQuery.setString(2, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

    public List<ReportResultData> retrieveSupplierCustomerReportOfASupplierData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_CUSTOMER_SUBSCRIPTIONS_PRIVACY);
        sqlQuery.setString(1, organizationId);
        sqlQuery.setString(2, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

    /**
     * @return Map the subscriptionId with the latest valid one
     * */
    public Map<String, String> retrieveLastValidSubscriptionIdMap() {
        HashMap<String, String> result = new HashMap<String, String>();
        Query query = ds.createNativeQuery(QUERY_LAST_VALID_SUBID_MAP);
        query.setParameter("status", SubscriptionStatus.DEACTIVATED.name());
        @SuppressWarnings("unchecked")
        List<Object[]> querymap = query.getResultList();
        for (Object[] objs : querymap) {
            result.put((String) objs[0], (String) objs[1]);
        }
        return result;
    }

    public List<ReportResultData> retrieveSupplierProductReportData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_SUPPLIER_PRODUCT_REPORT);
        sqlQuery.setString(1, organizationId);
        sqlQuery.setString(2, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

}
