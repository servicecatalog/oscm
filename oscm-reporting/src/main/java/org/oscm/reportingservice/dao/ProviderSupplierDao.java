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
public class ProviderSupplierDao {

    private final DataService ds;

    static final String QUERY_PROVIDER_SUPPLIER = "SELECT sup.organizationid, sup.name, sup.email, sup.registrationdate, tp.technicalproductid, template.productid FROM organization prov, organization sup, technicalproduct tp, product template, organizationreference ref WHERE (template.technicalproduct_tkey=tp.tkey AND template.template_tkey IS NULL) AND tp.organizationkey=prov.tkey AND template.vendorkey=sup.tkey AND sup.tkey=ref.targetkey AND ref.sourcekey=prov.tkey AND ref.referencetype = 'TECHNOLOGY_PROVIDER_TO_SUPPLIER' AND prov.organizationid=? ORDER BY sup.organizationid, tp.technicalproductid, template.productid";
    static final String QUERY_PROVIDER_INSTANCE = "SELECT tp.technicalproductid, p.productid, s.productinstanceid, pd.parameterid, param.value FROM organization prov, technicalproduct tp, (product p LEFT JOIN parameterset ps on parameterset_tkey=ps.tkey) left join parameter param on param.parametersetkey=ps.tkey left join parameterdefinition pd on pd.tkey=param.parameterdefinitionkey, subscription s WHERE p.tkey=s.product_tkey AND p.technicalproduct_tkey=tp.tkey AND tp.organizationkey=prov.tkey AND prov.organizationid=? GROUP BY tp.technicalproductid, p.productid, s.productinstanceid, ps.tkey,param.value,pd.parameterid";
    static final String QUERY_PROVIDER_SUBSCRIPTION = "SELECT sup.organizationid, sup.name, sup.email, sup.registrationdate, tp.technicalproductid, template.productid, count (copy.productid) as subcount FROM organization prov, organization sup, technicalproduct tp, product template, product copy, organizationreference ref, subscription s WHERE s.product_tkey=copy.tkey AND s.status=? AND copy.template_tkey=template.tkey AND (template.technicalproduct_tkey=tp.tkey AND template.template_tkey IS NULL) AND tp.organizationkey=prov.tkey AND template.vendorkey=sup.tkey AND sup.tkey=ref.targetkey AND ref.sourcekey=prov.tkey AND ref.referencetype = 'TECHNOLOGY_PROVIDER_TO_SUPPLIER' AND prov.organizationid=? GROUP BY sup.organizationid, sup.name, sup.email, sup.registrationdate, tp.technicalproductid, template.productid order by sup.organizationid, tp.technicalproductid, template.productid";

    public ProviderSupplierDao(DataService ds) {
        this.ds = ds;
    }

    public List<ReportResultData> retrieveProviderSubscriptionReportData(
            String organizationId, String status) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_PROVIDER_SUBSCRIPTION);
        sqlQuery.setString(1, status);
        sqlQuery.setString(2, organizationId);
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

    public List<ReportResultData> retrieveProviderInstanceReportData(
            String organizationId) {
        return retrieveReportData(organizationId, QUERY_PROVIDER_INSTANCE);
    }

    private List<ReportResultData> retrieveReportData(String organizationId,
            String query) {
        SqlQuery sqlQuery = new SqlQuery(query);
        sqlQuery.setString(1, organizationId);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToReportResultData(dataSet);
    }

    public List<ReportResultData> retrieveProviderSupplierReportData(
            String organizationId) {
        return retrieveReportData(organizationId, QUERY_PROVIDER_SUPPLIER);
    }

}
