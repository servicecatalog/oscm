/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.internal.pricing.PORevenueShare;

public class MarketplaceTest {

    @Test
    public void assignedOrgChanged_negative() {
        Marketplace m = new Marketplace();
        m.setOwningOrganizationId("owningOrganizationId");
        m.setOriginalOrgId(m.getOwningOrganizationId());
        assertFalse(m.assignedOrgChanged());
    }

    @Test
    public void assignedOrgChanged_null() {
        Marketplace m = new Marketplace();
        m.setOwningOrganizationId(null);
        m.setOriginalOrgId(null);
        assertFalse(m.assignedOrgChanged());
    }

    @Test
    public void assignedOrgChanged() {
        Marketplace m = new Marketplace();
        m.setOwningOrganizationId("owningOrganizationId");
        m.setOriginalOrgId("originalOrgId");
        assertTrue(m.assignedOrgChanged());
    }

    @Test
    public void assignedOrgChanged_emptt() {
        Marketplace m = new Marketplace();
        m.setOwningOrganizationId("  ");
        m.setOriginalOrgId(null);
        assertFalse(m.assignedOrgChanged());
    }

    @Test
    public void getMarketplaceRevenueShare() {
        Marketplace m = new Marketplace();
        assertNull(m.getMarketplaceRevenueShare());
    }

    @Test
    public void setMarketplaceRevenueShare() {
        Marketplace m = new Marketplace();

        m.setMarketplaceRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getMarketplaceRevenueShare());
    }

    @Test
    public void setMarketplaceRevenueShare_Null() {
        Marketplace m = new Marketplace();

        m.setMarketplaceRevenueShare(null);
        assertNull(m.getMarketplaceRevenueShare());
    }

    @Test
    public void setMarketplaceRevenueShare_NotNull() {
        Marketplace m = new Marketplace();

        m.setMarketplaceRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getMarketplaceRevenueShare());
    }

    @Test
    public void setMarketplaceRevenueShareObject_Null() {
        Marketplace m = new Marketplace();
        m.setMarketplaceRevenueShareObject(null);
        assertNull(m.getMarketplaceRevenueShareObject());
        assertNull(m.getMarketplaceRevenueShare());
    }

    @Test
    public void getResellerRevenueShare() {
        Marketplace m = new Marketplace();
        assertNull(m.getResellerRevenueShare());
    }

    @Test
    public void setResellerRevenueShare() {
        Marketplace m = new Marketplace();

        m.setResellerRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getResellerRevenueShare());
    }

    @Test
    public void setResellerRevenueShare_Null() {
        Marketplace m = new Marketplace();

        m.setResellerRevenueShare(null);
        assertNull(m.getResellerRevenueShare());
    }

    @Test
    public void setResellerRevenueShare_NotNull() {
        Marketplace m = new Marketplace();

        m.setResellerRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getResellerRevenueShare());
    }

    @Test
    public void setResellerRevenueShareObject_Null() {
        Marketplace m = new Marketplace();
        m.setResellerRevenueShareObject(null);
        assertNull(m.getResellerRevenueShareObject());
        assertNull(m.getResellerRevenueShare());
    }

    @Test
    public void getBrokerRevenueShare() {
        Marketplace m = new Marketplace();
        assertNull(m.getBrokerRevenueShare());
    }

    @Test
    public void setBrokerRevenueShare() {
        Marketplace m = new Marketplace();

        m.setBrokerRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getBrokerRevenueShare());
    }

    @Test
    public void setBrokerRevenueShare_Null() {
        Marketplace m = new Marketplace();

        m.setBrokerRevenueShare(null);
        assertNull(m.getBrokerRevenueShare());
    }

    @Test
    public void setBrokerRevenueShare_NotNull() {
        Marketplace m = new Marketplace();

        m.setBrokerRevenueShare(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, m.getBrokerRevenueShare());
    }

    @Test
    public void setBrokerRevenueShareObject_Null() {
        Marketplace m = new Marketplace();
        m.setBrokerRevenueShareObject(null);
        assertNull(m.getBrokerRevenueShareObject());
        assertNull(m.getBrokerRevenueShare());
    }

    @Test
    public void setRevenueSharesReadOnly_true() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        assertTrue(m.isRevenueSharesReadOnly());
    }

    @Test
    public void setRevenueSharesReadOnly_false() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(false);
        assertFalse(m.isRevenueSharesReadOnly());
    }

    @Test
    public void isMarketplaceRevenueSharePercentVisible() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setMarketplaceRevenueShareObject(new PORevenueShare());
        assertTrue(m.isMarketplaceRevenueShareVisible());
    }

    @Test
    public void isMarketplaceRevenueSharePercentVisible_notReadonly() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(false);
        m.setMarketplaceRevenueShareObject(new PORevenueShare());
        assertFalse(m.isMarketplaceRevenueShareVisible());
    }

    @Test
    public void isMarketplaceRevenueSharePercentVisible_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setMarketplaceRevenueShareObject(null);
        assertFalse(m.isMarketplaceRevenueShareVisible());
    }

    @Test
    public void isMarketplaceRevenueSharePercentVisible_notReadonly_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setMarketplaceRevenueShareObject(null);
        assertFalse(m.isMarketplaceRevenueShareVisible());
    }

    @Test
    public void isResellerRevenueSharePercentVisible() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setResellerRevenueShareObject(new PORevenueShare());
        assertTrue(m.isResellerRevenueShareVisible());
    }

    @Test
    public void isResellerRevenueSharePercentVisible_notReadonly() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(false);
        m.setResellerRevenueShareObject(new PORevenueShare());
        assertFalse(m.isResellerRevenueShareVisible());
    }

    @Test
    public void isResellerRevenueSharePercentVisible_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setResellerRevenueShareObject(null);
        assertFalse(m.isResellerRevenueShareVisible());
    }

    @Test
    public void isResellerRevenueSharePercentVisible_notReadonly_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setResellerRevenueShareObject(null);
        assertFalse(m.isResellerRevenueShareVisible());
    }

    @Test
    public void isBrokerRevenueSharePercentVisible() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setBrokerRevenueShareObject(new PORevenueShare());
        assertTrue(m.isBrokerRevenueShareVisible());
    }

    @Test
    public void isBrokerRevenueSharePercentVisible_notReadonly() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(false);
        m.setBrokerRevenueShareObject(new PORevenueShare());
        assertFalse(m.isBrokerRevenueShareVisible());
    }

    @Test
    public void isBrokerRevenueSharePercentVisible_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setBrokerRevenueShareObject(null);
        assertFalse(m.isBrokerRevenueShareVisible());
    }

    @Test
    public void isBrokerRevenueSharePercentVisible_notReadonly_noRevenueShare() {
        Marketplace m = new Marketplace();
        m.setRevenueSharesReadOnly(true);
        m.setBrokerRevenueShareObject(null);
        assertFalse(m.isBrokerRevenueShareVisible());
    }

}
