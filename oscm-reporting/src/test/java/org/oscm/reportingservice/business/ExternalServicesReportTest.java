/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.externalservices.RDOExternalService;
import org.oscm.reportingservice.business.model.externalservices.RDOExternalSupplier;
import org.oscm.reportingservice.dao.ExternalServicesDao;
import org.oscm.reportingservice.dao.ExternalServicesDao.ReportData;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author weiser
 * 
 */
public class ExternalServicesReportTest {

    private static final String PID = "pid";

    private ExternalServicesReport esp;

    private ExternalServicesDao dao;

    private PlatformUser user;

    @Before
    public void setup() {
        dao = mock(ExternalServicesDao.class);

        esp = new ExternalServicesReport(dao);

        user = new PlatformUser();
        Organization org = new Organization();
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganization(org);
        otr.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.PLATFORM_OPERATOR));
        org.getGrantedRoles().add(otr);
        user.setOrganization(org);
        user.setLocale("de");
    }

    @Test
    public void buildReport_Null() {
        RDOExternal result = esp.buildReport(null);

        assertNotNull(result);
        assertTrue(result.getExternalSuppliers().isEmpty());
    }

    @Test
    public void buildReport_NoOperator() {
        user.getOrganization().getGrantedRoles().clear();

        RDOExternal result = esp.buildReport(user);

        assertNotNull(result);
        assertTrue(result.getExternalSuppliers().isEmpty());
    }

    @Test
    public void buildReport() {
        ReportData rd = givenOrgWithService(false);

        RDOExternal result = esp.buildReport(user);
        assertNotNull(result);
        validateResult(rd, result);
    }

    /**
     * Bug 9978 internal product id's must be cut including '#'
     */
    @Test
    public void buildReport_InternalProductId() {
        ReportData rd = givenOrgWithService(true);

        RDOExternal result = esp.buildReport(user);
        assertNotNull(result);
        validateResult(rd, result);
    }

    private ReportData givenOrgWithService(boolean internalId) {
        ReportData rd1 = new ReportData();
        rd1.setAddress("address");
        rd1.setCountry("DE");
        rd1.setEmail("email");
        rd1.setModdate(new Date());
        rd1.setName("name");
        rd1.setPhone("phone");
        rd1.setProductId(PID);
        if (internalId) {
            rd1.setProductId(PID + "#u6ir86-8tutru56-85urh");
        }
        rd1.setProductKey(new Long(7));
        rd1.setProductStatus("ACTIVE");

        ReportData rd2 = new ReportData();
        rd2.setAddress("address");
        rd2.setCountry("DE");
        rd2.setEmail("email");
        rd2.setModdate(new Date());
        rd2.setName("name");
        rd2.setPhone("phone");
        rd2.setProductId(PID);
        if (internalId) {
            rd2.setProductId(PID + "#u6ir86-8tutru56-85urh");
        }
        rd2.setProductKey(new Long(7));
        rd2.setProductStatus("INACTIVE");

        when(dao.getReportData()).thenReturn(Arrays.asList(rd1, rd2));
        return rd1;
    }

    private void validateResult(ReportData rd, RDOExternal result) {
        assertEquals(1, result.getExternalSuppliers().size());
        assertTrue(result.getServerTimeZone().startsWith("UTC"));
        RDOExternalSupplier sup = result.getExternalSuppliers().get(0);
        assertEquals(rd.getAddress(), sup.getAddress());
        assertEquals(rd.getEmail(), sup.getEmail());
        assertEquals(rd.getName(), sup.getName());
        assertEquals(rd.getPhone(), sup.getPhone());

        assertEquals(1, sup.getExternalServices().size());
        RDOExternalService svc = sup.getExternalServices().get(0);
        assertEquals(PID, svc.getServiceName());
    }
}
