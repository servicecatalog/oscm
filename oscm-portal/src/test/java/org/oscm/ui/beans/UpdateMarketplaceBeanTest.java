/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Mar 16, 2012
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.marketplace.MarketplaceServiceManagePartner;
import org.oscm.internal.pricing.*;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.model.Marketplace;
import org.oscm.ui.model.User;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UiDelegateStub;

/**
 * @author tang
 *
 */
@SuppressWarnings("boxing")
public class UpdateMarketplaceBeanTest {

    private UpdateMarketplaceBean umpb;
    private ApplicationBean appBean;
    private MarketplaceService msmock;
    private VOMarketplace vMp1, vMp2;
    private Marketplace mp;
    private MenuBean mbMock;
    private User usrmock;
    private FacesContextStub fc;
    private PricingService ps;
    private MarketplaceServiceManagePartner mmps;
    private POMarketplacePricing mpPricing;
    private Response updateMarketplaceResponse;
    private SelectOrganizationIncludeBean selectOrganizationIncludeBean;
    UiDelegateStub ui;
    private ManageTenantService mts;

    @Before
    public void setup() throws Exception {
        vMp1 = new VOMarketplace();
        vMp1.setKey(1234);
        vMp1.setVersion(3);
        vMp1.setMarketplaceId("vo marketplaceId");
        vMp1.setName("vo name");
        vMp1.setOwningOrganizationId("vo id");
        vMp1.setOwningOrganizationName("vo org name");

        vMp2 = new VOMarketplace();
        vMp2.setKey(1235);
        vMp2.setVersion(4);
        vMp2.setMarketplaceId("another vo MarketplaceId");
        vMp2.setName("another vo name");
        vMp2.setOwningOrganizationId("another vo id");
        vMp2.setOwningOrganizationName("another vo org name");
        vMp2.setOpen(true);

        mp = new Marketplace();
        mp.setKey(1236);
        mp.setVersion(4);
        mp.setMarketplaceId("MarketplaceId");
        mp.setName("name");
        mp.setOwningOrganizationId("id");
        mp.setOrganizationSelectVisible(false);
        mp.setClosed(true);

        msmock = mock(MarketplaceService.class);
        
        when(msmock.getMarketplacesOwned()).thenReturn(
                Arrays.asList(vMp1, vMp2));
        when(msmock.getMarketplacesForOperator()).thenReturn(
                Arrays.asList(vMp1, vMp2));
        when(msmock.getMarketplaceById(matches(vMp1.getMarketplaceId())))
                .thenReturn(vMp1);
        when(msmock.getMarketplaceById(matches(vMp2.getMarketplaceId())))
                .thenReturn(vMp2);
        
        
        
        mbMock = mock(MenuBean.class);
        appBean = mock(ApplicationBean.class);
        //mbMock.setApplicationBean(appBean);
        when(mbMock.getApplicationBean()).thenReturn(appBean);
        when(mbMock.getApplicationBean().isInternalAuthMode()).thenReturn(true);
        umpb = spy(new UpdateMarketplaceBean());
        ui = spy(new UiDelegateStub());
        umpb.ui = ui;
        mts = mock(ManageTenantService.class);

        doReturn(Boolean.FALSE).when(umpb).isLoggedInAndPlatformOperator();
        doReturn(msmock).when(umpb).getMarketplaceService();
        doNothing().when(umpb).addMessage(anyString(), any(Severity.class),
                anyString(), any(Object[].class));
        // provide information about the logged in user
        VOUserDetails voUser = new VOUserDetails();
        voUser.setOrganizationId(vMp1.getOwningOrganizationId());

        usrmock = mock(User.class);
        when(Boolean.valueOf(usrmock.isMarketplaceOwner())).thenReturn(
                Boolean.TRUE);
        when(usrmock.getOrganizationId()).thenReturn(
                vMp1.getOwningOrganizationId());
        doReturn(usrmock).when(umpb).getUserFromSession();

        umpb.setMenuBean(mbMock);

        fc = spy(new FacesContextStub(Locale.ENGLISH));
        doNothing().when(fc).addMessage(anyString(), any(FacesMessage.class));
        fc.setCurrentInstance(fc);

        ps = mock(PricingService.class);
        doReturn(ps).when(umpb).getService(eq(PricingService.class), any());
        // mock marketplace revenue share
        PORevenueShare revenueShare = new PORevenueShare();
        revenueShare.setRevenueShare(BigDecimal.ZERO);
        POMarketplacePriceModel mpPriceModel = new POMarketplacePriceModel();
        mpPriceModel.setRevenueShare(revenueShare);
        // mock reseller revenue share
        POPartnerPriceModel partnerPriceModel = new POPartnerPriceModel();
        revenueShare = new PORevenueShare();
        revenueShare.setRevenueShare(BigDecimal.ZERO);
        partnerPriceModel.setRevenueShareResellerModel(revenueShare);
        // mock broker revenue share
        revenueShare = new PORevenueShare();
        revenueShare.setRevenueShare(BigDecimal.ZERO);
        partnerPriceModel.setRevenueShareBrokerModel(revenueShare);
        // mock marketplace pricing
        mpPricing = new POMarketplacePricing();
        mpPricing.setMarketplacePriceModel(mpPriceModel);
        mpPricing.setPartnerPriceModel(partnerPriceModel);
        Response response = new Response(mpPricing);
        doReturn(response).when(ps).getPricingForMarketplace(anyString());

        mmps = mock(MarketplaceServiceManagePartner.class);
        doReturn(mmps).when(umpb).getService(
                eq(MarketplaceServiceManagePartner.class), any());
        updateMarketplaceResponse = new Response(umpb.convertToValueObject(umpb
                .getModel()), umpb.convertToMarketplacePriceModel(umpb
                .getModel()), umpb.convertToPartnerPriceModel(umpb.getModel()));
        doReturn(updateMarketplaceResponse).when(mmps).updateMarketplace(
                any(VOMarketplace.class), any(POMarketplacePriceModel.class),
                any(POPartnerPriceModel.class));

        this.selectOrganizationIncludeBean = mock(SelectOrganizationIncludeBean.class);
        umpb.setSelectOrganizationIncludeBean(this.selectOrganizationIncludeBean);
    }

