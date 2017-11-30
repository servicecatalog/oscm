/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUserDetails;

public class GotoMarketplaceBeanTest {

    private MarketplaceService mpServiceMock;
    private HttpServletRequest servletRequestMock;
    private GotoMarketplaceBean marketplaceGotoBean;
    private HttpSession httpSessionMock;
    private IdentityService idServiceMock;

    @Before
    public void setup() {
        mpServiceMock = mock(MarketplaceService.class);
        marketplaceGotoBean = spy(new GotoMarketplaceBean());
        servletRequestMock = mock(HttpServletRequest.class);
        idServiceMock = mock(IdentityService.class);

        doReturn(mpServiceMock).when(marketplaceGotoBean)
                .getMarketplaceService();
        doReturn(servletRequestMock).when(marketplaceGotoBean).getRequest();

        httpSessionMock = mock(HttpSession.class);
        when(servletRequestMock.getSession(anyBoolean())).thenReturn(
                httpSessionMock);
        
        doReturn(idServiceMock).when(marketplaceGotoBean).getIdService();
        
        doReturn(Boolean.TRUE).when(marketplaceGotoBean)
                .isLoggedInAndMarketplaceOwner();
        doReturn(Boolean.TRUE).when(marketplaceGotoBean)
                .isLoggedInAndVendorManager();
        
        given(idServiceMock.getCurrentUserDetails()).willReturn(getUserDetails());
    }

    @Test
    public void getMarketplaces() {
        // given
        given(mpServiceMock.getMarketplacesForOrganization()).willReturn(
                marketplace(1L, "publishedMp"));
        given(mpServiceMock.getMarketplacesOwned()).willReturn(
                marketplace(2L, "ownedMp"));

        // when
        int size = marketplaceGotoBean.getMarketplaces().size();

        // then
        assertEquals(2, size);
        for (SelectItem m : marketplaceGotoBean.getMarketplaces()) {
            assertTrue(m.getValue().equals("publishedMp")
                    || m.getValue().equals("ownedMp"));
        }
    }

    private List<VOMarketplace> marketplace(long key, String mpId) {
        List<VOMarketplace> publish = new ArrayList<VOMarketplace>();
        VOMarketplace mp1 = new VOMarketplace();
        mp1.setKey(key);
        mp1.setMarketplaceId(mpId);
        publish.add(mp1);
        return publish;
    }

    @Test
    public void getMarketplaces_Cached() {
        // given some marketplaces
        given(mpServiceMock.getMarketplacesForOrganization()).willReturn(
                marketplace(1L, "publishedMp"));
        given(mpServiceMock.getMarketplacesOwned()).willReturn(
                marketplace(2L, "ownedMp"));

        // when calling twice
        marketplaceGotoBean.getMarketplaces();
        marketplaceGotoBean.getMarketplaces();

        // then loaded only once
        verify(mpServiceMock, times(1)).getMarketplacesForOrganization();
        verify(mpServiceMock, times(1)).getMarketplacesOwned();
    }

    @Test
    public void gotoMarketplace() {
        // given
        marketplaceGotoBean.setSelectedMarketplace("mid");

        // when
        String outcome = marketplaceGotoBean.gotoMarketplace();

        // then
        assertEquals("success", outcome);
        verify(marketplaceGotoBean, times(1)).setMarketplaceId(eq("mid"));
    }

    /**
     * Test if the goto marketplace button is enabled when marketplace ID is
     * set.
     */
    @Test
    public void isButtonEnabled() {
        // given
        marketplaceGotoBean.setSelectedMarketplace("mid");

        // when
        boolean isEnabled = marketplaceGotoBean.isButtonEnabled();

        // then
        assertTrue(isEnabled);

    }

    /**
     * Test if the goto marketplace button is disabled when marketplace ID is
     * not set.
     */
    @Test
    public void setSelectedMarketplace_null() {
        // given
        marketplaceGotoBean.setSelectedMarketplace(null);

        // when
        boolean isEnabled = marketplaceGotoBean.isButtonEnabled();

        // then
        assertFalse(isEnabled);

    }

