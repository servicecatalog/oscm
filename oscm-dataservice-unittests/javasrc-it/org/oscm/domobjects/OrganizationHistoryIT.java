/********************************************************************************
 *                                                                             
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld
 *                                                                              
 *  Creation Date: 20.01.2009                                                      
 *                                                                             
 *  Completion Time:                           
 *                                                                           
 ********************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.test.data.Organizations;

/**
 * Tests of the organization-related domain objects (incl. auditing
 * functionality)
 * 
 * @author cheld
 * 
 */
public class OrganizationHistoryIT extends DomainObjectTestBase {

    @Test
    public void findLastOrganizationHistory() throws Throwable {
        try {

            // given three OrganizationHistory objects
            final Organization org = runTX(new Callable<Organization>() {
                public Organization call() throws Exception {
                    Organization org = Organizations.createOrganization(mgr);
                    mgr.persist(org);
                    org.setName("first modification");
                    mgr.persist(org);
                    org.setName("last modification");
                    mgr.persist(org);
                    return org;
                }
            });

            // when loading last history object
            OrganizationHistory last = runTX(new Callable<OrganizationHistory>() {
                public OrganizationHistory call() throws Exception {
                    return (OrganizationHistory) mgr.findLastHistory(org);
                }
            });

            // then the latest persisted value is returned
            assertEquals("last modification", last.getOrganizationName());
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

}
