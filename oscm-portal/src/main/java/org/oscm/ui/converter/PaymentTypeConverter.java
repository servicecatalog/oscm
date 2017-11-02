/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.converter;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.vo.VOPaymentInfo;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

@Named
@RequestScoped
public class PaymentTypeConverter implements Converter {


    private AccountService accountingService;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        VOPaymentInfo retVal = null;
        for (VOPaymentInfo vopsp : accountingService.getPaymentInfos()) {
            if ((Long.valueOf(vopsp.getKey()).toString().equals(value))) {
                retVal = vopsp;
            }
        }
        return retVal;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object object) {
        String retVal;
        if (!(object instanceof VOPaymentInfo)) {
            retVal = "";
        } else {
            retVal = String.valueOf(((VOPaymentInfo) object).getKey());
        }
        return retVal;
    }

    public AccountService getAccountingService() {
        return accountingService;
    }
    @EJB
    public void setAccountingService(AccountService accountingService) {
        this.accountingService = accountingService;
    }
}
