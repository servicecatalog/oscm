/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock
 *                                                                              
 *  Creation Date: 16.11.2010                                                      
 *                                                                              
 *  Completion Time: 23.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.vatservice.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.intf.VatService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.VatRates;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.vatservice.assembler.VatRateAssembler;

public class VatServiceBeanIT extends EJBTestBase {

    private final static BigDecimal ONE = BigDecimal.valueOf(100, 2);
    private final static BigDecimal TEN = BigDecimal.valueOf(1000, 2);

    private DataService mgr;
    private VatServiceBean vatServiceBean;
    private VatService vatService;

    private Organization supplier;
    private List<Organization> customers = new ArrayList<>();
    private String userKey;

    @Override
    public void setup(final TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        });
        vatServiceBean = new VatServiceBean();
        container.addBean(vatServiceBean);

        vatService = container.get(VatService.class);

        mgr = container.get(DataService.class);

        container.login("setup", ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BaseAdmUmTest.createPaymentTypes(mgr);
                SupportedCountry de_country = SupportedCountries
                        .findOrCreate(mgr, Locale.GERMANY.getCountry());

                SupportedCountry ja_country = SupportedCountries
                        .findOrCreate(mgr, Locale.JAPAN.getCountry());

                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                supplier.setSupportedCountry(de_country);
                supplier.setSupportedCountry(ja_country);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        supplier, false, "user");
                userKey = String.valueOf(user.getKey());

                customers.add(Organizations.createCustomer(mgr, supplier));
                customers.add(Organizations.createCustomer(mgr, supplier));
                mgr.refresh(supplier);
                return null;
            }
        });

        container.login(userKey);
    }

    @Test
    public void testVatSupportDisabled() throws Exception {
        Assert.assertFalse(vatService.getVatSupport());
        Assert.assertNull(vatService.getDefaultVat());
        Assert.assertEquals(0, vatService.getCountryVats().size());
        Assert.assertEquals(0, vatService.getOrganizationVats().size());
    }

    @Test
    public void testVatSupportEnabled() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = mgr.getReference(Organization.class,
                        supplier.getKey());

                VatRates.createVatRate(mgr, supplier, TEN, null, null);
                return null;
            }
        });
        Assert.assertTrue(vatService.getVatSupport());
        Assert.assertEquals(TEN, vatService.getDefaultVat().getRate());
    }

    @Test(expected = EJBException.class)
    public void testMissingDefaultVat() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = mgr.getReference(Organization.class,
                        supplier.getKey());
                VatRates.createVatRate(mgr, supplier, TEN,
                        Locale.GERMANY.getCountry(), null);
                return null;
            }
        });

        vatService.getDefaultVat();
    }

    @Test
    public void testGetCountryVats() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = mgr.getReference(Organization.class,
                        supplier.getKey());
                VatRates.createVatRate(mgr, supplier, BigDecimal.ZERO, null,
                        null);
                VatRates.createVatRate(mgr, supplier, TEN,
                        Locale.GERMANY.getCountry(), null);
                return null;
            }
        });

        Assert.assertEquals(TEN, vatService.getCountryVats().get(0).getRate());
    }

    @Test
    public void testGetOrganizationVats() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = mgr.getReference(Organization.class,
                        supplier.getKey());

                VatRates.createVatRate(mgr, supplier, BigDecimal.ZERO, null,
                        null);
                Organization customer;
                customer = mgr.getReference(Organization.class,
                        customers.get(0).getKey());
                VatRates.createVatRate(mgr, supplier, ONE, null, customer);
                customer = mgr.getReference(Organization.class,
                        customers.get(1).getKey());
                VatRates.createVatRate(mgr, supplier, TEN, null, customer);
                return null;
            }
        });

        Assert.assertEquals(ONE,
                vatService.getOrganizationVats().get(0).getRate());
        Assert.assertEquals(TEN,
                vatService.getOrganizationVats().get(1).getRate());
    }

    @Test
    public void testSaveDefaultVat() throws Exception {
        createDefaultVat(TEN);
        Assert.assertEquals(TEN, vatService.getDefaultVat().getRate());
    }

    @Test
    public void testSaveDefaultVat_update() throws Exception {
        createDefaultVat(TEN);

        VOVatRate vo;
        vo = vatService.getDefaultVat();
        vo.setRate(ONE);
        vatService.saveDefaultVat(vo);

        Assert.assertEquals(ONE, vatService.getDefaultVat().getRate());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSaveDefaultVat_oldVersion() throws Exception {
        createDefaultVat(TEN);

        VOVatRate vo;
        vo = vatService.getDefaultVat();
        vo.setRate(ONE);
        vatService.saveDefaultVat(vo);

        vatService.saveDefaultVat(vo);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSaveDefaultVat_alreadyDeleted() throws Exception {
        createDefaultVat(TEN);

        VOVatRate vo;
        vo = vatService.getDefaultVat();
        vatService.saveDefaultVat(null);

        vatService.saveDefaultVat(vo);
    }

    @Test
    public void testSaveDefaultVat_rateNull() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createCountryVat(TEN);
        createOrganizationVat(TEN);

        VOVatRate vo = vatService.getDefaultVat();
        vo.setRate(null);
        vatService.saveDefaultVat(vo);

        Assert.assertNull(vatService.getDefaultVat());
        Assert.assertEquals(0, vatService.getCountryVats().size());
        Assert.assertEquals(0, vatService.getOrganizationVats().size());
        Assert.assertFalse(vatService.getVatSupport());
    }

    @Test
    public void testSaveDefaultVat_vatNull() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createCountryVat(TEN);
        createOrganizationVat(TEN);

        vatService.saveDefaultVat(null);

        Assert.assertNull(vatService.getDefaultVat());
        Assert.assertEquals(0, vatService.getCountryVats().size());
        Assert.assertEquals(0, vatService.getOrganizationVats().size());
        Assert.assertFalse(vatService.getVatSupport());
    }

    @Test
    public void testSaveDefaultVat_invalidVat() throws Exception {
        try {
            createDefaultVat(BigDecimal.valueOf(13.333));
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.VAT,
                    e.getReason());
        }
    }

    @Test(expected = ValidationException.class)
    public void testSaveOrganizationVats_notSupported() throws Exception {
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveOrganizationVats(null);
    }

    @Test
    public void testSaveOrganizationVats() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createOrganizationVat(TEN);

        List<VOOrganizationVatRate> orgVatRates = vatService
                .getOrganizationVats();
        Assert.assertEquals(1, orgVatRates.size());
        Assert.assertEquals(customers.get(0).getOrganizationId(),
                orgVatRates.get(0).getOrganization().getOrganizationId());
        Assert.assertEquals(TEN, orgVatRates.get(0).getRate());

        vatService.saveOrganizationVats(null);
        Assert.assertEquals(1, vatService.getOrganizationVats().size());

        vatService.saveOrganizationVats(new ArrayList<VOOrganizationVatRate>());
        Assert.assertEquals(1, vatService.getOrganizationVats().size());

        // add a second VAT
        orgVatRates = new ArrayList<>();
        VOOrganizationVatRate orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.getOrganization()
                .setOrganizationId(customers.get(1).getOrganizationId());
        orgVatRate.setRate(ONE);
        orgVatRates.add(orgVatRate);
        vatService.saveOrganizationVats(orgVatRates);
        Assert.assertEquals(2, vatService.getOrganizationVats().size());
        Assert.assertEquals(ONE,
                vatService.getOrganizationVats().get(1).getRate());
    }

    @Test
    public void testSaveOrganizationVats_update() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createOrganizationVat(TEN);

        List<VOOrganizationVatRate> orgVatRates = vatService
                .getOrganizationVats();
        orgVatRates.get(0).setRate(ONE);
        vatService.saveOrganizationVats(orgVatRates);
        Assert.assertEquals(ONE,
                vatService.getOrganizationVats().get(0).getRate());
    }

    @Test
    public void testSaveOrganizationVats_delete() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createOrganizationVat(TEN);

        List<VOOrganizationVatRate> orgVatRates = vatService
                .getOrganizationVats();
        orgVatRates.get(0).setRate(null);
        vatService.saveOrganizationVats(orgVatRates);
        Assert.assertEquals(0, vatService.getOrganizationVats().size());
    }

    @Test
    public void testSaveOrganizationVats_missingOrganization()
            throws Exception {
        createDefaultVat(BigDecimal.ZERO);

        List<VOOrganizationVatRate> orgVatRates = new ArrayList<>();
        VOOrganizationVatRate orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.setRate(TEN);
        orgVatRates.add(orgVatRate);
        try {
            vatService.saveOrganizationVats(orgVatRates);
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.REQUIRED,
                    e.getReason());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSaveOrganizationVats_unknownOrganization()
            throws Exception {
        createDefaultVat(BigDecimal.ZERO);

        List<VOOrganizationVatRate> orgVatRates = new ArrayList<>();
        VOOrganizationVatRate orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.getOrganization().setOrganizationId("unknown");
        orgVatRate.setRate(TEN);
        orgVatRates.add(orgVatRate);
        vatService.saveOrganizationVats(orgVatRates);
    }

    @Test
    public void testSaveOrganizationVats_dublicateOrganization()
            throws Exception {
        VOVatRate defaultVat = new VOVatRate();
        defaultVat.setRate(BigDecimal.ZERO);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveDefaultVat(defaultVat);

        List<VOOrganizationVatRate> orgVatRates = new ArrayList<>();

        VOOrganizationVatRate orgVatRate;
        orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.getOrganization()
                .setOrganizationId(customers.get(0).getOrganizationId());
        orgVatRate.setRate(TEN);
        orgVatRates.add(orgVatRate);

        orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.getOrganization()
                .setOrganizationId(customers.get(0).getOrganizationId());
        orgVatRate.setRate(TEN);
        orgVatRates.add(orgVatRate);
        try {
            vatService.saveOrganizationVats(orgVatRates);
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.DUPLICATE_VALUE,
                    e.getReason());
        }
    }

    @Test(expected = ValidationException.class)
    public void testSaveCountryVats_notSupported() throws Exception {
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveOrganizationVats(null);
    }

    @Test
    public void testSaveCountryVats() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createCountryVat(TEN);

        List<VOCountryVatRate> countryVatRates = vatService.getCountryVats();
        Assert.assertEquals(1, countryVatRates.size());
        Assert.assertEquals(Locale.GERMANY.getCountry(),
                countryVatRates.get(0).getCountry());
        Assert.assertEquals(TEN, countryVatRates.get(0).getRate());

        vatService.saveCountryVats(null);
        Assert.assertEquals(1, vatService.getCountryVats().size());

        vatService.saveCountryVats(new ArrayList<VOCountryVatRate>());
        Assert.assertEquals(1, vatService.getCountryVats().size());

        // add a second VAT
        countryVatRates = new ArrayList<>();
        VOCountryVatRate countryVatRate = new VOCountryVatRate();
        countryVatRate.setCountry(Locale.JAPAN.getCountry());
        countryVatRate.setRate(ONE);
        countryVatRates.add(countryVatRate);
        vatService.saveCountryVats(countryVatRates);
        Assert.assertEquals(2, vatService.getCountryVats().size());
        Assert.assertEquals(ONE, vatService.getCountryVats().get(1).getRate());
    }

    @Test
    public void testSaveCountryVats_update() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createCountryVat(TEN);

        List<VOCountryVatRate> countryVatRates = vatService.getCountryVats();
        countryVatRates.get(0).setRate(ONE);
        vatService.saveCountryVats(countryVatRates);
        Assert.assertEquals(ONE, vatService.getCountryVats().get(0).getRate());
    }

    @Test
    public void testSaveCountryVats_delete() throws Exception {
        createDefaultVat(BigDecimal.ZERO);
        createCountryVat(TEN);

        List<VOCountryVatRate> countryVatRates = vatService.getCountryVats();
        countryVatRates.get(0).setRate(null);
        vatService.saveCountryVats(countryVatRates);
        Assert.assertEquals(0, vatService.getCountryVats().size());
    }

    @Test
    public void testSaveCountryVats_missingCountry() throws Exception {
        VOVatRate defaultVat = new VOVatRate();
        defaultVat.setRate(BigDecimal.ZERO);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveDefaultVat(defaultVat);

        List<VOCountryVatRate> countryVatRates = new ArrayList<>();
        VOCountryVatRate countryVatRate = new VOCountryVatRate();
        countryVatRate.setRate(TEN);
        countryVatRates.add(countryVatRate);
        try {
            vatService.saveCountryVats(countryVatRates);
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.REQUIRED,
                    e.getReason());
        }
    }

    @Test
    public void testSaveCountryVats_unknownCountry() throws Exception {
        VOVatRate defaultVat = new VOVatRate();
        defaultVat.setRate(BigDecimal.ZERO);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveDefaultVat(defaultVat);

        List<VOCountryVatRate> countryVatRates = new ArrayList<>();
        VOCountryVatRate countryVatRate = new VOCountryVatRate();
        countryVatRate.setCountry(Locale.FRANCE.getCountry());
        countryVatRate.setRate(TEN);
        countryVatRates.add(countryVatRate);
        try {
            vatService.saveCountryVats(countryVatRates);
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(
                    ValidationException.ReasonEnum.COUNTRY_NOT_SUPPORTED,
                    e.getReason());
        }
    }

    @Test
    public void testSaveCountryVats_dublicateCountry() throws Exception {
        VOVatRate defaultVat = new VOVatRate();
        defaultVat.setRate(BigDecimal.ZERO);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveDefaultVat(defaultVat);

        List<VOCountryVatRate> countryVatRates = new ArrayList<>();
        VOCountryVatRate countryVatRate;
        countryVatRate = new VOCountryVatRate();
        countryVatRate.setCountry(Locale.GERMANY.getCountry());
        countryVatRate.setRate(TEN);
        countryVatRates.add(countryVatRate);

        countryVatRate = new VOCountryVatRate();
        countryVatRate.setCountry(Locale.GERMANY.getCountry());
        countryVatRate.setRate(TEN);
        countryVatRates.add(countryVatRate);

        try {
            vatService.saveCountryVats(countryVatRates);
            Assert.fail();
        } catch (ValidationException e) {
            Assert.assertEquals(ValidationException.ReasonEnum.DUPLICATE_VALUE,
                    e.getReason());
        }
    }

    @Test
    public void testSaveAllVats() throws Exception {
        VOVatRate defaultVat = new VOVatRate();
        defaultVat.setRate(TEN);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveAllVats(defaultVat, null, null);
        Assert.assertEquals(TEN, vatService.getDefaultVat().getRate());
        vatService.saveAllVats(null, null, null);
        Assert.assertFalse(vatService.getVatSupport());
    }

    @Test(expected = ValidationException.class)
    public void testSaveAllVats_invalidCountryList() throws Exception {
        List<VOCountryVatRate> list = new ArrayList<>();
        list.add(new VOCountryVatRate());
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveAllVats(null, list, null);
    }

    @Test(expected = ValidationException.class)
    public void testSaveAllVats_invalidOrganizationList() throws Exception {
        List<VOOrganizationVatRate> list = new ArrayList<>();
        list.add(new VOOrganizationVatRate());
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveAllVats(null, null, list);
    }

    @Test(expected = EJBException.class)
    public void testSaveDefaultVat_nonUniqueBusinessKey() throws Exception {
        vatServiceBean.dm = new DataServiceBean() {
            @Override
            public void persist(DomainObject<?> obj)
                    throws NonUniqueBusinessKeyException {
                throw new NonUniqueBusinessKeyException();
            }

        };
        VOVatRate vo = new VOVatRate();
        vo.setRate(TEN);
        vatService.saveDefaultVat(vo);
    }

    @Test
    public void testConstructors() throws Exception {
        assertNotNull(new VatRateAssembler());
        assertNotNull(new VatServiceBean());
    }

    @Test
    public void testToVO_NullParameters() throws Exception {
        assertNull(VatRateAssembler.toVOCountryVatRate(null));
        assertNull(VatRateAssembler.toVOOrganizationVatRate(null, null));
        assertNull(VatRateAssembler.toVOVatRate(null));
    }

    private void createDefaultVat(BigDecimal rate) throws Exception {
        VOVatRate vo = new VOVatRate();
        vo.setRate(rate);
        container.login(userKey, ROLE_SERVICE_MANAGER);
        vatService.saveDefaultVat(vo);
    }

    private void createCountryVat(BigDecimal rate) throws Exception {
        List<VOCountryVatRate> countryVats = new ArrayList<>();
        VOCountryVatRate countryVat = new VOCountryVatRate();
        countryVat.setCountry(Locale.GERMANY.getCountry());
        countryVat.setRate(rate);
        countryVats.add(countryVat);
        vatService.saveCountryVats(countryVats);
    }

    private void createOrganizationVat(BigDecimal rate) throws Exception {
        List<VOOrganizationVatRate> orgVatRates = new ArrayList<>();
        VOOrganizationVatRate orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setOrganization(new VOOrganization());
        orgVatRate.getOrganization()
                .setOrganizationId(customers.get(0).getOrganizationId());
        orgVatRate.setRate(rate);
        orgVatRates.add(orgVatRate);
        vatService.saveOrganizationVats(orgVatRates);
    }

}
