/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;

public class BrandBeanTest {

    private BrandBean brandBean;
    private MarketplaceBean marketplaceBean;
    private MarketplaceService marketplaceServiceMock;

    private ExternalContext extContextMock;

    protected byte[] brandingPackageData = new byte[] { 1, 2, 3, 4, 5 };

    private static final String WHITE_LABEL_PATH = "http://localhost:8180/oscm-portal";

    private static final String INVALID_URL = "http://thisisaunittest.com";

    private static final String WHITE_LABEL_URL = WHITE_LABEL_PATH
            + "/marketplace/css/mp.css";
    private static final String VALID_BRANDING_URL = WHITE_LABEL_URL;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    @Before
    public void setUp() throws Exception {
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };

        brandBean = spy(new BrandBean());
        marketplaceBean = spy(new MarketplaceBean());
        MenuBean menuBean = new MenuBean();
        marketplaceBean.setMenuBean(menuBean);

        brandBean.setMarketplaceBean(marketplaceBean);
        brandBean.setMarketplaceId("FUJITSU");

        // Mock the faces context of the brandBean
        FacesContext fcContextMock = mock(FacesContext.class);
        doReturn(fcContextMock).when(brandBean).getFacesContext();

        // Mock the external context of the faces context
        extContextMock = mock(ExternalContext.class);
        doReturn(extContextMock).when(fcContextMock).getExternalContext();

        // Mock the path of the white label
        doReturn(WHITE_LABEL_PATH).when(extContextMock).getRequestContextPath();

        // Mock the input stream of the external context
        InputStream inMock = new ByteArrayInputStream(brandingPackageData);
        doReturn(inMock).when(extContextMock).getResourceAsStream(anyString());

