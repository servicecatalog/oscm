/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                            
 *                                                                              
 *  Creation Date: 14.09.2012                                                      
 *                                                                                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

@SuppressWarnings("boxing")
public class MpOwnerShareResultAssemblerTest {

    private static final List<OrganizationRole> BROKER_ROLE = Collections
            .singletonList(new OrganizationRole(OrganizationRoleType.BROKER));

    private static final List<OrganizationRole> RESELLER_ROLE = Collections
            .singletonList(new OrganizationRole(OrganizationRoleType.RESELLER));

    private static final List<OrganizationRole> SUPPLIER_ROLE = Collections
            .singletonList(new OrganizationRole(OrganizationRoleType.SUPPLIER));

    SharesDataRetrievalServiceLocal dao = mock(SharesDataRetrievalServiceLocal.class);
    MpOwnerShareResultAssembler assembler = new MpOwnerShareResultAssembler(
            dao, null);

    @Before
    public void before() {
        assembler.result = new MarketplaceOwnerRevenueShareResult();
        assembler.xmlSearch = mock(XmlSearch.class);
        when(dao.loadSupplierHistoryOfProduct(anyLong())).thenReturn(
                new OrganizationHistory());

        when(dao.loadOperatorRevenueSharePercentage(anyLong(), anyLong()))
                .thenReturn(BigDecimal.TEN);

        when(
                assembler.billingRetrievalService
                        .getSupportedCountryCode(anyLong())).thenReturn("US");
    }

    @Test
    public void build_setMarketpleOwnerData() throws Exception {

        // given
        OrganizationHistory persistedOrg = new OrganizationHistory();
        persistedOrg.setObjKey(4711L);
        persistedOrg.getDataContainer().setOrganizationId("myOrgId");
        given(dao.loadLastOrganizationHistory(anyLong())).willReturn(
                persistedOrg);

        // when
        assembler.setMarketpleOwnerData(5L);

        // then
        assertThat(assembler.result.getOrganizationData().getCountryIsoCode(),
                equalTo("US"));
        assertThat(assembler.result.getOrganizationId(), equalTo("myOrgId"));
        assertThat(assembler.result.getOrganizationKey(),
                equalTo(BigInteger.valueOf(4711)));
    }

    /**
     * The period start and end times are UTC times
     * 
     * @throws Exception
     */
    @Test
    public void setPeriod() throws Exception {

        // given
        long givenStartPeriod = 0;
        long givenEndPeriod = 1347867425534L;

        // when
        assembler.setPeriod(givenStartPeriod, givenEndPeriod);

        // then
        Period period = assembler.result.getPeriod();
        assertEquals(givenStartPeriod, period.getStartDate().longValue());
        assertEquals("1970-01-01T00:00:00.000Z", period.getStartDateIsoFormat()
                .toString());
        assertEquals(givenEndPeriod, period.getEndDate().longValue());
        assertEquals("2012-09-17T07:37:05.534Z", period.getEndDateIsoFormat()
                .toString());
    }

    @Test
    public void setPeriod_sameFormatingWithDiffentLocales() throws Exception {

        // given
        long givenStartPeriod = 0;

        // when
        Locale.setDefault(Locale.ENGLISH);
        assembler.setPeriod(givenStartPeriod, 1347867425534L);
        String dateWithEnglishLocale = assembler.result.getPeriod()
                .getStartDateIsoFormat().toString();
        Locale.setDefault(Locale.GERMAN);
        assembler.setPeriod(givenStartPeriod, 1347867425534L);
        String dateWithGermanLocale = assembler.result.getPeriod()
                .getStartDateIsoFormat().toString();

        // then
        assertEquals(dateWithEnglishLocale, dateWithGermanLocale);
    }

    @Test
    public void addCurrency() throws Exception {

        // given
        String givenCurrency = "EUR";

        // when
        assembler.addCurrency(givenCurrency);

        // then
        assertEquals(assembler.result.getCurrency().get(0).getId(),
                givenCurrency);
    }