    @Test
    public void getModel_NotNull() {
        Marketplace model = umpb.getModel();
        assertNotNull(model);
    }

    @Test
    public void getModel_Same() {
        Marketplace model1 = umpb.getModel();
        Marketplace model2 = umpb.getModel();
        assertSame(model1, model2);
    }

    @Test
    public void getModel() {
        Marketplace model = umpb.getModel();
        assertFalse(model.isClosed());
        assertTrue(model.isTaggingEnabled());
        assertTrue(model.isReviewEnabled());
        assertTrue(model.isSocialBookmarkEnabled());
        assertTrue(model.isEditDisabled());
        assertTrue(model.isPropertiesDisabled());
        assertFalse(model.isOrganizationSelectVisible());
    }

    @Test
    public void getSelectableMarketplaces_NotNull() {
        List<SelectItem> list = umpb.getSelectableMarketplaces();
        assertNotNull(list);
        verify(msmock, times(1)).getMarketplacesOwned();
    }

    @Test
    public void getSelectableMarketplaces_Same() {
        List<SelectItem> list1 = umpb.getSelectableMarketplaces();
        verify(msmock, times(1)).getMarketplacesOwned();
        List<SelectItem> list2 = umpb.getSelectableMarketplaces();
        verifyNoMoreInteractions(msmock);
        assertSame(list1, list2);
    }

    @Test
    public void getSelectableMarketplaces() {
        List<SelectItem> list = umpb.getSelectableMarketplaces();
        verify(msmock, times(1)).getMarketplacesOwned();

        assertEquals(2, list.size());
        SelectItem item = list.get(0);
        assertEquals(vMp1.getName() + " (" + vMp1.getMarketplaceId() + ")",
                item.getLabel());
        assertEquals(vMp1.getMarketplaceId(), item.getValue());
    }

    @Test
    public void getLabel_Null() {
        String label = umpb.getLabel(null);
        assertEquals("", label);
    }

    @Test
    public void getLabel_NullName() {
        vMp1.setName(null);
        String label = umpb.getLabel(vMp1);
        assertEquals(vMp1.getMarketplaceId(), label);
    }