        // Mock the brand service
        marketplaceServiceMock = mock(MarketplaceService.class);
        doReturn(marketplaceServiceMock).when(brandBean)
                .getMarketplaceService();
        doReturn(marketplaceServiceMock).when(marketplaceBean)
                .getMarketplaceService();
        brandBean.setMarketplaceBean(marketplaceBean);
    }

    @Test
    public void testGetMarketplaceBean() throws Exception {
        assertEquals(marketplaceBean, brandBean.getMarketplaceBean());
    }

    @Test
    public void testGetMarketplace() throws Exception {
        assertFalse(brandBean.isMarketplaceSelected());
    }

    @Test
    public void testGetBrandingUrl() throws Exception {
        brandBean.setBrandingUrl("some url");
        assertEquals("some url", brandBean.getBrandingUrl());
    }

    @Test
    public void testGetWhiteLabeBrandingUrl() throws Exception {
        assertEquals(WHITE_LABEL_URL, brandBean.getWhiteLabelBrandingUrl());
    }

    @Test
    public void testFetchBrandingPackage_NullInputStream() throws Exception {
        doReturn(null).when(extContextMock).getResourceAsStream(anyString());

        brandBean.fetchBrandingPackage();

        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0).getSeverity());
    }

    @Test
    public void testFetchBrandingPackage() throws Exception {
        brandBean.fetchBrandingPackage();
        assertEquals(0, facesMessages.size());
    }

    @Test
    public void testIsBrandingPackageAvailable() throws Exception {
        assertFalse(brandBean.isBrandingPackageAvailable());
        brandBean.fetchBrandingPackage();
        assertTrue(brandBean.isBrandingPackageAvailable());
    }

    @Test
    public void testProcessValueChange() throws Exception {
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        doReturn("newMarketplaceId").when(eventMock).getNewValue();
        doReturn(VALID_BRANDING_URL).when(marketplaceServiceMock)
                .getBrandingUrl("newMarketplaceId");
        VOMarketplace mp = new VOMarketplace();
        doReturn(mp).when(marketplaceServiceMock).getMarketplaceById(
                anyString());
        brandBean.processValueChange(eventMock);
        assertEquals(VALID_BRANDING_URL, brandBean.getBrandingUrl());
        assertTrue(brandBean.isMarketplaceSelected());
    }

    @Test
    public void testProcessValueChange_NoSelectedMPL() throws Exception {
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        doReturn("0").when(eventMock).getNewValue();
        brandBean.processValueChange(eventMock);
        assertEquals(null, brandBean.getMarketplaceId());
        assertEquals(null, brandBean.getBrandingUrl());
    }

    @Test
    public void testProcessValueChange_ObjectNotFoundException()
            throws Exception {
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        doReturn("newMarketplaceId").when(eventMock).getNewValue();

        doThrow(new ObjectNotFoundException()).when(marketplaceServiceMock)
                .getBrandingUrl("newMarketplaceId");

        brandBean.processValueChange(eventMock);
        assertEquals(null, brandBean.getBrandingUrl());
        assertEquals(0, facesMessages.size());
    }

    @Test
    public void testDownloadBrandingPackage_NullBrandingPackageData()
            throws Exception {
    	brandBean.downloadBrandingPackage();
    	
        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0).getSeverity());
    }

    @Test
    public void testDownloadBrandingPackage() throws Exception {
        brandBean.fetchBrandingPackage();
        doNothing().when(brandBean).writeContentToResponse(
                eq(brandingPackageData), anyString(), anyString());
        brandBean.downloadBrandingPackage();

        verify(brandBean, times(1)).writeContentToResponse(
                eq(brandingPackageData), anyString(), anyString());

        assertEquals(0, facesMessages.size());
    }

    @Test
    public void testSaveBrandingUrl() throws Exception {
        // mock the service method
        doNothing().when(marketplaceServiceMock).saveBrandingUrl(
                any(VOMarketplace.class), anyString());
        // ensure a marketplace is selected
        ensureSelectedMarketplace();

        brandBean.setBrandingUrl(VALID_BRANDING_URL);
        brandBean.saveBrandingUrl();

        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0).getSeverity());
    }

    @Test
    public void testSaveBrandingUrl_InvalidUrl() throws Exception {
        doThrow(new ValidationException()).when(marketplaceServiceMock)
                .saveBrandingUrl(any(VOMarketplace.class), anyString());

        brandBean.saveBrandingUrl();

        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0).getSeverity());
    }

    @Test
    public void testSaveBrandingUrl_EmptyUrl() throws Exception {
        ensureSelectedMarketplace();

        brandBean.setBrandingUrl("");
        brandBean.saveBrandingUrl();

        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0).getSeverity());
    }

    @Test
    public void testSaveBrandingUrl_MPLNotFound() throws Exception {
        brandBean.setBrandingUrl(VALID_BRANDING_URL);
        doThrow(new ObjectNotFoundException()).when(marketplaceServiceMock)
                .saveBrandingUrl(any(VOMarketplace.class), anyString());

        brandBean.saveBrandingUrl();
       
        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0).getSeverity());
        assertNull(brandBean.getBrandingUrl());
    }

    @Ignore
    // FIXME : No!! This attempts a real connection! Furthermore the URL with
    // port is
    // hard-coded! Mock the connection - for real connection test create an
    // integration test
    // in the IT project!
    @Test
    public void validateCurrentUrl_ValidUrl() {
        // given
        brandBean.setBrandingUrl(WHITE_LABEL_PATH);

        // when
        brandBean.validateCurrentUrl();

        // then
        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0)
                .getSeverity());
    }

    @Test
    public void validateCurrentUrl_NullUrl() {
        // given
        brandBean.setBrandingUrl(null);

        // when
        brandBean.validateCurrentUrl();

        // then
        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0)
                .getSeverity());
    }

    @Test
    public void validateCurrentUrl_InvalidUrl() {
        // given
        brandBean.setBrandingUrl(INVALID_URL);

        // when
        brandBean.validateCurrentUrl();

        // then
        assertEquals(1, facesMessages.size());
        assertEquals(FacesMessage.SEVERITY_ERROR, facesMessages.get(0)
                .getSeverity());
    }

    private void ensureSelectedMarketplace() throws Exception {
        // ensure a marketplace is selected
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        doReturn("newMarketplaceId").when(eventMock).getNewValue();
        VOMarketplace mp = new VOMarketplace();
        doReturn(mp).when(marketplaceServiceMock).getMarketplaceById(
                anyString());
        brandBean.processValueChange(eventMock);
    }

}
