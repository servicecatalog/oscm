/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                         
 *                                                                              
 *  Creation Date: 16.11.2010                                                      
 *                                                                              
 *  Completion Time: 16.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.VatRates;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Test of the VatRate domain object.
 * 
 * @author pock
 * 
 */
public class VatRateIT extends DomainObjectTestBase {

    private Organization supplier;

    private Organization customer;

    private List<VatRate> objList = new ArrayList<VatRate>();

    @Override
    protected void dataSetup() throws Exception {
        supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER);
        SupportedCountries.createAllSupportedCountries(mgr);
        customer = Organizations.createCustomer(mgr, supplier);
        supplier.setSupportedCountry(SupportedCountries.findOrCreate(mgr, "DE"));
    }

    private void verify(ModificationType modType) throws Exception {
        verify(modType, objList, VatRate.class);
    }

    @Test
    public void testAdd() throws Exception {
        objList.add(createVatRate(null, null));
        objList.add(createVatRate(Locale.GERMANY.getCountry(), null));
        objList.add(createVatRate(null, customer));

        verify(ModificationType.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTargetCountryAlreadySet() throws Exception {
        objList.add(createVatRate(Locale.GERMANY.getCountry(), null));
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .get(0).getKey());
                vatRate.setTargetOrganization(customer);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTargetOrganizationAlreadySet() throws Exception {
        objList.add(createVatRate(null, customer));
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .get(0).getKey());
                vatRate.setTargetCountry(new SupportedCountry());
                return null;
            }
        });
    }

    @Test
    public void testModify() throws Exception {
        objList.add(createVatRate(null, null));
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization newSupplier = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .remove(0).getKey());
                vatRate.setOwningOrganization(newSupplier);
                vatRate.setRate(BigDecimal.valueOf(900, 2));

                objList.add((VatRate) ReflectiveClone.clone(vatRate));
                return null;
            }
        });
        verify(ModificationType.MODIFY);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                List<DomainHistoryObject<?>> history = mgr.findHistory(objList
                        .get(0));
                Assert.assertEquals(supplier.getKey(), VatRateHistory.class
                        .cast(history.get(0)).getOwningOrganizationObjKey());
                return null;
            }
        });
    }

    @Test
    public void testModifyTargetCountry() throws Exception {
        objList.add(createVatRate(Locale.GERMANY.getCountry(), null));
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .remove(0).getKey());
                SupportedCountry supportedCountry = SupportedCountries
                        .findOrCreate(mgr, Locale.UK.getCountry());
                // OrganizationToCountry o2c = new OrganizationToCountry(); TODO
                // o2c.setSupportedCountry(supportedCountry);
                // o2c.setOrganization(supplier);
                // mgr.persist(o2c);
                vatRate.setTargetCountry(supportedCountry);

                objList.add((VatRate) ReflectiveClone.clone(vatRate));
                return null;
            }
        });
        verify(ModificationType.MODIFY);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                List<DomainHistoryObject<?>> history = mgr.findHistory(objList
                        .get(0));
                VatRateHistory h = VatRateHistory.class.cast(history.get(0));
                SupportedCountry o2c = mgr.getReference(SupportedCountry.class,
                        h.getTargetCountryObjKey().longValue());
                Assert.assertEquals(Locale.GERMANY.getCountry(),
                        o2c.getCountryISOCode());
                return null;
            }
        });
    }

    @Test
    public void testModifyTargetOrganization() throws Exception {
        objList.add(createVatRate(null, customer));
        final Organization newCustomer = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .remove(0).getKey());
                supplier = Organizations.findOrganization(mgr,
                        supplier.getOrganizationId());
                Organization customer = Organizations.createCustomer(mgr,
                        supplier);
                vatRate.setTargetOrganization(customer);

                objList.add((VatRate) ReflectiveClone.clone(vatRate));
                return customer;
            }
        });
        verify(ModificationType.MODIFY);

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                VatRate vatRate = mgr.getReference(VatRate.class, objList
                        .get(0).getKey());
                Assert.assertEquals(newCustomer.getKey(), vatRate
                        .getTargetOrganization().getKey());
                List<DomainHistoryObject<?>> history = mgr.findHistory(objList
                        .get(0));
                Assert.assertEquals(customer.getKey(), VatRateHistory.class
                        .cast(history.get(0)).getTargetOrganizationObjKey()
                        .longValue());
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        objList.add(createVatRate(null, null));
        objList.add(createVatRate(Locale.GERMANY.getCountry(), null));
        objList.add(createVatRate(null, customer));
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                for (VatRate vatRate : objList) {
                    mgr.remove(mgr.getReference(VatRate.class, vatRate.getKey()));
                }
                return null;
            }
        });
        verify(ModificationType.DELETE);
    }

    private VatRate createVatRate(final String countryCode,
            final Organization customer) throws Exception {
        return runTX(new Callable<VatRate>() {
            public VatRate call() throws Exception {
                supplier = mgr.getReference(Organization.class,
                        supplier.getKey());
                Organization targetOrganization = null;
                if (customer != null) {
                    targetOrganization = mgr.getReference(Organization.class,
                            customer.getKey());
                }
                VatRate vatRate = VatRates.createVatRate(mgr, supplier,
                        BigDecimal.valueOf(1900, 2), countryCode,
                        targetOrganization);
                return (VatRate) ReflectiveClone.clone(vatRate);
            }
        });
    }
}
