/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/
package org.oscm.ws.billing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.intf.BillingService;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * Integration tests for BillingServiceWS.
 *
 * @author barzu
 */
public class BillingServiceWSTest {

  private static WebserviceTestSetup setup;

  @BeforeClass
  public static void setUpOnce() throws Exception {
    assumeFalse(ServiceFactory.getDefault().isSSOMode());
    WebserviceTestBase.getMailReader().deleteMails();
    WebserviceTestBase.getOperator().addCurrency("EUR");

    setup = new WebserviceTestSetup();
    setup.createSupplier("Supplier");
  }

  @Test
  public void getCustomerBillingData() throws Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -1);
    long start = calendar.getTimeInMillis();
    calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, 1);
    long end = calendar.getTimeInMillis();

    BillingService ws =
        ServiceFactory.getDefault()
            .getBillingService(setup.getSupplierUserKey(), WebserviceTestBase.DEFAULT_PASSWORD);
    String result =
        new String(ws.getCustomerBillingData(Long.valueOf(start), Long.valueOf(end), null));

    // can only test that an empty result is returned, because billing
    // results are generated first at the end of the month
    assertTrue(result.contains("<Billingdata"));
  }
}
