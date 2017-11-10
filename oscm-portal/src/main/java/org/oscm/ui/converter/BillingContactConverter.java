/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 21.01.15 14:17
 *
 * ******************************************************************************
 */

package org.oscm.ui.converter;

import org.oscm.ui.beans.BillingContactBean;
import org.oscm.internal.vo.VOBillingContact;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by ChojnackiD on 2015-01-21.
 */
@Named
@RequestScoped
public class BillingContactConverter implements Converter {

    @Inject
    private BillingContactBean billingContactBean;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        VOBillingContact retVal = null;
        for (VOBillingContact vopsp : billingContactBean.getBillingContacts()) {
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
        if (!(object instanceof VOBillingContact)) {
            retVal = "";
        } else {
            retVal = String.valueOf(((VOBillingContact) object).getKey());
        }
        return retVal;
    }

    public BillingContactBean getBillingContactBean() {
        return billingContactBean;
    }

    public void setBillingContactBean(BillingContactBean billingContactBean) {
        this.billingContactBean = billingContactBean;
    }
}
