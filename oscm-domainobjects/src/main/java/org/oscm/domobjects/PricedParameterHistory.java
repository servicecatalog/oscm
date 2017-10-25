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
 * History-Object of PricedParameter, used for auditing. Will be automatically
 * created during persist, save or remove operations.
 * 
 * @author PRavi
 */
@NamedQueries({
        @NamedQuery(name = "PricedParameterHistory.findActualPricedParametersForPriceModel", query = "SELECT p FROM PricedParameterHistory p WHERE p.priceModelObjKey=:priceModelObjKey AND p.modDate < :endTimeForPeriod AND p.objVersion IN (SELECT MAX(sp.objVersion) FROM PricedParameterHistory sp WHERE sp.objKey = p.objKey AND sp.modDate < :endTimeForPeriod))"),
        @NamedQuery(name = "PricedParameterHistory.findParameterDataForPriceModelAndPeriod", query = "SELECT pph, paramHist, pdh, pmh FROM PricedParameterHistory pph, ParameterHistory paramHist, ParameterDefinitionHistory pdh, PriceModelHistory pmh WHERE pph.priceModelObjKey = :pmKey AND pph.parameterObjKey = paramHist.objKey AND pdh.objKey = paramHist.parameterDefinitionObjKey AND pph.priceModelObjKey = pmh.objKey AND ((paramHist.modDate >= :startTime AND paramHist.modDate <= :endTime AND (paramHist.objVersion = (SELECT MAX(iph.objVersion) FROM ParameterHistory iph WHERE iph.objKey = paramHist.objKey AND iph.modDate = paramHist.modDate))) OR (paramHist.objVersion = (SELECT MAX(iph.objVersion) FROM ParameterHistory iph WHERE iph.objKey = paramHist.objKey AND iph.modDate < :startTime))) AND (pph.objVersion = (SELECT MAX(ipph.objVersion) FROM PricedParameterHistory ipph WHERE pph.objKey = ipph.objKey AND ipph.modDate <= :endTime)) AND (pdh.objVersion = (SELECT MAX(ipdh.objVersion) FROM ParameterDefinitionHistory ipdh WHERE pdh.objKey = ipdh.objKey AND ipdh.modDate <= :endTime)) AND pmh.objVersion = (SELECT MAX(ipmh.objVersion) FROM PriceModelHistory ipmh WHERE pmh.objKey = ipmh.objKey AND ipmh.modDate <= :endTime) ORDER BY pph.parameterObjKey ASC, paramHist.modDate DESC, pph.modDate DESC"),
        @NamedQuery(name = "PricedParameterHistory.findByObject", query = "select c from PricedParameterHistory c where c.objKey=:objKey order by objversion") })
@Entity
public class PricedParameterHistory extends
        DomainHistoryObject<PricedParameterData> {

    private static final long serialVersionUID = -1611153150337984808L;

    private long priceModelObjKey;

    private long parameterObjKey;

    public PricedParameterHistory() {
        dataContainer = new PricedParameterData();
    }

    public PricedParameterHistory(PricedParameter c) {
        super(c);
        if (c.getPriceModel() != null) {
            setPriceModelObjKey(c.getPriceModel().getKey());
        }
        if (c.getParameter() != null) {
            setParameterObjKey(c.getParameter().getKey());
        }
    }

    public long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    public void setPriceModelObjKey(long priceModelKey) {
        priceModelObjKey = priceModelKey;
    }

    public long getParameterObjKey() {
        return parameterObjKey;
    }

    public void setParameterObjKey(long parameterKey) {
        parameterObjKey = parameterKey;
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

}
