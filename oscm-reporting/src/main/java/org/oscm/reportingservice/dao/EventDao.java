/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;

/**
 * @author kulle
 * 
 */
public class EventDao {

    private final DataService ds;

    static final String QUERY_PROVIDER_EVENT = "SELECT tp.technicalproductid, tp.accesstype, tp.provisioningtype, p.productid, s.productinstanceid, ge.eventidentifier, SUM (ge.multiplier) as eventcount FROM technicalproduct tp, product p, subscription s, gatheredevent ge, organization o WHERE ge.subscriptiontkey=s.tkey AND s.product_tkey=p.tkey AND p.technicalproduct_tkey=tp.tkey AND tp.organizationkey=o.tkey AND o.organizationid=? GROUP BY tp.technicalproductid, tp.accesstype, tp.provisioningtype, p.productid, s.productinstanceid, ge.eventidentifier ORDER BY tp.technicalproductid, s.productinstanceid, ge.eventidentifier";
    static final String QUERY_CUSTOMER_EVENT = "SELECT ge.actor, ge.type, ge.eventidentifier, SUM(ge.multiplier), p.productid, ge.occurrencetime, pu.firstname, pu.lastname, sub.subscriptionid, ge.subscriptiontkey FROM subscription sub, organization org, product p, gatheredevent ge LEFT OUTER JOIN platformuser pu ON ge.actor=pu.userid WHERE ge.subscriptiontkey=sub.tkey AND (pu.organizationkey=org.tkey OR pu.organizationkey IS NULL) AND sub.product_tkey=p.tkey AND sub.organizationkey=org.tkey AND org.organizationid=?";
    static final String QUERY_CUSTOMER_EVENT_GROUP_ORDER = "GROUP BY ge.actor, ge.type, ge.eventidentifier, p.productid, ge.occurrencetime, pu.firstname, pu.lastname, sub.subscriptionid, ge.subscriptiontkey ORDER BY ge.occurrencetime DESC";
    static final String QUERY_CUSTOMER_EVENT_LOCALIZED = "SELECT ge.actor, ge.type, ge.eventidentifier, ge.multiplier, p.productid, ge.occurrencetime, pu.firstname, pu.lastname, sub.subscriptionid, ge.subscriptiontkey, lr.value, lr.locale FROM localizedresource lr, subscription sub, organization org, product p, gatheredevent ge LEFT OUTER JOIN platformuser pu ON ge.actor=pu.userid WHERE ge.subscriptiontkey=sub.tkey AND lr.locale=? and lr.objecttype='EVENT_DESC' AND lr.objectkey=(SELECT tkey FROM event WHERE event.eventidentifier=ge.eventidentifier AND event.eventtype = ge.type AND event.technicalproduct_tkey=p.technicalproduct_tkey) AND (pu.organizationkey=org.tkey OR pu.organizationkey IS NULL) and sub.product_tkey=p.tkey AND sub.organizationkey=org.tkey AND org.organizationid=? ORDER BY ge.occurrencetime DESC";

    public EventDao(DataService ds) {
        this.ds = ds;
    }

    public List<CustomerEventData> retrieveCustomerEventData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_CUSTOMER_EVENT + " "
                + QUERY_CUSTOMER_EVENT_GROUP_ORDER);
        sqlQuery.setMax(10000);
        sqlQuery.setString(1, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToCustomerEventData(dataSet);
    }

    public List<CustomerEventData> retrieveCustomerEventData(
            String organizationId, List<Long> unitKeys) {

        if (unitKeys == null || unitKeys.isEmpty()) {
            return new ArrayList<CustomerEventData>();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unitKeys.size(); i++) {
            builder.append("?,");
        }

        String query = QUERY_CUSTOMER_EVENT + " AND sub.usergroup_tkey IN ("
                + builder.deleteCharAt(builder.length() - 1).toString() + ") "
                + QUERY_CUSTOMER_EVENT_GROUP_ORDER;
        SqlQuery sqlQuery = new SqlQuery(query);
        sqlQuery.setMax(10000);
        sqlQuery.setString(1, organizationId);
        int index = 2;
        for (Long key : unitKeys) {
            sqlQuery.setLong(index++, key.longValue());
        }
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToCustomerEventData(dataSet);
    }

    private List<CustomerEventData> convertToCustomerEventData(DataSet rs) {
        List<CustomerEventData> result = new ArrayList<CustomerEventData>();
        while (rs.next()) {
            CustomerEventData row = new CustomerEventData();
            row.setActor(rs.getString(1));
            row.setType(rs.getString(2));
            row.setEventidentifier(rs.getString(3));
            row.setMultiplier(rs.getBigDecimal(4));
            row.setProductid(rs.getString(5));
            row.setOccurrencetime(rs.getLong(6));
            row.setFirstname(rs.getString(7));
            row.setLastname(rs.getString(8));
            row.setSubscriptionid(rs.getString(9));
            row.setSubscriptiontkey(new BigDecimal(rs.getLong(10)));
            row.setLocale(null);
            row.setEventdescription(null);
            result.add(row);
        }
        return result;
    }

    public List<CustomerEventData> retrieveLocalizedCustomerEventData(
            String organizationId, String locale) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_CUSTOMER_EVENT_LOCALIZED);
        sqlQuery.setMax(10000);
        sqlQuery.setString(1, locale);
        sqlQuery.setString(2, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToLocalizedCustomerEventData(dataSet);
    }

    private List<CustomerEventData> convertToLocalizedCustomerEventData(
            DataSet rs) {
        List<CustomerEventData> result = new ArrayList<CustomerEventData>();
        while (rs.next()) {
            CustomerEventData row = new CustomerEventData();
            row.setActor(rs.getString(1));
            row.setType(rs.getString(2));
            row.setEventidentifier(rs.getString(3));
            row.setMultiplier(new BigDecimal(rs.getLong(4)));
            row.setProductid(rs.getString(5));
            row.setOccurrencetime(rs.getLong(6));
            row.setFirstname(rs.getString(7));
            row.setLastname(rs.getString(8));
            row.setSubscriptionid(rs.getString(9));
            row.setSubscriptiontkey(new BigDecimal(rs.getLong(10)));
            row.setEventdescription(rs.getString(11));
            row.setLocale(rs.getString(12));
            result.add(row);
        }
        return result;
    }

    public List<ReportResultData> retrieveProviderEventReportData(
            String organizationId) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_PROVIDER_EVENT);
        sqlQuery.setMax(10000);
        sqlQuery.setString(1, organizationId);
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

}
