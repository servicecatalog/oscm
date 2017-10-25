/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.math.BigDecimal;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.internal.types.enumtypes.PriceModelType;

/**
 * Auxiliary class to modify price model settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PriceModelHandler {

    private DataService mgr;
    private PriceModel priceModel;
    private Long txnTime;

    /**
     * Creates a new instance of this object, using the DataManager reference
     * for all modifications on the price model. All operations must be
     * performed in one transaction, the caller has to take care that this
     * condition is met.
     * 
     * @param mgr
     *            The data manager to manipulate the data.
     * @param priceModel
     *            The price model to be modified.
     * @param txnTime
     *            The time the txn takes place, will be considered as
     *            modification time for all objects.
     */
    public PriceModelHandler(DataService mgr, PriceModel priceModel,
            long txnTime) {
        this.mgr = mgr;
        this.priceModel = priceModel;
        this.txnTime = Long.valueOf(txnTime);
    }

    /**
     * Sets all values back to null, setting the chargeable information to
     * false. As modifications to the list of priced events have no effect,
     * these objects stay untouched. Prices of selected parameters and if
     * existing their options will be set to 0.
     * 
     * @return The modified price model.
     */
    public PriceModel resetToNonChargeable(PriceModelType priceModelType) {

        priceModel.setType(priceModelType);
        priceModel.setPeriod(null);
        priceModel.setPricePerPeriod(BigDecimal.ZERO);
        priceModel.setPricePerUserAssignment(BigDecimal.ZERO);
        priceModel.setCurrency(null);
        priceModel.setOneTimeFee(BigDecimal.ZERO);

        // handle events
        List<PricedEvent> events = priceModel.getConsideredEvents();
        for (PricedEvent pricedEvent : events) {
            pricedEvent.setHistoryModificationTime(txnTime);
            List<SteppedPrice> steppedPrices = pricedEvent.getSteppedPrices();
            for (SteppedPrice price : steppedPrices) {
                price.setHistoryModificationTime(txnTime);
            }
            mgr.remove(pricedEvent);
        }
        events.clear();

        // handle parameters
        List<PricedParameter> selectedParameters = priceModel
                .getSelectedParameters();
        for (PricedParameter pricedParameter : selectedParameters) {
            pricedParameter.setHistoryModificationTime(txnTime);
            List<SteppedPrice> steppedPrices = pricedParameter
                    .getSteppedPrices();
            for (SteppedPrice price : steppedPrices) {
                price.setHistoryModificationTime(txnTime);
            }
            List<PricedProductRole> roleSpecificUserPrices = pricedParameter
                    .getRoleSpecificUserPrices();
            for (PricedProductRole price : roleSpecificUserPrices) {
                price.setHistoryModificationTime(txnTime);
            }

            List<PricedOption> pricedOptions = pricedParameter
                    .getPricedOptionList();
            for (PricedOption option : pricedOptions) {
                option.setHistoryModificationTime(txnTime);
                List<PricedProductRole> rolePrices = option
                        .getRoleSpecificUserPrices();
                for (PricedProductRole rolePrice : rolePrices) {
                    rolePrice.setHistoryModificationTime(txnTime);
                }
            }
            mgr.remove(pricedParameter);
        }
        selectedParameters.clear();

        // handle stepped prices
        List<SteppedPrice> steppedPrices = priceModel.getSteppedPrices();
        for (SteppedPrice steppedPrice : steppedPrices) {
            steppedPrice.setHistoryModificationTime(txnTime);
            mgr.remove(steppedPrice);
        }
        steppedPrices.clear();

        // handle role prices
        List<PricedProductRole> rolePrices = priceModel
                .getRoleSpecificUserPrices();
        for (PricedProductRole rolePrice : rolePrices) {
            rolePrice.setHistoryModificationTime(txnTime);
            mgr.remove(rolePrice);
        }
        rolePrices.clear();

        return priceModel;
    }

}
