/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
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
 * 
 */
public class PaymentDao {

    private final DataService ds;

    static final String QUERY_PAYMENT_INFORMATION = "SELECT pr.processingtime, pr.processingstatus, pr.processingresult, br.periodstarttime, br.periodendtime, br.tkey AS billingkey, orgh.organizationid FROM paymentresult pr, billingresult br, organizationhistory orgh WHERE pr.billingresult_tkey = br.tkey AND br.organizationtkey = orgh.objkey AND br.chargingorgkey = ? AND orgh.objversion = (SELECT MAX(iorgH.objversion) FROM organizationhistory iorgh WHERE orgh.objkey = iorgh.objkey)";

    public PaymentDao(DataService ds) {
        this.ds = ds;
    }

    public List<ReportResultData> retrievePaymentInformationData(
            long supplierKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_PAYMENT_INFORMATION);
        sqlQuery.setLong(1, supplierKey);
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
