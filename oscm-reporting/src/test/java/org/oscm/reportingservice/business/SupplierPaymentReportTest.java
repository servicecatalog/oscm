/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.PaymentDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author kulle
 * 
 */
public class SupplierPaymentReportTest {

    SupplierPaymentReport reporting;
    PaymentDao dao;
    SubscriptionDao subscriptionDao;
    DataSource ds;

    PlatformUser user;

    @Before
    public void setup() {
        dao = mock(PaymentDao.class);
        subscriptionDao = mock(SubscriptionDao.class);
        ds = mock(DataSource.class);
        reporting = new SupplierPaymentReport(dao, subscriptionDao);
    }

    void givenUser(OrganizationRoleType... roles) {
        Organization o = new Organization();
        o.setKey(1L);
        Set<OrganizationToRole> grantedRoles = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType roleType : roles) {
            OrganizationToRole otr = new OrganizationToRole();
            otr.setOrganizationRole(new OrganizationRole(roleType));
            otr.setOrganization(o);
            grantedRoles.add(otr);
        }
        o.setGrantedRoles(grantedRoles);

        user = new PlatformUser();
        user.setKey(10L);
        user.setOrganization(o);
        user.setLocale("en");
    }

    @SuppressWarnings("boxing")
    private void mockReportData(String columnName, Object value, int type) {
        ReportResultData resultData = new ReportResultData();
        resultData.setColumnCount(1);
        resultData.setColumnName(Arrays.asList(columnName));
        resultData.setColumnType(Arrays.asList(type));
        resultData.setColumnValue(Arrays.asList(value));
        doReturn(Arrays.asList(resultData)).when(subscriptionDao)
                .retrieveSubscriptionReportData(anyString());
        doReturn(Arrays.asList(resultData)).when(dao)
                .retrievePaymentInformationData(anyLong());
    }

    private static Element getFirstElement(List<Object> data) {
        Element dataElement = (Element) data.get(0);
        dataElement.getOwnerDocument().appendChild(
                dataElement.getOwnerDocument().adoptNode(dataElement));
        return dataElement;
    }

    @Test
    public void testGetReportClobColumnWithXPathHandlingProcessingResult()
            throws Exception {
        // given
        givenUser(OrganizationRoleType.SUPPLIER);
        mockReportData(
                "processingresult",
                "<Response><Transaction><Processing><Return>Transaction succeeded</Return></Processing></Transaction></Response>",
                Types.CLOB);

        // when
        List<Object> data = new ArrayList<Object>();
        VOReportResult reportResult = new VOReportResult();
        reportResult.setData(data);
        reporting.buildReport(1L, reportResult);

        // then
        assertEquals(1, data.size());
        Element dataElement = getFirstElement(data);
        String result = XMLConverter.getNodeTextContentByXPath(
                dataElement.getOwnerDocument(), "/row/PROCESSINGRESULT/text()");
        assertEquals("Transaction succeeded", result);
    }

}
