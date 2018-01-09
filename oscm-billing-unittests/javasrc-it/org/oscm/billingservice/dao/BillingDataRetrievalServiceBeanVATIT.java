/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 10.05.2010                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.VatRate;
import org.oscm.domobjects.VatRateHistory;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.VatRates;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Test class for BilllingDataRetrievalServiceBean functionality related to VAT
 * rates.
 * 
 */
public class BillingDataRetrievalServiceBeanVATIT extends EJBTestBase {

    private static final BigDecimal _12_25 = BigDecimal.valueOf(1225, 2);

    private DataService dm;
    private BillingDataRetrievalServiceLocal bdr;

    private long supplierKey;
    private long customerKeyForDefaultVat;
    private long customerKeyForCountryVat;
    private long customerKeyForCustomerVat;
    private long customerKeyForComplexVatScenario;
    private long defaultVatRateKey;
    private long countryVatRateKey;
    private long customerVatRateKey;

    private long complexCustomerVatRateKey;
    private long complexUnusedCountryVatRateKey;

    /**
     * Common setup for the test class.
     */
    @Override
    public void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(dm);
                SupportedCountries.createOneSupportedCountry(dm);
                SupportedCountries.findOrCreate(dm, "EN");

                Organization supplier = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                supplierKey = supplier.getKey();

