/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 25.10.2010                                                      
 *                                                                              
 *  Completion Time: 25.10.2010                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.BigDecimalComparator;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.test.stubs.DataServiceStub;

/**
 * Tests for the price model handler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PriceModelHandlerTest {

    private DataServiceStub dm;
    private PriceModel priceModel;
    private PriceModelHandler priceModelHandler;
    private static final Long time = Long.valueOf(10);

    private List<DomainObject<?>> removedObjects = new ArrayList<DomainObject<?>>();

    @Before
    public void setUp() throws Exception {
        dm = new DataServiceStub() {
            @Override
            public void remove(DomainObject<?> obj) {
                removedObjects.add(obj);
            }
        };
        priceModel = new PriceModel();
        priceModel.setOneTimeFee(BigDecimal.ONE);
        priceModel.setPricePerPeriod(BigDecimal.valueOf(2));
        priceModel.setPricePerUserAssignment(BigDecimal.valueOf(3));
        priceModel.setCurrency(new SupportedCurrency());

        priceModelHandler = new PriceModelHandler(dm, priceModel, 10);
    }

    @Test
    public void testResetToNonChargeable_PrimitiveFields() throws Exception {
        priceModel = priceModelHandler.resetToNonChargeable(PriceModelType.FREE_OF_CHARGE);
        assertFalse(priceModel.isChargeable());
        Assert.assertTrue(BigDecimalComparator.isZero(priceModel.getOneTimeFee()));
        Assert.assertTrue(BigDecimalComparator.isZero(priceModel.getPricePerPeriod()));
        Assert.assertTrue(BigDecimalComparator.isZero(priceModel
                .getPricePerUserAssignment()));
        assertNull(priceModel.getCurrency());
    }

    @Test
    public void testResetToNonChargeable_Events() throws Exception {
        PricedEvent evt = new PricedEvent();
        List<PricedEvent> pricedEvents = new ArrayList<PricedEvent>();
        pricedEvents.add(evt);
        priceModel.setConsideredEvents(pricedEvents);

        SteppedPrice steppedPrice = new SteppedPrice();
        evt.setSteppedPrices(Collections.singletonList(steppedPrice));

        priceModel = priceModelHandler.resetToNonChargeable(PriceModelType.FREE_OF_CHARGE);
        List<PricedEvent> consideredEvents = priceModel.getConsideredEvents();
        assertTrue(consideredEvents.isEmpty());
        assertEquals(1, removedObjects.size());
        assertEquals(time, evt.getHistoryModificationTime());
        assertEquals(time, steppedPrice.getHistoryModificationTime());
    }

    @Test
    public void testResetToNonChargeable_PriceModelLists() throws Exception {
        SteppedPrice steppedPrice = new SteppedPrice();
        List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
        steppedPrices.add(steppedPrice);
        priceModel.setSteppedPrices(steppedPrices);

        PricedProductRole ppr = new PricedProductRole();
        List<PricedProductRole> rolePrices = new ArrayList<PricedProductRole>();
        rolePrices.add(ppr);
        priceModel.setRoleSpecificUserPrices(rolePrices);

        priceModel = priceModelHandler.resetToNonChargeable(PriceModelType.FREE_OF_CHARGE);
        steppedPrices = priceModel.getSteppedPrices();
        List<PricedProductRole> roleSpecificUserPrices = priceModel
                .getRoleSpecificUserPrices();
        assertTrue(steppedPrices.isEmpty());
        assertTrue(roleSpecificUserPrices.isEmpty());
        assertEquals(2, removedObjects.size());
        assertEquals(time, ppr.getHistoryModificationTime());
        assertEquals(time, steppedPrice.getHistoryModificationTime());
    }

    @Test
    public void testResetToNonChargeable_Parameters() throws Exception {
        SteppedPrice steppedPrice = new SteppedPrice();
        List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
        steppedPrices.add(steppedPrice);

        PricedProductRole ppr = new PricedProductRole();
        List<PricedProductRole> rolePrices = new ArrayList<PricedProductRole>();
        rolePrices.add(ppr);

        PricedParameter param = new PricedParameter();
        List<PricedParameter> params = new ArrayList<PricedParameter>();
        params.add(param);
        priceModel.setSelectedParameters(params);

        param.setRoleSpecificUserPrices(rolePrices);
        param.setSteppedPrices(steppedPrices);

        PricedOption option = new PricedOption();
        List<PricedOption> options = new ArrayList<PricedOption>();
        options.add(option);
        option.setRoleSpecificUserPrices(rolePrices);
        param.setPricedOptionList(options);

        priceModel = priceModelHandler.resetToNonChargeable(PriceModelType.FREE_OF_CHARGE);

        steppedPrices = priceModel.getSteppedPrices();
        List<PricedProductRole> roleSpecificUserPrices = priceModel
                .getRoleSpecificUserPrices();
        assertTrue(steppedPrices.isEmpty());
        assertTrue(roleSpecificUserPrices.isEmpty());
        assertEquals(1, removedObjects.size());
        assertEquals(time, ppr.getHistoryModificationTime());
        assertEquals(time, steppedPrice.getHistoryModificationTime());
        assertEquals(time, option.getHistoryModificationTime());
        assertEquals(time, param.getHistoryModificationTime());
    }

}
