/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.billing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.billing.RDOParameter;
import org.oscm.reportingservice.business.model.billing.RDOSubscriptionFees;

/**
 * @author baumann
 * 
 */
public class RDOSubscriptionFeesTest {

    private RDOSubscriptionFees rdoSubscriptionFees;

    @Before
    public void setup() {
        rdoSubscriptionFees = new RDOSubscriptionFees();
    }

    @Test
    public void basePeriod() {
        // given
        final String BASE_PERIOD = "Month";
        rdoSubscriptionFees.setBasePeriod(BASE_PERIOD);

        // when
        String basePeriod = rdoSubscriptionFees.getBasePeriod();

        // then
        assertEquals("Wrong base period", BASE_PERIOD, basePeriod);
    }

    @Test
    public void calculationMode() {
        // given
        final String CALCULATION_MODE = "PRO_RATA";
        rdoSubscriptionFees.setCalculationMode(CALCULATION_MODE);

        // when
        String calculationMode = rdoSubscriptionFees.getCalculationMode();

        // then
        assertEquals("Wrong calculation mode", CALCULATION_MODE,
                calculationMode);
    }

    @Test
    public void serverTimeZone() {
        // given
        final String SERVER_TIMEZONE = "UTC+01:00";
        rdoSubscriptionFees.setServerTimeZone(SERVER_TIMEZONE);

        // when
        String serverTimeZone = rdoSubscriptionFees.getServerTimeZone();

        // then
        assertEquals("Wrong server timezone", SERVER_TIMEZONE, serverTimeZone);
    }

    @Test
    public void factor() {
        // given
        final String FACTOR = "0,88175";
        rdoSubscriptionFees.setFactor(FACTOR);

        // when
        String factor = rdoSubscriptionFees.getFactor();

        // then
        assertEquals("Wrong factor", FACTOR, factor);
    }

    @Test
    public void basePrice() {
        // given
        final String BASE_PRICE = "5,00";
        rdoSubscriptionFees.setBasePrice(BASE_PRICE);

        // when
        String basePrice = rdoSubscriptionFees.getBasePrice();

        // then
        assertEquals("Wrong base price", BASE_PRICE, basePrice);
    }

    @Test
    public void price() {
        // given
        final String PRICE = "10,00";
        rdoSubscriptionFees.setPrice(PRICE);

        // when
        String price = rdoSubscriptionFees.getPrice();

        // then
        assertEquals("Wrong price", PRICE, price);
    }

    @Test
    public void subtotalAmount() {
        // given
        final String SUBTOTAL_AMOUNT = "55,00";
        rdoSubscriptionFees.setSubtotalAmount(SUBTOTAL_AMOUNT);

        // when
        String subtotalAmount = rdoSubscriptionFees.getSubtotalAmount();

        // then
        assertEquals("Wrong subtotal amount", SUBTOTAL_AMOUNT, subtotalAmount);
    }

    @Test
    public void hideRecurringCharge() {
        // given
        final boolean HIDE_RECURRING_CHARGE = true;
        rdoSubscriptionFees.setHideRecurringCharge(HIDE_RECURRING_CHARGE);

        // when
        boolean hideRecurringCharge = rdoSubscriptionFees
                .isHideRecurringCharge();

        // then
        assertEquals("Recurring charge should be hidden",
                Boolean.valueOf(HIDE_RECURRING_CHARGE),
                Boolean.valueOf(hideRecurringCharge));
    }

    @Test
    public void hideSubscriptionFees() {
        // given
        final boolean HIDE_SUBSCRIPTION_FEES = true;
        rdoSubscriptionFees.setHideSubscriptionFees(HIDE_SUBSCRIPTION_FEES);

        // when
        boolean hideSubscriptionFees = rdoSubscriptionFees
                .isHideSubscriptionFees();

        // then
        assertEquals("Recurring charge should be hidden",
                Boolean.valueOf(HIDE_SUBSCRIPTION_FEES),
                Boolean.valueOf(hideSubscriptionFees));
    }

    @Test
    public void parameters() {
        // given
        final List<RDOParameter> PARAMETERS = new ArrayList<RDOParameter>();
        rdoSubscriptionFees.setParameters(PARAMETERS);

        // when
        List<RDOParameter> parameters = rdoSubscriptionFees.getParameters();

        // then
        assertSame("Wrong parameterlist", PARAMETERS, parameters);
    }
}
