/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.communicationservice.bean.CommunicationServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;

// Temporary ignored because of RQ: Flexible billing cut-off day 
public class BillingServiceGetCustomerBillingDataIT extends EJBTestBase {

    protected DataService dm;
    protected Organization supplier;
    protected PlatformUser supplierUser;
    protected Organization supplier2;
    protected PlatformUser supplier2User;
    protected Organization customer;
    protected BillingResult br1;
    protected BillingResult br2;
    protected BillingResult brForSupplier2;
    private BillingService bs;
    protected long start1;
    protected long end1;
    protected long start2;
    protected long end2;
    private static final BigDecimal GROSS_REVENUE = BigDecimal.valueOf(743342);
    private static final BigDecimal NET_REVENUE = BigDecimal.valueOf(423746);

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(mock(SharesDataRetrievalServiceBean.class));
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(mock(SharesCalculatorBean.class));
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new CommunicationServiceBean());
        container.addBean(new UserLicenseDao());
        container.addBean(new BillingServiceBean());

        dm = container.get(DataService.class);
        bs = container.get(BillingService.class);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                createOrganizationRoles(dm);
                createPaymentTypes(dm);
                createSupportedCurrencies(dm);
                SupportedCountries.createSomeSupportedCountries(dm);
                dm.flush();

                supplier = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                supplierUser = Organizations.createUserForOrg(dm, supplier,
                        true, "admin");
                customer = Organizations.createCustomer(dm, supplier);

                supplier2 = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                supplier2User = Organizations.createUserForOrg(dm, supplier2,
                        true, "admin2");
                Organizations.createOrganizationReference(supplier2, customer,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER, dm);
                return null;
            }
        });

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                start1 = getDate(2011, 0, 1);
                end1 = getDate(2011, 1, 1);
                start2 = getDate(2011, 1, 1);
                end2 = getDate(2011, 2, 1);

                SupportedCurrency currency_EUR = SupportedCurrencies
                        .findOrCreate(dm, "EUR");
                br1 = createBillingData(dm, customer.getKey(), start1, end1,
                        getBillingXml(start1, end1, null), supplier.getKey(),
                        currency_EUR);
                br2 = createBillingData(dm, customer.getKey(), start2, end2,
                        getBillingXml(start2, end2, null), supplier.getKey(),
                        currency_EUR);
                brForSupplier2 = createBillingData(dm, customer.getKey(),
                        start2, end2, getBillingXml(start2, end2, supplier2),
                        supplier2.getKey(), currency_EUR);
                return null;
            }
        });
    }

    @Test
    public void testBug7414_WithoutLimits() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(null, null,
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertTrue(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, null)));
        Assert.assertFalse(result,
                result.contains(getBillingXml(start2, end2, supplier2)));
    }

    @Test
    public void testBug7414_EndEqualsPeriodTwoEnd() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(null, Long.valueOf(end2),
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertTrue(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug7414_StartEqualsPeriodOneStart() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(Long.valueOf(start1), null,
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertTrue(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug7414_StartEqualsPeriodTwoStart() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(Long.valueOf(start2), null,
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertFalse(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug7414_EndEqualsPeriodOneEnd() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(null, Long.valueOf(end1),
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertTrue(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertFalse(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug7414_LimitsEqualPeriodOne() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(Long.valueOf(start1),
                Long.valueOf(end1),
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertTrue(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertFalse(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug7414_LimitsEqualPeriodTwo() throws Exception {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(Long.valueOf(start2),
                Long.valueOf(end2),
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertFalse(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, null)));
    }

    @Test
    public void testBug9088_LimitsChargingOrg() throws Exception {
        container.login(supplier2User.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(null, null, null);
        String result = new String(data, "UTF-8");
        Assert.assertFalse(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertFalse(result,
                result.contains(getBillingXml(start2, end2, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, supplier2)));
    }

    @Test
    public void testBug9088_LimitsChargingOrgForSpecificCustomer()
            throws Exception {
        container.login(supplier2User.getKey(), ROLE_SERVICE_MANAGER);
        byte[] data = bs.getCustomerBillingData(null, null,
                Arrays.asList(new String[] { customer.getOrganizationId() }));
        String result = new String(data, "UTF-8");
        Assert.assertFalse(result,
                result.contains(getBillingXml(start1, end1, null)));
        Assert.assertFalse(result,
                result.contains(getBillingXml(start2, end2, null)));
        Assert.assertTrue(result,
                result.contains(getBillingXml(start2, end2, supplier2)));
    }

    private static final BillingResult createBillingData(DataService mgr,
            long orgKey, long periodStartTime, long periodEndTime, String xml,
            long charchingOrgKey, SupportedCurrency currency_EUR)
            throws NonUniqueBusinessKeyException {
        BillingResult br = new BillingResult();
        br.setCreationTime(System.currentTimeMillis());
        br.setOrganizationTKey(orgKey);
        br.setPeriodStartTime(periodStartTime);
        br.setPeriodEndTime(periodEndTime);
        br.setResultXML(xml);
        br.setChargingOrgKey(charchingOrgKey);
        br.setCurrency(currency_EUR);
        br.setGrossAmount(GROSS_REVENUE);
        br.setNetAmount(NET_REVENUE);
        mgr.persist(br);
        mgr.flush();
        return br;
    }

    private static final long getDate(int year, int month, int date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(year, month, date, 0, 0, 0);
        return cal.getTimeInMillis();
    }

    private static final String getBillingXml(long start, long end,
            Organization supplier) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<BillingDetails>");
        buffer.append("<Period endDate=\"");
        buffer.append(start);
        buffer.append("\" startDate=\"");
        buffer.append(end);
        buffer.append("\" />");
        if (supplier != null) {
            buffer.append("<Subscriptions>");
            buffer.append("<Subscription id=\"");
            buffer.append(Long.toString(supplier.getKey()));
            buffer.append("\" />");
            buffer.append("</Subscriptions>");
        }
        buffer.append("</BillingDetails>");
        return buffer.toString();
    }
}
