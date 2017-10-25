/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                      
 *                                                                              
 *  Creation Date: 13.09.2011                                                      
 *                                                                              
 *  Completion Time: 13.09.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The overall costs of the billing. Note, the class is immutable and is not
 * modified. Instead a new object is returned in the corresponding methods.
 * 
 * @author cheld
 * 
 */
public class OverallCosts {

    /**
     * The class <code>Cost</code> combines currency and amount.
     * 
     * @author cheld
     * 
     */
    public static class Cost {

        public final String currency;

        public final BigDecimal amount;

        public Cost(String currency, BigDecimal amount) {
            this.currency = currency;
            this.amount = amount;
        }
    }

    private final Map<String, BigDecimal> costs;

    private OverallCosts() {
        costs = Collections.emptyMap();
    }

    private OverallCosts(HashMap<String, BigDecimal> costs) {
        this.costs = new HashMap<String, BigDecimal>(costs);
    }

    /**
     * Constructs a new instance.
     * 
     * @return OverallCosts
     */
    public static OverallCosts newInstance() {
        return new OverallCosts();
    }

    /**
     * Adds the given amount with the given currency to the overall costs. Note,
     * this object is immutable and is not modified. Instead a new object is
     * returned.
     * 
     * @param currency
     *            the currency of the amount
     * @param amount
     *            the amount to be added
     * @return the new overall costs
     */
    public OverallCosts add(String currency, BigDecimal amount) {
        BigDecimal oldAmount = costs.get(currency);
        if (oldAmount == null) {
            oldAmount = BigDecimal.ZERO;
        }
        BigDecimal newAmount = oldAmount.add(amount);
        HashMap<String, BigDecimal> newCosts = new HashMap<String, BigDecimal>(
                costs);
        newCosts.put(currency, newAmount);
        return new OverallCosts(newCosts);
    }

    /**
     * Sets the given amount for the given currency. In case a value was
     * defined, then it will be replaced. Note, this object is immutable and is
     * not modified. Instead a new object is returned.
     * 
     * @param currency
     *            the currency of the amount
     * @param amount
     *            the amount to be set
     * @return the new overall costs
     */
    public OverallCosts set(String currency, BigDecimal amount) {
        HashMap<String, BigDecimal> newCosts = new HashMap<String, BigDecimal>(
                costs);
        newCosts.put(currency, amount);
        return new OverallCosts(newCosts);
    }

    /**
     * Returns the overall cost for the given currency.
     * 
     * @param currency
     *            the currency of the overall cost to be returned
     * @return BigDecimal
     */
    public BigDecimal get(String currency) {
        return costs.get(currency);
    }

    /**
     * Iterates over all costs for each currency.
     * 
     * @return Iterator
     */
    public Iterator<Cost> iterator() {
        final Iterator<Entry<String, BigDecimal>> i = costs.entrySet()
                .iterator();
        return new Iterator<Cost>() {

            public boolean hasNext() {
                return i.hasNext();
            }

            public Cost next() {
                Entry<String, BigDecimal> entry = i.next();
                return new Cost(entry.getKey(), entry.getValue());
            }

            public void remove() {
                i.remove();
            }
        };
    }

}
