/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oscm.converter.PriceConverter;

/**
 * Contains the role prices for a set of containing elements like parameters for
 * a price model.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class RolePricingData {

    private final Map<Long, Map<Long, RolePricingDetails>> content = new HashMap<Long, Map<Long, RolePricingDetails>>();
    private BigDecimal costs = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private XParameterPeriodValue parent;

    void setParent(XParameterPeriodValue parent) {
        this.parent = parent;
    }

    public XParameterPeriodValue getParent() {
        return parent;
    }

    /**
     * Retrieves the role prices for one container.
     * <p>
     * For prices on priced parameter level, the key is the technical key of the
     * priced parameter objects, for options the key of the priced parameter
     * option.
     * </p>
     * 
     * @param containerKey
     *            The key of the container to retrieve the prices for.
     * @return The prices for the container as map, where the key is the
     *         technical key of the role definition and the value is the priced
     *         product role history. May be null if no role prices are defined!
     */
    public Map<Long, RolePricingDetails> getRolePricesForContainerKey(
            Long containerKey) {
        Map<Long, RolePricingDetails> result = content.get(containerKey);
        if (result == null) {
            result = new HashMap<Long, RolePricingDetails>();
        }
        return result;
    }

    /**
     * Sets the role prices for one container.
     * 
     * @param containerKey
     *            The key of the container to set the role prices for.
     * @param containerPrices
     *            The prices for the container as map, where the key is the
     *            technical key of the role definition and the value is the
     *            priced product role history.
     */
    public void addRolePricesForContainerKey(Long containerKey,
            Map<Long, RolePricingDetails> containerPrices) {
        content.put(containerKey, containerPrices);
    }

    /**
     * Returns the keys of the parameters that role pricing information is
     * currently stored for.
     * 
     * @return The keys of stored containers.
     */
    public Set<Long> getContainerKeys() {
        return content.keySet();
    }

    void addCosts(BigDecimal costs) {
        if (getParent() != null) {
            getParent().addTotalCostsForRoles(costs);
        }
        this.costs = this.costs.add(costs);
    }

    public BigDecimal getCosts() {
        return costs;
    }

    public Collection<RolePricingDetails> getAllRolePrices(Long containerKey) {
        return getRolePricesForContainerKey(containerKey).values();
    }

}