    @Test
    public void getLabel_EmptyName() {
        vMp1.setName("");
        String label = umpb.getLabel(vMp1);
        assertEquals(vMp1.getMarketplaceId(), label);
    }

    @Test
    public void getLabel() {
        String label = umpb.getLabel(vMp1);
        assertEquals(vMp1.getName() + " (" + vMp1.getMarketplaceId() + ")",
                label);
    }

    @Test
    public void getSelectableMarketplaces_Operator() {
        doReturn(Boolean.TRUE).when(umpb).isLoggedInAndPlatformOperator();
        List<SelectItem> list = umpb.getSelectableMarketplaces();
        verify(msmock, times(1)).getMarketplacesForOperator();
        assertEquals(2, list.size());
    }

    @Test
    public void selectedMarketplaceChanged_Empty() {
        umpb.getModel().setMarketplaceId("");
        umpb.marketplaceChanged();
        Marketplace model = umpb.getModel();
        assertNotNull(model);
        assertTrue(model.isEditDisabled());
    }

    @Test
    public void selectedMarketplaceChanged_Null() {
        umpb.getModel().setMarketplaceId(null);
        umpb.marketplaceChanged();
        Marketplace model = umpb.getModel();
        assertNotNull(model);
        assertTrue(model.isEditDisabled());
    }

    @Test
    public void selectedMarketplaceChanged_NotFound() throws Exception {
        // given
        String mpId = "fake id";
        umpb.getModel().setMarketplaceId(mpId);
        ObjectNotFoundException e = new ObjectNotFoundException(
                ClassEnum.MARKETPLACE, mpId);
        when(msmock.getMarketplaceById(anyString())).thenThrow(e);
        ArgumentCaptor<ObjectNotFoundException> onfe = ArgumentCaptor
                .forClass(ObjectNotFoundException.class);

        // when
        umpb.marketplaceChanged();

        // then
        Marketplace model = umpb.getModel();
        assertNotNull(model);
        assertTrue(model.isEditDisabled());

        verify(ui, times(1)).handleException(onfe.capture());
        assertEquals(ClassEnum.MARKETPLACE, onfe.getValue()
                .getDomainObjectClassEnum());
        assertEquals(mpId, onfe.getValue().getMessageParams()[0]);

        List<SelectItem> list = umpb.getSelectableMarketplaces();
        assertNotNull(list);

        verify(msmock, times(1)).getMarketplacesOwned();
    }

    @Test
    public void selectedMarketplaceChanged() {
        umpb.getModel().setMarketplaceId(vMp1.getMarketplaceId());
        umpb.marketplaceChanged();

        Marketplace model = umpb.getModel();

        assertNotNull(model);
        assertEquals(vMp1.getMarketplaceId(), model.getMarketplaceId());
        assertEquals(vMp1.getName(), model.getName());
        assertEquals(vMp1.getKey(), model.getKey());
        assertEquals(vMp1.getOwningOrganizationId(),
                model.getOwningOrganizationId());
        assertEquals(vMp1.getVersion(), model.getVersion());
    }

    @Test
    public void convertToMarketplacePriceModel_Null() {
        Object po = umpb.convertToMarketplacePriceModel(null);
        assertNull(po);
    }

    @Test
    public void convertToPartnerPriceModel_Null() {
        Object po = umpb.convertToPartnerPriceModel(null);
        assertNull(po);
    }

    @Test
    public void convertToModel_Null() {
        Marketplace model = umpb.convertToModel(null);
        assertNull(model);
    }

    @Test
    public void convertToModel() {
        Marketplace model = umpb.convertToModel(vMp1);
        assertNotNull(model);
        assertEquals(vMp1.getMarketplaceId(), model.getMarketplaceId());
        assertEquals(vMp1.getName(), model.getName());
        assertEquals(vMp1.getKey(), model.getKey());
        assertEquals(vMp1.getOwningOrganizationId(),
                model.getOwningOrganizationId());
        assertEquals(vMp1.getOwningOrganizationId(), model.getOriginalOrgId());
        assertEquals(vMp1.getVersion(), model.getVersion());
        assertEquals(vMp1.isOpen(), !model.isClosed());
        assertEquals(vMp1.isReviewEnabled(), model.isReviewEnabled());
        assertEquals(vMp1.isSocialBookmarkEnabled(),
                model.isSocialBookmarkEnabled());
        assertEquals(vMp1.isTaggingEnabled(), model.isTaggingEnabled());
        assertFalse(model.isOrganizationSelectVisible());
        assertFalse(model.isEditDisabled());
        assertFalse(model.isPropertiesDisabled());
        assertTrue(model.isRevenueSharesReadOnly());
    }

