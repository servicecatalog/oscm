/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;

/**
 * @author kulle
 * 
 */
public class ExternalServicesDao {

    public static class ReportData {
        private Object productKey;
        private String productId;
        private String productStatus;
        private Date moddate;
        private String address;
        private String name;
        private String phone;
        private String email;
        private String country;

        public Object getProductKey() {
            return productKey;
        }

        public void setProductKey(Object productKey) {
            this.productKey = productKey;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductStatus() {
            return productStatus;
        }

        public void setProductStatus(String productStatus) {
            this.productStatus = productStatus;
        }

        public Date getModdate() {
            return moddate;
        }

        public void setModdate(Date moddate) {
            this.moddate = moddate;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

    }

    private final DataService ds;
    private final List<ReportData> reportData = new ArrayList<ReportData>();

    static final String QUERY_EXTERNAL_SERVICES = "SELECT producthistory.objkey, producthistory.productid, producthistory.status, producthistory.moddate, organization.address, organization.name || ' (' || organization.organizationid || ')' AS name, organization.phone, organization.email, supportedcountry.countryisocode FROM technicalproduct, producthistory, organization, supportedcountry WHERE technicalproduct.accesstype = 'EXTERNAL' AND producthistory.status IN ('ACTIVE', 'INACTIVE') AND producthistory.technicalproductobjkey = technicalproduct.tkey AND organization.tkey = producthistory.vendorobjkey AND organization.domicilecountry_tkey = supportedcountry.tkey ORDER BY organization.tkey, technicalproduct.tkey, producthistory.objkey, producthistory.moddate";

    public ExternalServicesDao(DataService ds) {
        this.ds = ds;
    }

    public List<ReportData> getReportData() {
        return reportData;
    }

    public void executeQuery() {
        DataSet rs = ds.executeQueryForRawData(new SqlQuery(
                QUERY_EXTERNAL_SERVICES));
        convertToReportData(rs);
    }

    private void convertToReportData(DataSet rs) {
        while (rs.next()) {
            ReportData rd = new ReportData();
            rd.setProductKey(rs.getObject("objkey"));
            rd.setProductId(rs.getString("productid"));
            rd.setProductStatus(rs.getString("status"));
            rd.setModdate(rs.getDate("moddate"));
            rd.setAddress(rs.getString("address"));
            rd.setName(rs.getString("name"));
            rd.setPhone(rs.getString("phone"));
            rd.setEmail(rs.getString("email"));
            rd.setCountry(rs.getString("countryisocode"));
            reportData.add(rd);
        }
    }

}
