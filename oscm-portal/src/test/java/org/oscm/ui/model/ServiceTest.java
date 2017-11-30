/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 11.04.2011                                                      
 *                                                                              
 *  Completion Time: 11.04.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.ui.stubs.UIViewRootStub;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;

/**
 * @author weiser
 * 
 */
public class ServiceTest {

    private VOService voService;

    private static final String P1 = "P1";
    private static final String P2 = "P2";
    private static final String P3 = "P3";
    private static final String P4 = "P4";
    private static final String P5 = "P5";
    private static final String SERVICE_NAME_UNDEFINED = "<Service name is undefined>";

    private static final String EUR = "EUR";

    @Before
    public void setup() throws Exception {
        voService = new VOService();
        voService.setAccessType(ServiceAccessType.DIRECT);
        voService.setDescription("marketingName");
        voService.setFeatureURL("technicalURL");
        voService.setName("name");
        voService.setServiceId("serviceId");
        voService.setStatus(ServiceStatus.ACTIVE);
        voService.setSellerId("supplierId");
        voService.setSellerName("supplierName");
        voService.setTechnicalId("technicalId");

        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH);
        UIViewRootStub vrStub = mock(UIViewRootStub.class);
        when(vrStub.getLocale()).thenReturn(Locale.ENGLISH);

        contextStub.setViewRoot(vrStub);

