/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar                                                
 *                                                                              
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *  Completion Time:                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.marketableservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.junit.Test;

import org.oscm.ui.model.CategoryRow;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.service.POPartner;
import org.oscm.internal.service.POServiceForPublish;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author afschar
 */
public class MarketableServicePublishModelTest {
    private final MarketableServicePublishModel m = new MarketableServicePublishModel();

    @Test
    public void getServiceDetails() {
        // when
        POServiceForPublish s = m.getServiceDetails();

        // then
        assertNull(s);
        assertFalse(m.isPartOfUpgradePath());
    }

    @Test
    public void setServiceDetails() {
        // given
        POServiceForPublish s = new POServiceForPublish();
        s.setPartOfUpgradePath(true);

        // when
        m.setServiceDetails(s);

        // then
        assertTrue(s == m.getServiceDetails());
        assertTrue(m.isPartOfUpgradePath());
    }

    @Test
    public void getSelectedServiceKey() {
        // when
        long l = m.getSelectedServiceKey();

        // then
        assertEquals(0, l);
    }

    @Test
    public void setSelectedServiceKey() {
        // when
        m.setSelectedServiceKey(4L);

        // then
        assertEquals(4L, m.getSelectedServiceKey());
    }

    @Test
    public void isDisabled() {
        // when
        boolean b = m.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_POServiceSet() {
        // given
        m.setServiceDetails(new POServiceForPublish());

        // when
        boolean b = m.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ServiceSet() {
        // given
        m.setServiceDetails(new POServiceForPublish());
        m.getServiceDetails().setService(new VOServiceDetails());

        // when
        boolean b = m.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isSaveBtnDisabled_ActiveServiceSet() {
        // given
        VOServiceDetails srvDetails = new VOServiceDetails();
        srvDetails.setStatus(ServiceStatus.ACTIVE);
        srvDetails.setKey(1L);
        POServiceForPublish poSrv = new POServiceForPublish();
        poSrv.setService(srvDetails);
        m.setServiceDetails(poSrv);

        // when
        boolean b = m.isSaveBtnDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_ActiveServiceSet() {
        // given
        VOServiceDetails srvDetails = new VOServiceDetails();
        srvDetails.setStatus(ServiceStatus.ACTIVE);
        srvDetails.setKey(1L);
        POServiceForPublish poSrv = new POServiceForPublish();
        poSrv.setService(srvDetails);
        m.setServiceDetails(poSrv);

        // when
        boolean b = m.isDisabled();

        // then
        assertTrue(b);
    }

    @Test
    public void isDisabled_PositiveTest() {
        // given
        m.setServiceDetails(new POServiceForPublish());
        m.getServiceDetails().setService(new VOServiceDetails());
        m.getServiceDetails().getService().setKey(4L);

        // when
        boolean b = m.isDisabled();

        // then
        assertFalse(b);
    }

    @Test
    public void getCategorySelection() {
        // given

        // when
        List<CategoryRow> s = m.getCategorySelection();

        // then
        assertNull(s);
    }

    @Test
    public void initializeMarketplaceCategories() {
        // given

        // when
        m.initializeMarketplaceCategories(null);

        // then
        assertNotNull(m.getCategorySelection());
        assertTrue(m.getCategorySelection().isEmpty());
    }

    private List<VOCategory> getVOCategoryList() {
        final List<VOCategory> list = new ArrayList<VOCategory>();
        list.add(new VOCategory());
        list.get(0).setCategoryId("xyz");
        list.get(0).setKey(1L);
        list.add(new VOCategory());
        list.get(1).setCategoryId("abc");
        list.get(1).setKey(2L);
        return list;
    }

    @Test
    public void initializeMarketplaceCategories_WithData() {
        // given

        // when
        m.initializeMarketplaceCategories(getVOCategoryList());

        // then
        assertEquals(2, m.getCategorySelection().size());
        assertFalse(m.getCategorySelection().get(0).isSelected());
        assertFalse(m.getCategorySelection().get(1).isSelected());
    }

    @Test
    public void initializeMarketplaceCategories_WithDataAndSelectedRow() {
        // given
        final List<VOCategory> list = getVOCategoryList();
        m.setServiceDetails(new POServiceForPublish());
        m.getServiceDetails().setCatalogEntry(new VOCatalogEntry());
        m.getServiceDetails().getCatalogEntry().setCategories(list);

        // when
        m.initializeMarketplaceCategories(list);

        // then
        assertEquals(2, m.getCategorySelection().size());
        assertTrue(m.getCategorySelection().get(0).isSelected());
        assertTrue(m.getCategorySelection().get(1).isSelected());
    }

    @Test
    public void getBrokers() {
        // given
        m.setBrokers(Arrays.asList(new POPartner()));

        // when
        List<POPartner> s = m.getBrokers();

        // then
        assertNotNull(s);
        assertEquals(1, s.size());
    }

    @Test
    public void getResellers() {
        // given
        m.setResellers(Arrays.asList(new POPartner()));

        // when
        List<POPartner> s = m.getResellers();

        // then
        assertNotNull(s);
        assertEquals(1, s.size());
    }

    @Test
    public void getInitialMarketplaceId() {
        // given
        m.setInitialMarketplaceId("xy");

        // when
        String s = m.getInitialMarketplaceId();

        // then
        assertEquals("xy", s);
    }

    @Test
    public void getOperatorPriceModel() {
        // given
        POOperatorPriceModel expected = new POOperatorPriceModel();
        m.setOperatorPriceModel(expected);

        // when
        POOperatorPriceModel s = m.getOperatorPriceModel();

        // then
        assertEquals(expected, s);
    }

    @Test
    public void getMarketplacePartnerPriceModel() {
        // given
        POPartnerPriceModel expected = new POPartnerPriceModel();
        m.setMarketplacePartnerPriceModel(expected);

        // when
        POPartnerPriceModel s = m.getMarketplacePartnerPriceModel();

        // then
        assertEquals(expected, s);
    }

    @Test
    public void getMarketplacePriceModel() {
        // given
        POMarketplacePriceModel expected = new POMarketplacePriceModel();
        m.setMarketplacePriceModel(expected);

        // when
        POMarketplacePriceModel s = m.getMarketplacePriceModel();

        // then
        assertEquals(expected, s);
    }

    @Test
    public void getServicePartnerPriceModel() {
        // given
        POPartnerPriceModel expected = new POPartnerPriceModel();
        m.setServicePartnerPriceModel(expected);

        // when
        POPartnerPriceModel s = m.getServicePartnerPriceModel();

        // then
        assertEquals(expected, s);
    }

    @Test
    public void getServiceSpecificBrokerRevenueShare_Default() {
        // given an empty service-specific broker revenue share
        // and a non-empty marketplace-specific broker revenue share
        POPartnerPriceModel servicePartnerPriceModel = new POPartnerPriceModel();
        m.setServicePartnerPriceModel(servicePartnerPriceModel);

        createMarketplaceSpecificBrokerRevenueShare(new BigDecimal("10"));

        // when
        PORevenueShare result = m.getServiceSpecificBrokerRevenueShare();

        // then the marketplace-specific revenue share is returned
        assertEquals(new BigDecimal("10"), result.getRevenueShare());

    }

    @Test
    public void getServiceSpecificBrokerRevenueShare_ServiceSpecific() {
        // given a non-empty service-specific broker revenue share
        // and a non-empty marketplace-specific broker revenue share
        createMarketplaceSpecificBrokerRevenueShare(new BigDecimal("10"));
        createServiceSpecificBrokerRevenueShare(new BigDecimal("20"));

        // when
        PORevenueShare result = m.getServiceSpecificBrokerRevenueShare();

        // then the service-specific revenue share is returned
        assertEquals(new BigDecimal("20"), result.getRevenueShare());

    }

    @Test
    public void getServiceSpecificResellerRevenueShare_Default() {
        // given an empty service-specific reseller revenue share
        // and a non-empty marketplace-specific reseller revenue share
        POPartnerPriceModel servicePartnerPriceModel = new POPartnerPriceModel();
        m.setServicePartnerPriceModel(servicePartnerPriceModel);

        createMarketplaceSpecificResellerRevenueShare(new BigDecimal("10"));

        // when
        PORevenueShare result = m.getServiceSpecificResellerRevenueShare();

        // then the marketplace-specific revenue share is returned
        assertEquals(new BigDecimal("10"), result.getRevenueShare());

    }

    @Test
    public void getServiceSpecificResellerRevenueShare_ServiceSpecific() {
        // given a non-empty service-specific broker revenue share
        // and a non-empty marketplace-specific broker revenue share
        createMarketplaceSpecificResellerRevenueShare(new BigDecimal("10"));
        createServiceSpecificResellerRevenueShare(new BigDecimal("20"));

        // when
        PORevenueShare result = m.getServiceSpecificResellerRevenueShare();

        // then the service-specific revenue share is returned
        assertEquals(new BigDecimal("20"), result.getRevenueShare());

    }

    @Test
    public void setServiceTemplates() {
        // given
        List<SelectItem> expected = new ArrayList<SelectItem>();
        // when
        m.setServiceTemplates(expected);
        // then
        assertEquals(expected, m.getServiceTemplates());
    }

    @Test
    public void setSupplier() {
        // when
        m.setSupplier(true);
        // then
        assertTrue(m.isSupplier());
    }

    @Test
    public void setOperatorShareVisible() {
        // when
        m.setOperatorShareVisible(true);
        // then
        assertTrue(m.isOperatorShareVisible());
    }

    @Test
    public void setBrokerShareVisible() {
        // when
        m.setBrokerShareVisible(true);
        // then
        assertTrue(m.isBrokerShareVisible());
    }

    @Test
    public void setResellerShareVisible() {
        // when
        m.setResellerShareVisible(true);
        // then
        assertTrue(m.isResellerShareVisible());
    }

    @Test
    public void isConfirmationRequired() {
        // when
        boolean confirmationRequired = m.isConfirmationRequired();
        // then
        assertFalse(confirmationRequired);
    }

    @Test
    public void isConfirmationRequired_MarketplaceNotChanged() {
        POServiceForPublish service = new POServiceForPublish();
        service.setPartOfUpgradePath(true);
        service.setMarketplaceId("mId");
        m.setServiceDetails(service);
        m.setInitialMarketplaceId("mId");

        // when
        boolean confirmationRequired = m.isConfirmationRequired();
        // then
        assertFalse(confirmationRequired);
    }

    @Test
    public void isConfirmationRequired_MarketplaceChanged() {
        POServiceForPublish service = new POServiceForPublish();
        service.setPartOfUpgradePath(true);
        service.setMarketplaceId("mId");
        m.setServiceDetails(service);
        m.setInitialMarketplaceId("mId2");

        // when
        boolean confirmationRequired = m.isConfirmationRequired();
        // then
        assertTrue(confirmationRequired);
    }

    @Test
    public void isConfirmationRequired_NotPartOfUpgradePath() {
        POServiceForPublish service = new POServiceForPublish();
        service.setPartOfUpgradePath(false);
        service.setMarketplaceId("mId");
        m.setServiceDetails(service);
        m.setInitialMarketplaceId("mId");

        // when
        boolean confirmationRequired = m.isConfirmationRequired();
        // then
        assertFalse(confirmationRequired);
    }

    private void createMarketplaceSpecificBrokerRevenueShare(
            BigDecimal revenueShare) {
        PORevenueShare mplSpecificRevenueShare = new PORevenueShare();
        mplSpecificRevenueShare.setRevenueShare(revenueShare);

        POPartnerPriceModel mplPartnerPriceModel = new POPartnerPriceModel();
        mplPartnerPriceModel
                .setRevenueShareBrokerModel(mplSpecificRevenueShare);

        m.setMarketplacePartnerPriceModel(mplPartnerPriceModel);
    }

    private void createMarketplaceSpecificResellerRevenueShare(
            BigDecimal revenueShare) {
        PORevenueShare mplSpecificRevenueShare = new PORevenueShare();
        mplSpecificRevenueShare.setRevenueShare(revenueShare);

        POPartnerPriceModel mplPartnerPriceModel = new POPartnerPriceModel();
        mplPartnerPriceModel
                .setRevenueShareResellerModel(mplSpecificRevenueShare);

        m.setMarketplacePartnerPriceModel(mplPartnerPriceModel);
    }

    private void createServiceSpecificBrokerRevenueShare(BigDecimal revenueShare) {
        PORevenueShare serviceSpecificRevenueShare = new PORevenueShare();
        serviceSpecificRevenueShare.setRevenueShare(revenueShare);

        POPartnerPriceModel servicePartnerPriceModel = new POPartnerPriceModel();
        servicePartnerPriceModel
                .setRevenueShareBrokerModel(serviceSpecificRevenueShare);

        m.setServicePartnerPriceModel(servicePartnerPriceModel);
    }

    private void createServiceSpecificResellerRevenueShare(
            BigDecimal revenueShare) {
        PORevenueShare serviceSpecificRevenueShare = new PORevenueShare();
        serviceSpecificRevenueShare.setRevenueShare(revenueShare);

        POPartnerPriceModel servicePartnerPriceModel = new POPartnerPriceModel();
        servicePartnerPriceModel
                .setRevenueShareResellerModel(serviceSpecificRevenueShare);

        m.setServicePartnerPriceModel(servicePartnerPriceModel);
    }

}
