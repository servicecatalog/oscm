/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 14.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.List;

import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Backing bean for stepped price modification
 * 
 */
public class SteppedPriceBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 412210542228092367L;

    /**
     * @param priceModel
     *            the price model with the priced parameters.
     * @return true if any priced parameter row of the given price model
     *         contains any stepped price.
     */
    public static boolean isParametersWithSteppedPrices(
            List<PricedParameterRow> list) {
        if (list == null) {
            return false;
        }
        for (PricedParameterRow row : list) {
            if (row.getSteppedPrice() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param priceModel
     *            the price model with the priced events.
     * @return true if any priced event row of the given price model contains
     *         any stepped price.
     */
    public static boolean isPricedEventsWithSteppedPrices(
            List<PricedEventRow> events) {
        if (events == null) {
            return false;
        }
        for (PricedEventRow row : events) {
            if (row.getSteppedPrice() != null) {
                return true;
            }
        }
        return false;
    }

    private int index;
    private PriceModelBean priceModelBean;
    private String type;

    public String getTypePriceModel() {
        return "typePriceModel";
    }

    public String getTypePricedEvent() {
        return "typePricedEvent";
    }

    public String getTypePricedParameter() {
        return "typePricedParameter";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public PriceModelBean getPriceModelBean() {
        return priceModelBean;
    }

    public void setPriceModelBean(PriceModelBean priceModelBean) {
        this.priceModelBean = priceModelBean;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return true if any priced parameter row of the price model bean contains
     *         any stepped price.
     */
    public boolean isParametersWithSteppedPrices() {
        return isParametersWithSteppedPrices(priceModelBean.getParameters());
    }

    /**
     * @return true if any priced event row of the price model bean contains any
     *         stepped price.
     */
    public boolean isPricedEventsWithSteppedPrices() {
        return isPricedEventsWithSteppedPrices(priceModelBean.getPricedEvents());
    }

    /**
     * Add a new stepped price to the table defined by current type.
     * 
     * @return the logical outcome OUTCOME_SUCCESS.
     */
    public String add() {

        if (getTypePriceModel().equals(type)) {
            addToPriceModel();
        } else if (getTypePricedEvent().equals(type)) {
            addToEvents();
        } else if (getTypePricedParameter().equals(type)) {
            addToParameters();
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Remove a stepped price from the table defined by current type.
     * 
     * @return the logical outcome OUTCOME_SUCCESS.
     */
    public String remove() {

        if (getTypePriceModel().equals(type)) {
            removeFromPriceModel();
        } else if (getTypePricedEvent().equals(type)) {
            removeFromEvents();
        } else if (getTypePricedParameter().equals(type)) {
            removeFromParameters();
        }

        return OUTCOME_SUCCESS;
    }

    /**
     * Add a new stepped price to the price model.
     */
    private void addToPriceModel() {

        VOPriceModel priceModel = priceModelBean.getPriceModel();
        List<VOSteppedPrice> steps = priceModelBean.getSteppedPrices();
        int addToIndex = 0;

        if (steps != null) {
            addToIndex = 1;
            if (steps.isEmpty()) {
                addToIndex = 0;
                VOSteppedPrice vo = new VOSteppedPrice();
                vo.setLimit(Long.valueOf(1));
                vo.setPrice(priceModel.getPricePerUserAssignment());
                steps.add(vo);
            }
            if (index >= 0 && index < steps.size()) {
                VOSteppedPrice row = steps.get(index);
                VOSteppedPrice vo = new VOSteppedPrice();
                vo.setPrice(row.getPrice());
                steps.add(index + 1, vo);
            }
        }

        index += addToIndex;

    }

    /**
     * Remove a stepped price from the price model.
     */
    private void removeFromPriceModel() {

        VOPriceModel priceModel = priceModelBean.getPriceModel();
        List<VOSteppedPrice> steps = priceModelBean.getSteppedPrices();

        if (steps != null && index >= 0 && index < steps.size()) {
            steps.remove(index);
            if (steps.size() == 1) {
                priceModel.setPricePerUserAssignment(steps.get(0).getPrice());
                steps.remove(0);
            }
        }

    }

    /**
     * Add a new stepped price to the priced event table.
     */
    private void addToEvents() {

        List<PricedEventRow> events = priceModelBean.getPricedEvents();
        int addToIndex = 0;

        if (index >= 0 && index < events.size()) {
            PricedEventRow row = events.get(index);
            VOPricedEvent pricedEvent = row.getPricedEvent();
            addToIndex = 1;
            if (row.getSteppedPrice() == null) {
                VOSteppedPrice sp = new VOSteppedPrice();
                sp.setLimit(Long.valueOf(1));
                sp.setPrice(row.getPricedEvent().getEventPrice());
                pricedEvent.getSteppedPrices().add(sp);
                row.setSteppedPrice(sp);
                addToIndex = 0;
            }
            PricedEventRow rowToAdd = new PricedEventRow();
            rowToAdd.setPricedEvent(pricedEvent);
            VOSteppedPrice sp = new VOSteppedPrice();
            sp.setPrice(row.getSteppedPrice().getPrice());
            int i = pricedEvent.getSteppedPrices().indexOf(
                    row.getSteppedPrice());
            pricedEvent.getSteppedPrices().add(i + 1, sp);
            rowToAdd.setSteppedPrice(sp);
            events.add(index + 1, rowToAdd);
        }

        index += addToIndex;

    }

    /**
     * Remove a stepped price from the priced event table.
     */
    private void removeFromEvents() {

        List<PricedEventRow> events = priceModelBean.getPricedEvents();

        if (index >= 0 && index < events.size()) {
            PricedEventRow row = events.get(index);
            if (row.getSteppedPrice() != null) {
                row.getPricedEvent().getSteppedPrices()
                        .remove(row.getSteppedPrice());
                events.remove(index);
                if (row.getPricedEvent().getSteppedPrices().size() == 1) {
                    if (index > 0
                            && events.get(index - 1).getPricedEvent() == row
                                    .getPricedEvent()) {
                        row = events.get(index - 1);
                    } else if (index < events.size()
                            && events.get(index).getPricedEvent() == row
                                    .getPricedEvent()) {
                        row = events.get(index);
                    }
                    VOSteppedPrice sp = row.getPricedEvent().getSteppedPrices()
                            .remove(0);
                    row.setSteppedPrice(null);
                    row.getPricedEvent().setEventPrice(sp.getPrice());
                }
            }
        }

    }

    /**
     * Add a new stepped price to the priced parameter table.
     */
    public void addToParameters() {

        List<PricedParameterRow> params = priceModelBean.getParameters();
        int addToIndex = 0;

        if (index >= 0 && index < params.size()) {
            PricedParameterRow row = params.get(index);
            VOPricedParameter pricedPram = row.getPricedParameter();
            addToIndex = 1;
            if (row.getSteppedPrice() == null) {
                VOSteppedPrice sp = new VOSteppedPrice();
                sp.setLimit(Long.valueOf(1));
                if (row.getMinValue() != null) {
                    sp.setLimit(row.getMinValue());
                }
                sp.setPrice(row.getPricedParameter().getPricePerSubscription());
                row.getPricedParameter().getSteppedPrices().add(sp);
                row.setSteppedPrice(sp);
                addToIndex = 0;
            }
            PricedParameterRow rowToAdd = new PricedParameterRow(
                    row.getParameter(), null, pricedPram, null);
            VOSteppedPrice sp = new VOSteppedPrice();
            sp.setPrice(row.getSteppedPrice().getPrice());
            int i = rowToAdd.getPricedParameter().getSteppedPrices()
                    .indexOf(row.getSteppedPrice());
            rowToAdd.getPricedParameter().getSteppedPrices().add(i + 1, sp);
            rowToAdd.setSteppedPrice(sp);
            params.add(index + 1, rowToAdd);
        }

        index += addToIndex;

    }

    /**
     * Remove a stepped price from the priced parameter table.
     */
    private void removeFromParameters() {

        List<PricedParameterRow> params = priceModelBean.getParameters();

        if (index >= 0 && index < params.size()) {
            PricedParameterRow row = params.get(index);
            if (row.getSteppedPrice() != null) {
                row.getPricedParameter().getSteppedPrices()
                        .remove(row.getSteppedPrice());
                params.remove(index);
                if (row.getPricedParameter().getSteppedPrices().size() == 1) {
                    if (index > 0
                            && params.get(index - 1).getPricedParameter() == row
                                    .getPricedParameter()) {
                        row = params.get(index - 1);
                    } else if (index < params.size()
                            && params.get(index).getPricedParameter() == row
                                    .getPricedParameter()) {
                        row = params.get(index);
                    }
                    VOSteppedPrice sp = row.getPricedParameter()
                            .getSteppedPrices().remove(0);
                    row.setSteppedPrice(null);
                    row.getPricedParameter().setPricePerSubscription(
                            sp.getPrice());
                }
            }
        }

    }
}