    @Test
    public void convertToModel_Operator() {
        doReturn(Boolean.TRUE).when(umpb).isLoggedInAndPlatformOperator();
        Marketplace model = umpb.convertToModel(vMp1);
        assertNotNull(model);
        assertTrue(model.isOrganizationSelectVisible());
        assertFalse(model.isEditDisabled());
        assertFalse(model.isPropertiesDisabled());
    }

    @Test
    public void convertToModel_OperatorNotOwner() {
        doReturn(Boolean.TRUE).when(umpb).isLoggedInAndPlatformOperator();
        Marketplace model = umpb.convertToModel(vMp2);
        assertNotNull(model);
        assertTrue(model.isOrganizationSelectVisible());
        assertFalse(model.isEditDisabled());
        assertTrue(model.isPropertiesDisabled());
    }

    @Test
    public void isMpOwner_NotOwned() {
        assertFalse(umpb.isMpOwner(vMp2));
    }

    @Test
    public void isMpOwner_removeOwnerRole() {
        assertTrue(umpb.isMpOwner(vMp1));
        when(Boolean.valueOf(usrmock.isMarketplaceOwner())).thenReturn(
                Boolean.FALSE);
        assertFalse(umpb.isMpOwner(vMp1));
    }

    @Test
    public void isMpOwner() {
        assertTrue(umpb.isMpOwner(vMp1));
    }

    @Test
    public void convertToValueObject_Null() {
        VOMarketplace voMarketplace = umpb.convertToValueObject(null);
        assertNull(voMarketplace);
    }

    @Test
    public void convertToValueObject() {
        VOMarketplace vMp = umpb.convertToValueObject(mp);
        assertNotNull(vMp);
        verifyValueObject(mp, vMp);
    }

