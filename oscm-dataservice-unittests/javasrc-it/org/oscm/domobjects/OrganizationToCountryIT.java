/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;

public class OrganizationToCountryIT extends DomainObjectTestBase {

    private Organization org;

    private OrganizationToCountry removedCountry;

    /**
     * <b>Testcase:</b> Create new Organization object with 2 country codes<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>All objects can be retrieved from DB and are identical to provided
     * Organization objects</li>
     * <li>Cascaded OrganizationToCountry objects is also stored</li>
     * <li>A history object is created for each country code stored</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /*
     * Create organization with a couple of country codes
     */
    private void doTestAdd() throws NonUniqueBusinessKeyException {

        // create organization
        org = new Organization();
        org.setOrganizationId("OrganizationToCountryTest_testAdd");
        org.setCutOffDay(1);

        // create DE country
        SupportedCountry deCountry = SupportedCountries.findOrCreate(mgr, "DE");
        org.setSupportedCountry(deCountry);

        // create US country
        SupportedCountry usCountry = SupportedCountries.findOrCreate(mgr, "US");
        org.setSupportedCountry(usCountry);

        mgr.persist(org);
    }

    /*
     * Asserts that all created countries are persisted correctly
     */
    private void doTestAddCheck() {
        Organization reloadedOrganization = Organizations.findOrganization(mgr,
                org.getOrganizationId());

        // compare country codes reflectively
        checkSame(org.getOrganizationToCountries(),
                reloadedOrganization.getOrganizationToCountries());

    }

    /*
     * Assert that two given lists of countries are the same.
     */
    private void checkSame(List<OrganizationToCountry> persisted,
            List<OrganizationToCountry> reloaded) {
        for (int i = 0; i < persisted.size(); i++) {
            Assert.assertTrue(ReflectiveCompare.showDiffs(persisted.get(i),
                    reloaded.get(i)), ReflectiveCompare.compare(
                    persisted.get(i), reloaded.get(i)));
        }
    }

    /**
     * <b>Testcase:</b> Modify the list of supported country codes for an
     * existing organization object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the country codes</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPrepare();
                    return null;
                }

            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }

            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }

            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /*
     * Create one organization with one country code for testing
     */
    private void doTestModifyPrepare() throws NonUniqueBusinessKeyException {

        // insert new organization
        org = new Organization();
        org.setOrganizationId("OrganizationToCountryTest_testModify");
        org.setCutOffDay(1);

        // create DE country
        SupportedCountry deCountry = SupportedCountries.findOrCreate(mgr, "DE");
        org.setSupportedCountry(deCountry);

        mgr.persist(org);
    }

    /*
     * Remove the existing country code and add a country code
     */
    private void doTestModify() throws NonUniqueBusinessKeyException {
        org = Organizations.findOrganization(mgr, org.getOrganizationId());

        // remove DE country
        removedCountry = org.getOrganizationToCountries().get(0);
        org.getOrganizationToCountries().remove(removedCountry);
        mgr.remove(removedCountry);

        // add JP country
        SupportedCountry jpCountry = SupportedCountries.findOrCreate(mgr, "JP");
        org.setSupportedCountry(jpCountry);
        org.getOrganizationToCountries().get(0);

    }

    /*
     * Check that modifications have been persisted. Check history entries.
     */
    private void doTestModifyCheck() {
        Organization reloadedOrganization = Organizations.findOrganization(mgr,
                org.getOrganizationId());

        // assert changes of supported countries
        checkSame(org.getOrganizationToCountries(),
                reloadedOrganization.getOrganizationToCountries());

    }

    /**
     * Create supplier and organization. Supplier defined a list of supported
     * countries. The customer chooses a domicile country from the provided list
     * of the supplier.
     * 
     * @throws Throwable
     */
    @Test
    public void testSetDomicileCountry() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doSetDomicileCountry();
                    return null;
                }

            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doSetDomicileCountryCheck();
                    return null;
                }

            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doSetDomicileCountry() throws Exception {

        // create supplier that supports DE
        Organization supplier = new Organization();
        supplier.setOrganizationId("supplier");
        SupportedCountry deCountry = SupportedCountries.findOrCreate(mgr, "DE");
        supplier.setSupportedCountry(deCountry);
        supplier.setCutOffDay(1);
        mgr.persist(supplier);

        // create customer and set DE as domicile
        Organization customer = new Organization();
        customer.setOrganizationId("customer");
        customer.setDomicileCountry(deCountry);
        customer.setCutOffDay(1);
        mgr.persist(customer);

    }

    private void doSetDomicileCountryCheck() {
        Organization reloadedCustomer = Organizations.findOrganization(mgr,
                "customer");
        assertEquals("DE", reloadedCustomer.getDomicileCountryCode());

        // check history of organization to country
        SupportedCountry orgToCountryProxy = reloadedCustomer
                .getDomicileCountry();

        // check history of organization
        List<DomainHistoryObject<?>> entries = mgr
                .findHistory(reloadedCustomer);
        OrganizationHistory orgHistory = (OrganizationHistory) entries.get(0);
        assertEquals(orgToCountryProxy.getKey(), orgHistory
                .getDomicileCountryObjKey().longValue());
    }
}
