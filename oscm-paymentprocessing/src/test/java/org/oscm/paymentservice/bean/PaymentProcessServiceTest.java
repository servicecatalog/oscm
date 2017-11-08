/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 17.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPSetting;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentResult;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapter;
import org.oscm.paymentservice.local.PortLocatorLocal;
import org.oscm.paymentservice.transport.HttpClientFactory;
import org.oscm.paymentservice.transport.HttpMethodFactory;
import org.oscm.payproc.stubs.ConfigurationServiceStub;
import org.oscm.payproc.stubs.DataServiceStub;
import org.oscm.payproc.stubs.IdentityServiceStub;
import org.oscm.payproc.stubs.PaymentServiceProviderAdapterStub;
import org.oscm.payproc.stubs.QueryStub;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.types.enumtypes.PaymentProcessingStatus;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.PSPIdentifierForSellerException;
import org.oscm.internal.types.exception.PSPProcessingException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.Property;
import org.oscm.psp.data.RequestData;

/**
 * Unit tests for the payment service class.
 * 
 * @author Mike J&auml;ger
 */
public class PaymentProcessServiceTest {

    private PaymentServiceBean pps;
    private ConfigurationServiceStub ic;
    private IdentityServiceStub im;
    private DataServiceStub dm;
    private PaymentServiceProviderAdapterStub psps;
    public boolean throwIOException = false;

    private Organization customer;
    private BillingResult billingResult;
    private Organization supplier;
    private Organization platformOperatorOrg;

    public static String sampleResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Response version=\"1.0\">"
            + "<Transaction mode=\"LIVE\" response=\"SYNC\" channel=\"678a456b789c123d456e789f012g432\">"
            + "<Identification>"
            + "<TransactionID>MerchantAssignedID</TransactionID>"
            + "<UniqueID>h987i654j321k098l765m432n210o987</UniqueID>"
            + "<ShortID>1234.5678.9876</ShortID>"
            + "</Identification>"
            + "<Processing code=\"DD.DB.90.00\">"
            + "<Timestamp>2003-02-12 14:58:07</Timestamp>"
            + "<Result>ACK</Result>"
            + "<Status code=\"90\">NEW</Status>"
            + "<Reason code=\"00\">Successful Processing</Reason>"
            + "<Return code=\"000.000.000\">Transaction succeeded</Return>"
            + "</Processing>"
            + "<Payment code=\"DD.DB\">"
            + "<Clearing>"
            + "<Amount>1.00</Amount>"
            + "<Currency>EUR</Currency>"
            + "<Descriptor>shop.de 1234.1234.1234 +49 (89) 12345 678 Order Number 1234</Descriptor>"
            + "<Date>2003-02-13</Date>"
            + "<Support>+49 (89) 1234 567</Support>"
            + "</Clearing>"
            + "</Payment>" + "</Transaction>" + "</Response>";

    private static String sampleBillingResultMultiplePriceModels = "<BillingDetails> <Period endDate=\"1262300400000\" startDate=\"1259622000000\"/> <OrganizationDetails><Email>the customer's email</Email><Name>Name of organization 1000</Name> <Address>Address of organization 1000</Address> </OrganizationDetails> <Subscriptions> <Subscription id=\"sub\" purchaseOrderNumber=\"12345\"> <PriceModels> <PriceModel id=\"1\"> <UsagePeriod endDate=\"1262300400000\" startDate=\"1261263650015\"/> <GatheredEvents/> <PeriodFee basePeriod=\"MONTH\" basePrice=\"1000\" factor=\"0.3870781007317802\" price=\"387\"/> <UserAssignmentCosts basePeriod=\"MONTH\" basePrice=\"100\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/> <PriceModelCosts amount=\"387\"/> </PriceModel> <PriceModel id=\"2\"> <UsagePeriod endDate=\"1261263650015\" startDate=\"1260399600000\"/> <GatheredEvents> <Event id=\"FILE_UPLOAD\"> <SingleCost amount=\"1\"/> <NumberOfOccurrence amount=\"880\"/> <CostForEventType amount=\"880\"/> </Event> <Event id=\"USER_LOGIN_TO_SERVICE\"> <SingleCost amount=\"0\"/> <NumberOfOccurrence amount=\"880\"/> <CostForEventType amount=\"0\"/> </Event> <Event id=\"USER_LOGOUT_FROM_SERVICE\"> <SingleCost amount=\"0\"/> <NumberOfOccurrence amount=\"880\"/> <CostForEventType amount=\"0\"/> </Event> </GatheredEvents> <PeriodFee basePeriod=\"MONTH\" basePrice=\"20000\" factor=\"0.3225993186230585\" price=\"6452\"/> <UserAssignmentCosts/> <PriceModelCosts amount=\"7332\"/> </PriceModel> <PriceModel id=\"5\"> <UsagePeriod endDate=\"1260399600000\" startDate=\"1259622000000\"/> <GatheredEvents> <Event id=\"FILE_UPLOAD\"> <SingleCost amount=\"20\"/> <NumberOfOccurrence amount=\"120\"/> <CostForEventType amount=\"2400\"/> </Event> <Event id=\"USER_LOGIN_TO_SERVICE\"> <SingleCost amount=\"10\"/> <NumberOfOccurrence amount=\"120\"/> <CostForEventType amount=\"1200\"/> </Event> <Event id=\"USER_LOGOUT_FROM_SERVICE\"> <SingleCost amount=\"0\"/> <NumberOfOccurrence amount=\"120\"/> <CostForEventType amount=\"0\"/> </Event> </GatheredEvents> <PeriodFee basePeriod=\"MONTH\" basePrice=\"1000\" factor=\"0.2903225806451613\" price=\"290\"/> <UserAssignmentCosts basePeriod=\"MONTH\" basePrice=\"100\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/> <PriceModelCosts amount=\"3890\"/> </PriceModel> </PriceModels> <SubscriptionCosts amount=\"11609\"/> </Subscription> </Subscriptions> <OverallCosts currency=\"EUR\" grossAmount='13815'/></BillingDetails>";
    private QueryStub qs;
    private VOPaymentType ddPaymentType;
    private VOPaymentType ccPaymentType;
    private VOPaymentType ivPaymentType;
    private VOPaymentInfo voPaymentInfo;
    private PaymentType paymentType;
    private String currentUserLocale = Locale.ENGLISH.getLanguage();