    @Test
    public void updateMarketplace() throws Exception {
        // given
        ArgumentCaptor<VOMarketplace> captor = ArgumentCaptor
                .forClass(VOMarketplace.class);
        ArgumentCaptor<POMarketplacePriceModel> mpmCaptor = ArgumentCaptor
                .forClass(POMarketplacePriceModel.class);
        ArgumentCaptor<POPartnerPriceModel> ppmCaptor = ArgumentCaptor
                .forClass(POPartnerPriceModel.class);

        Marketplace model = umpb.getModel();
        model.setClosed(true);
        model.setKey(1234);
        String mId = "marketplaceId";
        model.setMarketplaceId(mId);
        model.setName("name");
        model.setOwningOrganizationId("owningOrganizationId");
        model.setOriginalOrgId("originalOrgId");
        model.setReviewEnabled(false);
        model.setSocialBookmarkEnabled(false);
        model.setTaggingEnabled(false);
        model.setVersion(7);
        model.setMarketplaceRevenueShare(BigDecimal.TEN);
        model.setResellerRevenueShare(BigDecimal.TEN);
        model.setBrokerRevenueShare(BigDecimal.TEN);

        // when
        umpb.updateMarketplace();

        // then:
        verify(mmps, times(1)).updateMarketplace(captor.capture(),
                mpmCaptor.capture(), ppmCaptor.capture());

        // the values passed to the service are the ones from the model
        VOMarketplace value = captor.getValue();
        verifyValueObject(model, value);
        assertEquals(model.getMarketplaceRevenueShare(), mpmCaptor.getValue()
                .getRevenueShare().getRevenueShare());
        assertEquals(model.getResellerRevenueShare(), ppmCaptor.getValue()
                .getRevenueShareResellerModel().getRevenueShare());
        assertEquals(model.getBrokerRevenueShare(), ppmCaptor.getValue()
                .getRevenueShareBrokerModel().getRevenueShare());

        // the values retrieved from the service are stored in the model
        verify(umpb, times(1)).convertToModel(
                updateMarketplaceResponse.getResult(VOMarketplace.class));
        verify(umpb, times(1)).addToModel(
                updateMarketplaceResponse
                        .getResult(POMarketplacePriceModel.class));
        verify(umpb, times(1)).addToModel(
                updateMarketplaceResponse.getResult(POPartnerPriceModel.class));

        verify(umpb, times(1)).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_INFO),
                eq(BaseBean.INFO_MARKETPLACE_SAVED), eq(new Object[] { mId }));
    }

    @Test
    public void updateMarketplace_OrgNotChanged() throws Exception {
        ArgumentCaptor<VOMarketplace> captor = ArgumentCaptor
                .forClass(VOMarketplace.class);

        Marketplace model = umpb.getModel();
        String mId = "marketplaceId";
        model.setMarketplaceId(mId);
        model.setOwningOrganizationId("owningOrganizationId");
        model.setOriginalOrgId(model.getOwningOrganizationId());

        umpb.updateMarketplace();

        verify(mmps, times(1)).updateMarketplace(captor.capture(),
                any(POMarketplacePriceModel.class),
                any(POPartnerPriceModel.class));
        VOMarketplace value = captor.getValue();
        verifyValueObject(model, value);

        verify(umpb, times(1)).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_INFO),
                eq(BaseBean.INFO_MARKETPLACE_SAVED), eq(new Object[] { mId }));
    }

    @Test
    public void updateMarketplace_NotPermitted() throws Exception {
        // given
        doThrow(new OperationNotPermittedException()).when(mmps)
                .updateMarketplace(any(VOMarketplace.class),
                        any(POMarketplacePriceModel.class),
                        any(POPartnerPriceModel.class));

        // when
        umpb.updateMarketplace();

        // then
        verify(mbMock, times(1)).resetMenuVisibility();
        assertTrue(ui.hasErrors());
    }

    /**
     * UI must be cleaned up if mp not found
     */
    @Test
    public void updateMarketplace_MarketplaceNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException(ClassEnum.MARKETPLACE, "mId"))
                .when(mmps).updateMarketplace(any(VOMarketplace.class),
                        any(POMarketplacePriceModel.class),
                        any(POPartnerPriceModel.class));

        // when
        umpb.updateMarketplace();

        // then
        verify(mbMock, times(1)).resetMenuVisibility();
        assertTrue(ui.hasErrors());
    }

    /**
     * Show only error if other object than mp is not found. no ui handling
     */
    @Test
    public void updateMarketplace_OtherObjectNotFound() throws Exception {
        // given that an item is not found that is not relevant for widgets
        doThrow(new ObjectNotFoundException()).when(mmps).updateMarketplace(
                any(VOMarketplace.class), any(POMarketplacePriceModel.class),
                any(POPartnerPriceModel.class));

        // when
        umpb.updateMarketplace();

        // then do not reset in UI
        verify(mbMock, times(0)).resetMenuVisibility();
        assertTrue(ui.hasErrors());
    }

    @Test
    public void updateMarketplace_SaasApplicationException() throws Exception {
        // given
        doThrow(new ValidationException()).when(mmps).updateMarketplace(
                any(VOMarketplace.class), any(POMarketplacePriceModel.class),
                any(POPartnerPriceModel.class));

        // when
        umpb.updateMarketplace();

        // then do not reset in UI
        verify(mbMock, times(0)).resetMenuVisibility();
        assertTrue(ui.hasErrors());
    }

    @Test
    public void applyOrgChange_empty() {
        umpb.applyOrgChange("");
        // new model will be generated with blank fields
        assertEquals(umpb.getModel().getVersion(), 0);
    }

    @Test
    public void applyOrgChange_null() {
        umpb.applyOrgChange(null);
        // new model will be generated with blank fields
        assertEquals(umpb.getModel().getVersion(), 0);
    }

    @Test
    public void applyOrgChange() {
        // given
        String mp2Id = vMp2.getMarketplaceId();
        mpPricing.getMarketplacePriceModel().getRevenueShare()
                .setRevenueShare(BigDecimal.TEN);
        mpPricing.getPartnerPriceModel().getRevenueShareResellerModel()
                .setRevenueShare(BigDecimal.ONE);
        mpPricing.getPartnerPriceModel().getRevenueShareBrokerModel()
                .setRevenueShare(BigDecimal.TEN);

        // when
        umpb.applyOrgChange(mp2Id);

        // then
        Marketplace mp = umpb.getModel();
        assertSame(mp.getName(), vMp2.getName());
        assertSame(mp.getMarketplaceId(), vMp2.getMarketplaceId());
        assertEquals(mp.getVersion(), vMp2.getVersion());
        assertSame(mp.getOwningOrganizationId(), vMp2.getOwningOrganizationId());
        assertEquals(mp.isClosed(), !vMp2.isOpen());
        assertEquals(mp.isReviewEnabled(), vMp2.isReviewEnabled());
        assertEquals(mp.isSocialBookmarkEnabled(),
                vMp2.isSocialBookmarkEnabled());
        assertEquals(mp.isTaggingEnabled(), vMp2.isTaggingEnabled());
        assertEquals(BigDecimal.TEN, mp.getMarketplaceRevenueShare());
        assertEquals(BigDecimal.ONE, mp.getResellerRevenueShare());
        assertEquals(BigDecimal.TEN, mp.getBrokerRevenueShare());
    }

    @Test
    public void applyOrgChange_ObjectNotFound() throws ObjectNotFoundException {
        // given
        String mp2Id = vMp2.getMarketplaceId();
        doThrow(new ObjectNotFoundException()).when(ps)
                .getPricingForMarketplace(anyString());

        // when
        umpb.applyOrgChange(mp2Id);

        // then
        assertNull(umpb.model);
        assertNull(umpb.selectableMarketplaces);
        assertTrue(ui.hasErrors());
    }

    /**
     * Test if the name change gets reflected in the selection list when the
     * marketplace name is updated.
     */
    @Test
    public void updateMarketplace_NameChange() throws Exception {
        VOMarketplace voMarketplace = new VOMarketplace();
        Response response = new Response(voMarketplace,
                umpb.convertToMarketplacePriceModel(umpb.getModel()),
                umpb.convertToPartnerPriceModel(umpb.getModel()));
        doReturn(response).when(mmps).updateMarketplace(
                any(VOMarketplace.class), any(POMarketplacePriceModel.class),
                any(POPartnerPriceModel.class));

        umpb.updateMarketplace();

        verify(umpb, times(1)).updateSelectionList(eq(voMarketplace),
                eq(umpb.selectableMarketplaces));
    }

    @Test
    public void updateSelectionList() {
        // given
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId("mId1");
        mp.setName("newName");
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        selectItems.add(new SelectItem("mId1", "oldName"));

        // when
        umpb.updateSelectionList(mp, selectItems);

        // then
        SelectItem si = selectItems.get(0);
        assertEquals("newName (mId1)", si.getLabel());
    }

    @Test
    public void getPricingService() {
        umpb.getPricingService();
        assertEquals(ps, umpb.pricingService);
        verify(umpb, times(1)).getService(eq(PricingService.class), any());
    }

    @Test
    public void getMarketplaceManagePartnerService() {
        umpb.getMarketplaceManagePartnerService();
        assertEquals(mmps, umpb.marketplaceManagePartnerService);
        verify(umpb, times(1)).getService(
                eq(MarketplaceServiceManagePartner.class), any());
    }
    

    private static void verifyValueObject(Marketplace mp, VOMarketplace vMp) {
        assertEquals(mp.getMarketplaceId(), vMp.getMarketplaceId());
        assertEquals(mp.getName(), vMp.getName());
        assertEquals(mp.getKey(), vMp.getKey());
        assertEquals(mp.getOwningOrganizationId(),
                vMp.getOwningOrganizationId());
        assertEquals(mp.getVersion(), vMp.getVersion());
        assertEquals(mp.isClosed(), !vMp.isOpen());
        assertEquals(mp.isReviewEnabled(), vMp.isReviewEnabled());
        assertEquals(mp.isSocialBookmarkEnabled(),
                vMp.isSocialBookmarkEnabled());
        assertEquals(mp.isTaggingEnabled(), vMp.isTaggingEnabled());
    }

}