        ResourceBundleStub resourceBundleStub = new ResourceBundleStub();
        ((ApplicationStub) contextStub.getApplication())
                .setResourceBundleStub(resourceBundleStub);
        resourceBundleStub.addResource("priceModel.text.free", P1);
        resourceBundleStub
                .addResource("priceModel.text.price", P2 + " {0} {1}");
        resourceBundleStub.addResource("priceModel.text.perSubscription", P3
                + " {0}");
        resourceBundleStub.addResource("priceModel.text.perUser", P4 + " {0}");
        resourceBundleStub.addResource("priceModel.text.seeDetails", P5);
        resourceBundleStub.addResource("service.name.undefined",
                SERVICE_NAME_UNDEFINED);
    }

    @Test
    public void testService() throws Exception {
        Service service = new Service(voService);
        Assert.assertEquals(voService.getDescription(),
                service.getDescription());
        Assert.assertEquals(voService.getFeatureURL(), service.getFeatureURL());
        Assert.assertEquals(voService.getName(), service.getName());
        Assert.assertEquals(null, service.getOrganizationId());
        Assert.assertEquals(null, service.getOrganizationName());
        Assert.assertEquals(null, service.getOrganizationKey());
        Assert.assertEquals(voService.getServiceId(), service.getServiceId());
        Assert.assertEquals(voService.getSellerName(), service.getSellerName());
        Assert.assertEquals(voService.getTechnicalId(),
                service.getTechnicalId());
    }

    /**
     * Retest for bug 8653.
     */
    @Test
    public void testGetServiceNameUndefined() throws Exception {
        voService.setName("");
        Service service = new Service(voService);

        Assert.assertEquals(SERVICE_NAME_UNDEFINED, service.getNameToDisplay());

    }

    /**
     * Retest for bug 7437.
     */
    @Test
    public void testGetPriceModelTextKey() {
        Service service = new Service(voService);
        Assert.assertNull(service.getPriceModel());
        Assert.assertEquals("priceModel.text.undefined",
                service.getPriceModelTextKey());

        VOPriceModel pm = new VOPriceModel();
        service.setPriceModel(new PriceModel(pm));
        Assert.assertEquals("priceModel.text.undefined",
                service.getPriceModelTextKey());

        pm.setKey(123);
        Assert.assertEquals("priceModel.text.free",
                service.getPriceModelTextKey());
    }

    @Test
    public void testGetPriceModelTextFree() {

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);

        Assert.assertEquals(P1, service.getPriceText());
        Assert.assertEquals("No unit text expected", "",
                service.getPriceUnitText());
    }

    @Test
    public void testGetPriceModelTextSimple() {

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(10));
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);
        String text = service.getPriceText();
        Assert.assertNotNull("Expected a price text", text);
        Assert.assertTrue("Wrong text", text.startsWith(P2));
        Assert.assertTrue("Missing the currency", text.contains(EUR));
        Assert.assertTrue("Price is missing", text.contains("10"));
        Assert.assertEquals("No unit text expected", "",
                service.getPriceUnitText());
    }

    @Test
    public void testGetPriceModelTextSubscription() {

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(10));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(20));
        priceModel.setPeriod(PricingPeriod.MONTH);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);
        String text = service.getPriceText();
        String unit = service.getPriceUnitText();
        Assert.assertNotNull("Expected a price text", text);
        Assert.assertTrue("Wrong text", text.startsWith(P2));
        Assert.assertTrue("Price is missing", text.contains("20"));
        Assert.assertTrue("Missing the currency", text.contains(EUR));
        Assert.assertNotNull("Expected a unit text", unit);
        Assert.assertTrue(unit.startsWith(P3));
    }

    @Test
    public void testGetPriceModelTextUser() {

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(10));
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(40));
        priceModel.setPeriod(PricingPeriod.MONTH);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);
        String text = service.getPriceText();
        String unit = service.getPriceUnitText();
        Assert.assertNotNull("Expected a price text", text);
        Assert.assertTrue("Wrong text", text.startsWith(P2));
        Assert.assertTrue("Price is missing", text.contains("40"));
        Assert.assertTrue("Missing the currency", text.contains(EUR));
        Assert.assertNotNull("Expected a unit text", unit);
        Assert.assertTrue(unit.startsWith(P4));
    }

    @Test
    public void testGetPriceModelTextDetails() {

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.ZERO);
        priceModel.setPricePerPeriod(BigDecimal.ZERO);
        priceModel.setPricePerUserAssignment(BigDecimal.ZERO);
        priceModel.setPeriod(PricingPeriod.MONTH);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);
        String text = service.getPriceText();
        Assert.assertNotNull("Expected a price text", text);
        Assert.assertTrue("Wrong text", text.startsWith(P5));
        Assert.assertEquals("No unit text expected", "",
                service.getPriceUnitText());
    }

    @Test
    public void testGetPriceTextToDisplay() {
        // given price model with recurring costs
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(10));
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(50));
        priceModel.setPeriod(PricingPeriod.MONTH);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);

        // when
        String priceToDisplay = service.getPriceToDisplay();

        // then
        String expected = service.getPriceText() + " "
                + service.getPriceUnitText();
        Assert.assertEquals(expected, priceToDisplay);
    }

    @Test
    public void testGetPriceTextToDisplay_NoUnit() {
        // given price model with one time costs only
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrencyISOCode("USD");
        priceModel.setOneTimeFee(BigDecimal.valueOf(60));
        priceModel.setPricePerUserAssignment(BigDecimal.ZERO);
        voService.setPriceModel(priceModel);
        Service service = new Service(voService);

        // when
        String priceToDisplay = service.getPriceToDisplay();

        // then
        String expected = service.getPriceText();
        Assert.assertEquals(expected, priceToDisplay);
    }

    @Test
    public void testGetNameToDisplay_NullName() {
        VOService voService = new VOService();
        voService.setName(null);

        Service service = new Service(voService);
        String result = service.getNameToDisplay();
        Assert.assertEquals(JSFUtils.getText("service.name.undefined", null),
                result);
    }

    @Test
    public void testGetNameToDisplay_EmptyName() {
        VOService voService = new VOService();
        voService.setName("");

        Service service = new Service(voService);
        String result = service.getNameToDisplay();
        Assert.assertEquals(JSFUtils.getText("service.name.undefined", null),
                result);
    }

    @Test
    public void testGetNameToDisplay_NotEmptyName() {
        VOService voService = new VOService();
        voService.setName("service1");

        Service service = new Service(voService);
        String result = service.getNameToDisplay();
        Assert.assertEquals(voService.getNameToDisplay(), result);
    }

    @Test
    public void isSupplierOrBroker_supplier() {
        // given
        Service service = createService(OfferingType.DIRECT);

        // when
        boolean supplierOrBroker = service.isSupplierOrBroker();

        // then
        assertTrue("Service is not recognized as supplier service",
                supplierOrBroker);
    }

    @Test
    public void isSupplierOrBroker_broker() {
        // given
        Service service = createService(OfferingType.BROKER);

        // when
        boolean supplierOrBroker = service.isSupplierOrBroker();

        // then
        assertTrue("Service is not recognized as broker service",
                supplierOrBroker);
    }

    @Test
    public void isSupplierOrBroker_reseller() {
        // given
        Service service = createService(OfferingType.RESELLER);

        // when
        boolean supplierOrBroker = service.isSupplierOrBroker();

        // then
        assertFalse("Reseller service not recognized", supplierOrBroker);
    }

    private Service createService(OfferingType offeringType) {
        VOService voService = new VOService();
        voService.setOfferingType(offeringType);
        Service service = new Service(voService);
        return service;
    }

    /**
     * Retest for bug 10079
     */
    @Test
    public void testGetServiceIdToDisplay() throws Exception {
        voService.setServiceIdToDisplay("Service (c912153e)");
        Service service = new Service(voService);

        assertEquals("Service (c912153e)", service.getServiceIdToDisplay());

    }

    @Test
    public void getConfiguratorUrl_CustomUrl() {
        // given
        VOService voService = new VOService();
        voService.setConfiguratorUrl("http://www.test.com");
        Service service = new Service(voService);

        // when
        String expectedUrl = service.getConfiguratorUrl();

        // then
        assertEquals("http://www.test.com", expectedUrl);
    }

}
