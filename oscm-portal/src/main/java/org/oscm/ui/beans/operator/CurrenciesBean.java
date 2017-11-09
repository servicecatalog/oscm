/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: 31.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Bean for managing currencies settings.
 * 
 * @author tokoda
 * 
 */
@ViewScoped
@ManagedBean(name="currenciesBean")
public class CurrenciesBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = 1601400574897971498L;

    private String currencyToManage;
    private List<String> supportedCurrencies;

    public String getCurrencyToManage() {
        return currencyToManage;
    }

    public void setCurrencyToManage(String currencyToManage) {
        this.currencyToManage = currencyToManage;
    }

    public List<String> getSupportedCurrencies() {
        if (supportedCurrencies == null) {
            supportedCurrencies = getProvisioningService()
                    .getSupportedCurrencies();
        }
        return supportedCurrencies;
    }

    public String add() throws ValidationException,
            OrganizationAuthoritiesException {

        if (currencyToManage == null || currencyToManage.trim().length() == 0) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_PARAMETER_VALUE_MANDATORY);
            return OUTCOME_ERROR;
        }
        getOperatorService().addCurrency(currencyToManage);
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_CURRENCIES_ADDED,
                currencyToManage);
        currencyToManage = null;
        supportedCurrencies = null;

        return BaseBean.OUTCOME_SUCCESS;
    }
}