    @Test
    public void addMarketplace() throws Exception {

        // given
        Currency givenCurrency = new Currency();
        MarketplaceHistory mp = new MarketplaceHistory();
        mp.getDataContainer().setMarketplaceId("marketplaceId");
        mp.setObjKey(123);
        given(dao.loadMarketplaceRevenueSharePercentage(anyLong(), anyLong()))
                .willReturn(BigDecimal.TEN);

        // when
        assembler.addMarketplace(mp, givenCurrency);

        // then
        Marketplace constructedMarketplace = givenCurrency.getMarketplace()
                .get(0);
        assertEquals("marketplaceId", constructedMarketplace.getId());
        assertEquals(123, constructedMarketplace.getKey().intValue());
        assertEquals(BigDecimal.TEN,
                constructedMarketplace.getRevenueSharePercentage());
    }

    @Test
    public void buildOrganizationData() {

        // given
        OrganizationHistory givenOrganization = new OrganizationHistory();
        givenOrganization.setObjKey(7);
        givenOrganization.getDataContainer().setOrganizationId("orgId");
        givenOrganization.getDataContainer().setEmail("email@server.com");
        givenOrganization.getDataContainer().setName("org name");
        givenOrganization.getDataContainer().setAddress("org address");

        // when
        OrganizationData constructedOrgData = assembler
                .buildOrganizationData(givenOrganization);

        // then
        assertEquals("US", constructedOrgData.getCountryIsoCode());
        assertEquals("orgId", constructedOrgData.getId());
        assertEquals("email@server.com", constructedOrgData.getEmail());
        assertEquals("org name", constructedOrgData.getName());
        assertEquals(7, constructedOrgData.getKey().intValue());
        assertEquals("org address", constructedOrgData.getAddress());
    }

    @Test
    public void buildService() throws Exception {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setObjKey(7);
        givenProduct.setTemplateObjKey(3000L);
        givenProduct.getDataContainer().setProductId("prodId");

        // when
        Service constructedSerive = assembler.buildService(givenProduct);

        // then
        assertEquals(7, constructedSerive.getKey().intValue());
        assertEquals("prodId", constructedSerive.getId());
        assertEquals("US", constructedSerive.getSupplier()
                .getOrganizationData().getCountryIsoCode());
    }

    @Test
    public void buildService_cleanProductId() throws Exception {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setObjKey(7);
        givenProduct.setTemplateObjKey(3000L);
        givenProduct.getDataContainer().setProductId("prodId#645646");

        // when
        Service constructedSerive = assembler.buildService(givenProduct);

        // then
        assertEquals(7, constructedSerive.getKey().intValue());
        assertEquals("prodId", constructedSerive.getId());
        assertEquals("US", constructedSerive.getSupplier()
                .getOrganizationData().getCountryIsoCode());
    }

    @Test
    public void buildService_supplierData() throws Exception {

        // given
        OrganizationHistory givenSupplier = new OrganizationHistory();
        givenSupplier.getDataContainer().setOrganizationId("supplierId");
        when(dao.loadSupplierHistoryOfProduct(anyLong())).thenReturn(
                givenSupplier);
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setTemplateObjKey(3000L);

        // when
        OrganizationData constructedSupplier = assembler
                .buildService(givenProduct).getSupplier().getOrganizationData();

        // then
        assertEquals("supplierId", constructedSupplier.getId());
        assertEquals("US", constructedSupplier.getCountryIsoCode());
    }

