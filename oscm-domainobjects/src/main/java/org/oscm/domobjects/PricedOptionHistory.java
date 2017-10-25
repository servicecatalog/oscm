/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 11, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of PricedOption, used for auditing. Will be automatically
 * created during persist, save or remove operations.
 * 
 * @author PRavi
 */
@NamedQueries({
        @NamedQuery(name = "PricedOptionHistory.findActualOptionsForParameter", query = "SELECT p FROM PricedOptionHistory p WHERE p.pricedParameterObjKey=:parameterObjKey AND p.modDate < :endTimeForPeriod AND p.objVersion IN (SELECT MAX(s.objVersion) FROM PricedOptionHistory s WHERE s.objKey = p.objKey AND s.modDate < :endTimeForPeriod))"),
        @NamedQuery(name = "PricedOptionHistory.findOptionsForParameter", query = "SELECT p, poHist FROM PricedOptionHistory p, ParameterOptionHistory poHist WHERE p.pricedParameterObjKey = :pricedparameterObjKey AND p.modDate < :endTimeForPeriod AND p.parameterOptionObjKey = poHist.objKey AND poHist.dataContainer.optionId = :optionId AND p.objVersion = (SELECT MAX(s.objVersion) FROM PricedOptionHistory s WHERE s.objKey = p.objKey AND s.modDate < :endTimeForPeriod) AND poHist.objVersion = (SELECT MAX(ipoHist.objVersion) FROM ParameterOptionHistory ipoHist WHERE ipoHist.objKey = poHist.objKey AND ipoHist.modDate < :endTimeForPeriod)"),
        @NamedQuery(name = "PricedOptionHistory.findByObject", query = "select c from PricedOptionHistory c where c.objKey=:objKey order by objversion") })
@Entity
public class PricedOptionHistory extends DomainHistoryObject<PricedOptionData> {
    private static final long serialVersionUID = 1145593525937079432L;

    private long pricedParameterObjKey;

    private long parameterOptionObjKey;

    public PricedOptionHistory() {
        dataContainer = new PricedOptionData();
    }

    public PricedOptionHistory(PricedOption po) {
        super(po);
        if (po != null) {
            setPricedParameterObjKey(po.getPricedParameter().getKey());
            setObjKey(po.getKey());
            setParameterOptionObjKey(po.getParameterOptionKey());
        }
    }

    public BigDecimal getPricePerUser() {
        return dataContainer.getPricePerUser();
    }

    public void setPricePerUser(BigDecimal price) {
        dataContainer.setPricePerUser(price);
    }

    public BigDecimal getPricePerSubscription() {
        return dataContainer.getPricePerSubscription();
    }

    public void setPricePerSubscription(BigDecimal price) {
        dataContainer.setPricePerSubscription(price);
    }

    public void setPricedParameterObjKey(long pricedParameterObjKey) {
        this.pricedParameterObjKey = pricedParameterObjKey;
    }

    public long getPricedParameterObjKey() {
        return pricedParameterObjKey;
    }

    public long getParameterOptionObjKey() {
        return parameterOptionObjKey;
    }

    public void setParameterOptionObjKey(long parameterOptionObjKey) {
        this.parameterOptionObjKey = parameterOptionObjKey;
    }

}