    @Before
    public void setUp() {
        throwIOException = false;
        pps = new PaymentServiceBean();
        ic = new ConfigurationServiceStub();
        dm = new DataServiceStub() {
            public PlatformUser getCurrentUser() {
                return im.getCurrentUser();
            }

            @Override
            public <T extends DomainObject<?>> T getReference(
                    Class<T> objclass, long id) throws ObjectNotFoundException {

                if (objclass.equals(Organization.class) && id == 0) {
                    return objclass.cast(customer);
                }

                return super.getReference(objclass, id);
            }

        };
        im = new IdentityServiceStub() {
            public PlatformUser getCurrentUser() {
                PlatformUser user = super.getCurrentUser();
                user.setLocale(currentUserLocale);
                return user;
            }
        };
        qs = new QueryStub();
        psps = new PaymentServiceProviderAdapterStub();

        dm.query = qs;
        pps.ic = ic;
        pps.dm = dm;
        pps.portLocator = new PortLocatorLocal() {

            public PaymentServiceProviderAdapter getPort(String wsdl)
                    throws IOException {
                if (throwIOException) {
                    throw new IOException();
                }
                return psps;
            }
        };
        pps.localizer = new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }
        };

        // define which payment info object should be returned by the data
        // manager
        PaymentInfo doPi = new PaymentInfo();
        doPi.setExternalIdentifier("extId");
        PaymentType pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        pt.setPaymentTypeId("CREDIT_CARD");

        doPi.setPaymentType(pt);
        doPi.setOrganization(customer);

        PSP psp = new PSP();
        psp.setWsdlUrl("http://localhost:8180/oscm-psp-heidelpay/PaymentServiceProvider?wsdl");
        pt.setPsp(psp);
        dm.setPaymentInfo(doPi);

        ddPaymentType = new VOPaymentType();
        ddPaymentType.setPaymentTypeId("DIRECT_DEBIT");
        ccPaymentType = new VOPaymentType();
        ccPaymentType.setPaymentTypeId("CREDIT_CARD");
        ivPaymentType = new VOPaymentType();
        ivPaymentType.setPaymentTypeId("INVOICE");

        paymentType = new PaymentType();
        paymentType.setPsp(psp);

        initCustomer();

        voPaymentInfo = new VOPaymentInfo();
        voPaymentInfo.setPaymentType(ccPaymentType);
        voPaymentInfo.setKey(1);

        qs.org = customer;
        qs.supplier = supplier;
        im.customer = customer;
        im.query = qs;
        im.paymentInfo = doPi;
    }

    private void assertRequestData(RequestData requestData,
            String paymentInfoKey, String paymentType, int settingsSize) {
        assertEquals("locale:", "en", requestData.getCurrentUserLocale());
        assertEquals("externalid:", "externalId",
                requestData.getExternalIdentifier());
        assertEquals("organizationkey:", "1",
                String.valueOf(requestData.getOrganizationKey()));
        assertEquals("email:", "supplier_email",
                requestData.getOrganizationEmail());
        assertEquals("id:", "supplier_id", requestData.getOrganizationId());
        assertEquals("name:", "supplier_name",
                requestData.getOrganizationName());
        assertEquals("paymentinfokey:", paymentInfoKey,
                String.valueOf(requestData.getPaymentInfoKey()));
        assertEquals("paymentinfoid:", paymentType,
                requestData.getPaymentTypeId());
        assertEquals("pspid:", "pspid", requestData.getPspIdentifier());

        List<Property> properties = psps.getRequestData().getProperties();
        assertEquals("properties size:", settingsSize, properties.size());
        if (settingsSize > 0) {
            assertEquals("setting1", properties.get(0).getKey());
            assertEquals("value1", properties.get(0).getValue());
            assertEquals("setting2", properties.get(1).getKey());
            assertEquals("value2", properties.get(1).getValue());
            assertEquals("setting3", properties.get(2).getKey());
            assertEquals("value3", properties.get(2).getValue());
        }
    }

    private void assertChargingData(ChargingData chargingData,
            String totalAmount, boolean considerDiscount, String currency) {
        assertEquals("address:", "Address of organization 1000",
                chargingData.getAddress());
        assertEquals("currency:", currency, chargingData.getCurrency());
        assertEquals("customerkey:", "0",
                String.valueOf(chargingData.getCustomerKey()));
        assertEquals("email:", "testEmail@provider.domain",
                chargingData.getEmail());
        assertEquals("externalid:", "externalId",
                chargingData.getExternalIdentifier());
        if (considerDiscount) {
            assertEquals("netdiscount", "50", chargingData.getNetDiscount()
                    .toPlainString());
            assertEquals("vat", "312", chargingData.getVatAmount()
                    .toPlainString());
            assertEquals("vat%", "40", chargingData.getVat());
        }
        assertEquals("costs", totalAmount, chargingData.getGrossAmount()
                .toPlainString());
        assertEquals("endtime", "133456789",
                String.valueOf(chargingData.getPeriodEndTime().getTime()));
        assertEquals("starttime", "123456789",
                String.valueOf(chargingData.getPeriodStartTime().getTime()));
        assertEquals("supplierkey", "1",
                String.valueOf(chargingData.getSellerKey()));
        assertEquals("subscriptionid", "sub", chargingData.getSubscriptionId());
        assertEquals("pon", "12345", chargingData.getPon());

    }

    /**
     * Creates a payment information object with a payment type matching the
     * specified collection type and payment type id.
     * 
     * @param collectionType
     *            The collection type to be used.
     * @param paymentTypeId
     *            The payment type id to be set.
     * @return The payment info object considering the specified parameters.
     */
    private PaymentInfo createPaymentInfo(PaymentCollectionType collectionType,
            String paymentTypeId) {
        PaymentInfo pi = new PaymentInfo();
        PaymentType pt = new PaymentType();
        pt.setCollectionType(collectionType);
        pt.setPaymentTypeId(paymentTypeId);
        if (paymentTypeId.equals(PaymentInfoType.DIRECT_DEBIT.name())) {
            pt.setPaymentTypeId("DIRECT_DEBIT");
        }
        if (paymentTypeId.equals(PaymentInfoType.CREDIT_CARD.name())) {
            pt.setPaymentTypeId("CREDIT_CARD");
        }
        pi.setPaymentType(pt);
        pi.setOrganization(customer);
        pi.setPaymentInfoId("paymentInfoId");
        return pi;
    }

    private void setPaymentType(String paymentTypeString,
            PaymentInfo paymentInfo) {
        paymentInfo.setExternalIdentifier("externalId");
        paymentInfo.setPaymentType(paymentType);
        paymentType.setPaymentTypeId(paymentTypeString);

        if (BaseAdmUmTest.INVOICE.equals(paymentTypeString)) {
            paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
            paymentType.setPaymentTypeId("INVOICE");
            voPaymentInfo.setPaymentType(ivPaymentType);
            dm.setPSPPaymentId(null);
        } else {
            paymentType
                    .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
            if (PaymentType.CREDIT_CARD.equals(paymentTypeString)) {
                dm.setPSPPaymentId("CC");
                paymentType.setPaymentTypeId("CREDIT_CARD");
                voPaymentInfo.setPaymentType(ccPaymentType);
            } else if (PaymentType.DIRECT_DEBIT.equals(paymentTypeString)) {
                dm.setPSPPaymentId("DD");
                paymentType.setPaymentTypeId("DIRECT_DEBIT");
                voPaymentInfo.setPaymentType(ddPaymentType);
            }
        }

        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        ortpt.setPaymentType(paymentType);

        OrganizationReference orgRef = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        ortpt.setOrganizationReference(orgRef);

        dm.setPaymentInfo(paymentInfo);
        qs.setPaymentType(paymentType);
        qs.setPaymentInfo(paymentInfo);
    }

    private void setPSP(PaymentType paymentType) {
        PSP psp = new PSP();
        psp.setDistinguishedName("name");
        psp.setIdentifier("pspid");
        psp.setKey(1L);
        psp.setWsdlUrl("wsdl url");
        paymentType.setPsp(psp);
        qs.setPsp(psp);
        dm.setPsp(psp);

        PSPAccount pspAccount = new PSPAccount();
        pspAccount.setPsp(psp);
        pspAccount.setPspIdentifier("pspid");
        pspAccount.setKey(1L);
        qs.setPspAccount(pspAccount);
        dm.setPspAccount(pspAccount);
    }

    private void setPspSettings(PSP psp) {
        // setting 1
        PSPSetting pspSetting1 = new PSPSetting();
        pspSetting1.setKey(10L);
        pspSetting1.setPsp(psp);
        pspSetting1.setSettingKey("setting1");
        pspSetting1.setSettingValue("value1");
        qs.getPspSettings().add(pspSetting1);
        psp.addPSPSetting(pspSetting1);

        // setting 1
        PSPSetting pspSetting2 = new PSPSetting();
        pspSetting2.setKey(11L);
        pspSetting2.setPsp(psp);
        pspSetting2.setSettingKey("setting2");
        pspSetting2.setSettingValue("value2");
        qs.getPspSettings().add(pspSetting2);
        psp.addPSPSetting(pspSetting2);

        // setting 1
        PSPSetting pspSetting3 = new PSPSetting();
        pspSetting3.setKey(12L);
        pspSetting3.setPsp(psp);
        pspSetting3.setSettingKey("setting3");
        pspSetting3.setSettingValue("value3");
        qs.getPspSettings().add(pspSetting3);
        psp.addPSPSetting(pspSetting3);
    }

    private void initCustomer() {
        customer = new Organization();
        customer.setOrganizationId("pspTestCustomer");
        customer.setEmail("testEmail@provider.domain");
        customer.setName("Firma Elektroinstallation Meier");
        customer.setAddress("Hamstergasse 9c\n80123 München\nGermany");

        supplier = new Organization();
        supplier.setKey(1L);
        supplier.setName("Firma Supplier X");
        supplier.setOrganizationId("SUPPLIER_ID");

        OrganizationToRole supplierRelation = new OrganizationToRole();
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.SUPPLIER);
        supplierRelation.setOrganization(supplier);
        supplierRelation.setOrganizationRole(role);

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(supplierRelation);
        supplier.setGrantedRoles(roles);

        OrganizationReference ref = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        customer.getSources().add(ref);
        supplier.getTargets().add(ref);

        platformOperatorOrg = new Organization();
        platformOperatorOrg.setKey(1L);
        platformOperatorOrg
                .setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                        .name());
        OrganizationToRole plRelation = new OrganizationToRole();
        role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.PLATFORM_OPERATOR);
        plRelation.setOrganization(platformOperatorOrg);
        plRelation.setOrganizationRole(role);
        Set<OrganizationToRole> pl_roles = new HashSet<OrganizationToRole>();
        pl_roles.add(plRelation);
        platformOperatorOrg.setGrantedRoles(pl_roles);

        OrganizationReference orgRef = new OrganizationReference(
                platformOperatorOrg, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        supplier.getSources().add(orgRef);
        platformOperatorOrg.getTargets().add(orgRef);

        supplier.setSources(Arrays.asList(new OrganizationReference(
                platformOperatorOrg, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER)));

        role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.SUPPLIER);

        List<OrganizationRefToPaymentType> availablePayments = new ArrayList<OrganizationRefToPaymentType>();

        OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
        apt.setOrganizationReference(supplier.getSources().get(0));
        apt.setOrganizationRole(role);
        apt.setUsedAsDefault(false);
        PaymentType pt = new PaymentType();
        pt.setKey(1);
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        pt.setPaymentTypeId(BaseAdmUmTest.CREDIT_CARD);
        apt.setPaymentType(pt);
        availablePayments.add(apt);

        apt = new OrganizationRefToPaymentType();
        apt.setOrganizationReference(supplier.getSources().get(0));
        apt.setOrganizationRole(role);
        apt.setUsedAsDefault(false);
        pt = new PaymentType();
        pt.setKey(2);
        pt.setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        pt.setPaymentTypeId(BaseAdmUmTest.DIRECT_DEBIT);
        apt.setPaymentType(pt);
        availablePayments.add(apt);

        apt = new OrganizationRefToPaymentType();
        apt.setOrganizationReference(supplier.getSources().get(0));
        apt.setOrganizationRole(role);
        apt.setUsedAsDefault(false);
        pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.ORGANIZATION);
        pt.setPaymentTypeId(BaseAdmUmTest.INVOICE);
        apt.setPaymentType(pt);
        availablePayments.add(apt);
    }

    /**
     * Initializes the global customer and billing result fields.
     * 
     * @param setPSPIdentifier
     *            Indicates whether a pspIdentifier setting should be made for
     *            the organization or not.
     * @param supplierKey
     *            The key value for the supplier of the customer.
     * @param setDiscount
     *            Indicates whether to set discount information or not.
     * @param totalAmount
     *            value for the total amount in the billing result
     */
    private void initCustomerAndBillingResult(boolean setPSPIdentifier,
            long supplierKey, boolean setDiscount, String totalAmount) {

        supplier.setKey(supplierKey);
        supplier.setName("supplier_name");
        supplier.setEmail("supplier_email");
        supplier.setOrganizationId("supplier_id");
        // if (setPSPIdentifier) {
        // supplier.setPspIdentifier("pspIdentifier");
        // } else {
        // supplier.setPspIdentifier(null);
        // }

        String discountEntry = "";
        if (setDiscount) {
            discountEntry = "<Discount discountNetAmount=\"50\" netAmountAfterDiscount=\"100\" netAmountBeforeDiscount=\"200\"/>";
        }

        billingResult = spy(new BillingResult());
        billingResult.setChargingOrgKey(supplierKey);
        billingResult.setOrganizationTKey(customer.getKey());
        billingResult.setKey(1);
        billingResult.setPeriodStartTime(123456789L);
        billingResult.setPeriodEndTime(133456789L);
        billingResult
                .setResultXML(String
                        .format("<BillingDetails><Period endDate=\"1262300400000\" startDate=\"1259622000000\"/>"
                                + "<OrganizationDetails><Email>the customer's email</Email><Name>Name of organization 1000</Name>"
                                + "<Address>Address of organization 1000</Address></OrganizationDetails>"
                                + "<Subscriptions><Subscription id=\"sub\" purchaseOrderNumber=\"12345\">"
                                + "<PriceModels><PriceModel id=\"5\"><UsagePeriod endDate=\"1262300400000\" startDate=\"1259622000000\"/>"
                                + "<GatheredEvents/><PeriodFee basePeriod=\"MONTH\" basePrice=\"1000\" factor=\"1.0\" price=\"1000\"/>"
                                + "<UserAssignmentCosts basePeriod=\"MONTH\" basePrice=\"100\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
                                + "<PriceModelCosts amount=\"1000\"/></PriceModel></PriceModels>"
                                + "<SubscriptionCosts amount=\"1000\"/></Subscription>"
                                + "<Subscription id=\"sub2\" purchaseOrderNumber=\"\"><PriceModels>"
                                + "<PriceModel id=\"6\"><UsagePeriod endDate=\"1262300400000\" startDate=\"1260428424625\"/>"
                                + "<GatheredEvents/><PeriodFee basePeriod=\"WEEK\" basePrice=\"500\" factor=\"3.095197379298942\" price=\"1548\"/>"
                                + "<UserAssignmentCosts basePeriod=\"DAY\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/><PriceModelCosts amount=\"1548\"/></PriceModel></PriceModels>"
                                + "<SubscriptionCosts amount=\"1548\"/></Subscription>"
                                + "<Subscription id=\"sub2\" purchaseOrderNumber=\"\"><PriceModels><PriceModel id=\"6\">"
                                + "<UsagePeriod endDate=\"1260039564578\" startDate=\"1259622000000\"/><GatheredEvents/>"
                                + "<PeriodFee basePeriod=\"WEEK\" basePrice=\"500\" factor=\"0.6904176223544973\" price=\"345\"/>"
                                + "<UserAssignmentCosts basePeriod=\"DAY\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
                                + "<PriceModelCosts amount=\"345\"/></PriceModel></PriceModels>"
                                + "<SubscriptionCosts amount=\"345\"/></Subscription>"
                                + "<Subscription id=\"sub3\" purchaseOrderNumber=\"\"><PriceModels><PriceModel id=\"7\">"
                                + "<UsagePeriod endDate=\"1261335624781\" startDate=\"1259780424671\"/><GatheredEvents/>"
                                + "<PeriodFee basePeriod=\"DAY\" basePrice=\"500\" factor=\"18.000001273148147\" price=\"9000\"/>"
                                + "<UserAssignmentCosts basePeriod=\"WEEK\" basePrice=\"4\" factor=\"0.0\" numberOfUsersTotal=\"0\" price=\"0\"/>"
                                + "<PriceModelCosts amount=\"9000\">%s<VAT percent='40' amount='312'/></PriceModelCosts></PriceModel></PriceModels>"
                                + "<SubscriptionCosts amount=\"9000\"/>"
                                + "</Subscription></Subscriptions>"
                                + "<OverallCosts currency=\"EUR\" grossAmount=\"%s\">"
                                + "%s<VAT percent='40' amount='312'/></OverallCosts></BillingDetails>",
                                discountEntry, totalAmount, discountEntry));
        billingResult.setSubscriptionKey(Long.valueOf(12345L));
        billingResult.setCurrency(new SupportedCurrency("EUR"));
        billingResult.setGrossAmount(new BigDecimal(totalAmount));

        qs.setQueryResultList(new ArrayList<PaymentResult>());
    }

    @Test(expected = PaymentDataException.class)
    public void testDetermineRegistrationLink_PaymentTypeWrong()
            throws ObjectNotFoundException, PSPCommunicationException,
            PaymentDataException, OperationNotPermittedException {
        paymentType = new PaymentType();
        paymentType.setKey(voPaymentInfo.getPaymentType().getKey());
        paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        dm.setPaymentType(paymentType);
        pps.determineRegistrationLink(voPaymentInfo);
    }

    @Test(expected = PaymentDataException.class)
    public void testDetermineRegistrationLink_PaymentTypeIdMissing()
            throws ObjectNotFoundException, PSPCommunicationException,
            PaymentDataException, OperationNotPermittedException {
        voPaymentInfo.getPaymentType().setPaymentTypeId(null);
        pps.determineRegistrationLink(voPaymentInfo);
    }

    @Test
    public void testDetermineRegistrationLink() throws Exception {
        im.setUseCreditCard(true);
        String registrationLink = pps.determineRegistrationLink(voPaymentInfo);
        assertTrue(registrationLink.trim().length() > 0);
    }

    @Test
    public void testDetermineRegistrationLink_NoOrganizationEmail()
            throws Exception {
        // given
        im.setUseCreditCard(true);
        customer.setEmail(" ");
        dm.getCurrentUser().setEmail("info@fujitsu.de");

        // when
        pps.determineRegistrationLink(voPaymentInfo);

        // then
        assertTrue(psps.getRequestData().getOrganizationEmail()
                .equals(dm.getCurrentUser().getEmail()));
    }

    @Test
    public void testDetermineRegistrationLink_RequestData() throws Exception {
        pps.determineRegistrationLink(voPaymentInfo);

        RequestData requestData = psps.getRequestData();
        assertEquals("en", requestData.getCurrentUserLocale());
        assertEquals("extId", requestData.getExternalIdentifier());
        assertEquals("testEmail@provider.domain",
                requestData.getOrganizationEmail());
        assertEquals("pspTestCustomer", requestData.getOrganizationId());
        assertEquals("0", String.valueOf(requestData.getOrganizationKey()));
        assertEquals("Firma Elektroinstallation Meier",
                requestData.getOrganizationName());
        assertEquals("1", String.valueOf(requestData.getPaymentInfoKey()));
        assertEquals("CREDIT_CARD", requestData.getPaymentTypeId());
    }

    @Test
    public void testDetermineReregistrationLink_RequestData() throws Exception {
        pps.determineReregistrationLink(voPaymentInfo);

        RequestData requestData = psps.getRequestData();
        assertEquals("en", requestData.getCurrentUserLocale());
        assertEquals("extId", requestData.getExternalIdentifier());
        assertEquals("testEmail@provider.domain",
                requestData.getOrganizationEmail());
        assertEquals("pspTestCustomer", requestData.getOrganizationId());
        assertEquals("0", String.valueOf(requestData.getOrganizationKey()));
        assertEquals("Firma Elektroinstallation Meier",
                requestData.getOrganizationName());
        assertEquals("1", String.valueOf(requestData.getPaymentInfoKey()));
        assertEquals("CREDIT_CARD", requestData.getPaymentTypeId());
    }

    @Test
    public void testDetermineRegistrationLink_UnsupportedPaymentType()
            throws Exception {
        im.setUseCreditCard(false);
        im.setUseDirectDebit(false);
        im.setUseInvoice(true);
        dm.setPSPPaymentId(null);
        try {
            // iv
            pps.determineRegistrationLink(voPaymentInfo);
            Assert.fail("Call must fail, as invoice type is not supported with PSP");
        } catch (PaymentDataException e) {
            Assert.assertEquals("Wrong reason for exception",
                    "ex.PaymentDataException.PAYMENT_TYPE_UNSUPPORTED_BY_PSP",
                    e.getMessageKey());
        }
    }

    @Test
    public void testDetermineReregistrationLink_UnsupportedPaymentType()
            throws Exception {
        im.setUseCreditCard(false);
        im.setUseDirectDebit(false);
        im.setUseInvoice(true);
        dm.setPSPPaymentId(null);
        try {
            pps.determineReregistrationLink(voPaymentInfo);
            Assert.fail("Call must fail, as invoice type is not supported with PSP");
        } catch (PaymentDataException e) {
            Assert.assertEquals("Wrong reason for exception",
                    "ex.PaymentDataException.PAYMENT_TYPE_UNSUPPORTED_BY_PSP",
                    e.getMessageKey());
        }
    }

    @Test(expected = PaymentDataException.class)
    public void testDetermineRegistrationLink_MissingPaymentType()
            throws Exception {
        voPaymentInfo.setPaymentType(null);
        im.setInitializePaymentRequiredSettings(false);
        pps.determineRegistrationLink(voPaymentInfo);
    }

    @Test(expected = PaymentDataException.class)
    public void testDetermineReregistrationLink_MissingPaymentType()
            throws Exception {
        voPaymentInfo.setPaymentType(null);
        im.setInitializePaymentRequiredSettings(false);
        pps.determineReregistrationLink(voPaymentInfo);
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void testDetermineRegistration_NullParam() throws Exception {
        pps.determineRegistrationLink(null);
    }

    @Test(expected = org.oscm.internal.types.exception.IllegalArgumentException.class)
    public void testDetermineReregistration_NullParam() throws Exception {
        pps.determineReregistrationLink(null);
    }

    @Test(expected = PSPCommunicationException.class)
    public void testDetermineRegistrationLink_ThrowIoException()
            throws Exception {
        im.setUseCreditCard(true);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());
        throwIOException = true;

        pps.determineRegistrationLink(voPaymentInfo);
    }

    @Test(expected = PSPCommunicationException.class)
    public void testDetermineReregistrationLink_ThrowIoException()
            throws Exception {
        im.setUseCreditCard(true);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());
        throwIOException = true;

        pps.determineReregistrationLink(voPaymentInfo);
    }

    @Test
    public void testDetermineRegistrationLink_NonExistingPayment()
            throws Exception {
        HttpClientFactory.setTestMode(true);
        HttpMethodFactory.setTestMode(true);

        im.setUseCreditCard(true);
        dm.setFailForPaymentRetrieval(true);
        voPaymentInfo.setKey(0);
        pps.determineRegistrationLink(voPaymentInfo);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDetermineReregistrationLink_NonExistingPayment()
            throws Exception {
        HttpClientFactory.setTestMode(true);
        HttpMethodFactory.setTestMode(true);

        im.setUseCreditCard(true);
        dm.setFailForPaymentRetrieval(true);
        pps.determineReregistrationLink(voPaymentInfo);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDetermineRegistrationLinkWrongCaller_PaymentInfo()
            throws Exception {
        im.setUseCreditCard(true);
        dm.setWrongPaymentUser(true);
        pps.determineReregistrationLink(voPaymentInfo);
    }

    @Test
    public void testReinvokePaymentProcessing() {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);

        boolean result = pps.reinvokePaymentProcessing();

        assertTrue("Call for no actions must not cause a negative result",
                result);
    }

    @Test
    public void testReinvokePaymentProcessing_OneHit() throws Exception {
        initBillingAndPaymentResult();
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());
        boolean result = pps.reinvokePaymentProcessing();

        assertTrue("Call for no actions must not cause a negative result",
                result);
        PaymentResult prChanged = (PaymentResult) dm.getPersistedObject();
        Assert.assertEquals("Wrong status for re-processed payment result",
                PaymentProcessingStatus.SUCCESS,
                prChanged.getProcessingStatus());
    }

    @Test
    public void testReinvokePaymentProcessing_OneHitFailure() throws Exception {
        initBillingAndPaymentResult();
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);

        boolean result = pps.reinvokePaymentProcessing();

        assertFalse("Call must cause a negative result", result);
        PaymentResult prChanged = (PaymentResult) dm.getPersistedObject();
        Assert.assertEquals("Wrong status for re-processed payment result",
                PaymentProcessingStatus.FAILED_INTERNAL,
                prChanged.getProcessingStatus());
    }

    /**
     * Creates a billing result and a payment result object that reference each
     * other.
     */
    private void initBillingAndPaymentResult() {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentResult pr = new PaymentResult();
        pr.setProcessingStatus(PaymentProcessingStatus.RETRY);
        pr.setProcessingTime(System.currentTimeMillis());
        pr.setBillingResult(billingResult);
        billingResult.setPaymentResult(pr);
        List<PaymentResult> initialPrs = new ArrayList<PaymentResult>();
        initialPrs.add(pr);
        qs.setQueryResultList(initialPrs);
    }

    @Test
    public void testChargeCustomerPSPUsageDisabled() throws Exception {
        ic.setPSPUsageEnabled(false);
        initCustomerAndBillingResult(true, 1L, false, "1030");

        pps.chargeCustomer(billingResult);

        // must have passed without exception, no payment result object must
        // have been generated for the billing result object.
        Assert.assertNull("No payment result must be there",
                billingResult.getPaymentResult());
    }

    @Test
    public void testDeregisterPaymentInPSPSystem_Null() throws Exception {
        pps.deregisterPaymentInPSPSystem(null);
        assertNull(psps.getRequestData());
    }

    @Test
    public void testDeregisterPaymentInPSPSystem_DirectDebit() throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.DIRECT_DEBIT.name());
        pi.getOrganization().setKey(1L);
        pi.getOrganization().setEmail("supplier_email");
        pi.getOrganization().setOrganizationId("supplier_id");
        pi.getOrganization().setName("supplier_name");
        pi.setKey(1);
        setPaymentType("DIRECT_DEBIT", pi);
        setPSP(pi.getPaymentType());
        setPspSettings(pi.getPaymentType().getPsp());

        // EXECUTE
        pps.deregisterPaymentInPSPSystem(pi);

        // ASSERT
        assertRequestData(psps.getRequestData(), "1", "DIRECT_DEBIT", 3);
    }

    @Test
    public void testDeregisterPaymentInPSPSystem_CreditCard() throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        pi.getOrganization().setKey(1L);
        pi.getOrganization().setEmail("supplier_email");
        pi.getOrganization().setOrganizationId("supplier_id");
        pi.getOrganization().setName("supplier_name");
        pi.setKey(1);
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());
        setPspSettings(pi.getPaymentType().getPsp());

        // EXECUTE
        pps.deregisterPaymentInPSPSystem(pi);

        // ASSERT
        assertRequestData(psps.getRequestData(), "1", "CREDIT_CARD", 3);
    }

    @Test(expected = PaymentDeregistrationException.class)
    public void testDeregisterPaymentInPSPSystemNegative() throws Exception {
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        initCustomerAndBillingResult(true, 1L, false, "1030");

        pi.setExternalIdentifier("externalIdentifier");
        pi.setKey(-1);
        pps.deregisterPaymentInPSPSystem(pi);
    }

    // tests bug 5297
    @Test
    public void testChargeCustomerVerifyEmailAddressForBilling()
            throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());
        customer.setEmail("newMail@customer.org");// change the customer's
                                                  // billing email

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        Assert.assertEquals("Wrong email address for customer",
                customer.getEmail(), psps.getChargingData().getEmail());
    }

    @Test
    public void testChargeCustomerCreditCard() throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "12.26");
        im.setUseDirectDebit(false);
        im.setUseCreditCard(true);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        assertChargingData(psps.getChargingData(), "12.26", false, "EUR");
        assertRequestData(psps.getRequestData(), "0", "CREDIT_CARD", 0);
    }

    @Test
    public void testChargeCustomerCreditCard_AmountGreaterThousand()
            throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "50110.30");
        im.setUseDirectDebit(false);
        im.setUseCreditCard(true);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        assertChargingData(psps.getChargingData(), "50110.30", false, "EUR");
        assertRequestData(psps.getRequestData(), "0", "CREDIT_CARD", 0);
    }

    @Test
    public void testChargeCustomer_CreditCardNoSubscription() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        billingResult.setSubscriptionKey(null);
        im.setUseDirectDebit(false);
        im.setUseCreditCard(true);

        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());

        setPaymentType("CREDIT_CARD", pi);
        assertFalse(pps.chargeCustomer(billingResult));
    }

    @Test
    public void testChargeCustomerCreditCardValidateDiscountForNoDiscount()
            throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        Assert.assertNull("No discount set, so no information must be given",
                psps.getChargingData().getNetDiscount());
    }

    @Test
    public void testChargeCustomerCreditCardValidateDiscountForExistingDiscount()
            throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, true, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        Assert.assertEquals(
                "Discount set, so the correct information must be returned",
                "50", psps.getChargingData().getNetDiscount().toPlainString());

    }

    @Test
    public void testChargeCustomerCreditCardNoOrgHistory() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        qs.setReturnOrganizationHistoryEntries(false);
        boolean result = pps.chargeCustomer(billingResult);
        Assert.assertFalse("Method call must fail", result);

        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        Assert.assertEquals("Wrong processing status",
                PaymentProcessingStatus.FAILED_INTERNAL,
                ps.getProcessingStatus());
        Assert.assertTrue(
                "Wrong exception in payment processing result",
                ps.getProcessingException().contains(
                        PSPProcessingException.class.getName()));
    }

    @Test
    public void testChargeCustomerCreditCardNoPaymentHistory() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        qs.setReturnPaymentInfoHistoryEntries(false);
        boolean result = pps.chargeCustomer(billingResult);
        Assert.assertFalse("Method call must fail", result);

        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        Assert.assertEquals("Wrong processing status",
                PaymentProcessingStatus.FAILED_INTERNAL,
                ps.getProcessingStatus());
        Assert.assertTrue(
                "Wrong exception in payment processing result",
                ps.getProcessingException().contains(
                        PSPProcessingException.class.getName()));
    }

    @Test
    public void testChargeCustomerDirectDebit() throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "12.26");
        im.setUseDirectDebit(true);
        im.setUseCreditCard(false);
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.DIRECT_DEBIT.name());
        setPaymentType("DIRECT_DEBIT", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        assertRequestData(psps.getRequestData(), "0", "DIRECT_DEBIT", 0);
        assertChargingData(psps.getChargingData(), "12.26", false, "EUR");
    }

    @Test
    public void testChargeCustomer_Invoice() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");

        setPaymentType(
                BaseAdmUmTest.INVOICE,
                createPaymentInfo(PaymentCollectionType.ORGANIZATION,
                        PaymentInfoType.INVOICE.name()));
        pps.chargeCustomer(billingResult);

        assertNull(
                "No payment result must have been created for the billing result, as the customer uses invoice as payment type.",
                billingResult.getPaymentResult());
    }

    @Test
    public void testChargeCustomerDirectDebit_USDCurrency() throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "12.26");
        im.setUseDirectDebit(true);
        im.setUseCreditCard(false);
        String newCurrencyCode = "USD";
        billingResult.setResultXML(billingResult.getResultXML().replace("EUR",
                newCurrencyCode));
        billingResult.setCurrency(new SupportedCurrency(newCurrencyCode));
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.DIRECT_DEBIT.name());
        setPaymentType("DIRECT_DEBIT", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        assertRequestData(psps.getRequestData(), "0", "DIRECT_DEBIT", 0);
        assertChargingData(psps.getChargingData(), "12.26", false, "USD");
    }

    @Test
    public void testChargeCustomerInvoice() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.INVOICE.name());
        setPaymentType("INVOICE", pi);

        pps.chargeCustomer(billingResult);

        // ASSERTS
        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        Assert.assertNull(
                "No payment result must be created, as charging took place", ps);
    }

    @Test
    public void testChargeCustomerDuplicateCallForBillingResult()
            throws Exception {
        // SETUP
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.DIRECT_DEBIT.name());
        setPaymentType("DIRECT_DEBIT", pi);
        setPSP(pi.getPaymentType());
        pps.chargeCustomer(billingResult);

        // EXECUTE
        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        long timeBeforeSecondInvocation = System.currentTimeMillis();
        pps.chargeCustomer(billingResult);

        // ASSERT
        // no processing must have taken place now, so ensure nothing has been
        // sent to the psp
        Assert.assertTrue("Wrong processing time for payment result",
                ps.getProcessingTime() < timeBeforeSecondInvocation);
    }

    @Test
    public void testChargeCustomerThrowIOException() throws Exception {
        // SETUP
        throwIOException = true;
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        setPSP(pi.getPaymentType());

        // EXECUTE
        pps.chargeCustomer(billingResult);

        // ASSERT
        Assert.assertEquals("Wrong status for payment result",
                PaymentProcessingStatus.RETRY, billingResult.getPaymentResult()
                        .getProcessingStatus());
        Assert.assertNull("No processing result must be set", billingResult
                .getPaymentResult().getProcessingResult());
        Assert.assertTrue("Wrong stack trace", billingResult.getPaymentResult()
                .getProcessingException().contains("IOException"));
    }

    @Test
    public void testChargeCustomer_MultiplePriceModels() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.DIRECT_DEBIT.name());
        setPaymentType("DIRECT_DEBIT", pi);
        setPSP(pi.getPaymentType());
        setPspSettings(pi.getPaymentType().getPsp());
        billingResult.setResultXML(sampleBillingResultMultiplePriceModels);
        BigDecimal newGrossAmount = BigDecimal.valueOf(13815);
        billingResult.setGrossAmount(newGrossAmount);

        pps.chargeCustomer(billingResult);

        assertChargingData(psps.getChargingData(), newGrossAmount.toString(),
                false, "EUR");
        assertRequestData(psps.getRequestData(), "0", "DIRECT_DEBIT", 3);
    }

    @Test
    public void testChargeCustomer_EmptyBillingResultData() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        billingResult.setResultXML("\r\n");

        assertTrue(pps.chargeCustomer(billingResult));
        assertNull(psps.getChargingData());
        assertNull(psps.getRequestData());
    }

    @Test
    public void testChargeCustomer_NoCosts() throws Exception {
        initCustomerAndBillingResult(true, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);
        String resultXML = billingResult.getResultXML();
        BigDecimal newGrossAmount = BigDecimal.ZERO;
        resultXML = resultXML.replace(
                "<OverallCosts currency=\"EUR\" grossAmount=\"1030\">",
                "<OverallCosts currency=\"EUR\" grossAmount=\""
                        + newGrossAmount + "\">");
        billingResult.setResultXML(resultXML);
        billingResult.setGrossAmount(newGrossAmount);

        assertTrue(pps.chargeCustomer(billingResult));
        assertNull(psps.getChargingData());
        assertNull(psps.getRequestData());
    }

    @Test
    public void testChargeCustomerNoSupplierForCustomer() throws Exception {
        initCustomerAndBillingResult(false, -1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);

        boolean success = pps.chargeCustomer(billingResult);

        Assert.assertFalse(
                "Operation must fail as no psp identifier is set for the supplier",
                success);
        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        Assert.assertEquals("Wrong processing status",
                PaymentProcessingStatus.FAILED_INTERNAL,
                ps.getProcessingStatus());
        Assert.assertTrue(
                "Wrong exception in payment processing result",
                ps.getProcessingException().contains(
                        PSPProcessingException.class.getName()));
    }

    @Test
    public void testChargeCustomerNoPSPIdentifier() throws Exception {
        // SETUP
        initCustomerAndBillingResult(false, 1L, false, "1030");
        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);

        // EXECUTE
        boolean success = pps.chargeCustomer(billingResult);

        // ASSERT
        Assert.assertFalse(
                "Operation must fail as no psp identifier is set for the supplier",
                success);
        PaymentResult ps = (PaymentResult) (dm.getPersistedObject());
        Assert.assertEquals("Wrong processing status",
                PaymentProcessingStatus.FAILED_INTERNAL,
                ps.getProcessingStatus());
        Assert.assertTrue(
                "Wrong exception in payment processing result",
                ps.getProcessingException().contains(
                        PSPIdentifierForSellerException.class.getName()));
    }

    @Test
    public void testChargeCustomer_RequestData() {
        // setup: supplier
        initCustomerAndBillingResult(true, 1L, false, "1030");

        // setup: payment info
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setKey(123L);
        setPaymentType(PaymentType.CREDIT_CARD, paymentInfo);
        setPSP(paymentInfo.getPaymentType());

        // setup: psp settings
        setPspSettings(paymentInfo.getPaymentType().getPsp());

        // EXECUTE: chargeCustomer
        pps.chargeCustomer(billingResult);

        // assert: request data
        assertRequestData(psps.getRequestData(), "123", "CREDIT_CARD", 3);
    }

    @Test
    public void testChargeCustomer_ChargingData() {
        // setup: supplier
        initCustomerAndBillingResult(true, 1L, true, "1030");

        // setup: payment info
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setKey(123L);
        setPaymentType(PaymentType.CREDIT_CARD, paymentInfo);
        setPSP(paymentInfo.getPaymentType());

        // setup: psp settings
        setPspSettings(paymentInfo.getPaymentType().getPsp());

        // EXECUTE: chargeCustomer
        pps.chargeCustomer(billingResult);

        // assert: charging data
        assertChargingData(psps.getChargingData(), "1030", true, "EUR");
    }

    @Test
    public void testChargeCustomer_getRequestData_B10207() {
        // given a supplier with credit card, German current user
        initCustomerAndBillingResult(true, 1L, true, "1030");

        givenPSPSetupWithCreditCard();

        dm = givenDataServiceMock();

        givenCurrentUserGermanLocale();

        // when chargeCustomer
        pps.chargeCustomer(billingResult);

        // then no getCurrentUser invocation and request data uses fix locale
        verify(dm, never()).getCurrentUser();
        verify(dm, atLeastOnce()).createNamedQuery(Matchers.anyString());
        
        assertEquals("de", pps.dm.getCurrentUser().getLocale());
        assertEquals("en", psps.getRequestData().getCurrentUserLocale());

    }

    @Test
    public void testChargeCustomer_PersistBillingResult_WithoutPaymentResultChanged_() {
        initCustomerAndBillingResult(true, 1L, true, "1031");

        PaymentInfo pi = createPaymentInfo(
                PaymentCollectionType.PAYMENT_SERVICE_PROVIDER,
                PaymentInfoType.CREDIT_CARD.name());
        setPaymentType("CREDIT_CARD", pi);

        // force the paymentResult unnecessary
        billingResult.setGrossAmount(new BigDecimal(0));

        boolean success = pps.chargeCustomer(billingResult);

        Assert.assertTrue("Operation must pass", success);

        verify(billingResult, atMost(1)).setPaymentResult(
                any(PaymentResult.class));

    }

    private void givenPSPSetupWithCreditCard() {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setKey(123L);
        setPaymentType(PaymentType.CREDIT_CARD, paymentInfo);
        setPSP(paymentInfo.getPaymentType());
        setPspSettings(paymentInfo.getPaymentType().getPsp());
    }

    private void givenCurrentUserGermanLocale() {
        currentUserLocale = Locale.GERMAN.getLanguage();
    }

    private DataServiceStub givenDataServiceMock() {
        DataServiceStub dm = Mockito.spy((DataServiceStub) pps.dm);
        pps.dm = dm;
        return dm;
    }
}
