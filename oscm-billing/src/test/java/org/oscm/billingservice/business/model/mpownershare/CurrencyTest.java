/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 31, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.mpownershare.Currency;
import org.oscm.billingservice.business.model.mpownershare.Marketplace;
import org.oscm.billingservice.business.model.mpownershare.Organization;

public class CurrencyTest {

    private Currency currency;
    private Currency newCurrency;
    private Marketplace newMarketplace;

    @Before
    public void setup() {
        currency = Util.createCurrency();
    }

    @Test
    public void calculate_noMarketplace() {
        // given see setup

        // when
        currency.calculate();

        // then no exception
    }

    @Test
    public void calculate_oneMarketplace() {

        // given see setup
        newCurrency = Util.createCurrency();
        newMarketplace = Util.setupMarketplace(newCurrency, 1);

        newCurrency.addMarketplace(newMarketplace);
        // when
        newCurrency.calculate();

        // then
        // amount broker revenue for all marketplaces
        List<Organization> mpsOrgBroker = newCurrency
                .getRevenuesOverAllMarketplaces().getBrokers()
                .getOrganization();
        assertNotNull(mpsOrgBroker);
        assertEquals(1, mpsOrgBroker.size());
        assertEquals(newMarketplace.getRevenuesPerMarketplace().getBrokers()
                .getOrganization(Util.getBrokerId(1)).getAmount(), mpsOrgBroker
                .get(0).getAmount());

        // amount resellers revenue for all marketplaces
        List<Organization> mpsOrgReseller = newCurrency
                .getRevenuesOverAllMarketplaces().getResellers()
                .getOrganization();
        assertNotNull(mpsOrgReseller);
        assertEquals(1, mpsOrgReseller.size());
        assertEquals(newMarketplace.getRevenuesPerMarketplace().getResellers()
                .getOrganization(Util.getResellerId(1)).getAmount(),
                mpsOrgReseller.get(0).getAmount());

        // amount suppliers revenue for all marketplaces
        List<Organization> mpsOrgSuppliers = newCurrency
                .getRevenuesOverAllMarketplaces().getSuppliers()
                .getOrganization();
        assertNotNull(mpsOrgSuppliers);
        assertEquals(1, mpsOrgSuppliers.size());
        assertEquals(newMarketplace.getRevenuesPerMarketplace().getSuppliers()
                .getOrganization(Util.getSupplierId(1)).getAmount(),
                mpsOrgSuppliers.get(0).getAmount());

        // amount marketplace owner revenue for this marketplace
        assertEquals(newMarketplace.getRevenuesPerMarketplace()
                .getMarketplaceOwner().getAmount(), newCurrency
                .getRevenuesOverAllMarketplaces().getMarketplaceOwner()
                .getAmount());
    }

    @Test
    public void calculate_twoMarketplaces() {
        // given see setup
        newCurrency = Util.createCurrency();
        Marketplace newMarketplace1 = Util.setupMarketplace(newCurrency, 2);
        Marketplace newMarketplace2 = Util.setupMarketplace(newCurrency, 1);
        newCurrency.addMarketplace(newMarketplace1);
        newCurrency.addMarketplace(newMarketplace2);
        // when
        newCurrency.calculate();

        // then
        // amount broker revenue for all marketplaces
        List<Organization> mpsOrgBroker = newCurrency
                .getRevenuesOverAllMarketplaces().getBrokers()
                .getOrganization();
        assertNotNull(mpsOrgBroker);
        assertEquals(1, mpsOrgBroker.size());
        BigDecimal expected1 = newMarketplace1.getRevenuesPerMarketplace()
                .getBrokers().getOrganization(Util.getBrokerId(1)).getAmount();
        BigDecimal expected2 = newMarketplace2.getRevenuesPerMarketplace()
                .getBrokers().getOrganization(Util.getBrokerId(1)).getAmount();
        assertEquals(expected1.add(expected2), mpsOrgBroker.get(0).getAmount());

        // amount resellers revenue for all marketplaces
        List<Organization> mpsOrgReseller = newCurrency
                .getRevenuesOverAllMarketplaces().getResellers()
                .getOrganization();
        assertNotNull(mpsOrgReseller);
        assertEquals(1, mpsOrgReseller.size());
        expected1 = newMarketplace1.getRevenuesPerMarketplace().getResellers()
                .getOrganization(Util.getResellerId(1)).getAmount();
        expected2 = newMarketplace2.getRevenuesPerMarketplace().getResellers()
                .getOrganization(Util.getResellerId(1)).getAmount();
        assertEquals(expected1.add(expected2), mpsOrgReseller.get(0)
                .getAmount());

        // amount suppliers revenue for all marketplaces
        List<Organization> mpsOrgSuppliers = newCurrency
                .getRevenuesOverAllMarketplaces().getSuppliers()
                .getOrganization();
        assertNotNull(mpsOrgSuppliers);
        assertEquals(1, mpsOrgSuppliers.size());
        expected1 = newMarketplace1.getRevenuesPerMarketplace().getSuppliers()
                .getOrganization(Util.getSupplierId(1)).getAmount();
        expected2 = newMarketplace2.getRevenuesPerMarketplace().getSuppliers()
                .getOrganization(Util.getSupplierId(1)).getAmount();
        assertEquals(expected1.add(expected2), mpsOrgSuppliers.get(0)
                .getAmount());

        // amount marketplace owner revenue for this marketplace
        expected1 = newMarketplace1.getRevenuesPerMarketplace()
                .getMarketplaceOwner().getAmount();
        expected2 = newMarketplace2.getRevenuesPerMarketplace()
                .getMarketplaceOwner().getAmount();
        assertEquals(expected1.add(expected2), newCurrency
                .getRevenuesOverAllMarketplaces().getMarketplaceOwner()
                .getAmount());
    }
}
