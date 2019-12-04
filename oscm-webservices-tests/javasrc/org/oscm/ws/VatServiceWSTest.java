/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/
package org.oscm.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.intf.VatService;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCountryVatRate;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationVatRate;
import org.oscm.vo.VOVatRate;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

public class VatServiceWSTest {

  private static VatService vatService;

  private List<VOOrganization> customers = new ArrayList<VOOrganization>();;

  private static final BigDecimal ONE = BigDecimal.valueOf(100, 2);
  private static final BigDecimal TEN = BigDecimal.valueOf(1000, 2);

  @Before
  public void setUp() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    // clean the mails
    WebserviceTestBase.getMailReader().deleteMails();
    // add currencies
    WebserviceTestBase.getOperator().addCurrency("EUR");

    WebserviceTestSetup setup = new WebserviceTestSetup();

    // Create a supplier
    setup.createSupplier("Supplier");

    // Create two customers
    customers.add(setup.createCustomer("Customer1"));
    customers.add(setup.createCustomer("Customer2"));
    // Get the Vat web service of the supplier.
    vatService =
        ServiceFactory.getDefault()
            .getVatService(
                String.valueOf(setup.getSupplierUserKey()), WebserviceTestBase.DEFAULT_PASSWORD);
  }

  @Test
  public void testSaveDefaultVat_vatNull() throws Exception {
    // save default vat rate
    vatService.saveDefaultVat(null);

    // checks if default vat rate is null and vat support is disabled.
    Assert.assertFalse(vatService.getVatSupport());
    Assert.assertNull(vatService.getDefaultVat());
  }

  @Test(expected = ValidationException.class)
  public void testSaveDefaultVat_invalidRate() throws Exception {
    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(BigDecimal.valueOf(25.444));

    vatService.saveDefaultVat(defaultVat);
  }

  @Test
  public void testSaveDefaultVat() throws Exception {
    // define a default vat rate of 10.00.
    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(TEN);

    // check that the vat support is disabled
    Assert.assertFalse(vatService.getVatSupport());

    // save default vat rate
    vatService.saveDefaultVat(defaultVat);

    // check that the default vat rate has the correct value and the vat
    // support is enabled.
    Assert.assertTrue(vatService.getVatSupport());
    Assert.assertEquals(TEN, vatService.getDefaultVat().getRate());
  }

  @Test
  public void testSaveCountryVats() throws Exception {
    // First enable vat support by defining a default vat rate.
    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(TEN);
    vatService.saveDefaultVat(defaultVat);

    // Define the country vat rates
    List<VOCountryVatRate> countryVats = new ArrayList<VOCountryVatRate>();
    VOCountryVatRate countryVat = new VOCountryVatRate();
    countryVat.setCountry(Locale.GERMANY.getCountry());
    countryVat.setRate(ONE);
    countryVats.add(countryVat);

    vatService.saveCountryVats(countryVats);

    Assert.assertTrue(vatService.getVatSupport());
    Assert.assertEquals(1, vatService.getCountryVats().size());
    Assert.assertEquals(ONE, vatService.getCountryVats().get(0).getRate());
  }

  @Test(expected = ValidationException.class)
  public void testSaveOrganizationVats_vatSupportDisabled() throws Exception {
    vatService.saveOrganizationVats(null);
  }

  @Test
  public void testSaveOrganizationVats() throws Exception {
    // first enable vat support by defining a default vat rate.
    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(TEN);
    vatService.saveDefaultVat(defaultVat);

    // define the organization vats
    List<VOOrganizationVatRate> organizationVats = new ArrayList<VOOrganizationVatRate>();

    VOOrganizationVatRate orgVatRate;
    orgVatRate = new VOOrganizationVatRate();
    orgVatRate.setOrganization(new VOOrganization());
    orgVatRate.getOrganization().setOrganizationId(customers.get(0).getOrganizationId());
    orgVatRate.setRate(TEN);
    organizationVats.add(orgVatRate);

    orgVatRate = new VOOrganizationVatRate();
    orgVatRate.setOrganization(new VOOrganization());
    orgVatRate.getOrganization().setOrganizationId(customers.get(1).getOrganizationId());
    orgVatRate.setRate(ONE);
    organizationVats.add(orgVatRate);

    // save the organization vats
    vatService.saveOrganizationVats(organizationVats);

    // checks if correct values have been saved.
    Assert.assertTrue(vatService.getVatSupport());
    Assert.assertEquals(2, vatService.getOrganizationVats().size());
    Assert.assertEquals(TEN, vatService.getOrganizationVats().get(0).getRate());
    Assert.assertEquals(ONE, vatService.getOrganizationVats().get(1).getRate());
  }

  @Test
  public void testSaveAllVats() throws Exception {
    // define the default vat rate
    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(TEN);

    // define the country and organization vats
    List<VOOrganizationVatRate> organizationVats = new ArrayList<VOOrganizationVatRate>();

    VOOrganizationVatRate orgVatRate;
    orgVatRate = new VOOrganizationVatRate();
    orgVatRate.setOrganization(new VOOrganization());
    orgVatRate.getOrganization().setOrganizationId(customers.get(0).getOrganizationId());
    orgVatRate.setRate(TEN);
    organizationVats.add(orgVatRate);

    orgVatRate = new VOOrganizationVatRate();
    orgVatRate.setOrganization(new VOOrganization());
    orgVatRate.getOrganization().setOrganizationId(customers.get(1).getOrganizationId());
    orgVatRate.setRate(ONE);
    organizationVats.add(orgVatRate);

    List<VOCountryVatRate> countryVats = new ArrayList<VOCountryVatRate>();
    VOCountryVatRate countryVat = new VOCountryVatRate();
    countryVat.setCountry(Locale.JAPAN.getCountry());
    countryVat.setRate(ONE);
    countryVats.add(countryVat);

    // save all vats.
    vatService.saveAllVats(defaultVat, countryVats, organizationVats);

    // checks if correct values have been saved.
    Assert.assertTrue(vatService.getVatSupport());
    Assert.assertEquals(TEN, vatService.getDefaultVat().getRate());
    Assert.assertEquals(1, vatService.getCountryVats().size());
    Assert.assertEquals(ONE, vatService.getCountryVats().get(0).getRate());
    Assert.assertEquals(2, vatService.getOrganizationVats().size());
    Assert.assertEquals(TEN, vatService.getOrganizationVats().get(0).getRate());
    Assert.assertEquals(ONE, vatService.getOrganizationVats().get(1).getRate());
  }

  @Test
  @Ignore
  public void testSaveDefaultVat_UserUnauthorized() throws Exception {
    VatService defaultVatService = ServiceFactory.getDefault().getVatService();

    VOVatRate defaultVat = new VOVatRate();
    defaultVat.setRate(TEN);

    try {
      defaultVatService.saveDefaultVat(defaultVat);
      fail();
    } catch (SOAPFaultException e) {
      checkAccessException(e);
    }
  }

  private void checkAccessException(SOAPFaultException e) {
    assertTrue(e.getMessage().contains("Unauthorized"));
  }
}