    @Test
    public void addService_asBroker() throws Exception {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setTemplateObjKey(7L);
        OrganizationHistory givenBroker = new OrganizationHistory();
        givenBroker.getDataContainer().setOrganizationId("brokerId");
        when(dao.loadOrganizationHistoryRoles(anyLong(), anyLong()))
                .thenReturn(BROKER_ROLE);
        when(dao.loadLastOrganizationHistory(anyLong()))
                .thenReturn(givenBroker);

        // when
        Service constructedSerive = assembler.buildService(givenProduct);

        // then
        assertEquals(OfferingType.BROKER, constructedSerive.getModel());
        assertEquals(7, constructedSerive.getTemplateKey().intValue());
        assertNull(constructedSerive.getReseller());
        assertEquals("brokerId", constructedSerive.getBroker()
                .getOrganizationData().getId());
        assertEquals("US", constructedSerive.getBroker().getOrganizationData()
                .getCountryIsoCode());
    }

    @Test
    public void addService_asReseller() throws Exception {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setTemplateObjKey(7L);
        OrganizationHistory givenReseller = new OrganizationHistory();
        givenReseller.getDataContainer().setOrganizationId("resellerId");
        when(dao.loadOrganizationHistoryRoles(anyLong(), anyLong()))
                .thenReturn(RESELLER_ROLE);
        when(dao.loadLastOrganizationHistory(anyLong())).thenReturn(
                givenReseller);

        // when
        Service constructedSerive = assembler.buildService(givenProduct);

        // then
        assertEquals(OfferingType.RESELLER, constructedSerive.getModel());
        assertEquals(7, constructedSerive.getTemplateKey().intValue());
        assertNull(constructedSerive.getBroker());
        assertEquals("resellerId", constructedSerive.getReseller()
                .getOrganizationData().getId());
        assertEquals("US", constructedSerive.getReseller()
                .getOrganizationData().getCountryIsoCode());
    }

    @Test
    public void addService_asSupplier() throws Exception {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setTemplateObjKey(7L);
        OrganizationHistory givenSupplier = new OrganizationHistory();
        givenSupplier.getDataContainer().setOrganizationId("supplierId");
        when(dao.loadOrganizationHistoryRoles(anyLong(), anyLong()))
                .thenReturn(SUPPLIER_ROLE);
        when(dao.loadLastOrganizationHistory(anyLong())).thenReturn(
                givenSupplier);

        // when
        Service constructedSerive = assembler.buildService(givenProduct);

        // then
        assertEquals(OfferingType.DIRECT, constructedSerive.getModel());
        assertNull(constructedSerive.getTemplateKey());
        assertNull(constructedSerive.getReseller());
        assertNull(constructedSerive.getBroker());
    }

