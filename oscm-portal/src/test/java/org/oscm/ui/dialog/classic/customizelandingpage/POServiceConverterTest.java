/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;
import org.oscm.internal.landingpageconfiguration.POService;

/**
 * JUnit tests for POServiceConverter class.
 * 
 * @author Yang Zou.
 * 
 */
public class POServiceConverterTest {

    private FacesContextStub context;
    private POServiceConverter converter;

    // Service name contains colon
    private String poServiceAsString = "10001$%&/$%5$%&/$%Hello Kitty November 6.66 1234:$%&/$%"
            + "Fujitsu$%&/$%/image?type=SERVICE_IMAGE&amp;serviceKey="
            + "10001$%&/$%status_NOT_ACTIVE";
    
    // Provider name contains comma
    private String poServiceAsStringWithComma = "10001$%&/$%5$%&/$%Hello Kitty November 6.66 1234:$%&/$%"
            + "Fujitsu&comma; INC.$%&/$%/image?type=SERVICE_IMAGE&amp;serviceKey="
            + "10001$%&/$%status_NOT_ACTIVE";
    
    /**
     * Setup method.
     */
    @Before
    public void setup() {
        converter = new POServiceConverter();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    /**
     * Test for getting value.
     * 
     * @throws ParseException
     */
    @Test
    public void testGetAsObject() throws ConverterException, ParseException {
        UIComponent component = getComponent();
        POService actual = (POService) converter.getAsObject(context,
                component, poServiceAsString);
        Assert.assertEquals(actual.getKey(), 10001);
        Assert.assertEquals(actual.getPictureUrl(),
                "/image?type=SERVICE_IMAGE&amp;serviceKey=10001");
        Assert.assertEquals(actual.getProviderName(), "Fujitsu");
        Assert.assertEquals(actual.getServiceName(),
                "Hello Kitty November 6.66 1234:");
        Assert.assertEquals(actual.getStatusSymbol(), "status_NOT_ACTIVE");
        Assert.assertEquals(actual.getVersion(), 5);
    }

    /**
     * Test for getting string of the value containing provider name with comma.
     */
    @Test
    public void testGetAsStringWithComma() {
        UIComponent component = getComponent();
        POService po = createPOService(10001,
                "/image?type=SERVICE_IMAGE&amp;serviceKey=10001", "Fujitsu, INC.",
                "Hello Kitty November 6.66 1234:", "status_NOT_ACTIVE", 5);
        String actual = converter.getAsString(context, component, po);
        Assert.assertTrue(actual.contains("&comma;"));
    }
    
    /**
     * Test for getting value with unescaped comma.
     * 
     * @throws ParseException
     */
    @Test
    public void testGetAsObjectWithComma() throws ConverterException, ParseException {
        UIComponent component = getComponent();
        POService actual = (POService) converter.getAsObject(context,
                component, poServiceAsStringWithComma);
        
        Assert.assertEquals(actual.getKey(), 10001);
        Assert.assertEquals(actual.getPictureUrl(),
                "/image?type=SERVICE_IMAGE&amp;serviceKey=10001");
        Assert.assertEquals(actual.getProviderName(), "Fujitsu, INC.");
        Assert.assertEquals(actual.getServiceName(),
                "Hello Kitty November 6.66 1234:");
        Assert.assertEquals(actual.getStatusSymbol(), "status_NOT_ACTIVE");
        Assert.assertEquals(actual.getVersion(), 5);
    }

    /**
     * Test for getting string of the value.
     */
    @Test
    public void testGetAsString() {
        UIComponent component = getComponent();
        POService po = createPOService(10001,
                "/image?type=SERVICE_IMAGE&amp;serviceKey=10001", "Fujitsu",
                "Hello Kitty November 6.66 1234:", "status_NOT_ACTIVE", 5);
        String actual = converter.getAsString(context, component, po);
        Assert.assertEquals(poServiceAsString, actual);
    }

    /**
     * Helper method.
     */
    private final UIComponentStub getComponent() {
        Map<String, Object> map = new HashMap<String, Object>();
        return new UIComponentStub(map);
    }

    private POService createPOService(long key, String pictureUrl,
            String providerName, String serviceName, String statusSymbol,
            int version) {
        POService po = new POService();
        po.setKey(key);
        po.setPictureUrl(pictureUrl);
        po.setProviderName(providerName);
        po.setServiceName(serviceName);
        po.setStatusSymbol(statusSymbol);
        po.setVersion(version);
        return po;
    }
}
