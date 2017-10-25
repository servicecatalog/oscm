/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 14, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.List;

/**
 * @author zhaoh
 * 
 */
public class RDOPaymentPreviewSummary extends RDOSummary {

    private static final long serialVersionUID = 7649594923007187647L;

    private List<RDOPriceModel> priceModels;

    /**
     * @return the priceModel
     */
    public List<RDOPriceModel> getPriceModels() {
        return priceModels;
    }

    /**
     * @param priceModels
     *            the priceModels to set
     */
    public void setPriceModels(List<RDOPriceModel> priceModels) {
        this.priceModels = priceModels;
    }
}