                Organization supplier2 = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);

                dm.flush();
                dm.refresh(supplier);

                SupportedCountry sc = (SupportedCountry) dm
                        .getReferenceByBusinessKey(new SupportedCountry(
                                Locale.GERMANY.getCountry()));

                Organization customer = Organizations.createCustomer(dm,
                        supplier);
                customer.setDomicileCountry(null);
                customerKeyForDefaultVat = customer.getKey();
                Organizations.addSupplierToCustomer(dm, supplier2, customer);

                Organization customer2 = Organizations.createCustomer(dm,
                        supplier);
                customer2.setDomicileCountry(sc);
                customerKeyForCountryVat = customer2.getKey();
                Organizations.addSupplierToCustomer(dm, supplier2, customer2);

                Organization customer3 = Organizations.createCustomer(dm,
                        supplier);
                customer3.setDomicileCountry(null);
                customerKeyForCustomerVat = customer3.getKey();
                Organizations.addSupplierToCustomer(dm, supplier2, customer3);

                Organization customer4 = Organizations.createCustomer(dm,
                        supplier);
                customer4.setDomicileCountry(sc);
                customerKeyForComplexVatScenario = customer4.getKey();

                VatRate vatRate = VatRates.createVatRate(dm, supplier, _12_25,
                        null, null);
                defaultVatRateKey = vatRate.getKey();
                VatRates.createVatRate(dm, supplier2,
                        BigDecimal.valueOf(1600, 2), null, null);

                VatRate countryVatRate = VatRates.createVatRate(dm, supplier,
                        BigDecimal.valueOf(22.25), "DE", null);
                countryVatRateKey = countryVatRate.getKey();

                VatRate customerVatRate = VatRates.createVatRate(dm, supplier,
                        BigDecimal.valueOf(32.25), null, customer3);
                customerVatRateKey = customerVatRate.getKey();
                VatRates.createVatRate(dm, supplier2, BigDecimal.valueOf(12.34),
                        null, customer3);

                VatRate complexCustomerVat = VatRates.createVatRate(dm,
                        supplier, BigDecimal.valueOf(61), null, customer4);
                complexCustomerVatRateKey = complexCustomerVat.getKey();
                VatRate complexUnusedCountryVat = VatRates.createVatRate(dm,
                        supplier, BigDecimal.valueOf(62), "EN", null);
                complexUnusedCountryVatRateKey = complexUnusedCountryVat
                        .getKey();

                return null;
            }
        });
        updateVATHistoryModDate(defaultVatRateKey, 0);
        updateVATHistoryModDate(countryVatRateKey, 0);
        updateVATHistoryModDate(customerVatRateKey, 0);
        updateVATHistoryModDate(complexCustomerVatRateKey, 0);
        updateVATHistoryModDate(complexUnusedCountryVatRateKey, 0);
    }

    @Test
    public void testGetVATForCustomer_NonExistingOrg() throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(-1, 1, supplierKey);
            }
        });
        assertNoVatRates(result);
    }

    /**
     * 
     * supplier customer too
     */
    @Test
    public void testGetVATForCustomer_SupplierOrg() throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(supplierKey, 1, supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(22.25), null);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVat() throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 1,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVatSetAfterPeriod()
            throws Exception {
        updateVATHistoryModDate(defaultVatRateKey, 2);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 1,
                        supplierKey);
            }
        });
        assertNoVatRates(result);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVatChangedAfterPeriod()
            throws Exception {
        updateVATRate(defaultVatRateKey, BigDecimal.valueOf(13), 2);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 1,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVatChangedInPeriod()
            throws Exception {
        updateVATRate(defaultVatRateKey, BigDecimal.valueOf(13), 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, BigDecimal.valueOf(1300, 2), null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVatDeletedAfterPeriod()
            throws Exception {
        deleteVatRate(defaultVatRateKey, 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerOrgDefaultVatDeletedInPeriod()
            throws Exception {
        deleteVatRate(defaultVatRateKey, 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForDefaultVat, 2,
                        supplierKey);
            }
        });
        assertNoVatRates(result);
    }

    @Test
    public void testGetVATForCustomer_CountryVatDefinedInPeriod()
            throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(22.25), null);
    }

    @Test
    public void testGetVATForCustomer_CountryVatDefinedAfterPeriod()
            throws Exception {
        updateVATHistoryModDate(countryVatRateKey, 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CountryVatModifiedAfterPeriod()
            throws Exception {
        updateVATRate(countryVatRateKey, BigDecimal.valueOf(50), 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(22.25), null);
    }

    @Test
    public void testGetVATForCustomer_CountryVatModifiedInPeriod()
            throws Exception {
        updateVATRate(countryVatRateKey, BigDecimal.valueOf(50), 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(5000, 2), null);
    }

    @Test
    public void testGetVATForCustomer_CountryVatDeletedAfterPeriod()
            throws Exception {
        deleteVatRate(countryVatRateKey, 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(22.25), null);
    }

    @Test
    public void testGetVATForCustomer_CountryVatDeletedInPeriod()
            throws Exception {
        deleteVatRate(countryVatRateKey, 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCountryVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerVatDefinedInPeriod()
            throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, BigDecimal.valueOf(3225, 2));
    }

    @Test
    public void testGetVATForCustomer_CustomerVatDefinedAfterPeriod()
            throws Exception {
        updateVATHistoryModDate(customerVatRateKey, 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_CustomerVatModifiedAfterPeriod()
            throws Exception {
        updateVATRate(customerVatRateKey, BigDecimal.valueOf(40), 4);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, BigDecimal.valueOf(3225, 2));
    }

    @Test
    public void testGetVATForCustomer_CustomerVatModifiedInPeriod()
            throws Exception {
        updateVATRate(customerVatRateKey, BigDecimal.valueOf(40), 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, BigDecimal.valueOf(4000, 2));
    }

    @Test
    public void testGetVATForCustomer_CustomerVatDeletedAfterPeriod()
            throws Exception {
        deleteVatRate(customerVatRateKey, 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, BigDecimal.valueOf(32.25f));
    }

    @Test
    public void testGetVATForCustomer_CustomerVatDeletedInPeriod()
            throws Exception {
        deleteVatRate(customerVatRateKey, 1);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForCustomerVat, 2,
                        supplierKey);
            }
        });
        assertVatRates(result, _12_25, null, null);
    }

    @Test
    public void testGetVATForCustomer_complexScenarioNoChanges()
            throws Exception {
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForComplexVatScenario,
                        2, supplierKey);
            }
        });
        assertVatRates(result, _12_25, BigDecimal.valueOf(22.25),
                BigDecimal.valueOf(6100, 2));
    }

    @Test
    public void testGetVATForCustomer_complexScenarioChanges()
            throws Exception {
        deleteVatRate(complexCustomerVatRateKey, 1);
        updateVATRate(defaultVatRateKey, BigDecimal.valueOf(34), 1);
        updateVATRate(countryVatRateKey, BigDecimal.valueOf(99), 3);
        VatRateDetails result = runTX(new Callable<VatRateDetails>() {
            @Override
            public VatRateDetails call() throws Exception {
                return bdr.loadVATForCustomer(customerKeyForComplexVatScenario,
                        2, supplierKey);
            }
        });
        assertVatRates(result, BigDecimal.valueOf(3400, 2),
                BigDecimal.valueOf(22.25), null);
    }

    // -------------------------------------------------------------------------------
    // internal methods

    private void deleteVatRate(final long vatRateKey, final long time)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VatRate reference = dm.getReference(VatRate.class, vatRateKey);
                reference.setHistoryModificationTime(Long.valueOf(time));
                dm.remove(reference);
                return null;
            }
        });
    }

    private void updateVATRate(final long vatRateKey, final BigDecimal rate,
            final long time) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VatRate reference = dm.getReference(VatRate.class, vatRateKey);
                reference.setRate(rate);
                reference.setHistoryModificationTime(Long.valueOf(time));
                return null;
            }
        });
    }

    private void updateVATHistoryModDate(final long vatRateKey,
            final long modDate) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = dm.createQuery(
                        "SELECT vh FROM VatRateHistory vh WHERE vh.objKey = :vatRateKey");
                query.setParameter("vatRateKey", Long.valueOf(vatRateKey));
                List<VatRateHistory> entries = ParameterizedTypes
                        .list(query.getResultList(), VatRateHistory.class);
                for (VatRateHistory entry : entries) {
                    entry.setModdate(new Date(modDate));
                }
                return null;
            }
        });
    }

    /**
     * Validates that the vat rate details contain no entries.
     * 
     * @param result
     *            The vat rate details object to be validated.
     */
    private void assertNoVatRates(VatRateDetails result) {
        assertNotNull(result);
        assertNull("DefaultVatRate", result.getDefaultVatRate());
        assertNull("CountryVatRate", result.getCountryVatRate());
        assertNull("CustomerVatRate", result.getCustomerVatRate());
    }

    /**
     * Validates that the vat rate details contain no entries.
     * 
     * @param result
     *            The vat rate details object to be validated.
     * @param defaultVatRate
     *            The expected value for the default VAT rate.
     * @param countryVatRate
     *            The country specific VAT rate that is expected.
     * @param customerVatRate
     *            The customer specific VAT rate that is expected.
     */
    private void assertVatRates(VatRateDetails result,
            BigDecimal defaultVatRate, BigDecimal countryVatRate,
            BigDecimal customerVatRate) {
        assertNotNull(result);
        assertEquals(defaultVatRate, result.getDefaultVatRate());
        assertEquals(countryVatRate, result.getCountryVatRate());
        assertEquals(customerVatRate, result.getCustomerVatRate());
    }

}