    @Test
    public void addSellersToRevenueSummary_directService() throws Exception {

        // given a directly offered service
        Service givenService = buildDirectService();
        Marketplace marketplace = buildMarketplace();
        marketplace.addService(givenService);
        Currency currency = buildCurrency();
        currency.addMarketplace(marketplace);

        // when
        assembler.addSellersToRevenueSummary(givenService, marketplace,
                currency);
        currency.calculate();

        // then the supplier must be added to the organization lists
        RevenuesPerMarketplace revenuePerMarketplace = marketplace
                .getRevenuesPerMarketplace();
        assertEquals("supplierId", revenuePerMarketplace.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertThat(revenuePerMarketplace.getBrokers().getOrganization(),
                hasNoItems());
        assertThat(revenuePerMarketplace.getResellers().getOrganization(),
                hasNoItems());

        assertEquals(new BigDecimal("511.00"), revenuePerMarketplace
                .getSuppliers().getAmount());

        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();
        assertEquals("supplierId", revenuesOverAllMarketplaces.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertThat(revenuesOverAllMarketplaces.getBrokers().getOrganization(),
                hasNoItems());
        assertThat(
                revenuesOverAllMarketplaces.getResellers().getOrganization(),
                hasNoItems());
    }

    @Test
    public void addSellersToRevenueSummary_directServiceWithDiscount()
            throws Exception {

        // given a directly offered service with discount
        Service givenService = buildDirectServiceWithDiscount();
        Marketplace marketplace = buildMarketplace();
        marketplace.addService(givenService);
        Currency currency = buildCurrency();
        currency.addMarketplace(marketplace);

        // when
        assembler.addSellersToRevenueSummary(givenService, marketplace,
                currency);
        currency.calculate();

        // then revenues for the marketplace must take the discount into the
        // consideration
        RevenuesPerMarketplace revenuePerMarketplace = marketplace
                .getRevenuesPerMarketplace();

        assertEquals(new BigDecimal("365.00"), revenuePerMarketplace
                .getSuppliers().getTotalAmount());
        assertEquals(new BigDecimal("36.50"), revenuePerMarketplace
                .getSuppliers().getMarketplaceRevenue());

        assertEquals(new BigDecimal("255.50"), revenuePerMarketplace
                .getSuppliers().getAmount());

        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();
        assertEquals(new BigDecimal("365.00"), revenuesOverAllMarketplaces
                .getSuppliers().getTotalAmount());
        assertEquals(new BigDecimal("36.50"), revenuesOverAllMarketplaces
                .getSuppliers().getMarketplaceRevenue());

        assertEquals(new BigDecimal("255.50"), revenuesOverAllMarketplaces
                .getSuppliers().getAmount());

    }

    @Test
    public void updateServiceRevenueShareDetails_directServiceWithDiscount()
            throws Exception {

        // given a directly offered service with discount
        Service givenService = buildDirectServiceWithDiscount();
        Marketplace marketplace = buildMarketplace();
        marketplace.addService(givenService);
        Currency currency = buildCurrency();
        currency.addMarketplace(marketplace);

        // when
        assembler.updateServiceRevenueShareDetails(givenService, marketplace);

        // then
        RevenueShareDetails revenueShareDetails = givenService
                .getRevenueShareDetails();

        assertEquals(new BigDecimal("1095.00"),
                revenueShareDetails.getServiceRevenue());
        assertEquals(new BigDecimal("20.0"),
                revenueShareDetails.getMarketplaceRevenueSharePercentage());
        assertEquals(new BigDecimal("219.00"),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(new BigDecimal("20.0"),
                revenueShareDetails.getOperatorRevenueSharePercentage());
        assertEquals(new BigDecimal("219.00"),
                revenueShareDetails.getOperatorRevenue());
        assertEquals(new BigDecimal("657.00"),
                revenueShareDetails.getAmountForSupplier());

    }

    @Test
    public void addSellersToRevenueSummary_brokerService() throws Exception {

        // given a service offered by a broker
        Service givenService = buildBrokerService();
        Marketplace marketplace = buildMarketplace();
        marketplace.addService(givenService);
        Currency currency = buildCurrency();
        currency.addMarketplace(marketplace);

        // when
        assembler.addSellersToRevenueSummary(givenService, marketplace,
                currency);
        currency.calculate();

        // then supplier and broker must be added to organization lists
        RevenuesPerMarketplace revenuePerMarketplace = marketplace
                .getRevenuesPerMarketplace();
        assertEquals("supplierId", revenuePerMarketplace.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertEquals("brokerId", revenuePerMarketplace.getBrokers()
                .getOrganization().get(0).getIdentifier());
        assertEquals("name", revenuePerMarketplace.getBrokers()
                .getOrganization().get(0).getName());
        assertThat(revenuePerMarketplace.getResellers().getOrganization(),
                hasNoItems());

        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();
        assertEquals("supplierId", revenuesOverAllMarketplaces.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertEquals("brokerId", revenuesOverAllMarketplaces.getBrokers()
                .getOrganization().get(0).getIdentifier());
        assertEquals("name", revenuesOverAllMarketplaces.getBrokers()
                .getOrganization().get(0).getName());
        assertThat(
                revenuesOverAllMarketplaces.getResellers().getOrganization(),
                hasNoItems());
    }

    @Test
    public void addSellersToRevenueSummary_resellerService() throws Exception {

        // given a service offered by a reseller
        Service givenService = buildResellerService();
        Marketplace marketplace = buildMarketplace();
        marketplace.addService(givenService);
        Currency currency = buildCurrency();
        currency.addMarketplace(marketplace);

        // when
        assembler.addSellersToRevenueSummary(givenService, marketplace,
                currency);
        currency.calculate();

        // then supplier and reseller must be added to organization lists
        RevenuesPerMarketplace revenuePerMarketplace = marketplace
                .getRevenuesPerMarketplace();
        assertEquals("supplierId", revenuePerMarketplace.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertThat(revenuePerMarketplace.getBrokers().getOrganization(),
                hasNoItems());
        assertEquals("resellerId", revenuePerMarketplace.getResellers()
                .getOrganization().get(0).getIdentifier());

        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();
        assertEquals("supplierId", revenuesOverAllMarketplaces.getSuppliers()
                .getOrganization().get(0).getIdentifier());
        assertThat(revenuesOverAllMarketplaces.getBrokers().getOrganization(),
                hasNoItems());
        assertEquals("resellerId", revenuesOverAllMarketplaces.getResellers()
                .getOrganization().get(0).getIdentifier());
    }

    /**
     * attribute values that have null value should not be rendered as 0.00
     */
    @Test
    public void serializeRevenueShareDetails() throws Exception {
        // given
        RevenueShareDetails revenueDetails = new RevenueShareDetails();

        // when
        String xmlAsString = serialize(revenueDetails);
        System.out.println(xmlAsString);

        // then xml does not contain null values
        Document doc = XMLConverter.convertToDocument(xmlAsString, false);
        Node revenueDetailsNode = XMLConverter.getNodeByXPath(doc,
                "/RevenueShareDetails");
        String value = XMLConverter.getStringAttValue(revenueDetailsNode,
                "amountForSupplier");
        assertNull(value);
    }

    private String serialize(Object jaxbTree) throws JAXBException,
            PropertyException {
        JAXBContext jc = JAXBContext.newInstance(jaxbTree.getClass());
        System.out.println(jc.getClass().getCanonicalName());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(jaxbTree, bos);
        String xmlAsString = Strings.toString(bos.toByteArray());
        return xmlAsString;
    }

    private Service buildDirectService() {
        Service service = new Service();
        service.setModel(OfferingType.DIRECT);
        Supplier supplier = new Supplier();
        service.setSupplier(supplier);
        OrganizationData orgData = new OrganizationData();
        supplier.setOrganizationData(orgData);
        orgData.setId("supplierId");
        orgData.setCountryIsoCode("US");
        orgData.setKey(BigInteger.valueOf(1L));

        RevenueShareDetails revenueShareDetails = new RevenueShareDetails();
        revenueShareDetails.setServiceRevenue(BigDecimal.valueOf(730.00));
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(BigDecimal.valueOf(730.00));
        doReturn(mockNetAmounts).when(assembler.xmlSearch).retrieveNetAmounts(
                anyLong());
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(10.00));
        revenueShareDetails.setOperatorRevenueSharePercentage(BigDecimal
                .valueOf(20.00));
        service.setRevenueShareDetails(revenueShareDetails);
        service.getRevenueShareDetails().calculate(service.getModel());
        return service;
    }

    private Service buildDirectServiceWithDiscount() {
        Service service = new Service();
        service.setModel(OfferingType.DIRECT);
        Supplier supplier = new Supplier();
        service.setSupplier(supplier);
        OrganizationData orgData = new OrganizationData();
        supplier.setOrganizationData(orgData);
        orgData.setId("supplierId");
        orgData.setCountryIsoCode("US");
        orgData.setKey(BigInteger.valueOf(1L));

        RevenueShareDetails revenueShareDetails = new RevenueShareDetails();
        revenueShareDetails.setServiceRevenue(BigDecimal.valueOf(730.00));
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(BigDecimal.valueOf(730.00));
        doReturn(mockNetAmounts).when(assembler.xmlSearch).retrieveNetAmounts(
                anyLong());
        doReturn(BigDecimal.valueOf(50.00)).when(assembler.xmlSearch)
                .retrieveDiscountPercent();
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(10.00));
        revenueShareDetails.setOperatorRevenueSharePercentage(BigDecimal
                .valueOf(20.00));
        service.setRevenueShareDetails(revenueShareDetails);
        return service;
    }

    private Service buildBrokerService() {
        Service service = new Service();
        service.setModel(OfferingType.BROKER);
        Supplier supplier = new Supplier();
        service.setSupplier(supplier);
        OrganizationData supplierData = new OrganizationData();
        supplier.setOrganizationData(supplierData);
        supplierData.setId("supplierId");
        supplierData.setName("name");
        supplierData.setCountryIsoCode("US");
        supplierData.setKey(BigInteger.valueOf(1L));
        Broker broker = new Broker();
        service.setBroker(broker);
        OrganizationData brokerData = new OrganizationData();
        broker.setOrganizationData(brokerData);
        brokerData.setId("brokerId");
        brokerData.setName("name");
        brokerData.setCountryIsoCode("UK");
        brokerData.setKey(BigInteger.valueOf(2L));

        RevenueShareDetails revenueShareDetails = new RevenueShareDetails();
        revenueShareDetails.setServiceRevenue(BigDecimal.valueOf(500.00));
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(BigDecimal.valueOf(500.00));
        doReturn(mockNetAmounts).when(assembler.xmlSearch).retrieveNetAmounts(
                anyLong());
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(15.00));
        revenueShareDetails.setOperatorRevenueSharePercentage(BigDecimal
                .valueOf(20.00));
        revenueShareDetails.setBrokerRevenueSharePercentage(BigDecimal
                .valueOf(5.00));
        service.setRevenueShareDetails(revenueShareDetails);
        service.getRevenueShareDetails().calculate(service.getModel());
        return service;
    }

    private Currency buildCurrency() {
        Currency currency = new Currency();
        currency.setRevenuesOverAllMarketplaces(new RevenuesOverAllMarketplaces());
        return currency;
    }

    private Marketplace buildMarketplace() {
        Marketplace marketplace = new Marketplace();
        marketplace.setRevenueSharePercentage(BigDecimal.valueOf(20.00));
        marketplace.setRevenuesPerMarketplace(new RevenuesPerMarketplace());
        return marketplace;
    }

    private Service buildResellerService() {
        Service service = new Service();
        service.setModel(OfferingType.RESELLER);
        Supplier supplier = new Supplier();
        service.setSupplier(supplier);
        OrganizationData supplierData = new OrganizationData();
        supplier.setOrganizationData(supplierData);
        supplierData.setId("supplierId");
        supplierData.setCountryIsoCode("RC");
        supplierData.setKey(BigInteger.valueOf(1L));
        Reseller reseller = new Reseller();
        service.setReseller(reseller);
        OrganizationData resellerData = new OrganizationData();
        reseller.setOrganizationData(resellerData);
        resellerData.setId("resellerId");
        resellerData.setCountryIsoCode("US");
        resellerData.setKey(BigInteger.valueOf(3L));
        RevenueShareDetails revenueShareDetails = new RevenueShareDetails();
        revenueShareDetails.setServiceRevenue(BigDecimal.valueOf(500.00));
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(BigDecimal.valueOf(500.00));
        doReturn(mockNetAmounts).when(assembler.xmlSearch).retrieveNetAmounts(
                anyLong());
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(15.00));
        revenueShareDetails.setOperatorRevenueSharePercentage(BigDecimal
                .valueOf(20.00));
        revenueShareDetails.setResellerRevenueSharePercentage(BigDecimal
                .valueOf(5.00));
        service.setRevenueShareDetails(revenueShareDetails);
        service.getRevenueShareDetails().calculate(service.getModel());
        return service;
    }
}