    @Test
    public void processValueChange() {
        // given
        marketplaceGotoBean.setSelectedMarketplace(null);
        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn("id").when(mockedEvent).getNewValue();

        // when
        marketplaceGotoBean.processValueChange(mockedEvent);

        // then
        assertNotNull(marketplaceGotoBean.getSelectedMarketplace());
    }

    /**
     * The user has only the MARKETPLACE_OWNER role, therefore only owned
     * marketplaces must be loaded.
     */
    @Test
    public void loadMarketplaces_NoServiceManager() {
        // given two marketplaces
        doReturn(Boolean.FALSE).when(marketplaceGotoBean)
                .isLoggedInAndVendorManager();
        given(mpServiceMock.getMarketplacesForOrganization()).willReturn(
                marketplace(1L, "publishedMp"));
        given(mpServiceMock.getMarketplacesOwned()).willReturn(
                marketplace(2L, "ownedMp"));

        // when
        List<SelectItem> marketplaces = marketplaceGotoBean.getMarketplaces();

        // then only one is loaded
        assertEquals(1, marketplaces.size());
        assertEquals("ownedMp", marketplaces.get(0).getValue());
    }

    /**
     * The user has only the SERVICE_MANAGER role, therefore only marketplaces
     * with publishing rights must be loaded.
     */
    @Test
    public void loadMarketplaces_NoMarketplaceOwner() {
        // given two marketplaces
        doReturn(Boolean.FALSE).when(marketplaceGotoBean)
                .isLoggedInAndMarketplaceOwner();
        given(mpServiceMock.getMarketplacesForOrganization()).willReturn(
                marketplace(1L, "publishedMp"));
        given(mpServiceMock.getMarketplacesOwned()).willReturn(
                marketplace(2L, "ownedMp"));

        // when
        List<SelectItem> marketplaces = marketplaceGotoBean.getMarketplaces();

        // then only one is loaded
        assertEquals(1, marketplaces.size());
        assertEquals("publishedMp", marketplaces.get(0).getValue());
    }
    
    @Test
    public void testValidateMarketplaceTenant(){
        
        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId("tnt1");
        String currentUserTenantId = "tnt1";
        
        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);
        
        //then
        assertTrue(validationResult);
    }
    
    @Test
    public void testValidateMarketplaceTenantWithDifferentTenant(){
        
        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId("tnt1");
        String currentUserTenantId = "tnt2";
        
        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);
        
        //then
        assertFalse(validationResult);
    }

    @Test
    public void testValidateMarketplaceTenantWithDifferentTenantNull(){

        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId(null);
        String currentUserTenantId = "tnt2";

        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);

        //then
        assertFalse(validationResult);
    }

    @Test
    public void testValidateMarketplaceTenantWithDifferentNullTenant(){

        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId("tnt2");
        String currentUserTenantId = "";

        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);

        //then
        assertFalse(validationResult);
    }

    @Test
    public void testValidateMarketplaceTenantEmptyTenant(){

        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId(null);
        String currentUserTenantId = "";

        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);

        //then
        assertFalse(validationResult);
    }

    @Test
    public void testValidateMarketplaceTenantNullNull(){

        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId(null);
        String currentUserTenantId = null;

        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);

        //then
        assertTrue(validationResult);
    }

    @Test
    public void testValidateMarketplaceTenantEmptyEmpty(){

        //given
        VOMarketplace mpl = new VOMarketplace();
        mpl.setTenantId("");
        String currentUserTenantId = "";

        //when
        boolean validationResult = marketplaceGotoBean.validateMarketplaceTenant(mpl, currentUserTenantId);

        //then
        assertTrue(validationResult);
    }
    
    private VOUserDetails getUserDetails(){
        
        VOUserDetails userDetails = new VOUserDetails(1, 0);
        return userDetails;
    }

}
