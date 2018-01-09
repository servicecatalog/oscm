/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;

public class ReportIT extends DomainObjectTestBase {

    @Test
    public void testCreation() throws Exception {
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                return null;
            }
        });
        final Report report = runTX(new Callable<Report>() {

            public Report call() throws Exception {
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.CUSTOMER);
                role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);
                Report r = new Report();
                ReportData data = new ReportData();
                data.setReportName("reportName");
                r.setDataContainer(data);
                r.setOrganizationRole(role);
                mgr.persist(r);
                return r;
            }
        });
        runTX(new Callable<Void>() {

            public Void call() throws Exception {
                Report r = mgr.find(Report.class, report.getKey());
                Assert.assertEquals(report.getOrganizationRole().getRoleName(),
                        r.getOrganizationRole().getRoleName());
                ReportData data = r.getDataContainer();
                Assert.assertEquals(report.getReportName(), data
                        .getReportName());
                return null;
            }
        });
    }

}
